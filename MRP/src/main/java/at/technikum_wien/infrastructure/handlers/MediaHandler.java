package at.technikum_wien.infrastructure.handlers;

import at.technikum_wien.application.services.MediaService;
import at.technikum_wien.domain.entities.Media;
import at.technikum_wien.infrastructure.security.AuthHelper;
import at.technikum_wien.infrastructure.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MediaHandler implements HttpHandler {
    private final MediaService mediaService;

    public MediaHandler(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        String requestBody = "";
        if ("POST".equals(method) || "PUT".equals(method)) {
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        }

        try {
            if (path.equals("/api/media")) {
                if (query != null && !query.isEmpty()) {
                    handleSearchMedia(exchange, query);
                } else {
                    handleMediaCollection(exchange, method, requestBody);
                }
            } else if (path.matches("/api/media/\\d+")) {
                handleSingleMedia(exchange, method, path, requestBody);
            } else if (path.matches("/api/media/\\d+/rate")) {
                handleRateMedia(exchange, path, requestBody);
            } else if (path.matches("/api/media/\\d+/favorite")) {
                handleFavoriteMedia(exchange, method, path);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleMediaCollection(HttpExchange exchange, String method, String requestBody) throws IOException {
        switch (method) {
            case "GET":
                handleGetAllMedia(exchange);
                break;
            case "POST":
                handleCreateMedia(exchange, requestBody);
                break;
            default:
                sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }

    private void handleSearchMedia(HttpExchange exchange, String query) throws IOException {
        try {
            // Parse query parameters
            Map<String, String> queryParams = parseQueryString(query);

            String title = queryParams.get("title");
            String genre = queryParams.get("genre");
            String mediaType = queryParams.get("mediaType");
            Integer releaseYear = queryParams.get("releaseYear") != null ?
                    Integer.parseInt(queryParams.get("releaseYear")) : null;
            Integer ageRestriction = queryParams.get("ageRestriction") != null ?
                    Integer.parseInt(queryParams.get("ageRestriction")) : null;
            String sortBy = queryParams.get("sortBy");

            List<Media> mediaList = mediaService.searchMedia(title, genre, mediaType, releaseYear, ageRestriction, sortBy);
            String response = JsonUtil.mediaListToJson(mediaList);
            sendResponse(exchange, 200, response);

        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid number format in query parameters\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleSingleMedia(HttpExchange exchange, String method, String path, String requestBody) throws IOException {
        int mediaId = extractIdFromPath(path);

        switch (method) {
            case "GET":
                handleGetMedia(exchange, mediaId);
                break;
            case "PUT":
                handleUpdateMedia(exchange, mediaId, requestBody);
                break;
            case "DELETE":
                handleDeleteMedia(exchange, mediaId);
                break;
            default:
                sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }

    private void handleGetAllMedia(HttpExchange exchange) throws IOException {
        try {
            List<Media> allMedia = mediaService.getAllMedia();
            String response = JsonUtil.mediaListToJson(allMedia);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleCreateMedia(HttpExchange exchange, String requestBody) throws IOException {
        try {
            Integer creatorId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (creatorId == null) {
                sendResponse(exchange, 401, "{\"error\": \"Authentication required\"}");
                return;
            }

            Map<String, Object> mediaData = JsonUtil.parseJsonToMap(requestBody);

            //handle both "type" & "mediaType" for compatibility -> sucks but I dont wanna change db now :c
            String type = (String) mediaData.get("type");
            if (type == null) {
                type = (String) mediaData.get("mediaType");
            }

            String title = (String) mediaData.get("title");

            if (type == null || title == null || type.trim().isEmpty() || title.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Type and title are required\"}");
                return;
            }

            String description = (String) mediaData.get("description");
            Integer releaseYear = mediaData.get("releaseYear") != null ?
                    Integer.parseInt(mediaData.get("releaseYear").toString()) : null;
            Integer ageRestriction = mediaData.get("ageRestriction") != null ?
                    Integer.parseInt(mediaData.get("ageRestriction").toString()) : null;

            @SuppressWarnings("unchecked")
            List<String> genres = (List<String>) mediaData.get("genres");

            Media newMedia = new Media(type, title, description, releaseYear, ageRestriction, creatorId, genres);
            Media savedMedia = mediaService.createMedia(newMedia);

            if (savedMedia != null) {
                String response = JsonUtil.mediaToJson(savedMedia);
                sendResponse(exchange, 201, response);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to create media\"}");
            }

        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid number format for releaseYear or ageRestriction\"}");
        } catch (IOException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetMedia(HttpExchange exchange, int mediaId) throws IOException {
        try {
            Media media = mediaService.getMediaById(mediaId);
            if (media != null) {
                String response = JsonUtil.mediaToJson(media);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Media not found\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleUpdateMedia(HttpExchange exchange, int mediaId, String requestBody) throws IOException {
        try {
            Integer userId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (userId == null) {
                sendResponse(exchange, 401, "{\"error\": \"Authentication required\"}");
                return;
            }

            Media existingMedia = mediaService.getMediaById(mediaId);
            if (existingMedia == null) {
                sendResponse(exchange, 404, "{\"error\": \"Media not found\"}");
                return;
            }

            if (existingMedia.getCreator_id() != userId) {
                sendResponse(exchange, 403, "{\"error\": \"You can only update your own media\"}");
                return;
            }

            Map<String, Object> mediaData = JsonUtil.parseJsonToMap(requestBody);

            // Handle both "type" and "mediaType" for compatibility
            if (mediaData.containsKey("type")) {
                existingMedia.setType((String) mediaData.get("type"));
            } else if (mediaData.containsKey("mediaType")) {
                existingMedia.setType((String) mediaData.get("mediaType"));
            }

            if (mediaData.containsKey("title")) {
                existingMedia.setTitle((String) mediaData.get("title"));
            }
            if (mediaData.containsKey("description")) {
                existingMedia.setDescription((String) mediaData.get("description"));
            }
            if (mediaData.containsKey("releaseYear")) {
                existingMedia.setRelease_year(Integer.parseInt(mediaData.get("releaseYear").toString()));
            }
            if (mediaData.containsKey("ageRestriction")) {
                existingMedia.setAge_restriction(Integer.parseInt(mediaData.get("ageRestriction").toString()));
            }
            if (mediaData.containsKey("genres")) {
                @SuppressWarnings("unchecked")
                List<String> newGenres = (List<String>) mediaData.get("genres");
                existingMedia.setGenres(newGenres);
            }

            Media updatedMedia = mediaService.updateMedia(existingMedia);
            if (updatedMedia != null) {
                String response = JsonUtil.mediaToJson(updatedMedia);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to update media\"}");
            }

        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid number format\"}");
        } catch (IOException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleDeleteMedia(HttpExchange exchange, int mediaId) throws IOException {
        try {
            Integer userId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (userId == null) {
                sendResponse(exchange, 401, "{\"error\": \"Authentication required\"}");
                return;
            }

            Media existingMedia = mediaService.getMediaById(mediaId);
            if (existingMedia == null) {
                sendResponse(exchange, 404, "{\"error\": \"Media not found\"}");
                return;
            }

            if (existingMedia.getCreator_id() != userId) {
                sendResponse(exchange, 403, "{\"error\": \"You can only delete your own media\"}");
                return;
            }

            mediaService.deleteMedia(mediaId);
            sendResponse(exchange, 200, "{\"message\": \"Media deleted successfully\"}");

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleRateMedia(HttpExchange exchange, String path, String requestBody) throws IOException {
        //TODO: Implement rate media (will need RatingService)
        sendResponse(exchange, 501, "{\"message\": \"Rate media - TODO (needs RatingService)\"}");
    }

    private void handleFavoriteMedia(HttpExchange exchange, String method, String path) throws IOException {
        //TODO: Implement favorites (will need FavoriteService)
        if ("POST".equals(method)) {
            sendResponse(exchange, 501, "{\"message\": \"Mark as favorite - TODO (needs FavoriteService)\"}");
        } else if ("DELETE".equals(method)) {
            sendResponse(exchange, 501, "{\"message\": \"Unmark as favorite - TODO (needs FavoriteService)\"}");
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }

    private int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private Map<String, String> parseQueryString(String query) {
        return java.util.Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(java.util.stream.Collectors.toMap(
                        pair -> pair[0],
                        pair -> pair.length > 1 ? pair[1] : ""
                ));
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
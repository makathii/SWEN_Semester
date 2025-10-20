package at.technikum_wien.old.handler;

import at.technikum_wien.old.model.Media;
import at.technikum_wien.old.repository.MediaRepository;
import at.technikum_wien.old.util.AuthHelper;
import at.technikum_wien.old.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MediaHandler implements HttpHandler {
    private final MediaRepository mediaRepository = new MediaRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        //read request body for POST/PUT
        String requestBody = "";
        if ("POST".equals(method) || "PUT".equals(method)) {
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        }

        try {
            if (path.equals("/api/media")) {
                handleMediaCollection(exchange, method, requestBody);
            } else if (path.startsWith("/api/media/")) {
                handleSingleMedia(exchange, method, path, requestBody);
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

    private void handleSingleMedia(HttpExchange exchange, String method, String path, String requestBody) throws IOException {
        //extract media ID from path: /api/media/123 → 123
        String[] pathParts = path.split("/");
        if (pathParts.length < 4) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid media ID\"}");
            return;
        }

        try {
            int mediaId = Integer.parseInt(pathParts[3]);

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
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid media ID format\"}");
        }
    }

    private void handleGetAllMedia(HttpExchange exchange) throws IOException {
        try {
            List<Media> allMedia = mediaRepository.getAllMedia();
            String response = JsonUtil.mediaListToJson(allMedia);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleCreateMedia(HttpExchange exchange, String requestBody) throws IOException {
        try {
            //check authentication
            Integer creatorId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (creatorId == null) {
                sendResponse(exchange, 401, "{\"error\": \"Authentication required\"}");
                return;
            }

            //parse JSON request
            Map<String, Object> mediaData = JsonUtil.parseJsonToMap(requestBody);

            //extract & validate required fields
            String type = (String) mediaData.get("type");
            String title = (String) mediaData.get("title");

            if (type == null || title == null || type.trim().isEmpty() || title.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Type and title are required\"}");
                return;
            }

            //extract optional fields
            String description = (String) mediaData.get("description");
            Integer releaseYear = mediaData.get("releaseYear") != null ?
                    Integer.parseInt(mediaData.get("releaseYear").toString()) : null;
            Integer ageRestriction = mediaData.get("ageRestriction") != null ?
                    Integer.parseInt(mediaData.get("ageRestriction").toString()) : null;

            //extract genres
            @SuppressWarnings("unchecked")
            List<String> genres = (List<String>) mediaData.get("genres");

            //create & save media
            Media newMedia = new Media(type, title, description, releaseYear, ageRestriction, creatorId, genres);
            Media savedMedia = mediaRepository.save(newMedia);

            //send response
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
            Media media = mediaRepository.getById(mediaId);

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
            //check authentication
            Integer userId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (userId == null) {
                sendResponse(exchange, 401, "{\"error\": \"Authentication required\"}");
                return;
            }

            //get existing media to check ownership
            Media existingMedia = mediaRepository.getById(mediaId);
            if (existingMedia == null) {
                sendResponse(exchange, 404, "{\"error\": \"Media not found\"}");
                return;
            }

            //check ownership - only creator can update
            if (existingMedia.getCreator_id() != userId) {
                sendResponse(exchange, 403, "{\"error\": \"You can only update your own media\"}");
                return;
            }

            //parse JSON request
            Map<String, Object> mediaData = JsonUtil.parseJsonToMap(requestBody);

            //update media fields (only if provided in request)
            if (mediaData.containsKey("type")) {
                existingMedia.setType((String) mediaData.get("type"));
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

            //save updated media
            Media updatedMedia = mediaRepository.save(existingMedia);

            //send response
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
        sendResponse(exchange, 501, "{\"message\": \"Delete media - TODO\"}");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

}
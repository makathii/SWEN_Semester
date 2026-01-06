package at.technikum_wien.handlers;

import at.technikum_wien.security.AuthHelper;
import at.technikum_wien.services.RatingService;
import at.technikum_wien.models.entities.Rating;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RatingHandler implements HttpHandler {
    private final RatingService ratingService;
    private final ObjectMapper objectMapper;
    private final Pattern ratingIdPattern = Pattern.compile("/api/ratings/(\\d+)");
    private final Pattern mediaRatingPattern = Pattern.compile("/api/media/(\\d+)/rate");

    public RatingHandler(RatingService ratingService) {
        this.ratingService = ratingService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        String requestBody = "";
        if ("POST".equals(method) || "PUT".equals(method)) {
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        }

        try {
            //handle different endpoints based on path patterns
            if (path.matches("/api/media/\\d+/rate") && "POST".equals(method)) {
                handleRateMedia(exchange, path, requestBody);
            } else if (path.matches("/api/ratings/\\d+/like") && "POST".equals(method)) {
                handleLikeRating(exchange, path);
            } else if (path.matches("/api/ratings/\\d+/confirm") && "POST".equals(method)) {
                handleConfirmRating(exchange, path);
            } else if (path.matches("/api/ratings/\\d+")) {
                handleRatingOperations(exchange, method, path, requestBody);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: " + e.getMessage() + "\"}");
        } catch (SecurityException e) {
            sendResponse(exchange, 403, "{\"error\": \"Forbidden: " + e.getMessage() + "\"}");
        } catch (IllegalStateException e) {
            sendResponse(exchange, 409, "{\"error\": \"Conflict: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }


    private void handleRateMedia(HttpExchange exchange, String path, String requestBody) throws IOException {
        //extract mediaId from path
        int mediaId = extractIdFromPath(path, mediaRatingPattern);

        //parse request body
        Map<String, Object> requestData = parseRequestBody(requestBody);

        //extract required fields
        int userId = getRequiredInt(requestData, "user_id");
        int stars = getRequiredInt(requestData, "stars");
        String comment = getOptionalString(requestData, "comment", "");
        Rating rating = ratingService.rateMedia(mediaId, userId, stars, comment);
        String response = objectMapper.writeValueAsString(Map.of("message", "Rating created successfully", "rating", Map.of("id", rating.getId(), "media_id", rating.getMedia_id(), "user_id", rating.getUser_id(), "stars", rating.getStars(), "comment", rating.getComment(), "confirmed", rating.getConfirmed(), "created_at", rating.getCreated_at())));
        sendResponse(exchange, 201, response);
    }

    private void handleLikeRating(HttpExchange exchange, String path) throws IOException {
        try {
            //extract ratingId from path
            int ratingId = extractIdFromPath(path, ratingIdPattern);

            //extract required fields
            Integer userId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (userId == null) {
                sendResponse(exchange, 401, "{\"error\": \"Authentication required\"}");
                return;
            }

            boolean success = ratingService.likeRating(ratingId, userId);
            if (success) {
                int likeCount = ratingService.getRatingLikeCount(ratingId);
                sendResponse(exchange, 200, "{\"message\": \"Rating liked successfully\", \"like_count\": " + likeCount + "}");
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Failed to like rating\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleConfirmRating(HttpExchange exchange, String path) throws IOException {
        try {
            //extract ratingId from path
            int ratingId = extractIdFromPath(path, ratingIdPattern);

            //extract user ID
            Integer userId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (userId == null) {
                sendResponse(exchange, 401, "{\"error\": \"Authentication required\"}");
                return;
            }

            //get rating to check who created it
            Rating rating = ratingService.getRatingById(ratingId);
            if (rating == null) {
                sendResponse(exchange, 404, "{\"error\": \"Rating not found\"}");
                return;
            }

            //authorization check
            if (rating.getUser_id() != userId) {
                sendResponse(exchange, 403, "{\"error\": \"You can only confirm your own rating comments\"}");
                return;
            }

            //check if there's even a comment to confirm
            if (rating.getComment() == null || rating.getComment().trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"No comment to confirm\"}");
                return;
            }

            //check if already confirmed
            if (rating.getConfirmed()) {
                sendResponse(exchange, 400, "{\"error\": \"Comment is already confirmed and publicly visible\"}");
                return;
            }

            //call service to make comment publicly visible
            boolean success = ratingService.confirmRating(ratingId, userId);
            if (success) {
                sendResponse(exchange, 200, "{\"message\": \"Comment confirmed and is now publicly visible\"}");
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Failed to confirm comment\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleRatingOperations(HttpExchange exchange, String method, String path, String requestBody) throws IOException {
        int ratingId = extractIdFromPath(path, ratingIdPattern);

        switch (method) {
            case "PUT":
                handleUpdateRating(exchange, ratingId, requestBody);
                break;
            case "DELETE":
                handleDeleteRating(exchange, ratingId);
                break;
            case "GET":
                handleGetRating(exchange, ratingId);
                break;
            default:
                sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }

    private void handleUpdateRating(HttpExchange exchange, int ratingId, String requestBody) throws IOException {
        try {
            //get user ID from authentication
            Integer userId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (userId == null) {
                sendResponse(exchange, 401, "{\"error\": \"Authentication required\"}");
                return;
            }

            //parse request body
            Map<String, Object> ratingData = parseRequestBody(requestBody);

            //extract fields
            if (!ratingData.containsKey("stars")) {
                sendResponse(exchange, 400, "{\"error\": \"Stars rating is required\"}");
                return;
            }

            Object starsObj = ratingData.get("stars");
            int stars;
            if (starsObj instanceof Integer) {
                stars = (Integer) starsObj;
            } else {
                try {
                    stars = Integer.parseInt(starsObj.toString());
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, "{\"error\": \"Stars must be a number between 1 and 5\"}");
                    return;
                }
            }

            String comment = (String) ratingData.getOrDefault("comment", "");

            //validate stars
            if (stars < 1 || stars > 5) {
                sendResponse(exchange, 400, "{\"error\": \"Stars must be between 1 and 5\"}");
                return;
            }

            Rating updatedRating = ratingService.updateRating(ratingId, stars, comment, userId);

            int likeCount = ratingService.getRatingLikeCount(ratingId);

            //create response
            Map<String, Object> responseData = Map.of("message", "Rating updated successfully", "rating", Map.of("id", updatedRating.getId(), "media_id", updatedRating.getMedia_id(), "user_id", updatedRating.getUser_id(), "stars", updatedRating.getStars(), "comment", updatedRating.getComment(), "confirmed", updatedRating.getConfirmed(), "created_at", updatedRating.getCreated_at(), "like_count", likeCount));

            String response = objectMapper.writeValueAsString(responseData);
            sendResponse(exchange, 200, response);

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (SecurityException e) {
            sendResponse(exchange, 403, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleDeleteRating(HttpExchange exchange, int ratingId) throws IOException {
        //extract user ID from query parameters
        Integer userId = AuthHelper.getUserIdFromAuthHeader(exchange);

        if (userId == 0) {
            sendResponse(exchange, 400, "{\"error\": \"User ID is required\"}");
            return;
        }

        //call service
        ratingService.deleteRating(ratingId, userId);
        sendResponse(exchange, 200, "{\"message\": \"Rating deleted successfully\"}");
    }

    private void handleGetRating(HttpExchange exchange, int ratingId) throws IOException {
        try {
            //get requesting user ID from auth
            Integer requestingUserId = AuthHelper.getUserIdFromAuthHeader(exchange);

            //use service method that handles comment visibility
            Rating rating = ratingService.getPublicRating(ratingId, requestingUserId);

            if (rating == null) {
                sendResponse(exchange, 404, "{\"error\": \"Rating not found\"}");
                return;
            }

            //get like count
            int likeCount = ratingService.getRatingLikeCount(ratingId);

            //create response - rating object already has filtered comments
            Map<String, Object> responseData = Map.of("rating", Map.of("id", rating.getId(), "media_id", rating.getMedia_id(), "user_id", rating.getUser_id(), "stars", rating.getStars(), "comment", rating.getComment(), //will be null if unconfirmed & not owner
                    "confirmed", rating.getConfirmed(), "created_at", rating.getCreated_at(), "like_count", likeCount));

            String response = objectMapper.writeValueAsString(responseData);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    //HELPERS
    private int extractIdFromPath(String path, Pattern pattern) {
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Invalid path format");
    }

    private Map<String, Object> parseRequestBody(String requestBody) throws IOException {
        if (requestBody == null || requestBody.trim().isEmpty()) {
            throw new IllegalArgumentException("Request body is required");
        }
        return objectMapper.readValue(requestBody, new TypeReference<>() {
        });
    }

    private int getRequiredInt(Map<String, Object> data, String key) {
        if (!data.containsKey(key)) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Field " + key + " must be a number");
    }

    private String getOptionalString(Map<String, Object> data, String key, String defaultValue) {
        if (!data.containsKey(key)) {
            return defaultValue;
        }
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
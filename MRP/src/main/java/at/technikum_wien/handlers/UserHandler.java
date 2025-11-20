package at.technikum_wien.handlers;

import at.technikum_wien.models.entities.User;
import at.technikum_wien.security.AuthHelper;
import at.technikum_wien.services.UserService;
import at.technikum_wien.services.RatingService;
import at.technikum_wien.services.MediaService;
import at.technikum_wien.handlers.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserHandler implements HttpHandler {
    private final UserService userService;
    private final RatingService ratingService;
    private final MediaService mediaService;
    private final Pattern userIdPattern = Pattern.compile("/api/users/(\\d+)");

    public UserHandler(UserService userService, RatingService ratingService, MediaService mediaService) {
        this.userService = userService;
        this.ratingService = ratingService;
        this.mediaService = mediaService;
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
            if (path.equals("/api/users/register")) {
                handleRegister(exchange, requestBody);
            } else if (path.equals("/api/users/login")) {
                handleLogin(exchange, requestBody);
            } else if (path.matches("/api/users/\\d+/profile")) {
                handleUserProfile(exchange, method, path, requestBody);
            } else if (path.matches("/api/users/\\d+/ratings")) {
                handleUserRatings(exchange, path);
            } else if (path.matches("/api/users/\\d+/favorites")) {
                handleUserFavorites(exchange, path);
            } else if (path.matches("/api/users/\\d+/recommendations")) {
                handleUserRecommendations(exchange, path);
            } else if (path.equals("/api/users/leaderboard")) {
                handleLeaderboard(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleRegister(HttpExchange exchange, String requestBody) throws IOException {
        try {
            Map<String, String> userData = JsonUtil.parseJson(requestBody);
            String username = userData.get("username");
            String password = userData.get("password");

            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Username and password are required\"}");
                return;
            }

            var user = userService.registerUser(username, password);
            String response = String.format("{\"message\": \"User created successfully\", \"userId\": %d}", user.getId());
            sendResponse(exchange, 201, response);

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 409, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (IOException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleLogin(HttpExchange exchange, String requestBody) throws IOException {
        try {
            Map<String, String> userData = JsonUtil.parseJson(requestBody);
            String username = userData.get("username");
            String password = userData.get("password");

            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Username and password are required\"}");
                return;
            }

            var loginResult = userService.loginUser(username, password);
            String response = String.format("{\"message\": \"Login successful\", \"token\": \"%s\", \"userId\": %d}", loginResult.token, loginResult.userId);
            sendResponse(exchange, 200, response);

        } catch (SecurityException e) {
            sendResponse(exchange, 401, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (IOException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleUserProfile(HttpExchange exchange, String method, String path, String requestBody) throws IOException {
        int userId = extractIdFromPath(path, userIdPattern);

        if ("GET".equals(method)) {
            handleGetUserProfile(exchange, userId);
        } else if ("PUT".equals(method)) {
            handleUpdateUserProfile(exchange, userId, requestBody);
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }

    private void handleGetUserProfile(HttpExchange exchange, int userId) throws IOException {
        try {
            // Verify the requesting user has access to this profile
            Integer requestingUserId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (requestingUserId == null || requestingUserId != userId) {
                sendResponse(exchange, 403, "{\"error\": \"Access denied\"}");
                return;
            }

            User user = userService.getUserById(userId);
            if (user == null) {
                sendResponse(exchange, 404, "{\"error\": \"User not found\"}");
                return;
            }

            // Get user statistics
            var ratings = ratingService.getRatingsByUser(userId);
            var createdMedia = mediaService.getMediaByCreator(userId);

            // Calculate statistics
            int totalRatings = ratings.size();
            double averageRating = ratings.stream().mapToInt(r -> r.getStars()).average().orElse(0.0);

            // Simple favorite genre calculation (you can enhance this)
            String favoriteGenre = "None";
            if (!ratings.isEmpty()) {
                // This would need genre data from media - you'd need to enhance this
                favoriteGenre = "Action"; // Placeholder
            }

            String response = String.format("{\"user\": {\"id\": %d, \"username\": \"%s\", \"createdAt\": \"%s\"}, " + "\"statistics\": {\"totalRatings\": %d, \"averageRating\": %.2f, \"mediaCreated\": %d, \"favoriteGenre\": \"%s\"}}", user.getId(), user.getUsername(), user.getCreatedAt(), totalRatings, averageRating, createdMedia.size(), favoriteGenre);

            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleUpdateUserProfile(HttpExchange exchange, int userId, String requestBody) throws IOException {
        try {
            // Verify the requesting user has access to update this profile
            Integer requestingUserId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (requestingUserId == null || requestingUserId != userId) {
                sendResponse(exchange, 403, "{\"error\": \"Access denied\"}");
                return;
            }

            Map<String, String> updateData = JsonUtil.parseJson(requestBody);
            String newUsername = updateData.get("username");
            String newPassword = updateData.get("password");

            User user = userService.getUserById(userId);
            if (user == null) {
                sendResponse(exchange, 404, "{\"error\": \"User not found\"}");
                return;
            }

            // Update username if provided
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                user.setUsername(newUsername);
            }

            // Update password if provided
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setPasswordHash(newPassword); // This should be hashed in the service
            }

            User updatedUser = userService.updateUser(user);
            String response = String.format("{\"message\": \"Profile updated successfully\", \"userId\": %d}", updatedUser.getId());
            sendResponse(exchange, 200, response);

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 409, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (IOException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleUserRatings(HttpExchange exchange, String path) throws IOException {
        try {
            int userId = extractIdFromPath(path, userIdPattern);

            // Verify the requesting user has access
            Integer requestingUserId = AuthHelper.getUserIdFromAuthHeader(exchange);
            if (requestingUserId == null || requestingUserId != userId) {
                sendResponse(exchange, 403, "{\"error\": \"Access denied\"}");
                return;
            }

            var ratings = ratingService.getRatingsByUser(userId);
            String response = JsonUtil.ratingListToJson(ratings);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleUserFavorites(HttpExchange exchange, String path) throws IOException {
        // TODO: Implement favorites functionality
        sendResponse(exchange, 501, "{\"message\": \"Favorites functionality coming soon\"}");
    }

    private void handleUserRecommendations(HttpExchange exchange, String path) throws IOException {
        // TODO: Implement recommendations
        sendResponse(exchange, 501, "{\"message\": \"Recommendations functionality coming soon\"}");
    }

    private void handleLeaderboard(HttpExchange exchange) throws IOException {
        try {
            // TODO: Implement leaderboard - most active users by rating count
            // This would require a new repository method to get users sorted by rating count
            sendResponse(exchange, 501, "{\"message\": \"Leaderboard functionality coming soon\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private int extractIdFromPath(String path, Pattern pattern) {
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Invalid path format");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
package at.technikum_wien.presentation.handlers;

import at.technikum_wien.application.services.UserService;
import at.technikum_wien.infrastructure.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserHandler implements HttpHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
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
            String response = String.format("{\"message\": \"Login successful\", \"token\": \"%s\", \"userId\": %d}",
                    loginResult.token, loginResult.userId);
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
        if ("GET".equals(method)) {
            //TODO: Get user profile
            sendResponse(exchange, 501, "{\"message\": \"Get user profile - TODO\"}");
        } else if ("PUT".equals(method)) {
            //TODO: Update user profile
            sendResponse(exchange, 501, "{\"message\": \"Update user profile - TODO\"}");
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }

    private void handleUserRatings(HttpExchange exchange, String path) throws IOException {
        //TODO: Get user rating history
        sendResponse(exchange, 501, "{\"message\": \"Get user ratings - TODO\"}");
    }

    private void handleUserFavorites(HttpExchange exchange, String path) throws IOException {
        //TODO: Get user favorites
        sendResponse(exchange, 501, "{\"message\": \"Get user favorites - TODO\"}");
    }

    private void handleUserRecommendations(HttpExchange exchange, String path) throws IOException {
        //TODO: Get user recommendations
        sendResponse(exchange, 501, "{\"message\": \"Get user recommendations - TODO\"}");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
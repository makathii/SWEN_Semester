package at.technikum_wien.presentation.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RatingHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        String requestBody = "";
        if ("POST".equals(method) || "PUT".equals(method)) {
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        }

        try {
            if (path.matches("/api/ratings/\\d+/like")) {
                handleLikeRating(exchange, path);
            } else if (path.matches("/api/ratings/\\d+")) {
                handleUpdateRating(exchange, method, path, requestBody);
            } else if (path.matches("/api/ratings/\\d+/confirm")) {
                handleConfirmRating(exchange, path);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleLikeRating(HttpExchange exchange, String path) throws IOException {
        //TODO: Implement like rating
        sendResponse(exchange, 501, "{\"message\": \"Like rating - TODO\"}");
    }

    private void handleUpdateRating(HttpExchange exchange, String method, String path, String requestBody) throws IOException {
        if ("PUT".equals(method)) {
            //TODO: Update rating
            sendResponse(exchange, 501, "{\"message\": \"Update rating - TODO\"}");
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }

    private void handleConfirmRating(HttpExchange exchange, String path) throws IOException {
        //TODO: Confirm rating
        sendResponse(exchange, 501, "{\"message\": \"Confirm rating - TODO\"}");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
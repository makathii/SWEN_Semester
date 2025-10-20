package at.technikum_wien.presentation.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class LeaderboardHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            //TODO: Get leaderboard
            sendResponse(exchange, 501, "{\"message\": \"Get leaderboard - TODO\"}");
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
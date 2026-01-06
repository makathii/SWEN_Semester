package at.technikum_wien.handlers;

import at.technikum_wien.services.LeaderboardService;
import at.technikum_wien.handlers.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class LeaderboardHandler implements HttpHandler {
    private final LeaderboardService leaderboardService;

    public LeaderboardHandler(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        try {
            if (!"GET".equals(method)) {
                sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
                return;
            }

            if (path.equals("/api/leaderboard")) {
                handleGetLeaderboardSummary(exchange);
            } else if (path.equals("/api/leaderboard/top-users")) {
                handleGetTopUsers(exchange, query);
            } else if (path.equals("/api/leaderboard/top-rated")) {
                handleGetTopRatedMedia(exchange, query);
            } else if (path.equals("/api/leaderboard/most-liked")) {
                handleGetMostLikedRatings(exchange, query);
            } else if (path.equals("/api/leaderboard/trending-genres")) {
                handleGetTrendingGenres(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetLeaderboardSummary(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> summary = Map.of(
                    "topUsers", leaderboardService.getMostActiveUsers(10),
                    "topRatedMedia", leaderboardService.getTopRatedMedia(10),
                    "mostLikedRatings", leaderboardService.getMostLikedRatings(10),
                    "trendingGenres", leaderboardService.getTrendingGenres()
            );

            String response = JsonUtil.mapToJson(summary);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetTopUsers(HttpExchange exchange, String query) throws IOException {
        try {
            Map<String, String> params = parseQueryString(query);
            int limit = 20;

            if (params.containsKey("limit")) {
                try {
                    limit = Integer.parseInt(params.get("limit"));
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, "{\"error\": \"Invalid limit parameter\"}");
                    return;
                }
            }

            var topUsers = leaderboardService.getMostActiveUsers(limit);
            String response = JsonUtil.objectToJson(topUsers);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetTopRatedMedia(HttpExchange exchange, String query) throws IOException {
        try {
            Map<String, String> params = parseQueryString(query);
            int limit = 20;

            if (params.containsKey("limit")) {
                try {
                    limit = Integer.parseInt(params.get("limit"));
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, "{\"error\": \"Invalid limit parameter\"}");
                    return;
                }
            }

            var topMedia = leaderboardService.getTopRatedMedia(limit);
            String response = JsonUtil.objectToJson(topMedia);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetMostLikedRatings(HttpExchange exchange, String query) throws IOException {
        try {
            Map<String, String> params = parseQueryString(query);
            int limit = 20;

            if (params.containsKey("limit")) {
                try {
                    limit = Integer.parseInt(params.get("limit"));
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, "{\"error\": \"Invalid limit parameter\"}");
                    return;
                }
            }

            var mostLiked = leaderboardService.getMostLikedRatings(limit);
            String response = JsonUtil.objectToJson(mostLiked);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetTrendingGenres(HttpExchange exchange) throws IOException {
        try {
            var trendingGenres = leaderboardService.getTrendingGenres();
            String response = JsonUtil.objectToJson(trendingGenres);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private Map<String, String> parseQueryString(String query) {
        if (query == null || query.isEmpty()) {
            return Map.of();
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(
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
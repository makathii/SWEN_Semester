package at.technikum_wien.security;

import com.sun.net.httpserver.HttpExchange;
import at.technikum_wien.database.repositories.TokenRepository;

public class AuthHelper {
    public static Integer getUserIdFromAuthHeader(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return TokenRepository.getUserIdFromToken(token);
        }
        return null;
    }

    public static boolean isAuthenticated(HttpExchange exchange) {
        return getUserIdFromAuthHeader(exchange) != null;
    }
}
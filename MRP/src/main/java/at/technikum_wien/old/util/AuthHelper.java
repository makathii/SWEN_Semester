package at.technikum_wien.old.util;

import at.technikum_wien.old.repository.TokenRepository;
import com.sun.net.httpserver.HttpExchange;

public class AuthHelper {
    private static final TokenRepository tokenRepo = new TokenRepository();

    public static Integer getUserIdFromAuthHeader(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return tokenRepo.getUserIdFromToken(token);
        }
        return null;
    }

    public static boolean isAuthenticated(HttpExchange exchange) {
        return getUserIdFromAuthHeader(exchange) != null;
    }
}
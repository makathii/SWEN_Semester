package at.technikum_wien.util;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {
    private static final ConcurrentHashMap<String, Integer> tokens = new ConcurrentHashMap<>();

    public static String generateToken(int userId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, userId);
        return token;
    }

    public static Integer getUserIdFromToken(String token) {
        return tokens.get(token);
    }

    public static boolean isValidToken(String token) {
        return tokens.containsKey(token);
    }

    public static void invalidateToken(String token) {
        tokens.remove(token);
    }
}
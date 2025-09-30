package at.technikum_wien.handler;

import at.technikum_wien.model.User;
import at.technikum_wien.repository.UserRepository;
import at.technikum_wien.util.PasswordHasher;
import com.sun.net.httpserver.HttpExchange;

import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class UserHandler implements HttpHandler{
    private final UserRepository userRepository = new UserRepository();

    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            handlePost(exchange);
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            //requestBody {"username":"test","password":"pass"}
            String username = "test";
            String plainPassword = "pass";

            String hashedPassword = PasswordHasher.hashPassword(plainPassword);
            User newUser = new User(username, hashedPassword);

            if (userRepository.getByName(username) != null) {
                sendResponse(exchange, 409, "Username already exists");
                return;
            }
            User savedUser = userRepository.save(newUser);

            if (savedUser != null) {
                sendResponse(exchange, 201, "User created with ID: " + savedUser.getId());
            } else {
                sendResponse(exchange, 400, "Failed to create user");
            }

        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (var os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}


package at.technikum_wien.old.handler;

import at.technikum_wien.old.model.User;
import at.technikum_wien.old.repository.TokenRepository;
import at.technikum_wien.old.repository.UserRepository;
import at.technikum_wien.old.util.JsonUtil;
import at.technikum_wien.old.util.PasswordHasher;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserHandler implements HttpHandler {
    private final UserRepository userRepository = new UserRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        String requestBody = "";
        if ("POST".equals(method) || "PUT".equals(method)) {
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        }

        try {
            switch (method) {
                case "POST":
                    if (path.equals("/api/users")) {
                        handleRegister(exchange, requestBody);
                    } else if (path.equals("/api/users/login")) {
                        handleLogin(exchange, requestBody);
                    } else {
                        sendResponse(exchange, 404, "Not Found");
                    }
                    break;
                case "GET":
                    //TODO: handle user profile
                    sendResponse(exchange, 501, "Not Implemented");
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleRegister(HttpExchange exchange, String requestBody) throws IOException {
        try {
            //parse JSON request
            Map<String, String> userData = JsonUtil.parseJson(requestBody);

            //extract username & password
            String username = userData.get("username");
            String password = userData.get("password");

            //validate input
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Username and password are required\"}");
                return;
            }

            //check if username already exists
            User existingUser = userRepository.getByUsername(username);
            if (existingUser != null) {
                sendResponse(exchange, 409, "{\"error\": \"Username already exists\"}");
                return;
            }

            //create new user
            String hashedPassword = PasswordHasher.hashPassword(password);
            User newUser = new User(username, hashedPassword);
            User savedUser = userRepository.save(newUser);

            //send success response
            if (savedUser != null) {
                String response = String.format("{\"message\": \"User created successfully\", \"userId\": %d}", savedUser.getId());
                sendResponse(exchange, 201, response);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to create user\"}");
            }

        } catch (IOException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleLogin(HttpExchange exchange, String requestBody) throws IOException {
        try {
            //parse JSON request
            Map<String, String> userData = JsonUtil.parseJson(requestBody);

            //extract username and password
            String username = userData.get("username");
            String password = userData.get("password");

            //validate input
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Username and password are required\"}");
                return;
            }

            //find user by username
            User user = userRepository.getByUsername(username);
            if (user == null) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid username or password\"}");
                return;
            }

            //verify password
            boolean passwordValid = PasswordHasher.checkPassword(password, user.getPasswordHash());
            if (!passwordValid) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid username or password\"}");
                return;
            }

            //generate token and send success response
            TokenRepository tokenRepo = new TokenRepository();
            String token = tokenRepo.createToken(user.getId());

            if (token != null) {
                String response = String.format("{\"message\": \"Login successful\", \"token\": \"%s\", \"userId\": %d}", token, user.getId());
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to generate token\"}");
            }

        } catch (IOException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
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
package at.technikum_wien.handler;

import at.technikum_wien.repository.UserRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class UserHandler implements HttpHandler {
    private final UserRepository userRepository = new UserRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case "POST":
                    if (path.equals("/api/users")) {
                        handleRegister(exchange);
                    } else if (path.equals("/api/users/login")) {
                        handleLogin(exchange);
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

    private void handleRegister(HttpExchange exchange) throws IOException {
        //TODO
        sendResponse(exchange, 501, "Register endpoint - TODO");
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        //TODO
        sendResponse(exchange, 501, "Login endpoint - TODO");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
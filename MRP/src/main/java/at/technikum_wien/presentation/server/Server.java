package at.technikum_wien.presentation.server;

import at.technikum_wien.application.services.MediaService;
import at.technikum_wien.application.services.UserService;
import at.technikum_wien.infrastructure.handlers.*;
import at.technikum_wien.infrastructure.repositories.MediaRepository;
import at.technikum_wien.infrastructure.repositories.TokenRepository;
import at.technikum_wien.infrastructure.repositories.UserRepository;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws IOException {
        //setup repositories
        MediaRepository mediaRepository = new MediaRepository();
        UserRepository userRepository = new UserRepository();
        TokenRepository tokenRepository = new TokenRepository();

        //create services
        UserService userService = new UserService(userRepository, tokenRepository);
        MediaService mediaService = new MediaService(mediaRepository);

        //create handlers
        UserHandler userHandler = new UserHandler(userService);
        MediaHandler mediaHandler = new MediaHandler(mediaService);
        RatingHandler ratingHandler = new RatingHandler();
        LeaderboardHandler leaderboardHandler = new LeaderboardHandler();

        //create server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("Starting Media Ratings Platform server on port 8080...");

        //register all handlers
        server.createContext("/api/users", userHandler);
        server.createContext("/api/media", mediaHandler);
        server.createContext("/api/ratings", ratingHandler);
        server.createContext("/api/leaderboard", leaderboardHandler);

        server.start();
        System.out.println("Server started successfully!");
        System.out.println("Available endpoints:");
        System.out.println("=== AUTHENTICATION (IMPLEMENTED) ===");
        System.out.println("  POST /api/users/register - Register user");
        System.out.println("  POST /api/users/login - Login user");

        System.out.println("=== MEDIA MANAGEMENT (IMPLEMENTED) ===");
        System.out.println("  GET  /api/media - Get all media");
        System.out.println("  POST /api/media - Create media (requires auth)");
        System.out.println("  GET  /api/media/{id} - Get media by ID");
        System.out.println("  PUT  /api/media/{id} - Update media (creator only)");
        System.out.println("  DELETE /api/media/{id} - Delete media (creator only)");

        System.out.println("=== MEDIA SEARCH/FILTER (IMPLEMENTED) ===");
        System.out.println("  GET  /api/media?title=inception&genre=sci-fi - Search media");

        /*
        System.out.println("=== USER MANAGEMENT (PARTIAL) ===");
        System.out.println("  GET  /api/users/{id}/profile - TODO (returns 501)");
        System.out.println("  PUT  /api/users/{id}/profile - TODO (returns 501)");
        System.out.println("  GET  /api/users/{id}/ratings - TODO (returns 501)");
        System.out.println("  GET  /api/users/{id}/favorites - TODO (returns 501)");
        System.out.println("  GET  /api/users/{id}/recommendations - TODO (returns 501)");

        System.out.println("=== RATING SYSTEM (TODO) ===");
        System.out.println("  POST /api/media/{id}/rate - TODO (returns 501)");
        System.out.println("  PUT  /api/ratings/{id} - TODO (returns 501)");
        System.out.println("  POST /api/ratings/{id}/like - TODO (returns 501)");
        System.out.println("  POST /api/ratings/{id}/confirm - TODO (returns 501)");
        System.out.println("=== FAVORITES (TODO) ===");

        System.out.println("  POST /api/media/{id}/favorite - TODO (returns 501)");
        System.out.println("  DELETE /api/media/{id}/favorite - TODO (returns 501)");
        System.out.println("=== LEADERBOARD (TODO) ===");

        System.out.println("  GET  /api/leaderboard - TODO (returns 501)");
         */
    }
}
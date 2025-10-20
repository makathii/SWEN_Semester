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
        System.out.println("  POST /api/users/register - Register user");
        System.out.println("  POST /api/users/login - Login user");
        System.out.println("  GET  /api/media - Get all media");
        System.out.println("  POST /api/media - Create media");
        System.out.println("  GET  /api/media/{id} - Get media by ID");
        System.out.println("  PUT  /api/media/{id} - Update media");
        System.out.println("---------------------------------------------------");
    }
}
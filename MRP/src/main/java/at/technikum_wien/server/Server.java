package at.technikum_wien.server;

import at.technikum_wien.database.repositories.*;
import at.technikum_wien.handlers.*;
import at.technikum_wien.services.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws IOException {
        //setup repositories
        MediaRepository mediaRepository = new MediaRepository();
        UserRepository userRepository = new UserRepository();
        TokenRepository tokenRepository = new TokenRepository();
        RatingRepository ratingRepository = new RatingRepository();
        FavoriteRepository favoriteRepository = new FavoriteRepository();

        //create services
        UserService userService = new UserService(userRepository, tokenRepository);
        MediaService mediaService = new MediaService(mediaRepository);
        RatingService ratingService = new RatingService(ratingRepository, mediaRepository, userRepository);
        FavoriteService favoriteService = new FavoriteService(favoriteRepository, userRepository, mediaRepository);
        RecommendationService recommendationService = new RecommendationService(mediaRepository,ratingRepository,favoriteRepository);

        //create handlers
        UserHandler userHandler = new UserHandler(userService,ratingService,mediaService,favoriteService,recommendationService);
        MediaHandler mediaHandler = new MediaHandler(mediaService,ratingService,favoriteService);
        RatingHandler ratingHandler = new RatingHandler(ratingService);

        //create server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("Starting Media Ratings Platform server on port 8080...");

        //register all handlers
        server.createContext("/api/users", userHandler);
        server.createContext("/api/media", mediaHandler);
        server.createContext("/api/ratings", ratingHandler);

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

        System.out.println("=== RATING SYSTEM (IMPLEMENTED) ===");
        System.out.println("  POST /api/media/{id}/rate - Rate media");
        System.out.println("  PUT  /api/ratings/{id} - Update rating");
        System.out.println("  POST /api/ratings/{id}/like - Like rating");
        System.out.println("  POST /api/ratings/{id}/confirm - Confirm rating");
        System.out.println("  DELETE /api/ratings/{id} - Delete rating");
        System.out.println("  GET  /api/ratings/{id} - Get rating details");

        System.out.println("=== USER MANAGEMENT ===");
        System.out.println("  GET  /api/users/{id}/profile - Get user profile");
        System.out.println("  PUT  /api/users/{id}/profile - Update user profile");
        System.out.println("  GET  /api/users/{id}/ratings - Get user ratings");
        System.out.println("  GET  /api/users/{id}/favorites - Get user favorites");
        System.out.println("  GET  /api/users/{id}/recommendations - Get user recommendations");

        System.out.println("=== LEADERBOARD ===");
        System.out.println("  GET  /api/leaderboard - Get complete leaderboard");
        System.out.println("  GET  /api/leaderboard/top-rated - Get top rated media");
        System.out.println("  GET  /api/leaderboard/most-liked - Get most liked ratings");
        System.out.println("  GET  /api/leaderboard/top-users - Get top users by activity");

        System.out.println("=== FAVORITES ===");
        System.out.println("  POST /api/media/{id}/favorite - Add media to favorites");
        System.out.println("  DELETE /api/media/{id}/favorite - Remove media from favorites");
        System.out.println("  GET  /api/users/favorites - Get current user's favorites");
    }
    /*
        MISSING:
        all the testies ofc
        GET Rating History
        GET Favorites
        Recommendations dont make sense yet
        Leaderboard
        Update User -> apparently we need an email column which I wasnt aware of, nice.
        also it might be smart to save the favorite genre in the database?
        test all of the handle stuff deffo
        meby rethink tokens hehe
     */
}
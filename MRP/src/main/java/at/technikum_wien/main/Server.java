package at.technikum_wien.main;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws IOException {
        //create HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        System.out.println("Starting Media Ratings Platform server on port 8080...");

        //TODO: Register handlers here
        //server.createContext("/api/users", new UserHandler());
        //server.createContext("/api/media", new MediaHandler());
        //server.createContext("/api/ratings", new RatingHandler());

        server.start();
        System.out.println("Server started! Available endpoints:");
        System.out.println("  http://localhost:8080/api/users");
        System.out.println("  http://localhost:8080/api/media");
        System.out.println("  http://localhost:8080/api/ratings");
    }
}
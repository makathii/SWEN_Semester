package at.technikum_wien.database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection{
    public static void main(String[] args) {
        try (Connection con = DatabaseManager.INSTANCE.getConnection()) {
            System.out.println("Connected to the database");
        } catch (SQLException e) {
            System.out.println("Failed to connect to database: " + e.getMessage());
        }
    }
}

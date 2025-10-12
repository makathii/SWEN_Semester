package at.technikum_wien.util;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection con = DatabaseConnection.getConnection()) {
            System.out.println("Connected to the database");
        } catch (SQLException e) {
            System.out.println("failed Database Connection: " + e.getMessage());
        }
    }
}

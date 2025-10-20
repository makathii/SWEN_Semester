package at.technikum_wien.infrastructure.repositories;

import at.technikum_wien.infrastructure.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TokenRepository {

    public String createToken(int userId, String username) {
        String token = username+"-mrpToken";
        String sql = "INSERT INTO tokens (token, user_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, token);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            return token;

        } catch (SQLException e) {
            System.err.println("Error creating token: " + e.getMessage());
            return null;
        }
    }

    public static Integer getUserIdFromToken(String token) {
        String sql = "SELECT user_id FROM tokens WHERE token = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
                return null;
            }

        } catch (SQLException e) {
            System.err.println("Error validating token: " + e.getMessage());
            return null;
        }
    }

    public void deleteToken(String token) {
        String sql = "DELETE FROM tokens WHERE token = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, token);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error deleting token: " + e.getMessage());
        }
    }
}
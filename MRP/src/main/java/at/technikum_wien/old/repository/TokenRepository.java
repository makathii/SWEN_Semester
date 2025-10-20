package at.technikum_wien.old.repository;

import at.technikum_wien.old.util.DatabaseConnection;
import java.sql.*;
import java.util.UUID;

public class TokenRepository {

    public String createToken(int userId) {
        String token = UUID.randomUUID().toString();
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

    public Integer getUserIdFromToken(String token) {
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
package at.technikum_wien.database.repositories;

import at.technikum_wien.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TokenRepository {

    public String createToken(int userId, String username) {
        String token = username + "-mrpToken";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection()) {
            String checkSql = "SELECT user_id FROM tokens WHERE token = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, token);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String updateSql = "UPDATE tokens SET user_id = ?, created_at = CURRENT_TIMESTAMP WHERE token = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, userId);
                            updateStmt.setString(2, token);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        String insertSql = "INSERT INTO tokens (token, user_id) VALUES (?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, token);
                            insertStmt.setInt(2, userId);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
            return token;
        } catch (SQLException e) {
            System.err.println("Error creating token: " + e.getMessage());
            return null;
        }
    }

    public static Integer getUserIdFromToken(String token) {
        String sql = "SELECT user_id FROM tokens WHERE token = ?";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting token: " + e.getMessage());
        }
    }
}
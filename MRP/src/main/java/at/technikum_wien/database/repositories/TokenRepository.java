package at.technikum_wien.database.repositories;

import at.technikum_wien.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TokenRepository {

    private static final int TOKEN_EXPIRY_HOURS = 24;

    public String createToken(int userId, String username) {
        String token = username + "-mrpToken";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection()) {
            //clean up expired tokens for this user first
            cleanupExpiredTokens(conn, userId);

            //check if user already has valid token
            String checkSql = "SELECT token FROM tokens WHERE user_id = ? AND expires_at > CURRENT_TIMESTAMP";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        //return existing valid token
                        return rs.getString("token");
                    } else {
                        String insertSql = "INSERT INTO tokens (token, user_id, expires_at, last_used_at) VALUES (?, ?, CURRENT_TIMESTAMP + (? || ' HOURS')::INTERVAL, CURRENT_TIMESTAMP)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, token);
                            insertStmt.setInt(2, userId);
                            insertStmt.setInt(3, TOKEN_EXPIRY_HOURS);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
            return token;
        } catch (SQLException e) {
            System.err.println("Error creating token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getUserIdFromToken(String token) {
        String sql = "SELECT user_id FROM tokens WHERE token = ? AND expires_at > CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    //update last_used_at
                    updateLastUsed(conn, token);
                    return userId;
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error validating token: " + e.getMessage());
            return null;
        }
    }

    private void cleanupExpiredTokens(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM tokens WHERE user_id = ? AND expires_at <= CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    private static void updateLastUsed(Connection conn, String token) throws SQLException {
        String sql = "UPDATE tokens SET last_used_at = CURRENT_TIMESTAMP WHERE token = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            pstmt.executeUpdate();
        }
    }
}
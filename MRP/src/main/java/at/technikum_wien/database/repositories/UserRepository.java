package at.technikum_wien.database.repositories;

import at.technikum_wien.database.DatabaseManager;
import at.technikum_wien.models.entities.User;
import at.technikum_wien.models.interfaces.IRepository;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UserRepository implements IRepository<User> {
    public User save(User user) {
        String sql = "";
        boolean isInsert = user.getId() == 0;

        if (isInsert) {
            sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        } else {
            sql = "UPDATE users SET username=?, password_hash=?, favorite_genre=? WHERE id=?";
        }

        try (Connection conn = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (isInsert) {
                // INSERT: only 2 parameters (username, password_hash)
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPasswordHash());
                // NO parameter 3 for INSERT!
            } else {
                // UPDATE: 4 parameters (username, password_hash, favorite_genre, id)
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPasswordHash());

                // Handle favorite_genre as INTEGER (foreign key) - FIXED HERE
                if (user.getFavoriteGenre() != null && !user.getFavoriteGenre().isEmpty()) {
                    try {
                        // Try to parse as integer (genre_id)
                        int genreId = Integer.parseInt(user.getFavoriteGenre());
                        pstmt.setInt(3, genreId);
                    } catch (NumberFormatException e) {
                        // If it's a genre name, look up the genre_id
                        int genreId = getGenreIdByName(user.getFavoriteGenre(), conn);
                        if (genreId > 0) {
                            pstmt.setInt(3, genreId);
                        } else {
                            pstmt.setNull(3, Types.INTEGER);
                        }
                    }
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }

                pstmt.setInt(4, user.getId());
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                if (isInsert) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int newId = generatedKeys.getInt(1);
                            user.setId(newId);
                            return user;
                        }
                    }
                } else {
                    return user; // For UPDATE
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
            e.printStackTrace(); // Add this to see the full error
        }
        return null;
    }

    // Helper method to get genre_id by genre name
    private int getGenreIdByName(String genreName, Connection conn) throws SQLException {
        String sql = "SELECT genre_id FROM genres WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, genreName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("genre_id");
                }
            }
        }
        return -1; // Not found
    }

    @Override
    public void deleteById(User T) {
        int id = T.getId();
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("No user found with ID: " + id);
            } else {
                System.out.println("User deleted successfully");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e);
        }
    }

    @Override
    public User getById(int id) {
        User foundUser = null;
        // Modified query to get genre name instead of just ID
        String sql = """
            SELECT u.*, g.name as genre_name 
            FROM users u 
            LEFT JOIN genres g ON u.favorite_genre = g.genre_id 
            WHERE u.id=?
            """;
        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foundUser = new User();
                    foundUser.setId(rs.getInt("id"));
                    foundUser.setUsername(rs.getString("username"));
                    foundUser.setPasswordHash(rs.getString("password_hash"));

                    // Get favorite genre name or ID
                    String favoriteGenre = rs.getString("genre_name");
                    if (favoriteGenre == null) {
                        // If no genre name, try to get the genre_id
                        int genreId = rs.getInt("favorite_genre");
                        if (!rs.wasNull()) {
                            favoriteGenre = String.valueOf(genreId);
                        }
                    }
                    foundUser.setFavoriteGenre(favoriteGenre);

                    foundUser.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
        }
        return foundUser;
    }

    public User getByName(String name) {
        User foundUser = null;
        // Modified query to get genre name instead of just ID
        String sql = """
            SELECT u.*, g.name as genre_name 
            FROM users u 
            LEFT JOIN genres g ON u.favorite_genre = g.genre_id 
            WHERE u.username=?
            """;
        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foundUser = new User();
                    foundUser.setId(rs.getInt("id"));
                    foundUser.setUsername(rs.getString("username"));
                    foundUser.setPasswordHash(rs.getString("password_hash"));

                    // Get favorite genre name or ID
                    String favoriteGenre = rs.getString("genre_name");
                    if (favoriteGenre == null) {
                        // If no genre name, try to get the genre_id
                        int genreId = rs.getInt("favorite_genre");
                        if (!rs.wasNull()) {
                            favoriteGenre = String.valueOf(genreId);
                        }
                    }
                    foundUser.setFavoriteGenre(favoriteGenre);

                    foundUser.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
        return foundUser;
    }

    public boolean updateFavoriteGenre(int userId, String favoriteGenre) {
        String sql = "UPDATE users SET favorite_genre = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (favoriteGenre == null || favoriteGenre.trim().isEmpty()) {
                stmt.setNull(1, Types.INTEGER);
            } else {
                try {
                    // Try to parse as integer (genre_id)
                    int genreId = Integer.parseInt(favoriteGenre);
                    stmt.setInt(1, genreId);
                } catch (NumberFormatException e) {
                    // If it's a genre name, look up the genre_id
                    int genreId = getGenreIdByName(favoriteGenre, conn);
                    if (genreId > 0) {
                        stmt.setInt(1, genreId);
                    } else {
                        stmt.setNull(1, Types.INTEGER);
                    }
                }
            }
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating favorite genre: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getUserStatistics(int userId) {
        Map<String, Object> stats = new HashMap<>();
        String sql = """
            SELECT 
                (SELECT COUNT(*) FROM ratings WHERE user_id = ?) as rating_count,
                (SELECT COUNT(*) FROM favorites WHERE user_id = ?) as favorite_count,
                (SELECT COUNT(*) FROM media WHERE creator_id = ?) as media_created_count,
                (SELECT COALESCE(AVG(stars), 0) FROM ratings WHERE user_id = ?) as avg_rating
            """;

        try (Connection conn = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("ratingCount", rs.getInt("rating_count"));
                    stats.put("favoriteCount", rs.getInt("favorite_count"));
                    stats.put("mediaCreatedCount", rs.getInt("media_created_count"));
                    stats.put("averageRating", rs.getDouble("avg_rating"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user statistics: " + e.getMessage());
        }
        return stats;
    }
}
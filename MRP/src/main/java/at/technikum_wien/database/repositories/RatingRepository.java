package at.technikum_wien.database.repositories;

import at.technikum_wien.database.DatabaseManager;
import at.technikum_wien.models.entities.Rating;
import at.technikum_wien.models.interfaces.IRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingRepository implements IRepository<Rating> {

    @Override
    public Rating save(Rating rating) {
        String sql = "";
        boolean isInsert = rating.getId() == 0;
        if (isInsert) {
            sql = "INSERT INTO ratings (media_id, user_id, stars, comment, confirmed) VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE ratings SET stars = ?, comment = ?, confirmed = ? WHERE id = ?";
        }

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = isInsert ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(sql)) {
            if (isInsert) {
                pstmt.setInt(1, rating.getMedia_id());
                pstmt.setInt(2, rating.getUser_id());
                pstmt.setInt(3, rating.getStars());
                pstmt.setString(4, rating.getComment());
                pstmt.setBoolean(5, rating.getConfirmed() != null ? rating.getConfirmed() : false);
            } else {
                pstmt.setInt(1, rating.getStars());
                pstmt.setString(2, rating.getComment());
                pstmt.setBoolean(3, rating.getConfirmed() != null ? rating.getConfirmed() : false);
                pstmt.setInt(4, rating.getId());
            }
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                if (isInsert) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int newId = generatedKeys.getInt(1);
                            rating.setId(newId);
                        }
                    }
                }
                return rating;
            }
        } catch (SQLException e) {
            System.err.println("Error saving rating: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteById(Rating rating) {
        String sql = "DELETE FROM ratings WHERE id = ?";
        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rating.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting rating: " + e.getMessage());
        }
    }

    @Override
    public Rating getById(int id) {
        String sql = "SELECT * FROM ratings WHERE id = ?";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRating(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            System.err.println("Error finding rating by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Rating> getAllRatingsByUser(int userId) {
        String sql = "SELECT * FROM ratings WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return executeRatingQuery(pstmt);
        } catch (SQLException e) {
            System.err.println("Error getting ratings by user: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Rating> getAllRatingsByMedia(int mediaId) {
        String sql = "SELECT * FROM ratings WHERE media_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mediaId);
            return executeRatingQuery(pstmt);
        } catch (SQLException e) {
            System.err.println("Error getting ratings by media: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Rating getRatingByUserAndMedia(int userId, int mediaId) {
        String sql = "SELECT * FROM ratings WHERE user_id = ? AND media_id = ?";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, mediaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRating(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error finding rating by user and media: " + e.getMessage());
        }
        return null;
    }

    public List<Rating> getAllRatings() {
        String sql = "SELECT * FROM ratings ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return executeRatingQuery(pstmt);
        } catch (SQLException e) {
            System.err.println("Error getting all ratings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean confirmRating(int ratingId) {
        String sql = "UPDATE ratings SET confirmed = true WHERE id = ?";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ratingId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error confirming rating: " + e.getMessage());
            return false;
        }
    }

    public boolean likeRating(int ratingId, int userId) {
        String sql = "INSERT INTO rating_likes (rating_id, user_id) VALUES (?, ?) ON CONFLICT (rating_id, user_id) DO NOTHING";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ratingId);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error liking rating: " + e.getMessage());
            return false;
        }
    }

    public int getLikeCount(int ratingId) {
        String sql = "SELECT COUNT(*) as like_count FROM rating_likes WHERE rating_id = ?";

        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ratingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("like_count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting like count: " + e.getMessage());
        }
        return 0;
    }

    // HELPER METHODS
    private List<Rating> executeRatingQuery(PreparedStatement pstmt) throws SQLException {
        List<Rating> ratingList = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                ratingList.add(mapResultSetToRating(rs));
            }
        }
        return ratingList;
    }

    private Rating mapResultSetToRating(ResultSet rs) throws SQLException {
        return new Rating(rs.getInt("id"), rs.getInt("media_id"), rs.getInt("user_id"), rs.getInt("stars"), rs.getString("comment"), rs.getBoolean("confirmed"), rs.getTimestamp("created_at").toLocalDateTime());
    }
}
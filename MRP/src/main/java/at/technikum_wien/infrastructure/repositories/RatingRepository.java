package at.technikum_wien.infrastructure.repositories;

import at.technikum_wien.domain.entities.Rating;
import at.technikum_wien.domain.interfaces.IRepository;
import at.technikum_wien.infrastructure.util.DatabaseConnection;

import java.sql.*;

public class RatingRepository implements IRepository<Rating> {

    @Override
    public Rating save(Rating rating) {
        String sql = "";
        if (rating.getId() == 0) {
            sql = "INSERT INTO ratings (media_id,user_id,stars,comment,confirmed) VALUES (?,?,?,?,?)";
        } else {
            sql = "UPDATE ratings SET stars=?, comment=?,confirmed=? WHERE id=?";
        }
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (rating.getId() == 0) {
                pstmt.setInt(1, rating.getMedia_id());
                pstmt.setInt(2, rating.getUser_id());
                pstmt.setInt(3, rating.getStars());
                pstmt.setString(4, rating.getComment());
                pstmt.setBoolean(5, rating.getConfirmed());
            } else {
                pstmt.setInt(1, rating.getStars());
                pstmt.setString(2, rating.getComment());
                pstmt.setBoolean(3, rating.getConfirmed());
                pstmt.setInt(4, rating.getId());
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        rating.setId(newId);
                        return rating;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving rating: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void deleteById(Rating rating) {
        String sql = "DELETE FROM ratings WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rating.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Successfully deleted rating with ID: " + rating.getId());
            } else {
                System.out.println("No rating found with ID: " + rating.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error deleting rating: " + e.getMessage());
        }
    }

    @Override
    public Rating getById(int id) {
        String sql = "SELECT * FROM ratings WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Rating(rs.getInt("id"), rs.getInt("media_id"), rs.getInt("user_id"), rs.getInt("stars"), rs.getString("comment"), rs.getBoolean("confirmed"), rs.getTimestamp("created_at").toLocalDateTime());
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error finding rating by ID: " + e.getMessage());
            return null;
        }
    }
}

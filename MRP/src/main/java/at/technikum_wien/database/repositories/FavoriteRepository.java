package at.technikum_wien.database.repositories;

import at.technikum_wien.database.DatabaseManager;
import at.technikum_wien.models.entities.Favorite;
import at.technikum_wien.models.entities.Media;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {
    public boolean addFavorite(Favorite favorite) {
        String sql = "INSERT INTO favorites (user_id, media_id) VALUES (?,?) ON CONFLICT (user_id, media_id) DO NOTHING";
        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, favorite.getUserId());
            pstmt.setInt(2, favorite.getMediaId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding favorite: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteFavorite(int userId, int mediaId) {
        String sql = "DELETE FROM favorites WHERE user_id=? AND media_id=?";
        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, mediaId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting favorite: " + e.getMessage());
            return false;
        }
    }

    public boolean isFavorite(int userId, int mediaId) {
        String sql = "SELECT * FROM favorites WHERE user_id=? AND media_id=?";
        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, mediaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking favorite: " + e.getMessage());
            return false;
        }
    }

    public List<Media> getUserFavorites(int userId) {
        String sql = "SELECT m.*, array_agg(g.name) as genres " + "FROM favorites f " + "JOIN media m ON f.media_id = m.id " + "LEFT JOIN media_genres mg ON m.id = mg.media_id " + "LEFT JOIN genres g ON mg.genre_id = g.genre_id " + "WHERE f.user_id = ? " + "GROUP BY m.id ";
        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return executeMediaQuery(pstmt);
        }catch (SQLException e){
            System.err.println("Error getting user favorites: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public int getFavoriteCountForMedia(int mediaId) {
        String sql = "SELECT COUNT(*) as favorite_count FROM favorites WHERE media_id=?";
        try (Connection conn = DatabaseManager.INSTANCE.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mediaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("favorite_count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting favorite count: " + e.getMessage());
        }
        return 0;
    }

    //HELPER
    private List<Media> executeMediaQuery(PreparedStatement pstmt) throws SQLException {
        List<Media> result = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapResultSetToMedia(rs));
            }
        }
        return result;
    }

    private Media mapResultSetToMedia(ResultSet rs) throws SQLException {
        String[] genreArray = (String[]) rs.getArray("genres").getArray();
        List<String> genres = new ArrayList<>();
        if (genreArray != null){
            for (String genre : genreArray) {
                if (genre!=null){
                    genres.add(genre);
                }
            }
        }
        return new Media(
                rs.getInt("id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("release_year"),
                rs.getInt("age_restriction"),
                rs.getInt("creator_id"),
                genres
                );
    }
}

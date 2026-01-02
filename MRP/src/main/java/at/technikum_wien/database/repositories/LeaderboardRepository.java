package at.technikum_wien.database.repositories;

import at.technikum_wien.database.DatabaseManager;
import at.technikum_wien.models.entities.LeaderboardEntry;
import at.technikum_wien.models.entities.TopRatedMedia;
import at.technikum_wien.models.entities.MostLikedRating;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardRepository {

    public List<LeaderboardEntry> getMostActiveUsers(int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String sql = """
            SELECT 
                u.id,
                u.username,
                COUNT(DISTINCT r.id) as rating_count,
                COUNT(DISTINCT f.media_id) as favorite_count,
                COUNT(DISTINCT m.id) as media_created_count,
                (
                    COUNT(DISTINCT r.id) * 2 + 
                    COUNT(DISTINCT f.media_id) + 
                    COUNT(DISTINCT m.id) * 3
                ) as activity_score
            FROM users u
            LEFT JOIN ratings r ON u.id = r.user_id
            LEFT JOIN favorites f ON u.id = f.user_id
            LEFT JOIN media m ON u.id = m.creator_id
            GROUP BY u.id, u.username
            ORDER BY activity_score DESC
            LIMIT ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LeaderboardEntry entry = new LeaderboardEntry(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getInt("rating_count"),
                        rs.getInt("favorite_count"),
                        rs.getInt("media_created_count"),
                        rs.getInt("activity_score")
                );
                entries.add(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public List<TopRatedMedia> getTopRatedMedia(int limit) {
        List<TopRatedMedia> mediaList = new ArrayList<>();
        String sql = """
            SELECT 
                m.id,
                m.title,
                m.type,
                m.release_year,
                COALESCE(AVG(r.stars), 0) as average_rating,
                COUNT(r.id) as rating_count
            FROM media m
            LEFT JOIN ratings r ON m.id = r.media_id
            GROUP BY m.id, m.title, m.type, m.release_year
            HAVING COUNT(r.id) >= 2
            ORDER BY average_rating DESC, rating_count DESC
            LIMIT ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TopRatedMedia media = new TopRatedMedia(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("type"),
                        rs.getInt("release_year"),
                        rs.getDouble("average_rating"),
                        rs.getInt("rating_count")
                );
                mediaList.add(media);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mediaList;
    }

    public List<MostLikedRating> getMostLikedRatings(int limit) {
        List<MostLikedRating> ratings = new ArrayList<>();
        String sql = """
            SELECT 
                r.id as rating_id,
                r.stars,
                r.comment,
                m.title as media_title,
                u.username as author_name,
                COUNT(rl.user_id) as like_count
            FROM ratings r
            JOIN media m ON r.media_id = m.id
            JOIN users u ON r.user_id = u.id
            LEFT JOIN rating_likes rl ON r.id = rl.rating_id
            WHERE r.confirmed = true
            GROUP BY r.id, r.stars, r.comment, m.title, u.username
            ORDER BY like_count DESC, r.created_at DESC
            LIMIT ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                MostLikedRating rating = new MostLikedRating(
                        rs.getInt("rating_id"),
                        rs.getInt("stars"),
                        rs.getString("comment"),
                        rs.getString("media_title"),
                        rs.getString("author_name"),
                        rs.getInt("like_count")
                );
                ratings.add(rating);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratings;
    }

    public List<String> getTrendingGenres() {
        List<String> genres = new ArrayList<>();
        String sql = """
            SELECT 
                g.name,
                COUNT(DISTINCT r.id) as recent_ratings
            FROM genres g
            JOIN media_genres mg ON g.genre_id = mg.genre_id
            JOIN ratings r ON mg.media_id = r.media_id 
                AND r.created_at >= NOW() - INTERVAL '7 days'
            GROUP BY g.name
            ORDER BY recent_ratings DESC
            LIMIT 5
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                genres.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }
}
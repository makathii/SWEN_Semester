package at.technikum_wien.infrastructure.repositories;

import at.technikum_wien.domain.entities.Media;
import at.technikum_wien.domain.interfaces.IRepository;
import at.technikum_wien.infrastructure.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaRepository implements IRepository<Media> {
    //interface functions
    @Override
    public Media save(Media media) {
        String sql = "";
        boolean isInsert = media.getId() == 0;

        if (isInsert) {
            sql = "INSERT INTO media (type,title,description,release_year,age_restriction,creator_id) VALUES (?,?,?,?,?,?)";
        } else {
            sql = "UPDATE media SET type=?,title=?,description=?,release_year=?,age_restriction=? WHERE id=?";
        }

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = isInsert ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(sql)) {

            if (isInsert) {
                pstmt.setString(1, String.valueOf(media.getType()));
                pstmt.setString(2, media.getTitle());
                pstmt.setString(3, media.getDescription());
                pstmt.setInt(4, media.getRelease_year());
                pstmt.setInt(5, media.getAge_restriction());
                pstmt.setInt(6, media.getCreator_id());
            } else {
                pstmt.setString(1, String.valueOf(media.getType()));
                pstmt.setString(2, media.getTitle());
                pstmt.setString(3, media.getDescription());
                pstmt.setInt(4, media.getRelease_year());
                pstmt.setObject(5, media.getAge_restriction());
                pstmt.setInt(6, media.getId());  // WHERE clause
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                if (isInsert) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int newId = generatedKeys.getInt(1);
                            media.setId(newId);
                        }
                    }
                }
                saveGenres(conn, media.getId(), media.getGenres());
                return media;  //return updated media object
            }

        } catch (SQLException e) {
            System.err.println("Error saving media: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public void deleteById(Media media) {
        String sql = "DELETE FROM media WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, media.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting media: " + e.getMessage());
        }
    }

    @Override
    public Media getById(int id) {
        String sql = "SELECT m.*, array_agg(g.name) as genres " + "FROM media m " + "LEFT JOIN media_genres mg ON m.id = mg.media_id " + "LEFT JOIN genres g ON mg.genre_id = g.genre_id " + "WHERE m.id = ? " + "GROUP BY m.id";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedia(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            System.err.println("Error finding Media by ID: " + e.getMessage());
        }
        return null;
    }

    public Media getByName(String name) throws SQLException {
        String sql = "SELECT m.*, array_agg(g.name) as genres " + "FROM media m " + "LEFT JOIN media_genres mg ON m.id = mg.media_id " + "LEFT JOIN genres g ON mg.genre_id = g.genre_id " + "WHERE LOWER(m.title) = LOWER(?) " + "GROUP BY m.id";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedia(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new SQLException("Error searching media by title: " + e.getMessage(), e);
        }
    }

    public List<Media> getAllMedia() {
        String sql = "SELECT m.*, array_agg(g.name) as genres " + "FROM media m " + "LEFT JOIN media_genres mg ON m.id = mg.media_id " + "LEFT JOIN genres g ON mg.genre_id = g.genre_id " + "GROUP BY m.id " + "ORDER BY m.id";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            return executeMediaQuery(pstmt);

        } catch (SQLException e) {
            System.err.println("Error getting all media: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //genre Stuff
    private Set<Integer> getCurrentGenreIds(Connection conn, int mediaId) throws SQLException {
        Set<Integer> genreIds = new HashSet<>();
        String sql = "SELECT genre_id FROM media_genres WHERE media_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mediaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    genreIds.add(rs.getInt("genre_id"));
                }
            }
        }
        return genreIds;
    }

    private int getGenreId(Connection conn, String genreName) throws SQLException {
        String sql = "SELECT genre_id FROM genres WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, genreName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("genre_id") : -1;
            }
        }
    }

    private void saveGenres(Connection conn, int mediaId, List<String> newGenres) throws SQLException {
        if (newGenres == null) newGenres = new ArrayList<>();

        //get current genre IDs for this media
        Set<Integer> currentGenreIds = getCurrentGenreIds(conn, mediaId);

        //convert new genre names to IDs
        Set<Integer> newGenreIds = new HashSet<>();
        for (String genreName : newGenres) {
            int genreId = getOrCreateGenreId(conn, genreName); // CHANGED: Create if doesn't exist
            if (genreId != -1) newGenreIds.add(genreId);
        }

        //delete genres that are no longer in the list
        String deleteSql = "DELETE FROM media_genres WHERE media_id = ? AND genre_id = ?";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            for (Integer genreId : currentGenreIds) {
                if (!newGenreIds.contains(genreId)) {
                    deleteStmt.setInt(1, mediaId);
                    deleteStmt.setInt(2, genreId);
                    deleteStmt.executeUpdate();
                }
            }
        }

        //insert new genres that aren't already there
        String insertSql = "INSERT INTO media_genres (media_id, genre_id) VALUES (?, ?) ON CONFLICT (media_id, genre_id) DO NOTHING";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            for (Integer genreId : newGenreIds) {
                if (!currentGenreIds.contains(genreId)) {
                    insertStmt.setInt(1, mediaId);
                    insertStmt.setInt(2, genreId);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    private int getOrCreateGenreId(Connection conn, String genreName) throws SQLException {
        //first try to find existing genre
        String selectSql = "SELECT genre_id FROM genres WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, genreName);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("genre_id");
                }
            }
        }

        //genre doesn't exist -> create it
        String insertSql = "INSERT INTO genres (name) VALUES (?) RETURNING genre_id";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, genreName.toLowerCase()); // Store as lowercase for consistency
            try (ResultSet rs = insertStmt.executeQuery()) {
                if (rs.next()) {
                    int newGenreId = rs.getInt("genre_id");
                    System.out.println("Created new genre: " + genreName + " with ID: " + newGenreId);
                    return newGenreId;
                }
            }
        }
        return -1;
    }

    //HELPER
    private Media mapResultSetToMedia(ResultSet rs) throws SQLException {
        String[] genreArray = (String[]) rs.getArray("genres").getArray();
        List<String> genres = new ArrayList<>();
        if (genreArray != null) {
            for (String genre : genreArray) {
                if (genre != null) genres.add(genre);
            }
        }

        return new Media(rs.getInt("id"), rs.getString("type"), rs.getString("title"), rs.getString("description"), rs.getInt("release_year"), rs.getInt("age_restriction"), rs.getInt("creator_id"), genres);
    }

    private List<Media> executeMediaQuery(PreparedStatement pstmt) throws SQLException {
        List<Media> mediaList = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                mediaList.add(mapResultSetToMedia(rs));
            }
        }
        return mediaList;
    }
}
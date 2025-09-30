package at.technikum_wien.repository;

import at.technikum_wien.model.Media;
import at.technikum_wien.model.MediaTypes;
import at.technikum_wien.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaRepository implements Repository<Media> {

    //Interface functions
    @Override
    public Media save(Media media) {
        String sql="";
        if(media.getId()==0){
            sql="INSERT INTO media (type,title,description,release_year,age_restriction,creator_id,genres) VALUES (?,?,?,?,?,?,?)";
        }else{
            sql="UPDATE media SET type=?,title=?,description=?,release_year=?,age_restriction=? WHERE id=?";
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if(media.getId()!=0){
                pstmt.setString(1, String.valueOf(media.getType()));
                pstmt.setString(2, media.getTitle());
                pstmt.setString(3, media.getDescription());
                pstmt.setInt(4, media.getRelease_year());
                pstmt.setInt(5, media.getAge_restriction());
                pstmt.setInt(6, media.getCreator_id());
                pstmt.setObject(7, media.getGenres());
            }else{
                pstmt.setString(1, String.valueOf(media.getType()));
                pstmt.setString(2, media.getTitle());
                pstmt.setString(3, media.getDescription());
                pstmt.setInt(4, media.getRelease_year());
                pstmt.setObject(5, media.getAge_restriction());
                pstmt.setInt(6, media.getId());
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        media.setId(newId);

                        saveGenres(conn, newId, media.getGenres());

                        return media;
                    }
                }
            }
            return null;
        }catch(SQLException e){
            System.err.println("Error saving media: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteById(Media media) {
        String sql="DELETE FROM media WHERE id=?";
        try(Connection conn=DatabaseConnection.getConnection();
              PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setInt(1,media.getId());
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.err.println("Error deleting media: " + e.getMessage());
        }
    }

    @Override
    public Media getById(int id) {
        String sql = "SELECT m.*, array_agg(g.name) as genres " +
                "FROM media m " +
                "LEFT JOIN media_genres mg ON m.id = mg.media_id " +
                "LEFT JOIN genres g ON mg.genre_id = g.genre_id " +
                "WHERE m.id = ? " +
                "GROUP BY m.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    @Override
    public Media getByName(String name) throws SQLException {
        String sql = "SELECT m.*, array_agg(g.name) as genres " +
                "FROM media m " +
                "LEFT JOIN media_genres mg ON m.id = mg.media_id " +
                "LEFT JOIN genres g ON mg.genre_id = g.genre_id " +
                "WHERE LOWER(m.title) = LOWER(?) " +
                "GROUP BY m.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try(ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()) {
                    return mapResultSetToMedia(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new SQLException("Error searching media by title: " + e.getMessage(), e);
        }
    }

    //genre Stuff
    private void saveGenres(Connection conn, int mediaId, List<String> genres) throws SQLException {
        if (genres == null || genres.isEmpty()) return;

        String genreSql = "INSERT INTO genres (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
        String selectGenreIdSql = "SELECT genre_id FROM genres WHERE name = ?";
        String mediaGenreSql = "INSERT INTO media_genres (media_id, genre_id) VALUES (?, ?)";

        try (PreparedStatement genreStmt = conn.prepareStatement(genreSql);
             PreparedStatement selectStmt = conn.prepareStatement(selectGenreIdSql);
             PreparedStatement mediaGenreStmt = conn.prepareStatement(mediaGenreSql)) {

            for (String genreName : genres) {
                genreStmt.setString(1, genreName);
                genreStmt.executeUpdate();

                selectStmt.setString(1, genreName);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        int genreId = rs.getInt("genre_id");
                        mediaGenreStmt.setInt(1, mediaId);
                        mediaGenreStmt.setInt(2, genreId);
                        mediaGenreStmt.executeUpdate();
                    }
                }
            }
        }
    }

    //HELPER
    private Media mapResultSetToMedia(ResultSet rs) throws SQLException{
        String[] genreArray = (String[]) rs.getArray("genres").getArray();
        List<String> genres = new ArrayList<>();
        if (genreArray != null) {
            for (String genre : genreArray) {
                if (genre != null) genres.add(genre);
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
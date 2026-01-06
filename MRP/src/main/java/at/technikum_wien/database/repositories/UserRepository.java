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
                //INSERT: only 2 parameters (username, password_hash)
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPasswordHash());
            } else {
                //UPDATE: 4 parameters (username, password_hash, favorite_genre, id)
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPasswordHash());

                //handle favorite_genre as INTEGER (foreign key)
                if (user.getFavoriteGenre() != null && !user.getFavoriteGenre().isEmpty()) {
                    try {
                        //try to parse as integer (genre_id)
                        int genreId = Integer.parseInt(user.getFavoriteGenre());
                        pstmt.setInt(3, genreId);
                    } catch (NumberFormatException e) {
                        //if it's a genre name, look up genre_id
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
                    return user; //for update
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    //helper method to get genre_id by genre name
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
        return -1; //not found :(
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

                    //get favorite genre name or ID
                    String favoriteGenre = rs.getString("genre_name");
                    if (favoriteGenre == null) {
                        //if no genre name, try to get genre_id
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

                    //get favorite genre name/ID
                    String favoriteGenre = rs.getString("genre_name");
                    if (favoriteGenre == null) {
                        //if no genre name, try to get genre_id
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
}
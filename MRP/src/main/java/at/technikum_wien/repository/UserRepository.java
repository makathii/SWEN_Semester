package at.technikum_wien.repository;

import at.technikum_wien.model.User;
import at.technikum_wien.repository.interfaces.IRepoDelete;
import at.technikum_wien.repository.interfaces.IRepoGetByID;
import at.technikum_wien.repository.interfaces.IRepoGetByName;
import at.technikum_wien.repository.interfaces.IRepoSave;
import at.technikum_wien.util.DatabaseConnection;

import java.sql.*;

public class UserRepository implements IRepoSave<User>, IRepoDelete<User>, IRepoGetByID<User>, IRepoGetByName<User> {
    public User save(User user) {
        String sql = "";
        if (user.getId() == 0) {
            sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        } else {
            sql = "UPDATE users SET username=?, password_hash=? WHERE id=?";
        }
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        user.setId(newId);
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteById(User T) {
        int id = T.getId();
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foundUser = new User();
                    foundUser.setId(rs.getInt("id"));
                    foundUser.setUsername(rs.getString("username"));
                    foundUser.setPasswordHash(rs.getString("password_hash"));
                    foundUser.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
        }
        return foundUser;
    }

    @Override
    public User getByName(String name) {
        User foundUser = null;
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foundUser = new User();
                    foundUser.setId(rs.getInt("id"));
                    foundUser.setUsername(rs.getString("username"));
                    foundUser.setPasswordHash(rs.getString("password_hash"));
                    foundUser.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
        return foundUser;
    }

    public User getByUsername(String username) {
        User foundUser = null;
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foundUser = new User();
                    foundUser.setId(rs.getInt("id"));
                    foundUser.setUsername(rs.getString("username"));
                    foundUser.setPasswordHash(rs.getString("password_hash"));
                    foundUser.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by Username: " + e.getMessage());
        }
        return foundUser;
    }
}

package at.technikum_wien.repository;

import at.technikum_wien.model.User;
import at.technikum_wien.util.DatabaseConnection;
import at.technikum_wien.util.PasswordHasher;

import java.sql.*;

public class UserRepository {

    public User createUser(String username, String plainTextPassword) {
        User newUser=null;
        String passwordHash = PasswordHasher.hashPassword(plainTextPassword);
        String sql="INSERT INTO users(username, password_hash) VALUES (?,?)";

        try (Connection con= DatabaseConnection.getConnection();
             PreparedStatement pstmt=con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1,username);
            pstmt.setString(2,passwordHash);

            int affectedRows=pstmt.executeUpdate();
            if(affectedRows>0){
                try(ResultSet generatedKeys=pstmt.getGeneratedKeys()){
                    if(generatedKeys.next()){
                        int newId=generatedKeys.getInt(1);

                        newUser=new User();
                        newUser.setId(newId);
                        newUser.setUsername(username);
                        newUser.setPasswordHash(passwordHash);
                        newUser.setCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
                    }
                }
            }
        }catch(SQLException e){
            System.err.println("Error creating user: "+e);
        }
        return newUser;
    }

    public User getUserByUsername(String username) {
        User foundUser=null;
        String sql="SELECT * FROM users WHERE username=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
            System.err.println("Error finding user by username: " + e.getMessage());
        }

        return foundUser;
    }

    public User getUserById(int id){
        User foundUser=null;
        String sql="SELECT * FROM users WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
}

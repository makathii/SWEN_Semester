package at.technikum_wien.repository;

import at.technikum_wien.model.Rating;
import at.technikum_wien.util.DatabaseConnection;

import java.sql.*;

public class RatingRepository implements Repository<Rating> {

    @Override
    public Rating save(Rating rating) {
        String sql="";
        if(rating.getId()==0) {
            sql = "INSERT INTO ratings (media_id,user_id,stars,comment,confirmed) VALUES (?,?,?,?,?)";
        }else{
            sql = "UPDATE ratings SET stars=?, comment=?,confirmed=? WHERE id=?";
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt=conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            if(rating.getId()==0){
                pstmt.setInt(1,rating.getMedia_id());
                pstmt.setInt(2,rating.getUser_id());
                pstmt.setInt(3,rating.getStars());
                pstmt.setString(4,rating.getComment());
                pstmt.setBoolean(5,rating.getConfirmed());
            }else{
                pstmt.setInt(1,rating.getStars());
                pstmt.setString(2,rating.getComment());
                pstmt.setBoolean(3,rating.getConfirmed());
                pstmt.setInt(4,rating.getId());
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
        }catch (SQLException e){
            System.err.println("Error saving rating: "+e.getMessage());
        }

        return null;
    }

    @Override
    public void deleteById(Rating rating) {

    }

    @Override
    public Rating getById(int id) {
        return null;
    }

    @Override
    public Rating getByName(String name) throws SQLException {
        return null;
    }
}

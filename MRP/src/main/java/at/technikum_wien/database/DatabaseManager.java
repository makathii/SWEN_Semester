package at.technikum_wien.database;

import at.technikum_wien.models.execeptions.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum DatabaseManager {
    INSTANCE;

    public Connection getConnection()
    {
        try {
            return DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/mrp_db",
                    "mrp_user",
                    "mrp_pwd");
        } catch (SQLException e) {
            throw new DataAccessException("Database connection failed!", e);
        }
    }
}
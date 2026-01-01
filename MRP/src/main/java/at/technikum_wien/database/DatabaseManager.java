package at.technikum_wien.database;

import at.technikum_wien.models.execeptions.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum DatabaseManager {
    INSTANCE;

    private String url = "jdbc:postgresql://localhost:5432/mrp_db";
    private String user = "mrp_user";
    private String password = "mrp_pwd";

    public void overrideForTests(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DataAccessException("Database connection failed!", e);
        }
    }
}

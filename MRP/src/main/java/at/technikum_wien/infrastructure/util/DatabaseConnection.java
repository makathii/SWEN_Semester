package at.technikum_wien.infrastructure.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/mrp_db";
    private static final String USER = "mrp_user";
    private static final String PASSWORD = "mrp_pwd";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

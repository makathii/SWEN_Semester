package at.technikum_wien.database.repositories;

import at.technikum_wien.database.DatabaseManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestBase {

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("test_db")
                    .withUsername("test_user")
                    .withPassword("test_pwd");

    @BeforeAll
    void startContainer() {
        POSTGRES.start();

        // Inject container DB into DatabaseManager
        DatabaseManager.INSTANCE.overrideForTests(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );

        runMigrations();
    }

    private void runMigrations() {
        // Normally you use Flyway, but for demonstration:
        try (Connection conn = DatabaseManager.INSTANCE.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create minimal schema needed (you can replace with Flyway later)

            stmt.execute("""
                CREATE TABLE users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            stmt.execute("""
                CREATE TABLE media (
                    id SERIAL PRIMARY KEY,
                    type VARCHAR(50) NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    description TEXT,
                    release_year INT,
                    age_restriction INT,
                    creator_id INT
                );
            """);

            stmt.execute("""
                CREATE TABLE genres (
                    genre_id SERIAL PRIMARY KEY,
                    name VARCHAR(50) UNIQUE
                );
            """);

            stmt.execute("""
                CREATE TABLE media_genres (
                    media_id INT,
                    genre_id INT,
                    PRIMARY KEY (media_id, genre_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE favorites (
                    user_id INT,
                    media_id INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, media_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE ratings (
                    id SERIAL PRIMARY KEY,
                    media_id INT,
                    user_id INT,
                    stars INT,
                    comment TEXT,
                    confirmed BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            stmt.execute("""
                CREATE TABLE rating_likes (
                    rating_id INT,
                    user_id INT,
                    PRIMARY KEY (rating_id, user_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE tokens (
                    token VARCHAR(255) PRIMARY KEY,
                    user_id INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

        } catch (Exception e) {
            throw new RuntimeException("Schema migration failed!", e);
        }
    }
}

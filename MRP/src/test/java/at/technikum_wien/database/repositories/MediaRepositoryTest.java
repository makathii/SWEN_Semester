package at.technikum_wien.database.repositories;

import at.technikum_wien.models.entities.Media;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MediaRepositoryTest extends IntegrationTestBase {

    private final MediaRepository repo = new MediaRepository();

    @Test
    void testInsertAndFetchMedia() {
        Media media = new Media(
                0, "movie", "Test Movie", "Desc", 2024, 16, 1,
                List.of("Action", "Comedy")
        );

        Media saved = repo.save(media);
        assertNotNull(saved);
        assertTrue(saved.getId() > 0);

        Media fetched = repo.getById(saved.getId());

        assertEquals("Test Movie", fetched.getTitle());
        assertTrue(fetched.getGenres().contains("action")); // lowercase stored
    }
}

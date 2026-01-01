package at.technikum_wien.database.repositories;

import at.technikum_wien.models.entities.Rating;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RatingRepositoryTest extends IntegrationTestBase {

    private final RatingRepository repo = new RatingRepository();

    @Test
    void testSaveRating() {
        Rating r = new Rating(0, 1, 1, 5, "Great!", false, null);

        Rating saved = repo.save(r);

        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
    }

    @Test
    void testLikeRating() {
        Rating r = repo.save(new Rating(0, 1, 1, 5, "Test", false, null));

        assertTrue(repo.likeRating(r.getId(), 99));
        assertEquals(1, repo.getLikeCount(r.getId()));
    }
}

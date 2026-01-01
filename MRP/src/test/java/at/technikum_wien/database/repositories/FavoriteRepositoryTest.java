package at.technikum_wien.database.repositories;

import at.technikum_wien.models.entities.Favorite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FavoriteRepositoryTest extends IntegrationTestBase {

    private final FavoriteRepository repo = new FavoriteRepository();

    @Test
    void testAddAndCheckFavorite() {
        repo.addFavorite(new Favorite(1, 10));

        assertTrue(repo.isFavorite(1, 10));
    }

    @Test
    void testDeleteFavorite() {
        repo.addFavorite(new Favorite(2, 20));

        assertTrue(repo.deleteFavorite(2, 20));
        assertFalse(repo.isFavorite(2, 20));
    }
}

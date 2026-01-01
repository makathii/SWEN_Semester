package at.technikum_wien.database.repositories;

import at.technikum_wien.models.entities.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest extends IntegrationTestBase {

    private final UserRepository repo = new UserRepository();

    @Test
    void testSaveAndGetById() {
        User u = new User();
        u.setUsername("john");
        u.setPasswordHash("pw");

        User saved = repo.save(u);

        assertNotNull(saved);
        assertTrue(saved.getId() > 0);

        User fetched = repo.getById(saved.getId());
        assertEquals("john", fetched.getUsername());
    }

    @Test
    void testGetByName() {
        User u = new User();
        u.setUsername("maria");
        u.setPasswordHash("pw123");
        repo.save(u);

        User found = repo.getByName("maria");
        assertNotNull(found);
    }
}

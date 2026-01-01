package at.technikum_wien.database.repositories;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenRepositoryTest extends IntegrationTestBase {

    private final TokenRepository repo = new TokenRepository();

    @Test
    void testTokenLifecycle() {
        String token = repo.createToken(1, "john");

        assertNotNull(token);

        Integer userId = TokenRepository.getUserIdFromToken(token);
        assertEquals(1, userId);

        repo.deleteToken(token);
        assertNull(TokenRepository.getUserIdFromToken(token));
    }
}

package at.technikum_wien.database.repositories;

import at.technikum_wien.models.entities.Media;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MediaRepositoryTest {
    static MediaRepository mediaRepository;
    @BeforeAll
    public static void setup() {
        mediaRepository = new MediaRepository();
    }
    @Test
    public void getById() {
        mediaRepository.getById(1);
        Media expectedMedia=mediaRepository.getById(1);
        Media actualMedia=mediaRepository.getById(1);

    }
}

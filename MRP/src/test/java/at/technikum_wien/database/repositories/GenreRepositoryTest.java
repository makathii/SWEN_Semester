package at.technikum_wien.database.repositories;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GenreRepositoryTest {
    static GenreRepository genreRepository;
    @BeforeAll
    public static void setup() {
        genreRepository = new GenreRepository();
    }
    @Test
    public void getById() {

    }

}

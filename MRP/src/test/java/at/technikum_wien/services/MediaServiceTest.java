package at.technikum_wien.services;

import at.technikum_wien.models.entities.Media;
import at.technikum_wien.database.repositories.MediaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    private MediaService mediaService;
    private List<Media> testMediaList;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(mediaRepository);

        //create test data
        Media media1 = new Media();
        media1.setId(1);
        media1.setTitle("The Dark Knight");
        media1.setGenres(List.of("Action", "Crime", "Drama"));
        media1.setType("movie");
        media1.setRelease_year(2008);
        media1.setAge_restriction(16);

        Media media2 = new Media();
        media2.setId(2);
        media2.setTitle("Inception");
        media2.setGenres(List.of("Action", "Sci-Fi", "Thriller"));
        media2.setType("movie");
        media2.setRelease_year(2010);
        media2.setAge_restriction(12);

        Media media3 = new Media();
        media3.setId(3);
        media3.setTitle("The Witcher 3");
        media3.setGenres(List.of("RPG", "Adventure", "Fantasy"));
        media3.setType("game");
        media3.setRelease_year(2015);
        media3.setAge_restriction(18);

        Media media4 = new Media();
        media4.setId(4);
        media4.setTitle("Toy Story");
        media4.setGenres(List.of("Animation", "Comedy", "Family"));
        media4.setType("movie");
        media4.setRelease_year(1995);
        media4.setAge_restriction(0);

        testMediaList = Arrays.asList(media1, media2, media3, media4);
    }

    @Test
    void searchMedia_FilterByTitle_ReturnsMatchingMedia() {
        //arrange
        when(mediaRepository.getAllMedia()).thenReturn(testMediaList);

        //act
        List<Media> result = mediaService.searchMedia("knight", null, null, null, null, null);

        //assert
        assertEquals(1, result.size());
        assertEquals("The Dark Knight", result.get(0).getTitle());
    }

    @Test
    void searchMedia_FilterByTitle_IsCaseInsensitive() {
        //arrange
        when(mediaRepository.getAllMedia()).thenReturn(testMediaList);

        //act
        List<Media> result = mediaService.searchMedia("DARK", null, null, null, null, null);

        //assert
        assertEquals(1, result.size());
        assertEquals("The Dark Knight", result.get(0).getTitle());
    }

    @Test
    void searchMedia_FilterByTitle_PartialMatch() {
        //arrange
        when(mediaRepository.getAllMedia()).thenReturn(testMediaList);

        //act
        List<Media> result = mediaService.searchMedia("The", null, null, null, null, null);

        //assert
        assertEquals(2, result.size()); //both movies have a "the" in the name
    }

    @Test
    void searchMedia_FilterByGenre_ReturnsMatchingMedia() {
        //arrange
        when(mediaRepository.getAllMedia()).thenReturn(testMediaList);

        //act
        List<Media> result = mediaService.searchMedia(null, "Action", null, null, null, null);

        //assert
        assertEquals(2, result.size()); //both movies have "Action" genre
        assertTrue(result.stream().anyMatch(m -> m.getTitle().equals("The Dark Knight")));
        assertTrue(result.stream().anyMatch(m -> m.getTitle().equals("Inception")));
    }

    @Test
    void searchMedia_FilterByGenre_IsCaseInsensitive() {
        //arrange
        when(mediaRepository.getAllMedia()).thenReturn(testMediaList);

        //act
        List<Media> result = mediaService.searchMedia(null, "ACTION", null, null, null, null);

        //assert
        assertEquals(2, result.size());
    }

    @Test
    void searchMedia_FilterByMediaType_ReturnsMatchingMedia() {
        //arrange
        when(mediaRepository.getAllMedia()).thenReturn(testMediaList);

        //act
        List<Media> result = mediaService.searchMedia(null, null, "game", null, null, null);

        //assert
        assertEquals(1, result.size());
        assertEquals("The Witcher 3", result.get(0).getTitle());
    }

    @Test
    void searchMedia_FilterByReleaseYear_ExactMatch() {
        //arrange
        when(mediaRepository.getAllMedia()).thenReturn(testMediaList);

        //act
        List<Media> result = mediaService.searchMedia(null, null, null, 2010, null, null);

        //assert
        assertEquals(1, result.size());
        assertEquals("Inception", result.get(0).getTitle());
    }

    @Test
    void searchMedia_FilterByAgeRestriction_LessOrEqual() {
        //arrange
        when(mediaRepository.getAllMedia()).thenReturn(testMediaList);

        //act - age 12 should return media with age restriction 12 and 0
        List<Media> result = mediaService.searchMedia(null, null, null, null, 12, null);

        //assert
        assertEquals(2, result.size()); //Toy Story (0) & Inception (12)
        assertTrue(result.stream().anyMatch(m -> m.getTitle().equals("Inception")));
        assertTrue(result.stream().anyMatch(m -> m.getTitle().equals("Toy Story")));
        assertFalse(result.stream().anyMatch(m -> m.getAge_restriction() > 12));
    }

    @Test
    void searchMedia_FilterByAgeRestriction_ZeroReturnsOnlyZeroRated() {
        //arrange
        when(mediaRepository.getAllMedia()).thenReturn(testMediaList);

        //act
        List<Media> result = mediaService.searchMedia(null, null, null, null, 0, null);

        //assert
        assertEquals(1, result.size()); //all media have age restriction >= 0
    }

    @Test
    void searchMedia_MultipleFilters_CombinedCorrectly() {
        //arrange
        when(mediaRepository.getAllMedia()).thenReturn(testMediaList);

        //act - Action movies from 2008 or later
        List<Media> result = mediaService.searchMedia(null, "Action", "movie", 2008, null, null);

        //assert
        assertEquals(1, result.size());
        assertEquals("The Dark Knight", result.get(0).getTitle());
        //Inception is filtered out because it's 2010 (> 2008)
    }
}
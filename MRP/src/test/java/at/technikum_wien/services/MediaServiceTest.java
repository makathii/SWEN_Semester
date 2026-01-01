package at.technikum_wien.services;

import at.technikum_wien.database.repositories.MediaRepository;
import at.technikum_wien.models.entities.Media;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    MediaRepository mediaRepository;

    @InjectMocks
    MediaService mediaService;

    private Media m1;
    private Media m2;
    private Media m3;

    @BeforeEach
    void setUp() {
        m1 = new Media(1, "movie", "Alpha", "d", 2020, 12, 1, List.of("Action", "Comedy"));
        m2 = new Media(2, "series", "Bravo", "d", 2021, 16, 2, List.of("Drama"));
        m3 = new Media(3, "movie", "Charlie", "d", 2019, 6, 1, List.of("Action"));
    }

    @Test
    void searchMedia_filtersAndSorts() {
        when(mediaRepository.getAllMedia()).thenReturn(List.of(m1, m2, m3));

        //filter by title substring
        var res1 = mediaService.searchMedia("alph", null, null, null, null, null);
        assertEquals(1, res1.size());
        assertEquals("Alpha", res1.get(0).getTitle());

        //filter by genre (its case-insensitive)
        var res2 = mediaService.searchMedia(null, "action", null, null, null, null);
        assertEquals(2, res2.size());

        //filter by mediaType
        var res3 = mediaService.searchMedia(null, null, "movie", null, null, null);
        assertEquals(2, res3.size());

        //filter by releaseYear
        var res4 = mediaService.searchMedia(null, null, null, 2021, null, null);
        assertEquals(1, res4.size());
        assertEquals("Bravo", res4.get(0).getTitle());

        //filter by ageRestriction (<=)
        var res5 = mediaService.searchMedia(null, null, null, null, 10, null);
        assertEquals(1, res5.size());
        assertEquals("Charlie", res5.get(0).getTitle());

        //sort by title
        var res6 = mediaService.searchMedia(null, null, null, null, null, "title");
        assertEquals(3, res6.size());
        assertEquals("Alpha", res6.get(0).getTitle());

        //sort by releaseYear (newest first)
        var res7 = mediaService.searchMedia(null, null, null, null, null, "releaseYear");
        assertEquals(3, res7.size());
        assertEquals(2021, res7.get(0).getRelease_year());
    }

    @Test
    void getMediaByCreator_filters() {
        when(mediaRepository.getAllMedia()).thenReturn(List.of(m1, m2, m3));
        var createdBy1 = mediaService.getMediaByCreator(1);
        assertEquals(2, createdBy1.size());
    }
}

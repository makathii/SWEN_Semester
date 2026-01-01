package at.technikum_wien.services;

import at.technikum_wien.database.repositories.RatingRepository;
import at.technikum_wien.database.repositories.MediaRepository;
import at.technikum_wien.database.repositories.UserRepository;
import at.technikum_wien.models.entities.Media;
import at.technikum_wien.models.entities.Rating;
import at.technikum_wien.models.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    RatingRepository ratingRepository;
    @Mock
    MediaRepository mediaRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    RatingService ratingService;

    private Media media;
    private User user;

    @BeforeEach
    void setUp() {
        media = new Media(1, "movie", "X", "d", 2020, 12, 2, List.of("Action"));
        user = new User();
        user.setId(10);
    }

    @Test
    void rateMedia_success_noComment_autoConfirmed() {
        when(mediaRepository.getById(1)).thenReturn(media);
        when(userRepository.getById(10)).thenReturn(user);
        when(ratingRepository.getRatingByUserAndMedia(10, 1)).thenReturn(null);

        // simulate saving: repository returns rating with id set
        ArgumentCaptor<Rating> captor = ArgumentCaptor.forClass(Rating.class);
        Rating saved = new Rating(1, 1, 10, 5, null, true, LocalDateTime.now());
        when(ratingRepository.save(any())).thenReturn(saved);

        Rating result = ratingService.rateMedia(1, 10, 5, null);
        assertNotNull(result);
        assertTrue(result.getConfirmed());
        verify(ratingRepository).save(captor.capture());
        assertEquals(5, captor.getValue().getStars());
    }

    @Test
    void rateMedia_withComment_unconfirmed() {
        when(mediaRepository.getById(1)).thenReturn(media);
        when(userRepository.getById(10)).thenReturn(user);
        when(ratingRepository.getRatingByUserAndMedia(10, 1)).thenReturn(null);

        Rating saved = new Rating(2, 1, 10, 4, "Good", false, LocalDateTime.now());
        when(ratingRepository.save(any())).thenReturn(saved);

        Rating res = ratingService.rateMedia(1, 10, 4, "Good");
        assertNotNull(res);
        assertFalse(res.getConfirmed());
        assertEquals("Good", res.getComment());
    }

    //tests if error message is correct?
    @Test
    void rateMedia_invalidStars() {
        when(mediaRepository.getById(1)).thenReturn(media);
        when(userRepository.getById(10)).thenReturn(user);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ratingService.rateMedia(1, 10, 0, ""));
        assertEquals("Stars must be between 1 and 5", ex.getMessage());
    }

    //tests if error message is correct? + Exception which makes sense I guess?
    @Test
    void rateMedia_mediaNotFound() {
        when(mediaRepository.getById(99)).thenReturn(null);
        when(userRepository.getById(10)).thenReturn(user);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ratingService.rateMedia(99, 10, 5, ""));
        assertTrue(ex.getMessage().contains("Media not found"));
    }

    //tests if error message is correct? + Exception which makes sense I guess?
    @Test
    void rateMedia_userNotFound() {
        when(mediaRepository.getById(1)).thenReturn(media);
        when(userRepository.getById(777)).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ratingService.rateMedia(1, 777, 5, ""));
        assertTrue(ex.getMessage().contains("User not found"));
    }

    //tests if error message is correct? + Exception which makes sense I guess?
    @Test
    void rateMedia_alreadyRated() {
        when(mediaRepository.getById(1)).thenReturn(media);
        when(userRepository.getById(10)).thenReturn(user);
        when(ratingRepository.getRatingByUserAndMedia(10, 1)).thenReturn(new Rating());
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> ratingService.rateMedia(1, 10, 5, ""));
        assertEquals("User has already rated this media. Use update instead.", ex.getMessage());
    }

    //tests if rating shows unconfirmed comment -> makes sense I Guess
    @Test
    void getPublicRating_hidesComment_forNonOwnerUnconfirmed() {
        Rating r = new Rating(5, 1, 20, 4, "secret", false, LocalDateTime.now());
        when(ratingRepository.getById(5)).thenReturn(r);

        Rating result = ratingService.getPublicRating(5, 99); // different user
        assertNotNull(result);
        assertNull(result.getComment());
        assertEquals(r.getStars(), result.getStars());
    }

    //tests if rating shows unconfrimed comment -> makes sense I guess
    @Test
    void getPublicRating_showsComment_forOwner() {
        Rating r = new Rating(6, 1, 30, 3, "mine", false, LocalDateTime.now());
        when(ratingRepository.getById(6)).thenReturn(r);

        Rating result = ratingService.getPublicRating(6, 30); // owner
        assertEquals("mine", result.getComment());
    }

    @Test
    void updateRating_success_and_commentChange_unconfirms() {
        Rating existing = new Rating(7, 1, 40, 4, "old", true, LocalDateTime.now());
        when(ratingRepository.getById(7)).thenReturn(existing);
        when(ratingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Rating updated = ratingService.updateRating(7, 5, "new", 40);
        assertEquals(5, updated.getStars());
        assertFalse(updated.getConfirmed());
    }

    @Test
    void updateRating_notOwner_throwsSecurity() {
        Rating existing = new Rating(8, 1, 50, 4, "old", true, LocalDateTime.now());
        when(ratingRepository.getById(8)).thenReturn(existing);
        SecurityException ex = assertThrows(SecurityException.class, () -> ratingService.updateRating(8, 5, "new", 999));
        assertTrue(ex.getMessage().contains("User is not authorized"));
    }

    @Test
    void confirmRating_noComment_throws() {
        Rating existing = new Rating(10, 1, 70, 5, null, false, LocalDateTime.now());
        when(ratingRepository.getById(10)).thenReturn(existing);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> ratingService.confirmRating(10, 70));
        assertEquals("No comment to confirm", ex.getMessage());
    }

    @Test
    void confirmRating_alreadyConfirmed_throws() {
        Rating existing = new Rating(11, 1, 80, 5, "ok", true, LocalDateTime.now());
        when(ratingRepository.getById(11)).thenReturn(existing);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> ratingService.confirmRating(11, 80));
        assertEquals("Comment is already confirmed", ex.getMessage());
    }

    @Test
    void confirmRating_notOwner_throwsSecurity() {
        Rating existing = new Rating(12, 1, 90, 4, "ok", false, LocalDateTime.now());
        when(ratingRepository.getById(12)).thenReturn(existing);
        SecurityException ex = assertThrows(SecurityException.class, () -> ratingService.confirmRating(12, 999));
        assertTrue(ex.getMessage().contains("User can only confirm"));
    }

    @Test
    void deleteRating_success() {
        Rating existing = new Rating(13, 1, 100, 3, "x", false, LocalDateTime.now());
        when(ratingRepository.getById(13)).thenReturn(existing);
        doNothing().when(ratingRepository).deleteById(existing);
        assertTrue(ratingService.deleteRating(13, 100));
        verify(ratingRepository).deleteById(existing);
    }

    @Test
    void deleteRating_notOwner_throwsSecurity() {
        Rating existing = new Rating(14, 1, 110, 3, "x", false, LocalDateTime.now());
        when(ratingRepository.getById(14)).thenReturn(existing);
        SecurityException ex = assertThrows(SecurityException.class, () -> ratingService.deleteRating(14, 999));
        assertTrue(ex.getMessage().contains("User is not authorized"));
    }

    @Test
    void likeRating_success() {
        Rating existing = new Rating(15, 1, 120, 4, "x", true, LocalDateTime.now());
        when(ratingRepository.getById(15)).thenReturn(existing);
        when(userRepository.getById(200)).thenReturn(new User());
        when(ratingRepository.likeRating(15, 200)).thenReturn(true);
        assertTrue(ratingService.likeRating(15, 200));
    }

    @Test
    void likeRating_cannotLikeOwn_throws() {
        Rating existing = new Rating(16, 1, 300, 4, "x", true, LocalDateTime.now());
        when(ratingRepository.getById(16)).thenReturn(existing);
        when(userRepository.getById(300)).thenReturn(new User());
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> ratingService.likeRating(16, 300));
        assertTrue(ex.getMessage().contains("cannot like their own"));
    }

    @Test
    void getRatingLikeCount_delegates() {
        when(ratingRepository.getById(17)).thenReturn(new Rating());
        when(ratingRepository.getLikeCount(17)).thenReturn(5);
        assertEquals(5, ratingService.getRatingLikeCount(17));
    }
}

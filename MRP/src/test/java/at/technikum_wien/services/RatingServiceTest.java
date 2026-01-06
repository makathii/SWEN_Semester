package at.technikum_wien.services;

import at.technikum_wien.database.repositories.RatingRepository;
import at.technikum_wien.database.repositories.MediaRepository;
import at.technikum_wien.database.repositories.UserRepository;
import at.technikum_wien.models.entities.Rating;
import at.technikum_wien.models.entities.Media;
import at.technikum_wien.models.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RatingService ratingService;

    //rateMedia
    @Test
    void rateMedia_ValidInputWithoutComment_CreatesConfirmedRating() {
        //arrange
        int mediaId = 1;
        int userId = 10;
        int stars = 4;
        String comment = null;

        Media media = new Media();
        User user = new User("testuser", "hash");
        when(mediaRepository.getById(mediaId)).thenReturn(media);
        when(userRepository.getById(userId)).thenReturn(user);
        when(ratingRepository.getRatingByUserAndMedia(userId, mediaId)).thenReturn(null);

        Rating savedRating = new Rating(mediaId, userId, stars, null);
        savedRating.setId(100);
        savedRating.setConfirmed(true);
        when(ratingRepository.save(any(Rating.class))).thenReturn(savedRating);

        //act
        Rating result = ratingService.rateMedia(mediaId, userId, stars, comment);

        //assert
        assertNotNull(result);
        assertEquals(100, result.getId());
        assertTrue(result.getConfirmed());
        assertNull(result.getComment());
        verify(ratingRepository).save(argThat(rating ->
                rating.getMedia_id() == mediaId &&
                        rating.getUser_id() == userId &&
                        rating.getStars() == stars &&
                        rating.getConfirmed() == true //no comment = automatically confirmed
        ));
    }

    @Test
    void rateMedia_ValidInputWithComment_CreatesUnconfirmedRating() {
        //arrange
        int mediaId = 1;
        int userId = 10;
        int stars = 5;
        String comment = "Great movie!";

        Media media = new Media();
        User user = new User("testuser", "hash");
        when(mediaRepository.getById(mediaId)).thenReturn(media);
        when(userRepository.getById(userId)).thenReturn(user);
        when(ratingRepository.getRatingByUserAndMedia(userId, mediaId)).thenReturn(null);

        Rating savedRating = new Rating(mediaId, userId, stars, comment);
        savedRating.setId(100);
        savedRating.setConfirmed(false);
        when(ratingRepository.save(any(Rating.class))).thenReturn(savedRating);

        //act
        Rating result = ratingService.rateMedia(mediaId, userId, stars, comment);

        //assert
        assertNotNull(result);
        assertEquals(100, result.getId());
        assertFalse(result.getConfirmed());
        assertEquals("Great movie!", result.getComment());
        verify(ratingRepository).save(argThat(rating ->
                rating.getMedia_id() == mediaId &&
                        rating.getUser_id() == userId &&
                        rating.getStars() == stars &&
                        "Great movie!".equals(rating.getComment()) &&
                        rating.getConfirmed() == false //has comment = needs confirmation
        ));
    }

    @Test
    void rateMedia_InvalidStars_ThrowsException() {
        //arrange
        int mediaId = 1;
        int userId = 10;
        int stars = 6; //invalid - too high
        String comment = null;

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.rateMedia(mediaId, userId, stars, comment)
        );

        assertEquals("Stars must be between 1 and 5", exception.getMessage());
        verify(mediaRepository, never()).getById(anyInt());
    }

    @Test
    void rateMedia_StarsZero_ThrowsException() {
        //arrange
        int mediaId = 1;
        int userId = 10;
        int stars = 0; //invalid - too low
        String comment = null;

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.rateMedia(mediaId, userId, stars, comment)
        );

        assertEquals("Stars must be between 1 and 5", exception.getMessage());
    }

    @Test
    void rateMedia_MediaNotFound_ThrowsException() {
        //arrange
        int mediaId = 999;
        int userId = 10;
        int stars = 3;
        String comment = null;

        when(mediaRepository.getById(mediaId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.rateMedia(mediaId, userId, stars, comment)
        );

        assertEquals("Media not found with ID: " + mediaId, exception.getMessage());
        verify(userRepository, never()).getById(anyInt());
    }

    @Test
    void rateMedia_UserNotFound_ThrowsException() {
        //arrange
        int mediaId = 1;
        int userId = 999;
        int stars = 3;
        String comment = null;

        Media media = new Media();
        when(mediaRepository.getById(mediaId)).thenReturn(media);
        when(userRepository.getById(userId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.rateMedia(mediaId, userId, stars, comment)
        );

        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(ratingRepository, never()).getRatingByUserAndMedia(anyInt(), anyInt());
    }

    @Test
    void rateMedia_AlreadyRated_ThrowsException() {
        //arrange
        int mediaId = 1;
        int userId = 10;
        int stars = 3;
        String comment = null;

        Media media = new Media();
        User user = new User("testuser", "hash");
        Rating existingRating = new Rating(mediaId, userId, 4, "Old comment");

        when(mediaRepository.getById(mediaId)).thenReturn(media);
        when(userRepository.getById(userId)).thenReturn(user);
        when(ratingRepository.getRatingByUserAndMedia(userId, mediaId)).thenReturn(existingRating);

        //act & assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ratingService.rateMedia(mediaId, userId, stars, comment)
        );

        assertEquals("User has already rated this media. Use update instead.", exception.getMessage());
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void rateMedia_SaveFails_ThrowsRuntimeException() {
        //arrange
        int mediaId = 1;
        int userId = 10;
        int stars = 3;
        String comment = null;

        Media media = new Media();
        User user = new User("testuser", "hash");
        when(mediaRepository.getById(mediaId)).thenReturn(media);
        when(userRepository.getById(userId)).thenReturn(user);
        when(ratingRepository.getRatingByUserAndMedia(userId, mediaId)).thenReturn(null);
        when(ratingRepository.save(any(Rating.class))).thenReturn(null);

        //act & assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> ratingService.rateMedia(mediaId, userId, stars, comment)
        );

        assertEquals("Failed to save rating", exception.getMessage());
    }

    //getPublicRating
    @Test
    void getPublicRating_RatingNotFound_ReturnsNull() {
        //arrange
        int ratingId = 999;
        Integer requestingUserId = 10;

        when(ratingRepository.getById(ratingId)).thenReturn(null);

        //act
        Rating result = ratingService.getPublicRating(ratingId, requestingUserId);

        //assert
        assertNull(result);
    }

    @Test
    void getPublicRating_ConfirmedRating_ShowsCommentToAnyone() {
        //arrange
        int ratingId = 100;
        Integer requestingUserId = 20; //different user
        Rating rating = new Rating(1, 10, 4, "Great movie!");
        rating.setId(ratingId);
        rating.setConfirmed(true);

        when(ratingRepository.getById(ratingId)).thenReturn(rating);

        //act
        Rating result = ratingService.getPublicRating(ratingId, requestingUserId);

        //assert
        assertSame(rating, result);
        assertEquals("Great movie!", result.getComment());
    }

    @Test
    void getPublicRating_UnconfirmedRating_OwnerSeesComment() {
        //arrange
        int ratingId = 100;
        int ownerUserId = 10;
        Rating rating = new Rating(1, ownerUserId, 4, "Private comment");
        rating.setId(ratingId);
        rating.setConfirmed(false);

        when(ratingRepository.getById(ratingId)).thenReturn(rating);

        //act - Owner requesting
        Rating result = ratingService.getPublicRating(ratingId, ownerUserId);

        //assert - Owner sees comment
        assertSame(rating, result);
        assertEquals("Private comment", result.getComment());
    }

    @Test
    void getPublicRating_UnconfirmedRating_OtherUser_HidesComment() {
        //arrange
        int ratingId = 100;
        int ownerUserId = 10;
        Integer otherUserId = 20;
        Rating rating = new Rating(1, ownerUserId, 4, "Private comment");
        rating.setId(ratingId);
        rating.setConfirmed(false);

        when(ratingRepository.getById(ratingId)).thenReturn(rating);

        //act - Other user requesting
        Rating result = ratingService.getPublicRating(ratingId, otherUserId);

        //assert - Comment is hidden
        assertNotSame(rating, result); //should be new Rating object
        assertEquals(ratingId, result.getId());
        assertEquals(4, result.getStars());
        assertNull(result.getComment()); // Comment hidden
        assertFalse(result.getConfirmed());
    }

    @Test
    void getPublicRating_UnconfirmedRating_NullUserId_HidesComment() {
        //arrange
        int ratingId = 100;
        Integer requestingUserId = null; // No user logged in
        Rating rating = new Rating(1, 10, 4, "Private comment");
        rating.setId(ratingId);
        rating.setConfirmed(false);

        when(ratingRepository.getById(ratingId)).thenReturn(rating);

        //act - Anonymous user
        Rating result = ratingService.getPublicRating(ratingId, requestingUserId);

        //assert - Comment is hidden
        assertNull(result.getComment()); //comment hidden
    }

    //getRatingsByUser
    @Test
    void getRatingsByUser_ValidUser_ReturnsRatingsList() {
        //arrange
        int userId = 10;
        List<Rating> expectedRatings = Arrays.asList(
                new Rating(1, userId, 5, "Great!"),
                new Rating(2, userId, 3, "Average")
        );

        when(ratingRepository.getAllRatingsByUser(userId)).thenReturn(expectedRatings);

        //act
        List<Rating> result = ratingService.getRatingsByUser(userId);

        //assert
        assertEquals(expectedRatings, result);
        verify(ratingRepository).getAllRatingsByUser(userId);
    }

    @Test
    void getRatingsByUser_NoRatings_ReturnsEmptyList() {
        //arrange
        int userId = 10;
        List<Rating> emptyList = List.of();
        when(ratingRepository.getAllRatingsByUser(userId)).thenReturn(emptyList);

        //act
        List<Rating> result = ratingService.getRatingsByUser(userId);

        //assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    //updateRating
    @Test
    void updateRating_ValidUpdateWithoutCommentChange_KeepsConfirmation() {
        //arrange
        int ratingId = 100;
        int userId = 10;
        int newStars = 4;
        String newComment = "Great movie!";

        Rating existingRating = new Rating(1, userId, 3, "Great movie!");
        existingRating.setId(ratingId);
        existingRating.setConfirmed(true);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);
        when(ratingRepository.save(any(Rating.class))).thenReturn(existingRating);

        //act
        Rating result = ratingService.updateRating(ratingId, newStars, newComment, userId);

        //assert - Same comment, stays confirmed
        assertEquals(newStars, result.getStars());
        assertEquals("Great movie!", result.getComment());
        assertTrue(result.getConfirmed()); //still confirmed
        verify(ratingRepository).save(existingRating);
    }

    @Test
    void updateRating_ValidUpdateWithCommentChange_Unconfirms() {
        //arrange
        int ratingId = 100;
        int userId = 10;
        int newStars = 4;
        String newComment = "Updated comment";

        Rating existingRating = new Rating(1, userId, 3, "Original comment");
        existingRating.setId(ratingId);
        existingRating.setConfirmed(true);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);
        when(ratingRepository.save(any(Rating.class))).thenReturn(existingRating);

        //act
        Rating result = ratingService.updateRating(ratingId, newStars, newComment, userId);

        //assert - Comment changed, becomes unconfirmed
        assertEquals(newStars, result.getStars());
        assertEquals("Updated comment", result.getComment());
        assertFalse(result.getConfirmed()); //now unconfirmed
        verify(ratingRepository).save(existingRating);
    }

    @Test
    void updateRating_RatingNotFound_ThrowsException() {
        //arrange
        int ratingId = 999;
        int userId = 10;
        int stars = 4;
        String comment = "Test";

        when(ratingRepository.getById(ratingId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.updateRating(ratingId, stars, comment, userId)
        );

        assertEquals("Rating not found with ID: " + ratingId, exception.getMessage());
    }

    @Test
    void updateRating_UnauthorizedUser_ThrowsSecurityException() {
        //arrange
        int ratingId = 100;
        int ownerUserId = 10;
        int otherUserId = 20; //not the owner
        int stars = 4;
        String comment = "Test";

        Rating existingRating = new Rating(1, ownerUserId, 3, "Original");
        existingRating.setId(ratingId);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);

        //act & assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> ratingService.updateRating(ratingId, stars, comment, otherUserId)
        );

        assertEquals("User is not authorized to update this rating", exception.getMessage());
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void updateRating_SaveFails_ThrowsRuntimeException() {
        //arrange
        int ratingId = 100;
        int userId = 10;
        int stars = 4;
        String comment = "Test";

        Rating existingRating = new Rating(1, userId, 3, "Original");
        existingRating.setId(ratingId);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);
        when(ratingRepository.save(any(Rating.class))).thenReturn(null);

        //act & assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> ratingService.updateRating(ratingId, stars, comment, userId)
        );

        assertEquals("Failed to update rating", exception.getMessage());
    }

    //confirmRating
    @Test
    void confirmRating_ValidConfirmation_ReturnsTrue() {
        //arrange
        int ratingId = 100;
        int userId = 10;

        Rating existingRating = new Rating(1, userId, 4, "Comment to confirm");
        existingRating.setId(ratingId);
        existingRating.setConfirmed(false);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);
        when(ratingRepository.confirmRating(ratingId)).thenReturn(true);

        //act
        boolean result = ratingService.confirmRating(ratingId, userId);

        //assert
        assertTrue(result);
        verify(ratingRepository).confirmRating(ratingId);
    }

    @Test
    void confirmRating_RatingNotFound_ThrowsException() {
        //arrange
        int ratingId = 999;
        int userId = 10;

        when(ratingRepository.getById(ratingId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.confirmRating(ratingId, userId)
        );

        assertEquals("Rating not found with ID: " + ratingId, exception.getMessage());
    }

    @Test
    void confirmRating_UnauthorizedUser_ThrowsSecurityException() {
        //arrange
        int ratingId = 100;
        int ownerUserId = 10;
        int otherUserId = 20;

        Rating existingRating = new Rating(1, ownerUserId, 4, "Comment");
        existingRating.setId(ratingId);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);

        //act & assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> ratingService.confirmRating(ratingId, otherUserId)
        );

        assertEquals("User can only confirm their own ratings", exception.getMessage());
        verify(ratingRepository, never()).confirmRating(anyInt());
    }

    @Test
    void confirmRating_NoComment_ThrowsException() {
        //arrange
        int ratingId = 100;
        int userId = 10;

        Rating existingRating = new Rating(1, userId, 4, null); // No comment
        existingRating.setId(ratingId);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);

        //act & assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ratingService.confirmRating(ratingId, userId)
        );

        assertEquals("No comment to confirm", exception.getMessage());
        verify(ratingRepository, never()).confirmRating(anyInt());
    }

    @Test
    void confirmRating_EmptyComment_ThrowsException() {
        //arrange
        int ratingId = 100;
        int userId = 10;

        Rating existingRating = new Rating(1, userId, 4, "   "); // Empty/whitespace comment
        existingRating.setId(ratingId);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);

        //act & assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ratingService.confirmRating(ratingId, userId)
        );

        assertEquals("No comment to confirm", exception.getMessage());
    }

    @Test
    void confirmRating_AlreadyConfirmed_ThrowsException() {
        //arrange
        int ratingId = 100;
        int userId = 10;

        Rating existingRating = new Rating(1, userId, 4, "Already confirmed");
        existingRating.setId(ratingId);
        existingRating.setConfirmed(true);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);

        //act & assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ratingService.confirmRating(ratingId, userId)
        );

        assertEquals("Comment is already confirmed", exception.getMessage());
        verify(ratingRepository, never()).confirmRating(anyInt());
    }

    //deleteRating
    @Test
    void deleteRating_ValidDelete_DeletesSuccessfully() {
        //arrange
        int ratingId = 100;
        int userId = 10;

        Rating existingRating = new Rating(1, userId, 4, "Comment");
        existingRating.setId(ratingId);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);
        doNothing().when(ratingRepository).deleteById(existingRating);

        //act & assert - Should not throw
        assertDoesNotThrow(() -> ratingService.deleteRating(ratingId, userId));

        verify(ratingRepository).deleteById(existingRating);
    }

    @Test
    void deleteRating_RatingNotFound_ThrowsException() {
        //arrange
        int ratingId = 999;
        int userId = 10;

        when(ratingRepository.getById(ratingId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.deleteRating(ratingId, userId)
        );

        assertEquals("Rating not found with ID: " + ratingId, exception.getMessage());
        verify(ratingRepository, never()).deleteById(any());
    }

    @Test
    void deleteRating_UnauthorizedUser_ThrowsSecurityException() {
        //arrange
        int ratingId = 100;
        int ownerUserId = 10;
        int otherUserId = 20;

        Rating existingRating = new Rating(1, ownerUserId, 4, "Comment");
        existingRating.setId(ratingId);

        when(ratingRepository.getById(ratingId)).thenReturn(existingRating);

        //act & assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> ratingService.deleteRating(ratingId, otherUserId)
        );

        assertEquals("User is not authorized to delete this rating", exception.getMessage());
        verify(ratingRepository, never()).deleteById(any());
    }

    //likeRating
    @Test
    void likeRating_ValidLike_ReturnsTrue() {
        //arrange
        int ratingId = 100;
        int ratingOwnerId = 10;
        int likingUserId = 20; //different user

        Rating rating = new Rating(1, ratingOwnerId, 4, "Comment");
        rating.setId(ratingId);
        User user = new User("liker", "hash");

        when(ratingRepository.getById(ratingId)).thenReturn(rating);
        when(userRepository.getById(likingUserId)).thenReturn(user);
        when(ratingRepository.likeRating(ratingId, likingUserId)).thenReturn(true);

        //act
        boolean result = ratingService.likeRating(ratingId, likingUserId);

        //assert
        assertTrue(result);
        verify(ratingRepository).likeRating(ratingId, likingUserId);
    }

    @Test
    void likeRating_RatingNotFound_ThrowsException() {
        //arrange
        int ratingId = 999;
        int userId = 20;

        when(ratingRepository.getById(ratingId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.likeRating(ratingId, userId)
        );

        assertEquals("Rating not found with ID: " + ratingId, exception.getMessage());
        verify(userRepository, never()).getById(anyInt());
    }

    @Test
    void likeRating_UserNotFound_ThrowsException() {
        //arrange
        int ratingId = 100;
        int userId = 999;

        Rating rating = new Rating(1, 10, 4, "Comment");
        when(ratingRepository.getById(ratingId)).thenReturn(rating);
        when(userRepository.getById(userId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.likeRating(ratingId, userId)
        );

        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(ratingRepository, never()).likeRating(anyInt(), anyInt());
    }

    @Test
    void likeRating_UserLikesOwnRating_ThrowsException() {
        //arrange
        int ratingId = 100;
        int userId = 10; //same as rating owner

        Rating rating = new Rating(1, userId, 4, "Comment");
        rating.setId(ratingId);
        User user = new User("owner", "hash");

        when(ratingRepository.getById(ratingId)).thenReturn(rating);
        when(userRepository.getById(userId)).thenReturn(user);

        //act & assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ratingService.likeRating(ratingId, userId)
        );

        assertEquals("Users cannot like their own ratings", exception.getMessage());
        verify(ratingRepository, never()).likeRating(anyInt(), anyInt());
    }

    //getRatingLikeCount
    @Test
    void getRatingLikeCount_ValidRating_ReturnsCount() {
        //arrange
        int ratingId = 100;
        int expectedCount = 42;

        Rating rating = new Rating(1, 10, 4, "Comment");
        when(ratingRepository.getById(ratingId)).thenReturn(rating);
        when(ratingRepository.getLikeCount(ratingId)).thenReturn(expectedCount);

        //act
        int result = ratingService.getRatingLikeCount(ratingId);

        //assert
        assertEquals(expectedCount, result);
        verify(ratingRepository).getLikeCount(ratingId);
    }

    @Test
    void getRatingLikeCount_RatingNotFound_ThrowsException() {
        //arrange
        int ratingId = 999;

        when(ratingRepository.getById(ratingId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.getRatingLikeCount(ratingId)
        );

        assertEquals("Rating not found with ID: " + ratingId, exception.getMessage());
        verify(ratingRepository, never()).getLikeCount(anyInt());
    }

    //getRatingById
    @Test
    void getRatingById_ValidRating_ReturnsRating() {
        //arrange
        int ratingId = 100;
        Rating expectedRating = new Rating(1, 10, 4, "Comment");
        expectedRating.setId(ratingId);

        when(ratingRepository.getById(ratingId)).thenReturn(expectedRating);

        //act
        Rating result = ratingService.getRatingById(ratingId);

        //assert
        assertSame(expectedRating, result);
    }

    @Test
    void getRatingById_RatingNotFound_ThrowsException() {
        //arrange
        int ratingId = 999;

        when(ratingRepository.getById(ratingId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.getRatingById(ratingId)
        );

        assertEquals("Rating not found with ID: " + ratingId, exception.getMessage());
    }
}
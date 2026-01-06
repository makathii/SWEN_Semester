package at.technikum_wien.services;

import at.technikum_wien.database.repositories.MediaRepository;
import at.technikum_wien.database.repositories.RatingRepository;
import at.technikum_wien.database.repositories.FavoriteRepository;
import at.technikum_wien.models.entities.Media;
import at.technikum_wien.models.entities.Rating;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    //helper methods for creating test data
    private Media createMedia(int id, String title, List<String> genres, String type, int ageRestriction) {
        Media media = new Media();
        media.setId(id);
        media.setTitle(title);
        media.setGenres(genres);
        media.setType(type);
        media.setAge_restriction(ageRestriction);
        return media;
    }

    private Rating createRating(int id, int mediaId, int userId, int stars) {
        Rating rating = new Rating(mediaId, userId, stars, "Comment");
        rating.setId(id);
        return rating;
    }

    //getGenreBasedRecommendations
    @Test
    void getGenreBasedRecommendations_UserHasHighRatings_ReturnsGenreBasedRecommendations() {
        //arrange
        int userId = 1;
        int limit = 5;

        //user's high ratings (4+ stars)
        Rating highRating1 = createRating(1, 101, userId, 5); //action movie
        Rating highRating2 = createRating(2, 102, userId, 4); //another action movie

        List<Rating> userRatings = Arrays.asList(highRating1, highRating2);

        //media data
        Media ratedMedia1 = createMedia(101, "Action Movie 1", Arrays.asList("action", "adventure"), "movie", 12);
        Media ratedMedia2 = createMedia(102, "Action Movie 2", Arrays.asList("action", "thriller"), "movie", 16);

        //available media for recommendations
        Media unratedMedia1 = createMedia(201, "Action Adventure", Arrays.asList("action", "adventure"), "movie", 12);
        Media unratedMedia2 = createMedia(202, "Romantic Comedy", Arrays.asList("comedy", "romance"), "movie", 12);
        Media unratedMedia3 = createMedia(203, "Sci-Fi Action", Arrays.asList("sci-fi", "action"), "movie", 12);

        List<Media> allMedia = Arrays.asList(ratedMedia1, ratedMedia2, unratedMedia1, unratedMedia2, unratedMedia3);

        when(ratingRepository.getAllRatingsByUser(userId)).thenReturn(userRatings);
        when(mediaRepository.getAllMedia()).thenReturn(allMedia);

        //mock getById calls in calculateFavoriteGenres
        when(mediaRepository.getById(101)).thenReturn(ratedMedia1);
        when(mediaRepository.getById(102)).thenReturn(ratedMedia2);

        //mock popularity calculations
        when(ratingRepository.getAllRatingsByMedia(anyInt())).thenReturn(Arrays.asList(
                createRating(10, 201, 2, 4),
                createRating(11, 202, 3, 3)
        ));
        when(favoriteRepository.getFavoriteCountForMedia(anyInt())).thenReturn(10);

        //act
        List<Media> result = recommendationService.getGenreBasedRecommendations(userId, limit);

        //assert
        assertNotNull(result);
        assertEquals(3, result.size()); //only unrated media
        //should prioritize action genre media
        assertEquals(201, result.get(0).getId()); //action Adventure has highest genre match
        //don't test exact order as it depends on popularity calculation

        verify(ratingRepository).getAllRatingsByUser(userId);
        verify(mediaRepository).getAllMedia();
    }

    @Test
    void getGenreBasedRecommendations_ExcludesAlreadyRatedMedia() {
        //arrange
        int userId = 1;
        int limit = 5;

        //user has rated some media
        Rating rating1 = createRating(1, 101, userId, 5);
        Rating rating2 = createRating(2, 102, userId, 4);

        List<Rating> userRatings = Arrays.asList(rating1, rating2);

        //create media - including already rated ones
        Media ratedMedia1 = createMedia(101, "Rated Movie 1", Arrays.asList("action"), "movie", 12);
        Media ratedMedia2 = createMedia(102, "Rated Movie 2", Arrays.asList("comedy"), "movie", 12);
        Media unratedMedia1 = createMedia(201, "Unrated Movie 1", Arrays.asList("action"), "movie", 12);
        Media unratedMedia2 = createMedia(202, "Unrated Movie 2", Arrays.asList("comedy"), "movie", 12);

        List<Media> allMedia = Arrays.asList(ratedMedia1, ratedMedia2, unratedMedia1, unratedMedia2);

        when(ratingRepository.getAllRatingsByUser(userId)).thenReturn(userRatings);
        when(mediaRepository.getAllMedia()).thenReturn(allMedia);

        //mock getById for rated media
        when(mediaRepository.getById(101)).thenReturn(ratedMedia1);
        when(mediaRepository.getById(102)).thenReturn(ratedMedia2);

        //mock popularity calculations
        when(ratingRepository.getAllRatingsByMedia(anyInt())).thenReturn(Arrays.asList(
                createRating(10, 201, 2, 4),
                createRating(11, 202, 3, 5)
        ));
        when(favoriteRepository.getFavoriteCountForMedia(anyInt())).thenReturn(5);

        //act
        List<Media> result = recommendationService.getGenreBasedRecommendations(userId, limit);

        //assert - Should not include already rated media
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(media -> media.getId() == 101 || media.getId() == 102));
        assertTrue(result.stream().allMatch(media -> media.getId() == 201 || media.getId() == 202));
    }

    @Test
    void getGenreBasedRecommendations_LimitExceedsAvailableMedia_ReturnsAllAvailable() {
        //arrange
        int userId = 1;
        int limit = 10; //high limit

        Rating rating = createRating(1, 101, userId, 5);
        List<Rating> userRatings = Collections.singletonList(rating);

        Media ratedMedia = createMedia(101, "Rated", Arrays.asList("action"), "movie", 12);
        Media unratedMedia1 = createMedia(201, "Unrated 1", Arrays.asList("action"), "movie", 12);
        Media unratedMedia2 = createMedia(202, "Unrated 2", Arrays.asList("drama"), "movie", 12);

        List<Media> allMedia = Arrays.asList(ratedMedia, unratedMedia1, unratedMedia2);

        when(ratingRepository.getAllRatingsByUser(userId)).thenReturn(userRatings);
        when(mediaRepository.getAllMedia()).thenReturn(allMedia);
        when(mediaRepository.getById(101)).thenReturn(ratedMedia);

        //mock popularity calculations
        when(ratingRepository.getAllRatingsByMedia(anyInt())).thenReturn(Collections.emptyList());
        when(favoriteRepository.getFavoriteCountForMedia(anyInt())).thenReturn(0);

        //act
        List<Media> result = recommendationService.getGenreBasedRecommendations(userId, limit);

        //assert - Only 2 unrated media available
        assertEquals(2, result.size());
    }

    //getContentBasedRecommendations
    @Test
    void getContentBasedRecommendations_UserHasRatings_ReturnsContentBasedRecommendations() {
        //arrange
        int userId = 1;
        int limit = 3;

        //user ratings
        Rating rating1 = createRating(1, 101, userId, 5); //action movie
        Rating rating2 = createRating(2, 102, userId, 4); //another action movie

        List<Rating> userRatings = Arrays.asList(rating1, rating2);

        //rated media
        Media ratedMedia1 = createMedia(101, "Action Movie 1", Arrays.asList("action"), "movie", 12);
        Media ratedMedia2 = createMedia(102, "Action Movie 2", Arrays.asList("action", "adventure"), "movie", 16);

        //available unrated media
        Media unratedMedia1 = createMedia(201, "Similar Action", Arrays.asList("action"), "movie", 12); //high match
        Media unratedMedia2 = createMedia(202, "Different Genre", Arrays.asList("comedy"), "show", 12); //low match
        Media unratedMedia3 = createMedia(203, "Different Type", Arrays.asList("action"), "show", 12); //medium match

        List<Media> allMedia = Arrays.asList(ratedMedia1, ratedMedia2, unratedMedia1, unratedMedia2, unratedMedia3);

        when(ratingRepository.getAllRatingsByUser(userId)).thenReturn(userRatings);
        when(mediaRepository.getAllMedia()).thenReturn(allMedia);

        //mock getById calls
        when(mediaRepository.getById(101)).thenReturn(ratedMedia1);
        when(mediaRepository.getById(102)).thenReturn(ratedMedia2);

        //mock popularity calculations
        when(ratingRepository.getAllRatingsByMedia(anyInt())).thenReturn(Arrays.asList(
                createRating(10, 201, 2, 4),
                createRating(11, 202, 3, 3),
                createRating(12, 203, 4, 5)
        ));
        when(favoriteRepository.getFavoriteCountForMedia(anyInt())).thenReturn(5);

        //act
        List<Media> result = recommendationService.getContentBasedRecommendations(userId, limit);

        //assert
        assertNotNull(result);
        assertEquals(3, result.size());
        //should prioritize similar content (action genre, movie type, similar age restriction)
        verify(ratingRepository).getAllRatingsByUser(userId);
    }

    @Test
    void getContentBasedRecommendations_NoUserRatings_ReturnsPopularMedia() {
        //arrange
        int userId = 1;
        int limit = 3;

        when(ratingRepository.getAllRatingsByUser(userId)).thenReturn(Collections.emptyList());

        Media media1 = createMedia(1, "Popular Movie 1", Arrays.asList("action"), "movie", 12);
        Media media2 = createMedia(2, "Popular Movie 2", Arrays.asList("drama"), "movie", 12);
        Media media3 = createMedia(3, "Popular Movie 3", Arrays.asList("comedy"), "movie", 12);

        List<Media> allMedia = Arrays.asList(media1, media2, media3);
        when(mediaRepository.getAllMedia()).thenReturn(allMedia);

        //mock popularity calculations
        when(ratingRepository.getAllRatingsByMedia(anyInt())).thenReturn(Arrays.asList(
                createRating(10, 1, 2, 5),
                createRating(11, 2, 3, 4),
                createRating(12, 3, 4, 3)
        ));
        when(favoriteRepository.getFavoriteCountForMedia(anyInt())).thenReturn(10);

        //act
        List<Media> result = recommendationService.getContentBasedRecommendations(userId, limit);

        //assert
        assertNotNull(result);
        assertEquals(limit, result.size());
        //should return popular media in descending order
    }

    @Test
    void getContentBasedRecommendations_ConsidersMediaTypePreference() {
        //arrange
        int userId = 1;
        int limit = 3;

        //user has rated only shows (not movies)
        Rating rating1 = createRating(1, 101, userId, 5); //show
        Rating rating2 = createRating(2, 102, userId, 4); //show

        List<Rating> userRatings = Arrays.asList(rating1, rating2);

        Media ratedMedia1 = createMedia(101, "TV Show 1", Arrays.asList("drama"), "show", 16);
        Media ratedMedia2 = createMedia(102, "TV Show 2", Arrays.asList("comedy"), "show", 12);

        Media unratedMedia1 = createMedia(201, "Another Show", Arrays.asList("drama"), "show", 16); // Same type
        Media unratedMedia2 = createMedia(202, "A Movie", Arrays.asList("drama"), "movie", 16); // Different type

        List<Media> allMedia = Arrays.asList(ratedMedia1, ratedMedia2, unratedMedia1, unratedMedia2);

        when(ratingRepository.getAllRatingsByUser(userId)).thenReturn(userRatings);
        when(mediaRepository.getAllMedia()).thenReturn(allMedia);
        when(mediaRepository.getById(101)).thenReturn(ratedMedia1);
        when(mediaRepository.getById(102)).thenReturn(ratedMedia2);

        //mock popularity calculations (simplified)
        when(ratingRepository.getAllRatingsByMedia(anyInt())).thenReturn(Collections.emptyList());
        when(favoriteRepository.getFavoriteCountForMedia(anyInt())).thenReturn(0);

        //act
        List<Media> result = recommendationService.getContentBasedRecommendations(userId, limit);

        //assert - Should prefer shows over movies
        assertEquals(2, result.size()); //only 2 unrated media
        //the show should be preferred (though exact order depends on other factors)
    }

    @Test
    void getContentBasedRecommendations_ConsidersAgeRestriction() {
        //arrange
        int userId = 1;
        int limit = 3;

        //user has rated adult content (age restriction 18)
        Rating rating1 = createRating(1, 101, userId, 5);
        Rating rating2 = createRating(2, 102, userId, 4);

        List<Rating> userRatings = Arrays.asList(rating1, rating2);

        Media ratedMedia1 = createMedia(101, "Adult Movie 1", Arrays.asList("action"), "movie", 18);
        Media ratedMedia2 = createMedia(102, "Adult Movie 2", Arrays.asList("drama"), "movie", 18);

        Media unratedMedia1 = createMedia(201, "Similar Age", Arrays.asList("action"), "movie", 18); // Same age
        Media unratedMedia2 = createMedia(202, "Different Age", Arrays.asList("action"), "movie", 12); // Different age

        List<Media> allMedia = Arrays.asList(ratedMedia1, ratedMedia2, unratedMedia1, unratedMedia2);

        when(ratingRepository.getAllRatingsByUser(userId)).thenReturn(userRatings);
        when(mediaRepository.getAllMedia()).thenReturn(allMedia);
        when(mediaRepository.getById(101)).thenReturn(ratedMedia1);
        when(mediaRepository.getById(102)).thenReturn(ratedMedia2);

        //mock popularity calculations
        when(ratingRepository.getAllRatingsByMedia(anyInt())).thenReturn(Collections.emptyList());
        when(favoriteRepository.getFavoriteCountForMedia(anyInt())).thenReturn(0);

        //act
        List<Media> result = recommendationService.getContentBasedRecommendations(userId, limit);

        //assert - Media with similar age restriction should be preferred
        assertEquals(2, result.size());
        //the 18+ media should score higher in age similarity
    }

    //EDGE CASE TESTS
    @Test
    void getGenreBasedRecommendations_EmptyMediaRepository_ReturnsEmptyList() {
        //arrange
        int userId = 1;
        int limit = 5;

        when(ratingRepository.getAllRatingsByUser(userId)).thenReturn(Collections.emptyList());
        when(mediaRepository.getAllMedia()).thenReturn(Collections.emptyList());

        //act
        List<Media> result = recommendationService.getGenreBasedRecommendations(userId, limit);

        //assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
package at.technikum_wien.services;

import at.technikum_wien.database.repositories.LeaderboardRepository;
import at.technikum_wien.models.entities.LeaderboardEntry;
import at.technikum_wien.models.entities.TopRatedMedia;
import at.technikum_wien.models.entities.MostLikedRating;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    //helper methods
    private LeaderboardEntry createLeaderboardEntry(int userId, String username, int activityScore) {
        LeaderboardEntry entry = new LeaderboardEntry();
        entry.setUserId(userId);
        entry.setUsername(username);
        entry.setActivityScore(activityScore);
        return entry;
    }

    private TopRatedMedia createTopRatedMedia(int mediaId, String title, double averageRating) {
        TopRatedMedia media = new TopRatedMedia();
        media.setMediaId(mediaId);
        media.setTitle(title);
        media.setAverageRating(averageRating);
        return media;
    }

    private MostLikedRating createMostLikedRating(int ratingId, String username, String comment, int likeCount) {
        MostLikedRating rating = new MostLikedRating();
        rating.setRatingId(ratingId);
        rating.setAuthorName(username);
        rating.setComment(comment);
        rating.setLikeCount(likeCount);
        return rating;
    }

    //getMostActiveUsers
    @Test
    void getMostActiveUsers_ValidLimit_ReturnsUsers() {
        //arrange
        int limit = 10;
        List<LeaderboardEntry> expectedEntries = List.of(
                createLeaderboardEntry(1, "user1", 50),
                createLeaderboardEntry(2, "user2", 30),
                createLeaderboardEntry(3, "user3", 20)
        );

        when(leaderboardRepository.getMostActiveUsers(limit)).thenReturn(expectedEntries);

        //act
        List<LeaderboardEntry> result = leaderboardService.getMostActiveUsers(limit);

        //assert
        assertNotNull(result);
        assertEquals(expectedEntries, result);
        verify(leaderboardRepository).getMostActiveUsers(limit);
    }

    @Test
    void getMostActiveUsers_ZeroLimit_UsesDefault() {
        //arrange
        int limit = 0;
        List<LeaderboardEntry> expectedEntries = List.of(
                createLeaderboardEntry(1, "user1", 50),
                createLeaderboardEntry(2, "user2", 30)
        );

        when(leaderboardRepository.getMostActiveUsers(20)).thenReturn(expectedEntries);

        //act
        List<LeaderboardEntry> result = leaderboardService.getMostActiveUsers(limit);

        //assert - should use default limit of 20
        assertNotNull(result);
        assertEquals(expectedEntries, result);
        verify(leaderboardRepository).getMostActiveUsers(20);
    }

    @Test
    void getMostActiveUsers_NegativeLimit_UsesDefault() {
        //arrange
        int limit = -5;
        List<LeaderboardEntry> expectedEntries = List.of(
                createLeaderboardEntry(1, "user1", 50)
        );

        when(leaderboardRepository.getMostActiveUsers(20)).thenReturn(expectedEntries);

        //act
        List<LeaderboardEntry> result = leaderboardService.getMostActiveUsers(limit);

        //assert - should use default limit of 20
        assertNotNull(result);
        assertEquals(expectedEntries, result);
        verify(leaderboardRepository).getMostActiveUsers(20);
    }

    @Test
    void getMostActiveUsers_LimitAboveMaximum_UsesDefault() {
        //arrange
        int limit = 150; //above max of 100
        List<LeaderboardEntry> expectedEntries = List.of(
                createLeaderboardEntry(1, "user1", 50)
        );

        when(leaderboardRepository.getMostActiveUsers(20)).thenReturn(expectedEntries);

        //act
        List<LeaderboardEntry> result = leaderboardService.getMostActiveUsers(limit);

        //assert - should use default limit of 20
        assertNotNull(result);
        assertEquals(expectedEntries, result);
        verify(leaderboardRepository).getMostActiveUsers(20);
    }

    @Test
    void getMostActiveUsers_EmptyResult_ReturnsEmptyList() {
        //arrange
        int limit = 10;
        when(leaderboardRepository.getMostActiveUsers(limit)).thenReturn(List.of());

        //act
        List<LeaderboardEntry> result = leaderboardService.getMostActiveUsers(limit);

        //assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(leaderboardRepository).getMostActiveUsers(limit);
    }

    @Test
    void getMostActiveUsers_MaximumLimit_ReturnsUsers() {
        //arrange
        int limit = 100; //maximum allowed
        List<LeaderboardEntry> expectedEntries = List.of(
                createLeaderboardEntry(1, "user1", 100),
                createLeaderboardEntry(2, "user2", 90)
        );

        when(leaderboardRepository.getMostActiveUsers(limit)).thenReturn(expectedEntries);

        //act
        List<LeaderboardEntry> result = leaderboardService.getMostActiveUsers(limit);

        //assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(leaderboardRepository).getMostActiveUsers(100);
    }

    //getTopRatedMedia
    @Test
    void getTopRatedMedia_ValidLimit_ReturnsMedia() {
        //arrange
        int limit = 5;
        List<TopRatedMedia> expectedMedia = List.of(
                createTopRatedMedia(1, "Movie 1", 4.8),
                createTopRatedMedia(2, "Movie 2", 4.5),
                createTopRatedMedia(3, "Movie 3", 4.2)
        );

        when(leaderboardRepository.getTopRatedMedia(limit)).thenReturn(expectedMedia);

        //act
        List<TopRatedMedia> result = leaderboardService.getTopRatedMedia(limit);

        //assert
        assertNotNull(result);
        assertEquals(expectedMedia, result);
        verify(leaderboardRepository).getTopRatedMedia(limit);
    }

    @Test
    void getTopRatedMedia_ZeroLimit_UsesDefault() {
        //arrange
        int limit = 0;
        List<TopRatedMedia> expectedMedia = List.of(
                createTopRatedMedia(1, "Movie 1", 4.8)
        );

        when(leaderboardRepository.getTopRatedMedia(20)).thenReturn(expectedMedia);

        //act
        List<TopRatedMedia> result = leaderboardService.getTopRatedMedia(limit);

        //assert - should use default limit of 20
        assertNotNull(result);
        assertEquals(expectedMedia, result);
        verify(leaderboardRepository).getTopRatedMedia(20);
    }

    @Test
    void getTopRatedMedia_NegativeLimit_UsesDefault() {
        //arrange
        int limit = -10;
        when(leaderboardRepository.getTopRatedMedia(20)).thenReturn(List.of());

        //act
        List<TopRatedMedia> result = leaderboardService.getTopRatedMedia(limit);

        //assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(leaderboardRepository).getTopRatedMedia(20);
    }

    @Test
    void getTopRatedMedia_LimitAtBoundary_WorksCorrectly() {
        //Test boundary: limit = 1 (minimum positive)
        //arrange
        int limit = 1;
        List<TopRatedMedia> expectedMedia = List.of(
                createTopRatedMedia(1, "Top Movie", 5.0)
        );

        when(leaderboardRepository.getTopRatedMedia(limit)).thenReturn(expectedMedia);

        //act
        List<TopRatedMedia> result = leaderboardService.getTopRatedMedia(limit);

        //assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(leaderboardRepository).getTopRatedMedia(1);
    }

    //getMostLikedRatings
    @Test
    void getMostLikedRatings_ValidLimit_ReturnsRatings() {
        //arrange
        int limit = 15;
        List<MostLikedRating> expectedRatings = List.of(
                createMostLikedRating(1, "user1", "Great review!", 100),
                createMostLikedRating(2, "user2", "Awesome!", 80),
                createMostLikedRating(3, "user3", "Loved it", 60)
        );

        when(leaderboardRepository.getMostLikedRatings(limit)).thenReturn(expectedRatings);

        //act
        List<MostLikedRating> result = leaderboardService.getMostLikedRatings(limit);

        //assert
        assertNotNull(result);
        assertEquals(expectedRatings, result);
        verify(leaderboardRepository).getMostLikedRatings(limit);
    }

    @Test
    void getMostLikedRatings_LimitTooHigh_UsesDefault() {
        //arrange
        int limit = 200; //above max of 100
        List<MostLikedRating> expectedRatings = List.of(
                createMostLikedRating(1, "user1", "Review", 50)
        );

        when(leaderboardRepository.getMostLikedRatings(20)).thenReturn(expectedRatings);

        //act
        List<MostLikedRating> result = leaderboardService.getMostLikedRatings(limit);

        //assert - should use default limit of 20
        assertNotNull(result);
        assertEquals(expectedRatings, result);
        verify(leaderboardRepository).getMostLikedRatings(20);
    }

    @Test
    void getMostLikedRatings_RepositoryReturnsNull_HandlesGracefully() {
        //arrange
        int limit = 10;
        when(leaderboardRepository.getMostLikedRatings(limit)).thenReturn(null);

        //act
        List<MostLikedRating> result = leaderboardService.getMostLikedRatings(limit);

        //assert
        assertNull(result);
        verify(leaderboardRepository).getMostLikedRatings(limit);
    }

    //getTrendingGenres
    @Test
    void getTrendingGenres_ReturnsGenres() {
        //arrange
        List<String> expectedGenres = List.of("Action", "Drama", "Comedy", "Sci-Fi");
        when(leaderboardRepository.getTrendingGenres()).thenReturn(expectedGenres);

        //act
        List<String> result = leaderboardService.getTrendingGenres();

        //assert
        assertNotNull(result);
        assertEquals(expectedGenres, result);
        verify(leaderboardRepository).getTrendingGenres();
    }

    @Test
    void getTrendingGenres_EmptyResult_ReturnsEmptyList() {
        //arrange
        when(leaderboardRepository.getTrendingGenres()).thenReturn(List.of());

        //act
        List<String> result = leaderboardService.getTrendingGenres();

        //assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTrendingGenres_RepositoryReturnsNull_ReturnsNull() {
        //arrange
        when(leaderboardRepository.getTrendingGenres()).thenReturn(null);

        //act
        List<String> result = leaderboardService.getTrendingGenres();

        //assert
        assertNull(result);
    }

    //EDGE CASE TESTS
    @Test
    void allMethods_WithBoundaryLimits_WorkCorrectly() {
        //test each method with limit exactly at boundary

        //test with limit = 1 (minimum positive)
        when(leaderboardRepository.getMostActiveUsers(1)).thenReturn(List.of(createLeaderboardEntry(1, "user", 10)));
        when(leaderboardRepository.getTopRatedMedia(1)).thenReturn(List.of(createTopRatedMedia(1, "Movie", 5.0)));
        when(leaderboardRepository.getMostLikedRatings(1)).thenReturn(List.of(createMostLikedRating(1, "user", "Review", 10)));

        assertNotNull(leaderboardService.getMostActiveUsers(1));
        assertNotNull(leaderboardService.getTopRatedMedia(1));
        assertNotNull(leaderboardService.getMostLikedRatings(1));

        //test with limit = 100 (maximum allowed)
        when(leaderboardRepository.getMostActiveUsers(100)).thenReturn(List.of(createLeaderboardEntry(1, "user", 10)));
        when(leaderboardRepository.getTopRatedMedia(100)).thenReturn(List.of(createTopRatedMedia(1, "Movie", 5.0)));
        when(leaderboardRepository.getMostLikedRatings(100)).thenReturn(List.of(createMostLikedRating(1, "user", "Review", 10)));

        assertNotNull(leaderboardService.getMostActiveUsers(100));
        assertNotNull(leaderboardService.getTopRatedMedia(100));
        assertNotNull(leaderboardService.getMostLikedRatings(100));
    }

    @Test
    void allMethods_ConsistentlyUseDefaultForInvalidLimits() {
        //test that all three limit-based methods use the same default logic

        //arrange
        when(leaderboardRepository.getMostActiveUsers(20)).thenReturn(List.of(createLeaderboardEntry(1, "user1", 50)));
        when(leaderboardRepository.getTopRatedMedia(20)).thenReturn(List.of(createTopRatedMedia(1, "Movie", 4.5)));
        when(leaderboardRepository.getMostLikedRatings(20)).thenReturn(List.of(createMostLikedRating(1, "user", "Review", 20)));

        //act - Test with various invalid limits
        leaderboardService.getMostActiveUsers(0);
        leaderboardService.getMostActiveUsers(-5);
        leaderboardService.getMostActiveUsers(101);

        leaderboardService.getTopRatedMedia(0);
        leaderboardService.getTopRatedMedia(-10);
        leaderboardService.getTopRatedMedia(150);

        leaderboardService.getMostLikedRatings(0);
        leaderboardService.getMostLikedRatings(-1);
        leaderboardService.getMostLikedRatings(200);

        //assert - All should use default limit of 20
        verify(leaderboardRepository, times(3)).getMostActiveUsers(20);
        verify(leaderboardRepository, times(3)).getTopRatedMedia(20);
        verify(leaderboardRepository, times(3)).getMostLikedRatings(20);
    }

    @Test
    void getTrendingGenres_UpperCaseLowerCase_ReturnsAsIs() {
        //test that genre case is preserved
        //arrange
        List<String> mixedCaseGenres = List.of("ACTION", "drama", "Sci-Fi", "comedy");
        when(leaderboardRepository.getTrendingGenres()).thenReturn(mixedCaseGenres);

        //act
        List<String> result = leaderboardService.getTrendingGenres();

        //assert
        assertEquals(mixedCaseGenres, result);
    }

    @Test
    void multipleCalls_WithSameLimit_CallsRepositoryEachTime() {
        //tests that repeated calls with same parameters work correctly
        //arrange
        int limit = 5;
        List<LeaderboardEntry> expected = List.of(
                createLeaderboardEntry(1, "user1", 30),
                createLeaderboardEntry(2, "user2", 20)
        );

        when(leaderboardRepository.getMostActiveUsers(limit)).thenReturn(expected);

        //act - call multiple times
        List<LeaderboardEntry> result1 = leaderboardService.getMostActiveUsers(limit);
        List<LeaderboardEntry> result2 = leaderboardService.getMostActiveUsers(limit);

        //assert
        assertEquals(expected, result1);
        assertEquals(expected, result2);
        //repository should be called each time
        verify(leaderboardRepository, times(2)).getMostActiveUsers(limit);
    }

    @Test
    void getMostActiveUsers_ValidPositiveLimit_ReturnsCorrectOrder() {
        //test that results are returned in correct order (should be sorted by repository)
        //arrange
        int limit = 3;
        List<LeaderboardEntry> expectedEntries = List.of(
                createLeaderboardEntry(1, "topUser", 100),
                createLeaderboardEntry(2, "middleUser", 75),
                createLeaderboardEntry(3, "lowUser", 50)
        );

        when(leaderboardRepository.getMostActiveUsers(limit)).thenReturn(expectedEntries);

        //act
        List<LeaderboardEntry> result = leaderboardService.getMostActiveUsers(limit);

        //assert
        assertEquals(3, result.size());
        assertEquals(100, result.get(0).getActivityScore());
        assertEquals(50, result.get(2).getActivityScore());
    }

    @Test
    void serviceMethods_WithValidData_ReturnImmutableLists() {
        //test that the service doesn't modify the lists returned by repository
        //arrange
        int limit = 5;
        List<LeaderboardEntry> repositoryResult = List.of(createLeaderboardEntry(1, "user", 10));
        when(leaderboardRepository.getMostActiveUsers(limit)).thenReturn(repositoryResult);

        //act
        List<LeaderboardEntry> serviceResult = leaderboardService.getMostActiveUsers(limit);

        //assert - should be the same list (not a copy)
        assertSame(repositoryResult, serviceResult);
    }

    @Test
    void getTrendingGenres_LimitNotApplied_ReturnsAllGenres() {
        //test that getTrendingGenres doesn't have a limit parameter
        //arrange
        List<String> manyGenres = List.of("Action", "Drama", "Comedy", "Sci-Fi", "Horror",
                "Romance", "Thriller", "Documentary", "Animation");
        when(leaderboardRepository.getTrendingGenres()).thenReturn(manyGenres);

        //act
        List<String> result = leaderboardService.getTrendingGenres();

        //assert - should return all genres (no limit applied)
        assertEquals(9, result.size());
        verify(leaderboardRepository).getTrendingGenres();
    }
}
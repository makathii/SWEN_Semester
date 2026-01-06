package at.technikum_wien.services;

import at.technikum_wien.database.repositories.FavoriteRepository;
import at.technikum_wien.database.repositories.MediaRepository;
import at.technikum_wien.database.repositories.UserRepository;
import at.technikum_wien.models.entities.Favorite;
import at.technikum_wien.models.entities.Media;
import at.technikum_wien.models.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    //addFavorite
    @Test
    void addFavorite_ValidInput_CreatesAndReturnsTrue() {
        //arrange
        int userId = 1;
        int mediaId = 100;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(userId, mediaId)).thenReturn(false);
        when(favoriteRepository.addFavorite(any(Favorite.class))).thenReturn(true);

        //act
        boolean result = favoriteService.addFavorite(userId, mediaId);

        //assert
        assertTrue(result);
        verify(favoriteRepository).addFavorite(argThat(favorite ->
                favorite.getUserId() == userId && favorite.getMediaId() == mediaId
        ));
    }

    @Test
    void addFavorite_UserNotFound_ThrowsException() {
        //arrange
        int userId = 999;
        int mediaId = 100;

        when(userRepository.getById(userId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> favoriteService.addFavorite(userId, mediaId)
        );

        assertEquals("User not found", exception.getMessage());
        verify(mediaRepository, never()).getById(anyInt());
        verify(favoriteRepository, never()).isFavorite(anyInt(), anyInt());
        verify(favoriteRepository, never()).addFavorite(any());
    }

    @Test
    void addFavorite_MediaNotFound_ThrowsException() {
        //arrange
        int userId = 1;
        int mediaId = 999;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> favoriteService.addFavorite(userId, mediaId)
        );

        assertEquals("Media not found", exception.getMessage());
        verify(favoriteRepository, never()).isFavorite(anyInt(), anyInt());
        verify(favoriteRepository, never()).addFavorite(any());
    }

    @Test
    void addFavorite_AlreadyExists_ThrowsException() {
        //arrange
        int userId = 1;
        int mediaId = 100;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(userId, mediaId)).thenReturn(true);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> favoriteService.addFavorite(userId, mediaId)
        );

        assertEquals("Favorite already exists", exception.getMessage());
        verify(favoriteRepository, never()).addFavorite(any());
    }

    @Test
    void addFavorite_RepositoryReturnsFalse_ReturnsFalse() {
        //arrange
        int userId = 1;
        int mediaId = 100;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(userId, mediaId)).thenReturn(false);
        when(favoriteRepository.addFavorite(any(Favorite.class))).thenReturn(false);

        //act
        boolean result = favoriteService.addFavorite(userId, mediaId);

        //assert
        assertFalse(result);
        verify(favoriteRepository).addFavorite(any(Favorite.class));
    }

    //removeFavorite
    @Test
    void removeFavorite_ValidInput_RemovesAndReturnsTrue() {
        //arrange
        int userId = 1;
        int mediaId = 100;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(userId, mediaId)).thenReturn(true);
        when(favoriteRepository.deleteFavorite(userId, mediaId)).thenReturn(true);

        //act
        boolean result = favoriteService.removeFavorite(userId, mediaId);

        //assert
        assertTrue(result);
        verify(favoriteRepository).deleteFavorite(userId, mediaId);
    }

    @Test
    void removeFavorite_UserNotFound_ThrowsException() {
        //arrange
        int userId = 999;
        int mediaId = 100;

        when(userRepository.getById(userId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> favoriteService.removeFavorite(userId, mediaId)
        );

        assertEquals("User not found", exception.getMessage());
        verify(favoriteRepository, never()).deleteFavorite(anyInt(), anyInt());
    }

    @Test
    void removeFavorite_MediaNotFound_ThrowsException() {
        //arrange
        int userId = 1;
        int mediaId = 999;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> favoriteService.removeFavorite(userId, mediaId)
        );

        assertEquals("Media not found", exception.getMessage());
        verify(favoriteRepository, never()).isFavorite(anyInt(), anyInt());
        verify(favoriteRepository, never()).deleteFavorite(anyInt(), anyInt());
    }

    @Test
    void removeFavorite_NotInFavorites_ThrowsException() {
        //arrange
        int userId = 1;
        int mediaId = 100;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(userId, mediaId)).thenReturn(false);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> favoriteService.removeFavorite(userId, mediaId)
        );

        assertEquals("Media is not in favorites", exception.getMessage());
        verify(favoriteRepository, never()).deleteFavorite(anyInt(), anyInt());
    }

    @Test
    void removeFavorite_RepositoryReturnsFalse_ReturnsFalse() {
        //arrange
        int userId = 1;
        int mediaId = 100;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(userId, mediaId)).thenReturn(true);
        when(favoriteRepository.deleteFavorite(userId, mediaId)).thenReturn(false);

        //act
        boolean result = favoriteService.removeFavorite(userId, mediaId);

        //assert
        assertFalse(result);
        verify(favoriteRepository).deleteFavorite(userId, mediaId);
    }

    //getUserFavorites
    @Test
    void getUserFavorites_ValidUser_ReturnsMediaList() {
        //arrange
        int userId = 1;
        List<Media> expectedFavorites = Arrays.asList(
                new Media(),
                new Media()
        );

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(favoriteRepository.getUserFavorites(userId)).thenReturn(expectedFavorites);

        //act
        List<Media> result = favoriteService.getUserFavorites(userId);

        //assert
        assertEquals(expectedFavorites, result);
        assertSame(expectedFavorites, result); //verify it's the same list from repository
        verify(favoriteRepository).getUserFavorites(userId);
    }

    @Test
    void getUserFavorites_UserNotFound_ThrowsException() {
        //arrange
        int userId = 999;

        when(userRepository.getById(userId)).thenReturn(null);

        //act & assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> favoriteService.getUserFavorites(userId)
        );

        assertEquals("User not found", exception.getMessage());
        verify(favoriteRepository, never()).getUserFavorites(anyInt());
    }

    @Test
    void getUserFavorites_EmptyFavorites_ReturnsEmptyList() {
        //arrange
        int userId = 1;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(favoriteRepository.getUserFavorites(userId)).thenReturn(Collections.emptyList());

        //act
        List<Media> result = favoriteService.getUserFavorites(userId);

        //assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(favoriteRepository).getUserFavorites(userId);
    }

    //EDGE CASE TESTS
    @Test
    void addFavorite_UserIdZero_ChecksUserExists() {
        //arrange
        int userId = 0;
        int mediaId = 100;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(userId, mediaId)).thenReturn(false);
        when(favoriteRepository.addFavorite(any(Favorite.class))).thenReturn(true);

        //act
        boolean result = favoriteService.addFavorite(userId, mediaId);

        //assert
        assertTrue(result);
        verify(userRepository).getById(userId); //should still check for user 0
    }

    @Test
    void addFavorite_MediaIdZero_ChecksMediaExists() {
        //arrange
        int userId = 1;
        int mediaId = 0;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(userId, mediaId)).thenReturn(false);
        when(favoriteRepository.addFavorite(any(Favorite.class))).thenReturn(true);

        //act
        boolean result = favoriteService.addFavorite(userId, mediaId);

        //assert
        assertTrue(result);
        verify(mediaRepository).getById(mediaId); //should still check for media 0
    }

    @Test
    void removeFavorite_AlreadyRemoved_RepositoryCalledOnce() {
        //arrange
        int userId = 1;
        int mediaId = 100;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(mediaRepository.getById(mediaId)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(userId, mediaId)).thenReturn(true);
        when(favoriteRepository.deleteFavorite(userId, mediaId)).thenReturn(true);

        //act
        boolean result = favoriteService.removeFavorite(userId, mediaId);

        //assert - second removal should not be attempted in this test
        assertTrue(result);
        verify(favoriteRepository, times(1)).deleteFavorite(userId, mediaId);
    }

    @Test
    void getUserFavorites_NullReturnFromRepository_HandlesNull() {
        //arrange
        int userId = 1;

        when(userRepository.getById(userId)).thenReturn(new User("testuser", "hash"));
        when(favoriteRepository.getUserFavorites(userId)).thenReturn(null);

        //act
        List<Media> result = favoriteService.getUserFavorites(userId);

        //assert
        assertNull(result);
        verify(favoriteRepository).getUserFavorites(userId);
    }
}
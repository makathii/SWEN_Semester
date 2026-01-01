package at.technikum_wien.services;

import at.technikum_wien.database.repositories.FavoriteRepository;
import at.technikum_wien.database.repositories.MediaRepository;
import at.technikum_wien.database.repositories.UserRepository;
import at.technikum_wien.models.entities.Media;
import at.technikum_wien.models.entities.User;
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
class FavoriteServiceTest {

    @Mock
    FavoriteRepository favoriteRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    MediaRepository mediaRepository;

    @InjectMocks
    FavoriteService favoriteService;

    @BeforeEach
    void setup() {
    }

    @Test
    void addFavorite_success() {
        when(userRepository.getById(1)).thenReturn(new User());
        when(mediaRepository.getById(10)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(1, 10)).thenReturn(false);
        when(favoriteRepository.addFavorite(any())).thenReturn(true);

        boolean result = favoriteService.addFavorite(1, 10);
        assertTrue(result);
        verify(favoriteRepository).addFavorite(any());
    }

    //tests exception
    @Test
    void addFavorite_userNotFound() {
        when(userRepository.getById(1)).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> favoriteService.addFavorite(1, 10));
        assertEquals("User not found", ex.getMessage());
    }

    //tests exception
    @Test
    void addFavorite_mediaNotFound() {
        when(userRepository.getById(1)).thenReturn(new User());
        when(mediaRepository.getById(10)).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> favoriteService.addFavorite(1, 10));
        assertEquals("Media not found", ex.getMessage());
    }

    @Test
    void addFavorite_alreadyExists() {
        when(userRepository.getById(1)).thenReturn(new User());
        when(mediaRepository.getById(10)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(1, 10)).thenReturn(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> favoriteService.addFavorite(1, 10));
        assertEquals("Favorite already exists", ex.getMessage());
    }

    @Test
    void removeFavorite_success() {
        when(userRepository.getById(2)).thenReturn(new User());
        when(mediaRepository.getById(20)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(2, 20)).thenReturn(true);
        when(favoriteRepository.deleteFavorite(2, 20)).thenReturn(true);

        boolean r = favoriteService.removeFavorite(2, 20);
        assertTrue(r);
        verify(favoriteRepository).deleteFavorite(2, 20);
    }

    @Test
    void removeFavorite_notInFavorites() {
        when(userRepository.getById(2)).thenReturn(new User());
        when(mediaRepository.getById(20)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(2, 20)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> favoriteService.removeFavorite(2, 20));
        assertEquals("Media is not in favorites", ex.getMessage());
    }

    @Test
    void toggleFavorite_removesWhenExists() {
        when(userRepository.getById(3)).thenReturn(new User());
        when(mediaRepository.getById(30)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(3, 30)).thenReturn(true);
        when(favoriteRepository.deleteFavorite(3, 30)).thenReturn(true);

        assertTrue(favoriteService.toggleFavorite(3, 30));
        verify(favoriteRepository).deleteFavorite(3, 30);
    }

    @Test
    void toggleFavorite_addsWhenNotExists() {
        when(userRepository.getById(4)).thenReturn(new User());
        when(mediaRepository.getById(40)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(4, 40)).thenReturn(false);
        when(favoriteRepository.addFavorite(any())).thenReturn(true);

        assertTrue(favoriteService.toggleFavorite(4, 40));
        verify(favoriteRepository).addFavorite(any());
    }

    @Test
    void getUserFavorites_userNotFound() {
        when(userRepository.getById(99)).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> favoriteService.getUserFavorites(99));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getUserFavorites_success() {
        when(userRepository.getById(5)).thenReturn(new User());
        when(favoriteRepository.getUserFavorites(5)).thenReturn(List.of(new Media()));
        List<Media> favorites = favoriteService.getUserFavorites(5);
        assertEquals(1, favorites.size());
    }

    @Test
    void isFavorite_checksCorrectly() {
        when(userRepository.getById(6)).thenReturn(new User());
        when(mediaRepository.getById(60)).thenReturn(new Media());
        when(favoriteRepository.isFavorite(6, 60)).thenReturn(true);
        assertTrue(favoriteService.isFavorite(6, 60));
    }

    @Test
    void getFavoriteCountForMedia_mediaNotFound() {
        when(mediaRepository.getById(500)).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> favoriteService.getFavoriteCountForMedia(500));
        assertEquals("Media not found", ex.getMessage());
    }

    @Test
    void getFavoriteCountForMedia_success() {
        when(mediaRepository.getById(7)).thenReturn(new Media());
        when(favoriteRepository.getFavoriteCountForMedia(7)).thenReturn(42);
        assertEquals(42, favoriteService.getFavoriteCountForMedia(7));
    }
}

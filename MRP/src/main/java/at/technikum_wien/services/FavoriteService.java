package at.technikum_wien.services;

import at.technikum_wien.database.repositories.FavoriteRepository;
import at.technikum_wien.database.repositories.MediaRepository;
import at.technikum_wien.database.repositories.UserRepository;
import at.technikum_wien.models.entities.Favorite;
import at.technikum_wien.models.entities.Media;

import java.util.List;

public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository, MediaRepository mediaRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.mediaRepository = mediaRepository;
    }

    public boolean addFavorite(int userId, int mediaId) {
        if(userRepository.getById(userId)==null){
            throw new IllegalArgumentException("User not found");
        }
        if(mediaRepository.getById(mediaId)==null){
            throw new IllegalArgumentException("Media not found");
        }
        if(favoriteRepository.isFavorite(userId,mediaId)){
            throw new IllegalArgumentException("Favorite already exists");
        }

        Favorite favorite = new Favorite(userId,mediaId);
        return  favoriteRepository.addFavorite(favorite);
    }

    public boolean removeFavorite(int userId, int mediaId) {
        if(userRepository.getById(userId)==null){
            throw new IllegalArgumentException("User not found");
        }
        if(mediaRepository.getById(mediaId)==null){
            throw new IllegalArgumentException("Media not found");
        }
        if(!favoriteRepository.isFavorite(userId,mediaId)){
            throw new IllegalArgumentException("Media is not in favorites");
        }
        return favoriteRepository.deleteFavorite(userId,mediaId);
    }

    public List<Media> getUserFavorites(int userId){
        if(userRepository.getById(userId)==null){
            throw new IllegalArgumentException("User not found");
        }
        return favoriteRepository.getUserFavorites(userId);
    }
}

package at.technikum_wien.repository.repoTests;

import at.technikum_wien.model.Media;
import at.technikum_wien.model.User;
import at.technikum_wien.repository.MediaRepository;
import at.technikum_wien.model.MediaTypes;
import at.technikum_wien.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestMediaRepository {
    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        User existingUser=userRepository.getById(1);
        System.out.println(existingUser);
        System.out.println(existingUser.getId());

        MediaRepository mediaRepo = new MediaRepository();
        Media newMedia=new Media(MediaTypes.movie.name(),"TestMovie","testMovieDescription",2024,16,1, Collections.singletonList("Horror"));
        Media savedMedia = mediaRepo.save(newMedia);

        if(savedMedia!=null){
            System.out.println("Successfully saved media: "+savedMedia.getId());
            System.out.println("Title: "+savedMedia.getTitle());
            System.out.println("Description: "+savedMedia.getDescription());
            System.out.println("Type: "+savedMedia.getType());
            System.out.println("Rating: "+savedMedia.getRating());
            System.out.println("Genres: "+savedMedia.getGenres());
        }else{
            System.out.println("Failed to save new media");
        }
    }
}

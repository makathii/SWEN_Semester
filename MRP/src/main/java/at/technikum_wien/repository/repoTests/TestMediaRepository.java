package at.technikum_wien.repository.repoTests;

import at.technikum_wien.model.Media;
import at.technikum_wien.repository.MediaRepository;
import at.technikum_wien.model.MediaTypes;

import java.sql.SQLException;
import java.util.Arrays;

public class TestMediaRepository {
    public static void main(String[] args) throws SQLException {
        MediaRepository mediaRepo = new MediaRepository();

        //check if media with ID 9 exists first
        Media existingMedia = mediaRepo.getById(9);
        Media savedMedia = null;

        if (existingMedia != null) {
            //update existing media
            System.out.println("Updating existing media with ID: " + existingMedia.getId());
            existingMedia.setTitle("Updated Title");
            existingMedia.setDescription("Updated description");
            existingMedia.setGenres(Arrays.asList("Horror","Thriller"));
            savedMedia = mediaRepo.save(existingMedia);
        } else {
            //create new media
            System.out.println("Creating new media");
            Media newMedia = new Media(MediaTypes.movie.name(), "TestMovie", "...",
                    2024, 16, 1, Arrays.asList("Horror","Comedy","Thriller"));
            savedMedia = mediaRepo.save(newMedia);
        }

        //check if save was successful
        if(savedMedia != null){
            System.out.println("Successfully saved media: " + savedMedia.getId());
            System.out.println("Title: " + savedMedia.getTitle());
            System.out.println("Description: " + savedMedia.getDescription());
            System.out.println("Type: " + savedMedia.getType());
            System.out.println("Genres: " + savedMedia.getGenres());
        } else {
            System.out.println("Failed to save media");
            return;
        }

        //test with existing id
        Media foundById = mediaRepo.getById(savedMedia.getId());
        if(foundById != null){
            System.out.println("Successfully found media by ID: " + foundById.getId());
        } else {
            System.out.println("Failed to find media by ID");
        }

        //test with non-existing id
        Media nonExistingMedia = mediaRepo.getById(999);
        if(nonExistingMedia == null){
            System.out.println("Correctly failed to get media with ID 999");
        } else {
            System.out.println("Unexpectedly found media with ID 999");
        }

        //test with existing name
        Media foundByName = mediaRepo.getByName(savedMedia.getTitle());
        if(foundByName != null){
            System.out.println("Successfully found media by name: " + foundByName.getId());
        } else {
            System.out.println("Failed to find media by name");
        }

        //test with non-existing name
        Media notFoundByName = mediaRepo.getByName("NonExistentMovieTitle123");
        if(notFoundByName == null){
            System.out.println("Correctly couldn't find media with non-existent title");
        } else {
            System.out.println("Unexpectedly found media: " + notFoundByName.getTitle());
        }
    }
}
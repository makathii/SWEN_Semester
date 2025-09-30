package at.technikum_wien.repository.repoTests;

import at.technikum_wien.model.Rating;
import at.technikum_wien.repository.RatingRepository;

public class TestRatingRepository {
    public static void main(String[] args) {
        RatingRepository ratingRepo=new RatingRepository();
        Rating newRating=new Rating(1,1,5,"testcomment");
        Rating savedRating=ratingRepo.save(newRating);

        if(savedRating!=null){
            System.out.println("Successfully saved rating: "+savedRating);
            System.out.println("Media_id: "+savedRating.getMedia_id());
            System.out.println("User_id: "+savedRating.getUser_id());
            System.out.println("Stars: "+savedRating.getStars());
            System.out.println("Comment: "+savedRating.getComment());
            System.out.println("Confirmed: "+savedRating.getConfirmed());
        }else{
            System.out.println("Failed to save rating");
        }
        //test with existing id
        Rating existingRating=ratingRepo.getById(savedRating.getId());
        if(existingRating!=null){
            System.out.println("Successfully found rating: "+existingRating);
        }else{
            System.out.println("Failed to find rating: ");
        }
        //test with non-existing id
        Rating non_existingRating=ratingRepo.getById(999);
        if(existingRating==null){
            System.out.println("Correctly failed to get rating: "+non_existingRating);
        }
    }
}

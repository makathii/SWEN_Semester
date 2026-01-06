package at.technikum_wien.services;

import at.technikum_wien.database.repositories.RatingRepository;
import at.technikum_wien.database.repositories.MediaRepository;
import at.technikum_wien.database.repositories.UserRepository;
import at.technikum_wien.models.entities.Rating;
import at.technikum_wien.models.entities.Media;
import at.technikum_wien.models.entities.User;

import java.util.List;

public class RatingService {
    private final RatingRepository ratingRepository;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository, MediaRepository mediaRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    //rate media entry
    public Rating rateMedia(int mediaId, int userId, int stars, String comment) {
        //validate input
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }

        //check if media exists
        Media media = mediaRepository.getById(mediaId);
        if (media == null) {
            throw new IllegalArgumentException("Media not found with ID: " + mediaId);
        }

        //check if user exists
        User user = userRepository.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        //check if user has already rated this media
        Rating existingRating = ratingRepository.getRatingByUserAndMedia(userId, mediaId);
        if (existingRating != null) {
            throw new IllegalStateException("User has already rated this media. Use update instead.");
        }

        boolean hasComment = comment != null && !comment.trim().isEmpty();
        boolean confirmed = !hasComment;

        Rating rating = new Rating(mediaId, userId, stars, comment);
        rating.setConfirmed(confirmed);

        Rating savedRating = ratingRepository.save(rating);

        if (savedRating == null) {
            throw new RuntimeException("Failed to save rating");
        }

        return savedRating;
    }

     //get single rating with comment visibility filtering
    public Rating getPublicRating(int ratingId, Integer requestingUserId) {
        Rating rating = ratingRepository.getById(ratingId);
        if (rating == null) return null;

        //hide comment if not confirmed & user is not owner
        if (!rating.getConfirmed() && (requestingUserId == null || rating.getUser_id() != requestingUserId)) {
            return createRatingWithHiddenComment(rating);
        }
        return rating;
    }

     //get user's own ratings - always see their own comments
    public List<Rating> getRatingsByUser(int userId) {
        return ratingRepository.getAllRatingsByUser(userId);
    }

    //update a rating - if comment changes, it becomes unconfirmed again
    public Rating updateRating(int ratingId, int stars, String comment, int userId) {
        //validate star input
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }

        //get existing rating
        Rating existingRating = ratingRepository.getById(ratingId);
        if (existingRating == null) {
            throw new IllegalArgumentException("Rating not found with ID: " + ratingId);
        }

        //check if user owns this rating
        if (existingRating.getUser_id() != userId) {
            throw new SecurityException("User is not authorized to update this rating");
        }

        //check if comment changed - if so, it needs reconfirmation
        boolean commentChanged = !equals(existingRating.getComment(), comment);
        if (commentChanged) {
            existingRating.setConfirmed(false);
        }

        //update rating
        existingRating.setStars(stars);
        existingRating.setComment(comment);

        Rating updatedRating = ratingRepository.save(existingRating);
        if (updatedRating == null) {
            throw new RuntimeException("Failed to update rating");
        }

        return updatedRating;
    }

     //confirm a rating comment - makes it publicly visible
    public boolean confirmRating(int ratingId, int userId) {
        //get existing rating
        Rating existingRating = ratingRepository.getById(ratingId);
        if (existingRating == null) {
            throw new IllegalArgumentException("Rating not found with ID: " + ratingId);
        }

        //check if user owns this rating
        if (existingRating.getUser_id() != userId) {
            throw new SecurityException("User can only confirm their own ratings");
        }

        //check if there's comment to confirm
        if (existingRating.getComment() == null || existingRating.getComment().trim().isEmpty()) {
            throw new IllegalStateException("No comment to confirm");
        }

        //check if already confirmed
        if (existingRating.getConfirmed()) {
            throw new IllegalStateException("Comment is already confirmed");
        }

        return ratingRepository.confirmRating(ratingId);
    }

    public void deleteRating(int ratingId, int userId) {
        Rating existingRating = ratingRepository.getById(ratingId);
        if (existingRating == null) {
            throw new IllegalArgumentException("Rating not found with ID: " + ratingId);
        }

        if (existingRating.getUser_id() != userId) {
            throw new SecurityException("User is not authorized to delete this rating");
        }

        ratingRepository.deleteById(existingRating);
    }

    public boolean likeRating(int ratingId, int userId) {
        Rating rating = ratingRepository.getById(ratingId);
        if (rating == null) {
            throw new IllegalArgumentException("Rating not found with ID: " + ratingId);
        }

        User user = userRepository.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        if (rating.getUser_id() == userId) {
            throw new IllegalStateException("Users cannot like their own ratings");
        }

        return ratingRepository.likeRating(ratingId, userId);
    }

    public int getRatingLikeCount(int ratingId) {
        Rating rating = ratingRepository.getById(ratingId);
        if (rating == null) {
            throw new IllegalArgumentException("Rating not found with ID: " + ratingId);
        }

        return ratingRepository.getLikeCount(ratingId);
    }

    public Rating getRatingById(int ratingId) {
        Rating rating = ratingRepository.getById(ratingId);
        if (rating == null) {
            throw new IllegalArgumentException("Rating not found with ID: " + ratingId);
        }
        return rating;
    }

    //HELPERS
    private Rating createRatingWithHiddenComment(Rating original) {
        return new Rating(original.getId(), original.getMedia_id(), original.getUser_id(), original.getStars(), null, // Hide the comment
                original.getConfirmed(), original.getCreated_at());
    }

    private boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }
}
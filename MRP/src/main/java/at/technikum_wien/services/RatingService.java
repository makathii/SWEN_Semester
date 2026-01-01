package at.technikum_wien.services;

import at.technikum_wien.database.repositories.RatingRepository;
import at.technikum_wien.database.repositories.MediaRepository;
import at.technikum_wien.database.repositories.UserRepository;
import at.technikum_wien.models.entities.Rating;
import at.technikum_wien.models.entities.Media;
import at.technikum_wien.models.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class RatingService {
    private final RatingRepository ratingRepository;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository, MediaRepository mediaRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    // Rate a media entry
    public Rating rateMedia(int mediaId, int userId, int stars, String comment) {
        // Validate input
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }

        // Check if media exists
        Media media = mediaRepository.getById(mediaId);
        if (media == null) {
            throw new IllegalArgumentException("Media not found with ID: " + mediaId);
        }

        // Check if user exists
        User user = userRepository.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        // Check if user has already rated this media
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

     // Get ratings for media with comment visibility filtering
     // - Stars are always visible
     // - Comments are only visible if confirmed OR if the requesting user is the rating owner


     // Get a single rating with comment visibility filtering
    public Rating getPublicRating(int ratingId, Integer requestingUserId) {
        Rating rating = ratingRepository.getById(ratingId);
        if (rating == null) return null;

        // Hide comment if not confirmed and user is not the owner
        if (!rating.getConfirmed() && (requestingUserId == null || rating.getUser_id() != requestingUserId)) {
            return createRatingWithHiddenComment(rating);
        }
        return rating;
    }

     // Get user's own ratings - they always see their own comments
    public List<Rating> getRatingsByUser(int userId) {
        return ratingRepository.getAllRatingsByUser(userId);
        // No filtering needed - user always sees their own ratings
    }

    //Update a rating - if comment changes, it becomes unconfirmed again
    public Rating updateRating(int ratingId, int stars, String comment, int userId) {
        // Validate input
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }

        // Get existing rating
        Rating existingRating = ratingRepository.getById(ratingId);
        if (existingRating == null) {
            throw new IllegalArgumentException("Rating not found with ID: " + ratingId);
        }

        // Check if user owns this rating
        if (existingRating.getUser_id() != userId) {
            throw new SecurityException("User is not authorized to update this rating");
        }

        //check if comment changed - if so, it needs reconfirmation
        boolean commentChanged = !equals(existingRating.getComment(), comment);
        if (commentChanged) {
            existingRating.setConfirmed(false); //comment changed, needs reconfirmation
        }

        // Update rating
        existingRating.setStars(stars);
        existingRating.setComment(comment);

        Rating updatedRating = ratingRepository.save(existingRating);
        if (updatedRating == null) {
            throw new RuntimeException("Failed to update rating");
        }

        return updatedRating;
    }

     // Confirm a rating comment - makes it publicly visible
    public boolean confirmRating(int ratingId, int userId) {
        // Get existing rating
        Rating existingRating = ratingRepository.getById(ratingId);
        if (existingRating == null) {
            throw new IllegalArgumentException("Rating not found with ID: " + ratingId);
        }

        // Check if user owns this rating
        if (existingRating.getUser_id() != userId) {
            throw new SecurityException("User can only confirm their own ratings");
        }

        // Check if there's a comment to confirm
        if (existingRating.getComment() == null || existingRating.getComment().trim().isEmpty()) {
            throw new IllegalStateException("No comment to confirm");
        }

        // Check if already confirmed
        if (existingRating.getConfirmed()) {
            throw new IllegalStateException("Comment is already confirmed");
        }

        return ratingRepository.confirmRating(ratingId);
    }

    // Rest of your existing methods remain the same...
    public boolean deleteRating(int ratingId, int userId) {
        Rating existingRating = ratingRepository.getById(ratingId);
        if (existingRating == null) {
            throw new IllegalArgumentException("Rating not found with ID: " + ratingId);
        }

        if (existingRating.getUser_id() != userId) {
            throw new SecurityException("User is not authorized to delete this rating");
        }

        ratingRepository.deleteById(existingRating);
        return true;
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

    // HELPER METHODS
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
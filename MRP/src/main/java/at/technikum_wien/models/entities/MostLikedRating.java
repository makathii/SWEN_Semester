package at.technikum_wien.models.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class MostLikedRating {
    private int ratingId;
    private int stars;
    private String comment;
    private String mediaTitle;
    private String authorName;
    private int likeCount;

    public MostLikedRating() {}

    public MostLikedRating(int ratingId, int stars, String comment,
                           String mediaTitle, String authorName, int likeCount) {
        this.ratingId = ratingId;
        this.stars = stars;
        this.comment = comment;
        this.mediaTitle = mediaTitle;
        this.authorName = authorName;
        this.likeCount = likeCount;
    }
}
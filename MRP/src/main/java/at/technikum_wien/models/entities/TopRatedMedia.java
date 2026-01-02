package at.technikum_wien.models.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class TopRatedMedia {
    private int mediaId;
    private String title;
    private String type;
    private int releaseYear;
    private double averageRating;
    private int ratingCount;

    public TopRatedMedia() {}

    public TopRatedMedia(int mediaId, String title, String type,
                         int releaseYear, double averageRating, int ratingCount) {
        this.mediaId = mediaId;
        this.title = title;
        this.type = type;
        this.releaseYear = releaseYear;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
    }
}
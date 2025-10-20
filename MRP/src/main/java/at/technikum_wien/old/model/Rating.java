package at.technikum_wien.old.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Rating {
    private int id;
    private int media_id;
    private int  user_id;
    private int stars;
    private String comment;
    private Boolean confirmed;
    private LocalDateTime created_at;

    public Rating(int id,int media_id, int user_id, int stars, String comment, Boolean confirmed, LocalDateTime created_at) {
        this.id = id;
        this.media_id = media_id;
        this.user_id = user_id;
        this.stars = stars;
        this.comment = comment;
        this.confirmed = confirmed;
        this.created_at = created_at;
    }
    public Rating(int media_id, int user_id, int stars, String comment) {
        this.media_id = media_id;
        this.user_id = user_id;
        this.stars = stars;
        this.comment = comment;
        this.confirmed = false;
    }
}

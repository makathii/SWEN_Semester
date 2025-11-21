package at.technikum_wien.models.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class Favorite {
    private int userId;
    private int mediaId;
    private LocalDateTime createdAt;

    public Favorite(){}

    public Favorite(int userId, int mediaId) {
        this.userId = userId;
        this.mediaId = mediaId;
    }

    public Favorite(int userId, int mediaId, LocalDateTime createdAt) {
        this.userId = userId;
        this.mediaId = mediaId;
        createdAt = LocalDateTime.now();
    }
}

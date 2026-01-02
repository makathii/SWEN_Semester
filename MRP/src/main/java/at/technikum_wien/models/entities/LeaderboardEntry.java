package at.technikum_wien.models.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class LeaderboardEntry {
    private int userId;
    private String username;
    private int ratingCount;
    private int favoriteCount;
    private int mediaCreatedCount;
    private int activityScore;

    public LeaderboardEntry() {}

    public LeaderboardEntry(int userId, String username, int ratingCount,
                            int favoriteCount, int mediaCreatedCount, int activityScore) {
        this.userId = userId;
        this.username = username;
        this.ratingCount = ratingCount;
        this.favoriteCount = favoriteCount;
        this.mediaCreatedCount = mediaCreatedCount;
        this.activityScore = activityScore;
    }
}
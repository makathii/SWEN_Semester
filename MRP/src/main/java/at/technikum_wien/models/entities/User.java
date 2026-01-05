package at.technikum_wien.models.entities;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class User {

    private int id;
    private String username;
    private String passwordHash;
    private String favoriteGenre;
    private LocalDateTime createdAt;

    public User(int id, String username, String passwordHash, String favoriteGenre, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.favoriteGenre = null;
        this.createdAt = createdAt;
    }

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.favoriteGenre = null;
    }

    public User() {
    }
}

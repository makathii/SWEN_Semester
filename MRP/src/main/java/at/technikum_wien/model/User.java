package at.technikum_wien.model;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class User {
    //GETTER / SETTER

    private int id;
    private String username;
    private String passwordHash;
    private LocalDateTime createdAt;

    public User(int id, String username, String passwordHash, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public User(){}
}

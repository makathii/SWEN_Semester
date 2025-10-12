package at.technikum_wien.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class Media {
    private int id;
    private String type;
    private String title;
    private String description;
    private int release_year;
    private int age_restriction;
    private int creator_id;
    private List<String> genres;
    private double rating;

    public Media(int id, String type, String title, String description, int release_year, int age_restriction, int creator_id, List<String> genres) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.release_year = release_year;
        this.age_restriction = age_restriction;
        this.creator_id = creator_id;
        this.genres = genres;
    }

    public Media(String type, String title, String description, int release_year, int age_restriction, int creator_id, List<String> genres) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.release_year = release_year;
        this.age_restriction = age_restriction;
        this.creator_id = creator_id;
        this.genres = genres;
    }

    public double calculateAvgRating() {
        return 0;
    }


}

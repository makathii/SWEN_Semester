package at.technikum_wien.services;

import at.technikum_wien.database.repositories.LeaderboardRepository;
import at.technikum_wien.models.entities.LeaderboardEntry;
import at.technikum_wien.models.entities.TopRatedMedia;
import at.technikum_wien.models.entities.MostLikedRating;

import java.util.List;

public class LeaderboardService {
    private final LeaderboardRepository leaderboardRepository;

    public LeaderboardService(LeaderboardRepository leaderboardRepository) {
        this.leaderboardRepository = leaderboardRepository;
    }

    public List<LeaderboardEntry> getMostActiveUsers(int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 20; // default
        }
        return leaderboardRepository.getMostActiveUsers(limit);
    }

    public List<TopRatedMedia> getTopRatedMedia(int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 20; // default
        }
        return leaderboardRepository.getTopRatedMedia(limit);
    }

    public List<MostLikedRating> getMostLikedRatings(int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 20; // default
        }
        return leaderboardRepository.getMostLikedRatings(limit);
    }

    public List<String> getTrendingGenres() {
        return leaderboardRepository.getTrendingGenres();
    }
}
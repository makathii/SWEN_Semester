package at.technikum_wien.services;

import at.technikum_wien.database.repositories.MediaRepository;
import at.technikum_wien.database.repositories.RatingRepository;
import at.technikum_wien.database.repositories.FavoriteRepository;
import at.technikum_wien.models.entities.Media;
import at.technikum_wien.models.entities.Rating;

import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {
    private final MediaRepository mediaRepository;
    private final RatingRepository ratingRepository;
    private final FavoriteRepository favoriteRepository;

    public RecommendationService(MediaRepository mediaRepository, RatingRepository ratingRepository, FavoriteRepository favoriteRepository) {
        this.mediaRepository = mediaRepository;
        this.ratingRepository = ratingRepository;
        this.favoriteRepository = favoriteRepository;
    }

    //get genre-based recommendations based on user's previously highly rated media
    public List<Media> getGenreBasedRecommendations(int userId, int limit) {
        List<Media> allMedia = mediaRepository.getAllMedia();
        List<Rating> userRatings = ratingRepository.getAllRatingsByUser(userId);

        if (userRatings.isEmpty()) {
            return getPopularMediaByCommonGenres(limit);
        }

        Set<String> favoriteGenres = calculateFavoriteGenres(userRatings);

        if (favoriteGenres.isEmpty()) {
            return getPopularMedia(limit);
        }

        return allMedia.stream().filter(media -> !hasUserRatedMedia(userRatings, media.getId())).sorted((m1, m2) -> {
            double score1 = calculateGenreSimilarityScore(m1, favoriteGenres);
            double score2 = calculateGenreSimilarityScore(m2, favoriteGenres);
            return Double.compare(score2, score1); // Descending order
        }).limit(limit).collect(Collectors.toList());
    }

    //get content-based recommendations considering genre, media type, and age restriction
    public List<Media> getContentBasedRecommendations(int userId, int limit) {
        List<Media> allMedia = mediaRepository.getAllMedia();
        List<Rating> userRatings = ratingRepository.getAllRatingsByUser(userId);

        if (userRatings.isEmpty()) {
            return getPopularMedia(limit);
        }

        UserPreferences preferences = calculateUserPreferences(userRatings);
        Map<Media, Double> mediaScores = new HashMap<>();

        for (Media media : allMedia) {
            if (hasUserRatedMedia(userRatings, media.getId())) {
                continue;
            }

            double score = calculateContentSimilarityScore(media, preferences);
            mediaScores.put(media, score);
        }

        return mediaScores.entrySet().stream().sorted(Map.Entry.<Media, Double>comparingByValue().reversed()).limit(limit).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    //HELPERS
    private static class UserPreferences {
        Set<String> favoriteGenres = new HashSet<>();
        String preferredMediaType;
        int preferredAgeRestriction = 0;

        double genreWeight = 0.5;
        double mediaTypeWeight = 0.3;
        double ageRestrictionWeight = 0.2;
    }

    private Set<String> calculateFavoriteGenres(List<Rating> userRatings) {
        Set<String> favoriteGenres = new HashSet<>();

        List<Rating> highRatings = userRatings.stream().filter(rating -> rating.getStars() >= 4).collect(Collectors.toList());

        for (Rating rating : highRatings) {
            Media media = mediaRepository.getById(rating.getMedia_id()); // Fixed method name
            if (media != null && media.getGenres() != null) {
                favoriteGenres.addAll(media.getGenres());
            }
        }

        return favoriteGenres;
    }

    private UserPreferences calculateUserPreferences(List<Rating> userRatings) {
        UserPreferences preferences = new UserPreferences();

        List<Rating> highRatings = userRatings.stream().filter(rating -> rating.getStars() >= 4).collect(Collectors.toList());

        if (highRatings.isEmpty()) {
            highRatings = userRatings;
        }

        preferences.favoriteGenres = calculateFavoriteGenres(userRatings);

        Map<String, Long> mediaTypeCounts = highRatings.stream().map(rating -> mediaRepository.getById(rating.getMedia_id())).filter(Objects::nonNull).collect(Collectors.groupingBy(Media::getType, Collectors.counting()));

        if (!mediaTypeCounts.isEmpty()) {
            preferences.preferredMediaType = mediaTypeCounts.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        }

        double avgAgeRestriction = highRatings.stream().map(rating -> mediaRepository.getById(rating.getMedia_id())).filter(Objects::nonNull).mapToInt(Media::getAge_restriction).average().orElse(0);
        preferences.preferredAgeRestriction = (int) Math.round(avgAgeRestriction);

        return preferences;
    }

    private double calculateGenreSimilarityScore(Media media, Set<String> favoriteGenres) {
        if (favoriteGenres.isEmpty() || media.getGenres().isEmpty()) {
            return 0.0;
        }

        Set<String> mediaGenres = new HashSet<>(media.getGenres());

        Set<String> intersection = new HashSet<>(favoriteGenres);
        intersection.retainAll(mediaGenres);

        Set<String> union = new HashSet<>(favoriteGenres);
        union.addAll(mediaGenres);

        double genreSimilarity = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();

        double popularityBoost = calculatePopularityScore(media) / 1000.0;

        return genreSimilarity + popularityBoost;
    }

    private double calculateContentSimilarityScore(Media media, UserPreferences preferences) {
        double score = 0.0;

        double genreScore = calculateGenreSimilarityScore(media, preferences.favoriteGenres);
        score += genreScore * preferences.genreWeight;

        if (media.getType().equals(preferences.preferredMediaType)) {
            score += preferences.mediaTypeWeight;
        }

        double ageDiff = Math.abs(media.getAge_restriction() - preferences.preferredAgeRestriction);
        double ageScore = 1.0 - (ageDiff / 18.0);
        score += ageScore * preferences.ageRestrictionWeight;

        return score;
    }

    private double calculatePopularityScore(Media media) {
        List<Rating> ratings = ratingRepository.getAllRatingsByMedia(media.getId());
        int favoriteCount = favoriteRepository.getFavoriteCountForMedia(media.getId());

        if (ratings.isEmpty()) {
            return favoriteCount;
        }

        double averageRating = ratings.stream().mapToInt(Rating::getStars).average().orElse(0.0);

        return (averageRating * ratings.size()) + (favoriteCount * 0.5);
    }

    private boolean hasUserRatedMedia(List<Rating> userRatings, int mediaId) {
        return userRatings.stream().anyMatch(rating -> rating.getMedia_id() == mediaId); // Fixed method name
    }

    private List<Media> getPopularMedia(int limit) {
        List<Media> allMedia = mediaRepository.getAllMedia();

        return allMedia.stream().sorted((m1, m2) -> Double.compare(calculatePopularityScore(m2), calculatePopularityScore(m1))).limit(limit).collect(Collectors.toList());
    }

    private List<Media> getPopularMediaByCommonGenres(int limit) {
        Set<String> commonGenres = Set.of("action", "drama", "comedy", "adventure");
        List<Media> allMedia = mediaRepository.getAllMedia();

        return allMedia.stream().sorted((m1, m2) -> {
            double score1 = calculateGenreSimilarityScore(m1, commonGenres);
            double score2 = calculateGenreSimilarityScore(m2, commonGenres);
            int genreCompare = Double.compare(score2, score1);

            if (genreCompare != 0) return genreCompare;

            return Double.compare(calculatePopularityScore(m2), calculatePopularityScore(m1));
        }).limit(limit).collect(Collectors.toList());
    }
}
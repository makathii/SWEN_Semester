package at.technikum_wien.application.services;

import at.technikum_wien.domain.entities.Media;
import at.technikum_wien.infrastructure.repositories.MediaRepository;

import java.util.List;
import java.util.stream.Collectors;

public class MediaService {
    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public List<Media> getAllMedia() {
        return mediaRepository.getAllMedia();
    }

    public Media getMediaById(int id) {
        return mediaRepository.getById(id);
    }

    public Media createMedia(Media media) {
        return mediaRepository.save(media);
    }

    public Media updateMedia(Media media) {
        return mediaRepository.save(media);
    }

    public void deleteMedia(int id) {
        Media media = mediaRepository.getById(id);
        if (media != null) {
            mediaRepository.deleteById(media);
        }
    }

    //search and filter methods
    public List<Media> searchMedia(String title, String genre, String mediaType,
                                   Integer releaseYear, Integer ageRestriction,
                                   String sortBy) {
        List<Media> allMedia = mediaRepository.getAllMedia();

        return allMedia.stream()
                .filter(media -> title == null || media.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(media -> genre == null || media.getGenres().stream()
                        .anyMatch(g -> g.equalsIgnoreCase(genre)))
                .filter(media -> mediaType == null || media.getType().equalsIgnoreCase(mediaType))
                .filter(media -> releaseYear == null || media.getRelease_year() == releaseYear)
                .filter(media -> ageRestriction == null || media.getAge_restriction() <= ageRestriction)
                .sorted((m1, m2) -> {
                    if ("title".equalsIgnoreCase(sortBy)) {
                        return m1.getTitle().compareToIgnoreCase(m2.getTitle());
                    } else if ("releaseYear".equalsIgnoreCase(sortBy)) {
                        return Integer.compare(m2.getRelease_year(), m1.getRelease_year()); // newest first
                    } else if ("ageRestriction".equalsIgnoreCase(sortBy)) {
                        return Integer.compare(m1.getAge_restriction(), m2.getAge_restriction());
                    }
                    return 0; //default no sort
                })
                .collect(Collectors.toList());
    }

    public List<Media> getMediaByCreator(int creatorId) {
        return mediaRepository.getAllMedia().stream()
                .filter(media -> media.getCreator_id() == creatorId)
                .collect(Collectors.toList());
    }
}
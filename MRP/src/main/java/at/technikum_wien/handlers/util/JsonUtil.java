package at.technikum_wien.handlers.util;

import at.technikum_wien.models.entities.Media;
import at.technikum_wien.models.entities.Rating;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, String> parseJson(String json) throws IOException {
        return objectMapper.readValue(json, Map.class);
    }

    public static String mediaToJson(Media media) throws IOException {
        return objectMapper.writeValueAsString(media);
    }

    public static Map<String, Object> parseJsonToMap(String json) throws IOException {
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
    }

    public static String mediaListToJson(List<Media> mediaList) throws IOException {
        return objectMapper.writeValueAsString(mediaList);
    }

    public static String ratingListToJson(List<Rating> ratings) {
        //implement JSON serialization for ratings list
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(ratings);
        } catch (Exception e) {
            return "[]";
        }
    }

    public static String objectToJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    public static String mapToJson(Map<String, Object> map) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(map);
    }
}
package at.technikum_wien.old.util;

import at.technikum_wien.old.model.Media;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.List;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, String> parseJson(String json) throws IOException {
        return objectMapper.readValue(json, Map.class);
    }

    public static String toJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
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
}
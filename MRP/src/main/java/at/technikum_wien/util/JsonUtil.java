package at.technikum_wien.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, String> parseJson(String json) throws IOException {
        return objectMapper.readValue(json, Map.class);
    }

    public static String toJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }
}
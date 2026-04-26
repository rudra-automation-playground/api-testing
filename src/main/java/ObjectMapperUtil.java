import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;

public final class ObjectMapperUtil {

    private static final ObjectMapper OBJECT_MAPPER = createMapper();

    private ObjectMapperUtil() {
        // Prevent instantiation
    }

    // 🔹 Centralized ObjectMapper configuration
    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Pretty print
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Ignore unknown properties
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Handle Java 8 Date/Time
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Include non-null fields only
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }

    // 🔹 Convert Java Object → JSON String
    public static String toJson(Object obj) {
        validateNotNull(obj, "Input object cannot be null");
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    // 🔹 Convert Java Object → JSON File
    public static void toJsonFile(Object obj, String filePath) {
        validateNotNull(obj, "Input object cannot be null");
        validateNotEmpty(filePath, "File path cannot be empty");

        try {
            OBJECT_MAPPER.writeValue(new File(filePath), obj);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON to file", e);
        }
    }

    // 🔹 Convert JSON String → Java Object
    public static <T> T fromJson(String json, Class<T> clazz) {
        validateNotEmpty(json, "JSON string cannot be empty");
        validateNotNull(clazz, "Target class cannot be null");

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON string", e);
        }
    }

    // 🔹 Convert JSON String → Generic Type
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        validateNotEmpty(json, "JSON string cannot be empty");
        validateNotNull(typeRef, "TypeReference cannot be null");

        try {
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON to generic type", e);
        }
    }

    // 🔹 Convert JSON File → Java Object
    public static <T> T fromJsonFile(String filePath, Class<T> clazz) {
        validateNotEmpty(filePath, "File path cannot be empty");

        try {
            return OBJECT_MAPPER.readValue(new File(filePath), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON from file", e);
        }
    }

    // 🔹 Convert JSON File → Generic Type
    public static <T> T fromJsonFile(String filePath, TypeReference<T> typeRef) {
        validateNotEmpty(filePath, "File path cannot be empty");

        try {
            return OBJECT_MAPPER.readValue(new File(filePath), typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON from file", e);
        }
    }

    // 🔹 Validation helpers
    private static void validateNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validateNotEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
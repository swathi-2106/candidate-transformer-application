package com.candidate.transformer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;

/**
 * Utility for parsing and serializing JSON files and objects using Jackson.
 */
@Component
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public JsonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Serializes an object to a formatted JSON string.
     *
     * @param obj object to serialize
     * @return pretty-printed JSON string
     * @throws IOException if serialization fails
     */
    public String toJsonString(Object obj) throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    /**
     * Deserializes a JSON file into an object of the specified class.
     *
     * @param file  source JSON file
     * @param clazz destination class type
     * @param <T>   generic return type
     * @return deserialized object
     * @throws IOException if parsing fails
     */
    public <T> T fromJsonFile(File file, Class<T> clazz) throws IOException {
        return objectMapper.readValue(file, clazz);
    }

    /**
     * Serializes an object to a JSON file.
     *
     * @param file target destination file
     * @param obj  object to serialize
     * @throws IOException if serialization fails
     */
    public void toJsonFile(File file, Object obj) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, obj);
    }
}

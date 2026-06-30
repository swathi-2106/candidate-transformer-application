package com.candidate.transformer.util;

import com.candidate.transformer.model.ProjectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads project-specific configurations (like projection fields or mapping settings) from files.
 */
@Component
public class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private final JsonUtils jsonUtils;

    public ConfigLoader(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }

    /**
     * Loads a ProjectionConfig from the filesystem.
     *
     * @param configPathStr path to the configuration JSON file
     * @return loaded config or a default instance if file loading fails
     */
    public ProjectionConfig loadProjectionConfig(String configPathStr) {
        Path path = Paths.get(configPathStr);
        File file = path.toFile();
        if (!file.exists()) {
            logger.warn("Projection configuration file not found at: {}. Using default configuration.", path.toAbsolutePath());
            return new ProjectionConfig();
        }
        try {
            ProjectionConfig config = jsonUtils.fromJsonFile(file, ProjectionConfig.class);
            logger.info("Successfully loaded projection configuration from: {}", path.toAbsolutePath());
            return config;
        } catch (IOException e) {
            logger.error("Failed to read configuration from path: {}", path.toAbsolutePath(), e);
            return new ProjectionConfig();
        }
    }
}

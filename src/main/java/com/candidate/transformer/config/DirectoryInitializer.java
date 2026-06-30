package com.candidate.transformer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Initializes target directories required by the application.
 * Runs on startup before the main CLI runner.
 */
@Component
@Order(1)
public class DirectoryInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryInitializer.class);

    private static final List<String> REQUIRED_DIRECTORIES = List.of("input", "config", "output");

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Initializing application directory structure validation...");

        for (String dirName : REQUIRED_DIRECTORIES) {
            Path path = Paths.get(dirName);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                    logger.info("Directory '{}' did not exist and has been successfully created.", path.toAbsolutePath());
                } catch (IOException e) {
                    logger.error("Failed to create required directory: '{}'", path.toAbsolutePath(), e);
                    throw new RuntimeException("Directory initialization failed for folder: " + dirName, e);
                }
            } else if (!Files.isDirectory(path)) {
                logger.error("Path '{}' exists but is not a directory.", path.toAbsolutePath());
                throw new IllegalStateException("Required path exists but is not a directory: " + dirName);
            } else {
                logger.debug("Directory '{}' already exists and is valid.", path.toAbsolutePath());
            }
        }
        logger.info("Required directory structure initialized successfully.");
    }
}

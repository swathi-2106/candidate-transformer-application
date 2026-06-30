package com.candidate.transformer.util;

import org.apache.commons.io.FilenameUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for common file operations, scans, and extension helpers.
 */
public final class FileUtils {

    private FileUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Lists all regular files in a directory.
     *
     * @param directory directory path to scan
     * @return list of file paths found
     * @throws IOException if scan fails
     */
    public static List<Path> listFilesInDirectory(Path directory) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(Files::isRegularFile)
                         .collect(Collectors.toList());
        }
    }

    /**
     * Extracts extension from a filename.
     *
     * @param filename the file name or path string
     * @return file extension in lowercase (e.g. "pdf") or empty string if none exists
     */
    public static String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename).toLowerCase();
    }
}

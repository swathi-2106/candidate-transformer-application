package com.candidate.transformer.util;

/**
 * Standard utility class for text validation, cleaning, and formatting.
 */
public final class StringUtils {

    private StringUtils() {
        // Prevent instantiation
    }

    /**
     * Checks if a string is null, empty, or whitespace-only.
     *
     * @param str string to check
     * @return true if empty or null, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Standardizes a string by trimming whitespace. Returns empty string if null.
     *
     * @param str target string
     * @return trimmed string
     */
    public static String clean(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str target string
     * @return capitalized string
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        String cleaned = str.trim();
        if (cleaned.length() == 1) {
            return cleaned.toUpperCase();
        }
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1).toLowerCase();
    }
}

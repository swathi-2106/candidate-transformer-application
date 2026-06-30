package com.candidate.transformer.util;

import com.candidate.transformer.constants.AppConstants;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Standard utility for parsing and formatting date/time objects using global application patterns.
 */
public final class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_ISO);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(AppConstants.DATE_TIME_FORMAT_ISO);

    private DateUtils() {
        // Prevent instantiation
    }

    /**
     * Formats a LocalDate into the standard date string.
     *
     * @param date target LocalDate
     * @return formatted date string or null if date is null
     */
    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    /**
     * Parses standard formatted date strings into LocalDate instances.
     *
     * @param dateStr date string (yyyy-MM-dd)
     * @return parsed LocalDate or null if input is empty
     */
    public static LocalDate parseDate(String dateStr) {
        return dateStr == null || dateStr.isBlank() ? null : LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
    }

    /**
     * Formats a LocalDateTime into standard format.
     *
     * @param dateTime target LocalDateTime
     * @return formatted date-time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Parses standard formatted date-time strings into LocalDateTime instances.
     *
     * @param dateTimeStr date-time string
     * @return parsed LocalDateTime or null if input is empty
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr == null || dateTimeStr.isBlank() ? null : LocalDateTime.parse(dateTimeStr.trim(), DATE_TIME_FORMATTER);
    }
}

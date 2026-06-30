package com.candidate.transformer.util;

import java.util.regex.Pattern;

/**
 * Common validation expressions and rules for data attributes.
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    // Standard phone validation supporting country codes, spaces, dashes
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s-]{7,15}$");

    private ValidationUtils() {
        // Prevent instantiation
    }

    /**
     * Asserts string compliance with RFC-based email patterns.
     *
     * @param email candidate email value
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Asserts string compliance with typical international telephone numbers.
     *
     * @param phone candidate phone value
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
}

package com.candidate.transformer.util;

/**
 * Utility for rendering pretty console outputs and pipeline execution logs.
 */
public final class ConsoleLogger {

    // Simple colors for console output (Windows PowerShell and command shell support)
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    private ConsoleLogger() {
        // Prevent instantiation
    }

    /**
     * Prints a standardized workflow step header.
     *
     * @param stepNum  sequence number of the step
     * @param stepName descriptive label
     * @param details  action details
     */
    public static void logStep(int stepNum, String stepName, String details) {
        System.out.printf("%s[STEP %02d] %s%s: %s%n", ANSI_CYAN, stepNum, stepName.toUpperCase(), ANSI_RESET, details);
    }

    /**
     * Prints a primary segment separator.
     *
     * @param header segment title
     */
    public static void logHeader(String header) {
        System.out.println();
        System.out.println("================================================================================");
        System.out.printf("   %s%s%s%n", ANSI_GREEN, header.toUpperCase(), ANSI_RESET);
        System.out.println("================================================================================");
    }

    /**
     * Prints a success alert.
     *
     * @param msg details
     */
    public static void logSuccess(String msg) {
        System.out.printf("%s[SUCCESS] %s%s%n", ANSI_GREEN, msg, ANSI_RESET);
    }

    /**
     * Prints a warning alert.
     *
     * @param msg details
     */
    public static void logWarning(String msg) {
        System.out.printf("%s[WARNING] %s%s%n", ANSI_YELLOW, msg, ANSI_RESET);
    }

    /**
     * Prints standard status information.
     *
     * @param msg details
     */
    public static void logInfo(String msg) {
        System.out.printf("%s[INFO] %s%s%n", ANSI_BLUE, msg, ANSI_RESET);
    }
}

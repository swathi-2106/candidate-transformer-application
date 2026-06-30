package com.candidate.transformer.exception;

/**
 * Exception thrown when validation fails on any candidate profile or config.
 */
public class ValidationException extends CandidateTransformerException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

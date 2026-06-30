package com.candidate.transformer.exception;

/**
 * Base custom runtime exception class for the Candidate Profile Transformer.
 */
public class CandidateTransformerException extends RuntimeException {

    public CandidateTransformerException(String message) {
        super(message);
    }

    public CandidateTransformerException(String message, Throwable cause) {
        super(message, cause);
    }
}

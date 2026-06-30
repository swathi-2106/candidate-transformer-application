package com.candidate.transformer.exception;

/**
 * Exception thrown when profile merging fails due to unresolvable conflicts.
 */
public class MergeException extends CandidateTransformerException {

    public MergeException(String message) {
        super(message);
    }

    public MergeException(String message, Throwable cause) {
        super(message, cause);
    }
}

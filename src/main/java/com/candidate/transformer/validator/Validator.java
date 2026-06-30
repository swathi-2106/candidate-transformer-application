package com.candidate.transformer.validator;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.ValidationResult;

/**
 * Performs validation checks against business rules on candidate profiles.
 */
public interface Validator {

    /**
     * Validates a candidate profile.
     *
     * @param profile candidate profile to validate
     * @return validation errors and warnings wrapper
     */
    ValidationResult validate(CandidateProfile profile);
}

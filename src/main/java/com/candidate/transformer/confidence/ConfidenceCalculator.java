package com.candidate.transformer.confidence;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.Confidence;

/**
 * Calculates confidence scores based on completeness, source reliability, and data conflicts.
 */
public interface ConfidenceCalculator {

    /**
     * Estimates confidence details for the given candidate profile.
     *
     * @param profile candidate profile
     * @return calculated confidence scoring details
     */
    Confidence calculateConfidence(CandidateProfile profile);
}

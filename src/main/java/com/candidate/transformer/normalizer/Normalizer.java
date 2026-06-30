package com.candidate.transformer.normalizer;

import com.candidate.transformer.model.CandidateProfile;

/**
 * Standardizes raw extracted profile information (e.g., location strings, skills, date formats, values).
 */
public interface Normalizer {

    /**
     * Normalizes fields in the candidate profile, returning a new or updated profile.
     *
     * @param profile raw candidate profile
     * @return normalized candidate profile
     */
    CandidateProfile normalize(CandidateProfile profile);
}

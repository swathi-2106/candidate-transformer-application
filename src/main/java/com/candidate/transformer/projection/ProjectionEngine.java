package com.candidate.transformer.projection;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.ProjectionConfig;

/**
 * Transforms a profile into custom formats by filtering or renaming fields based on dynamic configurations.
 */
public interface ProjectionEngine {

    /**
     * Projects candidate profile fields dynamically based on config rules.
     *
     * @param profile consolidated candidate profile
     * @param config  projection field filter mapping configuration
     * @return dynamic mapped representation (e.g. Map, or custom DTO)
     */
    Object project(CandidateProfile profile, ProjectionConfig config);
}

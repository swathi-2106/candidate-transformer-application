package com.candidate.transformer.extractor;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.CandidateSource;

/**
 * Interface defining operations to extract structured CandidateProfile attributes
 * from raw CandidateSource content.
 */
public interface DataExtractor {

    /**
     * Extracts structured candidate profiles from parsed raw candidate sources.
     *
     * @param source the parsed candidate source metadata and content
     * @return structured candidate profile
     * @throws Exception if extraction fails
     */
    CandidateProfile extract(CandidateSource source) throws Exception;
}

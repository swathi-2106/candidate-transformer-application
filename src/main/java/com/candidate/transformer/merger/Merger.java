package com.candidate.transformer.merger;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.MergeResult;
import java.util.List;

/**
 * Merges duplicate/conflicting candidate profiles into a single consolidated CandidateRecord.
 */
public interface Merger {

    /**
     * Consolidates several profiles identified as the same individual, tracking conflicts.
     *
     * @param profiles list of matching profiles
     * @return outcome containing the consolidated record and conflict traces
     * @throws Exception if consolidation fails
     */
    MergeResult merge(List<CandidateProfile> profiles) throws Exception;
}

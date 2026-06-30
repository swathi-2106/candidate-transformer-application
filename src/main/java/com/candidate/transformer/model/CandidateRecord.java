package com.candidate.transformer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the complete candidate entity, grouping all raw input sources and the resulting merged profile.
 */
public class CandidateRecord {

    private String recordId;
    private List<CandidateSource> sources = new ArrayList<>();
    private CandidateProfile mergedProfile;

    public CandidateRecord() {
    }

    public CandidateRecord(String recordId, List<CandidateSource> sources, CandidateProfile mergedProfile) {
        this.recordId = recordId;
        this.sources = sources != null ? sources : new ArrayList<>();
        this.mergedProfile = mergedProfile;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public List<CandidateSource> getSources() {
        return sources;
    }

    public void setSources(List<CandidateSource> sources) {
        this.sources = sources;
    }

    public void addSource(CandidateSource source) {
        if (this.sources == null) {
            this.sources = new ArrayList<>();
        }
        this.sources.add(source);
    }

    public CandidateProfile getMergedProfile() {
        return mergedProfile;
    }

    public void setMergedProfile(CandidateProfile mergedProfile) {
        this.mergedProfile = mergedProfile;
    }

    @Override
    public String toString() {
        return "CandidateRecord{" +
                "recordId='" + recordId + '\'' +
                ", sources=" + sources +
                ", mergedProfile=" + mergedProfile +
                '}';
    }
}

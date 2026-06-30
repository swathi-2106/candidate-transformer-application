package com.candidate.transformer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result representing the outcome of merging multiple candidate profiles.
 */
public class MergeResult {

    private CandidateRecord mergedRecord;
    private List<String> conflicts = new ArrayList<>();
    private String mergeSummary;

    public MergeResult() {
    }

    public MergeResult(CandidateRecord mergedRecord, List<String> conflicts, String mergeSummary) {
        this.mergedRecord = mergedRecord;
        this.conflicts = conflicts != null ? conflicts : new ArrayList<>();
        this.mergeSummary = mergeSummary;
    }

    public CandidateRecord getMergedRecord() {
        return mergedRecord;
    }

    public void setMergedRecord(CandidateRecord mergedRecord) {
        this.mergedRecord = mergedRecord;
    }

    public List<String> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<String> conflicts) {
        this.conflicts = conflicts;
    }

    public void addConflict(String conflict) {
        if (this.conflicts == null) {
            this.conflicts = new ArrayList<>();
        }
        this.conflicts.add(conflict);
    }

    public String getMergeSummary() {
        return mergeSummary;
    }

    public void setMergeSummary(String mergeSummary) {
        this.mergeSummary = mergeSummary;
    }

    @Override
    public String toString() {
        return "MergeResult{" +
                "mergedRecord=" + mergedRecord +
                ", conflicts=" + conflicts +
                ", mergeSummary='" + mergeSummary + '\'' +
                '}';
    }
}

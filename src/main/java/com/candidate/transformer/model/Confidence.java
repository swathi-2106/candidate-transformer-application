package com.candidate.transformer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the confidence score and explanatory notes/reasons for values in a candidate profile.
 */
public class Confidence {

    private double score;
    private List<String> reasons = new ArrayList<>();

    public Confidence() {
    }

    public Confidence(double score, List<String> reasons) {
        this.score = score;
        this.reasons = reasons != null ? reasons : new ArrayList<>();
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public void addReason(String reason) {
        if (this.reasons == null) {
            this.reasons = new ArrayList<>();
        }
        this.reasons.add(reason);
    }

    @Override
    public String toString() {
        return "Confidence{" +
                "score=" + score +
                ", reasons=" + reasons +
                '}';
    }
}

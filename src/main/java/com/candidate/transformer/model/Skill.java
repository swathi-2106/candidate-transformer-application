package com.candidate.transformer.model;

/**
 * Represents a skill or competency possessed by a candidate.
 */
public class Skill {

    private String name;
    private Integer yearsOfExperience;
    private String level; // e.g. Beginner, Intermediate, Expert
    private Confidence confidence;
    private Provenance provenance;

    public Skill() {
    }

    public Skill(String name, Integer yearsOfExperience, String level, Confidence confidence, Provenance provenance) {
        this.name = name;
        this.yearsOfExperience = yearsOfExperience;
        this.level = level;
        this.confidence = confidence;
        this.provenance = provenance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Confidence getConfidence() {
        return confidence;
    }

    public void setConfidence(Confidence confidence) {
        this.confidence = confidence;
    }

    public Provenance getProvenance() {
        return provenance;
    }

    public void setProvenance(Provenance provenance) {
        this.provenance = provenance;
    }

    @Override
    public String toString() {
        return "Skill{" +
                "name='" + name + '\'' +
                ", yearsOfExperience=" + yearsOfExperience +
                ", level='" + level + '\'' +
                ", confidence=" + confidence +
                ", provenance=" + provenance +
                '}';
    }
}

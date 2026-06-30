package com.candidate.transformer.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single employment record or work experience entry for a candidate.
 */
public class Experience {

    private String company;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private List<String> skillsUsed = new ArrayList<>();
    private Location location;
    private Confidence confidence;
    private Provenance provenance;

    public Experience() {
    }

    public Experience(String company, String role, LocalDate startDate, LocalDate endDate, String description,
                      List<String> skillsUsed, Location location, Confidence confidence, Provenance provenance) {
        this.company = company;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.skillsUsed = skillsUsed != null ? skillsUsed : new ArrayList<>();
        this.location = location;
        this.confidence = confidence;
        this.provenance = provenance;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSkillsUsed() {
        return skillsUsed;
    }

    public void setSkillsUsed(List<String> skillsUsed) {
        this.skillsUsed = skillsUsed;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
        return "Experience{" +
                "company='" + company + '\'' +
                ", role='" + role + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", description='" + description + '\'' +
                ", skillsUsed=" + skillsUsed +
                ", location=" + location +
                ", confidence=" + confidence +
                ", provenance=" + provenance +
                '}';
    }
}

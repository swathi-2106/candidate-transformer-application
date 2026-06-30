package com.candidate.transformer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Core model representing a candidate's complete consolidated profile.
 */
public class CandidateProfile {

    private String id;
    private String name;
    private String email;
    private String phone;
    private Location location;
    private Links links;
    private List<Skill> skills = new ArrayList<>();
    private List<Experience> experiences = new ArrayList<>();
    private List<Education> educations = new ArrayList<>();
    private Confidence confidence;
    private Provenance provenance;

    public CandidateProfile() {
    }

    public CandidateProfile(String id, String name, String email, String phone, Location location, Links links,
                            List<Skill> skills, List<Experience> experiences, List<Education> educations,
                            Confidence confidence, Provenance provenance) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.links = links;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.experiences = experiences != null ? experiences : new ArrayList<>();
        this.educations = educations != null ? educations : new ArrayList<>();
        this.confidence = confidence;
        this.provenance = provenance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Experience> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences;
    }

    public List<Education> getEducations() {
        return educations;
    }

    public void setEducations(List<Education> educations) {
        this.educations = educations;
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
        return "CandidateProfile{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", location=" + location +
                ", links=" + links +
                ", skills=" + skills +
                ", experiences=" + experiences +
                ", educations=" + educations +
                ", confidence=" + confidence +
                ", provenance=" + provenance +
                '}';
    }
}

package com.candidate.transformer.confidence;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.Confidence;
import com.candidate.transformer.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates confidence from profile completeness and source-backed evidence.
 */
@Component
public class DefaultConfidenceCalculator implements ConfidenceCalculator {

    @Override
    public Confidence calculateConfidence(CandidateProfile profile) {
        if (profile == null) {
            return new Confidence(0.0, List.of("No candidate profile available"));
        }

        double score = 0.0;
        List<String> reasons = new ArrayList<>();

        score += addIfPresent(profile.getName(), 0.15, "Name present", reasons);
        score += addIfPresent(profile.getEmail(), 0.20, "Email present", reasons);
        score += addIfPresent(profile.getPhone(), 0.10, "Phone present", reasons);

        if (profile.getLocation() != null && !StringUtils.isEmpty(profile.getLocation().getFormattedAddress())) {
            score += 0.08;
            reasons.add("Location present");
        }
        if (profile.getLinks() != null && (!StringUtils.isEmpty(profile.getLinks().getLinkedin())
                || !StringUtils.isEmpty(profile.getLinks().getGithub())
                || !StringUtils.isEmpty(profile.getLinks().getPortfolio()))) {
            score += 0.08;
            reasons.add("Profile links present");
        }
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            score += Math.min(0.16, profile.getSkills().size() * 0.04);
            reasons.add("Skills present: " + profile.getSkills().size());
        }
        if (profile.getExperiences() != null && !profile.getExperiences().isEmpty()) {
            score += Math.min(0.12, profile.getExperiences().size() * 0.06);
            reasons.add("Experience entries present: " + profile.getExperiences().size());
        }
        if (profile.getEducations() != null && !profile.getEducations().isEmpty()) {
            score += Math.min(0.06, profile.getEducations().size() * 0.03);
            reasons.add("Education entries present: " + profile.getEducations().size());
        }
        if (profile.getProvenance() != null && !StringUtils.isEmpty(profile.getProvenance().getSourceId())) {
            score += 0.05;
            reasons.add("Merged provenance available");
        }

        double boundedScore = Math.max(0.0, Math.min(1.0, Math.round(score * 100.0) / 100.0));
        if (boundedScore < 0.75) {
            reasons.add("Profile is incomplete or has limited corroborating evidence");
        } else {
            reasons.add("Profile has sufficient canonical evidence");
        }
        return new Confidence(boundedScore, reasons);
    }

    private double addIfPresent(String value, double weight, String reason, List<String> reasons) {
        if (StringUtils.isEmpty(value)) {
            return 0.0;
        }
        reasons.add(reason);
        return weight;
    }
}

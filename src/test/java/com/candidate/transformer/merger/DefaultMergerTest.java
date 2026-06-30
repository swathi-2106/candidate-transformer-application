package com.candidate.transformer.merger;

import com.candidate.transformer.confidence.DefaultConfidenceCalculator;
import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.Confidence;
import com.candidate.transformer.model.Experience;
import com.candidate.transformer.model.Links;
import com.candidate.transformer.model.Location;
import com.candidate.transformer.model.MergeResult;
import com.candidate.transformer.model.Provenance;
import com.candidate.transformer.model.Skill;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMergerTest {

    private final DefaultMerger merger = new DefaultMerger();

    @Test
    void merge_ResolvesScalarConflictsDeterministicallyAndUnionsCollections() throws Exception {
        CandidateProfile csv = profile(
                "John Doe",
                "john.doe@example.com",
                "+1-123-456-7890",
                0.90,
                "csv-1",
                "CSV Extractor");
        csv.setLocation(new Location("New York", "NY", "USA", "10001", "New York, NY, USA"));
        csv.setLinks(new Links("linkedin.com/in/johndoe", null, null, null));
        csv.setSkills(List.of(
                new Skill("Java", 3, "Intermediate", new Confidence(0.70, List.of("CSV skill")), null),
                new Skill("Spring Boot", 4, "Intermediate", new Confidence(0.85, List.of("CSV skill")), null)));
        csv.setExperiences(List.of(new Experience(
                "Acme Inc.",
                "Senior Developer",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2022, 1, 1),
                "Built APIs",
                List.of("Java"),
                null,
                null,
                null)));

        CandidateProfile resume = profile(
                "Jonathan Doe",
                "john.doe@example.com",
                "+1-123-456-7890",
                0.82,
                "pdf-1",
                "Apache PDFBox Resume Extractor");
        resume.setLinks(new Links(null, "github.com/johndoe", null, null));
        resume.setSkills(List.of(
                new Skill("Java", 5, "Expert", new Confidence(0.95, List.of("Resume skill")), null),
                new Skill("AWS", 2, "Beginner", new Confidence(0.80, List.of("Resume skill")), null)));
        resume.setExperiences(List.of(new Experience(
                "Acme Inc.",
                "Senior Developer",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2022, 1, 1),
                "Built APIs",
                List.of("Java", "AWS"),
                null,
                null,
                null)));

        MergeResult result = merger.merge(List.of(csv, resume));
        CandidateProfile merged = result.getMergedRecord().getMergedProfile();

        assertEquals("John Doe", merged.getName());
        assertEquals("john.doe@example.com", merged.getEmail());
        assertEquals("+1-123-456-7890", merged.getPhone());
        assertEquals("linkedin.com/in/johndoe", merged.getLinks().getLinkedin());
        assertEquals("github.com/johndoe", merged.getLinks().getGithub());
        assertEquals(3, merged.getSkills().size());
        assertTrue(merged.getSkills().stream().anyMatch(skill -> "Java".equals(skill.getName())
                && Integer.valueOf(5).equals(skill.getYearsOfExperience())));
        assertEquals(1, merged.getExperiences().size());
        assertFalse(result.getConflicts().isEmpty());
        assertTrue(result.getConflicts().get(0).contains("name"));
        assertNotNull(merged.getId());
        assertTrue(merged.getProvenance().getSourceId().contains("csv-1"));
        assertTrue(merged.getProvenance().getSourceId().contains("pdf-1"));
    }

    @Test
    void confidenceCalculator_ScoresCompletenessAndExplainsReasons() {
        CandidateProfile profile = profile(
                "John Doe",
                "john.doe@example.com",
                "+1-123-456-7890",
                0.90,
                "merge-1",
                "Deterministic Merge Engine");
        profile.setLocation(new Location("New York", "NY", "USA", "10001", "New York, NY, USA"));
        profile.setLinks(new Links("linkedin.com/in/johndoe", "github.com/johndoe", null, null));
        profile.setSkills(List.of(new Skill("Java", 5, "Expert", null, null), new Skill("AWS", 2, "Beginner", null, null)));
        profile.setExperiences(List.of(new Experience()));

        Confidence confidence = new DefaultConfidenceCalculator().calculateConfidence(profile);

        assertTrue(confidence.getScore() >= 0.80);
        assertTrue(confidence.getReasons().stream().anyMatch(reason -> reason.contains("Email present")));
        assertTrue(confidence.getReasons().stream().anyMatch(reason -> reason.contains("Skills present")));
    }

    private CandidateProfile profile(String name, String email, String phone, double confidenceScore,
                                     String sourceId, String extractionMethod) {
        CandidateProfile profile = new CandidateProfile();
        profile.setName(name);
        profile.setEmail(email);
        profile.setPhone(phone);
        profile.setConfidence(new Confidence(confidenceScore, List.of(extractionMethod)));
        profile.setProvenance(new Provenance(sourceId, LocalDateTime.now(), extractionMethod, name));
        return profile;
    }
}

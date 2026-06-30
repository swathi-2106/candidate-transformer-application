package com.candidate.transformer.projection;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.Confidence;
import com.candidate.transformer.model.Location;
import com.candidate.transformer.model.ProjectionConfig;
import com.candidate.transformer.model.ProjectionField;
import com.candidate.transformer.model.Provenance;
import com.candidate.transformer.model.Skill;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultProjectionEngineTest {

    private final DefaultProjectionEngine engine = new DefaultProjectionEngine();

    @Test
    void project_AppliesSelectionRenameRemapNormalizationAndMetadata() {
        CandidateProfile profile = profile();

        ProjectionField email = new ProjectionField("email", true, "emailAddress");
        email.setNormalization("LOWERCASE");
        email.setIncludeConfidence(true);

        ProjectionField city = new ProjectionField("city", true, "candidateCity");
        city.setSourcePath("location.city");
        city.setNormalization("UPPERCASE");

        ProjectionField skills = new ProjectionField("skills", true, "skillsList");

        ProjectionConfig config = new ProjectionConfig(List.of(email, city, skills), "JSON");
        config.setIncludeProvenance(true);
        config.setMissingValuePolicy("ERROR");

        @SuppressWarnings("unchecked")
        Map<String, Object> projected = (Map<String, Object>) engine.project(profile, config);

        assertEquals("john.doe@example.com", projected.get("emailAddress"));
        assertEquals("NEW YORK", projected.get("candidateCity"));
        assertTrue(projected.containsKey("skillsList"));
        assertTrue(projected.containsKey("emailAddressConfidence"));
        assertTrue(projected.containsKey("provenance"));
        assertFalse(projected.containsKey("name"));
    }

    @Test
    void project_AppliesMissingValuePolicies() {
        CandidateProfile profile = profile();
        profile.setPhone(null);

        ProjectionField omitted = new ProjectionField("phone", true, "phoneNumber");
        omitted.setMissingValuePolicy("OMIT");
        ProjectionField defaulted = new ProjectionField("educations", true, "academicHistory");
        defaulted.setMissingValuePolicy("DEFAULT");
        defaulted.setDefaultValue(List.of());

        ProjectionConfig config = new ProjectionConfig(List.of(omitted, defaulted), "JSON");

        @SuppressWarnings("unchecked")
        Map<String, Object> projected = (Map<String, Object>) engine.project(profile, config);

        assertFalse(projected.containsKey("phoneNumber"));
        assertEquals(List.of(), projected.get("academicHistory"));
    }

    @Test
    void project_ThrowsForSchemaErrorsAndRequiredMissingValues() {
        CandidateProfile profile = profile();
        ProjectionField missing = new ProjectionField("phone", true, "phoneNumber");
        missing.setMissingValuePolicy("ERROR");
        ProjectionConfig missingConfig = new ProjectionConfig(List.of(missing), "JSON");

        profile.setPhone(null);
        assertThrows(IllegalArgumentException.class, () -> engine.project(profile, missingConfig));

        ProjectionConfig invalidConfig = new ProjectionConfig(List.of(new ProjectionField("email", true, "email")), "XML");
        assertThrows(IllegalArgumentException.class, () -> engine.project(profile(), invalidConfig));
    }

    private CandidateProfile profile() {
        CandidateProfile profile = new CandidateProfile();
        profile.setId("cand-1");
        profile.setName("John Doe");
        profile.setEmail(" JOHN.DOE@EXAMPLE.COM ");
        profile.setPhone("+1-123-456-7890");
        profile.setLocation(new Location("New York", "NY", "USA", "10001", "New York, NY, USA"));
        profile.setSkills(List.of(new Skill("Java", 5, "Expert", null, null)));
        profile.setConfidence(new Confidence(0.88, List.of("test confidence")));
        profile.setProvenance(new Provenance("src-1", LocalDateTime.now(), "test", "original"));
        return profile;
    }
}

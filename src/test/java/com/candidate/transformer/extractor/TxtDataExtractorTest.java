package com.candidate.transformer.extractor;

import com.candidate.transformer.exception.ValidationException;
import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.CandidateSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TxtDataExtractorTest {

    private TxtDataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new TxtDataExtractor();
    }

    @Test
    void testExtract_StructuredNotes() throws Exception {
        String text = "Candidate Name: John Doe\n" +
                "Email: john.doe@example.com\n" +
                "Phone: +1 123-456-7890\n" +
                "Skills: Java, Spring Boot\n" +
                "Experience: 5+ years of experience\n" +
                "LinkedIn: linkedin.com/in/johndoe\n" +
                "GitHub: github.com/johndoe\n";

        CandidateSource source = new CandidateSource("src-1", "notes.txt", "TXT", text, LocalDateTime.now());
        CandidateProfile profile = extractor.extract(source);

        assertNotNull(profile);
        assertEquals("John Doe", profile.getName());
        assertEquals("john.doe@example.com", profile.getEmail());
        assertEquals("+1 123-456-7890", profile.getPhone());
        assertEquals("linkedin.com/in/johndoe", profile.getLinks().getLinkedin());
        assertEquals("github.com/johndoe", profile.getLinks().getGithub());

        // Assert explicit header skills and dictionary keywords
        assertTrue(profile.getSkills().stream().anyMatch(s -> "Java".equals(s.getName())));
        assertTrue(profile.getSkills().stream().anyMatch(s -> "Spring Boot".equals(s.getName())));
        
        // Assert experience
        assertEquals(1, profile.getExperiences().size());
        assertTrue(profile.getExperiences().get(0).getDescription().contains("5+ YOE stated in unstructured notes"));
    }

    @Test
    void testExtract_UnstructuredMeetingNotes() throws Exception {
        String text = "Notes on Jane Smith\n" +
                "Had a call today. Reachable at jane.smith@example.org or +31-6-12345678.\n" +
                "She has 4 yoe in building backend systems. Demonstrated strong knowledge of Python, SQL, and Docker.\n" +
                "GitHub profile is github.com/janesmith.\n";

        CandidateSource source = new CandidateSource("src-2", "meeting_notes.txt", "TXT", text, LocalDateTime.now());
        CandidateProfile profile = extractor.extract(source);

        assertNotNull(profile);
        assertEquals("Jane Smith", profile.getName());
        assertEquals("jane.smith@example.org", profile.getEmail());
        assertEquals("+31-6-12345678", profile.getPhone());
        assertEquals("github.com/janesmith", profile.getLinks().getGithub());

        // Assert dictionary skills
        assertTrue(profile.getSkills().stream().anyMatch(s -> "Python".equals(s.getName())));
        assertTrue(profile.getSkills().stream().anyMatch(s -> "SQL".equals(s.getName())));
        assertTrue(profile.getSkills().stream().anyMatch(s -> "Docker".equals(s.getName())));

        // Assert experience
        assertEquals(1, profile.getExperiences().size());
        assertTrue(profile.getExperiences().get(0).getDescription().contains("4+ YOE"));
    }

    @Test
    void testExtract_MissingMandatoryName() {
        String text = "Email: candidate@example.com\n" +
                "Skills: Java, SQL\n";

        CandidateSource source = new CandidateSource("src-3", "notes.txt", "TXT", text, LocalDateTime.now());

        ValidationException ex = assertThrows(ValidationException.class, () -> extractor.extract(source));
        assertTrue(ex.getMessage().contains("Candidate Name could not be resolved"));
    }

    @Test
    void testExtract_MissingMandatoryEmail() {
        String text = "Candidate Name: John Doe\n" +
                "Phone: 123-456-7890\n";

        CandidateSource source = new CandidateSource("src-4", "notes.txt", "TXT", text, LocalDateTime.now());

        ValidationException ex = assertThrows(ValidationException.class, () -> extractor.extract(source));
        assertTrue(ex.getMessage().contains("Candidate Email address could not be found"));
    }

    @Test
    void testExtract_InvalidEmailFormat() {
        String text = "Candidate Name: John Doe\n" +
                "Email: john.doe_at_example_com\n";

        CandidateSource source = new CandidateSource("src-5", "notes.txt", "TXT", text, LocalDateTime.now());

        ValidationException ex = assertThrows(ValidationException.class, () -> extractor.extract(source));
        assertTrue(ex.getMessage().contains("Candidate Email address could not be found"));
    }

    @Test
    void testExtract_EmptyNotes() {
        CandidateSource source = new CandidateSource("src-6", "notes.txt", "TXT", "", LocalDateTime.now());

        assertThrows(ValidationException.class, () -> extractor.extract(source));
    }
}

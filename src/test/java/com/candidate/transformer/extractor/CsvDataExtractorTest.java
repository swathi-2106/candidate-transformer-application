package com.candidate.transformer.extractor;

import com.candidate.transformer.exception.ValidationException;
import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.CandidateSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CsvDataExtractorTest {

    private CsvDataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new CsvDataExtractor();
    }

    @Test
    void testExtract_ValidCsv() throws Exception {
        String csv = "Full Name,Mail,Phone Number,Technologies,YOE,Residence,LinkedIn URL,Degree\n" +
                "John Doe,john.doe@example.com,+1-123-456-7890,\"Java;Spring Boot;SQL\",5,\"New York, NY, USA\",linkedin.com/in/johndoe,BS in CS\n";

        CandidateSource source = new CandidateSource("src-1", "spreadsheet.csv", "CSV", csv, LocalDateTime.now());
        CandidateProfile profile = extractor.extract(source);

        assertNotNull(profile);
        assertEquals("John Doe", profile.getName());
        assertEquals("john.doe@example.com", profile.getEmail());
        assertEquals("+1-123-456-7890", profile.getPhone());
        assertNotNull(profile.getLocation());
        assertEquals("New York, NY, USA", profile.getLocation().getFormattedAddress());
        assertEquals("linkedin.com/in/johndoe", profile.getLinks().getLinkedin());
        
        // Assert Skills
        assertEquals(3, profile.getSkills().size());
        assertEquals("Java", profile.getSkills().get(0).getName());
        assertEquals("Spring Boot", profile.getSkills().get(1).getName());
        
        // Assert Experience
        assertEquals(1, profile.getExperiences().size());
        assertTrue(profile.getExperiences().get(0).getDescription().contains("5 YOE"));

        // Assert Education
        assertEquals(1, profile.getEducations().size());
        assertEquals("BS in CS", profile.getEducations().get(0).getDegree());
    }

    @Test
    void testExtract_MissingMandatoryName() {
        String csv = "Full Name,Mail,Phone Number\n" +
                ",john.doe@example.com,+1-123-456-7890\n";

        CandidateSource source = new CandidateSource("src-2", "spreadsheet.csv", "CSV", csv, LocalDateTime.now());

        ValidationException ex = assertThrows(ValidationException.class, () -> extractor.extract(source));
        assertTrue(ex.getMessage().contains("Mandatory field 'Name' is missing"));
    }

    @Test
    void testExtract_MissingMandatoryEmail() {
        String csv = "Full Name,Mail,Phone Number\n" +
                "John Doe,,+1-123-456-7890\n";

        CandidateSource source = new CandidateSource("src-3", "spreadsheet.csv", "CSV", csv, LocalDateTime.now());

        ValidationException ex = assertThrows(ValidationException.class, () -> extractor.extract(source));
        assertTrue(ex.getMessage().contains("Mandatory field 'Email' is missing"));
    }

    @Test
    void testExtract_InvalidEmailFormat() {
        String csv = "Full Name,Mail,Phone Number\n" +
                "John Doe,john.doe_at_example.com,+1-123-456-7890\n";

        CandidateSource source = new CandidateSource("src-4", "spreadsheet.csv", "CSV", csv, LocalDateTime.now());

        ValidationException ex = assertThrows(ValidationException.class, () -> extractor.extract(source));
        assertTrue(ex.getMessage().contains("Email address 'john.doe_at_example.com' is malformed"));
    }

    @Test
    void testExtract_EmptyCsv() {
        CandidateSource source = new CandidateSource("src-5", "spreadsheet.csv", "CSV", "", LocalDateTime.now());

        assertThrows(ValidationException.class, () -> extractor.extract(source));
    }
}

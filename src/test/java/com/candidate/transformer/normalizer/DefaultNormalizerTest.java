package com.candidate.transformer.normalizer;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.Education;
import com.candidate.transformer.model.Experience;
import com.candidate.transformer.model.Links;
import com.candidate.transformer.model.Location;
import com.candidate.transformer.model.Skill;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultNormalizerTest {

    private final DefaultNormalizer normalizer = new DefaultNormalizer();

    @Test
    void normalize_CleansContactAndProfileFields() {
        CandidateProfile profile = new CandidateProfile();
        profile.setId("  cand-1  ");
        profile.setName("  JOHN   o'CONNOR-smith ");
        profile.setEmail("  JOHN.DOE@EXAMPLE.COM ");
        profile.setPhone(" (123) 456-7890 ");
        profile.setLocation(new Location(" new   york ", " ny ", " united   states ", " 10001 ", " New   York, NY "));
        profile.setLinks(new Links(
                " https://WWW.LinkedIn.com/in/johndoe/ ",
                "HTTP://github.com/JohnDoe/",
                " www.JohnDoe.dev/ ",
                new HashMap<>()));

        CandidateProfile normalized = normalizer.normalize(profile);

        assertEquals("cand-1", normalized.getId());
        assertEquals("John O'Connor-Smith", normalized.getName());
        assertEquals("john.doe@example.com", normalized.getEmail());
        assertEquals("+1-123-456-7890", normalized.getPhone());
        assertEquals("New York", normalized.getLocation().getCity());
        assertEquals("NY", normalized.getLocation().getState());
        assertEquals("United States", normalized.getLocation().getCountry());
        assertEquals("linkedin.com/in/johndoe", normalized.getLinks().getLinkedin());
        assertEquals("github.com/JohnDoe", normalized.getLinks().getGithub());
        assertEquals("johndoe.dev", normalized.getLinks().getPortfolio());
    }

    @Test
    void normalize_DeduplicatesSkillsAndKeepsRichestYears() {
        CandidateProfile profile = new CandidateProfile();
        profile.setSkills(List.of(
                new Skill(" java ", 3, " expert ", null, null),
                new Skill("JAVA", 5, null, null, null),
                new Skill(" spring   boot ", 4, " intermediate ", null, null),
                new Skill("SQL", null, null, null, null)));

        CandidateProfile normalized = normalizer.normalize(profile);

        assertEquals(3, normalized.getSkills().size());
        assertEquals("Java", normalized.getSkills().get(0).getName());
        assertEquals(5, normalized.getSkills().get(0).getYearsOfExperience());
        assertEquals("Expert", normalized.getSkills().get(0).getLevel());
        assertEquals("Spring Boot", normalized.getSkills().get(1).getName());
        assertEquals("SQL", normalized.getSkills().get(2).getName());
    }

    @Test
    void normalize_CleansExperiencesDatesAndDuplicates() {
        CandidateProfile profile = new CandidateProfile();
        Experience first = new Experience(
                " acme   inc ",
                " senior   developer ",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2020, 1, 1),
                " Built   APIs ",
                List.of("java", " Java ", "aws"),
                new Location(" austin ", " tx ", " usa ", null, " Austin, TX "),
                null,
                null);
        Experience duplicate = new Experience(
                "ACME INC.",
                "Senior Developer",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2022, 1, 1),
                "Built APIs",
                List.of("Java"),
                null,
                null,
                null);
        profile.setExperiences(List.of(first, duplicate));

        CandidateProfile normalized = normalizer.normalize(profile);

        assertEquals(1, normalized.getExperiences().size());
        Experience experience = normalized.getExperiences().get(0);
        assertEquals("Acme Inc.", experience.getCompany());
        assertEquals("Senior Developer", experience.getRole());
        assertEquals(LocalDate.of(2020, 1, 1), experience.getStartDate());
        assertEquals(LocalDate.of(2022, 1, 1), experience.getEndDate());
        assertEquals("Built APIs", experience.getDescription());
        assertEquals(List.of("Java", "AWS"), experience.getSkillsUsed());
        assertEquals("TX", experience.getLocation().getState());
    }

    @Test
    void normalize_CleansEducationAndRemovesDuplicates() {
        CandidateProfile profile = new CandidateProfile();
        Education first = new Education(
                " state   university ",
                " BS in CS ",
                " computer   science ",
                LocalDate.of(2019, 5, 1),
                LocalDate.of(2015, 9, 1),
                3.8,
                null,
                null);
        Education duplicate = new Education(
                "State University",
                "BS in CS",
                "Computer Science",
                LocalDate.of(2015, 9, 1),
                LocalDate.of(2019, 5, 1),
                3.8,
                null,
                null);
        profile.setEducations(List.of(first, duplicate));

        CandidateProfile normalized = normalizer.normalize(profile);

        assertEquals(1, normalized.getEducations().size());
        Education education = normalized.getEducations().get(0);
        assertEquals("State University", education.getInstitution());
        assertEquals("BS in CS", education.getDegree());
        assertEquals("Computer Science", education.getFieldOfStudy());
        assertEquals(LocalDate.of(2015, 9, 1), education.getStartDate());
        assertEquals(LocalDate.of(2019, 5, 1), education.getEndDate());
    }
}

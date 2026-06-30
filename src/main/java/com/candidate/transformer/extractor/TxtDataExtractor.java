package com.candidate.transformer.extractor;

import com.candidate.transformer.exception.ValidationException;
import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.Confidence;
import com.candidate.transformer.model.Education;
import com.candidate.transformer.model.Experience;
import com.candidate.transformer.model.Links;
import com.candidate.transformer.model.Location;
import com.candidate.transformer.model.Provenance;
import com.candidate.transformer.model.Skill;
import com.candidate.transformer.model.CandidateSource;
import com.candidate.transformer.util.StringUtils;
import com.candidate.transformer.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts supported canonical candidate fields from free-form recruiter notes.
 */
@Component
public class TxtDataExtractor {

    private static final Logger logger = LoggerFactory.getLogger(TxtDataExtractor.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\w)\\+?\\d[\\d\\s().-]{6,20}\\d(?!\\w)");
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)\\b(?:https?://|www\\.)?([a-z0-9.-]+\\.[a-z]{2,})(?:/[^\\s,;)]*)?");
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile("(?i)\\b(\\d{1,2})\\s*\\+?\\s*(?:years?|yrs?|yoe)\\b");
    private static final Pattern NOTES_ON_NAME_PATTERN = Pattern.compile("(?im)^\\s*(?:notes[ \\t]+on|candidate)[ \\t]+([A-Z][A-Za-z.'-]+(?:[ \\t]+[A-Z][A-Za-z.'-]+){1,3})\\b");

    private static final List<String> NAME_LABELS = List.of("candidate name", "name", "full name");
    private static final List<String> EMAIL_LABELS = List.of("email", "email address", "mail");
    private static final List<String> PHONE_LABELS = List.of("phone", "phone number", "mobile", "contact");
    private static final List<String> LOCATION_LABELS = List.of("location", "residence", "city", "address");
    private static final List<String> SKILLS_LABELS = List.of("skills", "technical skills", "key skills", "technologies");
    private static final List<String> EXPERIENCE_LABELS = List.of("experience", "years of experience", "yoe", "work experience");
    private static final List<String> LINKEDIN_LABELS = List.of("linkedin", "linkedin url");
    private static final List<String> GITHUB_LABELS = List.of("github", "github url");
    private static final List<String> PORTFOLIO_LABELS = List.of("portfolio", "portfolio url", "website");
    private static final List<String> EDUCATION_LABELS = List.of("education", "degree", "qualification");

    private static final List<String> KNOWN_SKILLS = List.of(
            "Java", "Spring Boot", "Python", "SQL", "Docker", "Kubernetes", "AWS", "Azure", "GCP",
            "Microservices", "React", "Angular", "Node.js", "JavaScript", "TypeScript", "REST",
            "GraphQL", "PostgreSQL", "MySQL", "MongoDB", "Kafka", "Redis", "CI/CD", "Jenkins",
            "Terraform", "Linux", "Git", "Machine Learning", "Data Engineering"
    );

    public CandidateProfile extract(CandidateSource source) throws Exception {
        logger.info("Extracting candidate profile from TXT source: {}", source.getSourceName());
        return extractFromText(
                source,
                "TXT Notes Extractor",
                "unstructured notes",
                "Free-text recruiter notes parsed with label and keyword heuristics");
    }

    public CandidateProfile extractResumeText(CandidateSource source, String extractionMethod) throws Exception {
        logger.info("Extracting candidate profile from {} resume source: {}", source.getSourceType(), source.getSourceName());
        return extractFromText(
                source,
                extractionMethod,
                "resume text",
                "Resume text parsed with label, contact, and keyword heuristics");
    }

    private CandidateProfile extractFromText(CandidateSource source, String extractionMethod, String sourceKind,
                                             String confidenceReason) throws Exception {
        String rawContent = source.getRawContent();
        if (StringUtils.isEmpty(rawContent)) {
            throw new ValidationException(sourceKind + " content is empty for source: " + source.getSourceName());
        }

        Map<String, String> labeledValues = extractLabeledValues(rawContent);

        String rawEmail = firstNonEmpty(findByLabels(labeledValues, EMAIL_LABELS), findFirst(EMAIL_PATTERN, rawContent));
        if (StringUtils.isEmpty(rawEmail) || !ValidationUtils.isValidEmail(rawEmail)) {
            throw new ValidationException("Candidate Email address could not be found in " + sourceKind + " source " + source.getSourceName());
        }

        String rawName = firstNonEmpty(
                findByLabels(labeledValues, NAME_LABELS),
                firstNonEmpty(extractNameFromNotes(rawContent), extractNameFromResumeHeader(rawContent)));
        if (StringUtils.isEmpty(rawName)) {
            throw new ValidationException("Candidate Name could not be resolved from " + sourceKind + " source " + source.getSourceName());
        }

        CandidateProfile profile = new CandidateProfile();
        profile.setName(rawName.trim());
        profile.setEmail(rawEmail.trim().toLowerCase(Locale.ROOT));

        String rawPhone = cleanPhone(firstNonEmpty(findByLabels(labeledValues, PHONE_LABELS), findFirst(PHONE_PATTERN, rawContent)));
        if (!StringUtils.isEmpty(rawPhone)) {
            if (!ValidationUtils.isValidPhone(rawPhone)) {
                logger.warn("Validation Warning: Phone number '{}' has an anomalous format in source {}", rawPhone, source.getSourceName());
            }
            profile.setPhone(rawPhone);
        }

        mapLocation(profile, findByLabels(labeledValues, LOCATION_LABELS));
        mapLinks(profile, labeledValues, rawContent);
        mapSkills(profile, source, labeledValues, firstNonEmpty(findByLabels(labeledValues, SKILLS_LABELS), rawContent), extractionMethod, sourceKind);
        mapExperience(profile, source, firstNonEmpty(findByLabels(labeledValues, EXPERIENCE_LABELS), rawContent), extractionMethod, sourceKind);
        mapEducation(profile, source, findByLabels(labeledValues, EDUCATION_LABELS), extractionMethod, sourceKind);

        profile.setProvenance(provenance(source, rawContent, extractionMethod));
        profile.setConfidence(new Confidence(0.78, List.of(confidenceReason)));
        return profile;
    }

    private Map<String, String> extractLabeledValues(String rawContent) {
        Map<String, String> values = new LinkedHashMap<>();
        String[] lines = rawContent.split("\\R");
        for (String line : lines) {
            Matcher matcher = Pattern.compile("^\\s*([A-Za-z][A-Za-z _/-]{1,40})\\s*:\\s*(.+?)\\s*$").matcher(line);
            if (matcher.matches()) {
                String key = normalizeKey(matcher.group(1));
                String value = stripTrailingPunctuation(matcher.group(2));
                if (!StringUtils.isEmpty(value)) {
                    values.put(key, value);
                }
            }
        }
        return values;
    }

    private String findByLabels(Map<String, String> labeledValues, List<String> labels) {
        for (String label : labels) {
            String value = labeledValues.get(normalizeKey(label));
            if (!StringUtils.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private String extractNameFromNotes(String rawContent) {
        Matcher matcher = NOTES_ON_NAME_PATTERN.matcher(rawContent);
        if (matcher.find()) {
            return stripTrailingPunctuation(matcher.group(1));
        }
        return null;
    }

    private String extractNameFromResumeHeader(String rawContent) {
        String[] lines = rawContent.split("\\R");
        for (String line : lines) {
            String candidate = stripTrailingPunctuation(line);
            if (StringUtils.isEmpty(candidate) || candidate.contains("@") || candidate.length() > 80) {
                continue;
            }
            if (candidate.matches("[A-Z][A-Za-z.'-]+(?:[ \\t]+[A-Z][A-Za-z.'-]+){1,3}")) {
                return candidate;
            }
        }
        return null;
    }

    private void mapLocation(CandidateProfile profile, String rawLocation) {
        if (StringUtils.isEmpty(rawLocation)) {
            return;
        }
        Location location = new Location();
        location.setFormattedAddress(rawLocation.trim());
        location.setCity(rawLocation.trim());
        profile.setLocation(location);
    }

    private void mapLinks(CandidateProfile profile, Map<String, String> labeledValues, String rawContent) {
        Links links = new Links();
        links.setLinkedin(firstNonEmpty(findByLabels(labeledValues, LINKEDIN_LABELS), findUrlContaining(rawContent, "linkedin.com")));
        links.setGithub(firstNonEmpty(findByLabels(labeledValues, GITHUB_LABELS), findUrlContaining(rawContent, "github.com")));
        links.setPortfolio(firstNonEmpty(findByLabels(labeledValues, PORTFOLIO_LABELS), findPortfolioUrl(rawContent)));
        profile.setLinks(links);
    }

    private void mapSkills(CandidateProfile profile, CandidateSource source, Map<String, String> labeledValues,
                           String skillText, String extractionMethod, String sourceKind) {
        Map<String, Skill> skillsByKey = new LinkedHashMap<>();

        String labeledSkills = findByLabels(labeledValues, SKILLS_LABELS);
        if (!StringUtils.isEmpty(labeledSkills)) {
            for (String token : labeledSkills.split("[;,]")) {
                addSkill(skillsByKey, source, token.trim(), 0.85, "Extracted from " + sourceKind + " skills label", extractionMethod);
            }
        }

        for (String knownSkill : KNOWN_SKILLS) {
            Pattern skillPattern = Pattern.compile("(?i)(?<![A-Za-z0-9+#])" + Pattern.quote(knownSkill) + "(?![A-Za-z0-9+#])");
            if (skillPattern.matcher(skillText).find()) {
                addSkill(skillsByKey, source, knownSkill, 0.72, "Matched " + sourceKind + " keyword dictionary", extractionMethod);
            }
        }

        profile.getSkills().addAll(skillsByKey.values());
    }

    private void addSkill(Map<String, Skill> skillsByKey, CandidateSource source, String skillName, double confidenceScore,
                          String reason, String extractionMethod) {
        if (StringUtils.isEmpty(skillName)) {
            return;
        }

        String normalizedKey = skillName.trim().toLowerCase(Locale.ROOT);
        skillsByKey.computeIfAbsent(normalizedKey, ignored -> {
            Skill skill = new Skill();
            skill.setName(skillName.trim());
            skill.setProvenance(provenance(source, skillName.trim(), extractionMethod));
            skill.setConfidence(new Confidence(confidenceScore, List.of(reason)));
            return skill;
        });
    }

    private void mapExperience(CandidateProfile profile, CandidateSource source, String experienceText,
                               String extractionMethod, String sourceKind) {
        Matcher matcher = EXPERIENCE_PATTERN.matcher(experienceText);
        if (!matcher.find()) {
            return;
        }

        int years = Integer.parseInt(matcher.group(1));
        String originalValue = matcher.group(0).trim();

        Experience experience = new Experience();
        experience.setCompany("Various / Historic");
        experience.setRole("Professional Experience");
        experience.setDescription(years + "+ YOE stated in " + sourceKind);
        experience.setStartDate(LocalDate.now().minusYears(years));
        experience.setEndDate(LocalDate.now());
        experience.setProvenance(provenance(source, originalValue, extractionMethod));
        experience.setConfidence(new Confidence(0.74, List.of("Years of experience inferred from " + sourceKind)));
        profile.getExperiences().add(experience);
    }

    private void mapEducation(CandidateProfile profile, CandidateSource source, String rawEducation,
                              String extractionMethod, String sourceKind) {
        if (StringUtils.isEmpty(rawEducation)) {
            return;
        }

        Education education = new Education();
        education.setDegree(rawEducation.trim());
        education.setInstitution("Unknown Institution");
        education.setProvenance(provenance(source, rawEducation, extractionMethod));
        education.setConfidence(new Confidence(0.75, List.of("Extracted from " + sourceKind + " education label")));
        profile.getEducations().add(education);
    }

    private String findPortfolioUrl(String rawContent) {
        Matcher matcher = URL_PATTERN.matcher(rawContent);
        while (matcher.find()) {
            String url = matcher.group(0);
            String lowerUrl = url.toLowerCase(Locale.ROOT);
            if (!lowerUrl.contains("linkedin.com") && !lowerUrl.contains("github.com")) {
                return stripTrailingPunctuation(url);
            }
        }
        return null;
    }

    private String findUrlContaining(String rawContent, String domain) {
        Matcher matcher = URL_PATTERN.matcher(rawContent);
        while (matcher.find()) {
            String url = matcher.group(0);
            if (url.toLowerCase(Locale.ROOT).contains(domain)) {
                return stripTrailingPunctuation(url);
            }
        }
        return null;
    }

    private String findFirst(Pattern pattern, String rawContent) {
        Matcher matcher = pattern.matcher(rawContent);
        return matcher.find() ? stripTrailingPunctuation(matcher.group(0)) : null;
    }

    private String cleanPhone(String rawPhone) {
        if (StringUtils.isEmpty(rawPhone)) {
            return null;
        }
        String cleaned = rawPhone.trim().replace("(", "").replace(")", "");
        return stripTrailingPunctuation(cleaned);
    }

    private String stripTrailingPunctuation(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("[\\s,;.]+$", "");
    }

    private String normalizeKey(String key) {
        return key.trim().toLowerCase(Locale.ROOT).replace('_', ' ').replace('-', ' ').replaceAll("\\s+", " ");
    }

    private String firstNonEmpty(String first, String second) {
        return !StringUtils.isEmpty(first) ? first : second;
    }

    private Provenance provenance(CandidateSource source, String originalValue, String extractionMethod) {
        LocalDateTime extractedAt = Optional.ofNullable(source.getReceivedAt()).orElse(LocalDateTime.now());
        return new Provenance(source.getSourceId(), extractedAt, extractionMethod, originalValue);
    }
}

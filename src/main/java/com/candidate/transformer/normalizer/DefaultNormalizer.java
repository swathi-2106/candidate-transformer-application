package com.candidate.transformer.normalizer;

import com.candidate.transformer.model.Education;
import com.candidate.transformer.model.Experience;
import com.candidate.transformer.model.Links;
import com.candidate.transformer.model.Location;
import com.candidate.transformer.model.Skill;
import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.util.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Default implementation of canonical cleanup, formatting, and duplicate removal.
 */
@Component
public class DefaultNormalizer implements Normalizer {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private static final Map<String, String> SKILL_CANONICAL_NAMES = Map.ofEntries(
            Map.entry("aws", "AWS"),
            Map.entry("gcp", "GCP"),
            Map.entry("sql", "SQL"),
            Map.entry("ci/cd", "CI/CD"),
            Map.entry("rest", "REST"),
            Map.entry("api", "API"),
            Map.entry("javascript", "JavaScript"),
            Map.entry("typescript", "TypeScript"),
            Map.entry("node.js", "Node.js"),
            Map.entry("spring boot", "Spring Boot"),
            Map.entry("postgresql", "PostgreSQL"),
            Map.entry("mysql", "MySQL"),
            Map.entry("mongodb", "MongoDB"),
            Map.entry("docker", "Docker"),
            Map.entry("kubernetes", "Kubernetes"),
            Map.entry("terraform", "Terraform"),
            Map.entry("python", "Python"),
            Map.entry("java", "Java"),
            Map.entry("react", "React"),
            Map.entry("angular", "Angular"),
            Map.entry("microservices", "Microservices"),
            Map.entry("machine learning", "Machine Learning"),
            Map.entry("data engineering", "Data Engineering")
    );

    private static final Map<String, String> COMPANY_SUFFIXES = Map.ofEntries(
            Map.entry("inc", "Inc."),
            Map.entry("inc.", "Inc."),
            Map.entry("llc", "LLC"),
            Map.entry("ltd", "Ltd."),
            Map.entry("ltd.", "Ltd."),
            Map.entry("corp", "Corp."),
            Map.entry("corp.", "Corp."),
            Map.entry("co", "Co."),
            Map.entry("co.", "Co."),
            Map.entry("gmbh", "GmbH"),
            Map.entry("plc", "PLC"),
            Map.entry("pvt", "Pvt."),
            Map.entry("pvt.", "Pvt.")
    );

    @Override
    public CandidateProfile normalize(CandidateProfile profile) {
        if (profile == null) {
            return null;
        }

        profile.setId(cleanToNull(profile.getId()));
        profile.setName(normalizePersonName(profile.getName()));
        profile.setEmail(normalizeEmail(profile.getEmail()));
        profile.setPhone(normalizePhone(profile.getPhone()));
        profile.setLocation(normalizeLocation(profile.getLocation()));
        profile.setLinks(normalizeLinks(profile.getLinks()));
        profile.setSkills(normalizeSkills(profile.getSkills()));
        profile.setExperiences(normalizeExperiences(profile.getExperiences()));
        profile.setEducations(normalizeEducations(profile.getEducations()));
        return profile;
    }

    private String normalizeEmail(String email) {
        String cleaned = cleanToNull(email);
        return cleaned == null ? null : cleaned.toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        String cleaned = cleanToNull(phone);
        if (cleaned == null) {
            return null;
        }

        boolean hadPlus = cleaned.startsWith("+");
        String digits = cleaned.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() == 10) {
            digits = "1" + digits;
        }
        if (!hadPlus && digits.length() < 11) {
            return digits;
        }
        if (digits.length() == 11 && digits.startsWith("1")) {
            return "+1-" + digits.substring(1, 4) + "-" + digits.substring(4, 7) + "-" + digits.substring(7);
        }
        return "+" + digits;
    }

    private String normalizePersonName(String name) {
        String cleaned = cleanToNull(name);
        return cleaned == null ? null : titleCasePhrase(cleaned);
    }

    private String normalizeCompanyName(String company) {
        String cleaned = cleanToNull(company);
        if (cleaned == null) {
            return null;
        }

        List<String> normalizedWords = new ArrayList<>();
        for (String word : cleaned.split(" ")) {
            String suffix = COMPANY_SUFFIXES.get(word.toLowerCase(Locale.ROOT));
            normalizedWords.add(suffix != null ? suffix : titleCaseToken(word));
        }
        return String.join(" ", normalizedWords);
    }

    private String normalizeTitle(String value) {
        String cleaned = cleanToNull(value);
        return cleaned == null ? null : titleCasePhrase(cleaned);
    }

    private Location normalizeLocation(Location location) {
        if (location == null) {
            return null;
        }
        location.setCity(normalizeTitle(location.getCity()));
        location.setState(cleanToNull(location.getState()));
        if (location.getState() != null && location.getState().length() <= 3) {
            location.setState(location.getState().toUpperCase(Locale.ROOT));
        } else {
            location.setState(normalizeTitle(location.getState()));
        }
        location.setCountry(normalizeTitle(location.getCountry()));
        location.setPostalCode(cleanToNull(location.getPostalCode()));
        location.setFormattedAddress(cleanToNull(location.getFormattedAddress()));
        return location;
    }

    private Links normalizeLinks(Links links) {
        if (links == null) {
            return null;
        }
        links.setLinkedin(normalizeUrl(links.getLinkedin()));
        links.setGithub(normalizeUrl(links.getGithub()));
        links.setPortfolio(normalizeUrl(links.getPortfolio()));
        Map<String, String> normalizedOther = new LinkedHashMap<>();
        if (links.getOther() != null) {
            for (Map.Entry<String, String> entry : links.getOther().entrySet()) {
                String key = cleanToNull(entry.getKey());
                String value = normalizeUrl(entry.getValue());
                if (key != null && value != null) {
                    normalizedOther.put(key.toLowerCase(Locale.ROOT), value);
                }
            }
        }
        links.setOther(normalizedOther);
        return links;
    }

    private List<Skill> normalizeSkills(List<Skill> skills) {
        Map<String, Skill> deduped = new LinkedHashMap<>();
        if (skills == null) {
            return new ArrayList<>();
        }

        for (Skill skill : skills) {
            if (skill == null) {
                continue;
            }
            String normalizedName = normalizeSkillName(skill.getName());
            if (normalizedName == null) {
                continue;
            }
            skill.setName(normalizedName);
            skill.setLevel(normalizeTitle(skill.getLevel()));
            String key = normalizedName.toLowerCase(Locale.ROOT);
            Skill existing = deduped.get(key);
            if (existing == null) {
                deduped.put(key, skill);
            } else {
                mergeSkill(existing, skill);
            }
        }
        return new ArrayList<>(deduped.values());
    }

    private void mergeSkill(Skill target, Skill incoming) {
        if (target.getYearsOfExperience() == null || (incoming.getYearsOfExperience() != null
                && incoming.getYearsOfExperience() > target.getYearsOfExperience())) {
            target.setYearsOfExperience(incoming.getYearsOfExperience());
        }
        if (target.getLevel() == null) {
            target.setLevel(incoming.getLevel());
        }
        if (target.getConfidence() == null) {
            target.setConfidence(incoming.getConfidence());
        }
        if (target.getProvenance() == null) {
            target.setProvenance(incoming.getProvenance());
        }
    }

    private List<Experience> normalizeExperiences(List<Experience> experiences) {
        Map<String, Experience> deduped = new LinkedHashMap<>();
        if (experiences == null) {
            return new ArrayList<>();
        }

        for (Experience experience : experiences) {
            if (experience == null) {
                continue;
            }
            experience.setCompany(normalizeCompanyName(experience.getCompany()));
            experience.setRole(normalizeTitle(experience.getRole()));
            experience.setDescription(cleanToNull(experience.getDescription()));
            experience.setSkillsUsed(normalizeSkillNames(experience.getSkillsUsed()));
            experience.setLocation(normalizeLocation(experience.getLocation()));
            normalizeDateOrder(experience);

            String key = key(
                    experience.getCompany(),
                    experience.getRole(),
                    dateKey(experience.getStartDate()),
                    dateKey(experience.getEndDate()));
            if (!deduped.containsKey(key)) {
                deduped.put(key, experience);
            }
        }
        return new ArrayList<>(deduped.values());
    }

    private List<Education> normalizeEducations(List<Education> educations) {
        Map<String, Education> deduped = new LinkedHashMap<>();
        if (educations == null) {
            return new ArrayList<>();
        }

        for (Education education : educations) {
            if (education == null) {
                continue;
            }
            education.setInstitution(normalizeTitle(education.getInstitution()));
            education.setDegree(cleanToNull(education.getDegree()));
            education.setFieldOfStudy(normalizeTitle(education.getFieldOfStudy()));
            if (education.getStartDate() != null && education.getEndDate() != null
                    && education.getStartDate().isAfter(education.getEndDate())) {
                LocalDate startDate = education.getStartDate();
                education.setStartDate(education.getEndDate());
                education.setEndDate(startDate);
            }
            String key = key(
                    education.getInstitution(),
                    education.getDegree(),
                    education.getFieldOfStudy(),
                    dateKey(education.getStartDate()),
                    dateKey(education.getEndDate()));
            if (!deduped.containsKey(key)) {
                deduped.put(key, education);
            }
        }
        return new ArrayList<>(deduped.values());
    }

    private List<String> normalizeSkillNames(List<String> skillNames) {
        Map<String, String> deduped = new LinkedHashMap<>();
        if (skillNames == null) {
            return new ArrayList<>();
        }
        for (String skillName : skillNames) {
            String normalized = normalizeSkillName(skillName);
            if (normalized != null) {
                deduped.putIfAbsent(normalized.toLowerCase(Locale.ROOT), normalized);
            }
        }
        return new ArrayList<>(deduped.values());
    }

    private void normalizeDateOrder(Experience experience) {
        if (experience.getStartDate() != null && experience.getEndDate() != null
                && experience.getStartDate().isAfter(experience.getEndDate())) {
            LocalDate startDate = experience.getStartDate();
            experience.setStartDate(experience.getEndDate());
            experience.setEndDate(startDate);
        }
    }

    private String normalizeSkillName(String skillName) {
        String cleaned = cleanToNull(skillName);
        if (cleaned == null) {
            return null;
        }
        String canonical = SKILL_CANONICAL_NAMES.get(cleaned.toLowerCase(Locale.ROOT));
        return canonical != null ? canonical : titleCasePhrase(cleaned);
    }

    private String normalizeUrl(String url) {
        String cleaned = cleanToNull(url);
        if (cleaned == null) {
            return null;
        }
        cleaned = cleaned.replaceFirst("(?i)^https?://", "").replaceFirst("(?i)^www\\.", "");
        cleaned = cleaned.replaceAll("/+$", "");
        int slashIndex = cleaned.indexOf('/');
        if (slashIndex < 0) {
            return cleaned.toLowerCase(Locale.ROOT);
        }
        return cleaned.substring(0, slashIndex).toLowerCase(Locale.ROOT) + cleaned.substring(slashIndex);
    }

    private String titleCasePhrase(String value) {
        List<String> normalizedWords = new ArrayList<>();
        for (String word : value.split(" ")) {
            normalizedWords.add(titleCaseToken(word));
        }
        return String.join(" ", normalizedWords);
    }

    private String titleCaseToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return token;
        }
        String[] hyphenParts = token.split("-", -1);
        for (int i = 0; i < hyphenParts.length; i++) {
            hyphenParts[i] = titleCaseApostropheToken(hyphenParts[i]);
        }
        return String.join("-", hyphenParts);
    }

    private String titleCaseApostropheToken(String token) {
        String[] parts = token.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = titleCaseSimple(parts[i]);
        }
        return String.join("'", parts);
    }

    private String titleCaseSimple(String token) {
        if (token.isEmpty()) {
            return token;
        }
        if (token.length() <= 3 && token.equals(token.toUpperCase(Locale.ROOT))) {
            return token;
        }
        String lower = token.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String cleanToNull(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return WHITESPACE.matcher(value.trim()).replaceAll(" ");
    }

    private String dateKey(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    private String key(String... values) {
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            normalized.add(value == null ? "" : value.toLowerCase(Locale.ROOT));
        }
        return String.join("|", normalized);
    }
}

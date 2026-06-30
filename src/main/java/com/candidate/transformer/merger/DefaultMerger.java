package com.candidate.transformer.merger;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.CandidateRecord;
import com.candidate.transformer.model.Confidence;
import com.candidate.transformer.model.Education;
import com.candidate.transformer.model.Experience;
import com.candidate.transformer.model.Links;
import com.candidate.transformer.model.Location;
import com.candidate.transformer.model.MergeResult;
import com.candidate.transformer.model.Provenance;
import com.candidate.transformer.model.Skill;
import com.candidate.transformer.util.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Deterministically merges duplicate candidate profiles into one canonical profile.
 */
@Component
public class DefaultMerger implements Merger {

    @Override
    public MergeResult merge(List<CandidateProfile> profiles) {
        List<CandidateProfile> validProfiles = profiles == null
                ? new ArrayList<>()
                : profiles.stream().filter(profile -> profile != null).toList();

        CandidateProfile merged = new CandidateProfile();
        List<String> conflicts = new ArrayList<>();

        if (!validProfiles.isEmpty()) {
            mergeScalar("name", validProfiles, CandidateProfile::getName, CandidateProfile::setName, merged, conflicts);
            mergeScalar("email", validProfiles, CandidateProfile::getEmail, CandidateProfile::setEmail, merged, conflicts);
            mergeScalar("phone", validProfiles, CandidateProfile::getPhone, CandidateProfile::setPhone, merged, conflicts);
            merged.setLocation(mergeLocation(validProfiles, conflicts));
            merged.setLinks(mergeLinks(validProfiles, conflicts));
            merged.setSkills(mergeSkills(validProfiles));
            merged.setExperiences(mergeExperiences(validProfiles));
            merged.setEducations(mergeEducations(validProfiles));
            merged.setProvenance(generateMergeProvenance(validProfiles));
            merged.setConfidence(new Confidence(0.0, List.of("Pending final confidence calculation")));
            merged.setId(generateCandidateId(merged));
        }

        CandidateRecord record = new CandidateRecord();
        record.setRecordId(generateRecordId(merged, validProfiles));
        record.setMergedProfile(merged);

        MergeResult result = new MergeResult();
        result.setMergedRecord(record);
        result.setConflicts(conflicts);
        result.setMergeSummary("Merged " + validProfiles.size() + " profile(s); resolved " + conflicts.size() + " conflict(s).");
        return result;
    }

    private void mergeScalar(String fieldName, List<CandidateProfile> profiles,
                             Function<CandidateProfile, String> getter,
                             BiConsumer<CandidateProfile, String> setter,
                             CandidateProfile merged,
                             List<String> conflicts) {
        List<FieldCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            String value = clean(getter.apply(profiles.get(i)));
            if (value != null) {
                candidates.add(new FieldCandidate(value, profiles.get(i), i));
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        FieldCandidate winner = candidates.stream().max(candidateComparator()).orElse(candidates.get(0));
        setter.accept(merged, winner.value());
        addConflictIfNeeded(fieldName, winner.value(), candidates, conflicts);
    }

    private Location mergeLocation(List<CandidateProfile> profiles, List<String> conflicts) {
        Location merged = new Location();
        mergeLocationScalar("location.city", profiles, location -> location.getCity(), merged::setCity, conflicts);
        mergeLocationScalar("location.state", profiles, location -> location.getState(), merged::setState, conflicts);
        mergeLocationScalar("location.country", profiles, location -> location.getCountry(), merged::setCountry, conflicts);
        mergeLocationScalar("location.postalCode", profiles, location -> location.getPostalCode(), merged::setPostalCode, conflicts);
        mergeLocationScalar("location.formattedAddress", profiles, location -> location.getFormattedAddress(), merged::setFormattedAddress, conflicts);

        if (allLocationFieldsEmpty(merged)) {
            return null;
        }
        if (merged.getFormattedAddress() == null) {
            List<String> parts = new ArrayList<>();
            if (merged.getCity() != null) parts.add(merged.getCity());
            if (merged.getState() != null) parts.add(merged.getState());
            if (merged.getCountry() != null) parts.add(merged.getCountry());
            merged.setFormattedAddress(String.join(", ", parts));
        }
        return merged;
    }

    private void mergeLocationScalar(String fieldName, List<CandidateProfile> profiles,
                                     Function<Location, String> getter,
                                     Consumer<String> setter,
                                     List<String> conflicts) {
        List<FieldCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            Location location = profiles.get(i).getLocation();
            if (location == null) {
                continue;
            }
            String value = clean(getter.apply(location));
            if (value != null) {
                candidates.add(new FieldCandidate(value, profiles.get(i), i));
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        FieldCandidate winner = candidates.stream().max(candidateComparator()).orElse(candidates.get(0));
        setter.accept(winner.value());
        addConflictIfNeeded(fieldName, winner.value(), candidates, conflicts);
    }

    private Links mergeLinks(List<CandidateProfile> profiles, List<String> conflicts) {
        Links merged = new Links();
        mergeLinkScalar("links.linkedin", profiles, links -> links.getLinkedin(), merged::setLinkedin, conflicts);
        mergeLinkScalar("links.github", profiles, links -> links.getGithub(), merged::setGithub, conflicts);
        mergeLinkScalar("links.portfolio", profiles, links -> links.getPortfolio(), merged::setPortfolio, conflicts);

        Map<String, String> other = new LinkedHashMap<>();
        for (CandidateProfile profile : profiles) {
            Links links = profile.getLinks();
            if (links == null || links.getOther() == null) {
                continue;
            }
            for (Map.Entry<String, String> entry : links.getOther().entrySet()) {
                String key = clean(entry.getKey());
                String value = clean(entry.getValue());
                if (key != null && value != null) {
                    other.putIfAbsent(key.toLowerCase(Locale.ROOT), value);
                }
            }
        }
        merged.setOther(other);
        if (merged.getLinkedin() == null && merged.getGithub() == null && merged.getPortfolio() == null && other.isEmpty()) {
            return null;
        }
        return merged;
    }

    private void mergeLinkScalar(String fieldName, List<CandidateProfile> profiles,
                                 Function<Links, String> getter,
                                 Consumer<String> setter,
                                 List<String> conflicts) {
        List<FieldCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            Links links = profiles.get(i).getLinks();
            if (links == null) {
                continue;
            }
            String value = clean(getter.apply(links));
            if (value != null) {
                candidates.add(new FieldCandidate(value, profiles.get(i), i));
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        FieldCandidate winner = candidates.stream().max(candidateComparator()).orElse(candidates.get(0));
        setter.accept(winner.value());
        addConflictIfNeeded(fieldName, winner.value(), candidates, conflicts);
    }

    private List<Skill> mergeSkills(List<CandidateProfile> profiles) {
        Map<String, Skill> deduped = new LinkedHashMap<>();
        for (CandidateProfile profile : profiles) {
            if (profile.getSkills() == null) {
                continue;
            }
            for (Skill skill : profile.getSkills()) {
                if (skill == null || clean(skill.getName()) == null) {
                    continue;
                }
                String key = skill.getName().toLowerCase(Locale.ROOT);
                Skill existing = deduped.get(key);
                if (existing == null || compareSkill(skill, existing) > 0) {
                    deduped.put(key, skill);
                }
            }
        }
        return new ArrayList<>(deduped.values());
    }

    private List<Experience> mergeExperiences(List<CandidateProfile> profiles) {
        Map<String, Experience> deduped = new LinkedHashMap<>();
        for (CandidateProfile profile : profiles) {
            if (profile.getExperiences() == null) {
                continue;
            }
            for (Experience experience : profile.getExperiences()) {
                if (experience == null) {
                    continue;
                }
                deduped.putIfAbsent(experienceKey(experience), experience);
            }
        }
        return new ArrayList<>(deduped.values());
    }

    private List<Education> mergeEducations(List<CandidateProfile> profiles) {
        Map<String, Education> deduped = new LinkedHashMap<>();
        for (CandidateProfile profile : profiles) {
            if (profile.getEducations() == null) {
                continue;
            }
            for (Education education : profile.getEducations()) {
                if (education == null) {
                    continue;
                }
                deduped.putIfAbsent(educationKey(education), education);
            }
        }
        return new ArrayList<>(deduped.values());
    }

    private Comparator<FieldCandidate> candidateComparator() {
        return Comparator.comparingDouble(this::candidateScore)
                .thenComparingInt(candidate -> sourcePriority(candidate.profile()))
                .thenComparingInt(candidate -> candidate.value().length())
                .thenComparingInt(candidate -> -candidate.sourceIndex());
    }

    private double candidateScore(FieldCandidate candidate) {
        Confidence confidence = candidate.profile().getConfidence();
        return confidence == null ? 0.5 : confidence.getScore();
    }

    private int sourcePriority(CandidateProfile profile) {
        Provenance provenance = profile.getProvenance();
        String method = provenance == null || provenance.getExtractionMethod() == null
                ? ""
                : provenance.getExtractionMethod().toLowerCase(Locale.ROOT);
        if (method.contains("pdfbox") || method.contains("poi") || method.contains("resume")) {
            return 40;
        }
        if (method.contains("csv") || method.contains("spreadsheet")) {
            return 30;
        }
        if (method.contains("txt") || method.contains("notes")) {
            return 20;
        }
        return 10;
    }

    private int compareSkill(Skill left, Skill right) {
        int confidenceCompare = Double.compare(confidenceScore(left.getConfidence()), confidenceScore(right.getConfidence()));
        if (confidenceCompare != 0) {
            return confidenceCompare;
        }
        int yearsCompare = Integer.compare(
                left.getYearsOfExperience() == null ? -1 : left.getYearsOfExperience(),
                right.getYearsOfExperience() == null ? -1 : right.getYearsOfExperience());
        if (yearsCompare != 0) {
            return yearsCompare;
        }
        return Integer.compare(clean(left.getName()).length(), clean(right.getName()).length());
    }

    private double confidenceScore(Confidence confidence) {
        return confidence == null ? 0.5 : confidence.getScore();
    }

    private void addConflictIfNeeded(String fieldName, String winner, List<FieldCandidate> candidates, List<String> conflicts) {
        List<String> distinctValues = candidates.stream()
                .map(FieldCandidate::value)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
        if (distinctValues.size() <= 1) {
            return;
        }
        List<String> originalValues = candidates.stream()
                .map(FieldCandidate::value)
                .distinct()
                .toList();
        conflicts.add("Resolved " + fieldName + " conflict; selected '" + winner + "' from candidates " + originalValues + ".");
    }

    private Provenance generateMergeProvenance(List<CandidateProfile> profiles) {
        String sourceIds = profiles.stream()
                .map(CandidateProfile::getProvenance)
                .filter(provenance -> provenance != null && provenance.getSourceId() != null)
                .map(Provenance::getSourceId)
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElse("unknown");
        return new Provenance(sourceIds, LocalDateTime.now(), "Deterministic Merge Engine", "Merged " + profiles.size() + " normalized profiles");
    }

    private String generateCandidateId(CandidateProfile profile) {
        String seed = clean(profile.getEmail());
        if (seed == null) {
            seed = clean(profile.getName());
        }
        if (seed == null) {
            seed = "unknown-candidate";
        }
        return "cand-" + UUID.nameUUIDFromBytes(seed.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8))
                .toString()
                .substring(0, 8);
    }

    private String generateRecordId(CandidateProfile merged, List<CandidateProfile> profiles) {
        String seed = merged.getId() == null ? "record-" + profiles.size() : merged.getId();
        return "record-" + UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString().substring(0, 8);
    }

    private boolean allLocationFieldsEmpty(Location location) {
        return location.getCity() == null
                && location.getState() == null
                && location.getCountry() == null
                && location.getPostalCode() == null
                && location.getFormattedAddress() == null;
    }

    private String experienceKey(Experience experience) {
        return key(experience.getCompany(), experience.getRole(), date(experience.getStartDate()), date(experience.getEndDate()));
    }

    private String educationKey(Education education) {
        return key(education.getInstitution(), education.getDegree(), education.getFieldOfStudy(), date(education.getStartDate()), date(education.getEndDate()));
    }

    private String key(String... values) {
        List<String> parts = new ArrayList<>();
        for (String value : values) {
            parts.add(value == null ? "" : value.toLowerCase(Locale.ROOT));
        }
        return String.join("|", parts);
    }

    private String date(Object date) {
        return date == null ? "" : date.toString();
    }

    private String clean(String value) {
        return StringUtils.isEmpty(value) ? null : value.trim();
    }

    private record FieldCandidate(String value, CandidateProfile profile, int sourceIndex) {
    }
}

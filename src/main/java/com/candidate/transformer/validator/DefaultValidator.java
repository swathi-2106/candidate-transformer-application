package com.candidate.transformer.validator;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.Skill;
import com.candidate.transformer.model.ValidationResult;
import com.candidate.transformer.util.StringUtils;
import com.candidate.transformer.util.ValidationUtils;
import org.springframework.stereotype.Component;

/**
 * Validates required canonical profile fields and common data quality issues.
 */
@Component
public class DefaultValidator implements Validator {

    @Override
    public ValidationResult validate(CandidateProfile profile) {
        ValidationResult result = new ValidationResult();
        if (profile == null) {
            result.addError("Candidate profile is missing.");
            return result;
        }

        if (StringUtils.isEmpty(profile.getId())) {
            result.addError("Candidate ID is missing.");
        }
        if (StringUtils.isEmpty(profile.getName())) {
            result.addError("Candidate name is missing.");
        }
        if (!ValidationUtils.isValidEmail(profile.getEmail())) {
            result.addError("Candidate email is missing or invalid.");
        }
        if (!StringUtils.isEmpty(profile.getPhone()) && !ValidationUtils.isValidPhone(profile.getPhone())) {
            result.addWarning("Candidate phone format is unusual: " + profile.getPhone());
        }
        if (profile.getSkills() == null || profile.getSkills().isEmpty()) {
            result.addWarning("Candidate has no skills.");
        } else {
            long unnamedSkills = profile.getSkills().stream()
                    .filter(skill -> skill == null || StringUtils.isEmpty(skill.getName()))
                    .count();
            if (unnamedSkills > 0) {
                result.addWarning("Candidate has " + unnamedSkills + " skill entry/entries without a name.");
            }
        }
        if (profile.getSkills() != null) {
            for (Skill skill : profile.getSkills()) {
                if (skill != null && skill.getYearsOfExperience() != null && skill.getYearsOfExperience() < 0) {
                    result.addWarning("Skill has negative years of experience: " + skill.getName());
                }
            }
        }
        if (profile.getConfidence() != null
                && (profile.getConfidence().getScore() < 0.0 || profile.getConfidence().getScore() > 1.0)) {
            result.addWarning("Candidate confidence score is outside the 0..1 range.");
        }
        return result;
    }
}

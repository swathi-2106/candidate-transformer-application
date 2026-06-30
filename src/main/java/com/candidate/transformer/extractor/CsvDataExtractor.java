package com.candidate.transformer.extractor;

import com.candidate.transformer.exception.ValidationException;
import com.candidate.transformer.model.*;
import com.candidate.transformer.util.StringUtils;
import com.candidate.transformer.util.ValidationUtils;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Extractor responsible for parsing Recruiter CSV raw contents, validating fields,
 * and mapping properties into the canonical CandidateProfile model.
 */
@Component
public class CsvDataExtractor {

    private static final Logger logger = LoggerFactory.getLogger(CsvDataExtractor.class);

    // Synonym maps for flexible header mapping
    private static final List<String> SYNONYMS_NAME = List.of("name", "full name", "fullname", "candidate name", "candidate_name", "username", "user name");
    private static final List<String> SYNONYMS_EMAIL = List.of("email", "email address", "emailaddress", "email_address", "mail");
    private static final List<String> SYNONYMS_PHONE = List.of("phone", "phone number", "phonenumber", "phone_number", "telephone", "tel", "contact", "mobile");
    private static final List<String> SYNONYMS_LOCATION = List.of("location", "city", "address", "residence", "location_details");
    private static final List<String> SYNONYMS_SKILLS = List.of("skills", "technical skills", "key skills", "skills list", "skills_list", "technologies");
    private static final List<String> SYNONYMS_EXP = List.of("experience", "experience years", "years of experience", "work experience", "exp", "yoe", "years_of_experience");
    private static final List<String> SYNONYMS_LINKEDIN = List.of("linkedin", "linkedin url", "linkedin_url");
    private static final List<String> SYNONYMS_GITHUB = List.of("github", "github url", "github_url");
    private static final List<String> SYNONYMS_PORTFOLIO = List.of("portfolio", "portfolio url", "portfolio_url", "website");
    private static final List<String> SYNONYMS_EDU = List.of("education", "degree", "qualification", "edu");

    /**
     * Extracts CandidateProfile from raw CSV CandidateSource.
     *
     * @param source parsed candidate source
     * @return candidate profile canonical model
     * @throws Exception if validation or parsing fails
     */
    public CandidateProfile extract(CandidateSource source) throws Exception {
        logger.info("Extracting candidate profile from CSV source: {}", source.getSourceName());

        String rawContent = source.getRawContent();
        if (StringUtils.isEmpty(rawContent)) {
            throw new ValidationException("CSV content is empty for source: " + source.getSourceName());
        }

        List<String[]> rows;
        try (CSVReader csvReader = new CSVReader(new StringReader(rawContent))) {
            rows = csvReader.readAll();
        } catch (Exception e) {
            throw new ValidationException("Failed to parse malformed CSV: " + source.getSourceName(), e);
        }

        if (rows.isEmpty()) {
            throw new ValidationException("CSV does not contain any headers or data rows.");
        }

        String[] headers = rows.get(0);
        if (headers == null || headers.length == 0) {
            throw new ValidationException("CSV headers row is empty.");
        }

        // Map headers to column indices
        int idxName = getHeaderIndex(headers, SYNONYMS_NAME);
        int idxEmail = getHeaderIndex(headers, SYNONYMS_EMAIL);
        int idxPhone = getHeaderIndex(headers, SYNONYMS_PHONE);
        int idxLocation = getHeaderIndex(headers, SYNONYMS_LOCATION);
        int idxSkills = getHeaderIndex(headers, SYNONYMS_SKILLS);
        int idxExp = getHeaderIndex(headers, SYNONYMS_EXP);
        int idxLinkedin = getHeaderIndex(headers, SYNONYMS_LINKEDIN);
        int idxGithub = getHeaderIndex(headers, SYNONYMS_GITHUB);
        int idxPortfolio = getHeaderIndex(headers, SYNONYMS_PORTFOLIO);
        int idxEdu = getHeaderIndex(headers, SYNONYMS_EDU);

        // Ensure we find at least one data row
        String[] dataRow = null;
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row != null && row.length > 0 && !isRowEmpty(row)) {
                dataRow = row;
                break; // Process the first valid candidate row
            }
        }

        if (dataRow == null) {
            throw new ValidationException("CSV does not contain any valid candidate data rows.");
        }

        // 1. Mandatory Field checks
        String rawName = getFieldValue(dataRow, idxName);
        String rawEmail = getFieldValue(dataRow, idxEmail);

        if (StringUtils.isEmpty(rawName)) {
            throw new ValidationException("Validation failed: Mandatory field 'Name' is missing in source " + source.getSourceName());
        }
        if (StringUtils.isEmpty(rawEmail)) {
            throw new ValidationException("Validation failed: Mandatory field 'Email' is missing in source " + source.getSourceName());
        }

        // 2. Format Validations
        if (!ValidationUtils.isValidEmail(rawEmail)) {
            throw new ValidationException("Validation failed: Email address '" + rawEmail + "' is malformed in source " + source.getSourceName());
        }

        String rawPhone = getFieldValue(dataRow, idxPhone);
        if (!StringUtils.isEmpty(rawPhone) && !ValidationUtils.isValidPhone(rawPhone)) {
            logger.warn("Validation Warning: Phone number '{}' has an anomalous format in source {}", rawPhone, source.getSourceName());
        }

        // 3. Build Canonical CandidateProfile
        CandidateProfile profile = new CandidateProfile();
        profile.setName(rawName.trim());
        profile.setEmail(rawEmail.trim().toLowerCase());
        profile.setPhone(StringUtils.isEmpty(rawPhone) ? null : rawPhone.trim());

        // Mapping Location
        String rawLoc = getFieldValue(dataRow, idxLocation);
        if (!StringUtils.isEmpty(rawLoc)) {
            Location loc = new Location();
            loc.setFormattedAddress(rawLoc.trim());
            // Simplistic mapping: assign raw location to city as default fallback
            loc.setCity(rawLoc.trim());
            profile.setLocation(loc);
        }

        // Mapping Links
        Links links = new Links();
        String rawLinkedin = getFieldValue(dataRow, idxLinkedin);
        String rawGithub = getFieldValue(dataRow, idxGithub);
        String rawPortfolio = getFieldValue(dataRow, idxPortfolio);
        if (!StringUtils.isEmpty(rawLinkedin)) links.setLinkedin(rawLinkedin.trim());
        if (!StringUtils.isEmpty(rawGithub)) links.setGithub(rawGithub.trim());
        if (!StringUtils.isEmpty(rawPortfolio)) links.setPortfolio(rawPortfolio.trim());
        profile.setLinks(links);

        // Mapping Skills
        String rawSkills = getFieldValue(dataRow, idxSkills);
        if (!StringUtils.isEmpty(rawSkills)) {
            String[] skillTokens = rawSkills.split("[;,]");
            for (String token : skillTokens) {
                String skillName = token.trim();
                if (!skillName.isEmpty()) {
                    Skill skill = new Skill();
                    skill.setName(skillName);
                    
                    // Assign Provenance and Confidence metadata
                    Provenance prov = new Provenance(source.getSourceId(), source.getReceivedAt(), "CSV Extractor", skillName);
                    skill.setProvenance(prov);
                    
                    Confidence conf = new Confidence(0.85, List.of("Extracted from recruiter spreadsheet skills tag"));
                    skill.setConfidence(conf);
                    
                    profile.getSkills().add(skill);
                }
            }
        }

        // Mapping Experience (overall years of experience)
        String rawExp = getFieldValue(dataRow, idxExp);
        if (!StringUtils.isEmpty(rawExp)) {
            try {
                // Extract integer if numeric
                String sanitizedExp = rawExp.replaceAll("[^0-9]", "");
                int yoe = Integer.parseInt(sanitizedExp);
                
                Experience exp = new Experience();
                exp.setCompany("Various / Historic");
                exp.setRole("Professional Experience");
                exp.setDescription(rawExp.trim() + " YOE stated in Recruiter CSV");
                exp.setStartDate(LocalDate.now().minusYears(yoe));
                exp.setEndDate(LocalDate.now());
                
                Provenance prov = new Provenance(source.getSourceId(), source.getReceivedAt(), "CSV Extractor", rawExp);
                exp.setProvenance(prov);
                
                profile.getExperiences().add(exp);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse numeric experience value: '{}'", rawExp);
            }
        }

        // Mapping Education
        String rawEdu = getFieldValue(dataRow, idxEdu);
        if (!StringUtils.isEmpty(rawEdu)) {
            Education edu = new Education();
            edu.setDegree(rawEdu.trim());
            edu.setInstitution("Unknown Institution");
            
            Provenance prov = new Provenance(source.getSourceId(), source.getReceivedAt(), "CSV Extractor", rawEdu);
            edu.setProvenance(prov);
            
            profile.getEducations().add(edu);
        }

        // Set global record Provenance & Confidence metadata
        Provenance globalProv = new Provenance(source.getSourceId(), source.getReceivedAt(), "CSV Extractor", Arrays.toString(dataRow));
        profile.setProvenance(globalProv);

        Confidence globalConf = new Confidence(0.9, List.of("Direct structural key values matched"));
        profile.setConfidence(globalConf);

        return profile;
    }

    private int getHeaderIndex(String[] headers, List<String> synonyms) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i] == null) continue;
            String h = headers[i].trim().toLowerCase();
            for (String syn : synonyms) {
                if (h.equals(syn)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String getFieldValue(String[] row, int index) {
        if (index >= 0 && index < row.length) {
            return row[index];
        }
        return null;
    }

    private boolean isRowEmpty(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}

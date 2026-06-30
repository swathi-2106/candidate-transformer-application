package com.candidate.transformer.extractor;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.CandidateSource;
import org.springframework.stereotype.Component;

/**
 * Extracts canonical candidate fields from resume text parsed out of binary documents.
 */
@Component
public class ResumeDataExtractor {

    private final TxtDataExtractor textDataExtractor;

    public ResumeDataExtractor(TxtDataExtractor textDataExtractor) {
        this.textDataExtractor = textDataExtractor;
    }

    public CandidateProfile extract(CandidateSource source) throws Exception {
        String method = "DOCX".equalsIgnoreCase(source.getSourceType())
                ? "Apache POI Resume Extractor"
                : "Apache PDFBox Resume Extractor";
        return textDataExtractor.extractResumeText(source, method);
    }
}

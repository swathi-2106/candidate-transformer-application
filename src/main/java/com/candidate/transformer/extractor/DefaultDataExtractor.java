package com.candidate.transformer.extractor;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.CandidateSource;
import org.springframework.stereotype.Component;

/**
 * Implementation of DataExtractor routing parsing to format-specific handlers.
 */
@Component
public class DefaultDataExtractor implements DataExtractor {

    private final CsvDataExtractor csvDataExtractor;
    private final TxtDataExtractor txtDataExtractor;
    private final ResumeDataExtractor resumeDataExtractor;

    public DefaultDataExtractor(CsvDataExtractor csvDataExtractor, TxtDataExtractor txtDataExtractor,
                                ResumeDataExtractor resumeDataExtractor) {
        this.csvDataExtractor = csvDataExtractor;
        this.txtDataExtractor = txtDataExtractor;
        this.resumeDataExtractor = resumeDataExtractor;
    }

    @Override
    public CandidateProfile extract(CandidateSource source) throws Exception {
        if ("CSV".equalsIgnoreCase(source.getSourceType())) {
            return csvDataExtractor.extract(source);
        } else if ("TXT".equalsIgnoreCase(source.getSourceType())) {
            return txtDataExtractor.extract(source);
        } else if ("PDF".equalsIgnoreCase(source.getSourceType()) || "DOCX".equalsIgnoreCase(source.getSourceType())) {
            return resumeDataExtractor.extract(source);
        }

        String sourceType = source == null ? "UNKNOWN" : source.getSourceType();
        throw new IllegalArgumentException("Unsupported candidate source type for extraction: " + sourceType);
    }
}

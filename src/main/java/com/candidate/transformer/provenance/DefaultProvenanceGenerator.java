package com.candidate.transformer.provenance;

import com.candidate.transformer.model.Provenance;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Default provenance implementation for field extraction and merge events.
 */
@Component
public class DefaultProvenanceGenerator implements ProvenanceGenerator {

    @Override
    public Provenance generateProvenance(String sourceId, String extractionMethod, String originalValue) {
        return new Provenance(
                defaultValue(sourceId, "unknown-source"),
                LocalDateTime.now(),
                defaultValue(extractionMethod, "Unknown Extraction Method"),
                originalValue == null ? "" : originalValue.trim());
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}

package com.candidate.transformer.model;

import java.time.LocalDateTime;

/**
 * Tracks the data source history, lineage, and extraction context for candidate fields.
 */
public class Provenance {

    private String sourceId;
    private LocalDateTime extractedAt;
    private String extractionMethod;
    private String originalValue;

    public Provenance() {
    }

    public Provenance(String sourceId, LocalDateTime extractedAt, String extractionMethod, String originalValue) {
        this.sourceId = sourceId;
        this.extractedAt = extractedAt;
        this.extractionMethod = extractionMethod;
        this.originalValue = originalValue;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public LocalDateTime getExtractedAt() {
        return extractedAt;
    }

    public void setExtractedAt(LocalDateTime extractedAt) {
        this.extractedAt = extractedAt;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    @Override
    public String toString() {
        return "Provenance{" +
                "sourceId='" + sourceId + '\'' +
                ", extractedAt=" + extractedAt +
                ", extractionMethod='" + extractionMethod + '\'' +
                ", originalValue='" + originalValue + '\'' +
                '}';
    }
}

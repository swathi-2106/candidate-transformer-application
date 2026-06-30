package com.candidate.transformer.model;

import java.time.LocalDateTime;

/**
 * Model representing metadata and raw content of a candidate source record.
 */
public class CandidateSource {

    private String sourceId;
    private String sourceName;
    private String sourceType; // e.g. CV, LinkedIn, Job Portal
    private String rawContent;
    private LocalDateTime receivedAt;

    public CandidateSource() {
    }

    public CandidateSource(String sourceId, String sourceName, String sourceType, String rawContent, LocalDateTime receivedAt) {
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.sourceType = sourceType;
        this.rawContent = rawContent;
        this.receivedAt = receivedAt;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString() {
        return "CandidateSource{" +
                "sourceId='" + sourceId + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", rawContent='" + (rawContent != null && rawContent.length() > 50 ? rawContent.substring(0, 50) + "..." : rawContent) + '\'' +
                ", receivedAt=" + receivedAt +
                '}';
    }
}

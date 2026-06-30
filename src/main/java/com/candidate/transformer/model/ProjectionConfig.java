package com.candidate.transformer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration containing projection rules, including field mappings and destination formats.
 */
public class ProjectionConfig {

    private List<ProjectionField> fields = new ArrayList<>();
    private String outputFormat; // e.g. JSON, XML, CSV
    private boolean includeProvenance;
    private boolean includeConfidence;
    private String missingValuePolicy = "NULL"; // NULL, OMIT, DEFAULT, ERROR
    private boolean schemaValidation = true;

    public ProjectionConfig() {
    }

    public ProjectionConfig(List<ProjectionField> fields, String outputFormat) {
        this.fields = fields != null ? fields : new ArrayList<>();
        this.outputFormat = outputFormat;
    }

    public List<ProjectionField> getFields() {
        return fields;
    }

    public void setFields(List<ProjectionField> fields) {
        this.fields = fields;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public boolean isIncludeProvenance() {
        return includeProvenance;
    }

    public void setIncludeProvenance(boolean includeProvenance) {
        this.includeProvenance = includeProvenance;
    }

    public boolean isIncludeConfidence() {
        return includeConfidence;
    }

    public void setIncludeConfidence(boolean includeConfidence) {
        this.includeConfidence = includeConfidence;
    }

    public String getMissingValuePolicy() {
        return missingValuePolicy;
    }

    public void setMissingValuePolicy(String missingValuePolicy) {
        this.missingValuePolicy = missingValuePolicy;
    }

    public boolean isSchemaValidation() {
        return schemaValidation;
    }

    public void setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

    @Override
    public String toString() {
        return "ProjectionConfig{" +
                "fields=" + fields +
                ", outputFormat='" + outputFormat + '\'' +
                ", includeProvenance=" + includeProvenance +
                ", includeConfidence=" + includeConfidence +
                ", missingValuePolicy='" + missingValuePolicy + '\'' +
                ", schemaValidation=" + schemaValidation +
                '}';
    }
}

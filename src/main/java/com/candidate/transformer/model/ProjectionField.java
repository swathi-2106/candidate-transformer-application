package com.candidate.transformer.model;

/**
 * Configuration mapping configuration for a single field in candidate profile projection.
 */
public class ProjectionField {

    private String fieldName;
    private boolean include = true;
    private String renameTo;
    private String sourcePath;
    private String normalization;
    private String missingValuePolicy;
    private Object defaultValue;
    private boolean includeProvenance;
    private boolean includeConfidence;

    public ProjectionField() {
    }

    public ProjectionField(String fieldName, boolean include, String renameTo) {
        this.fieldName = fieldName;
        this.include = include;
        this.renameTo = renameTo;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }

    public String getRenameTo() {
        return renameTo;
    }

    public void setRenameTo(String renameTo) {
        this.renameTo = renameTo;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getNormalization() {
        return normalization;
    }

    public void setNormalization(String normalization) {
        this.normalization = normalization;
    }

    public String getMissingValuePolicy() {
        return missingValuePolicy;
    }

    public void setMissingValuePolicy(String missingValuePolicy) {
        this.missingValuePolicy = missingValuePolicy;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
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

    @Override
    public String toString() {
        return "ProjectionField{" +
                "fieldName='" + fieldName + '\'' +
                ", include=" + include +
                ", renameTo='" + renameTo + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                ", normalization='" + normalization + '\'' +
                ", missingValuePolicy='" + missingValuePolicy + '\'' +
                ", defaultValue=" + defaultValue +
                ", includeProvenance=" + includeProvenance +
                ", includeConfidence=" + includeConfidence +
                '}';
    }
}

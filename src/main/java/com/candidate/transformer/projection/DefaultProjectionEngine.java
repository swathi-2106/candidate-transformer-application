package com.candidate.transformer.projection;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.ProjectionConfig;
import com.candidate.transformer.model.ProjectionField;
import com.candidate.transformer.util.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Projects canonical profiles into runtime-configured JSON-ready maps.
 */
@Component
public class DefaultProjectionEngine implements ProjectionEngine {

    private static final String POLICY_NULL = "NULL";
    private static final String POLICY_OMIT = "OMIT";
    private static final String POLICY_DEFAULT = "DEFAULT";
    private static final String POLICY_ERROR = "ERROR";

    @Override
    public Object project(CandidateProfile profile, ProjectionConfig config) {
        if (profile == null) {
            throw new IllegalArgumentException("Cannot project a null candidate profile.");
        }

        ProjectionConfig effectiveConfig = config == null ? new ProjectionConfig() : config;
        validateConfig(effectiveConfig);

        Map<String, Object> projected = new LinkedHashMap<>();
        List<ProjectionField> fields = effectiveConfig.getFields() == null ? new ArrayList<>() : effectiveConfig.getFields();
        if (fields.isEmpty()) {
            fields = defaultFields();
        }

        for (ProjectionField field : fields) {
            if (field == null || !field.isInclude()) {
                continue;
            }

            String sourcePath = firstNonEmpty(field.getSourcePath(), field.getFieldName());
            String outputName = firstNonEmpty(field.getRenameTo(), field.getFieldName());
            Object value = readPath(profile, sourcePath);
            value = normalizeValue(value, field.getNormalization());

            if (isMissing(value)) {
                String policy = policy(field.getMissingValuePolicy(), effectiveConfig.getMissingValuePolicy());
                if (POLICY_OMIT.equals(policy)) {
                    continue;
                }
                if (POLICY_DEFAULT.equals(policy)) {
                    value = field.getDefaultValue();
                } else if (POLICY_ERROR.equals(policy)) {
                    throw new IllegalArgumentException("Required projection field '" + sourcePath + "' is missing.");
                } else {
                    value = null;
                }
            }

            projected.put(outputName, value);

            if (field.isIncludeProvenance()) {
                projected.put(outputName + "Provenance", profile.getProvenance());
            }
            if (field.isIncludeConfidence()) {
                projected.put(outputName + "Confidence", profile.getConfidence());
            }
        }

        if (effectiveConfig.isIncludeProvenance()) {
            projected.put("provenance", profile.getProvenance());
        }
        if (effectiveConfig.isIncludeConfidence()) {
            projected.put("confidence", profile.getConfidence());
        }
        return projected;
    }

    private void validateConfig(ProjectionConfig config) {
        if (!config.isSchemaValidation()) {
            return;
        }
        if (config.getOutputFormat() != null && !"JSON".equalsIgnoreCase(config.getOutputFormat())) {
            throw new IllegalArgumentException("Unsupported outputFormat '" + config.getOutputFormat() + "'. Only JSON is supported.");
        }
        String globalPolicy = policy(null, config.getMissingValuePolicy());
        if (!isValidPolicy(globalPolicy)) {
            throw new IllegalArgumentException("Unsupported missingValuePolicy '" + config.getMissingValuePolicy() + "'.");
        }
        if (config.getFields() == null) {
            return;
        }
        for (ProjectionField field : config.getFields()) {
            if (field == null || !field.isInclude()) {
                continue;
            }
            if (StringUtils.isEmpty(field.getFieldName()) && StringUtils.isEmpty(field.getSourcePath())) {
                throw new IllegalArgumentException("Projection field requires fieldName or sourcePath.");
            }
            String fieldPolicy = policy(field.getMissingValuePolicy(), config.getMissingValuePolicy());
            if (!isValidPolicy(fieldPolicy)) {
                throw new IllegalArgumentException("Unsupported missingValuePolicy for field '" + field.getFieldName() + "'.");
            }
        }
    }

    private Object readPath(Object root, String path) {
        if (root == null || StringUtils.isEmpty(path)) {
            return null;
        }
        Object current = root;
        for (String token : path.split("\\.")) {
            current = readProperty(current, token);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private Object readProperty(Object target, String propertyName) {
        if (target == null || StringUtils.isEmpty(propertyName)) {
            return null;
        }
        if (target instanceof Map<?, ?> map) {
            return map.get(propertyName);
        }
        String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        try {
            Method getter = target.getClass().getMethod(getterName);
            return getter.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            String booleanGetterName = "is" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            try {
                Method getter = target.getClass().getMethod(booleanGetterName);
                return getter.invoke(target);
            } catch (ReflectiveOperationException nestedIgnored) {
                return null;
            }
        }
    }

    private Object normalizeValue(Object value, String normalization) {
        if (value == null || StringUtils.isEmpty(normalization)) {
            return value;
        }
        if (value instanceof String text) {
            return normalizeString(text, normalization);
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(item -> normalizeValue(item, normalization))
                    .toList();
        }
        return value;
    }

    private Object normalizeString(String value, String normalization) {
        String cleaned = value.trim().replaceAll("\\s+", " ");
        return switch (normalization.trim().toUpperCase(Locale.ROOT)) {
            case "LOWERCASE" -> cleaned.toLowerCase(Locale.ROOT);
            case "UPPERCASE" -> cleaned.toUpperCase(Locale.ROOT);
            case "TRIM", "WHITESPACE" -> cleaned;
            case "DATE_ISO" -> normalizeIsoDate(cleaned);
            default -> cleaned;
        };
    }

    private String normalizeIsoDate(String value) {
        try {
            return LocalDate.parse(value).toString();
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(value).toLocalDate().toString();
            } catch (Exception nestedIgnored) {
                return value;
            }
        }
    }

    private boolean isMissing(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String text) {
            return text.trim().isEmpty();
        }
        if (value instanceof List<?> list) {
            return list.isEmpty();
        }
        if (value instanceof Map<?, ?> map) {
            return map.isEmpty();
        }
        return false;
    }

    private String policy(String fieldPolicy, String globalPolicy) {
        String selected = firstNonEmpty(fieldPolicy, globalPolicy);
        return StringUtils.isEmpty(selected) ? POLICY_NULL : selected.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isValidPolicy(String policy) {
        return POLICY_NULL.equals(policy) || POLICY_OMIT.equals(policy) || POLICY_DEFAULT.equals(policy) || POLICY_ERROR.equals(policy);
    }

    private String firstNonEmpty(String first, String second) {
        return !StringUtils.isEmpty(first) ? first : second;
    }

    private List<ProjectionField> defaultFields() {
        return List.of(
                new ProjectionField("id", true, "id"),
                new ProjectionField("name", true, "name"),
                new ProjectionField("email", true, "email"),
                new ProjectionField("phone", true, "phone"),
                new ProjectionField("location", true, "location"),
                new ProjectionField("links", true, "links"),
                new ProjectionField("skills", true, "skills"),
                new ProjectionField("experiences", true, "experiences"),
                new ProjectionField("educations", true, "educations")
        );
    }
}

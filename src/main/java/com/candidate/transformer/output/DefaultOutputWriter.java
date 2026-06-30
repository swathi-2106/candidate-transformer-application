package com.candidate.transformer.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

/**
 * JSON output writer for projected candidate records.
 */
@Component
public class DefaultOutputWriter implements OutputWriter {

    private final ObjectMapper objectMapper;

    public DefaultOutputWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void write(Object data, OutputStream outputStream) throws Exception {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, data);
    }

    @Override
    public boolean supports(String format) {
        return format == null || "JSON".equalsIgnoreCase(format);
    }
}

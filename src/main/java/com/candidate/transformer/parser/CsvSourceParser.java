package com.candidate.transformer.parser;

import com.candidate.transformer.model.CandidateSource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Concrete parser for Recruiter CSV spreadsheets containing candidate sources.
 */
@Component
public class CsvSourceParser implements SourceParser {

    @Override
    public CandidateSource parse(InputStream inputStream, String sourceName) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        String content = new String(bytes, StandardCharsets.UTF_8);

        CandidateSource source = new CandidateSource();
        source.setSourceId("csv-" + System.currentTimeMillis());
        source.setSourceName(sourceName);
        source.setSourceType("CSV");
        source.setRawContent(content);
        source.setReceivedAt(LocalDateTime.now());
        return source;
    }

    @Override
    public boolean supports(String fileExtension) {
        return "csv".equalsIgnoreCase(fileExtension);
    }
}

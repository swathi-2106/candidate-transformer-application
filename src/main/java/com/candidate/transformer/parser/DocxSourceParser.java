package com.candidate.transformer.parser;

import com.candidate.transformer.model.CandidateSource;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * Concrete parser for Resume Word Documents (.docx) utilizing Apache POI.
 */
@Component
public class DocxSourceParser implements SourceParser {

    @Override
    public CandidateSource parse(InputStream inputStream, String sourceName) throws Exception {
        String content;
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            content = extractor.getText();
        }

        CandidateSource source = new CandidateSource();
        source.setSourceId("docx-" + System.currentTimeMillis());
        source.setSourceName(sourceName);
        source.setSourceType("DOCX");
        source.setRawContent(content);
        source.setReceivedAt(LocalDateTime.now());
        return source;
    }

    @Override
    public boolean supports(String fileExtension) {
        return "docx".equalsIgnoreCase(fileExtension);
    }
}

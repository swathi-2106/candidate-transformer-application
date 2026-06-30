package com.candidate.transformer.parser;

import com.candidate.transformer.model.CandidateSource;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * Concrete parser for Resume PDFs utilizing Apache PDFBox.
 */
@Component
public class PdfSourceParser implements SourceParser {

    @Override
    public CandidateSource parse(InputStream inputStream, String sourceName) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        String content;
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            content = textStripper.getText(document);
        }

        CandidateSource source = new CandidateSource();
        source.setSourceId("pdf-" + System.currentTimeMillis());
        source.setSourceName(sourceName);
        source.setSourceType("PDF");
        source.setRawContent(content);
        source.setReceivedAt(LocalDateTime.now());
        return source;
    }

    @Override
    public boolean supports(String fileExtension) {
        return "pdf".equalsIgnoreCase(fileExtension);
    }
}

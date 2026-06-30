package com.candidate.transformer.extractor;

import com.candidate.transformer.model.CandidateProfile;
import com.candidate.transformer.model.CandidateSource;
import com.candidate.transformer.parser.DocxSourceParser;
import com.candidate.transformer.parser.PdfSourceParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ResumeDataExtractorTest {

    private ResumeDataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new ResumeDataExtractor(new TxtDataExtractor());
    }

    @Test
    void testPdfParserAndExtractor_ValidResume() throws Exception {
        byte[] pdfBytes = createPdf("""
                John Doe
                Email: john.doe@example.com
                Phone: +1 123-456-7890
                Location: New York, NY, USA
                Skills: Java, Spring Boot, AWS
                Experience: 6 years
                Education: BS in Computer Science
                LinkedIn: linkedin.com/in/johndoe
                """);

        CandidateSource source = new PdfSourceParser().parse(new ByteArrayInputStream(pdfBytes), "resume.pdf");
        CandidateProfile profile = extractor.extract(source);

        assertEquals("PDF", source.getSourceType());
        assertTrue(source.getRawContent().contains("John Doe"));
        assertEquals("John Doe", profile.getName());
        assertEquals("john.doe@example.com", profile.getEmail());
        assertEquals("+1 123-456-7890", profile.getPhone());
        assertEquals("New York, NY, USA", profile.getLocation().getFormattedAddress());
        assertEquals("linkedin.com/in/johndoe", profile.getLinks().getLinkedin());
        assertTrue(profile.getSkills().stream().anyMatch(skill -> "Java".equals(skill.getName())));
        assertTrue(profile.getSkills().stream().anyMatch(skill -> "Spring Boot".equals(skill.getName())));
        assertEquals(1, profile.getExperiences().size());
        assertTrue(profile.getExperiences().get(0).getDescription().contains("6+ YOE stated in resume text"));
        assertEquals(1, profile.getEducations().size());
        assertEquals("Apache PDFBox Resume Extractor", profile.getProvenance().getExtractionMethod());
    }

    @Test
    void testDocxParserAndExtractor_ValidResume() throws Exception {
        byte[] docxBytes = createDocx("""
                Jane Smith
                Email: jane.smith@example.org
                Phone: +31-6-12345678
                Skills: Python, SQL, Docker
                Experience: 4 yoe
                GitHub: github.com/janesmith
                Portfolio: janesmith.dev
                """);

        CandidateSource source = new DocxSourceParser().parse(new ByteArrayInputStream(docxBytes), "resume.docx");
        CandidateProfile profile = extractor.extract(source);

        assertEquals("DOCX", source.getSourceType());
        assertTrue(source.getRawContent().contains("Jane Smith"));
        assertEquals("Jane Smith", profile.getName());
        assertEquals("jane.smith@example.org", profile.getEmail());
        assertEquals("+31-6-12345678", profile.getPhone());
        assertEquals("github.com/janesmith", profile.getLinks().getGithub());
        assertEquals("janesmith.dev", profile.getLinks().getPortfolio());
        assertTrue(profile.getSkills().stream().anyMatch(skill -> "Python".equals(skill.getName())));
        assertTrue(profile.getSkills().stream().anyMatch(skill -> "Docker".equals(skill.getName())));
        assertEquals(1, profile.getExperiences().size());
        assertEquals("Apache POI Resume Extractor", profile.getProvenance().getExtractionMethod());
    }

    @Test
    void testExtractor_MissingResumeEmail() {
        CandidateSource source = new CandidateSource(
                "pdf-1",
                "resume.pdf",
                "PDF",
                "John Doe\nSkills: Java, AWS",
                LocalDateTime.now());

        assertThrows(Exception.class, () -> extractor.extract(source));
    }

    private byte[] createPdf(String text) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 730);
                for (String line : text.split("\\R")) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -16);
                }
                contentStream.endText();
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createDocx(String text) throws Exception {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XWPFRun run = document.createParagraph().createRun();
            String[] lines = text.split("\\R");
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) {
                    run.addBreak(BreakType.TEXT_WRAPPING);
                }
                run.setText(lines[i]);
            }
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}

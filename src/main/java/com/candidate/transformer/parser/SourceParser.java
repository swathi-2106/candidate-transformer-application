package com.candidate.transformer.parser;

import com.candidate.transformer.model.CandidateSource;
import java.io.InputStream;

/**
 * Interface defining operations to parse raw input files (like PDFs, Excel, Word, CSV, JSON)
 * into canonical CandidateSource wrapper representations.
 */
public interface SourceParser {

    /**
     * Parses the incoming stream of raw content into a CandidateSource container.
     *
     * @param inputStream the stream containing raw file data
     * @param sourceName  the name or identifier of the source file
     * @return candidate source wrapper containing metadata and content
     * @throws Exception if parsing fails
     */
    CandidateSource parse(InputStream inputStream, String sourceName) throws Exception;

    /**
     * Determines whether this parser supports the given file format.
     *
     * @param fileExtension the file extension (e.g. "pdf", "csv", "json")
     * @return true if supported, false otherwise
     */
    boolean supports(String fileExtension);
}

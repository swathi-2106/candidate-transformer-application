package com.candidate.transformer.output;

import java.io.OutputStream;

/**
 * Interface defining operations to format and write output datasets to various destination streams.
 */
public interface OutputWriter {

    /**
     * Serializes and writes candidate details to the output stream.
     *
     * @param data         the structured candidate record or map to output
     * @param outputStream target output destination stream
     * @throws Exception if writing fails
     */
    void write(Object data, OutputStream outputStream) throws Exception;

    /**
     * Checks if this writer supports the specified target output format.
     *
     * @param format target format (e.g. "json", "csv")
     * @return true if supported, false otherwise
     */
    boolean supports(String format);
}

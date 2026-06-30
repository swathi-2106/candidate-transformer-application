package com.candidate.transformer.provenance;

import com.candidate.transformer.model.Provenance;

/**
 * Generates provenance records describing how specific field values were extracted or modified.
 */
public interface ProvenanceGenerator {

    /**
     * Generates a provenance descriptor for a data extraction event.
     *
     * @param sourceId         identifier of the original document or data source
     * @param extractionMethod technique used (e.g. PDFBox Regex, Jackson JSON)
     * @param originalValue    original text before normalization or merging
     * @return generated provenance details
     */
    Provenance generateProvenance(String sourceId, String extractionMethod, String originalValue);
}

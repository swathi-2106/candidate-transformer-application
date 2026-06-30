package com.candidate.transformer.service;

import com.candidate.transformer.constants.AppConstants;
import com.candidate.transformer.extractor.DataExtractor;
import com.candidate.transformer.merger.Merger;
import com.candidate.transformer.normalizer.Normalizer;
import com.candidate.transformer.output.OutputWriter;
import com.candidate.transformer.parser.SourceParser;
import com.candidate.transformer.projection.ProjectionEngine;
import com.candidate.transformer.validator.Validator;
import com.candidate.transformer.confidence.ConfidenceCalculator;
import com.candidate.transformer.util.*;
import com.candidate.transformer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Orchestrates the complete pipeline of discovering files, parsing documents,
 * extracting structures, standardizing attributes, merging records, performing validation,
 * assigning scores, applying projections, and serializing outputs.
 */
@Service
public class TransformerService {

    private static final Logger logger = LoggerFactory.getLogger(TransformerService.class);

    private final List<SourceParser> sourceParsers;
    private final DataExtractor dataExtractor;
    private final Normalizer normalizer;
    private final Merger merger;
    private final ProjectionEngine projectionEngine;
    private final Validator validator;
    private final List<OutputWriter> outputWriters;
    private final ConfidenceCalculator confidenceCalculator;
    private final ConfigLoader configLoader;

    public TransformerService(List<SourceParser> sourceParsers,
                              DataExtractor dataExtractor,
                              Normalizer normalizer,
                              Merger merger,
                              ProjectionEngine projectionEngine,
                              Validator validator,
                              List<OutputWriter> outputWriters,
                              ConfidenceCalculator confidenceCalculator,
                              ConfigLoader configLoader) {
        this.sourceParsers = sourceParsers;
        this.dataExtractor = dataExtractor;
        this.normalizer = normalizer;
        this.merger = merger;
        this.projectionEngine = projectionEngine;
        this.validator = validator;
        this.outputWriters = outputWriters;
        this.confidenceCalculator = confidenceCalculator;
        this.configLoader = configLoader;
    }

    /**
     * Executes the candidate processing workflow.
     */
    public void process() {
        Instant startedAt = Instant.now();
        ProcessingStats stats = new ProcessingStats();
        ConsoleLogger.logHeader("Starting Candidate Profile Transformer Pipeline");
        
        int step = 1;

        // 1. Loading configuration
        ConsoleLogger.logStep(step++, "Loading Configuration", "Reading projection-config.json configuration properties.");
        ProjectionConfig projectionConfig = configLoader.loadProjectionConfig("config/projection-config.json");
        ConsoleLogger.logInfo("Loaded Output Format: " + projectionConfig.getOutputFormat());
        ConsoleLogger.logInfo("Configured Fields to Map: " + projectionConfig.getFields().size());

        // 2. Validating Folders
        ConsoleLogger.logStep(step++, "Validating Folders", "Checking directory presence for 'input', 'config', and 'output'.");
        Path inputDir = Paths.get(AppConstants.DIR_INPUT);
        Path outputDir = Paths.get(AppConstants.DIR_OUTPUT);
        ConsoleLogger.logInfo("Input directory path: " + inputDir.toAbsolutePath());
        ConsoleLogger.logInfo("Output directory path: " + outputDir.toAbsolutePath());

        // 3. Discovering Input Files
        ConsoleLogger.logStep(step++, "Discovering Input Files", "Scanning input folder for files.");
        List<Path> files = new ArrayList<>();
        try {
            files = FileUtils.listFilesInDirectory(inputDir);
            stats.discoveredFiles = files.size();
            ConsoleLogger.logInfo("Discovered " + files.size() + " files in input directory.");
            for (Path f : files) {
                ConsoleLogger.logInfo(" -> Found file: " + f.getFileName());
            }
        } catch (IOException e) {
            logger.error("Failed to list files inside input directory", e);
            stats.warnings.add("Failed to list input files: " + e.getMessage());
        }

        if (files.isEmpty()) {
            ConsoleLogger.logWarning("Input directory is empty. Add supported candidate files to continue.");
            stats.elapsed = Duration.between(startedAt, Instant.now());
            logSummary(stats, null, null);
            return;
        }

        // 4. Validating Files
        ConsoleLogger.logStep(step++, "Validating Files", "Analyzing file extensions against supported parsers.");
        List<Path> validFiles = new ArrayList<>();
        for (Path f : files) {
            String extension = FileUtils.getFileExtension(f.getFileName().toString());
            boolean supported = false;
            for (SourceParser parser : sourceParsers) {
                if (parser.supports(extension)) {
                    supported = true;
                    break;
                }
            }
            if (supported) {
                validFiles.add(f);
                stats.supportedFiles++;
                ConsoleLogger.logInfo(" -> File '" + f.getFileName() + "' is supported (Format: " + extension.toUpperCase() + ")");
            } else {
                stats.unsupportedFiles++;
                ConsoleLogger.logWarning(" -> File '" + f.getFileName() + "' is unsupported and will be skipped (Format: " + extension.toUpperCase() + ")");
            }
        }

        // 5. Parsing Sources
        ConsoleLogger.logStep(step++, "Parsing Sources", "Invoking format-specific parser implementations.");
        List<CandidateSource> rawSources = new ArrayList<>();
        for (Path f : validFiles) {
            String extension = FileUtils.getFileExtension(f.getFileName().toString());
            for (SourceParser parser : sourceParsers) {
                if (parser.supports(extension)) {
                    try {
                        byte[] fileContent = Files.readAllBytes(f);
                        CandidateSource parsedSource = parser.parse(new ByteArrayInputStream(fileContent), f.getFileName().toString());
                        rawSources.add(parsedSource);
                        stats.parsedSources++;
                        ConsoleLogger.logInfo(" -> Parsed successfully: " + f.getFileName() + " (Source ID: " + parsedSource.getSourceId() + ")");
                    } catch (Exception e) {
                        stats.parseFailures++;
                        stats.warnings.add("Parse failed for " + f.getFileName() + ": " + e.getMessage());
                        ConsoleLogger.logWarning(" -> Failed to parse '" + f.getFileName() + "': " + e.getMessage());
                        logger.trace("Error occurred while parsing file " + f.getFileName(), e);
                    }
                    break;
                }
            }
        }

        // 6. Extraction & Normalization
        ConsoleLogger.logStep(step++, "Normalization & Extraction", "Standardizing raw parsed metrics into candidate profile components.");
        List<CandidateProfile> extractedProfiles = new ArrayList<>();
        
        for (CandidateSource source : rawSources) {
            try {
                CandidateProfile rawProfile = dataExtractor.extract(source);
                CandidateProfile normalizedProfile = normalizer.normalize(rawProfile);
                extractedProfiles.add(normalizedProfile);
                stats.extractedProfiles++;
                ConsoleLogger.logInfo(" -> Normalized profile extracted from source: " + source.getSourceName());
            } catch (Exception e) {
                stats.extractionFailures++;
                stats.warnings.add("Extraction failed for " + source.getSourceName() + ": " + e.getMessage());
                logger.error("Failed extraction", e);
            }
        }

        // 7. Merging & Conflict Resolution
        ConsoleLogger.logStep(step++, "Merging & Conflict Resolution", "Consolidating duplicate profile components.");
        CandidateRecord mergedRecord = null;
        List<String> conflicts = new ArrayList<>();
        try {
            MergeResult mergeResult = merger.merge(extractedProfiles);
            mergedRecord = mergeResult.getMergedRecord();
            conflicts = mergeResult.getConflicts() != null ? mergeResult.getConflicts() : new ArrayList<>();
            stats.conflicts = conflicts.size();

            ConsoleLogger.logInfo(" -> Merged " + extractedProfiles.size() + " profile(s) into single CandidateRecord.");
            for (String conflict : conflicts) {
                ConsoleLogger.logWarning("    [Conflict Resolved] " + conflict);
            }
        } catch (Exception e) {
            logger.error("Failed merging profiles", e);
        }

        if (mergedRecord == null || mergedRecord.getMergedProfile() == null) {
            ConsoleLogger.logWarning("No candidate profile was generated from merge process.");
            stats.elapsed = Duration.between(startedAt, Instant.now());
            logSummary(stats, null, null);
            return;
        }

        CandidateProfile consolidatedProfile = mergedRecord.getMergedProfile();

        // 8. Provenance & Confidence Calculation
        ConsoleLogger.logStep(step++, "Provenance & Confidence Tracking", "Calculating data lineage maps and overall confidence levels.");
        Confidence confidence = confidenceCalculator.calculateConfidence(consolidatedProfile);
        consolidatedProfile.setConfidence(confidence);

        ConsoleLogger.logInfo(" -> Generated data provenance mapping trace for field entries.");
        ConsoleLogger.logInfo(" -> Profile Confidence Score: " + consolidatedProfile.getConfidence().getScore());

        // 9. Canonical Profile Creation
        ConsoleLogger.logStep(step++, "Canonical Profile Creation", "Reviewing canonical consolidated CandidateProfile data.");
        ConsoleLogger.logInfo("Candidate ID   : " + consolidatedProfile.getId());
        ConsoleLogger.logInfo("Candidate Name : " + consolidatedProfile.getName());
        ConsoleLogger.logInfo("Candidate Email: " + consolidatedProfile.getEmail());
        ConsoleLogger.logInfo("Skills Found   : " + consolidatedProfile.getSkills().size());
        ConsoleLogger.logInfo("Experiences    : " + consolidatedProfile.getExperiences().size());
        ConsoleLogger.logInfo("Educations     : " + consolidatedProfile.getEducations().size());

        // 10. Projection
        ConsoleLogger.logStep(step++, "Projection", "Applying configured field-mapping projections.");
        Object projectedData = projectionEngine.project(consolidatedProfile, projectionConfig);
        int projectedFieldCount = projectedData instanceof Map<?, ?> map ? map.size() : 1;
        ConsoleLogger.logInfo(" -> Generated custom field mapping projection with " + projectedFieldCount + " active fields.");

        // 11. Schema Validation
        ConsoleLogger.logStep(step++, "Schema Validation", "Asserting validation compliance on canonical fields.");
        ValidationResult validationResult = validator.validate(consolidatedProfile);

        ConsoleLogger.logInfo(" -> Structural validation result: " + (validationResult.isValid() ? "PASS" : "FAIL"));
        ConsoleLogger.logInfo(" -> Validation errors: " + validationResult.getErrors().size());
        ConsoleLogger.logInfo(" -> Validation warnings: " + validationResult.getWarnings().size());
        stats.validationErrors = validationResult.getErrors().size();
        stats.validationWarnings = validationResult.getWarnings().size();
        for (String warning : validationResult.getWarnings()) {
            ConsoleLogger.logWarning("    [Validation Warning] " + warning);
        }
        for (String error : validationResult.getErrors()) {
            ConsoleLogger.logWarning("    [Validation Error] " + error);
        }

        // 12. JSON Generation & Output Writing
        ConsoleLogger.logStep(step++, "JSON Generation & Output", "Serializing output records to JSON.");
        Path outFilePath = outputDir.resolve(resolveOutputFileName(consolidatedProfile));
        try {
            OutputWriter writer = resolveWriter(projectionConfig.getOutputFormat());
            try (OutputStream outputStream = Files.newOutputStream(outFilePath)) {
                writer.write(projectedData, outputStream);
            }
            stats.outputGenerated = true;
            stats.outputPath = outFilePath.toAbsolutePath();
            ConsoleLogger.logSuccess("Successfully wrote final projected candidate JSON file to: " + outFilePath.toAbsolutePath());
        } catch (Exception e) {
            stats.warnings.add("Output generation failed: " + e.getMessage());
            logger.error("Failed to write final JSON output", e);
        }

        // 13. Processing Summary
        stats.elapsed = Duration.between(startedAt, Instant.now());
        logSummary(stats, consolidatedProfile, conflicts);
        ConsoleLogger.logSuccess("Candidate Transformation Completed Successfully.");
    }

    private OutputWriter resolveWriter(String outputFormat) {
        return outputWriters.stream()
                .filter(writer -> writer.supports(outputFormat))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No output writer registered for format: " + outputFormat));
    }

    private String resolveOutputFileName(CandidateProfile profile) {
        String id = profile != null && profile.getId() != null ? profile.getId() : "candidate-profile";
        return id.replaceAll("[^A-Za-z0-9._-]", "_") + "_profile.json";
    }

    private void logSummary(ProcessingStats stats, CandidateProfile profile, List<String> conflicts) {
        ConsoleLogger.logHeader("Processing Pipeline Summary");
        ConsoleLogger.logInfo("Files discovered       : " + stats.discoveredFiles);
        ConsoleLogger.logInfo("Supported files        : " + stats.supportedFiles);
        ConsoleLogger.logInfo("Unsupported files      : " + stats.unsupportedFiles);
        ConsoleLogger.logInfo("Parsed sources         : " + stats.parsedSources);
        ConsoleLogger.logInfo("Parse failures         : " + stats.parseFailures);
        ConsoleLogger.logInfo("Extracted profiles     : " + stats.extractedProfiles);
        ConsoleLogger.logInfo("Extraction failures    : " + stats.extractionFailures);
        ConsoleLogger.logInfo("Conflict count         : " + stats.conflicts);
        ConsoleLogger.logInfo("Validation errors      : " + stats.validationErrors);
        ConsoleLogger.logInfo("Validation warnings    : " + stats.validationWarnings);
        if (profile != null) {
            ConsoleLogger.logInfo("Merged profile name    : " + profile.getName());
            ConsoleLogger.logInfo("Overall confidence     : " + (profile.getConfidence() != null ? profile.getConfidence().getScore() : "n/a"));
        }
        if (stats.outputGenerated && stats.outputPath != null) {
            ConsoleLogger.logInfo("Output file generated  : " + stats.outputPath);
        }
        ConsoleLogger.logInfo("Elapsed time           : " + stats.elapsed.toMillis() + " ms");
        if (conflicts != null && !conflicts.isEmpty()) {
            ConsoleLogger.logInfo("Conflict details       : " + conflicts.size() + " resolved");
        }
        if (!stats.warnings.isEmpty()) {
            ConsoleLogger.logInfo("Warnings captured      : " + stats.warnings.size());
        }
    }

    private static class ProcessingStats {
        private int discoveredFiles;
        private int supportedFiles;
        private int unsupportedFiles;
        private int parsedSources;
        private int parseFailures;
        private int extractedProfiles;
        private int extractionFailures;
        private int conflicts;
        private int validationErrors;
        private int validationWarnings;
        private boolean outputGenerated;
        private Path outputPath;
        private Duration elapsed = Duration.ZERO;
        private final List<String> warnings = new ArrayList<>();
    }
}

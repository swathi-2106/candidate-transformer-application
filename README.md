# Candidate Profile Transformer

Spring Boot CLI that transforms candidate source files into a canonical candidate profile and writes a configurable JSON projection.

## Supported Inputs

- Recruiter spreadsheets: `.csv`
- Recruiter notes: `.txt`
- Resumes: `.pdf` via Apache PDFBox
- Resumes: `.docx` via Apache POI

Place source files in `input/`. Invalid or unsupported files are skipped with warnings and included in execution statistics.

## Runtime Configuration

Projection behavior is controlled by `config/projection-config.json`.

Supported projection options include:

- field inclusion and renaming
- source-path remapping, for example `location.city`
- per-field normalization
- missing-value policies: `NULL`, `OMIT`, `DEFAULT`, `ERROR`
- optional provenance and confidence output
- JSON schema/config validation

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

For non-interactive execution:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--batch
```

## Test

```powershell
.\mvnw.cmd test
```

## Output

Projected JSON files are written to `output/` using the generated candidate ID, for example:

```text
output/cand-8eb1b522_profile.json
```

The CLI prints step-by-step progress, warnings, validation results, confidence, conflict counts, output path, and elapsed execution time.

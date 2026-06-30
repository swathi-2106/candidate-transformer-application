# Candidate Profile Transformer

A Spring Boot CLI application that transforms candidate information from multiple structured and unstructured sources into a single canonical candidate profile.

The application ingests candidate data, normalizes inconsistent values, merges duplicate information, tracks provenance and confidence, validates the final profile, and generates a configurable JSON output.

---

## Features

- Recruiter CSV parsing
- Recruiter Notes (.txt) parsing
- Resume PDF extraction using Apache PDFBox
- Resume DOCX extraction using Apache POI
- Canonical candidate profile generation
- Data normalization
- Deterministic merge engine
- Conflict detection and resolution
- Provenance tracking
- Confidence calculation
- Runtime configurable output projection
- Schema validation
- Interactive CLI with execution progress
- JSON output generation

---

## Technology Stack

- Java 21
- Spring Boot
- Maven
- Jackson
- Apache PDFBox
- Apache POI
- OpenCSV
- Apache Commons

---

## Project Structure

```
candidate-profile-transformer
│
├── input/                 # Place candidate source files here
├── config/                # Runtime projection configuration
├── output/                # Generated JSON output
├── src/
├── pom.xml
└── README.md
```

---

## Supported Input Sources

| Source | Type |
|---------|------|
| Recruiter CSV | Structured |
| Recruiter Notes (.txt) | Unstructured |
| Resume PDF | Unstructured |
| Resume DOCX | Unstructured |

The application supports any combination of the above sources.

Examples:

- CSV only
- PDF only
- DOCX only
- TXT only
- CSV + PDF
- CSV + DOCX
- CSV + TXT
- PDF + DOCX
- CSV + PDF + DOCX
- CSV + TXT + PDF
- CSV + TXT + DOCX
- CSV + TXT + PDF + DOCX

Missing or malformed files are skipped gracefully without terminating execution.

---

## Running the Application

Clone the repository:

```bash
git clone <repository-url>
cd candidate-profile-transformer
```

Place your input files inside the `input/` directory.

Place the runtime configuration inside:

```
config/projection-config.json
```

Run the application:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Optional batch mode:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--batch
```

---

## Running Tests

Run all tests:

```powershell
.\mvnw.cmd test
```

---

## Processing Pipeline

The application executes the following deterministic pipeline:

```
Load Configuration
        ↓
Discover Input Files
        ↓
Validate Sources
        ↓
Parse CSV
        ↓
Parse TXT Notes
        ↓
Extract PDF
        ↓
Extract DOCX
        ↓
Normalize Data
        ↓
Merge Candidate Information
        ↓
Resolve Conflicts
        ↓
Generate Provenance
        ↓
Calculate Confidence
        ↓
Build Canonical Profile
        ↓
Apply Projection Configuration
        ↓
Validate Output Schema
        ↓
Generate JSON Output
```

---

## Runtime Configuration

The output structure is controlled using:

```
config/projection-config.json
```

Supported options include:

- Field selection
- Field renaming
- Canonical path remapping
- Per-field normalization
- Confidence toggle
- Provenance toggle
- Missing value policies
- Schema validation

No code changes are required to modify the output schema.

---

## Output

The generated JSON is written to:

```
output/
```

Example:

```
output/cand-8eb1b522_profile.json
```

The CLI displays:

- Processing stages
- Validation messages
- Warnings
- Merge statistics
- Conflict count
- Confidence information
- Output location
- Execution time

---

## Design Principles

- Deterministic processing
- Explainable transformations
- Canonical internal model
- Configurable projection layer
- Graceful error handling
- No invented data
- Single responsibility architecture
- Modular and extensible design

---

## Current Limitations

- Placeholder PDF/DOCX files included in the repository are not valid documents.
- Microsoft Word `.doc` files are not supported.
- Multi-candidate clustering is not implemented.
- Output directory and filename are currently fixed.
- Validation reports errors but does not prevent JSON generation.
- JSON is the only supported output format.

---

## Repository Contents

- Complete Spring Boot source code
- Maven project
- Runtime configuration
- Unit tests
- Sample project structure
- Interactive CLI
- JSON output generation

---

## Author

Swathi S

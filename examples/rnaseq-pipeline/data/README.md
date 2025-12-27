# Test Data for RNA-seq Pipeline Example

This directory contains sample test data to demonstrate the Illumina FASTQ filename parsing capabilities of nf-pregex.

## Files

### FASTQ Files (Empty placeholders)

The following empty `.fastq.gz` files follow the standard Illumina naming convention:

```
<sample>_S<sample_number>_L<lane>_R<read>_<chunk>.fastq.gz
```

**Sample 1 (SAMPLE_001):**
- `SAMPLE_001_S1_L001_R1_001.fastq.gz` - Read 1
- `SAMPLE_001_S1_L001_R2_001.fastq.gz` - Read 2

**Sample 2 (SAMPLE_002):**
- `SAMPLE_002_S2_L001_R1_001.fastq.gz` - Read 1
- `SAMPLE_002_S2_L001_R2_001.fastq.gz` - Read 2

**Sample 3 (SAMPLE_003):**
- `SAMPLE_003_S3_L001_R1_001.fastq.gz` - Read 1
- `SAMPLE_003_S3_L001_R2_001.fastq.gz` - Read 2

### Samplesheet

`samplesheet.csv` - A CSV file mapping sample names to their corresponding FASTQ files:

```csv
sample,fastq_1,fastq_2
SAMPLE_001,SAMPLE_001_S1_L001_R1_001.fastq.gz,SAMPLE_001_S1_L001_R2_001.fastq.gz
SAMPLE_002,SAMPLE_002_S2_L001_R1_001.fastq.gz,SAMPLE_002_S2_L001_R2_001.fastq.gz
SAMPLE_003,SAMPLE_003_S3_L001_R1_001.fastq.gz,SAMPLE_003_S3_L001_R2_001.fastq.gz
```

## Running the Example

From the `examples/rnaseq-pipeline` directory:

```bash
# Run with glob pattern
nextflow run main.nf --reads 'data/*_S*_L*_R*_*.fastq.gz'

# Or run from the repository root
cd ../../
nextflow run examples/rnaseq-pipeline/main.nf --reads 'examples/rnaseq-pipeline/data/*_S*_L*_R*_*.fastq.gz'
```

## Expected Output

The pipeline will parse each filename and extract metadata, creating JSON files in the `results/parsed_metadata/` directory:

```json
{
  "sample_name": "SAMPLE_001",
  "sample_number": 1,
  "lane": "001",
  "read": "R1",
  "chunk": "001",
  "filename": "SAMPLE_001_S1_L001_R1_001.fastq.gz",
  "parsing_method": "nf-pregex"
}
```

## Note on Test Files

These are **empty placeholder files** created with `touch` to demonstrate filename parsing. They do not contain actual sequencing data. For real RNA-seq analysis, you would use actual FASTQ files from an Illumina sequencer.

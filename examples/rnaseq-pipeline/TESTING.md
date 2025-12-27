# Testing the RNA-seq Pipeline Example

This guide explains how to test the RNA-seq pipeline example with the provided test data.

## Prerequisites

Since nf-pregex is a Nextflow plugin under development, you need to build and install it locally before running the example.

## Option 1: Build and Install the Plugin Locally (Recommended)

### Step 1: Build the Plugin

From the repository root:

```bash
# Build the plugin
make clean
./gradlew build

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

### Step 2: Configure Nextflow to Use Local Plugin

Add to your `~/.nextflow/config` or create a local config file:

```groovy
plugins {
    repositories {
        mavenLocal()
        maven {
            url 'https://plugins.nextflow.io'
        }
    }
}
```

### Step 3: Run the Example

```bash
cd examples/rnaseq-pipeline

# Run with the test data
nextflow run main.nf --reads 'data/*_S*_L*_R*_*.fastq.gz'
```

## Option 2: Test Without Plugin (Verify Data Structure)

If you just want to verify the test data structure without running the full pipeline, you can list the files:

```bash
cd examples/rnaseq-pipeline/data

# List all test FASTQ files
ls -lh *.fastq.gz

# View the samplesheet
cat samplesheet.csv

# Count files by sample
ls -1 SAMPLE_*.fastq.gz | cut -d_ -f1-2 | sort | uniq -c
```

Expected output:
```
2 SAMPLE_001
2 SAMPLE_002
2 SAMPLE_003
```

## Option 3: Use Nextflow Without the Plugin

You can test basic Nextflow functionality with traditional regex:

```bash
cd examples/rnaseq-pipeline

# Create a simple test workflow
cat > test_simple.nf << 'EOF'
#!/usr/bin/env nextflow

params.reads = 'data/*_S*_L*_R*_*.fastq.gz'

workflow {
    channel
        .fromPath(params.reads)
        .map { file ->
            // Traditional regex parsing
            def m = (file.name =~ /^(.+)_S(\d+)_L(\d{3})_(R[12])_(\d{3})\.fastq\.gz$/)
            if (!m) return null
            
            [
                sample_name: m[0][1],
                sample_number: m[0][2].toInteger(),
                lane: m[0][3],
                read: m[0][4],
                chunk: m[0][5],
                file: file
            ]
        }
        .filter { it != null }
        .view { meta ->
            "Sample: ${meta.sample_name}, Read: ${meta.read}, File: ${meta.file.name}"
        }
}
EOF

# Run the simple test
nextflow run test_simple.nf
```

## Expected Results

### When Running the Full Pipeline

The pipeline will create `results/parsed_metadata/` directory with JSON files for each input:

```
results/
└── parsed_metadata/
    ├── SAMPLE_001_S1_L001_R1_001.json
    ├── SAMPLE_001_S1_L001_R2_001.json
    ├── SAMPLE_002_S2_L001_R1_001.json
    ├── SAMPLE_002_S2_L001_R2_001.json
    ├── SAMPLE_003_S3_L001_R1_001.json
    └── SAMPLE_003_S3_L001_R2_001.json
```

Each JSON file contains parsed metadata:

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

## Test Data Details

The test data includes:
- **6 FASTQ files**: 3 samples × 2 reads (paired-end)
- **1 samplesheet**: CSV mapping samples to files
- **Illumina naming convention**: `<sample>_S<num>_L<lane>_R<read>_<chunk>.fastq.gz`

All FASTQ files are empty placeholders (created with `touch`) - they're only used to demonstrate filename parsing, not actual sequence processing.

## Troubleshooting

### Plugin Not Found Error

```
ERROR ~ Plugin with id nf-pregex not found in any repository
```

**Solution**: Build and publish the plugin locally (see Option 1 above).

### No Files Found

```
No files match pattern: data/*_S*_L*_R*_*.fastq.gz
```

**Solution**: Make sure you're running from the `examples/rnaseq-pipeline` directory, or adjust the path:

```bash
nextflow run main.nf --reads '/absolute/path/to/data/*_S*_L*_R*_*.fastq.gz'
```

### Permission Denied

**Solution**: Ensure the FASTQ files exist and are readable:

```bash
ls -l data/*.fastq.gz
```

## Next Steps

After verifying the example works:

1. **Examine the parsed output**: Check the JSON files in `results/parsed_metadata/`
2. **Compare implementations**: Review the traditional regex vs. nf-pregex code in `main.nf`
3. **Try your own patterns**: Modify `main.nf` to parse different filename formats
4. **Explore the plugin**: Check out other examples in the `examples/` directory

## Resources

- [nf-pregex Documentation](../../README.md)
- [Nextflow Documentation](https://www.nextflow.io/docs/latest/)
- [Illumina FASTQ Format](https://support.illumina.com/help/BaseSpace_Sequence_Hub_OLH_009008_2/Source/Informatics/BS/NamingConvention_FASTQ-files-swBS.htm)

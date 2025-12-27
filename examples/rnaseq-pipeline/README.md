# RNA-seq Pipeline - nf-pregex Example

A simple RNA-seq pipeline demonstrating the benefits of using the **nf-pregex plugin** for pattern matching and validation in Nextflow workflows.

## Overview

This example showcases how nf-pregex transforms complex regex patterns into readable, maintainable code using human-friendly pattern builders. Instead of cryptic regex strings, you get self-documenting patterns specifically designed for bioinformatics workflows.

## Pipeline Steps

1. **Quality Control** - FastQC analysis of raw reads
2. **Salmon Index** - Build transcriptome index for quantification
3. **Salmon Quant** - Transcript-level quantification using quasi-mapping
4. **MultiQC** - Aggregate QC reports from FastQC and Salmon

## Key Benefits Demonstrated

### 1. **Readable Input Validation** 🎯

**Traditional Regex Approach:**
```groovy
def pattern = /(\w)+_(R1|R2)\.fastq(\.gz)?/
// What does this match? Hard to tell at a glance!
```

**With nf-pregex:**
```groovy
def fastqPattern = Sequence(
    OneOrMore(WordChar()),     // Sample name - clear!
    ReadPair(),                // _R1, _R2, etc. - semantic!
    FastqExtension()           // .fastq.gz, .fq - standardized!
)
// Immediately obvious what this matches!
```

### 2. **Self-Documenting Code** 📖

The pattern definitions serve as documentation:
- `ReadPair()` - everyone knows this means R1/R2 or _1/_2
- `FastqExtension()` - no need to remember all possible FASTQ extensions
- `Chromosome()` - handles chr1, chr22, chrX, chrY, chrM automatically

### 3. **Bioinformatics-Specific Patterns** 🧬

Built-in patterns for common bioinformatics formats:

```groovy
// DNA sequence validation
def dna = DNASequence()              // ACGT nucleotides
def ambigDna = DNASequenceWithAmbiguity()  // Includes N, R, Y, etc.

// File extension matching
FastqExtension()        // .fastq, .fq, .fastq.gz
AlignmentExtension()    // .bam, .sam, .cram
VcfExtension()          // .vcf, .vcf.gz, .bcf
BedExtension()          // .bed, .bed.gz

// Genomic identifiers
Chromosome()            // chr1-22, chrX, chrY, chrM, with/without prefix
ReadPair()             // _R1, _R2, _1, _2, .R1, .R2
```

### 4. **Error Prevention** ✅

Automatic escaping prevents common regex errors:

```groovy
// Traditional regex - easy to forget escaping
def pattern = /file.txt/  // Oops! . matches any character

// With nf-pregex - automatic escaping
def pattern = Literal("file.txt")  // Correctly escaped: file\.txt
```

### 5. **Better Maintainability** 🔧

Complex patterns remain readable:

```groovy
// Extract sample metadata with clear structure
def samplePattern = Sequence(
    OneOrMore(WordChar()),     // Sample ID
    Literal("_"),
    OneOrMore(WordChar()),     // Condition (control/treatment)
    Literal("_"),
    Literal("rep"),
    Digit(),                   // Replicate number
    Literal("_"),
    Either(["R1", "R2"]),      // Read pair
    FastqExtension()
)
// Matches: SAMPLE001_control_rep1_R1.fastq.gz
```

## Quick Start

### Prerequisites

- Nextflow >= 23.10.0
- nf-pregex plugin >= 0.1.0

### Installation

The plugin is automatically loaded via the `nextflow.config` file:

```groovy
plugins {
    id 'nf-pregex@0.1.0'
}
```

### Running the Pipeline

```bash
# View help
nextflow run main.nf --help

# Run with default parameters (dry run - creates mock outputs)
nextflow run main.nf

# Run with custom inputs
nextflow run main.nf \
    --reads "data/*_{R1,R2}.fastq.gz" \
    --reference "reference/genome.fa" \
    --outdir "results"

# Skip quality control
nextflow run main.nf --skip_qc

# Use Docker containers
nextflow run main.nf -profile docker
```

### Expected Input Structure

```
project/
├── data/
│   ├── sample1_R1.fastq.gz
│   ├── sample1_R2.fastq.gz
│   ├── sample2_R1.fastq.gz
│   └── sample2_R2.fastq.gz
└── reference/
    └── genome.fa
```

## Pattern Examples from This Pipeline

### Input File Validation

```groovy
def validFastqPattern = Sequence(
    OneOrMore(WordChar()),     // Sample name
    ReadPair(),                // _R1, _R2, etc.
    FastqExtension()           // .fastq.gz, .fq
)

// Validates: sample1_R1.fastq.gz ✓
// Rejects: sample1.txt ✗
```

### Output File Naming

```groovy
def trimmedPattern = Sequence(
    Literal(meta.id),
    Literal("_trimmed"),
    ReadPair(),
    FastqExtension()
)
// Produces: sample1_trimmed_R1.fastq.gz
```

### Alignment Output

```groovy
def bamPattern = Sequence(
    Literal(meta.id),
    AlignmentExtension()       // .bam, .sam, .cram
)
// Produces: sample1.bam
```

### Chromosome Validation

```groovy
def chrPattern = Chromosome()  // chr1, chr2, chrX, etc.
// Validates chromosome names in outputs
```

## Comparison: Traditional vs nf-pregex

| Aspect | Traditional Regex | nf-pregex |
|--------|------------------|-----------|
| **Readability** | `/(\w)+_(R1\|R2)\.fastq(\.gz)?/` | `Sequence(OneOrMore(WordChar()), ReadPair(), FastqExtension())` |
| **Maintainability** | Need regex expertise | Self-explanatory |
| **Error-prone** | Easy to forget escaping | Automatic escaping |
| **Reusability** | Copy-paste strings | Import pattern functions |
| **Documentation** | Needs comments | Code is documentation |
| **Bioinformatics** | DIY everything | Built-in patterns |

## Benefits Summary

✅ **Readable** - Patterns are self-documenting  
✅ **Maintainable** - Easy to modify and understand  
✅ **Type-safe** - Compile-time checking in Groovy  
✅ **Reusable** - Build complex patterns from simple components  
✅ **Error-preventing** - Automatic escaping of special characters  
✅ **Domain-specific** - Bioinformatics patterns included  
✅ **Collaborative** - Team members without regex expertise can understand code  

## Real-World Use Cases

### Use Case 1: Complex Sample ID Parsing
```groovy
// Traditional regex: /S(\d){3}_(T|C)(\d)_(R1|R2)/
// Hard to understand what this matches!

// With nf-pregex:
def sampleIdPattern = Sequence(
    Literal("S"),
    Digit().exactly(3),        // Sample number: 001-999
    Literal("_"),
    Either(["T", "C"]),        // Treatment or Control
    Digit(),                   // Replicate: 1-9
    Literal("_"),
    Either(["R1", "R2"])       // Read pair
)
// Crystal clear: Matches S001_T1_R1, S123_C2_R2, etc.
```

### Use Case 2: Multi-format File Handling
```groovy
// Accept multiple input formats with clear validation
def sequencingFile = Sequence(
    OneOrMore(WordChar()),
    Either(
        FastqExtension(),      // .fastq, .fq, .fastq.gz
        VcfExtension(),        // .vcf, .vcf.gz, .bcf
        AlignmentExtension()   // .bam, .sam, .cram
    )
)
```

### Use Case 3: Metadata Extraction
```groovy
// Extract components from complex filenames
def plateWellPattern = Sequence(
    CharRange('A', 'H'),       // Row: A-H
    CharRange('0', '9').exactly(2)  // Column: 00-99
)
// Matches: A01, B12, H08 - plate coordinates!
```

## Pipeline Output

```
results/
├── fastqc/
│   ├── sample1_R1_fastqc.html
│   ├── sample1_R1_fastqc.zip
│   └── ...
├── trimmed/
│   ├── sample1_trimmed_R1.fastq.gz
│   ├── sample1_trimmed_R2.fastq.gz
│   └── ...
├── alignments/
│   ├── sample1.bam
│   ├── sample1.STAR.log
│   └── ...
├── counts/
│   ├── sample1.counts.txt
│   └── ...
├── pipeline_report.html
├── timeline.html
└── trace.txt
```

## Advanced Features

### Method Chaining

```groovy
// Combine patterns fluently
def pattern = Literal("sample")
    .then(Digit().exactly(3))
    .then(Literal(".txt"))
    .optional()
```

### Custom Patterns

```groovy
// Create reusable custom patterns
def customID = Sequence(
    MultiRange("'A'-'Z', '0'-'9'").exactly(3),
    Literal("-"),
    MultiRange("'a'-'z', '0'-'9'").exactly(4)
)
// Matches: A1B-x9y2, 3XZ-test, etc.
```

## Learn More

- [nf-pregex Plugin Documentation](../../README.md)
- [API Reference](../../docs/API.md)
- [Tutorial](../../docs/TUTORIAL.md)
- [Basic Examples](../basic_usage.nf)
- [File Matching Examples](../file_matching.nf)

## Contributing

Found a bug or have a suggestion? Please open an issue or pull request in the [nf-pregex repository](https://github.com/mribeirodantas/nf-pregex).

## License

This example is part of the nf-pregex plugin, licensed under the Apache License 2.0.

---

**💡 Pro Tip:** Start using nf-pregex patterns incrementally. Replace the most complex regex patterns first, and gradually adopt patterns throughout your pipeline. Your future self (and your collaborators) will thank you!

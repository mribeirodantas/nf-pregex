# nf-pregex Examples

This directory contains comprehensive examples demonstrating the nf-pregex plugin functionality.

## 🚀 Quick Start

All examples require the nf-pregex plugin. Enable it using one of these methods:

### Method 1: Configuration File (Recommended)
Add to `nextflow.config`:
```groovy
plugins {
    id 'nf-pregex@0.1.0'
}
```

### Method 2: Command Line
```bash
nextflow run <example>.nf -plugins nf-pregex@0.1.0
```

### Method 3: Use Provided Config
The examples directory includes a `nextflow.config` with the plugin already configured:
```bash
cd examples
nextflow run <example>.nf
```

## 📚 Available Examples

### 1. basic_usage.nf
**Purpose**: Introduction to core pattern building concepts

**What you'll learn**:
- Basic pattern builders (Either, Literal, Optional)
- Quantifiers (OneOrMore, Exactly, Range)
- Sequence composition
- Named groups for data extraction
- Method chaining

**Run**:
```bash
nextflow run basic_usage.nf -plugins nf-pregex@0.1.0
```

**Key patterns demonstrated**:
- Email-like patterns
- Sample ID patterns
- FASTQ filename patterns
- Metadata extraction with named groups

---

### 2. file_matching.nf
**Purpose**: Pattern matching for bioinformatics file formats

**What you'll learn**:
- FASTQ file pattern matching
- BAM/SAM patterns
- VCF file patterns with versioning
- Sample ID validation
- Using patterns with Nextflow channels
- Modern bioinformatics patterns (ReadPair, FastqExtension, etc.)
- Metadata extraction with named groups

**Run**:
```bash
nextflow run file_matching.nf -plugins nf-pregex@0.1.0
```

**Key patterns demonstrated**:
- `sample_R1.fastq.gz` matching
- `S001_T1_R1` sample ID format
- `variants.v1.vcf.gz` versioned VCF files
- Illumina filename parsing with named groups

---

### 3. bioinformatics_patterns.nf
**Purpose**: Comprehensive showcase of built-in bioinformatics patterns

**What you'll learn**:
- File extension patterns (FastqExtension, VcfExtension, AlignmentExtension, etc.)
- ReadPair patterns (_R1, _R2, .1, .2, etc.)
- Chromosome patterns (chr1, chrX, etc.)
- DNA/RNA sequence validation
- Protein sequence patterns
- Complete metadata extraction workflows

**Run**:
```bash
nextflow run bioinformatics_patterns.nf -plugins nf-pregex@0.1.0
```

**Key patterns demonstrated**:
- All built-in file extension patterns
- Chromosome name matching (flexible and strict modes)
- DNA sequence validation with IUPAC codes
- Illumina filename parsing
- VCF files with chromosome information
- Method chaining for cleaner code

---

### 4. named_groups_example.nf
**Purpose**: Deep dive into named capturing groups

**What you'll learn**:
- Creating named groups
- Extracting matched data by name
- Accessing groups by numeric index
- Practical bioinformatics use cases

**Run**:
```bash
nextflow run named_groups_example.nf -plugins nf-pregex@0.1.0
```

---

### 5. rnaseq-pipeline/
**Purpose**: Complete RNA-seq pipeline example

A full-featured example pipeline demonstrating nf-pregex in a real-world scenario:
- Input file validation
- Sample metadata parsing
- Quality control
- Alignment
- Quantification

**Run**:
```bash
cd rnaseq-pipeline
nextflow run main.nf -plugins nf-pregex@0.1.0
```

See `rnaseq-pipeline/README.md` for detailed documentation.

---

## 🎯 Pattern Categories

### Basic Patterns
- **Either**: Alternation (OR logic)
- **Literal**: Literal text with auto-escaping
- **Optional**: Zero or one occurrence
- **Sequence**: Concatenate patterns
- **Group**: Capturing groups (named or numbered)

### Quantifiers
- **OneOrMore**: One or more occurrences (+)
- **ZeroOrMore**: Zero or more occurrences (*)
- **Exactly**: Exact number of occurrences {n}
- **Range**: Between min and max {min,max}
- **AtLeast**: At least n occurrences {n,}

### Character Classes
- **Digit**: Numeric digits [0-9]
- **WordChar**: Word characters [a-zA-Z0-9_]
- **Whitespace**: Whitespace characters
- **AnyChar**: Any character (.)
- **CharClass**: Custom character set
- **CharRange**: Character ranges

### Bioinformatics Patterns

#### File Extensions (Case-Insensitive)
- **FastqExtension**: .fastq, .fq (with optional .gz)
- **VcfExtension**: .vcf, .bcf (with optional .gz)
- **AlignmentExtension**: .bam, .sam, .cram
- **BedExtension**: .bed (with optional .gz)
- **GffGtfExtension**: .gff, .gff3, .gtf (with optional .gz)
- **FastaExtension**: .fa, .fasta, .fna (with optional .gz)

#### Genomic Identifiers
- **ReadPair**: _R1, _R2, .R1, .R2, _1, _2, .1, .2
- **Chromosome**: chr1-22, chrX, chrY, chrM (with/without 'chr')
- **StrictChromosome**: Requires 'chr' prefix

#### Sequence Patterns
- **DNASequence**: DNA sequences (ACGT, case-insensitive)
- **StrictDNASequence**: Uppercase DNA only
- **DNASequenceWithAmbiguity**: DNA with IUPAC codes
- **ProteinSequence**: Protein sequences
- **PhredQuality**: Phred quality scores

---

## 💡 Common Use Cases

### Matching Paired-End FASTQ Files
```groovy
include { ReadPair; FastqExtension; Sequence; OneOrMore; WordChar } from 'plugin/nf-pregex'

def pattern = Sequence([
    OneOrMore(WordChar()),
    ReadPair(),
    FastqExtension()
])
// Matches: sample_R1.fastq.gz, test.1.fq, control_R2.fastq
```

### Extracting Sample Metadata
```groovy
include { Group; ReadPair; FastqExtension; Sequence; OneOrMore; WordChar; Literal } from 'plugin/nf-pregex'

def pattern = Sequence([
    Group('sample', OneOrMore(WordChar())),
    Literal('_'),
    Group('read', ReadPair()),
    FastqExtension()
])

def filename = "SampleA_R1.fastq.gz"
def matcher = filename =~ pattern
println matcher.group('sample')  // "SampleA"
println matcher.group('read')    // "R1"
```

### Validating DNA Sequences
```groovy
include { DNASequenceWithAmbiguity } from 'plugin/nf-pregex'

def pattern = DNASequenceWithAmbiguity()
def sequence = "ACGTNNRYSWKM"
if (sequence =~ /${pattern}/) {
    println "Valid DNA sequence with IUPAC codes"
}
```

### Method Chaining
```groovy
include { Literal; Digit; ReadPair; FastqExtension } from 'plugin/nf-pregex'

def pattern = Literal("sample")
    .then(Literal("_"))
    .then(Digit().oneOrMore())
    .then(Literal("_"))
    .then(ReadPair())
    .then(FastqExtension())
// Matches: sample_123_R1.fastq.gz
```

---

## 🧪 Testing Examples

All examples are self-contained and can be run independently:

```bash
# Test individual examples
nextflow run basic_usage.nf -plugins nf-pregex@0.1.0
nextflow run file_matching.nf -plugins nf-pregex@0.1.0
nextflow run bioinformatics_patterns.nf -plugins nf-pregex@0.1.0
nextflow run named_groups_example.nf -plugins nf-pregex@0.1.0

# Test the full pipeline
cd rnaseq-pipeline
nextflow run main.nf -plugins nf-pregex@0.1.0
```

---

## 📖 Additional Resources

- **Plugin Documentation**: [../README.md](../README.md)
- **API Reference**: [../docs/API.md](../docs/API.md)
- **Nextflow Documentation**: https://www.nextflow.io/docs/latest/
- **Original pregex (Python)**: https://github.com/manoss96/pregex

---

## 🤝 Contributing Examples

Have a useful pattern or example? Contributions are welcome! Please ensure:
1. Examples are well-commented
2. Include example input/output
3. Demonstrate a specific use case
4. Follow the existing code style

---

## 📝 Notes

- All examples use the strict syntax mode for better error reporting
- Patterns are designed to be composable and reusable
- Named groups are recommended for metadata extraction
- Use built-in bioinformatics patterns when available (more maintainable)

For questions or issues, please visit the [GitHub repository](https://github.com/mribeirodantas/nf-pregex).

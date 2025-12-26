# nf-pregex Tutorial

A step-by-step guide to using the nf-pregex plugin in your Nextflow pipelines.

## Introduction

Regular expressions are powerful but can be difficult to read and maintain. The nf-pregex plugin provides a human-readable API for building regex patterns, making your Nextflow pipelines more maintainable and easier to understand.

## Installation

Add the plugin to your `nextflow.config`:

```groovy
plugins {
    id 'nf-pregex@0.1.0'
}
```

## Lesson 1: Basic Patterns

### The Problem with Traditional Regex

Let's say you want to match FASTQ files with the pattern `sample_R1.fastq.gz` or `sample_R2.fastq.gz`:

**Traditional approach:**
```groovy
def pattern = /\w+_(R1|R2)\.fastq\.gz/
```

Questions:
- What does `\w+` mean again?
- Why do we need to escape the dots?
- Is that a pipe or a lowercase L?

**With nf-pregex:**
```groovy
include { Sequence; OneOrMore; Literal; Either; WordChar } from 'plugin/nf-pregex'

def pattern = Sequence([
    OneOrMore(WordChar()),      // One or more word characters
    Literal("_"),               // Literal underscore
    Either(["R1", "R2"]),         // Either R1 or R2
    Literal(".fastq.gz")        // Literal file extension
])
```

Much clearer! The pattern is self-documenting.

### Your First Pattern

Create a simple script `hello_pregex.nf`:

```groovy
#!/usr/bin/env nextflow

include { Either; Literal } from 'plugin/nf-pregex'

workflow {
    // Match "hello" or "hi"
    def greeting = Either(["hello", "hi"])
    
    // Test it
    def tests = ["hello", "hi", "hey"]
    tests.each { word ->
        if (word =~ /${greeting}/) {
            println "✓ '${word}' matches!"
        } else {
            println "✗ '${word}' doesn't match"
        }
    }
}
```

Run it:
```bash
nextflow run hello_pregex.nf -plugins nf-pregex@0.1.0
```

## Lesson 2: Building Complex Patterns

### File Extension Matching

Let's build a pattern for compressed files:

```groovy
include { 
    Sequence
    Literal
    Optional
    Either
} from 'plugin/nf-pregex'

workflow {
    // Match .txt, .txt.gz, .txt.zip, etc.
    def compressedText = Sequence([
        Literal(".txt"),
        Optional(Sequence([
            Literal("."),
            Either(["gz", "zip", "bz2"])
        ]))
    ])
    
    println compressedText  // → \.txt((\.(gz|zip|bz2)))?
}
```

### Sample ID Validation

Enforce specific sample naming conventions:

```groovy
include { 
    Sequence
    Literal
    Digit
    Either
} from 'plugin/nf-pregex'

workflow {
    // Pattern: SAMPLE001_T1 or SAMPLE001_C1
    // (T = Treatment, C = Control)
    def sampleId = Sequence([
        Literal("SAMPLE"),
        Digit().exactly(3),
        Literal("_"),
        Either(["T", "C"]),
        Digit()
    ])
    
    // Test samples
    def samples = [
        "SAMPLE001_T1",  // Valid
        "SAMPLE123_C2",  // Valid
        "SAMPLE01_T1",   // Invalid - only 2 digits
        "SAMPLE001_X1"   // Invalid - not T or C
    ]
    
    samples.each { sample ->
        println "${sample}: ${sample ==~ /${sampleId}/ ? '✓' : '✗'}"
    }
}
```

## Lesson 3: Working with Channels

### Filtering Files

```groovy
include { 
    Sequence
    OneOrMore
    Literal
    Either
    WordChar
} from 'plugin/nf-pregex'

workflow {
    // Pattern for paired-end FASTQ files
    def fastqPattern = Sequence([
        OneOrMore(WordChar()),
        Literal("_"),
        Either(["R1", "R2"]),
        Literal(".fastq.gz")
    ])
    
    // Create test channel
    channel
        .of(
            "sample001_R1.fastq.gz",
            "sample001_R2.fastq.gz",
            "sample002_R1.fastq.gz",
            "reference.fasta",
            "metadata.txt"
        )
        .filter { it =~ /${fastqPattern}/ }
        .view { "Found FASTQ: ${it}" }
}
```

### Extracting Information

```groovy
include { 
    Sequence
    OneOrMore
    Literal
    Either
    WordChar
} from 'plugin/nf-pregex'

workflow {
    def fastqPattern = Sequence([
        OneOrMore(WordChar()),
        Literal("_"),
        Either(["R1", "R2"]),
        Literal(".fastq.gz")
    ])
    
    channel
        .of(
            "sample001_R1.fastq.gz",
            "sample001_R2.fastq.gz"
        )
        .map { filename ->
            def matcher = filename =~ /(\w+)_(R[12])\.fastq\.gz/
            if (matcher) {
                def sampleId = matcher[0][1]
                def read = matcher[0][2]
                [sampleId, read, filename]
            }
        }
        .view { sample, read, file -> 
            "Sample: ${sample}, Read: ${read}, File: ${file}" 
        }
}
```

## Lesson 4: Reusable Patterns

Create a library of common patterns:

```groovy
include { 
    Sequence
    OneOrMore
    Literal
    Either
    Optional
    Digit
    WordChar
} from 'plugin/nf-pregex'

// Define reusable patterns
def sampleName() {
    OneOrMore(WordChar())
}

def readPair() {
    Either(["R1", "R2"])
}

def fastqExtension() {
    Sequence([
        Literal(".fastq"),
        Optional(Literal(".gz"))
    ])
}

// Compose them
workflow {
    def fastqPattern = Sequence([
        sampleName(),
        Literal("_"),
        readPair(),
        fastqExtension()
    ])
    
    println "FASTQ pattern: ${fastqPattern}"
}
```

## Lesson 5: Method Chaining

PRegEx patterns support fluent method chaining:

```groovy
include { Literal; WordChar } from 'plugin/nf-pregex'

workflow {
    // Make a pattern optional
    def pattern1 = Literal("test").optional()
    println pattern1  // → (test)?
    
    // Chain multiple operations
    def pattern2 = WordChar()
        .oneOrMore()
        .then(Literal("@"))
        .then(WordChar().oneOrMore())
        .then(Literal(".com"))
    
    println pattern2  // Email-like pattern
    
    // Complex chaining
    def filePattern = Literal("file")
        .then(Digit().exactly(3))
        .then(Literal(".txt").optional())
    
    println filePattern  // → file(\d){3}(\.txt)?
}
```

## Lesson 6: Real-World Example

Complete workflow for processing paired-end RNA-seq data:

```groovy
#!/usr/bin/env nextflow

include { 
    Sequence
    OneOrMore
    Literal
    Either
    Optional
    WordChar
} from 'plugin/nf-pregex'

// Define patterns
def fastqPattern = Sequence([
    OneOrMore(WordChar()),
    Literal("_"),
    Either(["R1", "R2"]),
    Literal(".fastq"),
    Optional(Literal(".gz"))
])

def sampleIdFromFastq = Sequence(
    OneOrMore(WordChar())
)

workflow {
    // Input channel with pattern matching
    reads_ch = channel
        .fromFilePairs("data/*_{R1,R2}.fastq.gz")
        .filter { id, files ->
            files.every { it.name =~ /${fastqPattern}/ }
        }
        .view { id, files -> 
            "Processing sample: ${id} with ${files.size()} files" 
        }
    
    // Process the files
    reads_ch
        .map { id, files -> 
            [id, files[0], files[1]]
        }
        .view { id, r1, r2 ->
            """
            Sample: ${id}
              R1: ${r1.name}
              R2: ${r2.name}
            """
        }
}
```

## Lesson 7: Debugging Patterns

### View the Generated Regex

```groovy
include { Sequence; Literal; Digit } from 'plugin/nf-pregex'

workflow {
    def pattern = Sequence([
        Literal("sample"),
        Digit().exactly(3)
    ])
    
    // See what regex string is generated
    println "Pattern: ${pattern}"
    println "Regex: ${pattern.toRegex()}"
    
    // Test it
    def test = "sample123"
    def matches = test =~ /${pattern}/
    println "Matches: ${matches.matches()}"
}
```

### Common Mistakes

**Mistake 1: Forgetting to escape special characters**
```groovy
// ❌ Wrong - dots are not escaped
def wrong = "file.txt"  // This is just a string, not a pattern

// ✓ Correct - use Literal to auto-escape
def correct = Literal("file.txt")  // → file\.txt
```

**Mistake 2: Using string interpolation incorrectly**
```groovy
include { Literal; Either } from 'plugin/nf-pregex'

// ❌ Wrong - interpolating pattern object directly
def pattern = Either(["R1", "R2"])
def wrong = "*_${pattern}.fastq.gz"  // Won't work as expected

// ✓ Correct - use pattern in regex context
def files = "*_R{1,2}.fastq.gz"  // For file globbing
def regex = /${pattern}/         // For pattern matching
```

## Lesson 8: Performance Tips

### Cache Compiled Patterns

```groovy
include { Sequence; OneOrMore; Literal; WordChar } from 'plugin/nf-pregex'

workflow {
    def pattern = Sequence([
        OneOrMore(WordChar()),
        Literal(".txt")
    ])
    
    // Compile once, reuse many times
    def compiled = ~/${pattern}/
    
    channel
        .of("file1.txt", "file2.txt", "file3.doc")
        .filter { it =~ compiled }  // Reuse compiled pattern
        .view()
}
```

### Simplify When Possible

```groovy
include { Either; Literal } from 'plugin/nf-pregex'

// If you only need simple alternation, use Nextflow's built-in
// file globbing instead of regex:

// Instead of this:
def pattern = Either(["R1", "R2"])
channel.fromPath("data/*_{R1,R2}.fastq.gz")

// This is more efficient:
channel.fromPath("data/*_R{1,2}.fastq.gz")
```

## Advanced Topics

### Creating Custom Pattern Functions

```groovy
include { 
    Sequence
    OneOrMore
    Literal
    Either
    Digit
    WordChar
} from 'plugin/nf-pregex'

// Custom helper for version numbers
def versionNumber() {
    Sequence([
        Digit().oneOrMore(),
        Literal("."),
        Digit().oneOrMore(),
        Optional(Sequence([
            Literal("."),
            Digit().oneOrMore()
        ]))
    ])
}

// Custom helper for dates (YYYY-MM-DD)
def isoDate() {
    Sequence([
        Digit().exactly(4),
        Literal("-"),
        Digit().exactly(2),
        Literal("-"),
        Digit().exactly(2)
    ])
}

workflow {
    println "Version pattern: ${versionNumber()}"
    println "Date pattern: ${isoDate()}"
    
    // Use them
    def files = ["release-1.2.3.tar.gz", "backup-2024-01-15.zip"]
    files.each { file ->
        if (file =~ /${versionNumber()}/) {
            println "Found version in: ${file}"
        }
        if (file =~ /${isoDate()}/) {
            println "Found date in: ${file}"
        }
    }
}
```

## Lesson 7: Bioinformatics Patterns

The nf-pregex plugin includes a comprehensive library of pre-built patterns for common bioinformatics file formats and data types. These patterns save time and ensure correctness for standard bioinformatics applications.

### Why Use Pre-built Patterns?

Bioinformatics has standard file formats and naming conventions. Instead of writing the same regex patterns repeatedly (and potentially incorrectly), use the built-in patterns:

**Without pre-built patterns:**
```groovy
// Complex, error-prone, hard to read
def fastq = /\w+[._](R1|R2|1|2)\.(?i)(fastq|fq)(\.(?i)gz)?/
```

**With pre-built patterns:**
```groovy
include { ReadPair; FastqExtension; Sequence; OneOrMore; WordChar } from 'plugin/nf-pregex'

def fastq = Sequence(
    OneOrMore(WordChar()),
    ReadPair(),
    FastqExtension()
)
```

Much clearer and less error-prone!

### Example 1: Matching FASTQ Files

```groovy
#!/usr/bin/env nextflow

include {
    Sequence
    OneOrMore
    WordChar
    ReadPair
    FastqExtension
} from 'plugin/nf-pregex'

workflow {
    // Build pattern for paired-end FASTQ files
    def fastqPattern = Sequence(
        OneOrMore(WordChar()),  // Sample name
        ReadPair(),             // _R1, _R2, _1, _2, etc.
        FastqExtension()        // .fastq, .fq, with optional .gz
    )
    
    // Filter files
    channel.fromPath("data/*")
        .filter { it.name =~ /${fastqPattern}/ }
        .view { "Found FASTQ: ${it.name}" }
}
```

This matches:
- `sample_R1.fastq.gz`
- `control_R2.fq`
- `test.1.fastq.gz`
- `experiment.2.FQ.GZ` (case-insensitive!)

### Example 2: DNA Sequence Validation

```groovy
#!/usr/bin/env nextflow

include { DNASequence; DNASequenceWithAmbiguity } from 'plugin/nf-pregex'

workflow {
    def dna = DNASequence()
    def dnaWithAmbiguity = DNASequenceWithAmbiguity()
    
    // Validate DNA sequences
    def sequences = [
        "ACGTACGT",      // Valid DNA
        "ACGTN",         // Has ambiguity code N
        "ACGTRYSWKMN",   // Multiple ambiguity codes
        "ACGT123"        // Invalid - has numbers
    ]
    
    sequences.each { seq ->
        def isStrictDNA = seq =~ /${dna}/
        def isAmbigDNA = seq =~ /${dnaWithAmbiguity}/
        
        println "${seq}:"
        println "  Strict DNA: ${isStrictDNA ? '✓' : '✗'}"
        println "  With ambiguity: ${isAmbigDNA ? '✓' : '✗'}"
    }
}
```

Output:
```
ACGTACGT:
  Strict DNA: ✓
  With ambiguity: ✓
ACGTN:
  Strict DNA: ✗
  With ambiguity: ✓
ACGTRYSWKMN:
  Strict DNA: ✗
  With ambiguity: ✓
ACGT123:
  Strict DNA: ✗
  With ambiguity: ✗
```

### Example 3: Comprehensive File Type Filtering

```groovy
#!/usr/bin/env nextflow

include {
    Either
    Sequence
    OneOrMore
    WordChar
    FastqExtension
    VcfExtension
    AlignmentExtension
    BedExtension
    FastaExtension
} from 'plugin/nf-pregex'

workflow {
    // Match any common sequencing data file
    def sequencingFiles = Sequence(
        OneOrMore(WordChar()),
        Either(
            FastqExtension(),      // .fastq, .fq
            VcfExtension(),        // .vcf, .bcf
            AlignmentExtension(),  // .bam, .sam, .cram
            BedExtension(),        // .bed
            FastaExtension()       // .fa, .fasta, .fna
        )
    )
    
    // Process all sequencing files
    channel.fromPath("data/**/*", type: 'file')
        .filter { it.name =~ /${sequencingFiles}/ }
        .view { file ->
            def ext = file.name.tokenize('.').last()
            "Found ${ext.toUpperCase()} file: ${file.name}"
        }
}
```

### Example 4: VCF Files with Chromosome Information

```groovy
#!/usr/bin/env nextflow

include {
    Sequence
    Chromosome
    Literal
    OneOrMore
    WordChar
    VcfExtension
} from 'plugin/nf-pregex'

workflow {
    // Pattern: chr1_variants.vcf.gz, 22_filtered.bcf, etc.
    def vcfWithChr = Sequence(
        Chromosome(),           // chr1, chr22, X, chrY, etc.
        Literal("_"),
        OneOrMore(WordChar()),  // Description
        VcfExtension()          // .vcf, .vcf.gz, .bcf
    )
    
    channel.fromPath("results/*")
        .filter { it.name =~ /${vcfWithChr}/ }
        .map { file ->
            def matcher = file.name =~ /^(chr\d+|chr[XYM]|\d+|[XYM])_/
            def chr = matcher ? matcher[0][1] : 'unknown'
            [chr, file]
        }
        .groupTuple()
        .view { chr, files ->
            "Chromosome ${chr}: ${files.size()} file(s)"
        }
}
```

### Available Bioinformatics Patterns

#### Sequence Patterns
- `DNASequence()` - ACGT (case-insensitive)
- `StrictDNASequence()` - ACGT (uppercase only)
- `DNASequenceWithAmbiguity()` - ACGT + IUPAC codes
- `ProteinSequence()` - 20 amino acids (case-insensitive)
- `StrictProteinSequence()` - 20 amino acids (uppercase)
- `ProteinSequenceWithAmbiguity()` - Amino acids + B, Z, X, *
- `PhredQuality()` - Phred+33 quality scores

#### Genomic Identifiers
- `Chromosome()` - chr1-22, chrX/Y/M (flexible)
- `StrictChromosome()` - Requires 'chr' prefix
- `ReadPair()` - _R1/_R2, _1/_2, .R1/.R2, .1/.2

#### File Extensions (All case-insensitive)
- `FastqExtension()` - .fastq, .fq (.gz optional)
- `VcfExtension()` - .vcf, .bcf (.gz optional)
- `AlignmentExtension()` - .bam, .sam, .cram
- `BedExtension()` - .bed (.gz optional)
- `GffGtfExtension()` - .gff, .gff3, .gtf (.gz optional)
- `FastaExtension()` - .fa, .fasta, .fna (.gz optional)

### Pro Tips

1. **Combine patterns**: Use `Sequence()` to combine multiple bioinformatics patterns
2. **Case-insensitive by default**: File extension patterns match any case
3. **Compression support**: Most file patterns automatically handle .gz files
4. **Flexible chromosome names**: Use `Chromosome()` for maximum flexibility
5. **Strict validation**: Use `Strict*` variants when you need exact formatting

---

## Conclusion

The nf-pregex plugin makes regex patterns:
- **Readable**: Self-documenting code
- **Maintainable**: Easy to modify and understand
- **Safe**: Automatic escaping prevents errors
- **Composable**: Build complex patterns from simple parts

### Next Steps

1. Try the examples in `examples/basic_usage.nf`
2. Read the complete [API documentation](API.md)
3. Create your own pattern library for your pipeline
4. Share your patterns with the community!

## Getting Help

- Check the [README](../README.md) for quick reference
- Browse the [API docs](API.md) for detailed information
- Report issues on GitHub

Happy pattern building! 🎉

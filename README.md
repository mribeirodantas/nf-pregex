# nf-pregex

A Nextflow plugin that provides human-readable regex pattern builders, inspired by Python's [pregex](https://github.com/manoss96/pregex) library.

## Overview

Writing regular expressions can be challenging and error-prone. The nf-pregex plugin provides an intuitive API for building regex patterns using method calls instead of cryptic regex syntax.

### Before (Traditional Regex)
```groovy
def pattern = /sample(\d)+_(R1|R2)\.fastq\.gz/
```

### After (nf-pregex)
```groovy
include { Sequence; Literal; OneOrMore; Digit; Either } from 'plugin/nf-pregex'

def pattern = Sequence([
    Literal("sample"),
    OneOrMore(Digit()),
    Literal("_"),
    Either(["R1", "R2"]),
    Literal(".fastq.gz")
])
```

## Installation

Add the plugin to your Nextflow configuration:

```groovy
plugins {
    id 'nf-pregex@0.1.0'
}
```

Or use it directly in your pipeline with the `-plugins` option:

```bash
nextflow run main.nf -plugins nf-pregex@0.1.0
```

## Quick Start

```groovy
#!/usr/bin/env nextflow

include { Either; Literal; Optional } from 'plugin/nf-pregex'

workflow {
    // Match "color" or "colour"
    def pattern = Sequence([
        Literal("colo"),
        Optional(Literal("u")),
        Literal("r")
    ])
    
    println pattern  // Outputs: colo(u)?r
    
    // Use in channel operations
    channel.of("color", "colour", "colors")
        .filter { it =~ /${pattern}/ }
        .view()
}
```

## Bioinformatics Patterns Library

nf-pregex includes a comprehensive library of pre-built patterns for common bioinformatics file formats and data types. These ready-to-use patterns save time and ensure correct regex for standard bioinformatics applications.

### Using Bioinformatics Patterns

```groovy
#!/usr/bin/env nextflow

include { 
    DNASequence
    FastqExtension
    ReadPair
    Chromosome
} from 'plugin/nf-pregex'

workflow {
    // Match FASTQ files with read pairs
    def fastqFiles = Sequence(
        OneOrMore(WordChar()),
        ReadPair(),
        FastqExtension()
    )
    
    // Filter files by pattern
    channel.fromPath("data/*")
        .filter { it.name =~ /${fastqFiles}/ }
        .view()
    
    // Match DNA sequences with validation
    def dna = DNASequence()
    def sequence = "ACGTACGT"
    if (sequence =~ /${dna}/) {
        println "Valid DNA sequence!"
    }
}
```

### Available Bioinformatics Patterns

#### Sequence Patterns
- **`DNASequence()`** - DNA sequences (ACGT, case-insensitive)
- **`StrictDNASequence()`** - Uppercase DNA only
- **`DNASequenceWithAmbiguity()`** - DNA with IUPAC ambiguity codes (N, R, Y, etc.)
- **`ProteinSequence()`** - Protein sequences (20 standard amino acids)
- **`StrictProteinSequence()`** - Uppercase protein sequences
- **`ProteinSequenceWithAmbiguity()`** - Protein with ambiguity codes (B, Z, X, *)
- **`PhredQuality()`** - Phred quality scores (Phred+33 encoding)

#### Genomic Identifiers
- **`Chromosome()`** - Chromosome names (chr1-22, chrX, chrY, chrM, with/without 'chr')
- **`StrictChromosome()`** - Chromosome names requiring 'chr' prefix
- **`ReadPair()`** - Read pair identifiers (_R1, _R2, _1, _2, .R1, .R2, .1, .2)

#### File Extensions (Case-Insensitive)
- **`FastqExtension()`** - .fastq, .fq, with optional .gz
- **`VcfExtension()`** - .vcf, .bcf, with optional .gz
- **`AlignmentExtension()`** - .bam, .sam, .cram
- **`BedExtension()`** - .bed, .bed.gz
- **`GffGtfExtension()`** - .gff, .gff3, .gtf, with optional .gz
- **`FastaExtension()`** - .fa, .fasta, .fna, with optional .gz

### Examples

#### Matching Paired-End FASTQ Files
```groovy
include { Sequence; OneOrMore; WordChar; ReadPair; FastqExtension } from 'plugin/nf-pregex'

def pairedEndPattern = Sequence(
    OneOrMore(WordChar()),  // sample name
    ReadPair(),             // _R1, _R2, etc.
    FastqExtension()        // .fastq.gz, .fq, etc.
)

// Matches: sample_R1.fastq.gz, control_R2.fq, test.1.fastq.gz
```

#### Validating VCF Files with Chromosome Names
```groovy
include { Sequence; OneOrMore; WordChar; Literal; Chromosome; VcfExtension } from 'plugin/nf-pregex'

def vcfPattern = Sequence(
    Chromosome(),           // chr1, chrX, 22, etc.
    Literal("_"),
    OneOrMore(WordChar()),
    VcfExtension()          // .vcf, .vcf.gz, .bcf
)

// Matches: chr1_variants.vcf.gz, 22_filtered.bcf, chrX_calls.vcf
```

#### DNA Sequence Validation
```groovy
include { DNASequenceWithAmbiguity } from 'plugin/nf-pregex'

def dnaPattern = DNASequenceWithAmbiguity()

// Validates sequences with ambiguity codes
def sequences = ["ACGTACGT", "ACGTN", "ACGTRYSWKMN"]
sequences.each { seq ->
    if (seq =~ /${dnaPattern}/) {
        println "✓ Valid: ${seq}"
    }
}
```

See [docs/API.md](docs/API.md) for complete documentation of all bioinformatics patterns.

## API Reference

### Pattern Builders

#### Either(List)
Creates an alternation pattern (OR operation).

```groovy
Either(["foo", "bar", "baz"])  // → (foo|bar|baz)
```

#### Literal(String)
Matches literal text with all special regex characters escaped.

```groovy
Literal("file.txt")  // → file\.txt
Literal("a+b*c")     // → a\+b\*c
```

#### Optional(PRegEx)
Matches zero or one occurrence of a pattern.

```groovy
Optional(Literal("s"))  // → (s)?
```

#### OneOrMore(PRegEx)
Matches one or more occurrences of a pattern.

```groovy
OneOrMore(Digit())  // → (\d)+
```

#### ZeroOrMore(PRegEx)
Matches zero or more occurrences of a pattern.

```groovy
ZeroOrMore(WordChar())  // → (\w)*
```

#### Exactly(PRegEx, int)
Matches exactly n occurrences of a pattern.

```groovy
Exactly(Digit(), 3)  // → (\d){3}
```

#### Range(PRegEx, int, int)
Matches between min and max occurrences of a pattern.

```groovy
Range(Digit(), 2, 4)  // → (\d){2,4}
```

#### AtLeast(PRegEx, int)
Matches at least n occurrences of a pattern.

```groovy
AtLeast(Digit(), 2)  // → (\d){2,}
```

#### Sequence(List)
Concatenates multiple patterns in order.

```groovy
Sequence([Literal("hello"), Literal(" "), Literal("world")])  // → hello world
```

### Character Classes

#### AnyChar()
Matches any single character.

```groovy
AnyChar()  // → .
```

#### Digit()
Matches any digit (0-9).

```groovy
Digit()  // → \d
```

#### WordChar()
Matches any word character (a-z, A-Z, 0-9, _).

```groovy
WordChar()  // → \w
```

#### Whitespace()
Matches any whitespace character.

```groovy
Whitespace()  // → \s
```

#### CharClass(String)
Matches any character in the specified set.

```groovy
CharClass("abc")  // → [abc]
```

#### NotCharClass(String)
Matches any character NOT in the specified set.

```groovy
NotCharClass("abc")  // → [^abc]
```

#### CharRange(char, char)
Matches a range of characters.

```groovy
CharRange('a' as char, 'z' as char)  // → [a-z]
CharRange('A' as char, 'Z' as char)  // → [A-Z]
CharRange('0' as char, '9' as char)  // → [0-9]
```

#### MultiRange(List)
Combines multiple character ranges into a single character class.

```groovy
def alphanumeric = MultiRange([
    CharRange('a' as char, 'z' as char),
    CharRange('A' as char, 'Z' as char),
    CharRange('0' as char, '9' as char)
])  // → [a-zA-Z0-9]
```

### Anchors

#### StartOfLine()
Matches the start of a line.

```groovy
StartOfLine()  // → ^
```

#### EndOfLine()
Matches the end of a line.

```groovy
EndOfLine()  // → $
```

### Method Chaining

All pattern objects support fluent method chaining:

```groovy
// Make a pattern optional
Literal("test").optional()  // → (test)?

// Chain patterns together
Literal("hello").then(Literal(" ")).then(Literal("world"))

// Apply quantifiers
WordChar().oneOrMore()     // → (\w)+
Digit().exactly(3)         // → (\d){3}
Literal("a").range(2, 5)   // → (a){2,5}
```

## Examples

### Email Pattern
```groovy
include { Sequence; OneOrMore; Literal; Either; WordChar } from 'plugin/nf-pregex'

def emailPattern = Sequence([
    OneOrMore(WordChar()),
    Literal("@"),
    OneOrMore(WordChar()),
    Literal("."),
    Either(["com", "org", "edu"])
])
// → (\w)+@(\w)+\.(com|org|edu)
```

### FASTQ File Pattern
```groovy
include { Sequence; OneOrMore; Literal; Either; Optional; WordChar } from 'plugin/nf-pregex'

def fastqPattern = Sequence([
    OneOrMore(WordChar()),
    Literal("_"),
    Either(["R1", "R2"]),
    Literal(".fastq"),
    Optional(Literal(".gz"))
])
// → (\w)+_(R1|R2)\.fastq(\.gz)?

// Use with channel operations
channel.fromPath("data/*")
    .filter { it.name =~ /${fastqPattern}/ }
    .view()
```

### Sample ID Pattern
```groovy
include { Sequence; Literal; Either; Digit } from 'plugin/nf-pregex'

def samplePattern = Sequence([
    Literal("S"),
    Digit().exactly(3),
    Literal("_"),
    Either(["T", "C"]),
    Digit(),
    Literal("_"),
    Either(["R1", "R2"])
])
// → S(\d){3}_(T|C)(\d)_(R1|R2)
```

### VCF File with Optional Version
```groovy
include { Sequence; OneOrMore; Literal; Optional; Digit; WordChar } from 'plugin/nf-pregex'

def vcfPattern = Sequence([
    OneOrMore(WordChar()),
    Literal("."),
    Optional(Sequence([
        Literal("v"),
        OneOrMore(Digit()),
        Literal(".")
    ])),
    Literal("vcf"),
    Optional(Literal(".gz"))
])
// Matches: variants.vcf, variants.v1.vcf.gz, etc.
```

### Custom Identifier with Character Ranges
```groovy
include { Sequence; CharRange; MultiRange; Literal } from 'plugin/nf-pregex'

// Match plate well identifiers like A01, B12, H08
def wellPattern = Sequence([
    CharRange('A' as char, 'H' as char),     // Row: A-H
    CharRange('0' as char, '9' as char).exactly(2)  // Column: 00-99
])
// → [A-H]([0-9]){2}

// Match custom alphanumeric codes
def codePattern = Sequence([
    MultiRange([
        CharRange('A' as char, 'Z' as char),
        CharRange('0' as char, '9' as char)
    ]).exactly(3),
    Literal("-"),
    MultiRange([
        CharRange('a' as char, 'z' as char),
        CharRange('0' as char, '9' as char)
    ]).exactly(4)
])
// → ([A-Z0-9]){3}-([a-z0-9]){4}
// Matches: A1B-x9y2, 3XZ-test, etc.
```

## Benefits

- **Readability**: Patterns are self-documenting
- **Maintainability**: Easy to modify and understand
- **Type Safety**: Compile-time checking in Groovy
- **Reusability**: Build complex patterns from simpler components
- **Error Prevention**: Automatic escaping of special characters

## Testing

Run the test suite:

```bash
make test
```

Or with Gradle:

```bash
./gradlew test
```

## Building

Build the plugin:

```bash
make assemble
```

Install locally:

```bash
make install
```

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## License

This plugin is licensed under the Apache License 2.0.

## Credits

Inspired by the [pregex](https://github.com/manoss96/pregex) Python library by Manos Stoumpos.

## See Also

- [Nextflow Documentation](https://www.nextflow.io/docs/latest/)
- [Nextflow Plugin Development](https://www.nextflow.io/docs/latest/plugins/developing-plugins.html)
- [PRegEx Python Library](https://github.com/manoss96/pregex)

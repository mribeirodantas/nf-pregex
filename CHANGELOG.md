# Changelog

All notable changes to the nf-pregex plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **BioinformaticsPatterns library** - Comprehensive pre-built patterns for common bioinformatics use cases:
  - **Sequence patterns:**
    - `DNASequence()` - DNA sequences (ACGT, case-insensitive)
    - `StrictDNASequence()` - Uppercase DNA only
    - `DNASequenceWithAmbiguity()` - DNA with IUPAC ambiguity codes (N, R, Y, S, W, K, M, B, D, H, V)
    - `ProteinSequence()` - Protein sequences (20 standard amino acids, case-insensitive)
    - `StrictProteinSequence()` - Uppercase protein sequences
    - `ProteinSequenceWithAmbiguity()` - Protein with ambiguity codes (B, Z, X, *)
    - `PhredQuality()` - Phred quality scores (Phred+33 encoding)
  - **Genomic identifiers:**
    - `Chromosome()` - Flexible chromosome names (chr1-22, chrX/Y/M, with/without 'chr' prefix)
    - `StrictChromosome()` - Chromosome names requiring 'chr' prefix
    - `ReadPair()` - Paired-end read identifiers (_R1/_R2, _1/_2, .R1/.R2, .1/.2)
  - **File extensions (case-insensitive, with automatic .gz support):**
    - `FastqExtension()` - .fastq, .fq
    - `VcfExtension()` - .vcf, .bcf
    - `AlignmentExtension()` - .bam, .sam, .cram
    - `BedExtension()` - .bed
    - `GffGtfExtension()` - .gff, .gff3, .gtf
    - `FastaExtension()` - .fa, .fasta, .fna

### Fixed
- Fixed `CharClass` to properly escape dash characters in regex patterns
- Improved handling of special characters in character class patterns

### Documentation
- Added comprehensive bioinformatics patterns section to README.md
- Added detailed API documentation for all bioinformatics patterns
- Added practical examples for common bioinformatics use cases
- Documented all pattern methods with usage examples and supported formats

## [0.1.0] - 2025-12-26

### Added
- Initial release of nf-pregex plugin
- Pattern builder functions:
  - `Either(String...)` - Alternation patterns
  - `Literal(String)` - Literal text with automatic escaping
  - `Optional(PRegEx)` - Zero or one occurrences
  - `OneOrMore(PRegEx)` - One or more occurrences
  - `ZeroOrMore(PRegEx)` - Zero or more occurrences
  - `Exactly(PRegEx, int)` - Exact number of occurrences
  - `Range(PRegEx, int, int)` - Range of occurrences
  - `AtLeast(PRegEx, int)` - Minimum occurrences
  - `Sequence(PRegEx...)` - Pattern concatenation
- Character class functions:
  - `AnyChar()` - Any single character
  - `Digit()` - Digit characters (0-9)
  - `WordChar()` - Word characters (a-z, A-Z, 0-9, _)
  - `Whitespace()` - Whitespace characters
  - `CharClass(String)` - Custom character class
  - `NotCharClass(String)` - Negated character class
- Anchor functions:
  - `StartOfLine()` - Start of line anchor
  - `EndOfLine()` - End of line anchor
- Method chaining support for all pattern types
- Comprehensive unit test suite
- Example workflows demonstrating plugin usage
- Complete documentation and API reference

### Features
- Human-readable regex pattern building
- Automatic escaping of special characters
- Type-safe pattern composition
- Integration with Nextflow channel operations
- Compatible with Nextflow 25.04+

[0.1.0]: https://github.com/seqera-ai/nf-pregex/releases/tag/v0.1.0

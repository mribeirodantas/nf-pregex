# Fix: Add Missing Extension Functions for CharRange, MultiRange, and BioinformaticsPatterns

## Problem

The documentation (README.md, docs/API.md, docs/TUTORIAL.md) showed that users could import CharRange, MultiRange, and BioinformaticsPatterns functions using `include` statements:

```groovy
include { CharRange; MultiRange; DNASequence; FastqExtension } from 'plugin/nf-pregex'
```

However, these functions were NOT exposed in `PRegExExtension.groovy`, making them unavailable to users despite being documented.

## Critical Issues Fixed

1. **CharRange and MultiRange missing** - These pattern builders were implemented in PRegEx.groovy but not exposed as extension functions
2. **All 16 BioinformaticsPatterns functions missing** - DNA/protein sequences, chromosome names, read pairs, and file extensions were implemented but not accessible via include statements

## Changes Made

### 1. PRegExExtension.groovy
Added 18 new extension functions:

#### Character Range Functions (2)
- `CharRange(String start, String end)` - Creates character range patterns like [a-z], [0-9]
- `MultiRange(String rangeSpec)` - Combines multiple ranges like [a-zA-Z0-9]

#### Bioinformatics Pattern Functions (16)
**Sequence Patterns:**
- `DNASequence()` - ACGT sequences (case-insensitive)
- `StrictDNASequence()` - Uppercase ACGT only
- `DNASequenceWithAmbiguity()` - ACGT + IUPAC codes
- `ProteinSequence()` - 20 amino acids (case-insensitive)
- `StrictProteinSequence()` - Uppercase amino acids
- `ProteinSequenceWithAmbiguity()` - Amino acids + ambiguity codes
- `PhredQuality()` - Phred+33 quality scores

**Genomic Identifiers:**
- `Chromosome()` - chr1-22, chrX, chrY, chrM (flexible)
- `StrictChromosome()` - Requires 'chr' prefix
- `ReadPair()` - _R1, _R2, _1, _2, .R1, .R2, .1, .2

**File Extensions:**
- `FastqExtension()` - .fastq, .fq (with optional .gz)
- `VcfExtension()` - .vcf, .bcf (with optional .gz)
- `AlignmentExtension()` - .bam, .sam, .cram
- `BedExtension()` - .bed, .bed.gz
- `GffGtfExtension()` - .gff, .gff3, .gtf (with optional .gz)
- `FastaExtension()` - .fa, .fasta, .fna (with optional .gz)

### 2. PRegExExtensionTest.groovy
Added comprehensive test coverage for all new functions:

- CharRange tests (3 tests) - basic ranges, digits, with quantifiers
- MultiRange tests (2 tests) - multiple ranges, with quantifiers
- BioinformaticsPatterns tests (14 tests) - one for each pattern function
- Integration test - complex pattern combining CharRange and BioinformaticsPatterns

**New test count:** 19 additional tests
**Total test count:** 284 tests (was 265, now 284)
**All tests pass:** ✅ 0 failures

## Verification

✅ All 284 tests pass  
✅ CharRange and MultiRange now available via include  
✅ All BioinformaticsPatterns functions now available via include  
✅ Documentation examples now match actual implementation  
✅ No breaking changes to existing functionality

## Examples Now Working

```groovy
#!/usr/bin/env nextflow

// Now works as documented!
include { 
    CharRange
    MultiRange
    DNASequence
    FastqExtension
    ReadPair
    Chromosome
} from 'plugin/nf-pregex'

workflow {
    // Match plate well identifiers (A01, B12, H08)
    def wellPattern = Sequence([
        CharRange('A', 'H'),
        CharRange('0', '9').exactly(2)
    ])
    
    // Match custom alphanumeric codes (ABC-xyz9)
    def codePattern = Sequence([
        MultiRange("'A'-'Z', '0'-'9'").exactly(3),
        Literal("-"),
        MultiRange("'a'-'z', '0'-'9'").exactly(4)
    ])
    
    // Match FASTQ files with DNA sequences
    def fastqPattern = Sequence([
        DNASequence(),
        ReadPair(),
        FastqExtension()
    ])
}
```

## Impact

This fix makes the plugin fully functional as documented. Users can now use all documented features via `include` statements as shown in README, API documentation, and tutorials.

## Files Changed

- `src/main/groovy/io/nextflow/pregex/PRegExExtension.groovy` - Added 18 extension functions
- `src/test/groovy/io/nextflow/pregex/PRegExExtensionTest.groovy` - Added 19 tests

## Testing

```bash
./gradlew test
# BUILD SUCCESSFUL
# 284 tests completed, 0 failures
```

# nf-pregex API Documentation

Complete API reference for all PRegEx pattern builders.

## Table of Contents

- [Pattern Builders](#pattern-builders)
- [Quantifiers](#quantifiers)
- [Character Classes](#character-classes)
- [Anchors](#anchors)
- [Method Chaining](#method-chaining)
- [Bioinformatics Patterns](#bioinformatics-patterns)
  - [Sequence Patterns](#sequence-patterns)
  - [Genomic Identifiers](#genomic-identifiers)
  - [File Extensions](#file-extensions)
- [Usage Patterns](#usage-patterns)

## Pattern Builders

### Either(List)

Creates an alternation pattern that matches any of the provided alternatives.

**Syntax:**
```groovy
Either(List alternatives)
```

**Parameters:**
- `alternatives` - List of string alternatives (at least one required)

**Returns:** PRegEx pattern object

**Examples:**
```groovy
Either(["foo", "bar"])              // → (foo|bar)
Either(["R1", "R2", "R3"])          // → (R1|R2|R3)
Either(["fastq", "fq"])             // → (fastq|fq)
```

**Notes:**
- Special regex characters in alternatives are automatically escaped
- Single alternative returns the literal without alternation syntax
- Empty alternatives list throws `IllegalArgumentException`

---

### Literal(String)

Matches literal text with all special regex characters automatically escaped.

**Syntax:**
```groovy
Literal(String text)
```

**Parameters:**
- `text` - The literal text to match

**Returns:** PRegEx pattern object

**Examples:**
```groovy
Literal("file.txt")               // → file\.txt
Literal("a+b*c")                  // → a\+b\*c
Literal("(test)")                 // → \(test\)
Literal("$10.00")                 // → \$10\.00
```

**Escaped Characters:**
```
. * + ? ^ $ { } ( ) [ ] | \
```

---

### Sequence(List)

Concatenates multiple patterns in order.

**Syntax:**
```groovy
Sequence(List patterns)
```

**Parameters:**
- `patterns` - List of PRegEx patterns to concatenate

**Returns:** PRegEx pattern object

**Examples:**
```groovy
Sequence([
    Literal("hello"),
    Literal(" "),
    Literal("world")
])                                 // → hello world

Sequence([
    Literal("sample"),
    Digit(),
    Literal(".txt")
])                                 // → sample\d\.txt
```

---

## Quantifiers

### Optional(PRegEx)

Matches zero or one occurrence of a pattern.

**Syntax:**
```groovy
Optional(PRegEx pattern)
```

**Examples:**
```groovy
Optional(Literal("s"))            // → (s)?
Optional(Digit())                 // → (\d)?
```

---

### OneOrMore(PRegEx)

Matches one or more occurrences of a pattern.

**Syntax:**
```groovy
OneOrMore(PRegEx pattern)
```

**Examples:**
```groovy
OneOrMore(Digit())                // → (\d)+
OneOrMore(WordChar())             // → (\w)+
```

---

### ZeroOrMore(PRegEx)

Matches zero or more occurrences of a pattern.

**Syntax:**
```groovy
ZeroOrMore(PRegEx pattern)
```

**Examples:**
```groovy
ZeroOrMore(Whitespace())          // → (\s)*
ZeroOrMore(AnyChar())             // → (.)*
```

---

### Exactly(PRegEx, int)

Matches exactly n occurrences of a pattern.

**Syntax:**
```groovy
Exactly(PRegEx pattern, int count)
```

**Parameters:**
- `pattern` - The pattern to repeat
- `count` - Exact number of repetitions (must be ≥ 0)

**Examples:**
```groovy
Exactly(Digit(), 3)               // → (\d){3}
Exactly(WordChar(), 5)            // → (\w){5}
```

**Validation:**
- Throws `IllegalArgumentException` if count < 0

---

### Range(PRegEx, int, int)

Matches between min and max occurrences of a pattern.

**Syntax:**
```groovy
Range(PRegEx pattern, int min, int max)
```

**Parameters:**
- `pattern` - The pattern to repeat
- `min` - Minimum number of repetitions
- `max` - Maximum number of repetitions

**Examples:**
```groovy
Range(Digit(), 2, 4)              // → (\d){2,4}
Range(WordChar(), 1, 10)          // → (\w){1,10}
```

**Validation:**
- Throws `IllegalArgumentException` if min < 0 or max < min

---

### AtLeast(PRegEx, int)

Matches at least n occurrences of a pattern.

**Syntax:**
```groovy
AtLeast(PRegEx pattern, int min)
```

**Parameters:**
- `pattern` - The pattern to repeat
- `min` - Minimum number of repetitions

**Examples:**
```groovy
AtLeast(Digit(), 2)               // → (\d){2,}
AtLeast(WordChar(), 1)            // → (\w){1,}
```

**Validation:**
- Throws `IllegalArgumentException` if min < 0

---

## Character Classes

### AnyChar()

Matches any single character.

**Syntax:**
```groovy
AnyChar()
```

**Returns:** Pattern matching `.`

**Examples:**
```groovy
AnyChar()                         // → .
OneOrMore(AnyChar())              // → (.)+
```

---

### Digit()

Matches any digit character (0-9).

**Syntax:**
```groovy
Digit()
```

**Returns:** Pattern matching `\d`

**Examples:**
```groovy
Digit()                           // → \d
Exactly(Digit(), 4)               // → (\d){4}
```

---

### WordChar()

Matches any word character (a-z, A-Z, 0-9, _).

**Syntax:**
```groovy
WordChar()
```

**Returns:** Pattern matching `\w`

**Examples:**
```groovy
WordChar()                        // → \w
OneOrMore(WordChar())             // → (\w)+
```

---

### Whitespace()

Matches any whitespace character (space, tab, newline, etc.).

**Syntax:**
```groovy
Whitespace()
```

**Returns:** Pattern matching `\s`

**Examples:**
```groovy
Whitespace()                      // → \s
ZeroOrMore(Whitespace())          // → (\s)*
```

---

### CharClass(String)

Matches any character in the specified set.

**Syntax:**
```groovy
CharClass(String chars)
```

**Parameters:**
- `chars` - String containing characters to match

**Examples:**
```groovy
CharClass("abc")                  // → [abc]
CharClass("0-9")                  // → [0\-9]
CharClass("aeiou")                // → [aeiou]
```

**Notes:**
- Special characters `^`, `-`, and `]` are automatically escaped

---

### NotCharClass(String)

Matches any character NOT in the specified set.

**Syntax:**
```groovy
NotCharClass(String chars)
```

**Parameters:**
- `chars` - String containing characters to exclude

**Examples:**
```groovy
NotCharClass("abc")               // → [^abc]
NotCharClass("0-9")               // → [^0\-9]
```

---

### CharRange(String, String) or CharRange(char, char)

Matches a range of characters using standard regex character class syntax.

**Syntax:**
```groovy
CharRange(String start, String end)  // Recommended: cleaner syntax
CharRange(char start, char end)      // Alternative: explicit char type
```

**Parameters:**
- `start` - Starting character of the range (inclusive), either as single-character String or char
- `end` - Ending character of the range (inclusive), either as single-character String or char

**Returns:** PRegEx pattern object

**Examples:**
```groovy
// Recommended syntax (String constructor)
CharRange('a', 'z')    // → [a-z]
CharRange('A', 'Z')    // → [A-Z]
CharRange('0', '9')    // → [0-9]
CharRange('a', 'f')    // → [a-f] (hex digits)

// Alternative syntax (char constructor)
CharRange('a' as char, 'z' as char)    // → [a-z]
CharRange('A' as char, 'Z' as char)    // → [A-Z]
```

**Validation:**
- Throws `IllegalArgumentException` if start > end
- For String constructor: throws `IllegalArgumentException` if strings are not single characters
- Start character must be less than or equal to end character in ASCII value

**Usage with Quantifiers:**
```groovy
CharRange('a', 'z').oneOrMore()       // → ([a-z])+
CharRange('0', '9').exactly(3)        // → ([0-9]){3}
CharRange('A', 'F').range(2, 4)       // → ([A-F]){2,4}
```

**Notes:**
- More concise than CharClass for ranges
- String constructor provides cleaner API without type casting
- Produces standard regex syntax: `[start-end]`
- Works seamlessly with all quantifiers and combinators

---

### MultiRange(String) or MultiRange(List<CharRange>)

Combines multiple CharRange patterns into a single character class for efficient matching.

**Syntax:**
```groovy
MultiRange(String rangeSpec)           // Recommended: cleaner syntax
MultiRange(List<CharRange> ranges)     // Alternative: explicit CharRange objects
```

**Parameters:**
- `rangeSpec` - String specification of ranges in format `'a'-'z', 'A'-'Z', '0'-'9'` (single or double quotes)
- `ranges` - List of CharRange patterns to combine (at least one required)

**Returns:** PRegEx pattern object

**Examples:**
```groovy
// Recommended syntax (String constructor)
MultiRange("'a'-'z'")                           // → [a-z]
MultiRange("'a'-'z', 'A'-'Z'")                  // → [a-zA-Z]
MultiRange("'a'-'z', 'A'-'Z', '0'-'9'")         // → [a-zA-Z0-9]
MultiRange("'a'-'f', 'A'-'F', '0'-'9'")         // → [a-fA-F0-9] (hex)

// Also supports double quotes
MultiRange('"a"-"z", "A"-"Z"')                  // → [a-zA-Z]

// Alternative syntax (List constructor)
def lower = new CharRange('a', 'z')
def upper = new CharRange('A', 'Z')
def digits = new CharRange('0', '9')
MultiRange([lower, upper, digits])              // → [a-zA-Z0-9]
```

**Validation:**
- String constructor: throws `IllegalArgumentException` if spec is null, empty, or contains no valid ranges
- List constructor: throws `IllegalArgumentException` if ranges list is null or empty
- At least one valid range is required

**Usage with Quantifiers:**
```groovy
def alphanumeric = MultiRange("'a'-'z', 'A'-'Z', '0'-'9'")

alphanumeric.oneOrMore()          // → ([a-zA-Z0-9])+
alphanumeric.exactly(5)           // → ([a-zA-Z0-9]){5}
alphanumeric.range(3, 8)          // → ([a-zA-Z0-9]){3,8}
```

**Combining with Other Patterns:**
```groovy
// Username pattern: letters followed by digits
def letters = MultiRange("'a'-'z', 'A'-'Z'")
def digits = CharRange('0', '9')

Sequence([
    letters.oneOrMore(),
    digits.exactly(3)
])                                // → ([a-zA-Z])+([0-9]){3}
```

**Real-World Examples:**
```groovy
// Match plate well identifiers (A01-H12)
Sequence([
    CharRange('A', 'H'),
    CharRange('0', '9').exactly(2)
])                                // → [A-H]([0-9]){2}

// Match custom alphanumeric codes (ABC-1234)
Sequence([
    MultiRange("'A'-'Z', '0'-'9'").exactly(3),
    Literal("-"),
    MultiRange("'a'-'z', '0'-'9'").exactly(4)
])                                // → ([A-Z0-9]){3}-([a-z0-9]){4}
```

**Notes:**
- String constructor provides the cleanest syntax for most use cases
- More efficient than multiple Either patterns for character classes
- Produces cleaner, more readable regex
- String format supports both single and double quotes
- Standard regex syntax understood by all regex engines
- Excellent for validating identifiers, codes, and custom formats

---

## Anchors

### StartOfLine()

Matches the start of a line or string.

**Syntax:**
```groovy
StartOfLine()
```

**Returns:** Pattern matching `^`

**Examples:**
```groovy
Sequence([
    StartOfLine(),
    Literal("hello")
])                                 // → ^hello
```

---

### EndOfLine()

Matches the end of a line or string.

**Syntax:**
```groovy
EndOfLine()
```

**Returns:** Pattern matching `$`

**Examples:**
```groovy
Sequence([
    Literal("world"),
    EndOfLine()
])                                 // → world$
```

---

## Method Chaining

All PRegEx objects support fluent method chaining for convenient pattern composition.

### optional()

Makes the pattern optional (zero or one occurrence).

```groovy
Literal("test").optional()        // → (test)?
Digit().optional()                // → (\d)?
```

### oneOrMore()

Repeats the pattern one or more times.

```groovy
WordChar().oneOrMore()            // → (\w)+
AnyChar().oneOrMore()             // → (.)+
```

### zeroOrMore()

Repeats the pattern zero or more times.

```groovy
Whitespace().zeroOrMore()         // → (\s)*
```

### exactly(int n)

Repeats the pattern exactly n times.

```groovy
Digit().exactly(3)                // → (\d){3}
```

### range(int min, int max)

Repeats the pattern between min and max times.

```groovy
WordChar().range(2, 5)            // → (\w){2,5}
```

### atLeast(int n)

Repeats the pattern at least n times.

```groovy
Digit().atLeast(1)                // → (\d){1,}
```

### then(PRegEx other)

Chains this pattern with another pattern.

```groovy
Literal("hello")
    .then(Literal(" "))
    .then(Literal("world"))       // → hello world
```

### group()

Creates a capturing group from the pattern.

```groovy
Either(["foo", "bar"]).group()      // → ((foo|bar))
```

---

## Bioinformatics Patterns

The `BioinformaticsPatterns` class provides pre-built regex patterns specifically designed for common bioinformatics use cases. These patterns handle the complexity of matching biological sequences, file formats, and genomic identifiers.

### Sequence Patterns

#### DNASequence()

Matches DNA sequences containing ACGT nucleotides (case-insensitive).

**Syntax:**
```groovy
DNASequence()
```

**Returns:** PRegEx pattern matching one or more DNA nucleotides

**Examples:**
```groovy
include { DNASequence } from 'plugin/nf-pregex'

def dna = DNASequence()
// Matches: ACGT, acgt, ATCGATCG, ACGTacgt
```

**Valid Characters:** A, C, G, T (case-insensitive)

---

#### StrictDNASequence()

Matches DNA sequences with uppercase nucleotides only.

**Syntax:**
```groovy
StrictDNASequence()
```

**Examples:**
```groovy
def strictDna = StrictDNASequence()
// Matches: ACGT, ATCGATCG
// Does NOT match: acgt, ACGTacgt
```

**Valid Characters:** A, C, G, T (uppercase only)

---

#### DNASequenceWithAmbiguity()

Matches DNA sequences including IUPAC ambiguity codes.

**Syntax:**
```groovy
DNASequenceWithAmbiguity()
```

**Examples:**
```groovy
def ambigDna = DNASequenceWithAmbiguity()
// Matches: ACGT, ACGTN, ACGTRYMKSW, ACGTBDHV
```

**Valid Characters:**
- Standard: A, C, G, T
- Ambiguity codes: R (A/G), Y (C/T), S (G/C), W (A/T), K (G/T), M (A/C)
- Wildcards: B (not A), D (not C), H (not G), V (not T), N (any)
- Case-insensitive

---

#### ProteinSequence()

Matches protein sequences with the 20 standard amino acids (case-insensitive).

**Syntax:**
```groovy
ProteinSequence()
```

**Examples:**
```groovy
def protein = ProteinSequence()
// Matches: ACDEFGHIKLMNPQRSTVWY, acdefghiklmnpqrstvwy, MVHLTPEEK
```

**Valid Characters:** A, C, D, E, F, G, H, I, K, L, M, N, P, Q, R, S, T, V, W, Y

---

#### StrictProteinSequence()

Matches protein sequences with uppercase amino acids only.

**Syntax:**
```groovy
StrictProteinSequence()
```

**Examples:**
```groovy
def strictProtein = StrictProteinSequence()
// Matches: ACDEFGHIKLMNPQRSTVWY, MVHLTPEEK
// Does NOT match: acdefg
```

---

#### ProteinSequenceWithAmbiguity()

Matches protein sequences including ambiguity codes.

**Syntax:**
```groovy
ProteinSequenceWithAmbiguity()
```

**Examples:**
```groovy
def ambigProtein = ProteinSequenceWithAmbiguity()
// Matches: MVHLTPEEKX, ACDEFGHBZX*
```

**Valid Characters:**
- Standard 20 amino acids
- B (Asx: Asp or Asn)
- Z (Glx: Glu or Gln)
- X (any amino acid)
- \* (stop codon)

---

#### PhredQuality()

Matches Phred quality scores using Phred+33 encoding.

**Syntax:**
```groovy
PhredQuality()
```

**Examples:**
```groovy
def quality = PhredQuality()
// Matches: !!!FFFFFF, IIIIIIIIII, ~~~~~~~~~~
```

**Character Range:** ASCII 33-126 (! through ~)

---

### Genomic Identifiers

#### Chromosome()

Matches chromosome names with flexible formatting.

**Syntax:**
```groovy
Chromosome()
```

**Examples:**
```groovy
def chr = Chromosome()
// Matches: chr1, chr22, chrX, chrY, chrM, 1, 22, X, Y, M
```

**Supported Formats:**
- With prefix: chr1-chr22, chrX, chrY, chrM (case-insensitive X/Y/M)
- Without prefix: 1-22, X, Y, M (case-insensitive X/Y/M)

---

#### StrictChromosome()

Matches chromosome names requiring the 'chr' prefix.

**Syntax:**
```groovy
StrictChromosome()
```

**Examples:**
```groovy
def strictChr = StrictChromosome()
// Matches: chr1, chr22, chrX, chrY, chrM
// Does NOT match: 1, 22, X
```

**Supported Formats:** chr1-chr22, chrX, chrY, chrM (case-insensitive X/Y/M)

---

#### ReadPair()

Matches paired-end read identifiers.

**Syntax:**
```groovy
ReadPair()
```

**Examples:**
```groovy
def readPair = ReadPair()
// Matches: _R1, _R2, .R1, .R2, _1, _2, .1, .2
```

**Supported Patterns:**
- Underscore separator: `_R1`, `_R2`, `_1`, `_2`
- Dot separator: `.R1`, `.R2`, `.1`, `.2`

---

### File Extensions

All file extension patterns support case-insensitive matching and handle compressed files (.gz) automatically where applicable.

#### FastqExtension()

Matches FASTQ file extensions.

**Syntax:**
```groovy
FastqExtension()
```

**Examples:**
```groovy
def fastq = FastqExtension()
// Matches: .fastq, .fq, .fastq.gz, .fq.gz, .FASTQ, .FQ.GZ
```

**Supported Extensions:** .fastq, .fq (with optional .gz compression)

---

#### VcfExtension()

Matches VCF file extensions.

**Syntax:**
```groovy
VcfExtension()
```

**Examples:**
```groovy
def vcf = VcfExtension()
// Matches: .vcf, .vcf.gz, .bcf, .VCF, .BCF
```

**Supported Extensions:** .vcf, .bcf (with optional .gz for VCF)

---

#### AlignmentExtension()

Matches alignment file extensions.

**Syntax:**
```groovy
AlignmentExtension()
```

**Examples:**
```groovy
def alignment = AlignmentExtension()
// Matches: .bam, .sam, .cram, .BAM, .SAM, .CRAM
```

**Supported Extensions:** .bam, .sam, .cram

---

#### BedExtension()

Matches BED file extensions.

**Syntax:**
```groovy
BedExtension()
```

**Examples:**
```groovy
def bed = BedExtension()
// Matches: .bed, .bed.gz, .BED, .BED.GZ
```

**Supported Extensions:** .bed (with optional .gz compression)

---

#### GffGtfExtension()

Matches GFF/GTF file extensions.

**Syntax:**
```groovy
GffGtfExtension()
```

**Examples:**
```groovy
def annotation = GffGtfExtension()
// Matches: .gff, .gff3, .gtf, .gff.gz, .gff3.gz, .gtf.gz
```

**Supported Extensions:** .gff, .gff3, .gtf (with optional .gz compression)

---

#### FastaExtension()

Matches FASTA file extensions.

**Syntax:**
```groovy
FastaExtension()
```

**Examples:**
```groovy
def fasta = FastaExtension()
// Matches: .fa, .fasta, .fna, .fa.gz, .fasta.gz, .fna.gz
```

**Supported Extensions:** .fa, .fasta, .fna (with optional .gz compression)

---

### Bioinformatics Pattern Examples

#### Complete FASTQ Filename Matching

```groovy
include { 
    Sequence
    OneOrMore
    WordChar
    ReadPair
    FastqExtension
} from 'plugin/nf-pregex'

// Match: sample_R1.fastq.gz, control_R2.fq, test.1.fastq.gz
def fastqPattern = Sequence(
    OneOrMore(WordChar()),  // Sample name
    ReadPair(),             // _R1, _R2, etc.
    FastqExtension()        // .fastq.gz, .fq, etc.
)
```

#### VCF Files with Chromosome Information

```groovy
include {
    Sequence
    Chromosome
    Literal
    OneOrMore
    WordChar
    VcfExtension
} from 'plugin/nf-pregex'

// Match: chr1_variants.vcf.gz, 22_filtered.bcf, chrX_calls.vcf
def vcfPattern = Sequence(
    Chromosome(),           // chr1, chrX, 22, etc.
    Literal("_"),
    OneOrMore(WordChar()),  // Description
    VcfExtension()          // .vcf, .vcf.gz, .bcf
)
```

#### DNA Sequence Validation in Workflow

```groovy
include { DNASequenceWithAmbiguity } from 'plugin/nf-pregex'

workflow {
    def dnaPattern = DNASequenceWithAmbiguity()
    
    channel.of("ACGTACGT", "ACGTN", "ACGTRYSWKMN", "INVALID123")
        .filter { it =~ /${dnaPattern}/ }
        .view { "Valid DNA: ${it}" }
}
```

#### Comprehensive File Filtering

```groovy
include {
    Either
    Sequence
    OneOrMore
    WordChar
    FastqExtension
    VcfExtension
    AlignmentExtension
} from 'plugin/nf-pregex'

workflow {
    // Match any sequencing data file
    def sequencingFile = Sequence(
        OneOrMore(WordChar()),
        Either(
            FastqExtension(),
            VcfExtension(),
            AlignmentExtension()
        )
    )
    
    channel.fromPath("data/*")
        .filter { it.name =~ /${sequencingFile}/ }
        .view { "Found: ${it.name}" }
}
```

---

## Usage Patterns

### Email Validation

```groovy
def emailPattern = Sequence([
    OneOrMore(WordChar()),
    Literal("@"),
    OneOrMore(WordChar()),
    Literal("."),
    Range(WordChar(), 2, 3)
])
// → (\w)+@(\w)+\.(\w){2,3}
```

### FASTQ Files

```groovy
def fastqPattern = Sequence([
    OneOrMore(WordChar()),
    Literal("_"),
    Either(["R1", "R2"]),
    Literal(".fastq"),
    Optional(Literal(".gz"))
])
// → (\w)+_(R1|R2)\.fastq(\.gz)?
```

### Sample IDs

```groovy
def sampleIdPattern = Sequence([
    Literal("sample"),
    Digit().exactly(3),
    Literal("_"),
    Either(["control", "treatment"])
])
// → sample(\d){3}_(control|treatment)
```

### Phone Numbers

```groovy
def phonePattern = Sequence([
    Optional(Literal("+")),
    Digit().range(10, 15)
])
// → (\+)?(\\d){10,15}
```

### IP Address (Simple)

```groovy
def ipPattern = Sequence([
    Digit().range(1, 3),
    Literal("."),
    Digit().range(1, 3),
    Literal("."),
    Digit().range(1, 3),
    Literal("."),
    Digit().range(1, 3)
])
// → (\d){1,3}\.(\d){1,3}\.(\d){1,3}\.(\d){1,3}
```

---

## Integration with Nextflow

### Using Patterns in Channel Operations

```groovy
include { Sequence; Literal; Either; OneOrMore; WordChar } from 'plugin/nf-pregex'

workflow {
    def pattern = Sequence([
        OneOrMore(WordChar()),
        Literal("_"),
        Either(["R1", "R2"]),
        Literal(".fastq.gz")
    ])
    
    // Filter files
    channel.fromPath("data/*")
        .filter { it.name =~ /${pattern}/ }
        .view()
    
    // Extract information
    channel.fromPath("data/*")
        .map { file ->
            def matcher = file.name =~ /${pattern}/
            matcher ? [file, matcher[0]] : null
        }
        .filter { it != null }
        .view()
}
```

### Converting Patterns to Strings

All patterns can be converted to standard regex strings:

```groovy
def pattern = Either("foo", "bar")
def regex = pattern.toString()    // "(foo|bar)"
def regex2 = pattern.toRegex()    // "(foo|bar)"
```

---

## Error Handling

The plugin provides clear error messages for common mistakes:

```groovy
// Empty alternatives
Either([])                          // IllegalArgumentException

// Negative counts
Exactly(Digit(), -1)              // IllegalArgumentException

// Invalid ranges
Range(Digit(), 5, 2)              // IllegalArgumentException
```

---

## Performance Considerations

- Pattern objects are immutable and can be safely reused
- Regex compilation happens at runtime when the pattern is used
- For frequently used patterns, consider caching the compiled regex:

```groovy
def pattern = Either("foo", "bar")
def regex = ~/${pattern}/         // Compiled regex pattern

// Reuse the compiled pattern
files.each { file ->
    if (file =~ regex) {
        // Process file
    }
}
```

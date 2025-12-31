# nf-pregex

A Nextflow plugin that provides human-readable regex pattern builders, inspired by Python's [pregex](https://github.com/manoss96/pregex) library.

## Summary

Regular expressions are powerful but notoriously difficult to read and maintain. The nf-pregex plugin solves this by providing an intuitive, chainable API for building regex patterns using method calls instead of cryptic syntax. This makes patterns self-documenting, easier to debug, and simpler to modify.

### Why nf-pregex?

**Traditional regex is cryptic and error-prone:**
```groovy
// What does this match? 🤔
def emailPattern = /^[\w\.-]+@[\w\.-]+\.(com|org|edu)$/
```

**nf-pregex is readable and self-documenting:**
```groovy
include { Sequence; StartOfLine; OneOrMore; WordChar; Literal; Either; EndOfLine } from 'plugin/nf-pregex'

def emailPattern = Sequence([
    StartOfLine(),
    OneOrMore(WordChar()),
    Literal("@"),
    OneOrMore(WordChar()),
    Literal("."),
    Either(["com", "org", "edu"]),
    EndOfLine()
])
// Now it's crystal clear! ✓
```

### Real-World Bioinformatics Example

**Without nf-pregex** (traditional regex):
```groovy
// Match Illumina paired-end FASTQ files with sample ID, lane, and read info
// What's the pattern here? Hard to tell! 😵
def pattern = /^([A-Za-z0-9_-]+)_(L\d{3})_(R[12])_\d{3}\.fastq\.gz$/

channel.fromPath("data/*.fastq.gz")
    .map { file ->
        def matcher = file.name =~ pattern
        if (matcher.matches()) {
            [
                sample: matcher.group(1),
                lane: matcher.group(2),
                read: matcher.group(3),
                file: file
            ]
        }
    }
```

**With nf-pregex** (readable and maintainable):
```groovy
include { 
    Sequence; StartOfLine; Group; OneOrMore; MultiRange
    Literal; Digit; Either; EndOfLine; FastqExtension 
} from 'plugin/nf-pregex'

// Build the pattern step by step - easy to understand and modify! 😊
def pattern = Sequence([
    StartOfLine(),
    Group('sample', OneOrMore(MultiRange("'A'-'Z', 'a'-'z', '0'-'9', '_', '-'"))),
    Literal("_"),
    Group('lane', Sequence([Literal("L"), Digit().exactly(3)])),
    Literal("_"),
    Group('read', Sequence([Literal("R"), Either(["1", "2"])])),
    Literal("_"),
    Digit().exactly(3),
    FastqExtension(),
    EndOfLine()
])

channel.fromPath("data/*.fastq.gz")
    .map { file ->
        def matcher = file.name =~ pattern
        if (matcher.matches()) {
            [
                sample: matcher.group('sample'),  // Named groups!
                lane: matcher.group('lane'),
                read: matcher.group('read'),
                file: file
            ]
        }
    }
```

### Benefits

✅ **Readable** - Patterns are self-documenting and obvious  
✅ **Maintainable** - Easy to modify without breaking  
✅ **Reusable** - Build complex patterns from simple components  
✅ **Safe** - Automatic escaping prevents regex injection  
✅ **Bioinformatics-Ready** - Pre-built patterns for FASTQ, VCF, BAM, and more  
✅ **Named Groups** - Extract data with descriptive names instead of numeric indices

## Get Started

### Prerequisites

- Nextflow 23.04.0 or later
- Java 11 or later

Check your versions:
```bash
nextflow -version
java -version
```

### Installation

The nf-pregex plugin is available in the [Nextflow Plugin Registry](https://www.nextflow.io/plugins.html) and can be enabled in three ways:

#### Method 1: Configuration File (Recommended)

Add the plugin to your `nextflow.config`:

```groovy
plugins {
    id 'nf-pregex@0.1.0'
}
```

Then run your pipeline normally:
```bash
nextflow run main.nf
```

#### Method 2: Command Line

Use the plugin without modifying your config:

```bash
nextflow run main.nf -plugins nf-pregex@0.1.0
```

#### Method 3: Environment Variable

Set the plugin globally for all pipeline runs:

```bash
export NXF_PLUGINS_DEFAULT=nf-pregex@0.1.0
nextflow run main.nf
```

### Quick Start Example

Create a simple workflow to test the plugin:

**`test.nf`:**
```groovy
#!/usr/bin/env nextflow

plugins {
    id 'nf-pregex@0.1.0'
}

include { Sequence; Literal; Either; OneOrMore; WordChar } from 'plugin/nf-pregex'

workflow {
    // Create a simple email pattern
    def pattern = Sequence([
        OneOrMore(WordChar()),
        Literal("@"),
        OneOrMore(WordChar()),
        Literal("."),
        Either(["com", "org", "edu"])
    ])
    
    println "Pattern: ${pattern}"
    
    // Test with some emails
    def emails = ["test@example.com", "user@domain.org", "invalid@test.xyz"]
    emails.each { email ->
        def matches = email =~ /${pattern}/
        println "${email}: ${matches ? '✓ MATCH' : '✗ NO MATCH'}"
    }
}
```

Run it:
```bash
nextflow run test.nf
```

Expected output:
```
Pattern: (\w)+@(\w)+\.(com|org|edu)
test@example.com: ✓ MATCH
user@domain.org: ✓ MATCH
invalid@test.xyz: ✗ NO MATCH
```

### Verification

Verify the plugin is installed correctly:

```bash
nextflow info
```

The output should list `nf-pregex` in the plugins section.

### Troubleshooting

**Plugin not found?**
- Ensure you're using Nextflow version 23.04.0 or later: `nextflow -version`
- Check plugin spelling and version: `nf-pregex@0.1.0`
- Try clearing the Nextflow cache: `rm -rf .nextflow/`

**Import errors?**
- Verify the import path: `include { ... } from 'plugin/nf-pregex'`
- Ensure the plugin is loaded before the include statement (add `plugins { id 'nf-pregex@0.1.0' }` to your script)

## Examples

### Example 1: Email Validation

**Problem:** Validate email addresses with common TLDs.

**Traditional Regex:**
```groovy
// Cryptic and hard to modify 😵
def emailPattern = /(\w)+@(\w)+\.(com|org|edu)/
```

**With nf-pregex:**
```groovy
include { Sequence; OneOrMore; Literal; Either; WordChar } from 'plugin/nf-pregex'

// Self-documenting and easy to understand! 😊
def emailPattern = Sequence([
    OneOrMore(WordChar()),          // Username
    Literal("@"),                   // @ symbol
    OneOrMore(WordChar()),          // Domain
    Literal("."),                   // Dot
    Either(["com", "org", "edu"])   // TLD options
])
// → (\w)+@(\w)+\.(com|org|edu)

// Use it
"user@example.com" =~ /${emailPattern}/  // ✓ matches
```

### Example 2: Paired-End FASTQ Files

**Problem:** Match and parse Illumina paired-end FASTQ files.

**Traditional Regex:**
```groovy
// What are we matching here? 🤔
def pattern = /(\w)+_(R1|R2)\.fastq(\.gz)?/
```

**With nf-pregex:**
```groovy
include { 
    Sequence; OneOrMore; WordChar; Literal
    Either; Optional; FastqExtension 
} from 'plugin/nf-pregex'

// Crystal clear! ✨
def fastqPattern = Sequence([
    OneOrMore(WordChar()),          // Sample name
    Literal("_"),                   // Underscore
    Either(["R1", "R2"]),           // Read pair indicator
    Literal(".fastq"),              // Extension
    Optional(Literal(".gz"))        // Optional compression
])
// → (\w)+_(R1|R2)\.fastq(\.gz)?

// Use in a Nextflow channel
channel.fromPath("data/*.fastq.gz")
    .filter { it.name =~ /${fastqPattern}/ }
    .view { "Processing: ${it.name}" }
```

### Example 3: Complex Bioinformatics Pipeline

**Problem:** Parse Illumina filenames with sample ID, lane, and read information.

**Traditional Regex:**
```groovy
// Completely unreadable! 😱
def pattern = /^([A-Za-z0-9_-]+)_(L\d{3})_(R[12])_\d{3}\.fastq\.gz$/

channel.fromPath("data/*.fastq.gz")
    .map { file ->
        def matcher = file.name =~ pattern
        if (matcher.matches()) {
            [matcher.group(1), matcher.group(2), matcher.group(3), file]
        }
    }
```

**With nf-pregex:**
```groovy
include { 
    Sequence; StartOfLine; EndOfLine; Group; OneOrMore
    MultiRange; Literal; Digit; Either; FastqExtension 
} from 'plugin/nf-pregex'

// Readable, maintainable, and using named groups! 🎉
def pattern = Sequence([
    StartOfLine(),
    Group('sample', OneOrMore(MultiRange("'A'-'Z', 'a'-'z', '0'-'9', '_', '-'"))),
    Literal("_"),
    Group('lane', Sequence([Literal("L"), Digit().exactly(3)])),
    Literal("_"),
    Group('read', Sequence([Literal("R"), Either(["1", "2"])])),
    Literal("_"),
    Digit().exactly(3),
    FastqExtension(),
    EndOfLine()
])

// Now you can use descriptive names instead of numbers!
channel.fromPath("data/*.fastq.gz")
    .map { file ->
        def matcher = file.name =~ pattern
        if (matcher.matches()) {
            [
                sample: matcher.group('sample'),  // Much better than group(1)!
                lane: matcher.group('lane'),
                read: matcher.group('read'),
                file: file
            ]
        }
    }
    .view { "Sample: ${it.sample}, Lane: ${it.lane}, Read: ${it.read}" }
```

### Example 4: VCF File Matching

**Problem:** Match VCF files with optional version numbers and compression.

**With nf-pregex:**
```groovy
include { 
    Sequence; OneOrMore; WordChar; Literal
    Optional; Digit; VcfExtension 
} from 'plugin/nf-pregex'

def vcfPattern = Sequence([
    OneOrMore(WordChar()),              // Base filename
    Optional(Sequence([                 // Optional version
        Literal(".v"),
        OneOrMore(Digit())
    ])),
    VcfExtension()                      // .vcf, .vcf.gz, .bcf
])

// Matches: variants.vcf, calls.v2.vcf.gz, filtered.bcf
channel.fromPath("vcf/*.{vcf,vcf.gz,bcf}")
    .filter { it.name =~ /${vcfPattern}/ }
    .view()
```

### Example 5: Using Pre-Built Bioinformatics Patterns

**Problem:** Quickly match common bioinformatics file types.

```groovy
include { 
    ReadPair; FastqExtension; AlignmentExtension
    VcfExtension; Chromosome; DNASequence
    Sequence; OneOrMore; WordChar; Literal
} from 'plugin/nf-pregex'

workflow {
    // Match paired FASTQ files - super easy!
    def fastqPattern = Sequence([
        OneOrMore(WordChar()),
        ReadPair(),           // Matches: _R1, _R2, .1, .2, etc.
        FastqExtension()      // Matches: .fastq, .fq, .fastq.gz, etc.
    ])
    
    // Match alignment files
    def bamPattern = Sequence([
        OneOrMore(WordChar()),
        AlignmentExtension()  // Matches: .bam, .sam, .cram
    ])
    
    // Match VCF with chromosome prefix
    def vcfPattern = Sequence([
        Chromosome(),         // Matches: chr1-22, chrX, chrY, chrM
        Literal("_variants"),
        VcfExtension()        // Matches: .vcf, .vcf.gz, .bcf
    ])
    
    // Validate DNA sequences
    def dnaValidator = DNASequence()
    "ACGTACGT" =~ /${dnaValidator}/  // ✓ valid
    "ACGTXYZ" =~ /${dnaValidator}/   // ✗ invalid
}
```

### More Examples

For comprehensive examples including:
- File matching and filtering
- Named capture groups
- Validation and debugging
- Complete RNA-seq pipeline integration

See the [examples directory](examples/) in the repository.

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
    def fastqFiles = Sequence([
        OneOrMore(WordChar()),
        ReadPair(),
        FastqExtension()
    ])
    
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

def pairedEndPattern = Sequence([
    OneOrMore(WordChar()),  // sample name
    ReadPair(),             // _R1, _R2, etc.
    FastqExtension()        // .fastq.gz, .fq, etc.
])

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

#### CharRange(String, String)
Matches a range of characters.

```groovy
CharRange('a', 'z')  // → [a-z]
CharRange('A', 'Z')  // → [A-Z]
CharRange('0', '9')  // → [0-9]
```

#### MultiRange(String)
Combines multiple character ranges into a single character class.

```groovy
MultiRange("'a'-'z', 'A'-'Z', '0'-'9'")  // → [a-zA-Z0-9]
MultiRange("'a'-'f', 'A'-'F', '0'-'9'")  // → [a-fA-F0-9] (hex)
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

### Capturing Groups

#### Group(PRegEx)
Creates a capturing group that can be referenced by numeric index.

```groovy
def pattern = Sequence([
    Group(OneOrMore(Digit())),
    Literal("-"),
    Group(OneOrMore(WordChar()))
])
// → (\d)+-(\w)+

def matcher = "123-abc" =~ pattern
matcher.group(1)  // → "123"
matcher.group(2)  // → "abc"
```

#### Group(String, PRegEx)
Creates a named capturing group that can be referenced by name or numeric index.
Group names must start with a letter and contain only alphanumeric characters.

```groovy
def pattern = Sequence([
    Group('samplename', OneOrMore(AnyChar())),
    CharClass('._'),
    Group('rp', ReadPair()),
    Literal('.fastq.gz')
])
// → (?<samplename>(?:.)+)[._](?<rp>(?:R1|R2|1|2))\.fastq\.gz

def filename = "sample_123_R1.fastq.gz"
def matcher = filename =~ pattern
matcher.group('samplename')  // → "sample_123"
matcher.group('rp')          // → "R1"

// Can also access by numeric index
matcher.group(1)  // → "sample_123"
matcher.group(2)  // → "R1"
```

**Practical Bioinformatics Example:**
```groovy
include { Sequence; Group; Literal; OneOrMore; WordChar; Digit; ReadPair; FastqExtension } from 'plugin/nf-pregex'

// Pattern to extract sample ID, lane, and read info from Illumina filenames
def illuminaPattern = Sequence([
    Group('sample', OneOrMore(WordChar())),
    Literal('_'),
    Group('lane', Literal('L').then(Digit().exactly(3))),
    Literal('_'),
    Group('read', ReadPair()),
    FastqExtension()
])

// Parse filenames in a channel
channel.fromPath('data/*.fastq.gz')
    .map { file ->
        def matcher = file.name =~ illuminaPattern
        if (matcher.matches()) {
            [
                sample: matcher.group('sample'),
                lane: matcher.group('lane'),
                read: matcher.group('read'),
                file: file
            ]
        }
    }
    .view()
// Output: [sample:SampleA, lane:L001, read:R1, file:/path/to/SampleA_L001_R1.fastq.gz]
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



## License

```
Copyright 2024 nf-pregex contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

This plugin is licensed under the **Apache License 2.0**, a permissive open-source license that allows you to freely use, modify, and distribute the software in both open-source and proprietary projects.

**Key Permissions:**
- ✅ Commercial use
- ✅ Modification
- ✅ Distribution
- ✅ Patent use
- ✅ Private use

**Requirements:**
- 📄 Include a copy of the license
- 📄 State significant changes
- 📄 Include original copyright notice

See the [LICENSE](LICENSE) file for the complete license text.

---

## Contributing

Contributions are welcome! Whether you want to:
- 🐛 Report a bug
- 💡 Suggest a feature
- 📖 Improve documentation
- 🔧 Submit a pull request

Please feel free to open an issue or pull request on GitHub.

## Development

### Building

Build the plugin:

```bash
make assemble
```

Or with Gradle:

```bash
./gradlew assemble
```

### Testing

Run the test suite:

```bash
make test
```

Or with Gradle:

```bash
./gradlew test
```

### Local Installation

Install the plugin locally for testing:

```bash
make install
```

## Credits

Inspired by the [pregex](https://github.com/manoss96/pregex) Python library by Manos Stoumpos.

## See Also

- [Nextflow Documentation](https://www.nextflow.io/docs/latest/)
- [Nextflow Plugin Development](https://www.nextflow.io/docs/latest/plugins/developing-plugins.html)
- [PRegEx Python Library](https://github.com/manoss96/pregex)
- [Complete API Documentation](docs/API.md)
- [Example Workflows](examples/)

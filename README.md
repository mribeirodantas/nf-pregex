# nf-pregex

A Nextflow plugin that provides human-readable regex pattern builders, inspired by Python's [pregex](https://github.com/manoss96/pregex) library.

## Overview

Regular expressions are powerful but notoriously hard to read, debug, and maintain. `nf-pregex` lets you build patterns from composable, self-documenting function calls instead of cryptic syntax — and ships a set of ready-made builders for common bioinformatics tokens (DNA/protein sequences, chromosomes, read pairs, file extensions).

**Traditional regex:**

```groovy
def pattern = /sample(\d+)_(R1|R2)\.fastq\.gz/
```

**With nf-pregex:**

```groovy
include { Sequence; Literal; OneOrMore; Digit; Group; Either } from 'plugin/nf-pregex'

def pattern = Sequence([
    Literal("sample"),
    Group(OneOrMore(Digit())),
    Literal("_"),
    Group(Either(["R1", "R2"])),
    Literal(".fastq.gz")
])
```

## Requirements

- Nextflow `24.10.0` or later

## Installation

Add the plugin to your `nextflow.config`:

```groovy
plugins {
    id 'nf-pregex@1.0.1'
}
```

Then import the builders you need in your pipeline script:

```groovy
include { Sequence; Literal; OneOrMore; Digit } from 'plugin/nf-pregex'
```

## Quick start

```groovy
include { Sequence; Literal; OneOrMore; Digit } from 'plugin/nf-pregex'

workflow {
    def pattern = Sequence([
        Literal("sample"),
        OneOrMore(Digit())
    ])

    // Compile to a regex string
    println pattern.toRegex()          // → sample(?:\d)+

    // Test against input
    println pattern.test("sample123")  // → true
    println pattern.matches("sample")  // → false

    // Use it to filter a channel
    channel.fromPath("data/*")
        .filter { f -> pattern.test(f.name) }
        .view()
}
```

## Building patterns

Every builder returns a `PRegEx` object that can be nested inside other builders and compiled with `.toRegex()`.

### Literals and sequences

| Builder | Produces | Description |
|---|---|---|
| `Literal(text)` | escaped `text` | Matches literal text (all regex metacharacters escaped) |
| `Sequence([a, b, ...])` | `ab...` | Concatenates sub-patterns in order |

### Quantifiers

| Builder | Produces | Description |
|---|---|---|
| `Optional(p)` | `(?:p)?` | Zero or one |
| `OneOrMore(p)` | `(?:p)+` | One or more |
| `ZeroOrMore(p)` | `(?:p)*` | Zero or more |
| `Exactly(p, n)` | `(?:p){n}` | Exactly `n` times |
| `AtLeast(p, n)` | `(?:p){n,}` | At least `n` times |
| `Range(p, min, max)` | `(?:p){min,max}` | Between `min` and `max` times |

### Alternation

| Builder | Produces | Description |
|---|---|---|
| `Either([a, b, ...])` | `(?:a\|b)` | Alternation over **literal strings** (each escaped) |
| `AnyOf([p1, p2, ...])` | `(?:p1\|p2)` | Alternation over **`PRegEx` sub-patterns** (joined verbatim) |

Use `Either` for a fixed set of strings; use `AnyOf` when the alternatives are themselves built patterns:

```groovy
include { AnyOf; Sequence; Literal; OneOrMore; Digit } from 'plugin/nf-pregex'

AnyOf([
    Sequence([Literal("chr"), OneOrMore(Digit())]),
    Literal("chrX")
]).toRegex()   // → (?:chr(?:\d)+|chrX)
```

### Character classes and shorthands

| Builder | Produces | Description |
|---|---|---|
| `AnyChar()` | `.` | Any character |
| `Digit()` | `\d` | A digit |
| `WordChar()` | `\w` | A word character |
| `Whitespace()` | `\s` | Whitespace |
| `CharClass(chars)` | `[chars]` | Custom character class |
| `NotCharClass(chars)` | `[^chars]` | Negated character class |
| `CharRange(start, end)` | `[start-end]` | Range, e.g. `CharRange("a", "z")` |
| `MultiRange(spec)` | `[spec]` | Multiple ranges, e.g. `MultiRange("a-zA-Z0-9")` |

### Anchors

| Builder | Produces | Description |
|---|---|---|
| `StartOfLine()` | `^` | Start of line |
| `EndOfLine()` | `$` | End of line |
| `StartOfString()` | `\A` | Start of string |
| `EndOfString()` | `\z` | End of string |

### Groups

| Builder | Produces | Description |
|---|---|---|
| `Group(p)` | `(p)` | Capturing group |
| `Group(name, p)` | `(?<name>p)` | Named capturing group |
| `NamedGroup(p, name)` | `(?<name>p)` | Named capturing group (pattern-first form) |

## Bioinformatics patterns

Ready-made builders for common genomics tokens:

**Sequences**

- `DNASequence()` / `StrictDNASequence()` / `DNASequenceWithAmbiguity()`
- `ProteinSequence()` / `StrictProteinSequence()` / `ProteinSequenceWithAmbiguity()`
- `PhredQuality()`

**Genomic identifiers**

- `Chromosome()` — flexible chromosome names (with or without `chr` prefix)
- `StrictChromosome()` — requires the `chr` prefix
- `ReadPair()` — paired-end read identifiers (`_R1`/`_R2`, `_1`/`_2`, etc.)

**File extensions** (case-insensitive, with automatic `.gz` support)

- `FastqExtension()`, `FastaExtension()`, `VcfExtension()`, `AlignmentExtension()`,
  `BedExtension()`, `GffGtfExtension()`

```groovy
include { Chromosome; FastqExtension } from 'plugin/nf-pregex'

Chromosome().test("chr22")        // → true
FastqExtension().test("x.fq.gz")  // → true
```

## Testing and validation

Every pattern exposes methods to test and extract matches:

```groovy
def p = Sequence([Literal("sample"), Group(OneOrMore(Digit()))])

p.test("test_sample456")     // → true  (matches a substring)
p.matches("sample456")       // → true  (matches the whole string)
p.extract("sample456")       // → ['0': 'sample456', 'match': 'sample456', '1': '456']

// Run a batch of assertions at once
p.testAll([
    "sample1"  : true,
    "sampleX"  : false
])
```

- `test(input)` — matches anywhere in the input (`Matcher.find()`)
- `matches(input)` — matches the entire input (`Matcher.matches()`)
- `extract(input)` — returns a map of numbered and named groups, or `null` if no match
- `testAll(cases)` — runs a map of `input → expected` and returns a `TestReport`

## Debugging and visualization

```groovy
def p = Sequence([Literal("chr"), OneOrMore(Digit())])

println p.explain()     // human-readable breakdown of the pattern
println p.visualize()   // tree view of the pattern structure
```

## Documentation

- [`docs/API.md`](docs/API.md) — complete API reference
- [`docs/TUTORIAL.md`](docs/TUTORIAL.md) — step-by-step guide
- [`examples/`](examples/) — runnable example pipelines
- [`CHANGELOG.md`](CHANGELOG.md) — release history

## License

See [LICENSE](LICENSE).

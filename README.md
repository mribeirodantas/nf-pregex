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

def pattern = Sequence(
    Literal("sample"),
    OneOrMore(Digit()),
    Literal("_"),
    Either("R1", "R2"),
    Literal(".fastq.gz")
)
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
    def pattern = Sequence(
        Literal("colo"),
        Optional(Literal("u")),
        Literal("r")
    )
    
    println pattern  // Outputs: colo(u)?r
    
    // Use in channel operations
    channel.of("color", "colour", "colors")
        .filter { it =~ /${pattern}/ }
        .view()
}
```

## API Reference

### Pattern Builders

#### Either(String...)
Creates an alternation pattern (OR operation).

```groovy
Either("foo", "bar", "baz")  // → (foo|bar|baz)
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

#### Sequence(PRegEx...)
Concatenates multiple patterns in order.

```groovy
Sequence(Literal("hello"), Literal(" "), Literal("world"))  // → hello world
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

def emailPattern = Sequence(
    OneOrMore(WordChar()),
    Literal("@"),
    OneOrMore(WordChar()),
    Literal("."),
    Either("com", "org", "edu")
)
// → (\w)+@(\w)+\.(com|org|edu)
```

### FASTQ File Pattern
```groovy
include { Sequence; OneOrMore; Literal; Either; Optional; WordChar } from 'plugin/nf-pregex'

def fastqPattern = Sequence(
    OneOrMore(WordChar()),
    Literal("_"),
    Either("R1", "R2"),
    Literal(".fastq"),
    Optional(Literal(".gz"))
)
// → (\w)+_(R1|R2)\.fastq(\.gz)?

// Use with channel operations
channel.fromPath("data/*")
    .filter { it.name =~ /${fastqPattern}/ }
    .view()
```

### Sample ID Pattern
```groovy
include { Sequence; Literal; Either; Digit } from 'plugin/nf-pregex'

def samplePattern = Sequence(
    Literal("S"),
    Digit().exactly(3),
    Literal("_"),
    Either("T", "C"),
    Digit(),
    Literal("_"),
    Either("R1", "R2")
)
// → S(\d){3}_(T|C)(\d)_(R1|R2)
```

### VCF File with Optional Version
```groovy
include { Sequence; OneOrMore; Literal; Optional; Digit; WordChar } from 'plugin/nf-pregex'

def vcfPattern = Sequence(
    OneOrMore(WordChar()),
    Literal("."),
    Optional(Sequence(
        Literal("v"),
        OneOrMore(Digit()),
        Literal(".")
    )),
    Literal("vcf"),
    Optional(Literal(".gz"))
)
// Matches: variants.vcf, variants.v1.vcf.gz, etc.
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

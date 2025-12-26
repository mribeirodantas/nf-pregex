# nf-pregex API Documentation

Complete API reference for all PRegEx pattern builders.

## Table of Contents

- [Pattern Builders](#pattern-builders)
- [Quantifiers](#quantifiers)
- [Character Classes](#character-classes)
- [Anchors](#anchors)
- [Method Chaining](#method-chaining)
- [Usage Patterns](#usage-patterns)

## Pattern Builders

### Either(String...)

Creates an alternation pattern that matches any of the provided alternatives.

**Syntax:**
```groovy
Either(String... alternatives)
```

**Parameters:**
- `alternatives` - Variable number of string alternatives (at least one required)

**Returns:** PRegEx pattern object

**Examples:**
```groovy
Either("foo", "bar")              // → (foo|bar)
Either("R1", "R2", "R3")          // → (R1|R2|R3)
Either("fastq", "fq")             // → (fastq|fq)
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

### Sequence(PRegEx...)

Concatenates multiple patterns in order.

**Syntax:**
```groovy
Sequence(PRegEx... patterns)
```

**Parameters:**
- `patterns` - Variable number of PRegEx patterns to concatenate

**Returns:** PRegEx pattern object

**Examples:**
```groovy
Sequence(
    Literal("hello"),
    Literal(" "),
    Literal("world")
)                                 // → hello world

Sequence(
    Literal("sample"),
    Digit(),
    Literal(".txt")
)                                 // → sample\d\.txt
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
Sequence(
    StartOfLine(),
    Literal("hello")
)                                 // → ^hello
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
Sequence(
    Literal("world"),
    EndOfLine()
)                                 // → world$
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
Either("foo", "bar").group()      // → ((foo|bar))
```

---

## Usage Patterns

### Email Validation

```groovy
def emailPattern = Sequence(
    OneOrMore(WordChar()),
    Literal("@"),
    OneOrMore(WordChar()),
    Literal("."),
    Range(WordChar(), 2, 3)
)
// → (\w)+@(\w)+\.(\w){2,3}
```

### FASTQ Files

```groovy
def fastqPattern = Sequence(
    OneOrMore(WordChar()),
    Literal("_"),
    Either("R1", "R2"),
    Literal(".fastq"),
    Optional(Literal(".gz"))
)
// → (\w)+_(R1|R2)\.fastq(\.gz)?
```

### Sample IDs

```groovy
def sampleIdPattern = Sequence(
    Literal("sample"),
    Digit().exactly(3),
    Literal("_"),
    Either("control", "treatment")
)
// → sample(\d){3}_(control|treatment)
```

### Phone Numbers

```groovy
def phonePattern = Sequence(
    Optional(Literal("+")),
    Digit().range(10, 15)
)
// → (\+)?(\\d){10,15}
```

### IP Address (Simple)

```groovy
def ipPattern = Sequence(
    Digit().range(1, 3),
    Literal("."),
    Digit().range(1, 3),
    Literal("."),
    Digit().range(1, 3),
    Literal("."),
    Digit().range(1, 3)
)
// → (\d){1,3}\.(\d){1,3}\.(\d){1,3}\.(\d){1,3}
```

---

## Integration with Nextflow

### Using Patterns in Channel Operations

```groovy
include { Sequence; Literal; Either; OneOrMore; WordChar } from 'plugin/nf-pregex'

workflow {
    def pattern = Sequence(
        OneOrMore(WordChar()),
        Literal("_"),
        Either("R1", "R2"),
        Literal(".fastq.gz")
    )
    
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
Either()                          // IllegalArgumentException

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

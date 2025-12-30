# Syntax Migration Guide

## ❌ Old Incorrect Syntax → ✅ Correct Syntax

The plugin does NOT use `new PRegEx.ClassName()` syntax. Here's how to fix your code:

### Basic Patterns

```groovy
// ❌ WRONG
def pattern = new PRegEx.Digit().exactly(3)

// ✅ CORRECT
include { Digit, Exactly } from 'plugin/nf-pregex'
def pattern = Exactly(Digit(), 3)
```

### Method Chaining

```groovy
// ❌ WRONG
def pattern = new PRegEx.Digit().exactly(3)
    .then(new PRegEx.Literal("-"))
    .then(new PRegEx.Digit().exactly(4))

// ✅ CORRECT
include { Digit, Exactly, Literal } from 'plugin/nf-pregex'
def pattern = Exactly(Digit(), 3)
    .then(Literal("-"))
    .then(Exactly(Digit(), 4))
```

### Named Groups

```groovy
// ❌ WRONG
def pattern = new PRegEx.Digit().exactly(3).namedGroup("area")
    .then(new PRegEx.Literal("-"))
    .then(new PRegEx.Digit().exactly(4).namedGroup("number"))

// ✅ CORRECT
include { Digit, Exactly, Literal, Group } from 'plugin/nf-pregex'
def pattern = Group("area", Exactly(Digit(), 3))
    .then(Literal("-"))
    .then(Group("number", Exactly(Digit(), 4)))
```

**Important:** Named groups use the `Group(name, pattern)` function, NOT `NamedGroup`!

## Complete Function Reference

Always import the functions you need:

```groovy
include { 
    // Basic patterns
    Literal          // Match literal text
    Digit            // Match digits [0-9]
    WordChar         // Match word characters [a-zA-Z0-9_]
    Whitespace       // Match whitespace
    
    // Quantifiers
    Exactly          // Exactly(pattern, n) - match n times
    Optional         // Optional(pattern) - match 0 or 1 time
    OneOrMore        // OneOrMore(pattern) - match 1+ times
    ZeroOrMore       // ZeroOrMore(pattern) - match 0+ times
    AtLeast          // AtLeast(pattern, n) - match n or more times
    AtMost           // AtMost(pattern, n) - match up to n times
    Range            // Range(pattern, min, max) - match between min and max times
    
    // Grouping
    Group            // Group(pattern) - capture group
                     // Group(name, pattern) - named capture group
    
    // Alternation
    Either           // Either(option1, option2, ...) - match any option
    
    // Anchors
    StartOfString    // Start of string anchor ^
    EndOfString      // End of string anchor $
    StartOfLine      // Start of line anchor
    EndOfLine        // End of line anchor
    
    // Composition
    Sequence         // Sequence(pattern1, pattern2, ...) - concatenate patterns
} from 'plugin/nf-pregex'
```

## Common Patterns

### Phone Number
```groovy
include { Digit, Exactly, Literal, Group } from 'plugin/nf-pregex'

def phonePattern = Group("area", Exactly(Digit(), 3))
    .then(Literal("-"))
    .then(Group("exchange", Exactly(Digit(), 3)))
    .then(Literal("-"))
    .then(Group("number", Exactly(Digit(), 4)))
```

### Email
```groovy
include { WordChar, OneOrMore, Literal, Group, AtLeast } from 'plugin/nf-pregex'

def emailPattern = Group("user", OneOrMore(WordChar()))
    .then(Literal("@"))
    .then(Group("domain", OneOrMore(WordChar())))
    .then(Literal("."))
    .then(Group("tld", AtLeast(WordChar(), 2)))
```

### FASTQ Filename
```groovy
include { WordChar, OneOrMore, Literal, Either, Group, Optional } from 'plugin/nf-pregex'

def fastqPattern = Group("sample", OneOrMore(WordChar()))
    .then(Literal("_"))
    .then(Group("read", Either(["R1", "R2"])))
    .then(Literal(".fastq"))
    .then(Optional(Literal(".gz")))
```

**Note:** `Either` takes a list: `Either(["R1", "R2"])` not `Either("R1", "R2")`

## Usage in Nextflow

```groovy
workflow {
    // Convert pattern to Java Pattern for matching
    def pattern = Exactly(Digit(), 3).then(Literal("-")).then(Exactly(Digit(), 4))
    
    // Use in filter
    channel.of("555-1234", "123-4567", "invalid")
        .filter { it =~ pattern.toPattern() }
        .view()
    
    // Get the regex string
    println "Regex: ${pattern.toRegex()}"
    
    // Extract named groups
    def text = "555-1234"
    def matcher = text =~ pattern.toRegex()
    if (matcher) {
        println "Match found!"
        // If you have named groups, access them with:
        // matcher.group('groupName')
    }
}
```

## Key Points

1. **Always import functions** - You must include the functions you want to use
2. **Use function calls** - Call functions like `Digit()`, not `new PRegEx.Digit()`
3. **Quantifiers wrap patterns** - Use `Exactly(Digit(), 3)` not `Digit().exactly(3)`
4. **Named groups** - Use `Group("name", pattern)` not `pattern.namedGroup("name")`
5. **Either takes a list** - Use `Either(["R1", "R2"])` not `Either("R1", "R2")`
6. **Convert to regex** - Use `.toRegex()` to get the regex string
7. **Access groups** - Call `matcher.find()` before accessing groups with `matcher.group('name')`

# How to Fix Your test.nf File

## The Problem

Your `test.nf` file is using **incorrect syntax** that doesn't exist in the nf-pregex plugin:

```groovy
// ❌ THIS DOESN'T WORK
def pattern = new PRegEx.Digit().exactly(3)
    .then(new PRegEx.Literal("-"))
    .then(new PRegEx.Digit().exactly(4))
```

## The Solution

Replace your entire `test.nf` file with this corrected version:

```groovy
#!/usr/bin/env nextflow

// Import PRegEx functions
include { 
    Digit
    Literal
    Exactly
    Group
} from 'plugin/nf-pregex'

workflow {
    // Example 1: Phone number pattern (simple version)
    def pattern = Exactly(Digit(), 3)
        .then(Literal("-"))
        .then(Exactly(Digit(), 4))
    
    println "Pattern 1: ${pattern.toRegex()}"
    
    // Example 2: Phone number with named groups
    def phonePattern = Group("area", Exactly(Digit(), 3))
        .then(Literal("-"))
        .then(Group("number", Exactly(Digit(), 4)))
    
    println "Pattern 2: ${phonePattern.toRegex()}"
    
    // Test the patterns
    def testNumber = "555-1234"
    println "\nTesting: ${testNumber}"
    println "Pattern 1 matches: ${testNumber ==~ pattern.toRegex()}"
    
    def matcher = testNumber =~ phonePattern.toRegex()
    if (matcher) {
        println "Pattern 2 matches!"
        println "  Area code: ${matcher.group('area')}"
        println "  Number: ${matcher.group('number')}"
    }
}
```

## Step-by-Step Changes

### 1. Add the include statement at the top
```groovy
include { 
    Digit
    Literal
    Exactly
    NamedGroup
} from 'plugin/nf-pregex'
```

### 2. Replace ALL instances of `new PRegEx.Digit()` with `Digit()`
```groovy
// Before:
new PRegEx.Digit()

// After:
Digit()
```

### 3. Replace ALL instances of `new PRegEx.Literal()` with `Literal()`
```groovy
// Before:
new PRegEx.Literal("-")

// After:
Literal("-")
```

### 4. Replace `.exactly(n)` with `Exactly(pattern, n)`
```groovy
// Before:
new PRegEx.Digit().exactly(3)

// After:
Exactly(Digit(), 3)
```

### 5. Replace `.namedGroup("name")` with `Group("name", pattern)`
```groovy
// Before:
new PRegEx.Digit().exactly(3).namedGroup("area")

// After:
Group("area", Exactly(Digit(), 3))
```

## Quick Reference

| ❌ Old (Wrong) | ✅ New (Correct) |
|---------------|------------------|
| `new PRegEx.Digit()` | `Digit()` |
| `new PRegEx.Literal("-")` | `Literal("-")` |
| `pattern.exactly(3)` | `Exactly(pattern, 3)` |
| `pattern.oneOrMore()` | `OneOrMore(pattern)` |
| `pattern.optional()` | `Optional(pattern)` |
| `pattern.namedGroup("name")` | `Group("name", pattern)` |

## Running Your Fixed File

After making the changes:

```bash
nextflow run test.nf -plugins nf-pregex@0.1.0
```

Or add to your `nextflow.config`:

```groovy
plugins {
    id 'nf-pregex@0.1.0'
}
```

Then run:

```bash
nextflow run test.nf
```

## Need More Examples?

Check out the working examples in the `examples/` directory:
- `examples/basic_usage.nf` - Basic pattern building
- `examples/validation-and-debugging.nf` - Pattern testing and validation
- `examples/file_matching.nf` - Matching filenames
- `examples/bioinformatics_patterns.nf` - Common bioinformatics patterns

All these examples use the **correct syntax** and should run without errors!

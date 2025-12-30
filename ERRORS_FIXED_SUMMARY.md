# nf-pregex: Errors Fixed and Correct Usage

## Summary of Issues

Your `test.nf` file was using **incorrect syntax** that doesn't exist in the nf-pregex plugin. The errors occurred because:

1. ❌ Using `new PRegEx.Digit()` - This class doesn't exist
2. ❌ Using method chaining like `.exactly(3)` - These methods don't exist on the base classes
3. ❌ Using `.namedGroup("name")` - This method doesn't exist

## The Correct Way

The plugin provides **standalone functions** that you import and call directly:

###  Key Syntax Rules

| Feature | ❌ WRONG | ✅ CORRECT |
|---------|----------|------------|
| Import | (no import) | `include { Digit, Literal } from 'plugin/nf-pregex'` |
| Digit pattern | `new PRegEx.Digit()` | `Digit()` |
| Literal pattern | `new PRegEx.Literal("-")` | `Literal("-")` |
| Exactly quantifier | `.exactly(3)` | `Exactly(pattern, 3)` |
| Named group | `.namedGroup("name")` | `Group("name", pattern)` |
| Either (alternation) | `Either("A", "B")` | `Either(["A", "B"])` ← **List required!** |
| Get regex string | (pattern itself) | `pattern.toRegex()` |
| Access named groups | `matcher.group('name')` | `matcher.find()` then `matcher.group('name')` |

## Fixed Example

Replace your entire `test.nf` with this:

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
    // Example 1: Simple phone number pattern (###-####)
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
    if (matcher.find()) {  // IMPORTANT: Must call find() before accessing groups!
        println "Pattern 2 matches!"
        println "  Area code: ${matcher.group('area')}"
        println "  Number: ${matcher.group('number')}"
    }
}
```

## Run It

```bash
cd /Users/mribeirodantas/dev/work/nf-pregex
nextflow run test.nf -plugins nf-pregex@0.1.0
```

## Expected Output

```
Pattern 1: \d{3}\-\d{4}
Pattern 2: (?<area>\d{3})\-(?<number>\d{4})

Testing: 555-1234
Pattern 1 matches: true
Pattern 2 matches!
  Area code: 555
  Number: 1234
```

## All Available Functions

See the sandbox file: `nf-pregex/test_working_example.nf` for a comprehensive working example that demonstrates:
- Simple patterns
- Named groups
- FASTQ filename matching
- Using patterns with Nextflow channels
- Proper error handling

Or run it directly:
```bash
cd /Users/mribeirodantas/dev/work/nf-pregex
nextflow run test_working_example.nf -plugins nf-pregex@0.1.0
```

## Additional Documentation

- `SYNTAX_MIGRATION.md` - Complete migration guide from wrong to correct syntax
- `FIX_YOUR_TEST_FILE.md` - Step-by-step instructions
- `COPY_THIS_TO_YOUR_TEST_NF.txt` - Ready-to-use test file
- `examples/` directory - Multiple working examples

## Common Pitfalls

1. **Forgetting to import functions**
   ```groovy
   // Add this at the top of your script:
   include { Digit, Literal, Exactly } from 'plugin/nf-pregex'
   ```

2. **Wrong Either syntax**
   ```groovy
   // ❌ WRONG: Either("R1", "R2")
   // ✅ CORRECT: Either(["R1", "R2"])
   ```

3. **Not calling matcher.find()**
   ```groovy
   def matcher = text =~ pattern.toRegex()
   matcher.find()  // ← MUST call this before accessing groups!
   println matcher.group('name')
   ```

4. **Wrong Group parameter order**
   ```groovy
   // ❌ WRONG: Group(pattern, "name")
   // ✅ CORRECT: Group("name", pattern)
   ```

## Quick Test

To verify your syntax is correct, check:
- [ ] Do you have `include { ... } from 'plugin/nf-pregex'`?
- [ ] Are you calling functions like `Digit()` not `new PRegEx.Digit()`?
- [ ] Are you using `Exactly(Digit(), 3)` not `Digit().exactly(3)`?
- [ ] Are you using `Group("name", pattern)` not `pattern.namedGroup("name")`?
- [ ] Are you using `Either(["A", "B"])` with square brackets?

If all checks pass, your code should work! 🎉

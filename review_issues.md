# Documentation and Code Review Issues

## Issues Found

### PRegEx.groovy

### PRegExExtension.groovy

**CRITICAL 1**: CharRange and MultiRange are missing from PRegExExtension.groovy
- Location: README.md line 403 shows: `include { Sequence; CharRange; MultiRange; Literal } from 'plugin/nf-pregex'`
- Problem: CharRange and MultiRange are NOT exposed in PRegExExtension.groovy
- Impact: Users cannot use these functions as documented
- Fix: Add CharRange and MultiRange extension functions

**CRITICAL 2**: BioinformaticsPatterns functions are missing from PRegExExtension.groovy
- Location: README.md line 70-76 shows: `include { DNASequence; FastqExtension; ReadPair; Chromosome } from 'plugin/nf-pregex'`
- Problem: All BioinformaticsPatterns functions (DNASequence, FastqExtension, etc.) are NOT exposed in PRegExExtension.groovy
- Impact: Users cannot use these functions as documented
- Fix: Add all BioinformaticsPatterns functions to PRegExExtension.groovy

### BioinformaticsPatterns.groovy

### Test Files

### README.md

### docs/API.md

### docs/TUTORIAL.md

### README.md

**Issue documented above** - CharRange, MultiRange, and BioinformaticsPatterns shown in include statements but not available.

### docs/API.md

**Issue documented above** - Extensive documentation of CharRange and MultiRange that cannot be used as documented.

### docs/TUTORIAL.md

**Issue documented above** - Tutorial examples show include statements for functions not exposed in PRegExExtension.

## Summary

Total Issues: 2 (blocking user functionality)
- **Critical: 2**
  1. CharRange and MultiRange missing from PRegExExtension
  2. All 16 BioinformaticsPatterns functions missing from PRegExExtension
- High: 0
- Medium: 0
- Low: 0

## Fix Strategy

Add all missing functions to PRegExExtension.groovy:
1. CharRange(String, String) - delegates to PRegEx.CharRange
2. MultiRange(String) - delegates to PRegEx.MultiRange  
3. All 16 BioinformaticsPatterns static methods

Then add extension tests for each new function.

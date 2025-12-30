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
    // Old incorrect syntax: def pattern = new PRegEx.Digit().exactly(3)
    // Correct syntax:
    def pattern = Exactly(Digit(), 3)
        .then(Literal("-"))
        .then(Exactly(Digit(), 4))
    
    println "Pattern 1: ${pattern.toRegex()}"
    
    // Example 2: Phone number with named groups
    // Old incorrect syntax: def phonePattern = new PRegEx.Digit().exactly(3).namedGroup("area")
    // Correct syntax:
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

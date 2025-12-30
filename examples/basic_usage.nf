#!/usr/bin/env nextflow

/*
 * Example demonstrating basic usage of the nf-pregex plugin.
 * 
 * This example shows how to use PRegEx pattern builders instead of
 * traditional regex strings for more readable pattern matching.
 * 
 * To run this example:
 *   nextflow run basic_usage.nf -plugins nf-pregex@0.1.0
 * 
 * Or add to your nextflow.config:
 *   plugins {
 *       id 'nf-pregex@0.1.0'
 *   }
 */

// Import PRegEx functions
include { 
    Either
    Literal
    Optional
    OneOrMore
    Digit
    WordChar
    Sequence
    Group
} from 'plugin/nf-pregex'

workflow {
    
    // Example 1: Simple alternation - match either "foo" or "bar"
    def pattern1 = Either(["foo", "bar"])
    println "Pattern 1: ${pattern1}"  // Outputs: (foo|bar)
    
    // Example 2: Literal text with special characters
    def pattern2 = Literal("file.txt")
    println "Pattern 2: ${pattern2}"  // Outputs: file\.txt
    
    // Example 3: Optional pattern - match "color" or "colour"
    def pattern3 = Sequence([
        Literal("colo"),
        Optional(Literal("u")),
        Literal("r")
    ])
    println "Pattern 3: ${pattern3}"  // Outputs: colo(u)?r
    
    // Example 4: Digit patterns - match numbers
    def pattern4 = OneOrMore(Digit())
    println "Pattern 4: ${pattern4}"  // Outputs: (\d)+
    
    // Example 5: Matching sample IDs like "sample123_R1.fastq.gz"
    def samplePattern = Sequence([
        Literal("sample"),
        OneOrMore(Digit()),
        Literal("_"),
        Either(["R1", "R2"]),
        Literal(".fastq.gz")
    ])
    println "Sample pattern: ${samplePattern}"
    // Outputs: sample(\d)+_(R1|R2)\.fastq\.gz
    
    // Example 6: Using patterns with channel operations
    def ch = channel.of(
        "sample001_R1.fastq.gz",
        "sample001_R2.fastq.gz",
        "sample002_R1.fastq.gz",
        "sample002_R2.fastq.gz",
        "other_file.txt"
    )
    
    // Filter files matching our pattern
    ch
        .filter { it =~ samplePattern.toPattern() }
        .view { "Matched: $it" }
    
    // Example 7: Email-like pattern
    def emailPattern = Sequence([
        OneOrMore(WordChar()),
        Literal("@"),
        OneOrMore(WordChar()),
        Literal("."),
        Either(["com", "org", "edu"])
    ])
    println "Email pattern: ${emailPattern}"
    
    // Example 8: Using method chaining
    def chainedPattern = Literal("test").optional()
    println "Chained pattern: ${chainedPattern}"  // Outputs: (test)?
    
    // Example 9: Complex pattern with multiple quantifiers
    def complexPattern = Sequence([
        Literal("prefix_"),
        OneOrMore(WordChar()),
        Literal("_"),
        Digit().exactly(3),
        Optional(Literal("_final"))
    ])
    println "Complex pattern: ${complexPattern}"
    
    // Example 10: Using named groups for data extraction
    def namedGroupPattern = Sequence([
        Group('sample', OneOrMore(WordChar())),
        Literal("_"),
        Group('read', Either(["R1", "R2"])),
        Literal(".fastq.gz")
    ])
    println "\nNamed group pattern: ${namedGroupPattern}"
    
    def filename = "test_sample_R1.fastq.gz"
    def matcher = filename =~ namedGroupPattern
    if (matcher.matches()) {
        println "Matched filename: ${filename}"
        println "  Sample: ${matcher.group('sample')}"
        println "  Read: ${matcher.group('read')}"
    }
    
    // Example 11: Method chaining for cleaner code
    def chainedComplex = Literal("prefix")
        .then(Literal("_"))
        .then(WordChar().oneOrMore())
        .then(Literal(".txt"))
    println "\nChained complex pattern: ${chainedComplex}"
}

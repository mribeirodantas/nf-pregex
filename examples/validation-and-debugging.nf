#!/usr/bin/env nextflow

/*
 * Example demonstrating pattern validation, testing, and debugging features
 * in the nf-pregex plugin
 * 
 * To run this example:
 *   nextflow run validation-and-debugging.nf -plugins nf-pregex@0.1.0
 * 
 * Or add to your nextflow.config:
 *   plugins {
 *       id 'nf-pregex@0.1.0'
 *   }
 */

// Import PRegEx functions
include { 
    Literal
    Digit
    WordChar
    Whitespace
    Exactly
    Optional
    OneOrMore
    AtLeast
    Range
    Either
    NamedGroup
    StartOfString
    EndOfString
    Group
    StartOfLine
    EndOfLine
} from 'plugin/nf-pregex'

workflow {
    
    println "\n╔═══════════════════════════════════════════════════════════════════╗"
    println "║  nf-pregex: Pattern Validation & Debugging Examples              ║"
    println "╚═══════════════════════════════════════════════════════════════════╝\n"
    
    // ============================================================
    // Example 1: Basic Pattern Testing
    // ============================================================
    
    println "\n" + "─" * 70
    println "Example 1: Basic Pattern Testing with test()"
    println "─" * 70
    
    def digitPattern = OneOrMore(Digit())
    
    println "\nPattern: ${digitPattern.toRegex()}"
    println "\nTesting various inputs:"
    
    def testInputs = [
        "abc123def": "contains digits",
        "123": "only digits",
        "no digits": "no digits",
        "": "empty string"
    ]
    
    testInputs.each { input, description ->
        def matches = digitPattern.test(input)
        def icon = matches ? "✓" : "✗"
        println "  ${icon} '${input}' (${description}): ${matches ? 'MATCH' : 'NO MATCH'}"
    }
    
    // ============================================================
    // Example 2: Full String Matching
    // ============================================================
    
    println "\n" + "─" * 70
    println "Example 2: Full String Matching with matches()"
    println "─" * 70
    
    def phonePattern = Exactly(Digit(), 3)
        .then(Literal("-"))
        .then(Exactly(Digit(), 4))
    
    println "\nPattern: ${phonePattern.toRegex()}"
    println "\nTesting for exact matches:"
    
    def phoneTests = [
        "555-1234": "valid format",
        "123-4567": "valid format",
        "call 555-1234 now": "contains valid format but has extra text",
        "555-123": "too few digits"
    ]
    
    phoneTests.each { input, description ->
        def exactMatch = phonePattern.matches(input)
        def icon = exactMatch ? "✓" : "✗"
        println "  ${icon} '${input}' (${description}): ${exactMatch ? 'EXACT MATCH' : 'NOT EXACT'}"
    }
    
    // ============================================================
    // Example 3: Extracting Matched Groups
    // ============================================================
    
    println "\n" + "─" * 70
    println "Example 3: Extracting Matched Groups with extract()"
    println "─" * 70
    
    def emailPattern = NamedGroup(OneOrMore(WordChar()), "user")
        .then(Literal("@"))
        .then(NamedGroup(OneOrMore(WordChar()), "domain"))
        .then(Literal("."))
        .then(NamedGroup(AtLeast(WordChar(), 2), "tld"))
    
    println "\nPattern: ${emailPattern.toRegex()}"
    
    def emailTests = [
        "Contact: user@example.com for info",
        "admin@company.co.uk",
        "test@domain.org"
    ]
    
    emailTests.each { input ->
        def extracted = emailPattern.extract(input)
        if (extracted) {
            println "\n  Input: '${input}'"
            println "  Match: ${extracted['match']}"
            println "  User: ${extracted['user']}"
            println "  Domain: ${extracted['domain']}"
            println "  TLD: ${extracted['tld']}"
        }
    }
    
    // ============================================================
    // Example 4: Comprehensive Pattern Testing
    // ============================================================
    
    println "\n" + "─" * 70
    println "Example 4: Comprehensive Testing with testAll()"
    println "─" * 70
    
    def urlPattern = Literal("http")
        .then(Optional(Literal("s")))
        .then(Literal("://"))
        .then(OneOrMore(WordChar()))
        .then(Literal("."))
        .then(AtLeast(WordChar(), 2))
    
    println "\nPattern: ${urlPattern.toRegex()}"
    
    def urlTestCases = [
        "https://example.com": true,
        "http://test.org": true,
        "https://site.co.uk": true,
        "ftp://invalid.com": false,
        "not a url": false,
        "http://": false
    ]
    
    def report = urlPattern.testAll(urlTestCases)
    println report.report()
    
    // ============================================================
    // Example 5: Pattern Explanation
    // ============================================================
    
    println "\n" + "─" * 70
    println "Example 5: Understanding Patterns with explain()"
    println "─" * 70
    
    def datePattern = NamedGroup(Exactly(Digit(), 4), "year")
        .then(Literal("-"))
        .then(NamedGroup(Exactly(Digit(), 2), "month"))
        .then(Literal("-"))
        .then(NamedGroup(Exactly(Digit(), 2), "day"))
    
    println "\n${datePattern.explain()}"
    
    // ============================================================
    // Example 6: Pattern Visualization
    // ============================================================
    
    println "\n" + "─" * 70
    println "Example 6: Visualizing Pattern Structure with visualize()"
    println "─" * 70
    
    def complexPattern = StartOfString()
        .then(
            NamedGroup(
                Literal("user-")
                    .then(NamedGroup(OneOrMore(Digit()), "id")),
                "username"
            )
        )
        .then(Literal("@"))
        .then(NamedGroup(OneOrMore(WordChar()), "domain"))
        .then(EndOfString())
    
    println complexPattern.visualize()
    
    // ============================================================
    // Example 7: IP Address Validation
    // ============================================================
    
    println "\n" + "─" * 70
    println "Example 7: Real-World Example - IP Address Validation"
    println "─" * 70
    
    // Simple IP address pattern (not production-ready, for demo only)
    def ipPattern = Range(Digit(), 1, 3)
        .then(Literal("."))
        .then(Range(Digit(), 1, 3))
        .then(Literal("."))
        .then(Range(Digit(), 1, 3))
        .then(Literal("."))
        .then(Range(Digit(), 1, 3))
    
    println "\nPattern: ${ipPattern.toRegex()}"
    
    def ipTestCases = [
        "192.168.1.1": true,
        "10.0.0.1": true,
        "255.255.255.255": true,
        "999.999.999.999": true,  // Note: This simple pattern doesn't validate ranges
        "192.168.1": false,
        "not.an.ip.address": false
    ]
    
    def ipReport = ipPattern.testAll(ipTestCases)
    println ipReport.report()
    
    // ============================================================
    // Example 8: File Name Pattern Analysis
    // ============================================================
    
    println "\n" + "─" * 70
    println "Example 8: Bioinformatics File Name Pattern"
    println "─" * 70
    
    def filePattern = NamedGroup(OneOrMore(WordChar()), "sample")
        .then(Literal("_"))
        .then(NamedGroup(Either("R1", "R2"), "read"))
        .then(Literal(".fastq"))
        .then(Optional(Literal(".gz")))
    
    println "\n${filePattern.explain()}"
    println "\n${filePattern.visualize()}"
    
    println "\nExtracting information from file names:"
    def fileNames = [
        "sample001_R1.fastq.gz",
        "sample002_R2.fastq.gz",
        "control_R1.fastq"
    ]
    
    fileNames.each { fileName ->
        def info = filePattern.extract(fileName)
        if (info) {
            println "  File: ${fileName}"
            println "    Sample: ${info['sample']}"
            println "    Read: ${info['read']}"
        }
    }
    
    // ============================================================
    // Example 9: Testing with Channel Data
    // ============================================================
    
    println "\n" + "─" * 70
    println "Example 9: Using Patterns with Channels"
    println "─" * 70
    
    def samplePattern = Literal("sample")
        .then(NamedGroup(OneOrMore(Digit()), "id"))
        .then(Literal("_"))
        .then(NamedGroup(Either("R1", "R2"), "read"))
    
    println "\nPattern: ${samplePattern.toRegex()}"
    
    def fileChannel = channel.of(
        "sample001_R1.fastq",
        "sample001_R2.fastq",
        "sample002_R1.fastq",
        "sample002_R2.fastq",
        "invalid_file.txt"
    )
    
    fileChannel
        .map { fileName ->
            def info = samplePattern.extract(fileName)
            if (info) {
                return [
                    file: fileName,
                    sample: info['id'],
                    read: info['read']
                ]
            } else {
                return [
                    file: fileName,
                    sample: 'unknown',
                    read: 'unknown'
                ]
            }
        }
        .view { item ->
            "  File: ${item.file} → Sample: ${item.sample}, Read: ${item.read}"
        }
    
    // ============================================================
    // Example 10: Complex Validation Scenario
    // ============================================================
    
    println "\n" + "─" * 70
    println "Example 10: Complex Validation - SRA Accession Numbers"
    println "─" * 70
    
    // SRA accession pattern: SRR followed by 6-10 digits
    def sraPattern = StartOfString()
        .then(Literal("SRR"))
        .then(NamedGroup(Range(Digit(), 6, 10), "accession"))
        .then(EndOfString())
    
    println "\n${sraPattern.explain()}"
    
    def sraTestCases = [
        "SRR1234567": true,
        "SRR12345678": true,
        "SRR123456789": true,
        "SRR12345": false,  // too few digits
        "SRR12345678901": false,  // too many digits
        "ERR1234567": false,  // wrong prefix
        "SRR1234567extra": false  // extra text
    ]
    
    def sraReport = sraPattern.testAll(sraTestCases)
    println sraReport.report()
    
    println "\n" + "═" * 70
    println "Examples completed successfully!"
    println "═" * 70 + "\n"
}

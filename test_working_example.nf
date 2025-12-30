#!/usr/bin/env nextflow

/*
 * Working test example for nf-pregex plugin
 * This file demonstrates the CORRECT syntax
 * 
 * To run: nextflow run test_working_example.nf -plugins nf-pregex@0.1.0
 */

// Import the PRegEx functions you need
include { 
    Digit
    Literal
    Exactly
    Group
    WordChar
    OneOrMore
    Either
    Optional
} from 'plugin/nf-pregex'

workflow {
    
    println "=" * 70
    println "nf-pregex Test Examples"
    println "=" * 70
    
    // =========================================================================
    // Example 1: Simple phone number pattern (###-####)
    // =========================================================================
    println "\n[Example 1] Simple phone number pattern"
    
    def simplePhone = Exactly(Digit(), 3)
        .then(Literal("-"))
        .then(Exactly(Digit(), 4))
    
    println "Pattern: ${simplePhone.toRegex()}"
    
    def testNumbers = ["555-1234", "123-4567", "invalid", "12-345"]
    testNumbers.each { num ->
        def matches = num ==~ simplePhone.toRegex()
        println "  ${num.padRight(15)} → ${matches ? '✓ MATCH' : '✗ NO MATCH'}"
    }
    
    // =========================================================================
    // Example 2: Phone number with named groups (area-exchange-number)
    // =========================================================================
    println "\n[Example 2] Phone number with named groups"
    
    def phoneWithGroups = Group("area", Exactly(Digit(), 3))
        .then(Literal("-"))
        .then(Group("exchange", Exactly(Digit(), 3)))
        .then(Literal("-"))
        .then(Group("number", Exactly(Digit(), 4)))
    
    println "Pattern: ${phoneWithGroups.toRegex()}"
    
    def fullNumber = "555-867-5309"
    def matcher = fullNumber =~ phoneWithGroups.toRegex()
    if (matcher) {
        println "  Input: ${fullNumber}"
        println "  ✓ MATCHED!"
        println "    Area code: ${matcher.group('area')}"
        println "    Exchange:  ${matcher.group('exchange')}"
        println "    Number:    ${matcher.group('number')}"
    }
    
    // =========================================================================
    // Example 3: FASTQ filename pattern
    // =========================================================================
    println "\n[Example 3] FASTQ filename pattern"
    
    def fastqPattern = Group("sample", OneOrMore(WordChar()))
        .then(Literal("_"))
        .then(Group("read", Either(["R1", "R2"])))
        .then(Literal(".fastq"))
        .then(Optional(Literal(".gz")))
    
    println "Pattern: ${fastqPattern.toRegex()}"
    
    def testFiles = [
        "sample1_R1.fastq.gz",
        "sample1_R2.fastq",
        "control_R1.fastq.gz",
        "invalid_R3.fastq",
        "no_read_indicator.fastq"
    ]
    
    testFiles.each { file ->
        def matches = file =~ fastqPattern.toRegex()
        if (matches) {
            println "  ${file.padRight(30)} → ✓ Sample: ${matches.group('sample')}, Read: ${matches.group('read')}"
        } else {
            println "  ${file.padRight(30)} → ✗ NO MATCH"
        }
    }
    
    // =========================================================================
    // Example 4: Using patterns with channels
    // =========================================================================
    println "\n[Example 4] Using patterns with Nextflow channels"
    
    def samplePattern = Group("id", OneOrMore(WordChar()))
        .then(Literal("_"))
        .then(Group("read", Either(["R1", "R2"])))
    
    println "Pattern: ${samplePattern.toRegex()}"
    
    channel.of(
        "sample001_R1.fastq.gz",
        "sample001_R2.fastq.gz",
        "sample002_R1.fastq.gz",
        "invalid_file.txt"
    )
        .filter { it =~ samplePattern.toRegex() }
        .map { filename ->
            def m = filename =~ samplePattern.toRegex()
            m.find()  // Must call find() before accessing groups
            [
                id: m.group('id'),
                read: m.group('read'),
                file: filename
            ]
        }
        .view { "  Matched: ${it.id} (${it.read}) - ${it.file}" }
    
    println "\n" + "=" * 70
    println "All tests completed!"
    println "=" * 70
}

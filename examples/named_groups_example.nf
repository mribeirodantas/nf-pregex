#!/usr/bin/env nextflow

/*
 * Example demonstrating named capturing groups in nf-pregex
 * 
 * This example shows how to use named groups to extract specific
 * parts of filenames for bioinformatics file processing.
 */

include { 
    Sequence
    Group
    Literal
    OneOrMore
    AnyChar
    CharClass
    WordChar
    Digit
    ReadPair
    FastqExtension
} from 'plugin/nf-pregex'

workflow {
    /*
     * Example 1: Simple pattern with named groups
     * Parse sample name and read pair from filename
     */
    def pattern1 = Sequence([
        Group('samplename', OneOrMore(AnyChar())),
        CharClass('._'),
        Group('rp', ReadPair()),
        Literal('.fastq.gz')
    ])
    
    println "Pattern 1: ${pattern1}"
    // → (?<samplename>(?:.)+)[._](?<rp>(?:R1|R2|1|2))\.fastq\.gz
    
    def filename1 = "sample_123_R1.fastq.gz"
    def matcher1 = filename1 =~ pattern1
    
    if (matcher1.matches()) {
        println "\nFilename: ${filename1}"
        println "Sample name: ${matcher1.group('samplename')}"
        println "Read pair: ${matcher1.group('rp')}"
    }
    
    /*
     * Example 2: Complex Illumina filename pattern
     * Parse sample ID, lane, and read information
     */
    def illuminaPattern = Sequence([
        Group('sample', OneOrMore(WordChar())),
        Literal('_'),
        Group('lane', Literal('L').then(Digit().exactly(3))),
        Literal('_'),
        Group('read', ReadPair()),
        FastqExtension()
    ])
    
    println "\n\nPattern 2: ${illuminaPattern}"
    
    def filename2 = "SampleA_L001_R1.fastq.gz"
    def matcher2 = filename2 =~ illuminaPattern
    
    if (matcher2.matches()) {
        println "\nFilename: ${filename2}"
        println "Sample: ${matcher2.group('sample')}"
        println "Lane: ${matcher2.group('lane')}"
        println "Read: ${matcher2.group('read')}"
    }
    
    /*
     * Example 3: Using named groups in a channel
     * Process multiple files and extract metadata
     */
    println "\n\nExample 3: Processing multiple files"
    
    channel.of(
        "Control_L001_R1.fastq.gz",
        "Treatment_L002_R2.fastq.gz",
        "Sample123_L003_R1.fastq.gz"
    )
    .map { filename ->
        def matcher = filename =~ illuminaPattern
        if (matcher.matches()) {
            [
                sample: matcher.group('sample'),
                lane: matcher.group('lane'),
                read: matcher.group('read'),
                filename: filename
            ]
        }
    }
    .view { metadata ->
        "Parsed: sample=${metadata.sample}, lane=${metadata.lane}, read=${metadata.read}, file=${metadata.filename}"
    }
}

#!/usr/bin/env nextflow

/*
 * Example demonstrating file pattern matching with nf-pregex.
 * 
 * This shows how to use PRegEx patterns for matching and parsing
 * filenames in typical bioinformatics workflows.
 */

include { 
    Either
    Literal
    OneOrMore
    Digit
    WordChar
    Sequence
    Optional
} from 'plugin/nf-pregex'

workflow {
    
    // ========================================
    // Example 1: FASTQ file pattern matching
    // ========================================
    
    // Pattern: sample_name_R1.fastq.gz or sample_name_R2.fastq.gz
    def fastqPattern = Sequence([
        OneOrMore(WordChar()),         // sample name
        Literal("_"),
        Either(["R1", "R2"]),          // read direction
        Literal(".fastq"),
        Optional(Literal(".gz"))       // optional compression
    ])
    
    println "FASTQ Pattern: ${fastqPattern}"
    
    // Test files
    def testFiles = [
        "sample001_R1.fastq.gz",
        "sample001_R2.fastq.gz",
        "control_R1.fastq",
        "sample002_R1.fastq.gz",
        "not_a_fastq.txt"
    ]
    
    println "\nFASTQ matching results:"
    testFiles.each { file ->
        def matches = file =~ /${fastqPattern}/
        println "  ${file}: ${matches ? 'MATCH' : 'NO MATCH'}"
    }
    
    
    // ========================================
    // Example 2: BAM/SAM file pattern
    // ========================================
    
    def bamPattern = Sequence([
        OneOrMore(WordChar()),         // sample name
        Literal("."),
        Either(["bam", "sam"])         // file type
    ])
    
    println "\n\nBAM/SAM Pattern: ${bamPattern}"
    
    
    // ========================================
    // Example 3: Sample ID with conditions
    // ========================================
    
    // Pattern: SampleID format like "S001_T1_R1" or "S001_C1_R2"
    def sampleIdPattern = Sequence([
        Literal("S"),
        Digit().exactly(3),             // 3-digit sample number
        Literal("_"),
        Either(["T", "C"]),             // Treatment or Control
        Digit(),                        // replicate number
        Literal("_"),
        Either(["R1", "R2"])            // read pair
    ])
    
    println "Sample ID Pattern: ${sampleIdPattern}"
    
    def sampleIds = [
        "S001_T1_R1",
        "S002_C1_R2",
        "S123_T2_R1",
        "S01_T1_R1",     // Won't match - only 2 digits
        "S001_X1_R1"     // Won't match - invalid condition
    ]
    
    println "\nSample ID matching results:"
    sampleIds.each { id ->
        def matches = id =~ /${sampleIdPattern}/
        println "  ${id}: ${matches ? 'MATCH' : 'NO MATCH'}"
    }
    
    
    // ========================================
    // Example 4: Using patterns with fromFilePairs
    // ========================================
    
    // Create a pattern for paired-end reads
    def pairedEndPattern = Sequence([
        Literal("*_"),
        Either(["R1", "R2"]),
        Literal(".fastq.gz")
    ])
    
    println "\n\nPaired-end pattern for fromFilePairs: ${pairedEndPattern}"
    println "Usage: channel.fromFilePairs('data/*_{R1,R2}.fastq.gz')"
    
    
    // ========================================
    // Example 5: VCF file versioning
    // ========================================
    
    def vcfPattern = Sequence([
        OneOrMore(WordChar()),         // base name
        Literal("."),
        Optional(Sequence([
            Literal("v"),
            OneOrMore(Digit()),
            Literal(".")
        ])),                           // optional version
        Literal("vcf"),
        Optional(Literal(".gz"))       // optional compression
    ])
    
    println "\n\nVCF Pattern: ${vcfPattern}"
    
    def vcfFiles = [
        "variants.vcf",
        "variants.vcf.gz",
        "variants.v1.vcf.gz",
        "variants.v20.vcf"
    ]
    
    println "VCF matching results:"
    vcfFiles.each { file ->
        def matches = file =~ /${vcfPattern}/
        println "  ${file}: ${matches ? 'MATCH' : 'NO MATCH'}"
    }
}

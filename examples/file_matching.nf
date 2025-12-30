#!/usr/bin/env nextflow

/*
 * Example demonstrating file pattern matching with nf-pregex.
 * 
 * This shows how to use PRegEx patterns for matching and parsing
 * filenames in typical bioinformatics workflows.
 * 
 * To run this example:
 *   nextflow run file_matching.nf -plugins nf-pregex@0.1.0
 */

include { 
    Either
    Literal
    OneOrMore
    Digit
    WordChar
    Sequence
    Optional
    ReadPair
    FastqExtension
    VcfExtension
    AlignmentExtension
    Group
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
        def matches = file =~ fastqPattern.toPattern()
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
    
    
    // ========================================
    // Example 6: Modern bioinformatics patterns
    // ========================================
    
    println "\n\n=== Using Built-in Bioinformatics Patterns ==="
    
    // Using ReadPair() instead of manual Either
    def modernFastqPattern = Sequence([
        OneOrMore(WordChar()),
        Literal("_"),
        ReadPair(),              // Matches _R1, _R2, .1, .2, etc.
        FastqExtension()         // Matches .fastq.gz, .fq, .fastq, etc.
    ])
    
    println "\nModern FASTQ pattern: ${modernFastqPattern}"
    
    def modernFastqFiles = [
        "sample_R1.fastq.gz",
        "sample_R2.fq",
        "test.1.fastq.gz",
        "control.2.fq.gz"
    ]
    
    println "Modern FASTQ matching:"
    modernFastqFiles.each { file ->
        def matches = file =~ /${modernFastqPattern}/
        println "  ${file}: ${matches ? 'MATCH' : 'NO MATCH'}"
    }
    
    
    // ========================================
    // Example 7: Alignment file patterns
    // ========================================
    
    def alignmentPattern = Sequence([
        OneOrMore(WordChar()),
        AlignmentExtension()     // Matches .bam, .sam, .cram
    ])
    
    println "\n\nAlignment pattern: ${alignmentPattern}"
    
    def alignmentFiles = [
        "sample.bam",
        "control.sam",
        "test.cram",
        "data.vcf"               // Won't match
    ]
    
    println "Alignment matching:"
    alignmentFiles.each { file ->
        def matches = file =~ /${alignmentPattern}/
        println "  ${file}: ${matches ? 'MATCH' : 'NO MATCH'}"
    }
    
    
    // ========================================
    // Example 8: Named groups for metadata extraction
    // ========================================
    
    def extractPattern = Sequence([
        Group('sample', OneOrMore(WordChar())),
        Literal("_"),
        Group('lane', Literal("L").then(Digit().exactly(3))),
        Literal("_"),
        Group('read', ReadPair()),
        FastqExtension()
    ])
    
    println "\n\nMetadata extraction pattern: ${extractPattern}"
    
    def illuminaFile = "SampleA_L001_R1.fastq.gz"
    def extractor = illuminaFile =~ extractPattern
    
    if (extractor.matches()) {
        println "\nExtracted from '${illuminaFile}':"
        println "  Sample: ${extractor.group('sample')}"
        println "  Lane: ${extractor.group('lane')}"
        println "  Read: ${extractor.group('read')}"
    }
    
    
    // ========================================
    // Example 9: Method chaining
    // ========================================
    
    println "\n\n=== Method Chaining Example ==="
    
    def chainedPattern = Literal("sample")
        .then(Literal("_"))
        .then(Digit().oneOrMore())
        .then(Literal("_"))
        .then(ReadPair())
        .then(FastqExtension())
    
    println "Chained pattern: ${chainedPattern}"
    
    def chainTest = "sample_001_R1.fastq.gz"
    println "Testing '${chainTest}': ${chainTest =~ /${chainedPattern}/ ? 'MATCH' : 'NO MATCH'}"
}

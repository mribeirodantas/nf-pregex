#!/usr/bin/env nextflow

/*
 * Comprehensive example showcasing bioinformatics-specific patterns.
 * 
 * This example demonstrates the built-in bioinformatics patterns
 * that simplify common file matching and validation tasks.
 * 
 * To run this example:
 *   nextflow run bioinformatics_patterns.nf -plugins nf-pregex@0.1.0
 */

include {
    // File extension patterns
    FastqExtension
    VcfExtension
    AlignmentExtension
    BedExtension
    GffGtfExtension
    FastaExtension
    
    // Genomic identifiers
    ReadPair
    Chromosome
    StrictChromosome
    
    // Sequence patterns
    DNASequence
    StrictDNASequence
    DNASequenceWithAmbiguity
    ProteinSequence
    
    // Basic builders
    Sequence
    Literal
    OneOrMore
    Optional
    WordChar
    Digit
    Group
} from 'plugin/nf-pregex'

workflow {
    
    println "=" * 70
    println "BIOINFORMATICS PATTERN MATCHING EXAMPLES"
    println "=" * 70
    
    
    // ========================================
    // Example 1: File Extension Patterns
    // ========================================
    
    println "\n\n=== File Extension Patterns ==="
    
    def testFiles = [
        "sample.fastq.gz": FastqExtension(),
        "data.fq": FastqExtension(),
        "variants.vcf.gz": VcfExtension(),
        "calls.bcf": VcfExtension(),
        "aligned.bam": AlignmentExtension(),
        "sorted.sam": AlignmentExtension(),
        "genome.fa.gz": FastaExtension(),
        "genes.gtf.gz": GffGtfExtension(),
        "peaks.bed": BedExtension()
    ]
    
    testFiles.each { filename, pattern ->
        def matches = filename =~ /${pattern}/
        println "  ${filename.padRight(20)} → ${matches ? '✓ MATCH' : '✗ NO MATCH'}"
    }
    
    
    // ========================================
    // Example 2: Read Pair Patterns
    // ========================================
    
    println "\n\n=== Read Pair Patterns ==="
    println "ReadPair() matches: _R1, _R2, .R1, .R2, _1, _2, .1, .2\n"
    
    def readPairFiles = [
        "sample_R1.fastq.gz",
        "sample_R2.fastq.gz",
        "test.1.fq",
        "test.2.fq",
        "control_1.fastq",
        "control_2.fastq"
    ]
    
    def readPairPattern = Sequence([
        OneOrMore(WordChar()),
        ReadPair(),
        FastqExtension()
    ])
    
    println "Pattern: ${readPairPattern}\n"
    
    readPairFiles.each { file ->
        def matches = file =~ /${readPairPattern}/
        println "  ${file.padRight(25)} → ${matches ? '✓ MATCH' : '✗ NO MATCH'}"
    }
    
    
    // ========================================
    // Example 3: Chromosome Patterns
    // ========================================
    
    println "\n\n=== Chromosome Patterns ==="
    
    def chromosomes = [
        "chr1",
        "chrX",
        "chrY",
        "chrM",
        "1",          // Chromosome() allows with or without 'chr'
        "22",
        "chrUn"       // Won't match - not a standard chromosome
    ]
    
    println "Chromosome() - flexible (with or without 'chr'):\n"
    chromosomes.each { chr ->
        def matches = chr =~ /${Chromosome()}/
        println "  ${chr.padRight(10)} → ${matches ? '✓ MATCH' : '✗ NO MATCH'}"
    }
    
    println "\n\nStrictChromosome() - requires 'chr' prefix:\n"
    chromosomes.each { chr ->
        def matches = chr =~ /${StrictChromosome()}/
        println "  ${chr.padRight(10)} → ${matches ? '✓ MATCH' : '✗ NO MATCH'}"
    }
    
    
    // ========================================
    // Example 4: Sequence Validation
    // ========================================
    
    println "\n\n=== DNA Sequence Validation ==="
    
    def sequences = [
        "ACGTACGT": "Valid DNA",
        "acgtacgt": "Valid DNA (lowercase)",
        "ACGTNNNN": "DNA with ambiguity codes",
        "ACGTRYSWKMN": "DNA with all IUPAC codes",
        "ACGTU": "Invalid (contains U - RNA)",
        "ACGT123": "Invalid (contains numbers)"
    ]
    
    sequences.each { seq, description ->
        println "\n${description}: ${seq}"
        println "  DNASequence(): ${seq =~ /${DNASequence()}/ ? '✓' : '✗'}"
        println "  StrictDNASequence(): ${seq =~ /${StrictDNASequence()}/ ? '✓' : '✗'}"
        println "  DNASequenceWithAmbiguity(): ${seq =~ /${DNASequenceWithAmbiguity()}/ ? '✓' : '✗'}"
    }
    
    
    // ========================================
    // Example 5: Named Groups for Metadata
    // ========================================
    
    println "\n\n=== Extracting Metadata with Named Groups ==="
    
    def illuminaPattern = Sequence([
        Group('sample', OneOrMore(WordChar())),
        Literal('_'),
        Group('lane', Literal('L').then(Digit().exactly(3))),
        Literal('_'),
        Group('read', ReadPair()),
        FastqExtension()
    ])
    
    println "Pattern: ${illuminaPattern}\n"
    
    def illuminaFiles = [
        "SampleA_L001_R1.fastq.gz",
        "Control_L002_R2.fq",
        "Test_L004_1.fastq.gz"
    ]
    
    illuminaFiles.each { filename ->
        def matcher = filename =~ illuminaPattern
        if (matcher.matches()) {
            println "${filename}:"
            println "  Sample: ${matcher.group('sample')}"
            println "  Lane: ${matcher.group('lane')}"
            println "  Read: ${matcher.group('read')}"
        }
    }
    
    
    // ========================================
    // Example 6: VCF with Chromosome
    // ========================================
    
    println "\n\n=== VCF Files with Chromosome Info ==="
    
    def vcfChrPattern = Sequence([
        Group('chr', Chromosome()),
        Literal('_'),
        Group('type', OneOrMore(WordChar())),
        VcfExtension()
    ])
    
    println "Pattern: ${vcfChrPattern}\n"
    
    def vcfFiles = [
        "chr1_variants.vcf.gz",
        "chrX_filtered.bcf",
        "22_calls.vcf"
    ]
    
    vcfFiles.each { file ->
        def matcher = file =~ vcfChrPattern
        if (matcher.matches()) {
            println "${file}:"
            println "  Chromosome: ${matcher.group('chr')}"
            println "  Type: ${matcher.group('type')}"
        }
    }
    
    
    // ========================================
    // Example 7: Method Chaining
    // ========================================
    
    println "\n\n=== Method Chaining for Cleaner Code ==="
    
    def chainedPattern = Literal("sample")
        .then(Literal("_"))
        .then(Digit().oneOrMore())
        .then(Literal("_"))
        .then(ReadPair())
        .then(FastqExtension())
    
    println "Chained pattern: ${chainedPattern}\n"
    
    def chainedFiles = [
        "sample_123_R1.fastq.gz",
        "sample_456_R2.fq",
        "test_789_1.fastq.gz"
    ]
    
    println "Testing files:"
    chainedFiles.each { file ->
        println "  ${file.padRight(30)} → ${file =~ /${chainedPattern}/ ? '✓ MATCH' : '✗ NO MATCH'}"
    }
    
    
    // ========================================
    // Example 8: Complete Pipeline Pattern
    // ========================================
    
    println "\n\n=== Complete Pipeline File Pattern ==="
    
    def pipelinePattern = Sequence([
        Group('sample', OneOrMore(WordChar())),
        Literal('_'),
        Optional(Sequence([
            Group('condition', Either(['treated', 'control'])),
            Literal('_')
        ])),
        Group('replicate', Literal('rep').then(Digit())),
        Literal('_'),
        Group('read', ReadPair()),
        FastqExtension()
    ])
    
    println "Pattern: ${pipelinePattern}\n"
    
    def pipelineFiles = [
        "geneA_treated_rep1_R1.fastq.gz",
        "geneB_control_rep2_R2.fq",
        "geneC_rep3_1.fastq.gz"
    ]
    
    println "Extracting metadata:"
    pipelineFiles.each { file ->
        def matcher = file =~ pipelinePattern
        if (matcher.matches()) {
            println "\n${file}:"
            println "  Sample: ${matcher.group('sample')}"
            if (matcher.group('condition')) {
                println "  Condition: ${matcher.group('condition')}"
            }
            println "  Replicate: ${matcher.group('replicate')}"
            println "  Read: ${matcher.group('read')}"
        }
    }
    
    
    println "\n\n" + "=" * 70
    println "For more examples, see: examples/basic_usage.nf and examples/file_matching.nf"
    println "=" * 70
}

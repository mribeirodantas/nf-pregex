#!/usr/bin/env nextflow

/*
 * Simple RNA-seq Pipeline Demonstrating nf-pregex Plugin Benefits
 * 
 * This pipeline showcases how nf-pregex improves pattern matching and validation
 * in bioinformatics workflows through human-readable regex builders.
 * 
 * Key Benefits Demonstrated:
 * 1. Readable input validation patterns
 * 2. Type-safe file extension matching
 * 3. Sample metadata extraction using clear patterns
 * 4. Bioinformatics-specific pattern libraries
 */

// Import nf-pregex pattern builders
include { 
    // Pattern builders
    Sequence
    Literal
    Either
    OneOrMore
    Optional
    
    // Character classes
    WordChar
    Digit
    
    // Bioinformatics patterns
    DNASequence
    FastqExtension
    ReadPair
    AlignmentExtension
    Chromosome
} from 'plugin/nf-pregex'

/*
 * Pipeline Parameters
 */
params.reads = "data/*_{R1,R2}.fastq.gz"
params.reference = "reference/genome.fa"
params.outdir = "results"
params.skip_qc = false

/*
 * BENEFIT #1: Clear, Self-Documenting Input Validation
 * 
 * Traditional regex: def pattern = /(\w)+_(R1|R2)\.fastq(\.gz)?/
 * With nf-pregex: See below - much more readable!
 */
def validateInputFiles(filePath) {
    // Define pattern for valid FASTQ paired-end files
    def validFastqPattern = Sequence(
        OneOrMore(WordChar()),     // Sample name (readable!)
        ReadPair(),                // _R1, _R2, etc. (semantic!)
        FastqExtension()           // .fastq.gz, .fq (standardized!)
    )
    
    def fileName = filePath.name
    def patternStr = validFastqPattern.toString()
    def isValid = fileName =~ patternStr
    
    if (!isValid) {
        error "Invalid input file: ${fileName}. Expected format: samplename_{R1,R2}.fastq[.gz]"
    }
    
    return isValid
}

/*
 * BENEFIT #2: Extract Metadata with Understandable Patterns
 */
def extractSampleInfo(fileName) {
    // Pattern to parse: sampleID_condition_replicate_{R1,R2}.fastq.gz
    // Example: SAMPLE001_control_rep1_R1.fastq.gz
    
    def samplePattern = Sequence(
        OneOrMore(WordChar()),     // Sample ID
        Literal("_"),
        OneOrMore(WordChar()),     // Condition
        Literal("_"),
        Literal("rep"),
        Digit(),                   // Replicate number
        Literal("_"),
        Either(["R1", "R2"]),
        FastqExtension()
    )
    
    // Try to match the complex pattern
    def patternStr = samplePattern.toString()
    def matcher = fileName =~ patternStr
    if (matcher) {
        // Extract components (simplified for demo)
        return [valid: true, pattern: samplePattern.toString()]
    }
    
    // Fall back to simple pattern
    def simpleSample = Sequence(
        OneOrMore(WordChar()),
        ReadPair(),
        FastqExtension()
    )
    
    return [valid: true, pattern: simpleSample.toString()]
}

/*
 * Process: Quality Control with FastQC
 * Demonstrates pattern validation in process input
 */
process FASTQC {
    tag "${meta.id}"
    publishDir "${params.outdir}/fastqc", mode: 'copy'
    
    input:
    tuple val(meta), path(reads)
    
    output:
    tuple val(meta), path("*.html"), emit: html
    tuple val(meta), path("*.zip"), emit: zip
    
    script:
    """
    # Using nf-pregex for clear validation messaging
    echo "Processing: ${meta.id}"
    echo "Pattern validation: ${meta.pattern}"
    
    # Mock FastQC (would be: fastqc -q ${reads})
    for read in ${reads}; do
        touch "\${read%.fastq.gz}_fastqc.html"
        touch "\${read%.fastq.gz}_fastqc.zip"
    done
    """
}

/*
 * Process: Trimming with Trimmomatic
 * Shows how nf-pregex patterns make output naming clear
 */
process TRIMMOMATIC {
    tag "${meta.id}"
    publishDir "${params.outdir}/trimmed", mode: 'copy'
    
    input:
    tuple val(meta), path(reads)
    
    output:
    tuple val(meta), path("*_trimmed_{R1,R2}.fastq.gz"), emit: reads
    tuple val(meta), path("*.log"), emit: log
    
    script:
    // BENEFIT #3: Document expected output format with readable patterns
    def trimmedPattern = Sequence(
        Literal(meta.id),
        Literal("_trimmed"),
        ReadPair(),
        FastqExtension()
    )
    
    """
    # Expected output pattern: ${trimmedPattern}
    echo "Trimming reads for ${meta.id}"
    
    # Mock trimming (would be actual Trimmomatic command)
    touch ${meta.id}_trimmed_R1.fastq.gz
    touch ${meta.id}_trimmed_R2.fastq.gz
    touch ${meta.id}_trimming.log
    """
}

/*
 * Process: Alignment with STAR
 * Demonstrates bioinformatics file extension patterns
 */
process STAR_ALIGN {
    tag "${meta.id}"
    publishDir "${params.outdir}/alignments", mode: 'copy'
    
    input:
    tuple val(meta), path(reads)
    path reference
    
    output:
    tuple val(meta), path("*.bam"), emit: bam
    tuple val(meta), path("*.log"), emit: log
    
    script:
    // BENEFIT #4: Use standardized bioinformatics patterns
    def bamPattern = Sequence(
        Literal(meta.id),
        AlignmentExtension()       // .bam, .sam, .cram - clear!
    )
    
    """
    # Expected output: ${bamPattern}
    echo "Aligning ${meta.id} to reference"
    
    # Mock alignment (would be actual STAR command)
    touch ${meta.id}.bam
    touch ${meta.id}.STAR.log
    """
}

/*
 * Process: Feature Counting
 * Shows chromosome pattern usage
 */
process FEATURECOUNTS {
    tag "${meta.id}"
    publishDir "${params.outdir}/counts", mode: 'copy'
    
    input:
    tuple val(meta), path(bam)
    
    output:
    tuple val(meta), path("*.counts.txt"), emit: counts
    
    script:
    // BENEFIT #5: Validate chromosome names in output
    def chrPattern = Chromosome()  // chr1, chr2, chrX, etc. - semantic!
    
    """
    # Processing chromosomes matching: ${chrPattern}
    echo "Counting features for ${meta.id}"
    
    # Mock counting (would be actual featureCounts command)
    echo "gene_id\t${meta.id}" > ${meta.id}.counts.txt
    echo "GENE001\t100" >> ${meta.id}.counts.txt
    echo "GENE002\t250" >> ${meta.id}.counts.txt
    """
}

/*
 * Main Workflow
 */
workflow {
    
    // Display patterns for user understanding
    log.info """
    ╔════════════════════════════════════════════════════════════════╗
    ║  RNA-seq Pipeline with nf-pregex - Pattern Showcase           ║
    ╚════════════════════════════════════════════════════════════════╝
    
    Input Validation Patterns:
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    """
    
    // Demonstrate pattern creation with nf-pregex
    def fastqPattern = Sequence(
        OneOrMore(WordChar()),
        ReadPair(),
        FastqExtension()
    )
    
    def bamPattern = Sequence(
        OneOrMore(WordChar()),
        AlignmentExtension()
    )
    
    log.info "  FASTQ files:     ${fastqPattern}"
    log.info "  Alignment files: ${bamPattern}"
    log.info "  Chromosomes:     ${Chromosome()}"
    log.info ""
    log.info "Benefits of nf-pregex:"
    log.info "  ✓ Readable pattern definitions"
    log.info "  ✓ Self-documenting code"
    log.info "  ✓ Bioinformatics-specific patterns"
    log.info "  ✓ Reduced regex errors"
    log.info "  ✓ Better maintainability"
    log.info ""
    
    // Create input channel with validation
    def reads_ch = channel
        .fromFilePairs(params.reads, checkIfExists: false)
        .map { id, files ->
            // Validate each file
            files.each { file -> validateInputFiles(file) }
            
            // Extract metadata with readable patterns
            def info = extractSampleInfo(files[0].name)
            
            // Create metadata map
            def meta = [
                id: id,
                pattern: info.pattern
            ]
            
            return tuple(meta, files)
        }
    
    // Run QC
    if (!params.skip_qc) {
        FASTQC(reads_ch)
    }
    
    // Trim reads
    TRIMMOMATIC(reads_ch)
    
    // Align reads
    def reference_ch = channel.fromPath(params.reference, checkIfExists: false)
    STAR_ALIGN(TRIMMOMATIC.out.reads, reference_ch)
    
    // Count features
    FEATURECOUNTS(STAR_ALIGN.out.bam)
    
    // Display completion message with pattern summary
    workflow.onComplete {
        log.info """
        ╔════════════════════════════════════════════════════════════════╗
        ║  Pipeline Complete!                                            ║
        ╚════════════════════════════════════════════════════════════════╝
        
        nf-pregex patterns used:
          • ReadPair():          ${ReadPair()}
          • FastqExtension():    ${FastqExtension()}
          • AlignmentExtension(): ${AlignmentExtension()}
          • Chromosome():        ${Chromosome()}
        
        Results: ${params.outdir}
        Status: ${workflow.success ? 'SUCCESS' : 'FAILED'}
        """
    }
}

#!/usr/bin/env nextflow

/*
 * Simple RNA-seq Pipeline Example with nf-pregex
 * 
 * A minimal RNA-seq workflow demonstrating practical pattern matching
 * with the nf-pregex plugin for input validation and file handling.
 * 
 * Pipeline steps:
 * 1. FastQC - Quality control of raw reads
 * 2. Salmon - Transcript quantification (quasi-mapping mode)
 */

// Import nf-pregex for input validation
include { 
    Sequence
    OneOrMore
    WordChar
    ReadPair
    FastqExtension
} from 'plugin/nf-pregex'

/*
 * Pipeline Parameters
 */
params.reads = "data/*_{1,2}.fastq.gz"
params.transcriptome = null
params.outdir = "results"
params.help = false

/*
 * Process: Quality Control with FastQC
 */
process FASTQC {
    tag "${meta.id}"
    container 'community.wave.seqera.io/library/fastqc:0.12.1--c32dc0120769c2c0'
    publishDir "${params.outdir}/fastqc", mode: 'copy'
    
    input:
    tuple val(meta), path(reads)
    
    output:
    tuple val(meta), path("*.html"), emit: html
    tuple val(meta), path("*.zip"), emit: zip
    
    script:
    """
    fastqc -q -t ${task.cpus} ${reads}
    """
}

/*
 * Process: Build Salmon index
 */
process SALMON_INDEX {
    tag "${transcriptome.name}"
    container 'community.wave.seqera.io/library/salmon:1.10.3--93ecff8e9d4d2cd9'
    
    input:
    path transcriptome
    
    output:
    path "salmon_index", emit: index
    
    script:
    """
    salmon index \\
        -t ${transcriptome} \\
        -i salmon_index \\
        -k 31 \\
        -p ${task.cpus}
    """
}

/*
 * Process: Salmon quantification
 */
process SALMON_QUANT {
    tag "${meta.id}"
    container 'community.wave.seqera.io/library/salmon:1.10.3--93ecff8e9d4d2cd9'
    publishDir "${params.outdir}/salmon", mode: 'copy'
    
    input:
    tuple val(meta), path(reads)
    path index
    
    output:
    tuple val(meta), path("${meta.id}"), emit: results
    tuple val(meta), path("${meta.id}/quant.sf"), emit: quant
    
    script:
    def read1 = reads[0]
    def read2 = reads[1]
    """
    salmon quant \\
        -i ${index} \\
        -l A \\
        -1 ${read1} \\
        -2 ${read2} \\
        -p ${task.cpus} \\
        --validateMappings \\
        -o ${meta.id}
    """
}

/*
 * Process: MultiQC aggregation
 */
process MULTIQC {
    container 'community.wave.seqera.io/library/multiqc:1.25.4--0bd44c59c1e5f89c'
    publishDir "${params.outdir}/multiqc", mode: 'copy'
    
    input:
    path('fastqc/*')
    path('salmon/*')
    
    output:
    path("multiqc_report.html"), emit: html
    path("multiqc_data"), emit: data
    
    script:
    """
    multiqc . \\
        --force \\
        --filename multiqc_report.html
    """
}

/*
 * Main Workflow
 */
workflow {
    
    // Show help message
    if (params.help) {
        log.info """
        Usage:
          nextflow run main.nf --reads 'data/*_{1,2}.fastq.gz' --transcriptome transcriptome.fa
        
        Required arguments:
          --reads            Path to paired-end FASTQ files
          --transcriptome    Path to transcriptome FASTA file
        
        Optional arguments:
          --outdir           Output directory (default: results)
          --help             Show this help message
        """
        exit 0
    }
    
    // Validate required parameters
    if (!params.transcriptome) {
        error "Please provide a transcriptome file with --transcriptome"
    }
    
    // Display patterns for user understanding
    log.info """
    ╔════════════════════════════════════════════════════════════════╗
    ║  RNA-seq Pipeline with nf-pregex                               ║
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
    
    log.info "  FASTQ files:     ${fastqPattern}"
    log.info ""
    log.info "Benefits of nf-pregex:"
    log.info "  ✓ Readable pattern definitions"
    log.info "  ✓ Self-documenting code"
    log.info "  ✓ Bioinformatics-specific patterns"
    log.info "  ✓ Reduced regex errors"
    log.info "  ✓ Better maintainability"
    log.info ""
    
    // Create input channel with metadata
    def reads_ch = channel
        .fromFilePairs(params.reads, checkIfExists: true)
        .map { id, files ->
            def meta = [id: id]
            tuple(meta, files)
        }
    
    // Build Salmon index
    def transcriptome_ch = channel.fromPath(params.transcriptome, checkIfExists: true)
    SALMON_INDEX(transcriptome_ch)
    
    // Run FastQC
    FASTQC(reads_ch)
    
    // Run Salmon quantification
    SALMON_QUANT(reads_ch, SALMON_INDEX.out.index)
    
    // Collect QC outputs for MultiQC
    def fastqc_ch = FASTQC.out.zip.map { _meta, zip -> zip }.collect()
    def salmon_ch = SALMON_QUANT.out.results.map { _meta, dir -> dir }.collect()
    
    // Run MultiQC
    MULTIQC(fastqc_ch, salmon_ch)
    
    // Display completion message with pattern summary
    workflow.onComplete {
        log.info """
        ╔════════════════════════════════════════════════════════════════╗
        ║  Pipeline Complete!                                            ║
        ╚════════════════════════════════════════════════════════════════╝
        
        nf-pregex patterns used:
          • ReadPair():       ${ReadPair()}
          • FastqExtension(): ${FastqExtension()}
        
        Results: ${params.outdir}
        Status: ${workflow.success ? 'SUCCESS' : 'FAILED'}
        """
    }
}

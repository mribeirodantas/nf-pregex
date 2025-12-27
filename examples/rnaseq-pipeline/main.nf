#!/usr/bin/env nextflow

/*
 * Illumina FASTQ Filename Parsing Example
 * 
 * Demonstrates how nf-pregex solves the real-world problem of parsing
 * complex Illumina FASTQ filenames to extract embedded metadata.
 * 
 * Illumina filename format: SAMPLE_001_S1_L001_R1_001.fastq.gz
 *   - SAMPLE_001: Sample name
 *   - S1: Sample number (order in sample sheet)
 *   - L001: Lane number
 *   - R1: Read direction (R1 or R2 for paired-end)
 *   - 001: Chunk/segment number
 * 
 * This is the actual pain point shown in Nextflow training materials!
 * Traditional regex: /^(.+)_S(\d+)_L(\d{3})_(R[12])_(\d{3})\.fastq(?:\.gz)?$/
 *                    ^^^ Hard to read, easy to break, difficult to maintain
 */

// Import nf-pregex patterns
include { 
    Sequence
    Literal
    OneOrMore
    Digit
    Either
    WordChar
    Optional
} from 'plugin/nf-pregex'

/*
 * Pipeline Parameters
 */
params.reads = "data/*_S*_L*_R*_*.fastq.gz"
params.outdir = "results"
params.help = false

/*
 * Process: Parse and display FASTQ metadata
 * 
 * This process demonstrates metadata extraction from Illumina filenames.
 * In a real pipeline, this metadata would be used for:
 * - Quality control decisions
 * - Lane merging
 * - Sample demultiplexing
 * - Output organization
 */
process PARSE_AND_QC {
    tag "${meta.sample_name}"
    publishDir "${params.outdir}/parsed_metadata", mode: 'copy'
    
    input:
    tuple val(meta), path(reads)
    
    output:
    tuple val(meta), path("${meta.sample_name}_metadata.json")
    
    script:
    """
    cat > ${meta.sample_name}_metadata.json <<EOF
    {
      "sample_name": "${meta.sample_name}",
      "sample_number": ${meta.sample_num},
      "lane": "${meta.lane}",
      "read": "${meta.read}",
      "chunk": "${meta.chunk}",
      "filename": "${reads.name}",
      "file_size": "${reads.size()} bytes",
      "parsing_method": "nf-pregex"
    }
    EOF
    
    echo "✅ Successfully parsed: ${reads.name}"
    echo "   Sample: ${meta.sample_name}"
    echo "   Lane: ${meta.lane}, Read: ${meta.read}"
    """
}

/*
 * Helper function: Parse Illumina filename using TRADITIONAL REGEX
 * This is the approach shown in Nextflow training materials
 */
def parseFilenameTraditional(fastq_path) {
    // Traditional regex - cryptic and error-prone!
    def m = (fastq_path.name =~ /^(.+)_S(\d+)_L(\d{3})_(R[12])_(\d{3})\.fastq(?:\.gz)?$/)
    
    if (!m) {
        return null
    }
    
    return [
        sample_name: m[0][1],
        sample_num: m[0][2].toInteger(),
        lane: m[0][3],
        read: m[0][4],
        chunk: m[0][5]
    ]
}

/*
 * Helper function: Parse Illumina filename using NF-PREGEX
 * Clean, readable, and self-documenting!
 */
def parseFilenameWithPregex(fastq_path) {
    // Build a readable pattern using nf-pregex
    // Note: OneOrMore, Either, and quantifiers already create capturing groups
    def illuminaPattern = Sequence([
        // Sample name: one or more word characters (capture group 1)
        OneOrMore(WordChar()),
        Literal("_S"),
        // Sample number: one or more digits (capture group 2)
        OneOrMore(Digit()),
        Literal("_L"),
        // Lane: exactly 3 digits (capture group 3)
        Digit().exactly(3),
        Literal("_"),
        // Read direction: R1 or R2 (capture group 4)
        Either(["R1", "R2"]),
        Literal("_"),
        // Chunk: exactly 3 digits (capture group 5)
        Digit().exactly(3),
        // Extension: .fastq or .fastq.gz
        Literal(".fastq"),
        Optional(Literal(".gz"))
    ])
    
    def m = (fastq_path.name =~ illuminaPattern)
    
    if (!m) {
        return null
    }
    
    return [
        sample_name: m[0][1],
        sample_num: m[0][2].toInteger(),
        lane: m[0][3],
        read: m[0][4],
        chunk: m[0][5]
    ]
}

/*
 * Main Workflow
 */
workflow {
    
    // Show help message
    if (params.help) {
        log.info """
        ╔════════════════════════════════════════════════════════════════╗
        ║  Illumina FASTQ Filename Parsing Example                      ║
        ╚════════════════════════════════════════════════════════════════╝
        
        Usage:
          nextflow run main.nf --reads 'data/*_S*_L*_R*_*.fastq.gz'
        
        This example demonstrates parsing Illumina FASTQ filenames like:
          SAMPLE_001_S1_L001_R1_001.fastq.gz
        
        Arguments:
          --reads    Path pattern to Illumina FASTQ files
          --outdir   Output directory (default: results)
          --help     Show this help message
        
        The pipeline extracts metadata:
          - Sample name (SAMPLE_001)
          - Sample number (S1)
          - Lane (L001)
          - Read direction (R1/R2)
          - Chunk number (001)
        """
        exit 0
    }
    
    log.info """
    ╔════════════════════════════════════════════════════════════════╗
    ║  Illumina FASTQ Filename Parsing with nf-pregex               ║
    ╚════════════════════════════════════════════════════════════════╝
    
    Problem: Illumina filenames encode metadata
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    Format: SAMPLE_001_S1_L001_R1_001.fastq.gz
             └─┬──┘  │    └─┬─┘ │└─┬─┘
               │     │      │   │  └─ Chunk (001)
               │     │      │   └──── Read (R1/R2)
               │     │      └──────── Lane (L001)
               │     └─────────────── Sample # (S1)
               └───────────────────── Sample name
    
    Traditional Regex (from Nextflow training):
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    /^(.+)_S(\\d+)_L(\\d{3})_(R[12])_(\\d{3})\\.fastq(?:\\.gz)?\$/
    
    ❌ Problems:
       • Hard to read - what does \\d{3} mean?
       • Easy to forget escaping (\\. vs .)
       • Difficult to modify
       • No self-documentation
       • Prone to subtle bugs
    
    With nf-pregex:
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    Sequence([
        OneOrMore(WordChar()),                  // Sample name - Clear!
        Literal("_S"),
        OneOrMore(Digit()),                     // Sample # - Readable!
        Literal("_L"),
        Digit().exactly(3),                     // Lane - Self-documenting!
        Literal("_"),
        Either(["R1", "R2"]),                   // Read - Obvious meaning!
        Literal("_"),
        Digit().exactly(3),                     // Chunk
        Literal(".fastq"),
        Optional(Literal(".gz"))
    ])
    
    ✅ Benefits:
       • Instantly understandable
       • Automatic escaping
       • Easy to modify
       • Self-documenting
       • Capture groups for metadata extraction
    
    Processing files...
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    """
    
    // Create input channel and parse filenames
    def reads_ch = channel
        .fromPath(params.reads, checkIfExists: true)
        .map { fastq_path ->
            // Parse filename using nf-pregex
            def file_meta = parseFilenameWithPregex(fastq_path)
            
            if (!file_meta) {
                error "Failed to parse filename: ${fastq_path.name}\nExpected Illumina format: SAMPLE_S#_L###_R#_###.fastq.gz"
            }
            
            log.info "  ✓ Parsed: ${fastq_path.name}"
            log.info "    └─ Sample: ${file_meta.sample_name} | Lane: ${file_meta.lane} | Read: ${file_meta.read}"
            
            tuple(file_meta, fastq_path)
        }
    
    // Run parsing and QC process
    PARSE_AND_QC(reads_ch)
}

/*
 * Workflow completion handler
 */
workflow.onComplete {
    def status_icon = workflow.success ? '✅ SUCCESS' : '❌ FAILED'
    log.info """
    
    ╔════════════════════════════════════════════════════════════════╗
    ║  Parsing Complete!                                             ║
    ╚════════════════════════════════════════════════════════════════╝
    
    Results: ${params.outdir}/parsed_metadata/
    
    Compare the approaches:
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    Traditional:  /^(.+)_S(\\d+)_L(\\d{3})_(R[12])_(\\d{3})\\.fastq(?:\\.gz)?\$/
    nf-pregex:    Sequence(OneOrMore(WordChar()), Literal("_S"), ...)
    
    Which would YOU rather maintain? 🤔
    
    Status: ${status_icon}
    """
}

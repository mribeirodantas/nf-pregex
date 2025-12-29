package io.nextflow.pregex

import groovy.transform.CompileStatic
import io.nextflow.pregex.PRegEx.*

/**
 * Pre-built regex patterns for common bioinformatics use cases.
 * 
 * This class provides ready-to-use patterns for matching DNA sequences,
 * protein sequences, chromosome names, read pair identifiers, and common
 * bioinformatics file extensions.
 * 
 * @author Seqera AI
 */
@CompileStatic
class BioinformaticsPatterns {
    
    /**
     * Matches a DNA sequence (one or more ACGT nucleotides).
     * Case-insensitive by default.
     * 
     * Examples:
     * - ACGT
     * - acgt
     * - ATCGATCG
     * 
     * @return A pattern matching DNA sequences
     */
    static PRegEx DNASequence() {
        return new CharClass("ACGTacgt", false).oneOrMore()
    }
    
    /**
     * Matches a strict DNA sequence (uppercase ACGT only).
     * 
     * Examples:
     * - ACGT
     * - ATCGATCG
     * 
     * @return A pattern matching uppercase DNA sequences
     */
    static PRegEx StrictDNASequence() {
        return new CharClass("ACGT", false).oneOrMore()
    }
    
    /**
     * Matches a DNA sequence with IUPAC ambiguity codes.
     * Includes: A, C, G, T, R (A or G), Y (C or T), S (G or C), W (A or T),
     * K (G or T), M (A or C), B (not A), D (not C), H (not G), V (not T), N (any)
     * 
     * Examples:
     * - ACGTRYMKSW
     * - ATCGN
     * - ACGTBDHV
     * 
     * @return A pattern matching DNA sequences with ambiguity codes
     */
    static PRegEx DNASequenceWithAmbiguity() {
        return new CharClass("ACGTRYSWKMBDHVNacgtryswkmbdhvn", false).oneOrMore()
    }
    
    /**
     * Matches a protein sequence (one or more amino acids).
     * Case-insensitive. Includes the 20 standard amino acids:
     * A, C, D, E, F, G, H, I, K, L, M, N, P, Q, R, S, T, V, W, Y
     * 
     * Examples:
     * - ACDEFGHIKLMNPQRSTVWY
     * - acdefghiklmnpqrstvwy
     * - MVHLTPEEK
     * 
     * @return A pattern matching protein sequences
     */
    static PRegEx ProteinSequence() {
        return new CharClass("ACDEFGHIKLMNPQRSTVWYacdefghiklmnpqrstvwy", false).oneOrMore()
    }
    
    /**
     * Matches a strict protein sequence (uppercase amino acids only).
     * 
     * Examples:
     * - ACDEFGHIKLMNPQRSTVWY
     * - MVHLTPEEK
     * 
     * @return A pattern matching uppercase protein sequences
     */
    static PRegEx StrictProteinSequence() {
        return new CharClass("ACDEFGHIKLMNPQRSTVWY", false).oneOrMore()
    }
    
    /**
     * Matches a protein sequence with ambiguity codes.
     * Includes standard amino acids plus: B (Asx), Z (Glx), X (any), * (stop)
     * 
     * Examples:
     * - MVHLTPEEKX
     * - ACDEFGHBZX*
     * 
     * @return A pattern matching protein sequences with ambiguity codes
     */
    static PRegEx ProteinSequenceWithAmbiguity() {
        return new CharClass("ACDEFGHIKLMNPQRSTVWYBZXacdefghiklmnpqrstvwybzx*", false).oneOrMore()
    }
    
    /**
     * Matches Phred quality scores (Phred+33 encoding).
     * Uses character range from ! to ~ (ASCII 33-126).
     * 
     * Examples:
     * - !!!FFFFFF
     * - IIIIIIIIII
     * - ~~~~~~~~~~
     * 
     * @return A pattern matching Phred quality scores
     */
    static PRegEx PhredQuality() {
        // Match Phred quality scores (Phred+33 encoding, ASCII 33-126)
        // Use a simpler approach - match any printable ASCII character
        // We'll create a pattern that matches characters in the ! to ~ range
        def qualityPattern = new PRegEx() {
            @Override
            String toRegex() {
                return "[!-~]+"  // Character class range from ! (33) to ~ (126)
            }
        }
        return qualityPattern
    }
    
    /**
     * Matches chromosome names in various formats.
     * Supports: chr1-22, chrX, chrY, chrM (with optional 'chr' prefix)
     * Case-insensitive for X, Y, M
     * 
     * Examples:
     * - chr1, chr22, chrX, chrY, chrM
     * - 1, 22, X, Y, M
     * 
     * @return A pattern matching chromosome names
     */
    static PRegEx Chromosome() {
        // Matches chromosome names with optional 'chr' prefix
        // Supports: 1-22, X, Y, M with or without 'chr' prefix
        def alternatives = [] as List<String>
        // With chr prefix
        (1..22).each { alternatives << "chr${it}".toString() }
        alternatives.addAll(['chrX', 'chrY', 'chrM', 'chrx', 'chry', 'chrm'])
        // Without chr prefix
        (1..22).each { alternatives << "${it}".toString() }
        alternatives.addAll(['X', 'Y', 'M', 'x', 'y', 'm'])
        
        return new Either(alternatives)
    }
    
    /**
     * Matches chromosome names that require the 'chr' prefix.
     * Supports: chr1-22, chrX, chrY, chrM
     * Case-insensitive for X, Y, M
     * 
     * Examples:
     * - chr1
     * - chr22
     * - chrX
     * - chrY
     * - chrM
     * 
     * @return A pattern matching strict chromosome names
     */
    static PRegEx StrictChromosome() {
        // Matches chromosome names that require the 'chr' prefix
        // Supports: chr1-22, chrX, chrY, chrM (case-insensitive for letters)
        def alternatives = [] as List<String>
        (1..22).each { alternatives << "chr${it}".toString() }
        alternatives.addAll(['chrX', 'chrY', 'chrM', 'chrx', 'chry', 'chrm'])
        
        return new Either(alternatives)
    }
    
    /**
     * Matches paired-end read identifiers without the separator.
     * Supports: R1, R2, 1, 2
     * 
     * Note: This pattern matches ONLY the identifier (R1, R2, 1, 2).
     * If you need to match the separator (_ or .), add CharClass("._") before ReadPair().
     * 
     * Examples:
     * - R1
     * - R2
     * - 1
     * - 2
     * 
     * @return A pattern matching read pair identifiers (without separator)
     */
    static PRegEx ReadPair() {
        // Matches only the read pair identifier (R1, R2, 1, 2) without separators
        return new Either(['R1', 'R2', '1', '2'])
    }
    
    /**
     * Matches FASTQ file extensions.
     * Supports: .fastq, .fq, .fastq.gz, .fq.gz (case-insensitive)
     * 
     * Examples:
     * - .fastq
     * - .fq
     * - .fastq.gz
     * - .fq.gz
     * 
     * @return A pattern matching FASTQ extensions
     */
    static PRegEx FastqExtension() {
        // Case-insensitive matching for .fastq, .fq with optional .gz
        return new PRegEx() {
            @Override
            String toRegex() {
                return "(?i)\\.(fastq|fq)(\\.(gz))?"
            }
        }
    }
    
    /**
     * Matches VCF file extensions.
     * Supports: .vcf, .vcf.gz, .bcf (case-insensitive)
     * 
     * Examples:
     * - .vcf
     * - .vcf.gz
     * - .bcf
     * 
     * @return A pattern matching VCF extensions
     */
    static PRegEx VcfExtension() {
        // Case-insensitive matching for .vcf, .bcf with optional .gz
        return new PRegEx() {
            @Override
            String toRegex() {
                return "(?i)\\.(vcf|bcf)(\\.(gz))?"
            }
        }
    }
    
    /**
     * Matches alignment file extensions.
     * Supports: .bam, .sam, .cram (case-insensitive)
     * 
     * Examples:
     * - .bam
     * - .sam
     * - .cram
     * 
     * @return A pattern matching alignment file extensions
     */
    static PRegEx AlignmentExtension() {
        // Case-insensitive matching for .bam, .sam, .cram
        return new PRegEx() {
            @Override
            String toRegex() {
                return "(?i)\\.(bam|sam|cram)"
            }
        }
    }
    
    /**
     * Matches BED file extensions.
     * Supports: .bed, .bed.gz (case-insensitive)
     * 
     * Examples:
     * - .bed
     * - .bed.gz
     * 
     * @return A pattern matching BED file extensions
     */
    static PRegEx BedExtension() {
        def dot = new Literal(".")
        def b = new CharClass("Bb", false)
        def e = new CharClass("Ee", false)
        def d = new CharClass("Dd", false)
        def g = new CharClass("Gg", false)
        def z = new CharClass("Zz", false)
        
        def bed = b.then(e).then(d)
        def gz = dot.then(g).then(z).optional()
        
        return dot.then(bed).then(gz)
    }
    
    /**
     * Matches GFF/GTF file extensions.
     * Supports: .gff, .gff3, .gtf, .gff.gz, .gff3.gz, .gtf.gz (case-insensitive)
     * 
     * Examples:
     * - .gff
     * - .gff3
     * - .gtf
     * - .gff.gz
     * 
     * @return A pattern matching GFF/GTF file extensions
     */
    static PRegEx GffGtfExtension() {
        // Case-insensitive matching for .gff, .gff3, .gtf with optional .gz
        return new PRegEx() {
            @Override
            String toRegex() {
                return "(?i)\\.(gff3?|gtf)(\\.(gz))?"
            }
        }
    }
    
    /**
     * Matches FASTA file extensions.
     * Supports: .fa, .fasta, .fna, .fa.gz, .fasta.gz, .fna.gz (case-insensitive)
     * 
     * Examples:
     * - .fa
     * - .fasta
     * - .fna
     * - .fa.gz
     * 
     * @return A pattern matching FASTA file extensions
     */
    static PRegEx FastaExtension() {
        // Case-insensitive matching for .fa, .fasta, .fna with optional .gz
        return new PRegEx() {
            @Override
            String toRegex() {
                return "(?i)\\.(fa|fasta|fna)(\\.(gz))?"
            }
        }
    }
}

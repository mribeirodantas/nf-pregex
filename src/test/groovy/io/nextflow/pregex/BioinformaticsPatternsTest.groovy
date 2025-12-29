package io.nextflow.pregex

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for BioinformaticsPatterns class.
 * 
 * @author Seqera AI
 */
class BioinformaticsPatternsTest extends Specification {

    // DNA Sequence Tests
    
    @Unroll
    def "DNASequence should match valid DNA: #input"() {
        given:
        def pattern = BioinformaticsPatterns.DNASequence()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            "ACGT",
            "acgt",
            "ATCGATCG",
            "atcgatcg",
            "ACGTacgt",
            "AAAAA",
            "CCCCC",
            "GGGGG",
            "TTTTT"
        ]
    }
    
    @Unroll
    def "DNASequence should NOT match invalid DNA: #input"() {
        given:
        def pattern = BioinformaticsPatterns.DNASequence()
        
        expect:
        !input.matches(pattern.toRegex())
        
        where:
        input << [
            "ACGTU",     // U is RNA
            "ACGTN",     // N is ambiguity code
            "ACG-T",     // contains dash
            "",          // empty
            "123",       // numbers
            "ACGT XYZ"   // spaces
        ]
    }
    
    @Unroll
    def "StrictDNASequence should only match uppercase DNA: #input"() {
        given:
        def pattern = BioinformaticsPatterns.StrictDNASequence()
        
        expect:
        input.matches(pattern.toRegex()) == shouldMatch
        
        where:
        input      | shouldMatch
        "ACGT"     | true
        "ATCGATCG" | true
        "acgt"     | false
        "ACGTacgt" | false
    }
    
    @Unroll
    def "DNASequenceWithAmbiguity should match DNA with IUPAC codes: #input"() {
        given:
        def pattern = BioinformaticsPatterns.DNASequenceWithAmbiguity()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            "ACGT",
            "ACGTN",
            "ACGTRYMKSW",
            "ACGTBDHV",
            "NNNNN",
            "RRRR",
            "YYYY",
            "acgtryswkmbdhvn"
        ]
    }
    
    // Protein Sequence Tests
    
    @Unroll
    def "ProteinSequence should match valid protein: #input"() {
        given:
        def pattern = BioinformaticsPatterns.ProteinSequence()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            "ACDEFGHIKLMNPQRSTVWY",
            "acdefghiklmnpqrstvwy",
            "MVHLTPEEK",
            "mvhltpeek",
            "ARNDCEQGHILKMFPSTWYV",
            "AAA",
            "KKK"
        ]
    }
    
    @Unroll
    def "ProteinSequence should NOT match invalid protein: #input"() {
        given:
        def pattern = BioinformaticsPatterns.ProteinSequence()
        
        expect:
        !input.matches(pattern.toRegex())
        
        where:
        input << [
            "ACDEFX",    // X is ambiguity
            "ACDEF*",    // * is stop
            "ACDEFB",    // B is ambiguity (Asx)
            "ACDEFZ",    // Z is ambiguity (Glx)
            "",          // empty
            "123",       // numbers
            "ACE-F"      // contains dash
        ]
    }
    
    @Unroll
    def "StrictProteinSequence should only match uppercase protein: #input"() {
        given:
        def pattern = BioinformaticsPatterns.StrictProteinSequence()
        
        expect:
        input.matches(pattern.toRegex()) == shouldMatch
        
        where:
        input     | shouldMatch
        "MVHLTPEEK" | true
        "mvhltpeek" | false
        "MvHlTpEeK" | false
    }
    
    @Unroll
    def "ProteinSequenceWithAmbiguity should match protein with ambiguity codes: #input"() {
        given:
        def pattern = BioinformaticsPatterns.ProteinSequenceWithAmbiguity()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            "ACDEFGHIKLMNPQRSTVWY",
            "MVHLTPEEKX",
            "ACDEFGHBZX",
            "XXXXX",
            "BBBBB",
            "ZZZZZ",
            "acdefghiklmnpqrstvwybzx"
        ]
    }
    
    // Phred Quality Tests
    
    @Unroll
    def "PhredQuality should match valid Phred scores: #input"() {
        given:
        def pattern = BioinformaticsPatterns.PhredQuality()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            "!!!FFFFFF",
            "IIIIIIIIII",
            "~~~~~~~~~~",
            "!\"#\$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
        ]
    }
    
    // Chromosome Tests
    
    @Unroll
    def "Chromosome should match various formats: #input"() {
        given:
        def pattern = BioinformaticsPatterns.Chromosome()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            "chr1",
            "chr22",
            "chrX",
            "chrY",
            "chrM",
            "1",
            "22",
            "X",
            "Y",
            "M",
            "chrx",
            "chry",
            "chrm",
            "x",
            "y",
            "m"
        ]
    }
    
    @Unroll
    def "Chromosome should NOT match invalid chromosomes: #input"() {
        given:
        def pattern = BioinformaticsPatterns.Chromosome()
        
        expect:
        !input.matches(pattern.toRegex())
        
        where:
        input << [
            "chr23",     // no chr23
            "chr0",      // no chr0
            "chrZ",      // no chrZ
            "chr",       // incomplete
            "",          // empty
            "chr1a",     // extra character
            "chr_1"      // underscore
        ]
    }
    
    @Unroll
    def "StrictChromosome should require 'chr' prefix: #input"() {
        given:
        def pattern = BioinformaticsPatterns.StrictChromosome()
        
        expect:
        input.matches(pattern.toRegex()) == shouldMatch
        
        where:
        input   | shouldMatch
        "chr1"  | true
        "chr22" | true
        "chrX"  | true
        "1"     | false
        "22"    | false
        "X"     | false
    }
    
    // Read Pair Tests
    
    @Unroll
    def "ReadPair should match read pair identifiers: #input"() {
        given:
        def pattern = BioinformaticsPatterns.ReadPair()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            "R1",
            "R2",
            "1",
            "2"
        ]
    }
    
    @Unroll
    def "ReadPair should NOT match invalid identifiers: #input"() {
        given:
        def pattern = BioinformaticsPatterns.ReadPair()
        
        expect:
        !input.matches(pattern.toRegex())
        
        where:
        input << [
            "_R1",     // includes separator
            "_R2",     // includes separator
            ".R1",     // includes separator
            "R3",      // invalid number
            "3",       // invalid number
            "R0",      // invalid number
            "-R1",     // wrong separator
            "_r1",     // lowercase r
            ""         // empty
        ]
    }
    
    // FASTQ Extension Tests
    
    @Unroll
    def "FastqExtension should match FASTQ extensions: #input"() {
        given:
        def pattern = BioinformaticsPatterns.FastqExtension()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            ".fastq",
            ".fq",
            ".fastq.gz",
            ".fq.gz",
            ".FASTQ",
            ".FQ",
            ".Fastq.Gz",
            ".Fq.GZ"
        ]
    }
    
    @Unroll
    def "FastqExtension should NOT match invalid extensions: #input"() {
        given:
        def pattern = BioinformaticsPatterns.FastqExtension()
        
        expect:
        !input.matches(pattern.toRegex())
        
        where:
        input << [
            "fastq",      // no dot
            ".fasta",     // wrong extension
            ".fastq.bz2", // wrong compression
            ".fastq.tar.gz",
            ""            // empty
        ]
    }
    
    // VCF Extension Tests
    
    @Unroll
    def "VcfExtension should match VCF extensions: #input"() {
        given:
        def pattern = BioinformaticsPatterns.VcfExtension()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            ".vcf",
            ".vcf.gz",
            ".bcf",
            ".VCF",
            ".VCF.GZ",
            ".BCF",
            ".Vcf.Gz"
        ]
    }
    
    // Alignment Extension Tests
    
    @Unroll
    def "AlignmentExtension should match alignment extensions: #input"() {
        given:
        def pattern = BioinformaticsPatterns.AlignmentExtension()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            ".bam",
            ".sam",
            ".cram",
            ".BAM",
            ".SAM",
            ".CRAM",
            ".Bam"
        ]
    }
    
    // BED Extension Tests
    
    @Unroll
    def "BedExtension should match BED extensions: #input"() {
        given:
        def pattern = BioinformaticsPatterns.BedExtension()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            ".bed",
            ".bed.gz",
            ".BED",
            ".BED.GZ",
            ".Bed.Gz"
        ]
    }
    
    // GFF/GTF Extension Tests
    
    @Unroll
    def "GffGtfExtension should match GFF/GTF extensions: #input"() {
        given:
        def pattern = BioinformaticsPatterns.GffGtfExtension()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            ".gff",
            ".gff3",
            ".gtf",
            ".gff.gz",
            ".gff3.gz",
            ".gtf.gz",
            ".GFF",
            ".GFF3",
            ".GTF",
            ".Gff.Gz"
        ]
    }
    
    // FASTA Extension Tests
    
    @Unroll
    def "FastaExtension should match FASTA extensions: #input"() {
        given:
        def pattern = BioinformaticsPatterns.FastaExtension()
        
        expect:
        input.matches(pattern.toRegex())
        
        where:
        input << [
            ".fa",
            ".fasta",
            ".fna",
            ".fa.gz",
            ".fasta.gz",
            ".fna.gz",
            ".FA",
            ".FASTA",
            ".FNA",
            ".Fa.Gz"
        ]
    }
    
    // Integration Tests - Real-world filename matching
    
    @Unroll
    def "Should extract components from real FASTQ filename: #filename"() {
        given:
        def samplePattern = new PRegEx.AnyChar().oneOrMore()
        def pairPattern = BioinformaticsPatterns.ReadPair()
        def extPattern = BioinformaticsPatterns.FastqExtension()
        def fullPattern = samplePattern.then(pairPattern).then(extPattern)
        
        expect:
        filename.matches(fullPattern.toRegex())
        
        where:
        filename << [
            "sample1_R1.fastq.gz",
            "sample1_R2.fastq.gz",
            "mydata_1.fq",
            "experiment.2.fastq",
            "test_R1.FQ.GZ"
        ]
    }
    
    @Unroll
    def "Should match real chromosome coordinates: #coord"() {
        given:
        def chrPattern = BioinformaticsPatterns.Chromosome()
        def colonPattern = new PRegEx.Literal(":")
        def numPattern = new PRegEx.Digit().oneOrMore()
        def dashPattern = new PRegEx.Literal("-")
        def fullPattern = chrPattern.then(colonPattern).then(numPattern).then(dashPattern).then(numPattern)
        
        expect:
        coord.matches(fullPattern.toRegex())
        
        where:
        coord << [
            "chr1:1000-2000",
            "chr22:5000000-5001000",
            "chrX:100-200",
            "chrM:1-16569",
            "1:1000-2000"
        ]
    }
    
    @Unroll
    def "Should validate DNA in FASTA format: #sequence"() {
        given:
        def headerPattern = new PRegEx.Literal(">").then(new PRegEx.AnyChar().oneOrMore())
        def newlinePattern = new PRegEx.Literal("\n")
        def dnaPattern = BioinformaticsPatterns.DNASequence()
        def fullPattern = headerPattern.then(newlinePattern).then(dnaPattern)
        
        expect:
        sequence.matches(fullPattern.toRegex())
        
        where:
        sequence << [
            ">seq1\nACGT",
            ">gene_X\nATCGATCGATCG",
            ">chr1:1000-2000\nAAAAAAAA"
        ]
    }
}

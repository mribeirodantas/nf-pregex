package io.nextflow.pregex

import groovy.transform.CompileStatic
import nextflow.Session
import nextflow.plugin.extension.Function
import nextflow.plugin.extension.PluginExtensionPoint

/**
 * Extension point that provides PRegEx pattern builder functions to Nextflow scripts.
 * 
 * These functions can be imported and used in Nextflow pipelines:
 * 
 * include { Either; Literal; Optional } from 'plugin/nf-pregex'
 * 
 * @author Marcel Ribeiro-Dantas <marcel@seqera.io>
 */
class PRegExExtension extends PluginExtensionPoint {

    @Override
    void init(Session session) {
        // Initialization logic if needed
    }

    /**
     * Creates a pattern that matches any of the provided alternatives.
     * 
     * Example: Either(["foo", "bar", "baz"]) produces "(foo|bar|baz)"
     * 
     * @param alternatives List of string alternatives
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Either(List alternatives) {
        if (!alternatives || alternatives.isEmpty()) {
            throw new IllegalArgumentException("Either requires at least one alternative")
        }
        def stringList = alternatives.collect { it.toString() }
        return new PRegEx.Either(stringList)
    }

    /**
     * Creates a pattern that matches the literal text (all special regex chars escaped).
     * 
     * Example: Literal("a.b") produces "a\\.b" (matches literal "a.b", not "a" + any char + "b")
     * 
     * @param text The literal text to match
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Literal(String text) {
        return new PRegEx.Literal(text)
    }

    /**
     * Creates a pattern that matches zero or one occurrence of the given pattern.
     * 
     * Example: Optional(Literal("s")) produces "(s)?"
     * 
     * @param pattern The pattern to make optional
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Optional(PRegEx pattern) {
        return new PRegEx.Optional(pattern)
    }

    /**
     * Creates a pattern that matches one or more occurrences of the given pattern.
     * 
     * Example: OneOrMore(Literal("a")) produces "(a)+"
     * 
     * @param pattern The pattern to repeat
     * @return PRegEx pattern object
     */
    @Function
    PRegEx OneOrMore(PRegEx pattern) {
        return new PRegEx.OneOrMore(pattern)
    }

    /**
     * Creates a pattern that matches zero or more occurrences of the given pattern.
     * 
     * Example: ZeroOrMore(Literal("a")) produces "(a)*"
     * 
     * @param pattern The pattern to repeat
     * @return PRegEx pattern object
     */
    @Function
    PRegEx ZeroOrMore(PRegEx pattern) {
        return new PRegEx.ZeroOrMore(pattern)
    }

    /**
     * Creates a pattern that matches exactly n occurrences of the given pattern.
     * 
     * Example: Exactly(Literal("a"), 3) produces "(a){3}"
     * 
     * @param pattern The pattern to repeat
     * @param n The exact number of repetitions
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Exactly(PRegEx pattern, int n) {
        return new PRegEx.Exactly(pattern, n)
    }

    /**
     * Creates a pattern that matches between min and max occurrences of the given pattern.
     * 
     * Example: Range(Literal("a"), 2, 4) produces "(a){2,4}"
     * 
     * @param pattern The pattern to repeat
     * @param min Minimum number of repetitions
     * @param max Maximum number of repetitions
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Range(PRegEx pattern, int min, int max) {
        return new PRegEx.Range(pattern, min, max)
    }

    /**
     * Creates a pattern that matches at least n occurrences of the given pattern.
     * 
     * Example: AtLeast(Literal("a"), 2) produces "(a){2,}"
     * 
     * @param pattern The pattern to repeat
     * @param n Minimum number of repetitions
     * @return PRegEx pattern object
     */
    @Function
    PRegEx AtLeast(PRegEx pattern, int n) {
        return new PRegEx.AtLeast(pattern, n)
    }

    /**
     * Creates a pattern that matches a sequence of patterns in order.
     * 
     * Example: Sequence([Literal("hello"), Literal(" "), Literal("world")])
     * 
     * @param patterns List of patterns to match in sequence
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Sequence(List patterns) {
        return new PRegEx.Sequence(patterns as List<PRegEx>)
    }

    /**
     * Creates a pattern that matches any single character.
     * 
     * Example: AnyChar() produces "."
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx AnyChar() {
        return new PRegEx.AnyChar()
    }

    /**
     * Creates a pattern that matches any digit (0-9).
     * 
     * Example: Digit() produces "\\d"
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Digit() {
        return new PRegEx.Digit()
    }

    /**
     * Creates a pattern that matches any word character (a-z, A-Z, 0-9, _).
     * 
     * Example: WordChar() produces "\\w"
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx WordChar() {
        return new PRegEx.WordChar()
    }

    /**
     * Creates a pattern that matches any whitespace character.
     * 
     * Example: Whitespace() produces "\\s"
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Whitespace() {
        return new PRegEx.Whitespace()
    }

    /**
     * Creates a pattern that matches the start of a line/string.
     * 
     * Example: StartOfLine() produces "^"
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx StartOfLine() {
        return new PRegEx.StartOfLine()
    }

    /**
     * Creates a pattern that matches the end of a line/string.
     * 
     * Example: EndOfLine() produces "$"
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx EndOfLine() {
        return new PRegEx.EndOfLine()
    }

    /**
     * Creates a capturing group for the given pattern.
     * This is essential for extracting matched substrings from the input.
     * 
     * Example: Group(Digit()) produces "(\d)" which captures the digit
     * 
     * @param pattern The pattern to capture
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Group(PRegEx pattern) {
        return new PRegEx.Group(pattern)
    }

    /**
     * Creates a character class pattern that matches any of the specified characters.
     * 
     * Example: CharClass("abc") produces "[abc]"
     * 
     * @param chars The characters to match
     * @return PRegEx pattern object
     */
    @Function
    PRegEx CharClass(String chars) {
        return new PRegEx.CharClass(chars, false)
    }

    /**
     * Creates a negated character class that matches any character NOT in the specified set.
     * 
     * Example: NotCharClass("abc") produces "[^abc]"
     * 
     * @param chars The characters to exclude
     * @return PRegEx pattern object
     */
    @Function
    PRegEx NotCharClass(String chars) {
        return new PRegEx.CharClass(chars, true)
    }

    /**
     * Creates a character range pattern matching characters from start to end.
     * 
     * Example: CharRange('a', 'z') produces "[a-z]"
     * Example: CharRange('0', '9') produces "[0-9]"
     * 
     * @param start The starting character (single character string)
     * @param end The ending character (single character string)
     * @return PRegEx pattern object
     */
    @Function
    PRegEx CharRange(String start, String end) {
        return new PRegEx.CharRange(start, end)
    }

    /**
     * Creates a multi-range pattern combining multiple character ranges.
     * 
     * Example: MultiRange("'a'-'z', 'A'-'Z', '0'-'9'") produces "[a-zA-Z0-9]"
     * Example: MultiRange("'a'-'f', 'A'-'F', '0'-'9'") produces "[a-fA-F0-9]"
     * 
     * @param rangeSpec String specification of ranges in format "'start'-'end', ..."
     * @return PRegEx pattern object
     */
    @Function
    PRegEx MultiRange(String rangeSpec) {
        return new PRegEx.MultiRange(rangeSpec)
    }

    // ========== Bioinformatics Patterns ==========

    /**
     * Matches a DNA sequence (one or more ACGT nucleotides, case-insensitive).
     * 
     * Example matches: ACGT, acgt, ATCGATCG
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx DNASequence() {
        return BioinformaticsPatterns.DNASequence()
    }

    /**
     * Matches a strict DNA sequence (uppercase ACGT only).
     * 
     * Example matches: ACGT, ATCGATCG
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx StrictDNASequence() {
        return BioinformaticsPatterns.StrictDNASequence()
    }

    /**
     * Matches a DNA sequence with IUPAC ambiguity codes.
     * Includes: A, C, G, T, R, Y, S, W, K, M, B, D, H, V, N
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx DNASequenceWithAmbiguity() {
        return BioinformaticsPatterns.DNASequenceWithAmbiguity()
    }

    /**
     * Matches a protein sequence (20 standard amino acids, case-insensitive).
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx ProteinSequence() {
        return BioinformaticsPatterns.ProteinSequence()
    }

    /**
     * Matches a strict protein sequence (uppercase amino acids only).
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx StrictProteinSequence() {
        return BioinformaticsPatterns.StrictProteinSequence()
    }

    /**
     * Matches a protein sequence with ambiguity codes (B, Z, X, *).
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx ProteinSequenceWithAmbiguity() {
        return BioinformaticsPatterns.ProteinSequenceWithAmbiguity()
    }

    /**
     * Matches Phred quality scores (Phred+33 encoding).
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx PhredQuality() {
        return BioinformaticsPatterns.PhredQuality()
    }

    /**
     * Matches chromosome names (chr1-22, chrX, chrY, chrM, with/without 'chr' prefix).
     * 
     * Example matches: chr1, 22, chrX, chrM
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Chromosome() {
        return BioinformaticsPatterns.Chromosome()
    }

    /**
     * Matches chromosome names requiring 'chr' prefix.
     * 
     * Example matches: chr1, chr22, chrX, chrY, chrM
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx StrictChromosome() {
        return BioinformaticsPatterns.StrictChromosome()
    }

    /**
     * Matches read pair identifiers (_R1, _R2, _1, _2, .R1, .R2, .1, .2).
     * 
     * Example matches: _R1, _R2, .1, .2
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx ReadPair() {
        return BioinformaticsPatterns.ReadPair()
    }

    /**
     * Matches FASTQ file extensions (.fastq, .fq, with optional .gz).
     * 
     * Example matches: .fastq, .fq, .fastq.gz, .fq.gz
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx FastqExtension() {
        return BioinformaticsPatterns.FastqExtension()
    }

    /**
     * Matches VCF file extensions (.vcf, .bcf, with optional .gz).
     * 
     * Example matches: .vcf, .bcf, .vcf.gz, .bcf.gz
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx VcfExtension() {
        return BioinformaticsPatterns.VcfExtension()
    }

    /**
     * Matches alignment file extensions (.bam, .sam, .cram).
     * 
     * Example matches: .bam, .sam, .cram
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx AlignmentExtension() {
        return BioinformaticsPatterns.AlignmentExtension()
    }

    /**
     * Matches BED file extensions (.bed, .bed.gz).
     * 
     * Example matches: .bed, .bed.gz
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx BedExtension() {
        return BioinformaticsPatterns.BedExtension()
    }

    /**
     * Matches GFF/GTF file extensions (.gff, .gff3, .gtf, with optional .gz).
     * 
     * Example matches: .gff, .gtf, .gff.gz, .gff3.gz
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx GffGtfExtension() {
        return BioinformaticsPatterns.GffGtfExtension()
    }

    /**
     * Matches FASTA file extensions (.fa, .fasta, .fna, with optional .gz).
     * 
     * Example matches: .fa, .fasta, .fna, .fa.gz
     * 
     * @return PRegEx pattern object
     */
    @Function
    PRegEx FastaExtension() {
        return BioinformaticsPatterns.FastaExtension()
    }
}

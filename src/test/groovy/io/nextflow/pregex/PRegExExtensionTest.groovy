package io.nextflow.pregex

import nextflow.Session
import spock.lang.Specification

/**
 * Unit tests for PRegExExtension functions.
 */
class PRegExExtensionTest extends Specification {

    PRegExExtension extension

    def setup() {
        extension = new PRegExExtension()
        extension.init(Mock(Session))
    }

    def "Either function should create Either pattern"() {
        when:
        def pattern = extension.Either(['foo', 'bar'])

        then:
        pattern.toRegex() == '(foo|bar)'
    }

    def "Literal function should create Literal pattern"() {
        when:
        def pattern = extension.Literal('test.txt')

        then:
        pattern.toRegex() == 'test\\.txt'
    }

    def "Optional function should create Optional pattern"() {
        when:
        def pattern = extension.Optional(extension.Literal('s'))

        then:
        pattern.toRegex() == '(s)?'
    }

    def "OneOrMore function should create OneOrMore pattern"() {
        when:
        def pattern = extension.OneOrMore(extension.Digit())

        then:
        pattern.toRegex() == '(\\d)+'
    }

    def "ZeroOrMore function should create ZeroOrMore pattern"() {
        when:
        def pattern = extension.ZeroOrMore(extension.WordChar())

        then:
        pattern.toRegex() == '(\\w)*'
    }

    def "Exactly function should create Exactly pattern"() {
        when:
        def pattern = extension.Exactly(extension.Digit(), 3)

        then:
        pattern.toRegex() == '(\\d){3}'
    }

    def "Range function should create Range pattern"() {
        when:
        def pattern = extension.Range(extension.Digit(), 2, 4)

        then:
        pattern.toRegex() == '(\\d){2,4}'
    }

    def "AtLeast function should create AtLeast pattern"() {
        when:
        def pattern = extension.AtLeast(extension.Digit(), 1)

        then:
        pattern.toRegex() == '(\\d){1,}'
    }

    def "Sequence function should create Sequence pattern"() {
        when:
        def pattern = extension.Sequence([
            extension.Literal('hello'),
            extension.Literal(' '),
            extension.Literal('world')
        ])

        then:
        pattern.toRegex() == 'hello world'
    }

    def "AnyChar function should create AnyChar pattern"() {
        when:
        def pattern = extension.AnyChar()

        then:
        pattern.toRegex() == '.'
    }

    def "Digit function should create Digit pattern"() {
        when:
        def pattern = extension.Digit()

        then:
        pattern.toRegex() == '\\d'
    }

    def "WordChar function should create WordChar pattern"() {
        when:
        def pattern = extension.WordChar()

        then:
        pattern.toRegex() == '\\w'
    }

    def "Whitespace function should create Whitespace pattern"() {
        when:
        def pattern = extension.Whitespace()

        then:
        pattern.toRegex() == '\\s'
    }

    def "StartOfLine function should create StartOfLine pattern"() {
        when:
        def pattern = extension.StartOfLine()

        then:
        pattern.toRegex() == '^'
    }

    def "EndOfLine function should create EndOfLine pattern"() {
        when:
        def pattern = extension.EndOfLine()

        then:
        pattern.toRegex() == '$'
    }

    def "CharClass function should create CharClass pattern"() {
        when:
        def pattern = extension.CharClass('abc')

        then:
        pattern.toRegex() == '[abc]'
    }

    def "NotCharClass function should create negated CharClass pattern"() {
        when:
        def pattern = extension.NotCharClass('abc')

        then:
        pattern.toRegex() == '[^abc]'
    }

    def "Complex composition should work"() {
        when:
        // Pattern for matching sample IDs like: sample_R1.fastq.gz or sample_R2.fastq.gz
        def pattern = extension.Sequence([
            extension.OneOrMore(extension.WordChar()),
            extension.Literal('_'),
            extension.Either(['R1', 'R2']),
            extension.Literal('.fastq.gz')
        ])

        then:
        pattern.toRegex() == '(\\w)+_(R1|R2)\\.fastq\\.gz'
    }

    def "Chaining with built-in methods should work"() {
        when:
        def pattern = extension.Literal('sample').then(extension.Digit())

        then:
        pattern.toRegex() == 'sample\\d'
    }

    // CharRange and MultiRange tests

    def "CharRange function should create CharRange pattern"() {
        when:
        def pattern = extension.CharRange('a', 'z')

        then:
        pattern.toRegex() == '[a-z]'
    }

    def "CharRange function should work with digits"() {
        when:
        def pattern = extension.CharRange('0', '9')

        then:
        pattern.toRegex() == '[0-9]'
    }

    def "CharRange function should work with quantifiers"() {
        when:
        def pattern = extension.CharRange('A', 'Z').exactly(3)

        then:
        pattern.toRegex() == '([A-Z]){3}'
    }

    def "MultiRange function should create MultiRange pattern"() {
        when:
        def pattern = extension.MultiRange("'a'-'z', 'A'-'Z', '0'-'9'")

        then:
        pattern.toRegex() == '[a-zA-Z0-9]'
    }

    def "MultiRange function should work with quantifiers"() {
        when:
        def pattern = extension.MultiRange("'a'-'z', 'A'-'Z'").oneOrMore()

        then:
        pattern.toRegex() == '([a-zA-Z])+'
    }

    // Bioinformatics Patterns tests

    def "DNASequence function should create DNA pattern"() {
        when:
        def pattern = extension.DNASequence()

        then:
        pattern.toRegex() == '([ACGTacgt])+'
    }

    def "StrictDNASequence function should create strict DNA pattern"() {
        when:
        def pattern = extension.StrictDNASequence()

        then:
        pattern.toRegex() == '([ACGT])+'
    }

    def "DNASequenceWithAmbiguity function should work"() {
        when:
        def pattern = extension.DNASequenceWithAmbiguity()

        then:
        pattern.toRegex().contains('A')
        pattern.toRegex().contains('N')
    }

    def "ProteinSequence function should create protein pattern"() {
        when:
        def pattern = extension.ProteinSequence()

        then:
        pattern.toRegex().contains('A')
        pattern.toRegex().contains('L')
    }

    def "Chromosome function should create chromosome pattern"() {
        when:
        def pattern = extension.Chromosome()

        then:
        pattern.toRegex() != null
    }

    def "StrictChromosome function should create strict chromosome pattern"() {
        when:
        def pattern = extension.StrictChromosome()

        then:
        pattern.toRegex() != null
    }

    def "ReadPair function should create read pair pattern"() {
        when:
        def pattern = extension.ReadPair()

        then:
        pattern.toRegex().contains('R1')
        pattern.toRegex().contains('R2')
    }

    def "FastqExtension function should create fastq extension pattern"() {
        when:
        def pattern = extension.FastqExtension()

        then:
        pattern.toRegex().contains('fastq')
        pattern.toRegex().contains('fq')
    }

    def "VcfExtension function should create vcf extension pattern"() {
        when:
        def pattern = extension.VcfExtension()

        then:
        pattern.toRegex().contains('vcf')
    }

    def "AlignmentExtension function should create alignment extension pattern"() {
        when:
        def pattern = extension.AlignmentExtension()

        then:
        pattern.toRegex().contains('bam')
        pattern.toRegex().contains('sam')
    }

    def "BedExtension function should create bed extension pattern"() {
        when:
        def pattern = extension.BedExtension()

        then:
        pattern.toRegex().contains('[Bb][Ee][Dd]')
    }

    def "GffGtfExtension function should create gff/gtf extension pattern"() {
        when:
        def pattern = extension.GffGtfExtension()

        then:
        pattern.toRegex().contains('gff')
        pattern.toRegex().contains('gtf')
    }

    def "FastaExtension function should create fasta extension pattern"() {
        when:
        def pattern = extension.FastaExtension()

        then:
        pattern.toRegex().contains('fasta')
        pattern.toRegex().contains('fa')
    }

    def "Complex pattern with CharRange and BioinformaticsPatterns should work"() {
        when:
        def pattern = extension.Sequence([
            extension.CharRange('A', 'H'),
            extension.CharRange('0', '9').exactly(2),
            extension.Literal('_'),
            extension.FastqExtension()
        ])

        then:
        pattern.toRegex() != null
    }
}

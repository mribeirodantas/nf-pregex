package io.nextflow.pregex

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for PRegEx pattern builders.
 */
class PRegExTest extends Specification {

    def "Either should create alternation pattern"() {
        when:
        def pattern = new PRegEx.Either(['foo', 'bar', 'baz'])

        then:
        pattern.toRegex() == '(foo|bar|baz)'
    }

    def "Either with single alternative should not add alternation"() {
        when:
        def pattern = new PRegEx.Either(['foo'])

        then:
        pattern.toRegex() == 'foo'
    }

    def "Either should escape special characters"() {
        when:
        def pattern = new PRegEx.Either(['a.b', 'c*d'])

        then:
        pattern.toRegex() == '(a\\.b|c\\*d)'
    }

    def "Literal should escape special regex characters"() {
        when:
        def pattern = new PRegEx.Literal('a.b*c+d?')

        then:
        pattern.toRegex() == 'a\\.b\\*c\\+d\\?'
    }

    def "Literal should handle brackets and parens"() {
        when:
        def pattern = new PRegEx.Literal('test[0-9](foo)')

        then:
        pattern.toRegex() == 'test\\[0\\-9\\]\\(foo\\)'
    }

    def "Optional should wrap pattern with ()?​"() {
        when:
        def pattern = new PRegEx.Optional(new PRegEx.Literal('test'))

        then:
        pattern.toRegex() == '(test)?'
    }

    def "OneOrMore should wrap pattern with ()+"() {
        when:
        def pattern = new PRegEx.OneOrMore(new PRegEx.Literal('a'))

        then:
        pattern.toRegex() == '(a)+'
    }

    def "ZeroOrMore should wrap pattern with ()*"() {
        when:
        def pattern = new PRegEx.ZeroOrMore(new PRegEx.Literal('a'))

        then:
        pattern.toRegex() == '(a)*'
    }

    def "Exactly should create {n} quantifier"() {
        when:
        def pattern = new PRegEx.Exactly(new PRegEx.Literal('a'), 3)

        then:
        pattern.toRegex() == '(a){3}'
    }

    def "Range should create {min,max} quantifier"() {
        when:
        def pattern = new PRegEx.Range(new PRegEx.Literal('a'), 2, 5)

        then:
        pattern.toRegex() == '(a){2,5}'
    }

    def "AtLeast should create {n,} quantifier"() {
        when:
        def pattern = new PRegEx.AtLeast(new PRegEx.Literal('a'), 2)

        then:
        pattern.toRegex() == '(a){2,}'
    }

    def "Sequence should concatenate patterns"() {
        when:
        def pattern = new PRegEx.Sequence([
            new PRegEx.Literal('hello'),
            new PRegEx.Literal(' '),
            new PRegEx.Literal('world')
        ])

        then:
        pattern.toRegex() == 'hello world'
    }

    def "AnyChar should produce dot"() {
        when:
        def pattern = new PRegEx.AnyChar()

        then:
        pattern.toRegex() == '.'
    }

    def "Digit should produce \\d"() {
        when:
        def pattern = new PRegEx.Digit()

        then:
        pattern.toRegex() == '\\d'
    }

    def "WordChar should produce \\w"() {
        when:
        def pattern = new PRegEx.WordChar()

        then:
        pattern.toRegex() == '\\w'
    }

    def "Whitespace should produce \\s"() {
        when:
        def pattern = new PRegEx.Whitespace()

        then:
        pattern.toRegex() == '\\s'
    }

    def "StartOfLine should produce ^"() {
        when:
        def pattern = new PRegEx.StartOfLine()

        then:
        pattern.toRegex() == '^'
    }

    def "EndOfLine should produce dollar sign"() {
        when:
        def pattern = new PRegEx.EndOfLine()

        then:
        pattern.toRegex() == '$'
    }

    def "CharClass should create character class"() {
        when:
        def pattern = new PRegEx.CharClass('abc', false)

        then:
        pattern.toRegex() == '[abc]'
    }

    def "CharClass with negation should create negated class"() {
        when:
        def pattern = new PRegEx.CharClass('abc', true)

        then:
        pattern.toRegex() == '[^abc]'
    }

    def "CharClass should escape special chars"() {
        when:
        def pattern = new PRegEx.CharClass('^a-z]', false)

        then:
        pattern.toRegex() == '[\\^a\\-z\\]]'
    }

    def "Group should wrap in parentheses"() {
        when:
        def pattern = new PRegEx.Group(new PRegEx.Literal('test'))

        then:
        pattern.toRegex() == '(test)'
    }

    def "Complex pattern should work correctly"() {
        when:
        // Match email-like pattern: word chars, @, word chars, dot, 2-3 letters
        def pattern = new PRegEx.Sequence([
            new PRegEx.OneOrMore(new PRegEx.WordChar()),
            new PRegEx.Literal('@'),
            new PRegEx.OneOrMore(new PRegEx.WordChar()),
            new PRegEx.Literal('.'),
            new PRegEx.Range(new PRegEx.WordChar(), 2, 3)
        ])

        then:
        pattern.toRegex() == '(\\w)+@(\\w)+\\.(\\w){2,3}'
    }

    def "Chained methods should work"() {
        when:
        def pattern = new PRegEx.Literal('test').optional()

        then:
        pattern.toRegex() == '(test)?'
    }

    def "then() method should concatenate patterns"() {
        when:
        def pattern = new PRegEx.Literal('hello')
            .then(new PRegEx.Literal(' '))
            .then(new PRegEx.Literal('world'))

        then:
        pattern.toRegex() == 'hello world'
    }

    @Unroll
    def "Either should throw exception for empty alternatives"() {
        when:
        new PRegEx.Either([])

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "Exactly should throw exception for negative count"() {
        when:
        new PRegEx.Exactly(new PRegEx.Literal('a'), -1)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "Range should throw exception for invalid range"() {
        when:
        new PRegEx.Range(new PRegEx.Literal('a'), min, max)

        then:
        thrown(IllegalArgumentException)

        where:
        min | max
        5   | 2
        -1  | 5
    }

    def "toString() should return regex"() {
        when:
        def pattern = new PRegEx.Literal('test')

        then:
        pattern.toString() == 'test'
    }
}

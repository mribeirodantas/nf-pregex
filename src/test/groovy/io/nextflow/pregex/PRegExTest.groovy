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

    // CharRange tests
    
    def "CharRange should create simple lowercase range"() {
        when:
        def pattern = new PRegEx.CharRange('a' as char, 'z' as char)

        then:
        pattern.toRegex() == '[a-z]'
    }

    def "CharRange should create simple uppercase range"() {
        when:
        def pattern = new PRegEx.CharRange('A' as char, 'Z' as char)

        then:
        pattern.toRegex() == '[A-Z]'
    }

    def "CharRange should create digit range"() {
        when:
        def pattern = new PRegEx.CharRange('0' as char, '9' as char)

        then:
        pattern.toRegex() == '[0-9]'
    }

    def "CharRange should create single character range"() {
        when:
        def pattern = new PRegEx.CharRange('a' as char, 'a' as char)

        then:
        pattern.toRegex() == '[a-a]'
    }

    def "CharRange should handle partial ranges"() {
        when:
        def pattern = new PRegEx.CharRange('a' as char, 'f' as char)

        then:
        pattern.toRegex() == '[a-f]'
    }

    def "CharRange should throw exception for invalid range"() {
        when:
        new PRegEx.CharRange('z' as char, 'a' as char)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("must be less than or equal to")
    }

    @Unroll
    def "CharRange should match string #testString with range #start-#end"() {
        given:
        def pattern = new PRegEx.CharRange(start as char, end as char)
        def regex = pattern.toRegex()

        expect:
        testString.matches("${regex}+") == shouldMatch

        where:
        start | end | testString | shouldMatch
        'a'   | 'z' | 'hello'    | true
        'a'   | 'z' | 'HELLO'    | false
        'A'   | 'Z' | 'HELLO'    | true
        'A'   | 'Z' | 'hello'    | false
        '0'   | '9' | '12345'    | true
        '0'   | '9' | 'abc'      | false
        'a'   | 'f' | 'abc'      | true
        'a'   | 'f' | 'xyz'      | false
    }

    def "CharRange should work with quantifiers"() {
        when:
        def pattern = new PRegEx.CharRange('a' as char, 'z' as char).oneOrMore()

        then:
        pattern.toRegex() == '([a-z])+'
    }

    // CharRange String constructor tests

    def "CharRange String constructor should create simple lowercase range"() {
        when:
        def pattern = new PRegEx.CharRange('a', 'z')

        then:
        pattern.toRegex() == '[a-z]'
    }

    def "CharRange String constructor should create simple uppercase range"() {
        when:
        def pattern = new PRegEx.CharRange('A', 'Z')

        then:
        pattern.toRegex() == '[A-Z]'
    }

    def "CharRange String constructor should create digit range"() {
        when:
        def pattern = new PRegEx.CharRange('0', '9')

        then:
        pattern.toRegex() == '[0-9]'
    }

    def "CharRange String constructor should throw exception for invalid range"() {
        when:
        new PRegEx.CharRange('z', 'a')

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("must be less than or equal to")
    }

    def "CharRange String constructor should throw exception for multi-char start"() {
        when:
        new PRegEx.CharRange('ab', 'z')

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("single character")
    }

    def "CharRange String constructor should throw exception for multi-char end"() {
        when:
        new PRegEx.CharRange('a', 'xyz')

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("single character")
    }

    def "CharRange String constructor should throw exception for null start"() {
        when:
        new PRegEx.CharRange(null, 'z')

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("single character")
    }

    def "CharRange String constructor should throw exception for empty start"() {
        when:
        new PRegEx.CharRange('', 'z')

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("single character")
    }

    def "CharRange String constructor should work with quantifiers"() {
        when:
        def pattern = new PRegEx.CharRange('a', 'z').oneOrMore()

        then:
        pattern.toRegex() == '([a-z])+'
    }

    // MultiRange tests

    def "MultiRange should create pattern from single range"() {
        when:
        def range = new PRegEx.CharRange('a' as char, 'z' as char)
        def pattern = new PRegEx.MultiRange([range])

        then:
        pattern.toRegex() == '[a-z]'
    }

    def "MultiRange should combine two ranges"() {
        when:
        def range1 = new PRegEx.CharRange('a' as char, 'z' as char)
        def range2 = new PRegEx.CharRange('A' as char, 'Z' as char)
        def pattern = new PRegEx.MultiRange([range1, range2])

        then:
        pattern.toRegex() == '[a-zA-Z]'
    }

    def "MultiRange should combine three ranges (alphanumeric)"() {
        when:
        def range1 = new PRegEx.CharRange('a' as char, 'z' as char)
        def range2 = new PRegEx.CharRange('A' as char, 'Z' as char)
        def range3 = new PRegEx.CharRange('0' as char, '9' as char)
        def pattern = new PRegEx.MultiRange([range1, range2, range3])

        then:
        pattern.toRegex() == '[a-zA-Z0-9]'
    }

    def "MultiRange should throw exception for empty list"() {
        when:
        new PRegEx.MultiRange([])

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("At least one CharRange is required")
    }

    def "MultiRange should throw exception for null list"() {
        when:
        new PRegEx.MultiRange(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("At least one CharRange is required")
    }

    @Unroll
    def "MultiRange should match alphanumeric string: #testString"() {
        given:
        def range1 = new PRegEx.CharRange('a' as char, 'z' as char)
        def range2 = new PRegEx.CharRange('A' as char, 'Z' as char)
        def range3 = new PRegEx.CharRange('0' as char, '9' as char)
        def pattern = new PRegEx.MultiRange([range1, range2, range3])
        def regex = pattern.toRegex()

        expect:
        testString.matches("${regex}+") == shouldMatch

        where:
        testString  | shouldMatch
        'abc'       | true
        'ABC'       | true
        '123'       | true
        'abc123'    | true
        'Test123'   | true
        'hello_'    | false
        'test-123'  | false
        'a b c'     | false
    }

    def "MultiRange should work with quantifiers"() {
        when:
        def range1 = new PRegEx.CharRange('a' as char, 'z' as char)
        def range2 = new PRegEx.CharRange('0' as char, '9' as char)
        def pattern = new PRegEx.MultiRange([range1, range2]).oneOrMore()

        then:
        pattern.toRegex() == '([a-z0-9])+'
    }

    def "MultiRange should work with exactly quantifier"() {
        when:
        def range1 = new PRegEx.CharRange('a' as char, 'z' as char)
        def range2 = new PRegEx.CharRange('A' as char, 'Z' as char)
        def pattern = new PRegEx.MultiRange([range1, range2]).exactly(5)

        then:
        pattern.toRegex() == '([a-zA-Z]){5}'
    }

    def "CharRange and MultiRange should work in Sequence"() {
        when:
        def alphaRange = new PRegEx.MultiRange([
            new PRegEx.CharRange('a' as char, 'z' as char),
            new PRegEx.CharRange('A' as char, 'Z' as char)
        ])
        def digitRange = new PRegEx.CharRange('0' as char, '9' as char)
        def pattern = new PRegEx.Sequence([
            alphaRange.oneOrMore(),
            digitRange.exactly(3)
        ])

        then:
        pattern.toRegex() == '([a-zA-Z])+([0-9]){3}'
    }
}

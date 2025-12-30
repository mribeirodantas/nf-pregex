package io.nextflow.pregex

import spock.lang.Specification

/**
 * Test suite for pattern validation and testing features
 */
class PatternValidationTest extends Specification {

    def "test() should match partial strings"() {
        given:
        def pattern = new PRegEx.Digit().oneOrMore()

        expect:
        pattern.test("abc123def") == true
        pattern.test("123") == true
        pattern.test("abc") == false
        pattern.test(null) == false
    }

    def "matches() should require full string match"() {
        given:
        def pattern = new PRegEx.Digit().oneOrMore()

        expect:
        pattern.matches("123") == true
        pattern.matches("abc123def") == false
        pattern.matches("abc") == false
        pattern.matches(null) == false
    }

    def "extract() should return matched groups"() {
        given:
        def pattern = new PRegEx.Digit().oneOrMore()

        when:
        def result = pattern.extract("abc123def")

        then:
        result != null
        result['0'] == "123"
        result['match'] == "123"
    }

    def "extract() should handle named groups"() {
        given:
        def pattern = new PRegEx.Literal("user-")
                .then(new PRegEx.Digit().oneOrMore().namedGroup("id"))

        when:
        def result = pattern.extract("user-42")

        then:
        result != null
        result['match'] == "user-42"
        result['id'] == "42"
        result['1'] == "42"
    }

    def "extract() should handle multiple groups"() {
        given:
        def pattern = new PRegEx.Literal("(")
                .then(new PRegEx.Digit().exactly(3).namedGroup("area"))
                .then(new PRegEx.Literal(") "))
                .then(new PRegEx.Digit().exactly(3).namedGroup("prefix"))
                .then(new PRegEx.Literal("-"))
                .then(new PRegEx.Digit().exactly(4).namedGroup("line"))

        when:
        def result = pattern.extract("(555) 123-4567")

        then:
        result != null
        result['match'] == "(555) 123-4567"
        result['area'] == "555"
        result['prefix'] == "123"
        result['line'] == "4567"
    }

    def "extract() should return null for no match"() {
        given:
        def pattern = new PRegEx.Digit().oneOrMore()

        when:
        def result = pattern.extract("no digits here")

        then:
        result == null
    }

    def "testAll() should run multiple test cases"() {
        given:
        def pattern = new PRegEx.Digit().exactly(3)
                .then(new PRegEx.Literal("-"))
                .then(new PRegEx.Digit().exactly(4))

        def testCases = [
            "123-4567": true,
            "555-1234": true,
            "12-345": false,
            "abc-defg": false
        ]

        when:
        def report = pattern.testAll(testCases)

        then:
        report.total == 4
        report.passed == 4  // All assertions should pass (actual == expected)
        report.failed == 0
        report.allPassed
    }

    def "testAll() should pass all valid test cases"() {
        given:
        def pattern = new PRegEx.WordChar().oneOrMore()

        def testCases = [
            "hello": true,
            "world123": true,
            "test_case": true,
            "": false
        ]

        when:
        def report = pattern.testAll(testCases)

        then:
        report.total == 4
        report.passed == 4
        report.failed == 0
        report.allPassed
    }

    def "test() should work with complex patterns"() {
        given:
        def emailPattern = new PRegEx.WordChar().oneOrMore()
                .then(new PRegEx.Literal("@"))
                .then(new PRegEx.WordChar().oneOrMore())
                .then(new PRegEx.Literal("."))
                .then(new PRegEx.WordChar().atLeast(2))

        expect:
        emailPattern.test("user@example.com") == true
        emailPattern.test("test.user@domain.co.uk") == true
        emailPattern.test("invalid@") == false
        emailPattern.test("@invalid.com") == false
    }

    def "matches() should work with anchored patterns"() {
        given:
        def pattern = new PRegEx.StartOfString()
                .then(new PRegEx.Digit().exactly(3))
                .then(new PRegEx.EndOfString())

        expect:
        pattern.matches("123") == true
        pattern.matches("0123") == false
        pattern.matches("123 ") == false
    }

    def "extract() should handle nested groups"() {
        given:
        def pattern = new PRegEx.Literal("file-")
                .then(
                    new PRegEx.Digit().oneOrMore().namedGroup("year")
                            .then(new PRegEx.Literal("-"))
                            .then(new PRegEx.Digit().exactly(2).namedGroup("month"))
                            .namedGroup("date")
                )
                .then(new PRegEx.Literal(".txt"))

        when:
        def result = pattern.extract("file-2025-01.txt")

        then:
        result != null
        result['match'] == "file-2025-01.txt"
        result['year'] == "2025"
        result['month'] == "01"
        result['date'] == "2025-01"
    }

    def "test() should handle character classes"() {
        given:
        def pattern = new PRegEx.CharClass("aeiou", false).oneOrMore()

        expect:
        pattern.test("hello") == true
        pattern.test("aeiou") == true
        pattern.test("xyz") == false
    }

    def "testAll() report should format correctly"() {
        given:
        def pattern = new PRegEx.Digit().exactly(3)

        def testCases = [
            "123": true,
            "456": true,
            "12": false
        ]

        when:
        def report = pattern.testAll(testCases)
        def reportText = report.report()

        then:
        reportText.contains("Test Report")
        reportText.contains("Pattern:")
        reportText.contains("Results:")
        reportText.contains("3 total")
    }

    def "extract() should handle Either patterns"() {
        given:
        def pattern = new PRegEx.Either(["cat", "dog", "bird"])
                .namedGroup("animal")

        when:
        def result1 = pattern.extract("I have a cat")
        def result2 = pattern.extract("My dog is cute")
        def result3 = pattern.extract("I saw a bird")

        then:
        result1['animal'] == "cat"
        result2['animal'] == "dog"
        result3['animal'] == "bird"
    }

    def "test() should handle optional patterns"() {
        given:
        def pattern = new PRegEx.Literal("colo")
                .then(new PRegEx.Literal("u").optional())
                .then(new PRegEx.Literal("r"))

        expect:
        pattern.test("color") == true
        pattern.test("colour") == true
        pattern.test("colr") == false
    }

    def "matches() should validate complete email addresses"() {
        given:
        def emailPattern = new PRegEx.StartOfString()
                .then(new PRegEx.WordChar().oneOrMore())
                .then(new PRegEx.Literal("@"))
                .then(new PRegEx.WordChar().oneOrMore())
                .then(new PRegEx.Literal("."))
                .then(new PRegEx.WordChar().range(2, 6))
                .then(new PRegEx.EndOfString())

        expect:
        emailPattern.matches("user@example.com") == true
        emailPattern.matches("test@domain.co") == true
        emailPattern.matches("invalid@domain") == false
        emailPattern.matches("@example.com") == false
    }
}

package io.nextflow.pregex

import spock.lang.Specification

/**
 * Test suite for pattern debugging and visualization features
 */
class PatternDebuggingTest extends Specification {

    def "explain() should provide pattern description"() {
        given:
        def pattern = new PRegEx.Digit().oneOrMore()

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("Pattern:")
        explanation.contains("Breakdown:")
        explanation.contains("\\d")  // Pattern contains \d (digit pattern)
    }

    def "explain() should describe simple literals"() {
        given:
        def pattern = new PRegEx.Literal("hello")

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("Literal text")
        explanation.contains("hello")
    }

    def "explain() should describe character classes"() {
        given:
        def pattern = new PRegEx.Digit()

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("digit")
        explanation.contains("0-9")
    }

    def "explain() should describe quantifiers"() {
        given:
        def pattern = new PRegEx.WordChar().oneOrMore()

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("One or more")
    }

    def "explain() should describe sequences"() {
        given:
        def pattern = new PRegEx.Literal("user-")
                .then(new PRegEx.Digit().oneOrMore())

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("Sequence")
        explanation.contains("Literal text")
        explanation.contains("One or more")
    }

    def "explain() should describe groups"() {
        given:
        def pattern = new PRegEx.Digit().oneOrMore().namedGroup("id")

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("Named group 'id'")
    }

    def "visualize() should create tree structure"() {
        given:
        def pattern = new PRegEx.Literal("hello")

        when:
        def visualization = pattern.visualize()

        then:
        visualization.contains("Pattern Structure:")
        visualization.contains("Regex:")
        visualization.contains("hello")
    }

    def "visualize() should show hierarchy for sequences"() {
        given:
        def pattern = new PRegEx.Literal("user-")
                .then(new PRegEx.Digit().oneOrMore())
                .then(new PRegEx.Literal("@domain"))

        when:
        def visualization = pattern.visualize()

        then:
        visualization.contains("└──")
        visualization.contains("Sequence")
        visualization.contains("Literal text")
    }

    def "visualize() should show quantifier hierarchy"() {
        given:
        def pattern = new PRegEx.WordChar().oneOrMore()

        when:
        def visualization = pattern.visualize()

        then:
        visualization.contains("One or more")
        visualization.contains("word character")
    }

    def "visualize() should show group hierarchy"() {
        given:
        def pattern = new PRegEx.Digit().exactly(3).namedGroup("code")

        when:
        def visualization = pattern.visualize()

        then:
        visualization.contains("Named group 'code'")
        visualization.contains("Exactly 3 times")
    }

    def "explain() should describe Either patterns"() {
        given:
        def pattern = new PRegEx.Either(["cat", "dog", "bird"])

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("One of:")
        explanation.contains("cat")
        explanation.contains("dog")
        explanation.contains("bird")
    }

    def "visualize() should show Either options"() {
        given:
        def pattern = new PRegEx.Either(["yes", "no"])

        when:
        def visualization = pattern.visualize()

        then:
        visualization.contains("One of:")
        visualization.contains("yes")
        visualization.contains("no")
    }

    def "explain() should describe complex nested patterns"() {
        given:
        def pattern = new PRegEx.Literal("(")
                .then(new PRegEx.Digit().exactly(3).namedGroup("area"))
                .then(new PRegEx.Literal(") "))
                .then(new PRegEx.Digit().exactly(3).namedGroup("prefix"))
                .then(new PRegEx.Literal("-"))
                .then(new PRegEx.Digit().exactly(4).namedGroup("line"))

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("Sequence")
        explanation.contains("Named group 'area'")
        explanation.contains("Named group 'prefix'")
        explanation.contains("Named group 'line'")
        explanation.contains("Exactly 3 times")
        explanation.contains("Exactly 4 times")
    }

    def "visualize() should show complex pattern hierarchy"() {
        given:
        def pattern = new PRegEx.StartOfString()
                .then(new PRegEx.WordChar().oneOrMore().namedGroup("user"))
                .then(new PRegEx.Literal("@"))
                .then(new PRegEx.WordChar().oneOrMore().namedGroup("domain"))

        when:
        def visualization = pattern.visualize()

        then:
        visualization.contains("Sequence of patterns")
        visualization.contains("Named group 'user'")
        visualization.contains("Named group 'domain'")
        visualization.contains("├──")
        visualization.contains("└──")
    }

    def "explain() should describe optional patterns"() {
        given:
        def pattern = new PRegEx.Literal("http")
                .then(new PRegEx.Literal("s").optional())

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("Optional:")
    }

    def "explain() should describe range quantifiers"() {
        given:
        def pattern = new PRegEx.Digit().range(2, 4)

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("Between 2 and 4 times")
    }

    def "explain() should describe atLeast quantifiers"() {
        given:
        def pattern = new PRegEx.WordChar().atLeast(5)

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("At least 5 times")
    }

    def "visualize() should handle deeply nested patterns"() {
        given:
        def pattern = new PRegEx.Literal("start-")
                .then(
                    new PRegEx.Digit().oneOrMore()
                            .then(new PRegEx.Literal("-"))
                            .then(new PRegEx.WordChar().oneOrMore())
                            .namedGroup("middle")
                )
                .then(new PRegEx.Literal("-end"))

        when:
        def visualization = pattern.visualize()

        then:
        visualization.contains("Sequence")
        visualization.contains("Named group 'middle'")
        visualization.contains("│")
    }

    def "explain() should describe whitespace patterns"() {
        given:
        def pattern = new PRegEx.Whitespace().oneOrMore()

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("whitespace")
    }

    def "explain() should describe anyChar patterns"() {
        given:
        def pattern = new PRegEx.AnyChar().oneOrMore()

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("Any character")
    }

    def "visualize() should format email pattern nicely"() {
        given:
        def emailPattern = new PRegEx.WordChar().oneOrMore().namedGroup("user")
                .then(new PRegEx.Literal("@"))
                .then(new PRegEx.WordChar().oneOrMore().namedGroup("domain"))
                .then(new PRegEx.Literal("."))
                .then(new PRegEx.WordChar().atLeast(2).namedGroup("tld"))

        when:
        def visualization = emailPattern.visualize()

        then:
        visualization.contains("Pattern Structure:")
        visualization.contains("Named group 'user'")
        visualization.contains("Named group 'domain'")
        visualization.contains("Named group 'tld'")
        visualization.contains("Regex:")
    }

    def "explain() should provide useful information for debugging"() {
        given:
        def pattern = new PRegEx.StartOfString()
                .then(new PRegEx.Digit().exactly(3))
                .then(new PRegEx.Literal("-"))
                .then(new PRegEx.Digit().exactly(2))
                .then(new PRegEx.Literal("-"))
                .then(new PRegEx.Digit().exactly(4))
                .then(new PRegEx.EndOfString())

        when:
        def explanation = pattern.explain()

        then:
        explanation.contains("Pattern:")
        explanation.contains("Breakdown:")
        explanation.contains("Exactly 3 times")
        explanation.contains("Exactly 2 times")
        explanation.contains("Exactly 4 times")
    }
}

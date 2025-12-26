package io.nextflow.pregex

import groovy.transform.CompileStatic

/**
 * Base class for PRegEx pattern builders.
 * 
 * PRegEx provides a human-readable API for building regular expression patterns.
 * Each pattern can be converted to a standard Java regex string using toString().
 * 
 * @author Seqera AI
 */
@CompileStatic
abstract class PRegEx {
    
    /**
     * Converts this pattern to a standard regex string.
     * @return The regex pattern as a string
     */
    abstract String toRegex()

    /**
     * Returns the regex string representation.
     */
    @Override
    String toString() {
        return toRegex()
    }

    /**
     * Chains this pattern with another using concatenation.
     * @param other The pattern to concatenate
     * @return A new Sequence pattern
     */
    PRegEx then(PRegEx other) {
        return new Sequence([this, other])
    }

    /**
     * Makes this pattern optional (zero or one occurrence).
     * @return A new Optional pattern
     */
    PRegEx optional() {
        return new Optional(this)
    }

    /**
     * Repeats this pattern one or more times.
     * @return A new OneOrMore pattern
     */
    PRegEx oneOrMore() {
        return new OneOrMore(this)
    }

    /**
     * Repeats this pattern zero or more times.
     * @return A new ZeroOrMore pattern
     */
    PRegEx zeroOrMore() {
        return new ZeroOrMore(this)
    }

    /**
     * Repeats this pattern exactly n times.
     * @param n Number of repetitions
     * @return A new Exactly pattern
     */
    PRegEx exactly(int n) {
        return new Exactly(this, n)
    }

    /**
     * Repeats this pattern between min and max times.
     * @param min Minimum repetitions
     * @param max Maximum repetitions
     * @return A new Range pattern
     */
    PRegEx range(int min, int max) {
        return new Range(this, min, max)
    }

    /**
     * Repeats this pattern at least n times.
     * @param n Minimum repetitions
     * @return A new AtLeast pattern
     */
    PRegEx atLeast(int n) {
        return new AtLeast(this, n)
    }

    /**
     * Creates a capturing group from this pattern.
     * @return A new Group pattern
     */
    PRegEx group() {
        return new Group(this)
    }

    /**
     * Pattern that matches any of the provided alternatives (OR).
     */
    @CompileStatic
    static class Either extends PRegEx {
        private final List<String> alternatives

        Either(List<String> alternatives) {
            if (!alternatives || alternatives.isEmpty()) {
                throw new IllegalArgumentException("Either requires at least one alternative")
            }
            this.alternatives = alternatives.asImmutable()
        }

        @Override
        String toRegex() {
            if (alternatives.size() == 1) {
                return escapeRegex(alternatives[0])
            }
            return "(" + alternatives.collect { escapeRegex(it) }.join("|") + ")"
        }
    }

    /**
     * Pattern that matches literal text with all special characters escaped.
     */
    @CompileStatic
    static class Literal extends PRegEx {
        private final String text

        Literal(String text) {
            this.text = text
        }

        @Override
        String toRegex() {
            return escapeRegex(text)
        }
    }

    /**
     * Pattern that matches zero or one occurrence (optional).
     */
    @CompileStatic
    static class Optional extends PRegEx {
        private final PRegEx pattern

        Optional(PRegEx pattern) {
            this.pattern = pattern
        }

        @Override
        String toRegex() {
            return "(" + pattern.toRegex() + ")?"
        }
    }

    /**
     * Pattern that matches one or more occurrences.
     */
    @CompileStatic
    static class OneOrMore extends PRegEx {
        private final PRegEx pattern

        OneOrMore(PRegEx pattern) {
            this.pattern = pattern
        }

        @Override
        String toRegex() {
            return "(" + pattern.toRegex() + ")+"
        }
    }

    /**
     * Pattern that matches zero or more occurrences.
     */
    @CompileStatic
    static class ZeroOrMore extends PRegEx {
        private final PRegEx pattern

        ZeroOrMore(PRegEx pattern) {
            this.pattern = pattern
        }

        @Override
        String toRegex() {
            return "(" + pattern.toRegex() + ")*"
        }
    }

    /**
     * Pattern that matches exactly n occurrences.
     */
    @CompileStatic
    static class Exactly extends PRegEx {
        private final PRegEx pattern
        private final int count

        Exactly(PRegEx pattern, int count) {
            if (count < 0) {
                throw new IllegalArgumentException("Count must be non-negative")
            }
            this.pattern = pattern
            this.count = count
        }

        @Override
        String toRegex() {
            return "(" + pattern.toRegex() + "){" + count + "}"
        }
    }

    /**
     * Pattern that matches between min and max occurrences.
     */
    @CompileStatic
    static class Range extends PRegEx {
        private final PRegEx pattern
        private final int min
        private final int max

        Range(PRegEx pattern, int min, int max) {
            if (min < 0 || max < min) {
                throw new IllegalArgumentException("Invalid range: min=${min}, max=${max}")
            }
            this.pattern = pattern
            this.min = min
            this.max = max
        }

        @Override
        String toRegex() {
            return "(" + pattern.toRegex() + "){" + min + "," + max + "}"
        }
    }

    /**
     * Pattern that matches at least n occurrences.
     */
    @CompileStatic
    static class AtLeast extends PRegEx {
        private final PRegEx pattern
        private final int min

        AtLeast(PRegEx pattern, int min) {
            if (min < 0) {
                throw new IllegalArgumentException("Minimum must be non-negative")
            }
            this.pattern = pattern
            this.min = min
        }

        @Override
        String toRegex() {
            return "(" + pattern.toRegex() + "){" + min + ",}"
        }
    }

    /**
     * Pattern that matches a sequence of patterns in order.
     */
    @CompileStatic
    static class Sequence extends PRegEx {
        private final List<PRegEx> patterns

        Sequence(List<PRegEx> patterns) {
            if (!patterns || patterns.isEmpty()) {
                throw new IllegalArgumentException("Sequence requires at least one pattern")
            }
            this.patterns = patterns.asImmutable()
        }

        @Override
        String toRegex() {
            return patterns.collect { it.toRegex() }.join("")
        }
    }

    /**
     * Pattern that matches any single character.
     */
    @CompileStatic
    static class AnyChar extends PRegEx {
        @Override
        String toRegex() {
            return "."
        }
    }

    /**
     * Pattern that matches any digit (0-9).
     */
    @CompileStatic
    static class Digit extends PRegEx {
        @Override
        String toRegex() {
            return "\\d"
        }
    }

    /**
     * Pattern that matches any word character (a-z, A-Z, 0-9, _).
     */
    @CompileStatic
    static class WordChar extends PRegEx {
        @Override
        String toRegex() {
            return "\\w"
        }
    }

    /**
     * Pattern that matches any whitespace character.
     */
    @CompileStatic
    static class Whitespace extends PRegEx {
        @Override
        String toRegex() {
            return "\\s"
        }
    }

    /**
     * Pattern that matches the start of a line/string.
     */
    @CompileStatic
    static class StartOfLine extends PRegEx {
        @Override
        String toRegex() {
            return "^"
        }
    }

    /**
     * Pattern that matches the end of a line/string.
     */
    @CompileStatic
    static class EndOfLine extends PRegEx {
        @Override
        String toRegex() {
            return '$'
        }
    }

    /**
     * Pattern that matches a character class.
     */
    @CompileStatic
    static class CharClass extends PRegEx {
        private final String chars
        private final boolean negated

        CharClass(String chars, boolean negated) {
            this.chars = chars
            this.negated = negated
        }

        @groovy.transform.CompileDynamic
        @Override
        String toRegex() {
            def escaped = chars.replaceAll(/([\\^\-\]])/) { match -> 
                '\\' + match[1]
            }
            return negated ? "[^${escaped}]" : "[${escaped}]"
        }
    }

    /**
     * Pattern that creates a capturing group.
     */
    @CompileStatic
    static class Group extends PRegEx {
        private final PRegEx pattern

        Group(PRegEx pattern) {
            this.pattern = pattern
        }

        @Override
        String toRegex() {
            return "(" + pattern.toRegex() + ")"
        }
    }

    /**
     * Escapes special regex characters in a string to match it literally.
     */
    @groovy.transform.CompileDynamic
    private static String escapeRegex(String text) {
        return text.replaceAll("([\\\\.*+?^" + '$' + "{}()\\[\\]|\\-])", '\\\\$1')
    }
}

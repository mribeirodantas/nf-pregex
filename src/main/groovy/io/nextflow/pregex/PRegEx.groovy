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
     * Helper method to determine if a pattern string needs grouping when applying quantifiers.
     * Simple patterns like \d, \w, \s, character classes, etc. don't need grouping.
     */
    protected static boolean needsGroupingForQuantifier(String pattern) {
        // Single character classes (\d, \w, \s, etc.) don't need grouping
        if (pattern.matches('\\\\[dwsDWS]')) {
            return false
        }
        // Single dot doesn't need grouping
        if (pattern == '.') {
            return false
        }
        // Character classes like [abc] don't need grouping
        if (pattern.matches('\\[.+\\]')) {
            return false
        }
        // Escaped single characters like \. don't need grouping
        if (pattern.matches('\\\\.')) {
            return false
        }
        // Everything else needs grouping for safety
        return true
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
     * Creates a named capturing group from this pattern.
     * @param name The name for the capturing group
     * @return A new NamedGroup pattern
     */
    PRegEx namedGroup(String name) {
        return new NamedGroup(name, this)
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
            return "(?:" + alternatives.collect { escapeRegex(it) }.join("|") + ")"
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
            return "(?:" + pattern.toRegex() + ")?"
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
            return "(?:" + pattern.toRegex() + ")+"
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
            return "(?:" + pattern.toRegex() + ")*"
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
            // Check if we need to wrap in a group
            // Simple patterns like \d, \w, \s, ., or single characters don't need grouping
            def patternStr = pattern.toRegex()
            def needsGroup = PRegEx.needsGroupingForQuantifier(patternStr)
            
            if (needsGroup) {
                return "(?:" + patternStr + "){" + count + "}"
            } else {
                return patternStr + "{" + count + "}"
            }
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
            def patternStr = pattern.toRegex()
            def needsGroup = PRegEx.needsGroupingForQuantifier(patternStr)
            
            if (needsGroup) {
                return "(?:" + patternStr + "){" + min + "," + max + "}"
            } else {
                return patternStr + "{" + min + "," + max + "}"
            }
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
            def patternStr = pattern.toRegex()
            def needsGroup = PRegEx.needsGroupingForQuantifier(patternStr)
            
            if (needsGroup) {
                return "(?:" + patternStr + "){" + min + ",}"
            } else {
                return patternStr + "{" + min + ",}"
            }
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
     * Pattern that matches a character range.
     * Creates patterns like [a-z], [0-9], [A-Z].
     */
    @CompileStatic
    static class CharRange extends PRegEx {
        private final char start
        private final char end

        CharRange(char start, char end) {
            if (start > end) {
                throw new IllegalArgumentException("Start character '${start}' must be less than or equal to end character '${end}'")
            }
            this.start = start
            this.end = end
        }

        CharRange(String start, String end) {
            if (start == null || start.length() != 1) {
                throw new IllegalArgumentException("Start must be a single character string")
            }
            if (end == null || end.length() != 1) {
                throw new IllegalArgumentException("End must be a single character string")
            }
            this.start = start.charAt(0)
            this.end = end.charAt(0)
            if (this.start > this.end) {
                throw new IllegalArgumentException("Start character '${this.start}' must be less than or equal to end character '${this.end}'")
            }
        }

        @Override
        String toRegex() {
            // Escape special characters if needed
            def startChar = escapeCharForRange(start)
            def endChar = escapeCharForRange(end)
            return "[${startChar}-${endChar}]"
        }

        private static String escapeCharForRange(char c) {
            // Characters that need escaping in character classes: ], \, ^, -
            switch (c) {
                case '\\':
                    return '\\\\'
                case '^':
                    return '\\^'
                case ']':
                    return '\\]'
                case '-':
                    return '\\-'
                default:
                    return c.toString()
            }
        }
    }

    /**
     * Pattern that matches multiple character ranges.
     * Creates patterns like [a-zA-Z], [a-zA-Z0-9], etc.
     */
    @CompileStatic
    static class MultiRange extends PRegEx {
        private final List<CharRange> ranges

        MultiRange(List<CharRange> ranges) {
            if (ranges == null || ranges.isEmpty()) {
                throw new IllegalArgumentException("At least one CharRange is required")
            }
            this.ranges = ranges
        }

        MultiRange(String rangeSpec) {
            if (rangeSpec == null || rangeSpec.isEmpty()) {
                throw new IllegalArgumentException("Range specification cannot be null or empty")
            }
            this.ranges = parseRangeSpec(rangeSpec)
            if (this.ranges.isEmpty()) {
                throw new IllegalArgumentException("At least one valid range is required")
            }
        }

        private static List<CharRange> parseRangeSpec(String spec) {
            List<CharRange> ranges = new ArrayList<>()
            // Match patterns like 'a'-'z', 'A'-'Z', '0'-'9'
            def pattern = ~/['"](.)['"]\s*-\s*['"](.)['"]/
            def matcher = pattern.matcher(spec)
            
            while (matcher.find()) {
                def start = matcher.group(1)
                def end = matcher.group(2)
                ranges.add(new CharRange(start, end))
            }
            
            return ranges
        }

        @Override
        String toRegex() {
            if (ranges.size() == 1) {
                // Single range - just return the range pattern
                return ranges[0].toRegex()
            }
            
            // Multiple ranges - combine into a single character class
            def rangeStrings = ranges.collect { range ->
                // Extract the range part without the brackets
                def regex = range.toRegex()
                // Remove the [ and ] from each range
                regex.substring(1, regex.length() - 1)
            }
            
            return "[${rangeStrings.join('')}]"
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
            // Escape backslash first, then other special chars
            def escaped = chars.replace('\\', '\\\\')
                              .replace('^', '\\^')
                              .replace(']', '\\]')
                              .replace('-', '\\-')
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
     * Pattern that creates a named capturing group.
     */
    @CompileStatic
    static class NamedGroup extends PRegEx {
        private final String name
        private final PRegEx pattern

        NamedGroup(String name, PRegEx pattern) {
            if (!name || !name.matches('[a-zA-Z][a-zA-Z0-9]*')) {
                throw new IllegalArgumentException("Group name must start with a letter and contain only alphanumeric characters (no underscores)")
            }
            this.name = name
            this.pattern = pattern
        }

        @Override
        String toRegex() {
            return "(?<" + name + ">" + pattern.toRegex() + ")"
        }

        String getName() {
            return name
        }
    }

    /**
     * Pattern that creates a backreference to a named group.
     */
    @CompileStatic
    static class NamedBackreference extends PRegEx {
        private final String name

        NamedBackreference(String name) {
            if (!name) {
                throw new IllegalArgumentException("Group name cannot be null or empty")
            }
            this.name = name
        }

        @Override
        String toRegex() {
            return "\\k<" + name + ">"
        }

        String getName() {
            return name
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

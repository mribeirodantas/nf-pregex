package io.nextflow.pregex

import groovy.transform.CompileStatic

/**
 * Base class for PRegEx pattern builders.
 * 
 * PRegEx provides a human-readable API for building regular expression patterns.
 * Each pattern can be converted to a standard Java regex string using toString().
 * 
 * @author Marcel Ribeiro-Dantas <marcel@seqera.io>
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
     * Pattern that matches the start of a string (not line).
     */
    @CompileStatic
    static class StartOfString extends PRegEx {
        @Override
        String toRegex() {
            return "\\A"
        }
    }

    /**
     * Pattern that matches the end of a string (not line).
     */
    @CompileStatic
    static class EndOfString extends PRegEx {
        @Override
        String toRegex() {
            return "\\z"
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

    // ========================================
    // Pattern Validation & Testing
    // ========================================

    /**
     * Tests if this pattern matches the given input string.
     * 
     * @param input The string to test against this pattern
     * @return true if the pattern matches, false otherwise
     */
    boolean test(String input) {
        if (input == null) {
            return false
        }
        def pattern = java.util.regex.Pattern.compile(this.toRegex())
        return pattern.matcher(input).find()
    }

    /**
     * Tests if this pattern fully matches the entire input string.
     * 
     * @param input The string to test against this pattern
     * @return true if the pattern matches the entire string, false otherwise
     */
    boolean matches(String input) {
        if (input == null) {
            return false
        }
        def pattern = java.util.regex.Pattern.compile(this.toRegex())
        return pattern.matcher(input).matches()
    }

    /**
     * Extracts matched groups from the input string.
     * 
     * @param input The string to extract matches from
     * @return A map containing matched groups (by name and index) or null if no match
     */
    @groovy.transform.CompileDynamic
    Map<String, String> extract(String input) {
        if (input == null) {
            return null
        }
        
        def pattern = java.util.regex.Pattern.compile(this.toRegex())
        def matcher = pattern.matcher(input)
        
        if (!matcher.find()) {
            return null
        }
        
        def result = [:]
        
        // Add full match
        result['0'] = matcher.group(0)
        result['match'] = matcher.group(0)
        
        // Add numbered groups
        for (int i = 1; i <= matcher.groupCount(); i++) {
            result[i.toString()] = matcher.group(i)
        }
        
        // Try to extract named groups using pattern inspection
        try {
            def namedGroups = extractNamedGroups(this.toRegex())
            namedGroups.each { name ->
                try {
                    result[name] = matcher.group(name)
                } catch (IllegalArgumentException ignored) {
                    // Group name doesn't exist, skip
                }
            }
        } catch (Exception ignored) {
            // If named group extraction fails, continue with numbered groups only
        }
        
        return result
    }

    /**
     * Extracts all named groups from a regex pattern string.
     */
    @groovy.transform.CompileDynamic
    private static List<String> extractNamedGroups(String regex) {
        def names = []
        def pattern = java.util.regex.Pattern.compile('\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>')
        def matcher = pattern.matcher(regex)
        while (matcher.find()) {
            names.add(matcher.group(1))
        }
        return names
    }

    /**
     * Tests this pattern against multiple test cases.
     * 
     * @param testCases A map of test strings to expected boolean results
     * @return A TestReport with the results
     */
    TestReport testAll(Map<String, Boolean> testCases) {
        def report = new TestReport(pattern: this)
        
        testCases.each { input, expected ->
            def actual = this.test(input)
            def assertionPassed = (actual == expected)
            
            // Store actual result as 'passed' to match test expectations
            // where 'passed' means "input matched the pattern"
            report.addResult(input, expected, actual, assertionPassed)
        }
        
        return report
    }

    // ========================================
    // Pattern Debugging & Visualization
    // ========================================

    /**
     * Provides a human-readable explanation of this pattern.
     * 
     * @return A formatted string explaining the pattern structure
     */
    String explain() {
        def regex = this.toRegex()
        def explanation = new StringBuilder()
        
        explanation.append("Pattern: ").append(regex).append("\n")
        explanation.append("━" * 70).append("\n\n")
        
        // Add pattern type information
        explanation.append("Pattern Type: ").append(this.class.simpleName).append("\n\n")
        
        // Add component breakdown if it's a composite pattern
        explanation.append("Breakdown:\n")
        explanation.append(explainComponents(this, 0))
        
        // Add common matches/non-matches examples
        explanation.append("\n").append("━" * 70).append("\n")
        explanation.append("This pattern will match strings containing: ")
        explanation.append(describePattern(this))
        
        return explanation.toString()
    }

    /**
     * Recursively explains pattern components.
     */
    @groovy.transform.CompileDynamic
    private static String explainComponents(PRegEx pattern, int depth) {
        def indent = "  " * depth
        def result = new StringBuilder()
        
        def description = getPatternDescription(pattern)
        result.append(indent).append("• ").append(description).append("\n")
        
        // Handle composite patterns
        if (pattern instanceof Sequence) {
            def patterns = getFieldValue(pattern, 'patterns')
            patterns?.eachWithIndex { p, i ->
                result.append(explainComponents(p, depth + 1))
            }
        } else if (pattern instanceof Either) {
            result.append(indent).append("  Options:\n")
            def alternatives = getFieldValue(pattern, 'alternatives')
            alternatives?.each { alt ->
                result.append(indent).append("    - ").append(alt).append("\n")
            }
        } else if (pattern instanceof OneOrMore || pattern instanceof ZeroOrMore || 
                   pattern instanceof Optional || pattern instanceof Exactly ||
                   pattern instanceof Range || pattern instanceof AtLeast) {
            // Use reflection to access private pattern field
            try {
                def field = pattern.class.getDeclaredField('pattern')
                field.setAccessible(true)
                def nestedPattern = field.get(pattern)
                if (nestedPattern) {
                    result.append(explainComponents(nestedPattern, depth + 1))
                }
            } catch (Exception ignored) {
                // If reflection fails, continue without nested explanation
            }
        } else if (pattern instanceof Group || pattern instanceof NamedGroup) {
            try {
                def field = pattern.class.getDeclaredField('pattern')
                field.setAccessible(true)
                def nestedPattern = field.get(pattern)
                if (nestedPattern) {
                    result.append(explainComponents(nestedPattern, depth + 1))
                }
            } catch (Exception ignored) {
                // If reflection fails, continue without nested explanation
            }
        }
        
        return result.toString()
    }

    /**
     * Helper to get a private field value using reflection.
     */
    private static Object getFieldValue(Object obj, String fieldName) {
        try {
            def field = obj.class.getDeclaredField(fieldName)
            field.setAccessible(true)
            return field.get(obj)
        } catch (Exception e) {
            return null
        }
    }

    /**
     * Gets a human-readable description for a pattern.
     */
    @groovy.transform.CompileDynamic
    private static String getPatternDescription(PRegEx pattern) {
        if (pattern instanceof Literal) {
            def text = getFieldValue(pattern, 'text')
            return "Literal text: \"${text}\""
        } else if (pattern instanceof Digit) {
            return "Any digit (0-9)"
        } else if (pattern instanceof WordChar) {
            return "Any word character (a-z, A-Z, 0-9, _)"
        } else if (pattern instanceof Whitespace) {
            return "Any whitespace character"
        } else if (pattern instanceof AnyChar) {
            return "Any character"
        } else if (pattern instanceof OneOrMore) {
            return "One or more of:"
        } else if (pattern instanceof ZeroOrMore) {
            return "Zero or more of:"
        } else if (pattern instanceof Optional) {
            return "Optional:"
        } else if (pattern instanceof Exactly) {
            def count = getFieldValue(pattern, 'count')
            return "Exactly ${count} times:"
        } else if (pattern instanceof Range) {
            def min = getFieldValue(pattern, 'min')
            def max = getFieldValue(pattern, 'max')
            return "Between ${min} and ${max} times:"
        } else if (pattern instanceof AtLeast) {
            def min = getFieldValue(pattern, 'min')
            return "At least ${min} times:"
        } else if (pattern instanceof Either) {
            def alternatives = getFieldValue(pattern, 'alternatives')
            return "One of: ${alternatives.join(', ')}"
        } else if (pattern instanceof NamedGroup) {
            def name = getFieldValue(pattern, 'name')
            return "Named group '${name}':"
        } else if (pattern instanceof Group) {
            return "Capturing group:"
        } else if (pattern instanceof Sequence) {
            return "Sequence of patterns"
        } else if (pattern instanceof CharClass) {
            def chars = getFieldValue(pattern, 'chars')
            return "Character class: [${chars}]"
        } else {
            return pattern.class.simpleName
        }
    }

    /**
     * Describes what kind of strings this pattern matches.
     */
    @groovy.transform.CompileDynamic
    private static String describePattern(PRegEx pattern) {
        if (pattern instanceof Literal) {
            def text = getFieldValue(pattern, 'text')
            return "the exact text '${text}'"
        } else if (pattern instanceof Digit) {
            return "numeric digits"
        } else if (pattern instanceof WordChar) {
            return "alphanumeric characters and underscores"
        } else if (pattern instanceof Either) {
            def alternatives = getFieldValue(pattern, 'alternatives')
            return "one of: ${alternatives.join(', ')}"
        } else {
            return "patterns matching: ${pattern.toRegex()}"
        }
    }

    /**
     * Creates an ASCII visualization of the pattern structure.
     * 
     * @return A tree-like ASCII representation of the pattern
     */
    String visualize() {
        def result = new StringBuilder()
        result.append("\nPattern Structure:\n")
        result.append("═" * 70).append("\n")
        result.append(visualizeTree(this, "", true))
        result.append("═" * 70).append("\n")
        result.append("\nRegex: ").append(this.toRegex()).append("\n")
        return result.toString()
    }

    /**
     * Creates a tree visualization recursively.
     */
    @groovy.transform.CompileDynamic
    private static String visualizeTree(PRegEx pattern, String prefix, boolean isLast) {
        def result = new StringBuilder()
        def connector = isLast ? "└── " : "├── "
        def description = getPatternDescription(pattern)
        
        result.append(prefix).append(connector).append(description).append("\n")
        
        def childPrefix = prefix + (isLast ? "    " : "│   ")
        
        if (pattern instanceof Sequence) {
            def patterns = getFieldValue(pattern, 'patterns')
            patterns?.eachWithIndex { p, i ->
                def last = (i == patterns.size() - 1)
                result.append(visualizeTree(p, childPrefix, last))
            }
        } else if (pattern instanceof Either) {
            def alternatives = getFieldValue(pattern, 'alternatives')
            alternatives?.eachWithIndex { alt, i ->
                def last = (i == alternatives.size() - 1)
                def literalPattern = new Literal(alt.toString())
                result.append(visualizeTree(literalPattern, childPrefix, last))
            }
        } else if (pattern instanceof OneOrMore || pattern instanceof ZeroOrMore || 
                   pattern instanceof Optional || pattern instanceof Exactly ||
                   pattern instanceof Range || pattern instanceof AtLeast) {
            try {
                def field = pattern.class.getDeclaredField('pattern')
                field.setAccessible(true)
                def nestedPattern = field.get(pattern)
                if (nestedPattern) {
                    result.append(visualizeTree(nestedPattern, childPrefix, true))
                }
            } catch (Exception ignored) {
                // If reflection fails, continue without nested visualization
            }
        } else if (pattern instanceof Group || pattern instanceof NamedGroup) {
            try {
                def field = pattern.class.getDeclaredField('pattern')
                field.setAccessible(true)
                def nestedPattern = field.get(pattern)
                if (nestedPattern) {
                    result.append(visualizeTree(nestedPattern, childPrefix, true))
                }
            } catch (Exception ignored) {
                // If reflection fails, continue without nested visualization
            }
        }
        
        return result.toString()
    }

    /**
     * Container for test results.
     */
    static class TestReport {
        PRegEx pattern
        List<TestResult> results = []
        
        void addResult(String input, boolean expected, boolean actual, boolean passed) {
            results.add(new TestResult(
                input: input,
                expected: expected,
                actual: actual,
                passed: passed
            ))
        }
        
        int getPassed() {
            return results.count { it.passed } as int
        }
        
        int getFailed() {
            return results.count { !it.passed } as int
        }
        
        int getTotal() {
            return results.size()
        }
        
        boolean isAllPassed() {
            return results.every { it.passed }
        }
        
        String report() {
            def sb = new StringBuilder()
            
            sb.append("\n")
            sb.append("╔═" + "═" * 68 + "═╗\n")
            sb.append("║  Test Report" + " " * 55 + "║\n")
            sb.append("╚═" + "═" * 68 + "═╝\n\n")
            
            sb.append("Pattern: ").append(pattern.toRegex()).append("\n")
            sb.append("─" * 70).append("\n\n")
            
            results.each { result ->
                def icon = result.passed ? "✓" : "✗"
                def status = result.passed ? "PASS" : "FAIL"
                
                sb.append(String.format("  %s [%s] \"%s\"\n", icon, status, result.input))
                sb.append(String.format("      Expected: %s | Actual: %s\n", 
                    result.expected ? "match" : "no match",
                    result.actual ? "match" : "no match"))
            }
            
            sb.append("\n").append("─" * 70).append("\n")
            sb.append(String.format("Results: %d passed, %d failed, %d total\n", 
                passed, failed, total))
            
            if (allPassed) {
                sb.append("Status: ✓ ALL TESTS PASSED\n")
            } else {
                sb.append("Status: ✗ SOME TESTS FAILED\n")
            }
            
            return sb.toString()
        }
    }

    /**
     * Container for individual test result.
     */
    static class TestResult {
        String input
        boolean expected
        boolean actual
        boolean passed
    }
}

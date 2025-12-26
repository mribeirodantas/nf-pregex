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
 * @author Seqera AI
 */
@CompileStatic
class PRegExExtension extends PluginExtensionPoint {

    @Override
    void init(Session session) {
        // Initialization logic if needed
    }

    /**
     * Creates a pattern that matches any of the provided alternatives.
     * 
     * Example: Either("foo", "bar", "baz") produces "(foo|bar|baz)"
     * 
     * @param alternatives Variable number of string alternatives
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Either(String... alternatives) {
        if (!alternatives || alternatives.length == 0) {
            throw new IllegalArgumentException("Either requires at least one alternative")
        }
        return new PRegEx.Either(alternatives as List<String>)
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
     * Example: Sequence(Literal("hello"), Literal(" "), Literal("world"))
     * 
     * @param patterns Variable number of patterns to match in sequence
     * @return PRegEx pattern object
     */
    @Function
    PRegEx Sequence(PRegEx... patterns) {
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
}

# Changelog

All notable changes to the nf-pregex plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2025-12-26

### Added
- Initial release of nf-pregex plugin
- Pattern builder functions:
  - `Either(String...)` - Alternation patterns
  - `Literal(String)` - Literal text with automatic escaping
  - `Optional(PRegEx)` - Zero or one occurrences
  - `OneOrMore(PRegEx)` - One or more occurrences
  - `ZeroOrMore(PRegEx)` - Zero or more occurrences
  - `Exactly(PRegEx, int)` - Exact number of occurrences
  - `Range(PRegEx, int, int)` - Range of occurrences
  - `AtLeast(PRegEx, int)` - Minimum occurrences
  - `Sequence(PRegEx...)` - Pattern concatenation
- Character class functions:
  - `AnyChar()` - Any single character
  - `Digit()` - Digit characters (0-9)
  - `WordChar()` - Word characters (a-z, A-Z, 0-9, _)
  - `Whitespace()` - Whitespace characters
  - `CharClass(String)` - Custom character class
  - `NotCharClass(String)` - Negated character class
- Anchor functions:
  - `StartOfLine()` - Start of line anchor
  - `EndOfLine()` - End of line anchor
- Method chaining support for all pattern types
- Comprehensive unit test suite
- Example workflows demonstrating plugin usage
- Complete documentation and API reference

### Features
- Human-readable regex pattern building
- Automatic escaping of special characters
- Type-safe pattern composition
- Integration with Nextflow channel operations
- Compatible with Nextflow 25.04+

[0.1.0]: https://github.com/seqera-ai/nf-pregex/releases/tag/v0.1.0

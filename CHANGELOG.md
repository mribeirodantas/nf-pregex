# Changelog

All notable changes to the nf-pregex plugin are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- `AnyOf` pattern builder for alternation over arbitrary sub-patterns.
  Unlike `Either`, which alternates over literal strings (escaping each
  element), `AnyOf` joins the compiled regex of its child `PRegEx` patterns
  verbatim — e.g. `AnyOf([Sequence([Literal("chr"), OneOrMore(Digit())]), Literal("chrX")])`
  produces `(?:chr(?:\d)+|chrX)`. It collapses to a single child when given
  one and exposes its children for `explain()`/`visualize()` introspection.
- `children()` method on the `PRegEx` base class (empty for leaf nodes,
  overridden by every composite node) to expose nested patterns, plus the
  missing `getCount`, `getMin`, `getMax`, `getChars` and `getRegex` getters.

### Changed
- Migrated the eight compact `BioinformaticsPatterns` builders
  (`PhredQuality`, `Chromosome`, `StrictChromosome`, `FastqExtension`,
  `VcfExtension`, `AlignmentExtension`, `GffGtfExtension`, `FastaExtension`)
  from anonymous `PRegEx` subclasses to the `Raw` node. The emitted regex is
  byte-for-byte identical, but these patterns now report `Raw regex: ...` in
  `explain()`/`visualize()` and `Raw` is no longer dead code.
- Rewrote `explain()`/`visualize()` tree traversal to recurse over the new
  type-safe `children()` method and read values through getters, replacing
  the previous Java reflection approach (`getDeclaredField`/`setAccessible`
  and a `getFieldValue()` helper that silently swallowed exceptions). The
  old approach was fragile: it broke quietly on field renames, defeated
  `@CompileStatic`, and risked access failures under newer JDK module
  restrictions.
- `Chromosome()` and `StrictChromosome()` now emit a compact numeric range
  instead of a 56-branch alternation, e.g.
  `(?:chr)?(?:2[0-2]|1[0-9]|[1-9]|[XYMxym])`. Number alternatives are ordered
  longest-first so `Matcher.find()` and `extract()` consume the full number
  (e.g. `chr22` → `chr22`) rather than stopping at a leading digit.
- Corrected the `@Function` javadoc examples for `Either`, `Optional`,
  `OneOrMore`, `ZeroOrMore`, `Exactly`, `Range` and `AtLeast` to show the
  non-capturing `(?:...)` groups the builders actually produce, not
  capturing `(...)`.

### Fixed
- `explain()`/`visualize()` now handle the `Raw` node, reporting its regex
  instead of falling back to the bare class name.
- Chromosome extraction previously stopped at a leading digit (returning
  `chr2` for `chr22`); it now consumes the full chromosome number.
- The eight compact `BioinformaticsPatterns` builders rendered a blank
  description in `explain()`/`visualize()` (empty `Pattern Type:` and bare
  bullet) because their anonymous subclasses had no simple class name.
  Routing them through the `Raw` node restores meaningful output.
- CI plugin resolution: the example configs, script headers, and doc
  snippets still declared `nf-pregex@0.1.0` while `make install` stages
  `1.0.1`, so `nextflow run` failed with *"Plugin nf-pregex with version
  @0.1.0 does not exist in the repository."* All references are now pinned
  to `1.0.1`.

### Removed
- `debug_escape.groovy`, a leftover debugging scratch file.
- `src/resources/META-INF/extensions.idx`, a stale hand-tracked index that
  sat outside the `src/main/resources` sourceSet (so it was never on the
  resource path). The real index is generated at build time by the
  nextflow-plugin Gradle plugin from `build.gradle`'s `extensionPoints`.

## [1.0.0] - 2025-12-30

### Added
- Initial stable release of the nf-pregex plugin.

[Unreleased]: https://github.com/mribeirodantas/nf-pregex/compare/1.0.0...HEAD
[1.0.0]: https://github.com/mribeirodantas/nf-pregex/releases/tag/1.0.0

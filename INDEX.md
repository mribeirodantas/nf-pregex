# nf-pregex Project Index

**Welcome to nf-pregex!** This document helps you navigate the project and find what you need.

## 🚀 Quick Start

### New to nf-pregex?
**Start here**: [README.md](README.md) - Complete project overview and API documentation

### Want to learn by examples?
**Start here**: [examples/comprehensive/simple/01_basic_usage.nf](examples/comprehensive/simple/01_basic_usage.nf)
**Then see**: [examples/comprehensive/README.md](examples/comprehensive/README.md) for the learning path

### Need a quick reference?
**Start here**: [examples/comprehensive/QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md)

### Want to understand the project status?
**Start here**: [PROJECT_STATUS.md](PROJECT_STATUS.md)

---

## 📚 Documentation Map

### Core Documentation

| Document | Purpose | When to Read |
|----------|---------|--------------|
| [README.md](README.md) | Complete project overview, API docs, architecture | First visit, API reference |
| [PROJECT_STATUS.md](PROJECT_STATUS.md) | Current status, roadmap, timeline | Understanding project phase |
| [EXAMPLES_SUMMARY.md](EXAMPLES_SUMMARY.md) | Complete examples overview | Before diving into examples |
| [COMPLETION_REPORT.md](COMPLETION_REPORT.md) | Design phase achievements | Understanding project completeness |
| [INDEX.md](INDEX.md) | This file - Navigation guide | Finding what you need |

### Examples Documentation

| Document | Purpose | When to Read |
|----------|---------|--------------|
| [examples/comprehensive/README.md](examples/comprehensive/README.md) | Examples overview, learning path | Before starting examples |
| [examples/comprehensive/QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) | Pattern syntax reference | While writing patterns |
| [examples/comprehensive/USAGE.md](examples/comprehensive/USAGE.md) | How to run examples | Before running examples |

---

## 🎯 Use Case Navigation

### "I want to..."

#### Learn the Basics
1. Read [README.md](README.md) - Section: "Getting Started"
2. Run [examples/comprehensive/simple/01_basic_usage.nf](examples/comprehensive/simple/01_basic_usage.nf)
3. Continue with other simple examples
4. **Time**: ~1 hour

#### Build a Validator
1. See [examples/comprehensive/intermediate/01_email_validator.nf](examples/comprehensive/intermediate/01_email_validator.nf)
2. Review [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) - "Common Use Cases"
3. Copy and adapt patterns
4. **Time**: ~30 minutes

#### Parse Bioinformatics Files
1. See [examples/comprehensive/advanced/01_fastq_parser.nf](examples/comprehensive/advanced/01_fastq_parser.nf)
2. See [examples/comprehensive/advanced/02_sample_sheet_validator.nf](examples/comprehensive/advanced/02_sample_sheet_validator.nf)
3. Review bioinformatics patterns in [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md)
4. **Time**: ~1 hour

#### Integrate with My Pipeline
1. Review [README.md](README.md) - Section: "Integration with Nextflow"
2. See [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) - "Integration with Nextflow"
3. Study example integration in advanced examples
4. **Time**: ~30 minutes

---

## 📂 Project Structure

```
nf-pregex/
│
├── INDEX.md                     # ← You are here
├── README.md                    # Main documentation (START HERE)
├── PROJECT_STATUS.md            # Current status & roadmap
├── EXAMPLES_SUMMARY.md          # Examples overview
├── COMPLETION_REPORT.md         # Design phase report
│
├── build.gradle                 # Build configuration
├── settings.gradle              # Project settings
│
├── examples/
│   └── comprehensive/
│       ├── README.md            # Examples learning guide
│       ├── QUICK_REFERENCE.md   # Pattern syntax reference
│       ├── USAGE.md             # How to run examples
│       ├── run_all_examples.sh  # Test all examples
│       │
│       ├── simple/              # Beginner examples
│       │   ├── 01_basic_usage.nf
│       │   ├── 02_named_groups.nf
│       │   └── 03_quantifiers.nf
│       │
│       ├── intermediate/        # Practical examples
│       │   ├── 01_email_validator.nf
│       │   └── 02_filename_parser.nf
│       │
│       └── advanced/            # Bioinformatics examples
│           ├── 01_fastq_parser.nf
│           └── 02_sample_sheet_validator.nf
│
└── src/                         # To be implemented (Phase 4)
    ├── main/groovy/             # Plugin code
    └── test/groovy/             # Test suite
```

---

## 🎓 Learning Paths

### Path 1: Complete Beginner (3-4 hours total)

**Week 1: Fundamentals**
1. Read [README.md](README.md) "Getting Started" section (20 min)
2. Run `examples/comprehensive/simple/01_basic_usage.nf` (15 min)
3. Run `examples/comprehensive/simple/02_named_groups.nf` (20 min)
4. Run `examples/comprehensive/simple/03_quantifiers.nf` (25 min)
5. Review [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) basic sections (30 min)

**Week 2: Applications**
1. Study `examples/comprehensive/intermediate/01_email_validator.nf` (30 min)
2. Study `examples/comprehensive/intermediate/02_filename_parser.nf` (40 min)
3. Try modifying examples for your use cases (1 hour)

**Week 3: Integration**
1. Study advanced examples (1.5 hours)
2. Build your own patterns (1 hour)
3. Integrate into a pipeline (varies)

---

### Path 2: Experienced Developer (1-2 hours total)

1. Skim [README.md](README.md) API section (10 min)
2. Browse [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) (15 min)
3. Run intermediate examples (30 min)
4. Apply to your use case (varies)

---

### Path 3: Bioinformatician (2-3 hours total)

1. Read [README.md](README.md) "Use Cases" section (15 min)
2. Go straight to `examples/comprehensive/advanced/` (1 hour)
3. Review bioinformatics patterns in [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) (20 min)
4. Adapt for your pipelines (varies)

---

## 🔍 Topic Navigation

### Pattern Syntax
- **Overview**: [README.md](README.md) - "Core Patterns" section
- **Complete Reference**: [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md)
- **Examples**: All files in `examples/comprehensive/`

### Quantifiers
- **Documentation**: [README.md](README.md) - "Quantifiers" section
- **Reference**: [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) - "Quantifiers" section
- **Examples**: `examples/comprehensive/simple/03_quantifiers.nf`

### Named Groups
- **Documentation**: [README.md](README.md) - "Groups and Capturing" section
- **Reference**: [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) - "Groups & Capturing"
- **Examples**: `examples/comprehensive/simple/02_named_groups.nf`

### Bioinformatics Patterns
- **Documentation**: [README.md](README.md) - "Use Cases" section
- **Reference**: [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) - "Bioinformatics Patterns"
- **Examples**: `examples/comprehensive/advanced/`

### Integration with Nextflow
- **Documentation**: [README.md](README.md) - "Integration with Nextflow"
- **Reference**: [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) - "Integration with Nextflow"
- **Examples**: All advanced examples show integration

---

## 📝 Common Tasks

### Running Examples

```bash
# Run a single example
nextflow run examples/comprehensive/simple/01_basic_usage.nf

# Run all examples
cd examples/comprehensive
./run_all_examples.sh
```

### Looking Up Pattern Syntax

1. Quick lookup: [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) - Quick Reference Card at bottom
2. Detailed syntax: [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) - Full document
3. Examples in context: Search in `examples/comprehensive/`

---

## 🎨 By Role

### I'm a Nextflow User
**Your path**:
1. [README.md](README.md) - Getting Started
2. [examples/comprehensive/simple/](examples/comprehensive/simple/)
3. [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md)

### I'm a Bioinformatician
**Your path**:
1. [README.md](README.md) - Use Cases section
2. [examples/comprehensive/advanced/](examples/comprehensive/advanced/)
3. [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md) - Bioinformatics section

### I'm a Pipeline Developer
**Your path**:
1. [README.md](README.md) - Integration section
2. [examples/comprehensive/intermediate/](examples/comprehensive/intermediate/)
3. [examples/comprehensive/advanced/](examples/comprehensive/advanced/)

---

## ❓ FAQ

### Where do I start?
See [Quick Start](#-quick-start) above

### How do I run examples?
See [examples/comprehensive/USAGE.md](examples/comprehensive/USAGE.md)

### What patterns are available?
See [README.md](README.md) "Core Patterns" or [QUICK_REFERENCE.md](examples/comprehensive/QUICK_REFERENCE.md)

### How do I integrate with my pipeline?
See [README.md](README.md) "Integration with Nextflow"

### What's the project status?
See [PROJECT_STATUS.md](PROJECT_STATUS.md)

---

**Last Updated**: December 31, 2024
**Project Phase**: Design & Documentation Complete ✅

**Navigate confidently!** This index is your map to everything nf-pregex.

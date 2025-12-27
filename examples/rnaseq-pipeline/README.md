# Illumina FASTQ Filename Parsing - Real-World nf-pregex Example

**THE PROBLEM**: Illumina sequencers generate FASTQ files with complex filenames that encode critical metadata. Extracting this metadata requires regex patterns that are notoriously difficult to write, read, and maintain.

**THE SOLUTION**: nf-pregex transforms cryptic regex into readable, self-documenting code.

## The Real Pain Point

If you've worked with Illumina sequencing data, you've encountered filenames like this:

```
SAMPLE_001_S1_L001_R1_001.fastq.gz
```

This filename encodes:
- **SAMPLE_001**: Sample name
- **S1**: Sample number (position in sample sheet)
- **L001**: Lane number
- **R1**: Read direction (R1 or R2 for paired-end)
- **001**: Chunk/segment number

### The Traditional Approach (From Nextflow Training)

The [official Nextflow training materials](https://training.nextflow.io/) show this regex for parsing Illumina filenames:

```groovy
def m = (fastq_path.name =~ /^(.+)_S(\d+)_L(\d{3})_(R[12])_(\d{3})\.fastq(?:\.gz)?$/)
```

**Problems with this approach:**

❌ **Cryptic** - What does `\d{3}` mean? Lane or chunk?  
❌ **Error-prone** - Easy to forget escaping (`\.` vs `.`)  
❌ **Hard to modify** - Adding a new field requires regex expertise  
❌ **No documentation** - Requires comments to explain  
❌ **Subtle bugs** - Wrong capture group order breaks everything  
❌ **Maintenance nightmare** - Six months later, you won't remember what this does

### The nf-pregex Approach

```groovy
def illuminaPattern = Sequence(
    OneOrMore(WordChar()).capture("sample"),      // Sample name
    Literal("_S"),
    OneOrMore(Digit()).capture("sample_num"),     // Sample number
    Literal("_L"),
    Digit().exactly(3).capture("lane"),           // Lane (3 digits)
    Literal("_"),
    Either(["R1", "R2"]).capture("read"),         // Read direction
    Literal("_"),
    Digit().exactly(3).capture("chunk"),          // Chunk (3 digits)
    Literal(".fastq"),
    Optional(Literal(".gz"))                      // Optional compression
)
```

**Benefits:**

✅ **Instantly readable** - Anyone can understand this  
✅ **Self-documenting** - Comments explain *why*, not *what*  
✅ **Automatic escaping** - `Literal(".")` handles escaping  
✅ **Named captures** - `m.group("lane")` vs `m[0][3]`  
✅ **Easy to modify** - Add fields without regex expertise  
✅ **Maintainable** - Future you will thank present you

## This Example

This minimal example demonstrates **real-world filename parsing** with both traditional regex and nf-pregex, showing:

1. **Side-by-side comparison** - See both approaches in working code
2. **Actual Illumina format** - The exact pattern from Nextflow training
3. **Metadata extraction** - Parse all components from the filename
4. **Named capture groups** - Access fields by name, not index
5. **JSON output** - Extracted metadata saved for downstream use

## Why This Matters

Real nf-core pipelines struggle with this exact problem:

- [nf-core/ampliseq #182](https://github.com/nf-core/ampliseq/issues/182): "fastq filename convention and sample name parsing" - filename parsing breaks with certain naming patterns
- Nextflow training devotes an entire section to this regex pattern
- Every bioinformatician who's built a pipeline has wrestled with this

**This is the actual pain point nf-pregex solves.**

## Quick Start

### Running the Example

```bash
# View help and see the pattern comparison
nextflow run main.nf --help

# Create test data
mkdir -p data
touch data/SAMPLE_001_S1_L001_R1_001.fastq.gz
touch data/SAMPLE_001_S1_L001_R2_001.fastq.gz
touch data/SAMPLE_002_S2_L001_R1_001.fastq.gz

# Run the parsing example
nextflow run main.nf --reads 'data/*_S*_L*_R*_*.fastq.gz'

# View extracted metadata
cat results/parsed_metadata/*.json
```

### Expected Output

The pipeline will parse each filename and extract:

```json
{
  "sample_name": "SAMPLE_001",
  "sample_number": 1,
  "lane": "001",
  "read": "R1",
  "chunk": "001",
  "filename": "SAMPLE_001_S1_L001_R1_001.fastq.gz",
  "parsing_method": "nf-pregex"
}
```

## Code Comparison

### Traditional Regex (Cryptic)

```groovy
def parseFilenameTraditional(fastq_path) {
    // What does this even match? 🤔
    def m = (fastq_path.name =~ /^(.+)_S(\d+)_L(\d{3})_(R[12])_(\d{3})\.fastq(?:\.gz)?$/)
    
    if (!m) return null
    
    return [
        sample_name: m[0][1],    // Which capture group is which?
        sample_num: m[0][2].toInteger(),
        lane: m[0][3],
        read: m[0][4],
        chunk: m[0][5]
    ]
}
```

**Issues:**
- Need to count parentheses to understand captures
- `m[0][3]` - is that lane or read?
- Forgetting `\.` vs `.` breaks everything
- `(?:\.gz)?` - non-capturing group syntax is cryptic

### nf-pregex (Readable)

```groovy
def parseFilenameWithPregex(fastq_path) {
    // Crystal clear! 😊
    def illuminaPattern = Sequence(
        OneOrMore(WordChar()).capture("sample"),      // Sample name
        Literal("_S"),
        OneOrMore(Digit()).capture("sample_num"),     // Sample number  
        Literal("_L"),
        Digit().exactly(3).capture("lane"),           // Lane (3 digits)
        Literal("_"),
        Either(["R1", "R2"]).capture("read"),         // Read direction
        Literal("_"),
        Digit().exactly(3).capture("chunk"),          // Chunk (3 digits)
        Literal(".fastq"),
        Optional(Literal(".gz"))
    )
    
    def m = (fastq_path.name =~ illuminaPattern)
    
    if (!m) return null
    
    return [
        sample_name: m.group("sample"),      // Named access!
        sample_num: m.group("sample_num").toInteger(),
        lane: m.group("lane"),
        read: m.group("read"),
        chunk: m.group("chunk")
    ]
}
```

**Benefits:**
- Each line has a clear purpose
- Named capture groups (no counting!)
- Automatic escaping (`Literal(".")`)
- Comments explain intent, not mechanics
- Easy to add new fields

## Real-World Use Cases

This parsing pattern is essential for:

### 1. **Lane Merging**
```groovy
// Group files by sample and read, merge across lanes
channel
    .fromPath(params.reads)
    .map { file -> tuple(parseFilename(file), file) }
    .groupTuple(by: [0, 1])  // Group by [sample, read]
    .map { meta, files -> 
        // Merge L001, L002, L003... for same sample
        tuple(meta, files.flatten())
    }
```

### 2. **Quality Control Routing**
```groovy
// Route to different QC processes based on sample number
channel
    .fromPath(params.reads)
    .map { file -> tuple(parseFilename(file), file) }
    .branch {
        highPriority: it[0].sample_num <= 10
        standard: true
    }
```

### 3. **Metadata-Driven Processing**
```groovy
// Use lane info for parallelization decisions
def meta = parseFilename(fastq_path)
if (meta.lane == "001" && meta.chunk == "001") {
    // First chunk of first lane - initialize
    initializeAnalysis(meta, fastq_path)
} else {
    // Subsequent chunks - append
    appendToAnalysis(meta, fastq_path)
}
```

## Why Not Just Use String.split()?

You might think: "Can't I just split on underscores?"

```groovy
// Seems simple...
def parts = filename.split("_")
def sample = parts[0]
def sampleNum = parts[1].replace("S", "")
def lane = parts[2].replace("L", "")
// etc...
```

**Problems:**
- ❌ Breaks if sample name contains underscores (`SAMPLE_001` → problem!)
- ❌ No validation (garbage in, garbage out)
- ❌ Fragile (wrong number of parts = crash)
- ❌ No type conversion (everything is a string)
- ❌ Hard to handle optional extensions

**Regex is the right tool, nf-pregex makes it usable.**

## Comparison Table

| Aspect | Traditional Regex | String.split() | nf-pregex |
|--------|------------------|----------------|-----------|
| **Readable** | ❌ Cryptic | ⚠️ Fragile | ✅ Clear |
| **Validates** | ✅ Yes | ❌ No | ✅ Yes |
| **Named captures** | ⚠️ By index | ❌ No | ✅ By name |
| **Handles edge cases** | ⚠️ If correct | ❌ No | ✅ Yes |
| **Maintainable** | ❌ Expertise needed | ⚠️ Breaks easily | ✅ Easy |
| **Self-documenting** | ❌ Needs comments | ❌ Needs comments | ✅ Inherent |

## The Bottom Line

**If you've ever:**
- Spent 30 minutes debugging a regex
- Forgotten to escape a `.` and matched wrong files
- Struggled to explain a regex pattern to a colleague
- Had a regex break when requirements changed

**Then nf-pregex is for you.**

This example solves a **real problem** that **real pipelines** face. The Illumina filename parsing regex from Nextflow training is genuinely difficult - nf-pregex makes it genuinely easy.

## Learn More

- [nf-pregex Plugin Documentation](../../README.md)
- [Basic Examples](../basic_usage.nf)
- [File Matching Examples](../file_matching.nf)
- [Nextflow Training: Pattern Matching](https://training.nextflow.io/)

## References

- [Illumina FASTQ Naming Convention](https://support.illumina.com/help/BaseSpace_Sequence_Hub_OLH_009008_2/Source/Informatics/BS/NamingConvention_FASTQ-files-swBS.htm)
- [Nextflow Training: String Processing](https://training.nextflow.io/0.dev/side_quests/essential_scripting_patterns/)
- [nf-core/ampliseq Issue #182](https://github.com/nf-core/ampliseq/issues/182) - Real-world filename parsing problems

---

**💡 Real-world impact:** Every bioinformatics pipeline that processes Illumina data deals with this exact problem. nf-pregex turns a frustrating regex into readable code.

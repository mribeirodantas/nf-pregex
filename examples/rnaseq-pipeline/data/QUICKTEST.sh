#!/bin/bash
# Quick test script for the RNA-seq pipeline example
# This script provides simple commands to verify the test data

echo "=================================="
echo "nf-pregex RNA-seq Example - Quick Test"
echo "=================================="
echo ""

echo "📁 Test Data Files:"
echo "-----------------------------------"
ls -lh *.fastq.gz 2>/dev/null || echo "❌ No FASTQ files found"
echo ""

echo "📊 Sample Count:"
echo "-----------------------------------"
if ls SAMPLE_*.fastq.gz >/dev/null 2>&1; then
    ls -1 SAMPLE_*.fastq.gz | cut -d_ -f1-2 | sort | uniq -c
else
    echo "❌ No sample files found"
fi
echo ""

echo "📋 Samplesheet:"
echo "-----------------------------------"
if [ -f "samplesheet.csv" ]; then
    cat samplesheet.csv
else
    echo "❌ samplesheet.csv not found"
fi
echo ""

echo "🧪 Testing Traditional Regex Pattern:"
echo "-----------------------------------"
for file in SAMPLE_*.fastq.gz; do
    if [ -f "$file" ]; then
        # Use basic grep to validate filename pattern
        if echo "$file" | grep -qE "^[A-Z_0-9]+_S[0-9]+_L[0-9]{3}_R[12]_[0-9]{3}\.fastq\.gz$"; then
            echo "✅ $file - Valid Illumina format"
        else
            echo "❌ $file - Invalid format"
        fi
    fi
done
echo ""

echo "📦 File Details:"
echo "-----------------------------------"
echo "Total FASTQ files: $(ls -1 SAMPLE_*.fastq.gz 2>/dev/null | wc -l)"
echo "Total samples: $(ls -1 SAMPLE_*.fastq.gz 2>/dev/null | cut -d_ -f1-2 | sort -u | wc -l)"
echo "Paired-end reads: $(ls -1 SAMPLE_*_R1_*.fastq.gz 2>/dev/null | wc -l) R1, $(ls -1 SAMPLE_*_R2_*.fastq.gz 2>/dev/null | wc -l) R2"
echo ""

echo "🚀 To run the pipeline:"
echo "-----------------------------------"
echo "cd .."
echo "nextflow run main.nf --reads 'data/*_S*_L*_R*_*.fastq.gz'"
echo ""
echo "Or see TESTING.md for more options"
echo "=================================="

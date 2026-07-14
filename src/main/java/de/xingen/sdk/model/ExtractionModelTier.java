package de.xingen.sdk.model;

/**
 * Extraction quality/cost tier for AI-based PDF invoice extraction:
 * - {@code FAST} — lower-cost model, good for clean/text-based PDFs (available to all tiers)
 * - {@code ACCURATE} — highest-accuracy model, recommended for scanned/low-quality PDFs (Pro only)
 */
public enum ExtractionModelTier {
    FAST,
    ACCURATE
}

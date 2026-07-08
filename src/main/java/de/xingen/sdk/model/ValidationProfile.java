package de.xingen.sdk.model;

/**
 * - {@code EN16931} — European standard EN 16931 (free)
 * - {@code PEPPOL} — PEPPOL BIS Billing 3.0 (free)
 * - {@code XRECHNUNG} — German XRechnung standard (Pro)
 * - {@code FRANCE} — French Factur-X standard (Pro) — not yet implemented server-side
 * - {@code ITALY} — Italian FatturaPA standard (Pro) — not yet implemented server-side
 */
public enum ValidationProfile {
    EN16931,
    PEPPOL,
    XRECHNUNG,
    FRANCE,
    ITALY
}

package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class TaxBreakdown {
    private BigDecimal taxableAmount;
    private BigDecimal taxAmount;
    /** S / Z / E / AE / K / G / O */
    private String categoryCode;
    /** Null for exempt categories (E/AE/K/G/O). */
    private BigDecimal rate;
    private String exemptionReason;
    private String exemptionReasonCode;
}

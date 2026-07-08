package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/** Document-level allowance/charge (BG-20/BG-21). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class AllowanceCharge {
    /** true = charge, false = allowance. */
    private boolean charge;
    private BigDecimal amount;
    private BigDecimal baseAmount;
    private BigDecimal percentage;
    private String vatCategoryCode;
    private BigDecimal vatRate;
    private String reason;
    private String reasonCode;
}

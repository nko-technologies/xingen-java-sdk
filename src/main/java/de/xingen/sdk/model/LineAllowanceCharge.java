package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/** Line-level allowance/charge (BT-136..BT-141). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class LineAllowanceCharge {
    private boolean charge;
    private BigDecimal amount;
    private BigDecimal baseAmount;
    private BigDecimal percentage;
    private String reason;
    private String reasonCode;
}

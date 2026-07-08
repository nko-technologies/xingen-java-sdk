package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/** Invoicing period, at document level (BG-14) or line level (BG-26). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class InvoicePeriod {
    private LocalDate startDate;
    private LocalDate endDate;
    /** Document level only (UNTDID 2005 tax point date code). */
    private String descriptionCode;
}

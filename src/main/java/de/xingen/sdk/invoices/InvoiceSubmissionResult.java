package de.xingen.sdk.invoices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** The {@code 202 Accepted} response every submit/validate endpoint returns. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class InvoiceSubmissionResult {
    private String id;
    private InvoiceStatus status;
}

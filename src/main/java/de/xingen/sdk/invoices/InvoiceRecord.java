package de.xingen.sdk.invoices;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.xingen.sdk.model.Invoice;
import de.xingen.sdk.model.ValidationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * The envelope the backend returns for a submitted invoice: submission metadata plus the parsed
 * {@link Invoice} and, once validation has finished, its {@link ValidationResult}. Distinct from
 * {@code Invoice} itself because {@code GET /v1/invoices/{id}} never returns a bare invoice — the
 * backend column backing this field is named {@code canonicalJson}, remapped here to {@code invoice}
 * so the SDK's public field name is meaningful rather than leaking an internal storage detail.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class InvoiceRecord {

    private String id;
    private InvoiceStatus status;

    @JsonProperty("canonicalJson")
    private Invoice invoice;

    /** Null while {@link #getStatus()} is {@link InvoiceStatus#PROCESSING}. */
    private ValidationResult validationResult;

    private Instant createdAt;
    private String validationProfile;
    private String invoiceFormat;
    private String uploadedBy;
    private boolean sandbox;
    private UUID apiKeyId;

    /** Extraction quality tier used ({@code FAST}/{@code ACCURATE}) — only set for AI PDF extractions ({@link #getInvoiceFormat()} {@code == "PDF_AI"}). */
    private String extractionTier;
}

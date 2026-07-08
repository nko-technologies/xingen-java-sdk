package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Read-only invoice model, as returned inside an {@link de.xingen.sdk.invoices.InvoiceRecord}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Invoice {
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate taxPointDate;
    private String currency;
    private String buyerReference;
    private String specificationId;
    private String profileId;
    private String typeCode;
    private String orderReference;
    private String salesOrderReference;
    private String projectReference;
    private String contractReference;
    private String receivingAdviceReference;
    private String despatchAdviceReference;
    private String tenderOrLotReference;
    private String invoicedObjectId;
    private String invoicedObjectSchemeId;
    private String buyerAccountingReference;
    private List<String> notes;
    private String paymentTermsNote;
    private List<PrecedingInvoiceReference> precedingInvoiceReferences;
    private List<SupportingDocument> supportingDocuments;
    private int projectReferenceCount;
    private LocalDate deliveryPeriodStart;
    private LocalDate deliveryPeriodEnd;
    /** Null iff no document-level invoicing period was present in the source document. */
    private InvoicePeriod invoicePeriod;
    /** Null iff no delivery element was present in the source document. */
    private Delivery delivery;

    private Party supplier;
    private Party buyer;
    /** Null unless the payee differs from the seller. */
    private Party payee;
    /** Null unless a tax representative is present. */
    private Party taxRepresentative;

    private List<InvoiceLine> lines;
    private List<TaxBreakdown> taxBreakdowns;
    private List<AllowanceCharge> allowanceCharges;
    private List<PaymentMeans> paymentMeans;

    private String taxCurrencyCode;
    private int taxTotalWithSubtotalsCount;
    private int taxTotalWithoutSubtotalsCount;

    private BigDecimal lineExtensionAmount;
    private BigDecimal allowanceTotalAmount;
    private BigDecimal chargeTotalAmount;
    private BigDecimal taxExclusiveAmount;
    private BigDecimal taxAmount;
    private BigDecimal taxAmountInAccountingCurrency;
    private BigDecimal taxInclusiveAmount;
    private BigDecimal prepaidAmount;
    private BigDecimal payableRoundingAmount;
    private BigDecimal payableAmount;
}

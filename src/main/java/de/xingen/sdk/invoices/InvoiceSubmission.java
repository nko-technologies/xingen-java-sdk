package de.xingen.sdk.invoices;

import de.xingen.sdk.model.ValidationProfile;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request body for {@link InvoicesClient#submit}. Mirrors the backend's flat {@code InvoiceRequest}
 * shape — deliberately not the same class hierarchy as the much richer {@link de.xingen.sdk.model.Invoice}
 * read model, since submit and read are genuinely different contracts.
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class InvoiceSubmission {

    private final String invoiceNumber;
    private final LocalDate issueDate;
    /** Payment due date (BT-9). Either this or paymentTermsNote is required whenever the payable amount is positive. */
    private final LocalDate dueDate;
    /** Value added tax point date (BT-7). */
    private final LocalDate taxPointDate;
    private final String currency;
    /** VAT accounting currency code (BT-6), if different from currency. */
    private final String taxCurrencyCode;
    /** Optional in general, mandatory in practice for XRechnung (Leitweg-ID). */
    private final String buyerReference;
    /** Payment terms (BT-20). Either this or dueDate is required whenever the payable amount is positive. */
    private final String paymentTermsNote;
    private final String orderReference;
    private final String salesOrderReference;
    private final String projectReference;
    private final String contractReference;
    private final String receivingAdviceReference;
    private final String despatchAdviceReference;
    private final String tenderOrLotReference;
    private final String invoicedObjectId;
    private final String invoicedObjectSchemeId;
    private final String buyerAccountingReference;
    @Singular("addNote")
    private final List<String> notes;
    @Singular("addPrecedingInvoiceReference")
    private final List<PrecedingInvoiceReferenceInput> precedingInvoiceReferences;
    @Singular("addSupportingDocument")
    private final List<SupportingDocumentInput> supportingDocuments;
    private final LocalDate deliveryPeriodStart;
    private final LocalDate deliveryPeriodEnd;
    private final InvoicePeriodInput invoicePeriod;
    private final DeliveryInput delivery;
    private final ValidationProfile validationProfile;
    private final PartyInput supplier;
    private final PartyInput buyer;
    /** Payee, if different from the seller (BG-10). */
    private final PartyInput payee;
    /** Seller's tax representative (BG-11). */
    private final PartyInput taxRepresentative;
    @Singular("addLine")
    private final List<LineInput> lines;
    @Singular("addPaymentMeans")
    private final List<PaymentMeansInput> paymentMeans;
    @Singular("addAllowanceCharge")
    private final List<AllowanceChargeInput> allowanceCharges;

    /** Seller, buyer, payee, or tax representative, as submitted with a new invoice. */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class PartyInput {
        private final String name;
        /** Legal registration name, if different from the trading name (BT-27/BT-44). */
        private final String registrationName;
        private final String vatId;
        /** Tax registration identifier, non-VAT scheme (BT-32). */
        private final String taxRegistrationId;
        /** Legal registration identifier (BT-30/BT-47). */
        private final String legalRegistrationId;
        /** Legal registration identifier scheme (BT-30-1/BT-47-1). */
        private final String legalRegistrationSchemeId;
        /** Additional legal information, e.g. legal form (BT-33). */
        private final String additionalLegalInfo;
        private final String leitwegId;
        /** Postal address (BG-5/BG-8) — mandatory for every profile for supplier/buyer; the backend rejects a party with no address. */
        private final AddressInput address;
        private final ContactInput contact;
        private final String endpointId;
        private final String endpointSchemeId;
        @Singular("addIdentifier")
        private final List<PartyIdentifierInput> identifiers;
    }

    /** Postal address (BG-5/BG-8/BG-15) of a {@link PartyInput} or {@link DeliveryInput}. Only {@code countryCode} is mandatory server-side. */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class AddressInput {
        private final String streetName;
        private final String additionalStreetName;
        private final String addressLine3;
        private final String city;
        private final String postalZone;
        private final String countrySubdivision;
        private final String countryCode;
    }

    /** Contact details (BG-6/BG-9) of a {@link PartyInput}. */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class ContactInput {
        private final String name;
        private final String telephone;
        private final String email;
    }

    /** Additional party identifier (BT-29/BT-46/BT-60), e.g. a SEPA creditor identifier. */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class PartyIdentifierInput {
        private final String id;
        private final String schemeId;
    }

    /** Preceding invoice reference (BG-3) — e.g. the original invoice a credit note corrects. */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class PrecedingInvoiceReferenceInput {
        private final String id;
        private final LocalDate issueDate;
    }

    /** Additional supporting document (BG-24). */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class SupportingDocumentInput {
        private final String id;
        private final String schemeId;
        private final String typeCode;
        private final String description;
        private final String externalUri;
        private final String mimeCode;
        private final String filename;
    }

    /** Invoicing period (BG-14 document-level / BG-26 line-level). */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class InvoicePeriodInput {
        private final LocalDate startDate;
        private final LocalDate endDate;
        /** Tax point date code, UNTDID 2005 (BT-8, document level only). */
        private final String descriptionCode;
    }

    /** Delivery information (BG-13). */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class DeliveryInput {
        private final String partyName;
        private final String locationId;
        private final String locationSchemeId;
        private final AddressInput address;
        private final LocalDate actualDeliveryDate;
    }

    /** Item classification (BT-158). */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class ItemClassificationInput {
        private final String code;
        private final String listId;
        private final String listVersionId;
    }

    /** Additional item attribute (BG-32). */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class ItemAttributeInput {
        private final String name;
        private final String value;
    }

    /** A single invoice line, as submitted with a new invoice. */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class LineInput {
        private final String description;
        /** Item name (BT-153), distinct from the free-text description if both are needed. */
        private final String itemName;
        private final String note;
        private final String objectId;
        private final String objectIdSchemeId;
        private final String orderLineReference;
        private final String accountingReference;
        private final String sellerItemId;
        private final String buyerItemId;
        private final String standardItemId;
        private final String standardItemIdSchemeId;
        private final String originCountryCode;
        @Singular("addClassification")
        private final List<ItemClassificationInput> classifications;
        @Singular("addAttribute")
        private final List<ItemAttributeInput> attributes;
        private final BigDecimal quantity;
        private final String unit;
        private final BigDecimal price;
        private final BigDecimal grossPrice;
        private final BigDecimal priceDiscount;
        private final BigDecimal priceBaseQuantity;
        private final String priceBaseQuantityUnit;
        /** VAT category code, UNCL5305 (BT-151). Defaults to Standard rate if omitted. */
        private final String taxCategoryCode;
        private final BigDecimal taxRate;
        /** VAT exemption reason text (BT-120) — set when taxCategoryCode is exempt/reverse-charge/out-of-scope. */
        private final String exemptionReason;
        /** VAT exemption reason code, UNCL5305 (BT-121). */
        private final String exemptionReasonCode;
        private final InvoicePeriodInput period;
        @Singular("addAllowanceCharge")
        private final List<LineAllowanceChargeInput> allowanceCharges;
    }

    /** Payment means (BG-16). */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class PaymentMeansInput {
        private final String typeCode;
        private final String paymentMeansText;
        private final String remittanceInformation;
        private final String creditTransferAccountId;
        private final String accountName;
        private final String serviceProviderId;
        private final String mandateReferenceId;
        private final String cardAccountNumber;
        private final String cardHolderName;
        private final String creditorId;
        private final String debitedAccountId;
    }

    /** Document-level allowance or charge (BG-20/BG-21). */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class AllowanceChargeInput {
        /** true = charge (BG-21), false = allowance (BG-20). */
        private final Boolean charge;
        private final BigDecimal amount;
        private final BigDecimal baseAmount;
        private final BigDecimal percentage;
        private final String vatCategoryCode;
        private final BigDecimal vatRate;
        private final String reason;
        private final String reasonCode;
    }

    /** Line-level allowance or charge (BG-27/BG-28). */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class LineAllowanceChargeInput {
        private final Boolean charge;
        private final BigDecimal amount;
        private final BigDecimal baseAmount;
        private final BigDecimal percentage;
        private final String reason;
        private final String reasonCode;
    }
}

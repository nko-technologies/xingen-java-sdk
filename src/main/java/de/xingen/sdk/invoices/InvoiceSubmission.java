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
    private final String currency;
    /** Optional in general, mandatory in practice for XRechnung (Leitweg-ID). */
    private final String buyerReference;
    private final ValidationProfile validationProfile;
    private final PartyInput supplier;
    private final PartyInput buyer;
    @Singular("addLine")
    private final List<LineInput> lines;

    /** Seller or buyer, as submitted with a new invoice. */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class PartyInput {
        private final String name;
        private final String vatId;
        private final String leitwegId;
        /** Postal address (BG-5/BG-8) — mandatory for every profile; the backend rejects a party with no address. */
        private final AddressInput address;
    }

    /** Postal address (BG-5/BG-8) of a {@link PartyInput}. Only {@code countryCode} is mandatory server-side. */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class AddressInput {
        private final String streetName;
        private final String city;
        private final String postalZone;
        private final String countryCode;
    }

    /** A single invoice line, as submitted with a new invoice. */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class LineInput {
        private final String description;
        private final BigDecimal quantity;
        private final String unit;
        private final BigDecimal price;
        private final BigDecimal taxRate;
    }
}

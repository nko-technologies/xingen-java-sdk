package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class InvoiceLine {
    private String lineId;
    private String note;
    private String objectId;
    private String objectIdSchemeId;
    private String orderLineReference;
    private String accountingReference;
    private String itemName;
    private String description;
    private String sellerItemId;
    private String buyerItemId;
    private String standardItemId;
    private String standardItemIdSchemeId;
    private String originCountryCode;
    private List<ItemClassification> classifications;
    private List<ItemAttribute> attributes;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private BigDecimal grossPrice;
    private BigDecimal priceDiscount;
    private boolean priceHasCharge;
    private BigDecimal priceBaseQuantity;
    private String priceBaseQuantityUnit;
    private String taxCategoryCode;
    private BigDecimal taxRate;
    private BigDecimal lineNetAmount;
    /** Null iff no line-level invoicing period was present in the source document. */
    private InvoicePeriod period;
    private int documentReferenceCount;
    private String documentReferenceTypeCode;
    private List<LineAllowanceCharge> allowanceCharges;
}

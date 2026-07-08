package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Delivery {
    private String partyName;
    private String locationId;
    private String locationSchemeId;
    /** Deliver-to address (BG-15); null iff absent from the source document. */
    private Address address;
    private LocalDate actualDeliveryDate;
}

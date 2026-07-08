package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Address {
    private String streetName;
    private String additionalStreetName;
    private String addressLine3;
    private String city;
    private String postalZone;
    private String countrySubdivision;
    private String countryCode;
}

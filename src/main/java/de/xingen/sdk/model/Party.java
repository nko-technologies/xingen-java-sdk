package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Party {
    private String name;
    private String registrationName;
    private String vatId;
    private String taxRegistrationId;
    private String legalRegistrationId;
    private String legalRegistrationSchemeId;
    private String additionalLegalInfo;
    private String leitwegId;
    private String endpointId;
    private String endpointSchemeId;
    private List<PartyIdentifier> identifiers;
    /** Null iff no postal address element was present in the source document. */
    private Address address;
    /** Null iff no contact element was present in the source document. */
    private Contact contact;
}

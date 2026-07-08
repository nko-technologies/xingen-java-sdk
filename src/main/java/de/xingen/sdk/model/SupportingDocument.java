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
public class SupportingDocument {
    private String id;
    private String schemeId;
    /** UNTDID 1001 document type code, e.g. "50", "130". */
    private String typeCode;
    private String description;
    /** Null = no external-reference element; empty string = present but the URI is missing. */
    private String externalUri;
    private String mimeCode;
    private String filename;
    private boolean embeddedPresent;
}

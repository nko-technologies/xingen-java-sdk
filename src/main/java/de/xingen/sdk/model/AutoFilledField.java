package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** A field the backend fills in automatically when it isn't supplied, e.g. from {@code GET /v1/invoices/auto-filled-fields}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class AutoFilledField {
    /** Canonical Invoice field path, e.g. "typeCode" or "lines[].lineId". */
    private String field;
    /** The value that will be set, or a short description when it isn't a fixed value. */
    private String value;
    /** Why it's set automatically, in user-facing language. */
    private String reason;
}

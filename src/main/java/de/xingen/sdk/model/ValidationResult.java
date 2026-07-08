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
public class ValidationResult {
    private boolean valid;
    private List<ValidationError> errors;
    /** Only populated for XML-based validation paths (UBL/CII/IDoc). Null otherwise. */
    private KositResult kositResult;
}

package de.xingen.sdk.error;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;

/** 400 — malformed request body or bean-validation failure. */
@Getter
public class ValidationRequestException extends ApiException {

  /**
   * -- GETTER --
   * Empty unless the failure was a bean-validation error on a request body (as opposed to a plain
   * ).
   */
  private final Map<String, String> fieldErrors;

    public ValidationRequestException(String message, ErrorResponse errorResponse, String rawBody) {
        super(message, 400, errorResponse, rawBody);
        this.fieldErrors = Optional.ofNullable(errorResponse)
            .map(ErrorResponse::getFieldErrors)
            .orElse(Map.of());
    }

}

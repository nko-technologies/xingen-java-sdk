package de.xingen.sdk.error;

import lombok.Getter;

/** Any HTTP response the SDK understands as an error (4xx/5xx). Subtyped for the common statuses. */
@Getter
public class ApiException extends XingenException {

    private final int statusCode;
  /**
   * -- GETTER --
   * The raw response body, always retained even if it could not be parsed as
   * .
   */
  private final String rawBody;
  /**
   * -- GETTER --
   * Null if the response body didn't match the expected shape (e.g. an unexpected 5xx from a proxy).
   */
  private final ErrorResponse errorResponse;

    public ApiException(String message, int statusCode, ErrorResponse errorResponse, String rawBody) {
        super(message);
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
        this.rawBody = rawBody;
    }

}

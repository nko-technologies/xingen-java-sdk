package de.xingen.sdk.error;

/** 404 — the requested resource does not exist. */
public class NotFoundException extends ApiException {

    public NotFoundException(String message, ErrorResponse errorResponse, String rawBody) {
        super(message, 404, errorResponse, rawBody);
    }
}

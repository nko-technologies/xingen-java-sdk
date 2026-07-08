package de.xingen.sdk.error;

/** 403 — e.g. the requested invoice exists but is not owned by the caller. */
public class PermissionException extends ApiException {

    public PermissionException(String message, ErrorResponse errorResponse, String rawBody) {
        super(message, 403, errorResponse, rawBody);
    }
}

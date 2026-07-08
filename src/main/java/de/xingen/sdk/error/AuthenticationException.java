package de.xingen.sdk.error;

/** 401 — missing or invalid API key. The backend returns no application-level body for this status. */
public class AuthenticationException extends ApiException {

    public AuthenticationException(String message, String rawBody) {
        super(message, 401, null, rawBody);
    }
}

package de.xingen.sdk.error;

/**
 * 429 — the API key's request quota has been exhausted. This bypasses the backend's normal error
 * pipeline, so the body is {@code {"error": "..."}}, not the standard {@link ErrorResponse} shape.
 */
public class QuotaExceededException extends ApiException {

    public QuotaExceededException(String message, String rawBody) {
        super(message, 429, null, rawBody);
    }
}

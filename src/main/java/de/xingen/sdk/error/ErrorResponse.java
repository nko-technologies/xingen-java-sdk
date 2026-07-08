package de.xingen.sdk.error;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Mirrors the backend's standard error body shape. Present on 400/403/404/500 responses.
 * <b>Not</b> present on 429 (see {@link QuotaExceededException}) or 401, which use a
 * different or empty body.
 */
@Getter
public final class ErrorResponse {

    private String message;
    private String error;
    private int code;
    private Instant timestamp;
    private Map<String, String> fieldErrors;

    public ErrorResponse() {}

}

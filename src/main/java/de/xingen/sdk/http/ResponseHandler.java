package de.xingen.sdk.http;

import com.fasterxml.jackson.core.type.TypeReference;
import de.xingen.sdk.error.ApiException;
import de.xingen.sdk.error.AuthenticationException;
import de.xingen.sdk.error.ErrorResponse;
import de.xingen.sdk.error.NotFoundException;
import de.xingen.sdk.error.PermissionException;
import de.xingen.sdk.error.QuotaExceededException;
import de.xingen.sdk.error.ValidationRequestException;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Centralizes HTTP status -&gt; exception mapping so every resource client handles errors the same
 * way. Two shapes never go through the normal {@link ErrorResponse} parse path: 429 (quota,
 * written raw by a security filter ahead of the backend's exception pipeline) and 401 (no
 * application-level body at all). Parsing never throws a secondary exception that could mask the
 * real HTTP error — callers always get a typed {@link ApiException} with the raw body attached.
 */
public final class ResponseHandler {

    private ResponseHandler() {}

    public static <T> T decodeOrThrow(HttpResponse<byte[]> response, Class<T> type, JsonCodec codec) {
        requireSuccess(response, codec);
        return codec.decode(response.body(), type);
    }

    public static <T> T decodeOrThrow(HttpResponse<byte[]> response, TypeReference<T> type, JsonCodec codec) {
        requireSuccess(response, codec);
        return codec.decode(response.body(), type);
    }

    public static byte[] bytesOrThrow(HttpResponse<byte[]> response, JsonCodec codec) {
        requireSuccess(response, codec);
        return response.body();
    }

    public static void requireSuccess(HttpResponse<byte[]> response, JsonCodec codec) {
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return;
        }
        throw toException(status, response.body(), codec);
    }

    private static ApiException toException(int status, byte[] body, JsonCodec codec) {
        String raw = body != null ? new String(body, StandardCharsets.UTF_8) : "";

        if (status == 429) {
            String message = codec.tryDecodeField(body, "error").orElse("Quota exceeded");
            return new QuotaExceededException(message, raw);
        }
        if (status == 401) {
            return new AuthenticationException("Authentication failed — check your API key", raw);
        }

        ErrorResponse errorResponse = codec.tryDecode(body, ErrorResponse.class).orElse(null);
        String message = errorMessage(errorResponse, raw, status);

        switch (status) {
            case 403:
                return new PermissionException(message, errorResponse, raw);
            case 404:
                return new NotFoundException(message, errorResponse, raw);
            case 400:
                return new ValidationRequestException(message, errorResponse, raw);
            default:
                return new ApiException(message, status, errorResponse, raw);
        }
    }

    private static String errorMessage(ErrorResponse errorResponse, String raw, int status) {
        if (errorResponse != null && errorResponse.getMessage() != null) {
            return errorResponse.getMessage();
        }
        return raw.isBlank() ? ("Request failed with status " + status) : raw;
    }
}

package de.xingen.sdk.http;

import de.xingen.sdk.error.ApiException;
import de.xingen.sdk.error.AuthenticationException;
import de.xingen.sdk.error.NotFoundException;
import de.xingen.sdk.error.PermissionException;
import de.xingen.sdk.error.QuotaExceededException;
import de.xingen.sdk.error.ValidationRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseHandlerTest {

    private final JsonCodec codec = new JsonCodec();

    @Test
    void succeedsSilentlyOn2xx() {
        ResponseHandler.requireSuccess(FakeHttpResponse.of(202, "{\"id\":\"abc\"}"), codec);
    }

    @Test
    void mapsQuotaExceededShapeThatDiffersFromErrorResponse() {
        var response = FakeHttpResponse.of(429, "{\"error\":\"Quota exceeded\"}");

        assertThatThrownBy(() -> ResponseHandler.requireSuccess(response, codec))
            .isInstanceOf(QuotaExceededException.class)
            .hasMessage("Quota exceeded")
            .satisfies(e -> {
                QuotaExceededException ex = (QuotaExceededException) e;
                assertThat(ex.getStatusCode()).isEqualTo(429);
                assertThat(ex.getErrorResponse()).isNull();
                assertThat(ex.getRawBody()).contains("Quota exceeded");
            });
    }

    @Test
    void mapsAuthenticationExceptionWithoutAttemptingErrorResponseParse() {
        var response = FakeHttpResponse.of(401, "");

        assertThatThrownBy(() -> ResponseHandler.requireSuccess(response, codec))
            .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void mapsAuthenticationExceptionEvenWithUnexpectedHtmlBody() {
        var response = FakeHttpResponse.of(401, "<html>not json</html>");

        assertThatThrownBy(() -> ResponseHandler.requireSuccess(response, codec))
            .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void mapsForbiddenWithErrorResponseBody() {
        var response = FakeHttpResponse.of(403,
            "{\"message\":\"Invoice exists but is not owned by caller\",\"error\":\"FORBIDDEN\",\"code\":403,\"timestamp\":\"2026-07-08T00:00:00Z\"}");

        assertThatThrownBy(() -> ResponseHandler.requireSuccess(response, codec))
            .isInstanceOf(PermissionException.class)
            .hasMessage("Invoice exists but is not owned by caller");
    }

    @Test
    void mapsNotFound() {
        var response = FakeHttpResponse.of(404,
            "{\"message\":\"The requested resource was not found\",\"error\":\"NOT_FOUND\",\"code\":404,\"timestamp\":\"2026-07-08T00:00:00Z\"}");

        assertThatThrownBy(() -> ResponseHandler.requireSuccess(response, codec))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void mapsBadRequestAndSurfacesFieldErrors() {
        var response = FakeHttpResponse.of(400,
            "{\"message\":\"Validation failed\",\"error\":\"BAD_REQUEST\",\"code\":400,"
                + "\"timestamp\":\"2026-07-08T00:00:00Z\",\"fieldErrors\":{\"invoiceNumber\":\"must not be blank\"}}");

        assertThatThrownBy(() -> ResponseHandler.requireSuccess(response, codec))
            .isInstanceOf(ValidationRequestException.class)
            .satisfies(e -> assertThat(((ValidationRequestException) e).getFieldErrors())
                .containsEntry("invoiceNumber", "must not be blank"));
    }

    @Test
    void mapsUnmappedStatusToGenericApiExceptionWithoutThrowingOnMalformedBody() {
        var response = FakeHttpResponse.of(500, "not even json {{{");

        assertThatThrownBy(() -> ResponseHandler.requireSuccess(response, codec))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> {
                ApiException apiException = (ApiException) e;
                assertThat(apiException.getStatusCode()).isEqualTo(500);
                assertThat(apiException.getErrorResponse()).isNull();
                assertThat(apiException.getRawBody()).isEqualTo("not even json {{{");
            });
    }

}

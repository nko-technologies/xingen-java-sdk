package de.xingen.sdk.http;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/** Minimal {@link HttpResponse} stand-in so unit tests can exercise status/body handling without real I/O. */
final class FakeHttpResponse implements HttpResponse<byte[]> {

    private final int statusCode;
    private final byte[] body;

    FakeHttpResponse(int statusCode, byte[] body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    static FakeHttpResponse of(int statusCode, String body) {
        return new FakeHttpResponse(statusCode, body == null ? new byte[0] : body.getBytes());
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public HttpRequest request() {
        return HttpRequest.newBuilder(URI.create("https://example.invalid")).build();
    }

    @Override
    public Optional<HttpResponse<byte[]>> previousResponse() {
        return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
        return HttpHeaders.of(java.util.Map.of(), (a, b) -> true);
    }

    @Override
    public byte[] body() {
        return body;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return Optional.empty();
    }

    @Override
    public URI uri() {
        return URI.create("https://example.invalid");
    }

    @Override
    public HttpClient.Version version() {
        return HttpClient.Version.HTTP_1_1;
    }
}

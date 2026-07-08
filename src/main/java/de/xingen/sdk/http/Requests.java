package de.xingen.sdk.http;

import de.xingen.sdk.error.XingenIOException;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Wraps {@link HttpTransport#send} so every resource client reports transport failures the same way. */
public final class Requests {

    private Requests() {}

    public static HttpResponse<byte[]> send(HttpTransport transport, HttpRequest request) {
        try {
            return transport.send(request);
        } catch (IOException e) {
            throw new XingenIOException("Request to " + request.uri() + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new XingenIOException("Request to " + request.uri() + " was interrupted", e);
        }
    }
}

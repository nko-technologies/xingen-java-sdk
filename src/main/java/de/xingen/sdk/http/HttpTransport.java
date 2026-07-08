package de.xingen.sdk.http;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Sends a request and returns its raw response. Exists as a seam so tests can substitute a fake
 * transport instead of performing real network I/O.
 */
public interface HttpTransport {

    HttpResponse<byte[]> send(HttpRequest request) throws IOException, InterruptedException;
}

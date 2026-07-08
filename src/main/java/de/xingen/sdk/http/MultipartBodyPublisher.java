package de.xingen.sdk.http;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Hand-rolls a {@code multipart/form-data} body — {@code java.net.http.HttpClient} has no built-in
 * support for it. Segments are kept as separate byte arrays (headers, file bytes, trailing CRLF)
 * rather than concatenated into one buffer, so large files aren't copied twice.
 */
public final class MultipartBodyPublisher {

    private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.UTF_8);

    private final String boundary = "xingen-boundary-" + UUID.randomUUID();
    private final List<byte[]> segments = new ArrayList<>();

    public MultipartBodyPublisher addFormField(String fieldName, String value) {
        segments.add(partHeader(fieldName, null, null));
        segments.add(value.getBytes(StandardCharsets.UTF_8));
        segments.add(CRLF);
        return this;
    }

    public MultipartBodyPublisher addFilePart(String fieldName, String filename, byte[] content, String contentType) {
        segments.add(partHeader(fieldName, filename, contentType));
        segments.add(content);
        segments.add(CRLF);
        return this;
    }

    public MultipartBodyPublisher addFilePart(String fieldName, Path path, String contentType) {
        try {
            return addFilePart(fieldName, path.getFileName().toString(), Files.readAllBytes(path), contentType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public HttpRequest.BodyPublisher build() {
        List<byte[]> withClosingBoundary = new ArrayList<>(segments);
        withClosingBoundary.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(withClosingBoundary);
    }

    public String contentTypeHeader() {
        return "multipart/form-data; boundary=" + boundary;
    }

    private byte[] partHeader(String fieldName, String filename, String contentType) {
        StringBuilder header = new StringBuilder();
        header.append("--").append(boundary).append("\r\n");
        header.append("Content-Disposition: form-data; name=\"").append(fieldName).append('"');
        if (filename != null) {
            header.append("; filename=\"").append(filename).append('"');
        }
        header.append("\r\n");
        if (contentType != null) {
            header.append("Content-Type: ").append(contentType).append("\r\n");
        }
        header.append("\r\n");
        return header.toString().getBytes(StandardCharsets.UTF_8);
    }
}

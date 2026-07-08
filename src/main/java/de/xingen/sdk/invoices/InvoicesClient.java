package de.xingen.sdk.invoices;

import com.fasterxml.jackson.core.type.TypeReference;
import de.xingen.sdk.error.XingenCancellationException;
import de.xingen.sdk.error.XingenTimeoutException;
import de.xingen.sdk.http.HttpTransport;
import de.xingen.sdk.http.JsonCodec;
import de.xingen.sdk.http.MultipartBodyPublisher;
import de.xingen.sdk.http.RequestBuilder;
import de.xingen.sdk.http.Requests;
import de.xingen.sdk.http.ResponseHandler;
import de.xingen.sdk.internal.Preconditions;
import de.xingen.sdk.model.ValidationProfile;
import de.xingen.sdk.paging.Page;
import de.xingen.sdk.paging.PageIterator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

/** Submit invoices for validation and retrieve results. Reachable via {@code XingenClient#invoices()}. */
public final class InvoicesClient {

    private static final String BASE_PATH = "/v1/invoices";
    private static final String VALIDATE_PATH = BASE_PATH + "/validate";
    private static final String VALIDATE_IDOC_PATH = BASE_PATH + "/validate/idoc";
    private static final String VALIDATE_ODATA_PATH = BASE_PATH + "/validate/odata";

    private final HttpTransport transport;
    private final RequestBuilder requestBuilder;
    private final JsonCodec json;
    private final Sleeper sleeper;

    public InvoicesClient(HttpTransport transport, RequestBuilder requestBuilder, JsonCodec json) {
        this(transport, requestBuilder, json, Sleeper.REAL);
    }

    InvoicesClient(HttpTransport transport, RequestBuilder requestBuilder, JsonCodec json, Sleeper sleeper) {
        this.transport = transport;
        this.requestBuilder = requestBuilder;
        this.json = json;
        this.sleeper = sleeper;
    }

    /** Queues a structured JSON invoice for async validation. Poll {@link #get} or use {@link #submitAndWait} for the result. */
    public InvoiceSubmissionResult submit(InvoiceSubmission submission) {
        Preconditions.requireNonNull(submission, "submission");
        HttpRequest httpRequest = jsonPost(BASE_PATH, submission);
        return decode(httpRequest, InvoiceSubmissionResult.class);
    }

    public InvoiceRecord get(String id) {
        Preconditions.requireNonBlank(id, "id");
        HttpRequest httpRequest = requestBuilder.newRequest(BASE_PATH + "/" + id).GET().build();
        return decode(httpRequest, InvoiceRecord.class);
    }

    /** Matches the backend's default sort of {@code createdAt,desc} when {@code sort} is null. */
    public Page<InvoiceRecord> list(int page, int size, String sort) {
        Map<String, String> query = RequestBuilder.query()
            .put("page", page)
            .put("size", size)
            .put("sort", sort)
            .build();
        HttpRequest httpRequest = requestBuilder.newRequest(BASE_PATH, query).GET().build();
        HttpResponse<byte[]> response = Requests.send(transport, httpRequest);
        return ResponseHandler.decodeOrThrow(response, new TypeReference<>() {
        }, json);
    }

    /** Lazily iterates every invoice across all pages, fetching the next page only once the current one is exhausted. */
    public PageIterator<InvoiceRecord> listAll(int pageSize) {
        return new PageIterator<>(pageIndex -> list(pageIndex, pageSize, "createdAt,desc"));
    }

    /**
     * Uploads a UBL XML, CII XML, or ZUGFeRD PDF file for validation. Processing is asynchronous —
     * poll {@link #get} or use {@link #validateFileAndWait} for the result.
     */
    public InvoiceSubmissionResult validateFile(Path file, ValidationProfile profile) {
        Preconditions.requireNonNull(file, "file");
        return validate(VALIDATE_PATH, file.getFileName().toString(), readBytes(file), profile);
    }

    /** Same as {@link #validateFile(Path, ValidationProfile)}, for callers who already hold the file bytes in memory. */
    public InvoiceSubmissionResult validateFile(String filename, byte[] content, ValidationProfile profile) {
        return validate(VALIDATE_PATH, filename, content, profile);
    }

    /** Uploads a SAP IDoc XML file for validation. Processing is asynchronous — poll {@link #get} or use {@link #validateIdocAndWait}. */
    public InvoiceSubmissionResult validateIdoc(Path file, ValidationProfile profile) {
        Preconditions.requireNonNull(file, "file");
        return validate(VALIDATE_IDOC_PATH, file.getFileName().toString(), readBytes(file), profile);
    }

    /** Same as {@link #validateIdoc(Path, ValidationProfile)}, for callers who already hold the file bytes in memory. */
    public InvoiceSubmissionResult validateIdoc(String filename, byte[] content, ValidationProfile profile) {
        return validate(VALIDATE_IDOC_PATH, filename, content, profile);
    }

    /**
     * Submits a raw SAP S/4HANA OData supplier-invoice JSON payload for validation. Ships as a thin
     * passthrough in v1 rather than a fully typed model — the payload is large and SAP-integration
     * specific; a typed model is a candidate for a later release based on demand.
     */
    public InvoiceSubmissionResult submitOData(String rawJson, ValidationProfile profile) {
        Preconditions.requireNonBlank(rawJson, "rawJson");
        HttpRequest httpRequest = odataPost(rawJson.getBytes(StandardCharsets.UTF_8), profile);
        return decode(httpRequest, InvoiceSubmissionResult.class);
    }

    /** Same as {@link #submitOData(String, ValidationProfile)}, for callers building the payload as a {@code Map} instead of raw JSON. */
    public InvoiceSubmissionResult submitOData(Map<String, Object> payload, ValidationProfile profile) {
        Preconditions.requireNonNull(payload, "payload");
        HttpRequest httpRequest = odataPost(json.encode(payload), profile);
        return decode(httpRequest, InvoiceSubmissionResult.class);
    }

    private HttpRequest odataPost(byte[] body, ValidationProfile profile) {
        Preconditions.requireNonNull(profile, "profile");
        Map<String, String> query = RequestBuilder.query().put("profile", profile.name()).build();
        return requestBuilder.newRequest(VALIDATE_ODATA_PATH, query)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();
    }

    /** Exports a validated invoice as a ZUGFeRD-compliant PDF with embedded XML. */
    public byte[] downloadPdf(String id) {
        Preconditions.requireNonBlank(id, "id");
        HttpRequest httpRequest = requestBuilder.newRequest(BASE_PATH + "/" + id + "/download")
            .setHeader("Accept", "application/pdf")
            .GET().build();
        return ResponseHandler.bytesOrThrow(Requests.send(transport, httpRequest), json);
    }

    /** Exports a validated invoice as a SAP IDoc XML file. */
    public byte[] downloadIdocXml(String id) {
        Preconditions.requireNonBlank(id, "id");
        HttpRequest httpRequest = requestBuilder.newRequest(BASE_PATH + "/" + id + "/download/idoc")
            .setHeader("Accept", "application/xml")
            .GET().build();
        return ResponseHandler.bytesOrThrow(Requests.send(transport, httpRequest), json);
    }

    /** Submits {@code submission} and polls {@link #get} until validation reaches a terminal status. */
    public InvoiceRecord submitAndWait(InvoiceSubmission submission, PollOptions options) {
        return pollUntilTerminal(submit(submission).getId(), options);
    }

    /** Uploads {@code file} and polls {@link #get} until validation reaches a terminal status. */
    public InvoiceRecord validateFileAndWait(Path file, ValidationProfile profile, PollOptions options) {
        return pollUntilTerminal(validateFile(file, profile).getId(), options);
    }

    /** Same as {@link #validateFileAndWait(Path, ValidationProfile, PollOptions)}, for in-memory file bytes. */
    public InvoiceRecord validateFileAndWait(String filename, byte[] content, ValidationProfile profile, PollOptions options) {
        return pollUntilTerminal(validateFile(filename, content, profile).getId(), options);
    }

    /** Uploads {@code file} as a SAP IDoc and polls {@link #get} until validation reaches a terminal status. */
    public InvoiceRecord validateIdocAndWait(Path file, ValidationProfile profile, PollOptions options) {
        return pollUntilTerminal(validateIdoc(file, profile).getId(), options);
    }

    /** Same as {@link #validateIdocAndWait(Path, ValidationProfile, PollOptions)}, for in-memory file bytes. */
    public InvoiceRecord validateIdocAndWait(String filename, byte[] content, ValidationProfile profile, PollOptions options) {
        return pollUntilTerminal(validateIdoc(filename, content, profile).getId(), options);
    }

    /**
     * Polls {@code GET /v1/invoices/{id}} with exponential backoff until the invoice reaches
     * {@link InvoiceStatus#VALIDATED} or {@link InvoiceStatus#FAILED_VALIDATION} — both are terminal,
     * successful outcomes from the SDK's perspective (a failed validation is still a completed API
     * call), so only a cancellation, timeout, or transport failure throws.
     */
    private InvoiceRecord pollUntilTerminal(String id, PollOptions options) {
        Instant deadline = Instant.now().plus(options.getTimeout());
        Duration interval = options.getInitialInterval();
        InvoiceRecord latest = get(id);

        while (!latest.getStatus().isTerminal()) {
            if (options.getCancellationCheck().getAsBoolean()) {
                throw new XingenCancellationException("Polling for invoice " + id + " was cancelled");
            }
            if (Instant.now().isAfter(deadline)) {
                throw new XingenTimeoutException(
                    "Timed out waiting for invoice " + id + " to reach a terminal status", latest);
            }
            sleep(interval);
            interval = Duration.ofMillis(Math.min(
                (long) (interval.toMillis() * options.getBackoffMultiplier()),
                options.getMaxInterval().toMillis()));
            latest = get(id);
        }
        return latest;
    }

    private void sleep(Duration duration) {
        try {
            sleeper.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new XingenCancellationException("Polling was interrupted");
        }
    }

    private InvoiceSubmissionResult validate(String path, String filename, byte[] content, ValidationProfile profile) {
        Preconditions.requireNonBlank(filename, "filename");
        Preconditions.requireNonNull(content, "content");
        Preconditions.requireNonNull(profile, "profile");

        // `profile` is a query parameter here, not a form field, even though the endpoint is
        // multipart/form-data — the backend binds it from the query string.
        MultipartBodyPublisher multipart = new MultipartBodyPublisher()
            .addFilePart("file", filename, content, guessContentType(filename));
        Map<String, String> query = RequestBuilder.query().put("profile", profile.name()).build();

        HttpRequest httpRequest = requestBuilder.newRequest(path, query)
            .header("Content-Type", multipart.contentTypeHeader())
            .POST(multipart.build())
            .build();
        return decode(httpRequest, InvoiceSubmissionResult.class);
    }

    private static byte[] readBytes(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String guessContentType(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".xml")) {
            return "application/xml";
        }
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        return "application/octet-stream";
    }

    HttpRequest jsonPost(String path, Object body) {
        return requestBuilder.newRequest(path)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofByteArray(json.encode(body)))
            .build();
    }

    <T> T decode(HttpRequest httpRequest, Class<T> type) {
        HttpResponse<byte[]> response = Requests.send(transport, httpRequest);
        return ResponseHandler.decodeOrThrow(response, type, json);
    }
}

package de.xingen.sdk.invoices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.xingen.sdk.error.XingenCancellationException;
import de.xingen.sdk.error.XingenTimeoutException;
import de.xingen.sdk.http.JdkHttpTransport;
import de.xingen.sdk.http.JsonCodec;
import de.xingen.sdk.http.RequestBuilder;
import de.xingen.sdk.model.ValidationProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the {@code *AndWait} polling loop with a fake {@link Sleeper} so the tests run
 * instantly regardless of the configured backoff — only the HTTP round trips (to a real, local
 * {@link HttpServer}) consume actual wall-clock time.
 */
class PollingTest {

    private HttpServer server;
    private InvoicesClient client;
    private final List<Duration> recordedSleeps = new CopyOnWriteArrayList<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        RequestBuilder requestBuilder = new RequestBuilder(
            URI.create("http://localhost:" + server.getAddress().getPort()),
            "xgn_test_abc123", "test-agent", Duration.ofSeconds(5));
        client = new InvoicesClient(
            new JdkHttpTransport(HttpClient.newHttpClient()),
            requestBuilder,
            new JsonCodec(),
            duration -> recordedSleeps.add(duration));
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void submitAndWaitPollsUntilValidatedApplyingExponentialBackoff() {
        server.createContext("/v1/invoices", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                respond(exchange, 202, "{\"id\":\"inv_1\",\"status\":\"processing\"}");
            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        });
        AtomicInteger pollCount = new AtomicInteger();
        server.createContext("/v1/invoices/inv_1", exchange -> {
            boolean terminal = pollCount.incrementAndGet() >= 3;
            respond(exchange, 200, recordJson("inv_1", terminal ? "validated" : "processing", true));
        });

        InvoiceRecord result = client.submitAndWait(minimalSubmission(), PollOptions.defaults());

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.VALIDATED);
        assertThat(result.getValidationResult().isValid()).isTrue();
        // 2 processing responses observed -> 2 sleeps before the 3rd (terminal) poll.
        assertThat(recordedSleeps).hasSize(2);
        assertThat(recordedSleeps.get(0)).isEqualTo(Duration.ofMillis(500));
        assertThat(recordedSleeps.get(1)).isEqualTo(Duration.ofMillis(750)); // 500 * 1.5 backoff
    }

    @Test
    void validateFileAndWaitReturnsNormallyOnFailedValidation() {
        server.createContext("/v1/invoices/validate", exchange -> respond(exchange, 202, "{\"id\":\"inv_2\",\"status\":\"processing\"}"));
        server.createContext("/v1/invoices/inv_2", exchange -> respond(exchange, 200, recordJson("inv_2", "failed_validation", false)));

        InvoiceRecord result = client.validateFileAndWait("invoice.xml", "<x/>".getBytes(StandardCharsets.UTF_8),
            ValidationProfile.EN16931, PollOptions.defaults());

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.FAILED_VALIDATION);
        assertThat(result.getValidationResult().isValid()).isFalse();
    }

    @Test
    void timesOutWithPartialResultWhenDeadlineElapses() {
        server.createContext("/v1/invoices", exchange -> respond(exchange, 202, "{\"id\":\"inv_3\",\"status\":\"processing\"}"));
        server.createContext("/v1/invoices/inv_3", exchange -> respond(exchange, 200, recordJson("inv_3", "processing", true)));

        PollOptions immediateTimeout = PollOptions.builder().timeout(Duration.ZERO).build();

        assertThatThrownBy(() -> client.submitAndWait(minimalSubmission(), immediateTimeout))
            .isInstanceOf(XingenTimeoutException.class)
            .satisfies(e -> {
                InvoiceRecord partial = (InvoiceRecord) ((XingenTimeoutException) e).getPartialResult();
                assertThat(partial.getId()).isEqualTo("inv_3");
                assertThat(partial.getStatus()).isEqualTo(InvoiceStatus.PROCESSING);
            });
    }

    @Test
    void cancellationCheckAbortsPolling() {
        server.createContext("/v1/invoices", exchange -> respond(exchange, 202, "{\"id\":\"inv_4\",\"status\":\"processing\"}"));
        server.createContext("/v1/invoices/inv_4", exchange -> respond(exchange, 200, recordJson("inv_4", "processing", true)));

        PollOptions cancelled = PollOptions.builder().cancellationCheck(() -> true).build();

        assertThatThrownBy(() -> client.submitAndWait(minimalSubmission(), cancelled))
            .isInstanceOf(XingenCancellationException.class);
        assertThat(recordedSleeps).isEmpty();
    }

    private static InvoiceSubmission minimalSubmission() {
        return InvoiceSubmission.builder()
            .invoiceNumber("INV-1")
            .issueDate(java.time.LocalDate.of(2026, 1, 1))
            .currency("EUR")
            .validationProfile(ValidationProfile.EN16931)
            .supplier(InvoiceSubmission.PartyInput.builder().name("Seller").build())
            .buyer(InvoiceSubmission.PartyInput.builder().name("Buyer").build())
            .addLine(InvoiceSubmission.LineInput.builder()
                .description("Item")
                .quantity(java.math.BigDecimal.ONE)
                .unit("C62")
                .price(java.math.BigDecimal.TEN)
                .taxRate(java.math.BigDecimal.ZERO)
                .build())
            .build();
    }

    private static String recordJson(String id, String status, boolean valid) {
        boolean processing = "processing".equals(status);
        String canonicalJson = processing ? "null" : "{\"invoiceNumber\":\"INV-1\",\"currency\":\"EUR\",\"lines\":[],\"notes\":[]}";
        String validationResult = processing ? "null" : "{\"valid\":" + valid + ",\"errors\":[],\"kositResult\":null}";
        return "{\"id\":\"" + id + "\",\"status\":\"" + status + "\",\"createdAt\":\"2026-07-08T09:30:00Z\","
            + "\"validationProfile\":\"EN16931\",\"invoiceFormat\":\"UBL\",\"uploadedBy\":\"user_abc\","
            + "\"sandbox\":false,\"apiKeyId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\","
            + "\"canonicalJson\":" + canonicalJson + ",\"validationResult\":" + validationResult + "}";
    }

    private static void respond(HttpExchange exchange, int status, String body) {
        try {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            exchange.close();
        }
    }
}

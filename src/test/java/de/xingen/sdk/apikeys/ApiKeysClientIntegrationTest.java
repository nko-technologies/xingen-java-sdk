package de.xingen.sdk.apikeys;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.xingen.sdk.XingenClient;
import de.xingen.sdk.error.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises {@link ApiKeysClient} against a real (loopback) HTTP server so the actual
 * {@code java.net.http.HttpClient} code path — not a fake transport — is under test.
 */
class ApiKeysClientIntegrationTest {

    private HttpServer server;
    private XingenClient client;
    private final AtomicReference<HttpExchange> lastExchange = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        client = XingenClient.builder()
            .apiKey("xgn_test_abc123")
            .baseUrl("http://localhost:" + server.getAddress().getPort())
            .build();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void createReturnsRawKeyOnce() throws Exception {
        UUID id = UUID.randomUUID();
        AtomicReference<String> capturedBody = new AtomicReference<>();
        server.createContext("/v1/api-keys", exchange -> {
            lastExchange.set(exchange);
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }
            // Must read the request body before responding — closing the exchange also closes its streams.
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String body = "{\"id\":\"" + id + "\",\"rawKey\":\"xgn_test_generated\",\"name\":\"CI\","
                + "\"sandbox\":true,\"quotaLimit\":null,\"createdAt\":\"2026-07-08T00:00:00Z\"}";
            respond(exchange, 201, body);
        });

        CreatedApiKey created = client.apiKeys().create(CreateApiKeyRequest.builder()
            .name("CI")
            .sandbox(true)
            .build());

        assertThat(created.getId()).isEqualTo(id);
        assertThat(created.getRawKey()).isEqualTo("xgn_test_generated");
        assertThat(created.isSandbox()).isTrue();
        assertThat(created.getQuotaLimit()).isNull();

        assertThat(lastExchange.get().getRequestHeaders().getFirst("Authorization")).isEqualTo("Bearer xgn_test_abc123");
        assertThat(capturedBody.get()).contains("\"name\":\"CI\"").contains("\"sandbox\":true");
    }

    @Test
    void listDeserializesEachKey() throws Exception {
        UUID id = UUID.randomUUID();
        server.createContext("/v1/api-keys", exchange -> {
            String body = "[{\"id\":\"" + id + "\",\"name\":\"CI\",\"keyPrefix\":\"xgn_live\",\"sandbox\":false,"
                + "\"active\":true,\"quotaLimit\":10000,\"quotaUsed\":42,\"lastUsedAt\":null,"
                + "\"createdAt\":\"2026-07-01T00:00:00Z\",\"revokedAt\":null}]";
            respond(exchange, 200, body);
        });

        List<ApiKey> keys = client.apiKeys().list();

        assertThat(keys).hasSize(1);
        ApiKey key = keys.get(0);
        assertThat(key.getId()).isEqualTo(id);
        assertThat(key.getQuotaUsed()).isEqualTo(42);
        assertThat(key.isActive()).isTrue();
    }

    @Test
    void revokeSendsDeleteToKeyPath() throws Exception {
        UUID id = UUID.randomUUID();
        server.createContext("/v1/api-keys/" + id, exchange -> {
            lastExchange.set(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });

        client.apiKeys().revoke(id);

        assertThat(lastExchange.get().getRequestMethod()).isEqualTo("DELETE");
    }

    @Test
    void revokeUnknownKeyThrowsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        server.createContext("/v1/api-keys/" + id, exchange -> respond(exchange, 404,
            "{\"message\":\"API key not found\",\"error\":\"NOT_FOUND\",\"code\":404,\"timestamp\":\"2026-07-08T00:00:00Z\"}"));

        assertThatThrownBy(() -> client.apiKeys().revoke(id)).isInstanceOf(NotFoundException.class);
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}

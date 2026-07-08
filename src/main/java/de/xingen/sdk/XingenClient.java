package de.xingen.sdk;

import de.xingen.sdk.apikeys.ApiKeysClient;
import de.xingen.sdk.http.HttpTransport;
import de.xingen.sdk.http.JdkHttpTransport;
import de.xingen.sdk.http.JsonCodec;
import de.xingen.sdk.http.RequestBuilder;
import de.xingen.sdk.internal.Preconditions;
import de.xingen.sdk.invoices.InvoicesClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Entry point for the Xingen API. Construct via {@link #builder()}, then use {@link #invoices()}
 * and {@link #apiKeys()} to reach the resource-specific clients. A single {@code XingenClient}
 * holds one connection-pooled {@link HttpClient} and should be reused across calls rather than
 * rebuilt per request.
 */
public final class XingenClient {

    private static final URI DEFAULT_BASE_URL = URI.create("https://app.xingen.de/api");
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final InvoicesClient invoices;
    private final ApiKeysClient apiKeys;

    private XingenClient(Builder builder) {
        Preconditions.requireNonBlank(builder.apiKey, "apiKey is required");

        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(builder.connectTimeout)
            .build();
        HttpTransport transport = new JdkHttpTransport(httpClient);
        JsonCodec json = new JsonCodec();
        String userAgent = "xingen-java-sdk/" + sdkVersion() + " (Java/" + System.getProperty("java.version") + ")";
        RequestBuilder requestBuilder = new RequestBuilder(builder.baseUrl, builder.apiKey, userAgent, builder.requestTimeout);

        this.invoices = new InvoicesClient(transport, requestBuilder, json);
        this.apiKeys = new ApiKeysClient(transport, requestBuilder, json);
    }

    public static Builder builder() {
        return new Builder();
    }

    public InvoicesClient invoices() {
        return invoices;
    }

    public ApiKeysClient apiKeys() {
        return apiKeys;
    }

    private static String sdkVersion() {
        String version = XingenClient.class.getPackage().getImplementationVersion();
        return version != null ? version : "dev";
    }

    public static final class Builder {
        private String apiKey;
        private URI baseUrl = DEFAULT_BASE_URL;
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;

        private Builder() {}

        /** Required. An {@code xgn_live_}/{@code xgn_test_} prefixed API key. */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /** Overrides the default {@code https://app.xingen.de/api} base URL — useful for self-hosted or local testing. */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = URI.create(baseUrl);
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = Preconditions.requireNonNull(connectTimeout, "connectTimeout");
            return this;
        }

        /** Per-request timeout, applied to every call the SDK makes (not the total time of *AndWait polling helpers). */
        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = Preconditions.requireNonNull(requestTimeout, "requestTimeout");
            return this;
        }

        public XingenClient build() {
            return new XingenClient(this);
        }
    }
}

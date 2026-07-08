package de.xingen.sdk.http;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds {@link HttpRequest}s against a fixed base URL, attaching auth/user-agent headers and
 * handling query-string encoding so callers never string-concatenate URLs by hand.
 */
public final class RequestBuilder {

    private final String baseUrl;
    private final String apiKey;
    private final String userAgent;
    private final Duration requestTimeout;

    public RequestBuilder(URI baseUrl, String apiKey, String userAgent, Duration requestTimeout) {
        String normalized = baseUrl.toString();
        this.baseUrl = normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
        this.apiKey = apiKey;
        this.userAgent = userAgent;
        this.requestTimeout = requestTimeout;
    }

    public HttpRequest.Builder newRequest(String path) {
        return newRequest(path, Map.of());
    }

    public HttpRequest.Builder newRequest(String path, Map<String, String> queryParams) {
        return HttpRequest.newBuilder(buildUri(path, queryParams))
            .timeout(requestTimeout)
            .header("Authorization", "Bearer " + apiKey)
            .header("Accept", "application/json")
            .header("User-Agent", userAgent);
    }

    public static QueryParams query() {
        return new QueryParams();
    }

    private URI buildUri(String path, Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append(path.startsWith("/") ? path : "/" + path);
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            sb.append(first ? '?' : '&');
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }
        return URI.create(sb.toString());
    }

    /** Small ordered-map builder for query parameters, so call sites read as a fluent chain. */
    public static final class QueryParams {
        private final Map<String, String> params = new LinkedHashMap<>();

        public QueryParams put(String key, String value) {
            params.put(key, value);
            return this;
        }

        public QueryParams put(String key, Object value) {
            return put(key, value == null ? null : String.valueOf(value));
        }

        public Map<String, String> build() {
            return params;
        }
    }
}

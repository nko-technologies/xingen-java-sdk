package de.xingen.sdk.apikeys;

import com.fasterxml.jackson.core.type.TypeReference;
import de.xingen.sdk.http.HttpTransport;
import de.xingen.sdk.http.JsonCodec;
import de.xingen.sdk.http.Requests;
import de.xingen.sdk.http.RequestBuilder;
import de.xingen.sdk.http.ResponseHandler;
import de.xingen.sdk.internal.Preconditions;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

/** Create, list, and revoke API keys. Reachable via {@code XingenClient#apiKeys()}. */
public final class ApiKeysClient {

    private static final String BASE_PATH = "/v1/api-keys";

    private final HttpTransport transport;
    private final RequestBuilder requestBuilder;
    private final JsonCodec json;

    public ApiKeysClient(HttpTransport transport, RequestBuilder requestBuilder, JsonCodec json) {
        this.transport = transport;
        this.requestBuilder = requestBuilder;
        this.json = json;
    }

    /** The {@code rawKey} on the result is shown only this once — persist it immediately. */
    public CreatedApiKey create(CreateApiKeyRequest request) {
        Preconditions.requireNonNull(request, "request");
        byte[] body = json.encode(request);
        HttpRequest httpRequest = requestBuilder.newRequest(BASE_PATH)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();
        HttpResponse<byte[]> response = Requests.send(transport, httpRequest);
        return ResponseHandler.decodeOrThrow(response, CreatedApiKey.class, json);
    }

    public List<ApiKey> list() {
        HttpRequest httpRequest = requestBuilder.newRequest(BASE_PATH).GET().build();
        HttpResponse<byte[]> response = Requests.send(transport, httpRequest);
        return ResponseHandler.decodeOrThrow(response, new TypeReference<>() {
        }, json);
    }

    public void revoke(UUID id) {
        Preconditions.requireNonNull(id, "id");
        HttpRequest httpRequest = requestBuilder.newRequest(BASE_PATH + "/" + id).DELETE().build();
        HttpResponse<byte[]> response = Requests.send(transport, httpRequest);
        ResponseHandler.requireSuccess(response, json);
    }
}

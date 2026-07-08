package de.xingen.sdk.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

/**
 * Wraps a single, centrally-configured {@link ObjectMapper}. Unknown properties are tolerated on
 * decode since the backend's response shapes evolve independently of SDK releases.
 */
public final class JsonCodec {

    private final ObjectMapper mapper;

    public JsonCodec() {
        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public byte[] encode(Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public <T> T decode(byte[] body, Class<T> type) {
        try {
            return mapper.readValue(body, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public <T> T decode(byte[] body, TypeReference<T> type) {
        try {
            return mapper.readValue(body, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Never throws — returns empty on any parse failure, including an empty/null body. */
    public <T> Optional<T> tryDecode(byte[] body, Class<T> type) {
        if (body == null || body.length == 0) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(mapper.readValue(body, type));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /** Reads a single top-level string field without committing to a full type — used for the 429 body shape. */
    public Optional<String> tryDecodeField(byte[] body, String fieldName) {
        if (body == null || body.length == 0) {
            return Optional.empty();
        }
        try {
            JsonNode node = mapper.readTree(body);
            JsonNode field = node.get(fieldName);
            return (field == null || field.isNull()) ? Optional.empty() : Optional.of(field.asText());
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}

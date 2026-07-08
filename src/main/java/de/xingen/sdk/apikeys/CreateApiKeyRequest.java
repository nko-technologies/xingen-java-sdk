package de.xingen.sdk.apikeys;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Request body for {@link ApiKeysClient#create}. */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class CreateApiKeyRequest {

    private final String name;
    /** If true, requests using this key don't count toward quota. Defaults to {@code false}. */
    @Builder.Default
    private final boolean sandbox = false;
    /** Optional monthly quota. Null means unlimited (Pro only) — free-tier keys are capped server-side regardless. */
    private final Integer quotaLimit;
}

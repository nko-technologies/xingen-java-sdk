package de.xingen.sdk.apikeys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * Response from {@link ApiKeysClient#create}. {@link #getRawKey()} is shown only this once —
 * the backend never returns it again, so callers must persist it immediately.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "rawKey")
@EqualsAndHashCode
public class CreatedApiKey {

    private UUID id;
    private String rawKey;
    private String name;
    private boolean sandbox;
    /** Null means unlimited. */
    private Integer quotaLimit;
    private Instant createdAt;
}

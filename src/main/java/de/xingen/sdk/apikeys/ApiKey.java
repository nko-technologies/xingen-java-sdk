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

/** API key metadata as returned by list/create — the raw key value is never included here. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ApiKey {

    private UUID id;
    private String name;
    private String keyPrefix;
    private boolean sandbox;
    private boolean active;
    /** Null means unlimited. */
    private Integer quotaLimit;
    private int quotaUsed;
    private Instant lastUsedAt;
    private Instant createdAt;
    /** Null if the key is still active. */
    private Instant revokedAt;
}

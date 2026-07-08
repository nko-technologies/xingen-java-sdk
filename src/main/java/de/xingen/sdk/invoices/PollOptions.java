package de.xingen.sdk.invoices;

import lombok.Getter;

import java.time.Duration;
import java.util.function.BooleanSupplier;

/** Configures the polling loop used by the {@code *AndWait} helpers on {@link InvoicesClient}. */
@Getter
public final class PollOptions {

    private final Duration initialInterval;
    private final Duration maxInterval;
    private final double backoffMultiplier;
    private final Duration timeout;
    private final BooleanSupplier cancellationCheck;

    private PollOptions(Builder builder) {
        this.initialInterval = builder.initialInterval;
        this.maxInterval = builder.maxInterval;
        this.backoffMultiplier = builder.backoffMultiplier;
        this.timeout = builder.timeout;
        this.cancellationCheck = builder.cancellationCheck;
    }

    public static PollOptions defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

  public static final class Builder {
        private Duration initialInterval = Duration.ofMillis(500);
        private Duration maxInterval = Duration.ofSeconds(5);
        private double backoffMultiplier = 1.5;
        private Duration timeout = Duration.ofSeconds(60);
        private BooleanSupplier cancellationCheck = () -> false;

        private Builder() {}

        public Builder initialInterval(Duration initialInterval) {
            this.initialInterval = initialInterval;
            return this;
        }

        public Builder maxInterval(Duration maxInterval) {
            this.maxInterval = maxInterval;
            return this;
        }

        public Builder backoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        /** Total time budget across the whole poll loop, not a per-request timeout. */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /** Polled once per loop iteration; a {@code true} result aborts the wait with {@link de.xingen.sdk.error.XingenCancellationException}. */
        public Builder cancellationCheck(BooleanSupplier cancellationCheck) {
            this.cancellationCheck = cancellationCheck;
            return this;
        }

        public PollOptions build() {
            return new PollOptions(this);
        }
    }
}

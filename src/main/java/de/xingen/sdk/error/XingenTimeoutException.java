package de.xingen.sdk.error;

import lombok.Getter;

/**
 * Thrown by a {@code *AndWait} polling helper when the configured {@code PollOptions} timeout
 * elapses before the invoice reaches a terminal status. The last known state is still reachable
 * via {@link #getPartialResult()} — it is typed as {@code Object} here to keep this package free
 * of a dependency on the {@code invoices} package; callers cast to {@code InvoiceRecord}.
 */
@Getter
public class XingenTimeoutException extends XingenException {

    private final Object partialResult;

    public XingenTimeoutException(String message, Object partialResult) {
        super(message);
        this.partialResult = partialResult;
    }

}

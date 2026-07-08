package de.xingen.sdk.error;

/** Wraps a network/transport-level failure (connection refused, DNS failure, etc.) — not an HTTP error response. */
public class XingenIOException extends XingenException {

    public XingenIOException(String message, Throwable cause) {
        super(message, cause);
    }
}

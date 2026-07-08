package de.xingen.sdk.error;

/** Base type for every exception thrown by this SDK. */
public class XingenException extends RuntimeException {

    public XingenException(String message) {
        super(message);
    }

    public XingenException(String message, Throwable cause) {
        super(message, cause);
    }
}

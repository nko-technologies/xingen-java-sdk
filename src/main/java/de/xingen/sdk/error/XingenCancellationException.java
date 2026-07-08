package de.xingen.sdk.error;

/** Thrown by a {@code *AndWait} polling helper when the caller-supplied cancellation check returns true. */
public class XingenCancellationException extends XingenException {

    public XingenCancellationException(String message) {
        super(message);
    }
}

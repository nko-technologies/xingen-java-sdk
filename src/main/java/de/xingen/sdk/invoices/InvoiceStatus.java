package de.xingen.sdk.invoices;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InvoiceStatus {
    PROCESSING("processing"),
    VALIDATED("validated"),
    FAILED_VALIDATION("failed_validation");

    private final String wireValue;

    InvoiceStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String wireValue() {
        return wireValue;
    }

    public boolean isTerminal() {
        return this != PROCESSING;
    }

    @JsonCreator
    public static InvoiceStatus fromWireValue(String value) {
        for (InvoiceStatus status : values()) {
            if (status.wireValue.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown invoice status: " + value);
    }
}

package de.xingen.sdk.invoices;

import de.xingen.sdk.http.JsonCodec;
import de.xingen.sdk.model.Invoice;
import de.xingen.sdk.model.Severity;
import de.xingen.sdk.model.ValidationError;
import de.xingen.sdk.model.ValidationLayer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceRecordDeserializationTest {

    private final JsonCodec codec = new JsonCodec();

    @Test
    void decodesValidatedInvoiceWithNestedInvoiceAndValidationResult() {
        InvoiceRecord record = decode("invoice-record.json");

        assertThat(record.getId()).isEqualTo("inv_01HXYZ");
        assertThat(record.getStatus()).isEqualTo(InvoiceStatus.VALIDATED);
        assertThat(record.isSandbox()).isTrue();

        Invoice invoice = record.getInvoice();
        assertThat(invoice.getInvoiceNumber()).isEqualTo("INV-2024-0042");
        assertThat(invoice.getIssueDate()).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(invoice.getPayableAmount()).isEqualByComparingTo(new BigDecimal("1184.05"));
        assertThat(invoice.getSupplier().getVatId()).isEqualTo("DE123456789");
        assertThat(invoice.getSupplier().getAddress().getCity()).isEqualTo("Berlin");
        assertThat(invoice.getLines()).hasSize(1);
        assertThat(invoice.getLines().get(0).getItemName()).isEqualTo("Software License Q1");

        assertThat(record.getValidationResult().isValid()).isTrue();
        assertThat(record.getValidationResult().getErrors()).isEmpty();
    }

    @Test
    void decodesProcessingInvoiceWithNullInvoiceAndValidationResult() {
        InvoiceRecord record = decode("invoice-record-processing.json");

        assertThat(record.getStatus()).isEqualTo(InvoiceStatus.PROCESSING);
        assertThat(record.getStatus().isTerminal()).isFalse();
        assertThat(record.getInvoice()).isNull();
        assertThat(record.getValidationResult()).isNull();
    }

    @Test
    void decodesFailedValidationWithErrorDetails() {
        InvoiceRecord record = decode("invoice-record-failed.json");

        assertThat(record.getStatus()).isEqualTo(InvoiceStatus.FAILED_VALIDATION);
        assertThat(record.getStatus().isTerminal()).isTrue();
        assertThat(record.getValidationResult().isValid()).isFalse();

        ValidationError error = record.getValidationResult().getErrors().get(0);
        assertThat(error.getCode()).isEqualTo("BR-01");
        assertThat(error.getLayer()).isEqualTo(ValidationLayer.CORE);
        assertThat(error.getSeverity()).isEqualTo(Severity.ERROR);
    }

    private InvoiceRecord decode(String fixtureName) {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/" + fixtureName)) {
            return codec.decode(in.readAllBytes(), InvoiceRecord.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

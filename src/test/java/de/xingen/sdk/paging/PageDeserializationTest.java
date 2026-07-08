package de.xingen.sdk.paging;

import com.fasterxml.jackson.core.type.TypeReference;
import de.xingen.sdk.http.JsonCodec;
import de.xingen.sdk.invoices.InvoiceRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;

class PageDeserializationTest {

    private final JsonCodec codec = new JsonCodec();

    @Test
    void decodesPageIgnoringUnknownSpringDataFields() {
        Page<InvoiceRecord> page;
        try (InputStream in = getClass().getResourceAsStream("/fixtures/page-of-invoices.json")) {
            page = codec.decode(in.readAllBytes(), new TypeReference<Page<InvoiceRecord>>() {});
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo("inv_01HXYZ");
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
        assertThat(page.isEmpty()).isFalse();
    }
}

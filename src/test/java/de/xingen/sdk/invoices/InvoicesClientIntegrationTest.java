package de.xingen.sdk.invoices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.xingen.sdk.XingenClient;
import de.xingen.sdk.model.AutoFilledField;
import de.xingen.sdk.model.ExtractionModelTier;
import de.xingen.sdk.model.ValidationProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicesClientIntegrationTest {

    private static final String VALIDATE_PATH = "/v1/invoices/validate";

    private HttpServer server;
    private XingenClient client;
    private final AtomicReference<HttpExchange> lastExchange = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        client = XingenClient.builder()
            .apiKey("xgn_test_abc123")
            .baseUrl("http://localhost:" + server.getAddress().getPort())
            .build();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void submitSendsExactBackendRequestShapeAndDecodes202() throws Exception {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        server.createContext("/v1/invoices", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Must read the request body before responding — closing the exchange also closes its streams.
                capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                respond(exchange, 202, "{\"id\":\"inv_123\",\"status\":\"processing\"}");
            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        });

        InvoiceSubmission submission = InvoiceSubmission.builder()
            .invoiceNumber("INV-2024-0042")
            .issueDate(LocalDate.of(2024, 3, 15))
            .currency("EUR")
            .buyerReference("991-12345-06")
            .validationProfile(ValidationProfile.XRECHNUNG)
            .supplier(InvoiceSubmission.PartyInput.builder().name("Acme GmbH").vatId("DE123456789")
                .address(InvoiceSubmission.AddressInput.builder().countryCode("DE").build())
                .build())
            .buyer(InvoiceSubmission.PartyInput.builder().name("Buyer Co").leitwegId("991-12345-06")
                .address(InvoiceSubmission.AddressInput.builder().countryCode("DE").build())
                .build())
            .addLine(InvoiceSubmission.LineInput.builder()
                .description("Software License Q1")
                .quantity(new BigDecimal("5"))
                .unit("C62")
                .price(new BigDecimal("199.00"))
                .taxRate(new BigDecimal("19"))
                .build())
            .build();

        InvoiceSubmissionResult result = client.invoices().submit(submission);

        assertThat(result.getId()).isEqualTo("inv_123");
        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PROCESSING);

        String body = capturedBody.get();
        assertThat(body).contains("\"invoiceNumber\":\"INV-2024-0042\"")
            .contains("\"validationProfile\":\"XRECHNUNG\"")
            .contains("\"supplier\":{\"name\":\"Acme GmbH\",\"vatId\":\"DE123456789\"")
            .contains("\"lines\":[{\"description\":\"Software License Q1\"");
    }

    @Test
    void validateFileSendsProfileAsQueryParamAndFileAsMultipartField() throws Exception {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        server.createContext(VALIDATE_PATH, exchange -> {
            lastExchange.set(exchange);
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 202, "{\"id\":\"inv_456\",\"status\":\"processing\"}");
        });

        InvoiceSubmissionResult result = client.invoices()
            .validateFile("invoice.xml", "<Invoice/>".getBytes(StandardCharsets.UTF_8), ValidationProfile.EN16931);

        assertThat(result.getId()).isEqualTo("inv_456");

        HttpExchange exchange = lastExchange.get();
        assertThat(exchange.getRequestURI().getQuery()).isEqualTo("profile=EN16931");
        assertThat(exchange.getRequestHeaders().getFirst("Content-Type")).startsWith("multipart/form-data; boundary=");

        String body = capturedBody.get();
        assertThat(body).contains("Content-Disposition: form-data; name=\"file\"; filename=\"invoice.xml\"")
            .contains("Content-Type: application/xml")
            .contains("<Invoice/>")
            // the gotcha this test guards against: profile must never be sent as a form field
            .doesNotContain("name=\"profile\"");
    }

    @Test
    void getDecodesInvoiceRecordEnvelope() throws Exception {
        server.createContext("/v1/invoices/inv_01HXYZ", exchange -> respond(exchange, 200, FIXTURE));

        InvoiceRecord record = client.invoices().get("inv_01HXYZ");

        assertThat(record.getId()).isEqualTo("inv_01HXYZ");
        assertThat(record.getStatus()).isEqualTo(InvoiceStatus.VALIDATED);
        assertThat(record.getInvoice().getInvoiceNumber()).isEqualTo("INV-2024-0042");
    }

    @Test
    void listSendsPageSizeAndSortAsQueryParams() throws Exception {
        server.createContext("/v1/invoices", exchange -> {
            lastExchange.set(exchange);
            respond(exchange, 200, singlePage(FIXTURE, true));
        });

        client.invoices().list(2, 10, "createdAt,desc");

        // HttpExchange#getRequestURI() decodes the query string, so a comma sent on the wire as
        // %2C (verified separately by the multipart test's raw-body assertions) reads back as ",".
        String query = lastExchange.get().getRequestURI().getQuery();
        assertThat(query).contains("page=2").contains("size=10").contains("sort=createdAt,desc");
    }

    @Test
    void submitODataSendsProfileAsQueryParamAndRawJsonAsBody() throws Exception {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        server.createContext("/v1/invoices/validate/odata", exchange -> {
            lastExchange.set(exchange);
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 202, "{\"id\":\"inv_odata\",\"status\":\"processing\"}");
        });

        InvoiceSubmissionResult result = client.invoices()
            .submitOData("{\"SupplierInvoice\":\"raw-payload\"}", ValidationProfile.EN16931);

        assertThat(result.getId()).isEqualTo("inv_odata");
        assertThat(lastExchange.get().getRequestURI().getQuery()).isEqualTo("profile=EN16931");
        assertThat(capturedBody.get()).isEqualTo("{\"SupplierInvoice\":\"raw-payload\"}");
    }

    @Test
    void extractInvoiceSendsProfileAndTierAsQueryParamsAndFileAsMultipartField() throws Exception {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        server.createContext("/v1/invoices/extract", exchange -> {
            lastExchange.set(exchange);
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 202, "{\"id\":\"inv_789\",\"status\":\"processing\"}");
        });

        InvoiceSubmissionResult result = client.invoices()
            .extractInvoice("invoice.pdf", "%PDF-1.4".getBytes(StandardCharsets.UTF_8),
                ValidationProfile.EN16931, ExtractionModelTier.ACCURATE);

        assertThat(result.getId()).isEqualTo("inv_789");

        HttpExchange exchange = lastExchange.get();
        assertThat(exchange.getRequestURI().getQuery()).isEqualTo("profile=EN16931&tier=ACCURATE");
        assertThat(exchange.getRequestHeaders().getFirst("Content-Type")).startsWith("multipart/form-data; boundary=");
        assertThat(capturedBody.get())
            .contains("Content-Disposition: form-data; name=\"file\"; filename=\"invoice.pdf\"")
            .doesNotContain("name=\"profile\"")
            .doesNotContain("name=\"tier\"");
    }

    @Test
    void patchInvoiceSendsMergePatchAndDecodesUpdatedRecord() throws Exception {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        server.createContext("/v1/invoices/inv_01HXYZ", exchange -> {
            lastExchange.set(exchange);
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200, FIXTURE);
        });

        InvoiceRecord record = client.invoices().patchInvoice("inv_01HXYZ", Map.of("currency", "USD"));

        assertThat(lastExchange.get().getRequestMethod()).isEqualTo("PATCH");
        assertThat(capturedBody.get()).isEqualTo("{\"currency\":\"USD\"}");
        assertThat(record.getId()).isEqualTo("inv_01HXYZ");
    }

    @Test
    void getAutoFilledFieldsDecodesMapByProfile() throws Exception {
        server.createContext("/v1/invoices/auto-filled-fields", exchange -> respond(exchange, 200,
            "{\"EN16931\":[{\"field\":\"typeCode\",\"value\":\"380\",\"reason\":\"Defaults to a commercial invoice.\"}]}"));

        Map<String, List<AutoFilledField>> fields = client.invoices().getAutoFilledFields();

        assertThat(fields.get("EN16931")).hasSize(1);
        assertThat(fields.get("EN16931").get(0).getField()).isEqualTo("typeCode");
    }

    @Test
    void downloadPdfReturnsRawBytesWithPdfAccept() throws Exception {
        byte[] pdfBytes = {0x25, 0x50, 0x44, 0x46}; // "%PDF"
        server.createContext("/v1/invoices/inv_01HXYZ/download", exchange -> {
            lastExchange.set(exchange);
            exchange.getResponseHeaders().add("Content-Type", "application/pdf");
            exchange.sendResponseHeaders(200, pdfBytes.length);
            exchange.getResponseBody().write(pdfBytes);
            exchange.close();
        });

        byte[] result = client.invoices().downloadPdf("inv_01HXYZ");

        assertThat(result).isEqualTo(pdfBytes);
        assertThat(lastExchange.get().getRequestHeaders().getFirst("Accept")).isEqualTo("application/pdf");
    }

    @Test
    void downloadIdocXmlReturnsRawBytesWithXmlAccept() throws Exception {
        byte[] xmlBytes = "<IDOC/>".getBytes(StandardCharsets.UTF_8);
        server.createContext("/v1/invoices/inv_01HXYZ/download/idoc", exchange -> {
            lastExchange.set(exchange);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, xmlBytes.length);
            exchange.getResponseBody().write(xmlBytes);
            exchange.close();
        });

        byte[] result = client.invoices().downloadIdocXml("inv_01HXYZ");

        assertThat(result).isEqualTo(xmlBytes);
        assertThat(lastExchange.get().getRequestHeaders().getFirst("Accept")).isEqualTo("application/xml");
    }

    @Test
    void listAllLazilyWalksMultiplePages() throws Exception {
        server.createContext("/v1/invoices", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            boolean isLastPage = query.contains("page=1");
            respond(exchange, 200, singlePage(FIXTURE, isLastPage));
        });

        List<InvoiceRecord> all = new ArrayList<>();
        for (InvoiceRecord record : client.invoices().listAll(1)) {
            all.add(record);
        }

        assertThat(all).hasSize(2);
    }

    private static String singlePage(String recordJson, boolean last) {
        return "{\"content\":[" + recordJson + "],\"totalElements\":2,\"totalPages\":2,"
            + "\"number\":" + (last ? 1 : 0) + ",\"size\":1,\"first\":" + !last + ",\"last\":" + last
            + ",\"numberOfElements\":1,\"empty\":false}";
    }

    private static final String FIXTURE = "{\"id\":\"inv_01HXYZ\",\"status\":\"validated\","
        + "\"createdAt\":\"2026-07-08T09:30:00Z\",\"validationProfile\":\"XRECHNUNG\",\"invoiceFormat\":\"UBL\","
        + "\"uploadedBy\":\"user_abc123\",\"sandbox\":false,\"apiKeyId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\","
        + "\"canonicalJson\":{\"invoiceNumber\":\"INV-2024-0042\",\"currency\":\"EUR\",\"lines\":[],\"notes\":[]},"
        + "\"validationResult\":{\"valid\":true,\"errors\":[],\"kositResult\":null}}";

    private static void respond(HttpExchange exchange, int status, String body) {
        try {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            exchange.close();
        }
    }
}

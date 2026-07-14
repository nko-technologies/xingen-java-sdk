# xingen-sdk

Java client SDK for the [Xingen](https://xingen.de) e-invoice validation API — submit UBL, CII,
ZUGFeRD, and SAP IDoc/OData invoices for validation against EN16931, XRechnung, and Peppol.

Requires Java 11+. Built on `java.net.http.HttpClient` — the only required dependency is Jackson,
which most JVM backends already have on the classpath.

> Status: v1, covering invoice submission/validation and API key management. Contacts and
> dashboard/user endpoints are not exposed (they're Firebase-auth-only on the backend).

## Install

**Maven Central** (once a release is published — see [Publishing](#publishing)):

```kotlin
dependencies {
    implementation("de.xingen:xingen-sdk:<version>")
}
```

```xml
<dependency>
    <groupId>de.xingen</groupId>
    <artifactId>xingen-sdk</artifactId>
    <version>&lt;version&gt;</version>
</dependency>
```

**JitPack** also works for any tag, including ones not yet promoted to Central:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.nko-technologies:xingen-java-sdk:<tag>")
}
```

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.nko-technologies</groupId>
    <artifactId>xingen-java-sdk</artifactId>
    <version>&lt;tag&gt;</version>
</dependency>
```

## Authentication

Every request needs an API key (`xgn_live_...` for production, `xgn_test_...` for sandbox — sandbox
requests never count toward quota). Create one from the Xingen dashboard or via `client.apiKeys()`.

```java
XingenClient client = XingenClient.builder()
    .apiKey(System.getenv("XINGEN_API_KEY"))
    .build();
```

`XingenClient` holds one connection-pooled `HttpClient` — construct it once and reuse it, don't
rebuild it per request. `.baseUrl(...)` overrides the default `https://app.xingen.de/api`, useful
for self-hosted or local (`./gradlew bootRun`, port 10001) testing.

## Validate a file

Every validate/submit endpoint is asynchronous — the backend queues the invoice and returns
immediately. Use a `*AndWait` helper to submit and poll for the result in one call:

```java
InvoiceRecord result = client.invoices().validateFileAndWait(
    Path.of("invoice.xml"),
    ValidationProfile.XRECHNUNG,
    PollOptions.defaults());

if (result.getStatus() == InvoiceStatus.VALIDATED && result.getValidationResult().isValid()) {
    System.out.println("Valid!");
} else {
    result.getValidationResult().getErrors().forEach(error ->
        System.out.println(error.getSeverity() + ": " + error.getMessage() + " (" + error.getField() + ")"));
}
```

`PollOptions` controls the backoff (`initialInterval`, `maxInterval`, `backoffMultiplier`), the
overall `timeout`, and an optional `cancellationCheck`. A **failed validation is not an exception**
— it's a completed API call that found the invoice invalid, so `*AndWait` returns normally with
`validationResult.isValid() == false`. Only a transport failure, cancellation, or timeout throws.

```java
PollOptions options = PollOptions.builder()
    .initialInterval(Duration.ofMillis(300))
    .maxInterval(Duration.ofSeconds(3))
    .timeout(Duration.ofSeconds(30))
    .build();
```

If you'd rather manage polling yourself, use the low-level pair:

```java
InvoiceSubmissionResult submitted = client.invoices().validateFile(Path.of("invoice.xml"), ValidationProfile.EN16931);
// ... later ...
InvoiceRecord record = client.invoices().get(submitted.getId());
```

`validateIdoc` / `validateIdocAndWait` work the same way for SAP IDoc XML files. Both also accept
`(String filename, byte[] content, ...)` if you already hold the file bytes in memory instead of a
`Path`.

## Submit a structured invoice (JSON)

```java
InvoiceSubmission submission = InvoiceSubmission.builder()
    .invoiceNumber("INV-2024-0042")
    .issueDate(LocalDate.of(2024, 3, 15))
    .currency("EUR")
    .buyerReference("991-12345-06")
    .validationProfile(ValidationProfile.XRECHNUNG)
    .supplier(InvoiceSubmission.PartyInput.builder().name("Acme GmbH").vatId("DE123456789")
        .address(InvoiceSubmission.AddressInput.builder().city("Berlin").countryCode("DE").build())
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

InvoiceRecord result = client.invoices().submitAndWait(submission, PollOptions.defaults());
```

SAP S/4HANA OData supplier-invoice payloads are supported as a thin passthrough — pass raw JSON or
a `Map<String, Object>` rather than a fully typed model:

```java
client.invoices().submitOData(rawODataJson, ValidationProfile.EN16931);
```

## Extract an invoice from a PDF (AI)

Upload a plain invoice PDF — including scanned/image-based PDFs — and let the backend extract
structured fields with Claude. Works exactly like the other submit endpoints: async, so use
`extractInvoiceAndWait` or the low-level `extractInvoice`/`get` pair.

```java
InvoiceRecord result = client.invoices().extractInvoiceAndWait(
    Path.of("scanned-invoice.pdf"),
    ValidationProfile.EN16931,
    ExtractionModelTier.FAST,   // or ACCURATE — higher accuracy, Pro subscription required
    PollOptions.defaults());
```

If the extraction missed a field or validation flagged something, correct it with a JSON
merge-patch (RFC 7386) and re-validate synchronously — only invoices that finished processing
(`VALIDATED` or `FAILED_VALIDATION`) can be corrected. Array fields (`lines`, `paymentMeans`,
`allowanceCharges`, `taxBreakdowns`) are replaced wholesale when present in the patch:

```java
InvoiceRecord corrected = client.invoices().patchInvoice(result.getId(), Map.of(
    "currency", "EUR",
    "buyerReference", "991-12345-06"));
```

To find out which fields the backend fills in automatically per profile (so you know what *not*
to prompt the user for):

```java
Map<String, List<AutoFilledField>> autoFilled = client.invoices().getAutoFilledFields();
```

## List and retrieve invoices

```java
Page<InvoiceRecord> page = client.invoices().list(0, 20, "createdAt,desc");

// or, to walk every invoice without managing page indices yourself:
for (InvoiceRecord record : client.invoices().listAll(50)) {
    System.out.println(record.getId() + " -> " + record.getStatus());
}

InvoiceRecord one = client.invoices().get("inv_01HXYZ");
```

## Download results

```java
byte[] pdf = client.invoices().downloadPdf(id);       // ZUGFeRD PDF with embedded XML
byte[] idocXml = client.invoices().downloadIdocXml(id); // SAP IDoc XML
```

## API keys

```java
CreatedApiKey created = client.apiKeys().create(CreateApiKeyRequest.builder()
    .name("Production CI")
    .sandbox(false)
    .build());
System.out.println("Store this now, it's shown only once: " + created.getRawKey());

List<ApiKey> keys = client.apiKeys().list();
client.apiKeys().revoke(created.getId());
```

## Error handling

All SDK exceptions extend `XingenException`. HTTP errors map to typed subtypes of `ApiException`:

| Exception | Status | Notes |
|---|---|---|
| `AuthenticationException` | 401 | Missing or invalid API key |
| `PermissionException` | 403 | Resource exists but isn't owned by the caller |
| `NotFoundException` | 404 | |
| `ValidationRequestException` | 400 | `getFieldErrors()` has details for request-body validation failures |
| `QuotaExceededException` | 429 | Monthly request quota exhausted |
| `ApiException` | other 4xx/5xx | Fallback; `getStatusCode()` / `getRawBody()` always available |

```java
try {
    client.invoices().submit(submission);
} catch (ValidationRequestException e) {
    e.getFieldErrors().forEach((field, message) -> System.out.println(field + ": " + message));
} catch (QuotaExceededException e) {
    System.out.println("Quota exceeded — upgrade or wait for the next billing period");
} catch (XingenException e) {
    System.out.println("Request failed: " + e.getMessage());
}
```

## Design notes

- **No automatic retries.** Retrying a `submit()` after a client-side timeout is unsafe without
  idempotency keys, which the API doesn't support yet — a retried submit could create a duplicate
  invoice. Handle retries at the call site if you need them.
- **Jackson is a normal dependency**, not shaded or relocated. Most JVM backends (Spring Boot,
  Micronaut, Quarkus) already pull it in; declaring it normally lets Gradle/Maven dedupe against
  your existing version instead of fighting a shaded copy.

## Contributing

```bash
./gradlew build test
```

Tests run against a real (loopback) `com.sun.net.httpserver.HttpServer`, not a mocking framework —
no network calls leave the machine, and no external test-server dependency is required.

## Publishing

Releases are built and signed with the [Vanniktech Maven Publish plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/)
and pushed to Maven Central's Central Portal. One-time setup, before this can run:

1. Verify the `de.xingen` namespace at [central.sonatype.com](https://central.sonatype.com) via a
   DNS TXT record on `xingen.de`.
2. Generate a GPG key and export the ASCII-armored private key.
3. Add these repo secrets: `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD` (a Central Portal user
   token, not your account password), `GPG_SIGNING_KEY`, `GPG_SIGNING_KEY_ID`,
   `GPG_SIGNING_KEY_PASSWORD`.

To cut a release: bump `version` in `gradle.properties` (drop the `-SNAPSHOT` suffix), commit, tag
`vX.Y.Z`, and push the tag — `.github/workflows/publish.yml` builds, signs, and stages the release.
Staged releases require a manual "Publish" click on the Central Portal (`automaticRelease = false`
in `build.gradle.kts`); switch that to `true` once the process is trusted, since Central artifacts
can never be deleted once published.

## License

MIT — see [LICENSE](LICENSE).

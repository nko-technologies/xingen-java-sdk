# Changelog

## 0.2.0

- AI-based PDF invoice extraction: `extractInvoice`/`extractInvoiceAndWait` (`POST /v1/invoices/extract`),
  new `ExtractionModelTier` enum (`FAST`/`ACCURATE`) and `InvoiceRecord#getExtractionTier()`.
- `patchInvoice` — correct invoice fields via JSON merge-patch and re-validate synchronously
  (`PATCH /v1/invoices/{id}`).
- `getAutoFilledFields` — list the invoice fields the backend fills in automatically, per
  validation profile (`GET /v1/invoices/auto-filled-fields`), via the new `AutoFilledField` model.

## 0.1.0

- Initial v1 release.
- `XingenClient` with `invoices()` and `apiKeys()` resource clients.
- Invoice submission (JSON, multipart file upload, SAP IDoc, SAP OData passthrough), retrieval,
  pagination, PDF/IDoc-XML downloads.
- `*AndWait` polling helpers with configurable exponential backoff, timeout, and cancellation.
- API key create/list/revoke.
- Typed exception hierarchy for 400/401/403/404/429 and generic HTTP errors.
- Published via JitPack; MIT licensed.

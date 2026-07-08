# Changelog

## Unreleased

- Initial v1 release.
- `XingenClient` with `invoices()` and `apiKeys()` resource clients.
- Invoice submission (JSON, multipart file upload, SAP IDoc, SAP OData passthrough), retrieval,
  pagination, PDF/IDoc-XML downloads.
- `*AndWait` polling helpers with configurable exponential backoff, timeout, and cancellation.
- API key create/list/revoke.
- Typed exception hierarchy for 400/401/403/404/429 and generic HTTP errors.
- Published via JitPack; MIT licensed.

# Changelog

## 0.2.0

- AI-based PDF invoice extraction: `extractInvoice`/`extractInvoiceAndWait` (`POST /v1/invoices/extract`),
  new `ExtractionModelTier` enum (`FAST`/`ACCURATE`) and `InvoiceRecord#getExtractionTier()`.
- `patchInvoice` — correct invoice fields via JSON merge-patch and re-validate synchronously
  (`PATCH /v1/invoices/{id}`).
- `getAutoFilledFields` — list the invoice fields the backend fills in automatically, per
  validation profile (`GET /v1/invoices/auto-filled-fields`), via the new `AutoFilledField` model.
- `InvoiceSubmission.PartyInput` gained an `address` field (new `InvoiceSubmission.AddressInput`
  nested type: `streetName`/`city`/`postalZone`/`countryCode`) — the backend now rejects a party
  with no postal address on every profile, and `submit()` had no way to supply one.
- `submit()` reaches full parity with the backend's domain model, so every invoice type it can
  validate can now also be submitted as structured JSON. New on `InvoiceSubmission`: `dueDate`,
  `taxPointDate`, `taxCurrencyCode`, `paymentTermsNote`, the BT-11..BT-19 reference fields, `notes`,
  `precedingInvoiceReferences`, `supportingDocuments`, `deliveryPeriodStart`/`End`, `invoicePeriod`,
  `delivery`, `payee`, `taxRepresentative`, `paymentMeans`, and `allowanceCharges`. New on
  `PartyInput`: `registrationName`, `taxRegistrationId`, `legalRegistrationId`/`SchemeId`,
  `additionalLegalInfo`, `contact` (new `ContactInput`), `identifiers` (new
  `PartyIdentifierInput`). New on `AddressInput`: `additionalStreetName`, `addressLine3`,
  `countrySubdivision`. New on `LineInput`: `itemName`, `note`, object/order/accounting
  references, seller/buyer/standard item ids, `originCountryCode`, `classifications` (new
  `ItemClassificationInput`), `attributes` (new `ItemAttributeInput`), `grossPrice`,
  `priceDiscount`, `priceBaseQuantity`/`Unit`, `taxCategoryCode`, `exemptionReason`/`Code`, `period`
  (new `InvoicePeriodInput`), and `allowanceCharges` (new `LineAllowanceChargeInput`). New
  top-level `PaymentMeansInput` and `AllowanceChargeInput` types.

## 0.1.0

- Initial v1 release.
- `XingenClient` with `invoices()` and `apiKeys()` resource clients.
- Invoice submission (JSON, multipart file upload, SAP IDoc, SAP OData passthrough), retrieval,
  pagination, PDF/IDoc-XML downloads.
- `*AndWait` polling helpers with configurable exponential backoff, timeout, and cancellation.
- API key create/list/revoke.
- Typed exception hierarchy for 400/401/403/404/429 and generic HTTP errors.
- Published via JitPack; MIT licensed.

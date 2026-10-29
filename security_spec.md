# Security Specification — Netra AI Dialer Firestore Rules

## 1. Data Invariants
1. **Strict Tenant Isolation**: Every document under `/users/{userId}/...` can only be read, queried, created, or modified when `request.auth != null && request.auth.uid == userId`, and the document body's `userId` field equals `request.auth.uid`.
2. **Path Variable Hardening**: Every document ID (`userId`, `callId`, `contactId`, `docId`, `auditId`) on single-document operations must match `^[a-zA-Z0-9_\-]+$` and be `<= 128` characters.
3. **Immutable Audit Trail**: Documents in `/users/{userId}/identity_audit_logs/{auditId}` are strictly append-only (`allow update, delete: if false;`).
4. **Immutable Creation Metadata**: On `update` of `call_logs`, `crm_contacts`, and `business_identity`, `userId` and `createdAt` must remain strictly equal to `resource.data`.
5. **Temporal Integrity**: All `createdAt` and `updatedAt` fields must be valid Firestore `timestamp` objects `<= request.time`.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read/List**: Calling `get` or `list` on `/users/alice_123/call_logs` with `auth == null`.
2. **Cross-Tenant PII/CRM Scrape**: Authenticated user `bob_456` reading `/users/alice_123/crm_contacts/c1`.
3. **Spoofed Owner Payload**: `alice_123` creating `/users/alice_123/call_logs/call_1` with `userId: "bob_456"`.
4. **Ghost Field Shadow Update**: `alice_123` updating `/users/alice_123/business_identity/primary` with an undeclared field `isSuperAdmin: true`.
5. **Invalid Enum Injection**: `alice_123` creating a call log with `verifiedCallerIdStatus: "SPOOFED_VERIFIED"`.
6. **Negative Duration Poisoning**: `alice_123` creating a call log with `durationSeconds: -500`.
7. **Unbounded Array Resource Exhaustion**: `alice_123` submitting `aiActionItems` with 50 items (exceeding max 15).
8. **Oversized String Denial-of-Wallet**: `alice_123` submitting a 10,000-character `aiSummaryOverview` (exceeding max 2,000).
9. **Immutable Timestamp Tampering**: `alice_123` mutating `createdAt` during an update to `crm_contacts`.
10. **Audit Log Tampering**: `alice_123` attempting to `update` or `delete` an existing document in `identity_audit_logs`.
11. **Malformed Document ID Poisoning**: Creating a document with whitespace or special characters in `{callId}`.
12. **Client Clock-Skew Future Timestamp**: Writing `createdAt` set 1 hour in the future (`> request.time`).

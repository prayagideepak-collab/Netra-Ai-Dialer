# Netra AI Dialer (Prayagi Smart Dialer)

Netra AI Dialer is an enterprise-grade, ultra-low-power Android default phone and business communications suite built for **Prayagi Store & Services**. It unifies native Android telephony, non-negotiable **Official Business Identity Lock** (GSTIN/Udyam verified), **Verified Business Caller ID** telemetry, an advanced **Call Log with Verified Caller ID Sorting & Filtering**, automatic **CRM Contact Record Synchronization**, and **Gemini AI Post-Call Summarization** within a strict 24/7 battery budget.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following core product decisions synthesize your initial master specification, the Official Business Identity Lock PDF addendum, and your latest requirements for Verified Call Logs, CRM Integration, and AI Call Summarization.

- **Confirmed Feature 1 — Verified Outgoing Call Log with Multi-Criteria Sort & Filter**: Every outgoing and incoming call record tracks exact date, timestamp, duration, telephony route (Business vs. Personal SIM/VoIP), and **Verified Business Caller ID Status** (`Provider Approved`, `Identity & Number Verified`, `Registration Pending`, `Rejected/Unavailable`, or `Personal Line`). Users can sort by Date/Time, Duration, or Verification Status, and filter by Verified Business Calls Only, Call Direction, Duration thresholds, or CRM Sync state.
- **Confirmed Feature 2 — Automatic CRM Contact Record Integration**: Concluding a call automatically logs the caller ID, verified business status, call duration, timestamp, and AI-generated action items directly into the corresponding **CRM Contact Record** (persisted locally in Room and synced in real time to Firebase Firestore under authenticated Google Sign-In).
- **Confirmed Feature 3 — AI Post-Call Summarization & Action Item Extractor**: Immediately after a call concludes (or via on-demand analysis of call notes/recordings), Gemini generates a structured JSON summary containing a concise executive overview, key discussion points, customer sentiment, and checkable follow-up action items linked to both the Call Log and CRM profile.
- **Confirmed Decision 4 — Default Security, Recording & Power Defaults**:
  - **Official Business Identity Lock**: Supports both **GSTIN** and **Udyam Registration Number** verification with independent applicant authorization checks, immutable legal-name storage separated from editable profile fields, fail-closed Voice AI greeting enforcement, and a tamper-evident audit trail.
  - **Call Recording Manager**: Configured to attempt recording when device/OS/law permit, with transparent hardware compatibility reporting, Android Keystore AES-GCM encryption, and opt-in backup.
  - **Hybrid Spam & Caller ID**: Combines an indexed local offline Room database (zero network latency during incoming call screening) with Gemini Search & Maps Grounding (`gemini-3.8-flash`) for post-screening business verification.
  - **Adaptive 24/7 Power Budget**: Operates across 4 deterministic power tiers (`Normal >30%`, `Saver 15–30%`, `Critical <15%`, `Survival <5%`) with thermal drop and hysteresis so Tier-0 telephony never lags or drains battery.

---

### 1. Overview & Core Concept

- **What It Does**: Functions as a complete Android dialer (`InCallService`, `CallScreeningService`, `RoleManager.ROLE_DIALER`) combined with a verified business communications command center. It protects incoming calls with offline-first spam screening and configurable TTS caller announcements, locks outbound and AI-assistant business identity to verified government records (GSTIN/Udyam), logs every call with verified caller-ID telemetry, automatically updates CRM contact histories, and synthesizes post-call summaries and tasks using Gemini.
- **Target Audience / Persona**: Business owners and operations teams (specifically **Prayagi Store & Services**) who require trustworthy outbound business identity, zero-latency incoming call handling, automated CRM record-keeping, and intelligent post-call follow-ups without background battery drain.
- **Key Value**: Eliminates manual post-call data entry and caller-ID spoofing risks while guaranteeing that core phone functionality remains instantaneous, private, and reliable 24/7 even at 5% battery.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Dialer, T9 Search & Verified Outbound Calling**: User opens the keypad or searches contacts via T9. A prominent **Calling Identity Selector** toggles between *Verified Business Calling* (displaying the locked legal name, verified outbound number, and truthful provider delivery status badge) and *Personal Calling* (when supported by the telephony route).
  2. **Call Log with Verified Caller ID Sort & Filter**: In the **Call Logs** workspace, each row displays the contact/number, date, start time, formatted duration, call direction, a **Verified Business Caller ID Badge** (with detailed delivery telemetry sheet on tap), CRM sync pill, and an expandable **AI Call Summary** card. A persistent filter/sort bar lets the user sort by *Date/Time (Newest/Oldest)*, *Duration (Longest/Shortest)*, or *Verified Status*, and filter by *Verified Business Caller ID*, *Personal Calls*, *Missed/Rejected*, or *Has AI Summary*.
  3. **Post-Call AI Summarization & Automatic CRM Logging**: When a call ends (either via live telephony callback or the interactive Call Simulator / Post-Call Workbench for testing in browser emulators), the app automatically:
     - Computes call duration and records the exact Verified Business Caller ID status used during the call.
     - Invokes Gemini (`gemini-3.8-flash` structured output) to generate a **Call Summary**, **Discussion Points**, and **Action Items**.
     - Pushes the call record—including Caller ID, Verified Status, Duration, and AI Summary—into the matching **CRM Contact Record** (creating a new CRM lead if unknown) and marks the CRM sync badge as `Synced`.
  4. **CRM Workspace & Contact Timeline**: Users browse and search CRM contacts, view lifetime call duration, verified outbound call ratio, deal stage, bioscience/technical notes, and an chronological timeline of all logged calls with their AI summaries and interactive follow-up checklists.
  5. **Official Business Identity Lock & Caller ID Center**: A dedicated security workflow where the owner submits a GSTIN or Udyam Registration Number, completes authoritative record verification and applicant authorization, and locks the official legal name (`Prayagi Store & Services`). Editable profile fields (FAQs, business hours, description) are visually and architecturally isolated from the locked legal identity, accompanied by an immutable **Identity Audit Trail**.
  6. **AI Assistant Hub (Multi-Turn Chat, High-Thinking Analysis, Vision & Banner Studio)**:
     - **Voice AI Business Assistant & Bioscience Advisor**: Multi-turn chat (`gemini-3.8-flash` for standard fast responses, `gemini-3.1-flash-lite` for low-latency quick replies, and `gemini-4-argon` with `ThinkingLevel.HIGH` for complex regulatory, technical, or bioscience inquiries) using the locked legal business greeting.
     - **Document & Image Scanner**: Upload or capture registration documents, business cards, or product/scientific labels for multimodal analysis via `gemini-4-argon`.
     - **Branded Caller Card Studio**: Generate verified business caller banners and avatars using `gemini-nano-banana-2.1` / `gemini-3-pro-image` with selectable aspect ratios (`1:1`, `2:3`, `3:2`, `3:4`, `4:3`, `9:16`, `16:9`, `21:9`).
  7. **Spam Shield, Call Recorder, Drive Backup & 24/7 Power Diagnostics**: Dedicated management screens for offline/hybrid spam rules, encrypted call recordings, Google Drive OAuth backup/restore, and a real-time **Power Tier & Self-Diagnostics Console** reporting battery tier, wakeups/hour, default dialer role, and circuit-breaker health.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: **Precision Telecom Obsidian & Emerald Authority** — an ultra-clean, high-contrast executive interface engineered for instant legibility during calls and zero-overdraw OLED power efficiency.
  - *Color Palette & Mood*:
    - `--background`: Deep Obsidian (`#0B0F17` dark / `#F8FAFC` light)
    - `--surface`: Elevated Slate (`#131B2E` dark / `#FFFFFF` light) with subtle tonal elevation
    - `--primary`: Netra Royal Indigo (`#4F46E5` / `#818CF8`) for primary dialer actions
    - `--accent-verified`: Authority Emerald (`#10B981`) exclusively reserved for **Verified Business Identity & Approved Caller ID** badges
    - `--warning`: Amber Alert (`#F59E0B`) for pending verification or Saver power tier
    - `--danger`: Crimson Shield (`#EF4444`) for verified spam warnings and suspended identity alerts
  - *Typography & Hierarchy*: Locally bundled Google Fonts via `font-util`: **Space Grotesk** for crisp, high-legibility phone numbers, call timers, and headers; **Plus Jakarta Sans** for clean CRM records and AI summaries; **JetBrains Mono** for GSTIN/Udyam identifiers, audit hashes, durations, and diagnostic telemetry.
  - *Component Styling & Layout*: Material Design 3 adaptive layout supporting `Compact`, `Medium`, and `Expanded` window size classes (switching to a two-pane List-Detail view for Call Logs + CRM/AI Summary on tablets and foldables). All interactive touch targets enforce a minimum `48.dp` size with semantic `Modifier.testTag(...)` identifiers.

- **Interactive Feedback & Motion**:
  - Instantaneous zero-animation rendering in `Critical` and `Survival` battery tiers (`<15%` battery) to strictly honor the power budget.
  - Smooth spring transitions (`<250ms`) in `Normal` power tier when expanding AI Call Summaries, filtering call logs, or transitioning between Dialpad and CRM contact timelines.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual-Engine Persistence (Local Room + Authenticated Firestore Cloud Sync)**
  - *Chosen Approach*: Store all call logs, offline spam entries, identity lock state, audit logs, and CRM records locally in **Room** for instant `<5ms` zero-network access during incoming/outgoing calls, while synchronizing CRM contact records, call summaries, and verified business audit trails to **Firebase Firestore** (authenticated via Google Sign-In with Jetpack `CredentialManager`).
  - *Why*: Incoming call screening and dialer launch must never wait on network I/O or cloud authentication, while CRM records and post-call AI summaries require durable multi-device cloud persistence.
  - *Alternatives Considered*: Cloud-only storage (rejected because it violates offline incoming-call reliability and battery rules) or local-only storage (rejected because CRM integration and cloud backup require durable cloud sync).

- **Decision 2: Architectural Separation of Locked Business Identity vs. Editable Profile**
  - *Chosen Approach*: Persist `VerifiedBusinessIdentity` (legal name, GSTIN/Udyam number, verification source, applicant authority status, cryptographic lock state) in a dedicated read-only repository table governed by strict state-machine transitions and an append-only `IdentityAuditLog`. Ordinary profile edits only mutate `EditableBusinessProfile` (description, hours, FAQs).
  - *Why*: Directly enforces the PDF specification's non-negotiable rule that user-entered text or screenshots can never override the authoritative government-verified legal name used in Voice AI greetings and outbound Business Caller ID.
  - *Alternatives Considered*: Storing business name as a single string in shared preferences (rejected as insecure and prone to accidental overwrite).

- **Decision 3: Event-Driven Post-Call CRM & AI Pipeline with Adaptive Power Gating**
  - *Chosen Approach*: Trigger CRM contact logging immediately on `CALL_ENDED` locally, and run Gemini AI Call Summarization (`gemini-3.8-flash`) asynchronously with structured JSON output. If the device is in `Critical (<15%)` or `Survival (<5%)` power tier or offline, queue the AI summarization job cleanly for the next `Normal` power window via WorkManager with exponential backoff.
  - *Why*: Ensures 100% CRM call logging reliability while respecting the strict 24/7 battery budget.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                           NETRA AI DIALER — PRESENTATION LAYER                       │
│  ┌────────────────────┐  ┌────────────────────────┐  ┌────────────────────────────┐  │
│  │  Dialer & Call Log │  │ CRM & AI Summary Hub   │  │ Identity Lock & Shield Hub │  │
│  │  • T9 Keypad       │  │ • CRM Contact List/Det │  │ • GSTIN/Udyam Verifier     │  │
│  │  • Verified Badge  │  │ • Auto Call Log Sync   │  │ • Outbound Caller ID Setup │  │
│  │  • Sort & Filter   │  │ • AI Summary & Tasks   │  │ • Audit Trail & Spam Rules │  │
│  │  • Call Simulator  │  │ • Gemini Multi-Model   │  │ • Drive Backup & Power Hub │  │
│  └─────────┬──────────┘  └───────────┬────────────┘  └─────────────┬──────────────┘  │
└────────────┼─────────────────────────┼─────────────────────────────┼─────────────────┘
             │                         │                             │
             ▼                         ▼                             ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                        DOMAIN & ADAPTIVE POWER GATEWAY LAYER                         │
│  ┌──────────────────────────┐ ┌─────────────────────────┐ ┌───────────────────────┐  │
│  │ Telephony & Caller ID    │ │ Post-Call CRM & AI      │ │ Official Identity     │  │
│  │ Engine                   │ │ Summarization Pipeline  │ │ Lock State Machine    │  │
│  │ • NetraInCallService     │ │ • Auto CRM Logger       │ │ • Authoritative Check │  │
│  │ • NetraScreeningService  │ │ • Structured Summary    │ │ • Applicant Auth Lock │  │
│  │ • TTS Caller Announcer   │ │ • Action Item Extractor │ │ • Fail-Closed Guard   │  │
│  └────────────┬─────────────┘ └────────────┬────────────┘ └───────────┬───────────┘  │
│               └────────────────────────────┼──────────────────────────┘              │
│                                            ▼                                         │
│  ┌────────────────────────────────────────────────────────────────────────────────┐  │
│  │ Adaptive Power Tier Controller (Normal >30% | Saver | Critical | Survival <5%) │  │
│  └─────────────────────────────────────────┬──────────────────────────────────────┘  │
└────────────────────────────────────────────┼─────────────────────────────────────────┘
                                             │
             ┌───────────────────────────────┴───────────────────────────────┐
             ▼                                                               ▼
┌─────────────────────────────────────────┐     ┌──────────────────────────────────────┐
│     LOCAL STORAGE LAYER (ROOM + KS)     │     │   CLOUD & AI LAYER (FIREBASE + GCP)  │
│  • CallLogEntity (Date, Time, Duration, │     │  • Firebase Auth (Google Sign-In)    │
│    VerifiedCallerIdStatus, SummaryJson) │     │  • Firestore (/users/{uid}/crm_*,    │
│  • CrmContactEntity & CrmInteraction    │     │    call_logs, identity_audit)        │
│  • LockedIdentityEntity & AuditLogEntity│     │  • Firebase AI Logic:                │
│  • SpamRuleEntity & RecordingMetadata   │     │    - gemini-3.8-flash (Summary/Maps) │
│  • Android Keystore AES-GCM Encryption  │     │    - gemini-4-argon (High Thinking)  │
│                                         │     │    - gemini-3.1-flash-lite (Fast)    │
│                                         │     │    - gemini-nano-banana-2.1 (Image)  │
└─────────────────────────────────────────┘     └──────────────────────────────────────┘
```

- **Data Model & State**:
  - `CallLogEntry`: `id`, `phoneNumber`, `contactName`, `direction` (`OUTGOING`, `INCOMING`, `MISSED`, `REJECTED`), `timestampMillis`, `durationSeconds`, `callingMode` (`BUSINESS_VERIFIED`, `PERSONAL`), `verifiedCallerIdStatus` (`PROVIDER_APPROVED`, `IDENTITY_AND_NUMBER_VERIFIED`, `REGISTRATION_PENDING`, `PROVIDER_REJECTED`, `PERSONAL_UNBRANDED`), `verifiedLegalNameUsed`, `crmSyncStatus` (`SYNCED`, `PENDING`, `FAILED`), `crmContactId`, `aiSummaryOverview`, `aiDiscussionPoints` (list), `aiActionItems` (list of checkable tasks), `isSpam`, `recordingId`.
  - `CrmContactRecord`: `id`, `fullName`, `companyName`, `phoneNumber`, `email`, `stage` (`LEAD`, `ACTIVE_CLIENT`, `VIP_PARTNER`, `SUPPORT`), `totalCalls`, `totalDurationSeconds`, `verifiedOutboundCallsCount`, `lastInteractionTimestamp`, `latestAiSummary`, `openActionItems`, `notes`, `updatedAt`.
  - `VerifiedBusinessIdentity`: `registrationType` (`GSTIN`, `UDYAM`), `registrationNumberMasked`, `verifiedLegalName`, `verifiedTradeName`, `verificationSource`, `applicantAuthorized` (boolean), `lockStatus` (`LOCKED_ACTIVE`, `REVERIFICATION_PENDING`, `SUSPENDED_INVALID`, `UNVERIFIED`), `outboundNumber`, `outboundNumberVerified` (boolean), `callerIdProviderStatus` (`PROVIDER_APPROVED`, `PENDING`, `REJECTED`, `UNAVAILABLE`), `lastVerifiedAt`.
  - `IdentityAuditEvent`: `id`, `timestampMillis`, `eventType`, `source`, `matchedLegalName`, `previousStatus`, `newStatus`, `failureReason`.

- **Interactive Component & State Mapping**:
  - **Call Log Sort & Filter Bar**: Mutates `CallLogFilterState` (`sortBy`: `DATE_DESC`, `DATE_ASC`, `DURATION_DESC`, `DURATION_ASC`, `VERIFIED_FIRST`; `filterVerified`: `ALL`, `VERIFIED_BUSINESS_ONLY`, `PERSONAL_ONLY`; `filterDirection`: `ALL`, `OUTGOING`, `INCOMING`, `MISSED`; `searchQuery`). Reactive Room/Firestore `Flow` immediately recomposes the list and displays aggregate statistics (Total Outgoing Verified Calls, Average Duration, CRM Sync Rate).
  - **Call Lifecycle & Simulator Controls**: Allows placing real system calls (`TelecomManager.placeCall`) or running a full end-to-end **Simulated Outgoing/Incoming Business Call** (with live timer, recording status check, TTS announcement, and call termination) that immediately triggers the **Automatic CRM Contact Logger** and **Gemini Post-Call Summarizer**.
  - **Official Identity Lock Workflow**: Validates GSTIN (`15-char checksum + authoritative record verification`) or Udyam (`UDYAM-XX-00-0000000`) plus applicant representation authority, transitions `lockStatus` to `LOCKED_ACTIVE`, records an `IdentityAuditEvent`, and unlocks Verified Business Calling and branded Voice AI greetings. Attempting to submit another business's identifier or an invalid status immediately blocks activation and logs the rejection reason.

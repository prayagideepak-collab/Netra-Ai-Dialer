# ULTRA-LOW POWER & RELIABLE 24/7 OPERATION
## Permanent Repository Engineering Standard — Version 2.0

**Effective date:** 19 November 2026  
**Scope:** This repository and its application, modules, services, workers, libraries, and release process.

This document is a mandatory engineering policy. Apply it to all future audits, plans, implementation, code reviews, tests, and releases in this repository. It supplements the application's own requirements; it does not authorize unrelated rewrites or removal of valid features.

## 1. Priority order
Resolve conflicts in this order:
1. Device safety, user privacy/security, Android platform rules, and Google Play policy.
2. Minimum practical battery and energy consumption.
3. Reliability of explicitly approved essential (Tier-0) functions.
4. Durable state preservation and compliant recovery.
5. Thermal stability, memory efficiency, and resource control.
6. Performance and responsiveness.
7. Advanced, convenience, and decorative features.

A lower-priority requirement must never violate a higher-priority one.

## 2. Mandatory first step: inspect and plan
Before changing code, inspect the repository, current branch/commit, working-tree state where available, app modules, manifest, permissions, target/min SDK, services, workers, receivers, alarms, wake locks, sensors, location, network calls, database writes, threads, third-party SDKs, tests, and CI configuration.

Deliver a repository-specific **Power & Reliability Engineering Plan** first. Include confirmed defects separately from suspected risks. Define the app's actual purpose, Tier-0 functions (maximum five), tier matrix, measurable budgets, Android compliance, recovery design, test matrix, implementation sequence, risks, and acceptance criteria.

Mark unknown information as OPEN and unmeasured results as NOT VERIFIED. Do not invent values or test outcomes. Wait for explicit user approval before implementation or architectural changes.

## 3. Two independent classifications
Keep feature importance separate from runtime power mode.

**Feature tiers**
- **Tier-0:** essential functions that preserve the app's fundamental purpose. Maximum five entries; justify why each needs background execution, if any.
- **Tier-1:** important but deferrable functions.
- **Tier-2:** secondary functions, optional sync/monitoring.
- **Tier-3:** cosmetic or optional processing.

Anything not expressly justified as Tier-0 is non-essential and must be throttled or suspended in restrictive modes. Tier-0 status does not authorize infinite loops, permanent wake locks, or unrestricted foreground services.

**Runtime modes**
- **Normal:** full permitted features, event-driven and batched.
- **Saver:** entered at battery 15–30% inclusive, or Battery Saver ON; reduce frequency and defer non-essential work.
- **Critical:** battery above 5% and below 15%; Tier-0 minimal execution only.
- **Survival:** below 5%; minimum permitted Tier-0 execution, no optional sensors, animations, or routine network activity.

At exactly 5%, use Critical. Apply the most restrictive active condition. Charging never overrides critical battery, thermal, Doze, permission, or policy restrictions. Use separate entry/exit thresholds, hysteresis, appropriate dwell times, immediate restrictive transitions, and gradual restoration. Initial exit margins (Saver 33%, Critical 18%, Survival 8%) are configurable starting points, not universal guarantees.

## 4. Centralized power controller
Implement or specify one authoritative PowerTierManager/equivalent. Components must consume its state rather than invent independent power decisions.

Use Android-supported callbacks and events for battery, charging, Battery Saver, thermal status/headroom, foreground/background lifecycle, screen state, network/metering/Data Saver, permissions, and other relevant conditions. Determine Doze and App Standby state only through supported mechanisms; do not poll continuously or assume every internal state is directly observable.

Unavailable data must be UNKNOWN/UNAVAILABLE, never fabricated. Persist enough configuration for restoration, but recalculate current device conditions after process recreation.

## 5. Per-app measurable power budget
Set a separate budget for this app from a measured baseline. No universal numeric target may be guessed.

For every tier, define:
- Maximum attributable wakeups/hour.
- Maximum network requests/hour and bytes/day.
- Background CPU time.
- Wake-lock count and cumulative duration.
- Sensor/location active duration and sampling rate.
- Worker/job execution count and runtime.
- Memory and storage/logging bounds.
- Screen-off idle drain target (% points/24 h) and measured/estimated mAh where defensible.

Unknown limits remain OPEN — NOT VERIFIED, not unlimited. Establish targets through controlled baseline and app-enabled A/B tests with repeat runs and documented uncertainty.

The aspirational target of about one battery percentage point over three hours at 5% battery with Tier-0 only is experimental, not guaranteed and not a universal release gate. Record battery gauge resolution, device/battery condition, thermal state, external activity, and measurement method.

Never describe total device energy or ODPM rail data as exact application-only consumption unless attribution is defensible. Separate battery percentage, estimated mAh, measured energy, and app-attributable statistics.

## 6. Measurement tools
Prefer supported current tools: Android Studio Power Profiler, Perfetto/System Trace, Macrobenchmark power metrics where supported, Batterystats, Android Vitals, and relevant ADB diagnostics. Battery Historian may be used as supplementary/legacy analysis, not the sole or assumed actively maintained authority.

Useful diagnostics include dumpsys batterystats, dumpsys power, dumpsys deviceidle, dumpsys jobscheduler, and dumpsys alarm, subject to Android version and availability. Record exact commands, devices, build, conditions, trace files, and interpretation. A command running successfully is not proof of a passing test.

## 7. Event-driven execution
Prefer Android callbacks/events and lifecycle-aware listeners; use FCM only for genuine policy-compliant remote events; WorkManager/JobScheduler for deferrable persistent work; inexact alarms where suitable. Exact alarms and foreground services require a documented essential use case and current policy eligibility.

No busy loops, infinite loops, artificial heartbeats, unnecessary polling, self-restarting services, repeated short alarms, or artificial process persistence. WorkManager is not an exact-time scheduler or a 24/7 watchdog. Make work unique, idempotent, constrained, cancellable, and bounded.

## 8. CPU, wake locks, sensors, location, UI
Avoid wake locks. If genuinely needed, document why, set a strict timeout, release reliably in finally/equivalent cleanup, handle cancellation, and measure duration. A foreground service does not automatically keep the CPU awake; do not add a manual wake lock without independent evidence.

Activate sensors/location only while the actual feature needs them. Prefer batching, significant-change/passive APIs, and lowest adequate sampling frequency. Stop requests/listeners promptly; honor lifecycle and permission changes. Never use continuous GPS or sensors to keep the process alive.

Pause screen-only work when UI is not visible. For Compose, avoid work in unmanaged recomposition, duplicate state emissions, repeated allocations, unnecessary animations, overdraw, and UI-owned background lifetimes. Bound caches, queues, memory, and logs.

## 9. Network and storage
Batch network/database operations; cache; use delta sync; avoid duplicates; cancel obsolete requests; compress only where total energy/data benefit is plausible; respect Data Saver and metered constraints. Use bounded exponential backoff with jitter and classify permanent versus transient errors. No retry loops.

Use transactions, atomic persistence, schema-versioned state, safe migrations, and bounded storage. Avoid frequent writes for sensor events. Routine telemetry/log upload should be deferred until charging on unmetered network with suitable thermal conditions, subject to user consent, privacy, and a genuine need.

## 10. Reliability and recovery within Android limits
Handle lifecycle recreation, process death, crashes, screen/network transitions, Doze, standby, Battery Saver, thermal restrictions, permission changes, reboot, app updates, and storage constraints without losing essential state.

Use DataStore, Room, or suitable durable storage. Recovery may use WorkManager, JobScheduler, and system-permitted callbacks. Do not rely on START_STICKY alone.

Workers must be idempotent and uniquely scheduled to avoid duplicates. Reboot receivers and Direct Boot are allowed only where genuinely needed and permitted; do not access credential-protected data before unlock or start prohibited service types from boot.

Respect user Force Stop. Do not try to defeat it with alarms, broadcasts, companion apps, or external services. Do not claim uninterrupted 24/7 execution: Android, OEM restrictions, power-off, Force Stop, permissions, and system resource management can prevent execution.

## 11. Self-healing without drain
Detect failure only at natural wake points or valid scheduled execution. Classify temporary, permanent, permission, network, thermal, resource, configuration, and platform-restriction failures.

Use exponential backoff, jitter, hard retry caps, and a closed/open/half-open circuit breaker. Recovery work counts against the power budget. Do not restart a component if the cause cannot be fixed by restart. Tier-2/3 failure must not destabilize Tier-0. No independent continuously running watchdog.

## 12. Thermal protection
Thermal safety overrides performance and convenience. Use Android thermal status/headroom APIs where available. Reduce optional CPU, sensors, location, sync, and rendering as thermal conditions worsen; enter minimal safe behavior at severe/critical states. Restore gradually. Never infer physical temperature from thermal status or invent missing readings.

## 13. Android/Google Play compliance
Before each release, recheck current official Android and Play documentation for the app's target SDK and supported OS versions. Verify background execution, Doze, standby/job quotas, alarms, FGS types/permissions/start restrictions/timeouts, boot behavior, runtime permissions, privacy, and battery optimization policy.

For Android 14+, verify declared FGS types and matching permissions/prerequisites. For Android 15+, handle applicable FGS time limits and Service.onTimeout(), including restrictions on starting certain FGS types from boot. For Android 16+, account for JobScheduler quotas and jobs associated with foreground services. Do not assume older patterns remain valid.

Default to inexact alarms. Exact alarms require a genuinely eligible core use case, permission-state checks, graceful denial/revocation handling, and policy validation. Request battery-optimization exemption only where the actual use case qualifies; never force users to disable protections.

## 14. Third-party SDK audit and privacy
Inspect SDKs for automatic initialization, hidden wake locks, jobs, alarms, FGS, polling, network traffic, logging, memory retention, and data collection. Configure or remove unnecessary activity only after checking functional impact. No new dependency without demonstrated benefit.

Use least privilege. Do not log secrets, tokens, or unnecessary personal/location/sensor data. Respect permission revocation, consent, retention, and applicable privacy policy.

## 15. Feature gate — no exceptions
Before merge, each new feature must provide a Power Impact Assessment covering purpose, tier, eligible runtime modes, background need, APIs/permissions/FGS types, CPU/memory, wakeups, network requests/bytes, sensors/location, thermal impact, retry/recovery behavior, measured battery impact, OS/Play eligibility, and rollback plan.

Provide baseline and post-change evidence on comparable hardware. Estimates must be labeled estimates. Without the assessment and required evidence, do not merge.

## 16. Controlled implementation and source-control discipline
Inspect branch, commit, working tree, build configuration, and tests before changes. Preserve valid existing functionality; do not perform unrelated rewrites or alter package identity, signing, permissions, release configuration, branches, commits, PRs, or releases without authorization.

After explicit approval, implement one module at a time. For each change report files, rationale, expected benefit, risk, tests, actual results, and measurement comparison. Do not commit, push, merge, publish, or release without explicit authorization.

## 17. Verification matrix — release gate
Test applicable combinations of:
- Battery 100%, 30%, 15%, 5%; charging/discharging.
- Screen on/off; Battery Saver on/off.
- Normal, moderate, severe thermal conditions.
- Doze and relevant standby buckets.
- Metered, unmetered, poor, and unavailable network.
- Process death/crash, Force Stop, reboot, app update.
- Permission revocation, state restoration, retry/circuit breaker.
- At least 8-hour screen-off operation and 24-hour standby where practical.
- Supported Android versions from the app's actual minSdk through latest stable; include Android 10 only if supported/required. Test previews separately.
- Low-RAM and different OEM devices where feasible.

Measure per tier: battery change, defensible mAh/energy, CPU time, wakeups, wake-lock duration, network calls/bytes, sensor/location runtime, memory, workers/jobs, crash/ANR, and thermal behavior. Record test ID, device, OS, build, starting battery, mode, screen/network/thermal conditions, duration, method, expected/actual result, and PASS/FAIL/NOT RUN.

Block release for Tier-0 acceptance failure, unexplained power regression, unnecessary/unbounded wake locks, runaway retries, lost state, thermal-safety failure, policy violation, excessive background work, or missing mandatory evidence.

## 18. Honest reporting
Use only:
- VERIFIED — passed with reproducible evidence.
- PARTIALLY VERIFIED — some tests passed; others remain.
- NOT VERIFIED — evidence unavailable.
- FAILED — acceptance criteria not met.
- BLOCKED — infrastructure/access prevented testing.
- NOT APPLICABLE — genuinely irrelevant.

Never report an unexecuted test as passed. Never claim battery savings without a valid comparison. Clearly distinguish confirmed defects from suspected risks, static analysis from runtime tests, and device-level from app-attributable measurements.

## 19. Required engineering report
Produce:
1. App profile and source audit.
2. Tier-0 list (max five) and justification.
3. Existing architecture and execution map.
4. Confirmed defects and suspected risks.
5. Per-tier power budget, marking OPEN values.
6. Power modes and component behavior matrix.
7. Controller, scheduler, thermal, recovery, persistence, and diagnostic design.
8. Android/Play compliance assessment with current source references.
9. Verification matrix and repeatable methods.
10. Module-wise implementation plan.
11. Before/after metrics and uncertainty.
12. Remaining limitations and release recommendation.

Allowed release recommendations: READY FOR RELEASE, READY WITH DOCUMENTED LIMITATIONS, NOT READY — POWER REGRESSION, NOT READY — RELIABILITY FAILURE, NOT READY — POLICY VIOLATION, or NOT VERIFIED — INSUFFICIENT EVIDENCE.

## 20. APP-SPECIFIC PROFILE
Fill from repository inspection; unknown fields remain OPEN:
- App name/package:
- Purpose:
- minSdk/targetSdk/compileSdk:
- Kotlin/Java and UI framework:
- Tier-0 functions (max five):
- Sensors/location/hardware:
- Network endpoints, FCM, sync, offline needs:
- FGS, alarms, permissions, exemptions:
- Distribution (Play/direct APK):
- Baseline idle drain target:
- Per-tier wakeup/network/CPU budgets:
- Available test devices and measurement tooling:

## FINAL PRINCIPLE
Build an application that knows what must work, when it may work, why it needs resources, how much energy it is allowed to consume, when it must throttle or stop, how to preserve state, and when recovery must stop.

MINIMUM PRACTICAL ENERGY. MAXIMUM ESSENTIAL RELIABILITY. INTELLIGENT RESOURCE MANAGEMENT. MEASURABLE ENGINEERING. ZERO UNNECESSARY BACKGROUND EXECUTION.

This standard is mandatory for future work in this repository. Reassess it when Android, Google Play, target SDK requirements, hardware, or application purpose changes.

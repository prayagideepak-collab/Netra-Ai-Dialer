package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class CallLogRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val phoneNumber: String = "",
    val contactName: String = "",
    val direction: String = "OUTGOING", // OUTGOING, INCOMING, MISSED, REJECTED
    val timestampMillis: Long = 0L,
    val durationSeconds: Long = 0L,
    val callingMode: String = "BUSINESS_VERIFIED", // BUSINESS_VERIFIED, PERSONAL
    val verifiedCallerIdStatus: String = "PROVIDER_APPROVED", // PROVIDER_APPROVED, IDENTITY_AND_NUMBER_VERIFIED, REGISTRATION_PENDING, PROVIDER_REJECTED, PERSONAL_UNBRANDED
    val verifiedLegalNameUsed: String = "",
    val crmSyncStatus: String = "SYNCED", // SYNCED, PENDING, FAILED
    val crmContactId: String = "",
    val aiSummaryOverview: String = "",
    val aiDiscussionPoints: List<String> = emptyList(),
    val aiActionItems: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

val CallLogRecord.isVerifiedBusinessCall: Boolean
    get() = callingMode == "BUSINESS_VERIFIED" &&
        (verifiedCallerIdStatus == "PROVIDER_APPROVED" || verifiedCallerIdStatus == "IDENTITY_AND_NUMBER_VERIFIED")

@IgnoreExtraProperties
data class CrmContactRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val companyName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val stage: String = "LEAD", // LEAD, ACTIVE_CLIENT, VIP_PARTNER, SUPPORT
    val totalCalls: Long = 0L,
    val totalDurationSeconds: Long = 0L,
    val verifiedOutboundCallsCount: Long = 0L,
    val lastInteractionTimestamp: Long = 0L,
    val latestAiSummary: String = "",
    val openActionItems: List<String> = emptyList(),
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class VerifiedBusinessIdentity(
    @DocumentId val id: String = "",
    val userId: String = "",
    val registrationType: String = "GSTIN", // GSTIN, UDYAM
    val registrationNumberMasked: String = "",
    val verifiedLegalName: String = "",
    val verifiedTradeName: String = "",
    val verificationSource: String = "",
    val applicantAuthorized: Boolean = false,
    val lockStatus: String = "UNVERIFIED", // LOCKED_ACTIVE, REVERIFICATION_PENDING, SUSPENDED_INVALID, UNVERIFIED
    val outboundNumber: String = "",
    val outboundNumberVerified: Boolean = false,
    val callerIdProviderStatus: String = "PENDING", // PROVIDER_APPROVED, PENDING, REJECTED, UNAVAILABLE
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

val VerifiedBusinessIdentity.canActivateBrandedAnswering: Boolean
    get() = lockStatus == "LOCKED_ACTIVE" && applicantAuthorized && verifiedLegalName.isNotBlank()

val VerifiedBusinessIdentity.canPlaceVerifiedBusinessCall: Boolean
    get() = lockStatus == "LOCKED_ACTIVE" &&
        applicantAuthorized &&
        outboundNumberVerified &&
        callerIdProviderStatus == "PROVIDER_APPROVED"

@IgnoreExtraProperties
data class IdentityAuditEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventType: String = "",
    val source: String = "",
    val matchedLegalName: String = "",
    val previousStatus: String = "",
    val newStatus: String = "",
    val failureReason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class CallLogSortOption(val label: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    DURATION_DESC("Longest Duration"),
    DURATION_ASC("Shortest Duration"),
    VERIFIED_FIRST("Verified Business ID First")
}

enum class CallLogVerifiedFilter(val label: String) {
    ALL("All Calls"),
    VERIFIED_BUSINESS_ONLY("Verified Business ID"),
    PERSONAL_ONLY("Personal Calls"),
    PENDING_OR_FALLBACK("Pending / Unverified")
}

enum class CallLogDirectionFilter(val label: String) {
    ALL("All Types"),
    OUTGOING("Outgoing"),
    INCOMING("Incoming"),
    MISSED_OR_REJECTED("Missed / Rejected")
}

enum class PowerTier(
    val title: String,
    val badgeText: String,
    val maxWakeupsPerHour: Int,
    val maxNetworkCallsPerHour: Int,
    val description: String
) {
    NORMAL(
        title = "Normal Tier (>30% / Charging)",
        badgeText = "TIER: NORMAL",
        maxWakeupsPerHour = 12,
        maxNetworkCallsPerHour = 20,
        description = "Full AI post-call summarization, instant CRM sync, TTS announcements, and scheduled spam sync active."
    ),
    SAVER(
        title = "Saver Tier (15–30% / Battery Saver)",
        badgeText = "TIER: SAVER",
        maxWakeupsPerHour = 4,
        maxNetworkCallsPerHour = 6,
        description = "Batched CRM sync, deferred Drive backup, reduced UI animations, local spam screening active."
    ),
    CRITICAL(
        title = "Critical Tier (<15% Battery)",
        badgeText = "TIER: CRITICAL",
        maxWakeupsPerHour = 2,
        maxNetworkCallsPerHour = 2,
        description = "Tier-0 essential telephony and local call logging only. AI summarization queued for next charging window."
    ),
    SURVIVAL(
        title = "Survival Tier (<5% Battery)",
        badgeText = "TIER: SURVIVAL",
        maxWakeupsPerHour = 0,
        maxNetworkCallsPerHour = 0,
        description = "Minimal Tier-0 dialer and emergency call routing only. Zero background network, sensors, or animations."
    )
}

data class AiCallSummaryResult(
    val overview: String,
    val discussionPoints: List<String>,
    val actionItems: List<String>,
    val sentiment: String
)

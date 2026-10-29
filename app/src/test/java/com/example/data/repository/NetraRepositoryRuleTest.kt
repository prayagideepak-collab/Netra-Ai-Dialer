package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.CallLogRecord
import com.example.model.CrmContactRecord
import com.example.model.IdentityAuditEvent
import com.example.model.VerifiedBusinessIdentity
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class NetraRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveCallLog_withVerifiedCallerId_succeedsForOwner() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = NetraFirestoreRepository(firestore, auth)
        val uniqueCallId = "call_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val record = CallLogRecord(
            id = uniqueCallId,
            userId = uid,
            phoneNumber = "+919876543210",
            contactName = "Dr. Aarav Mehta",
            direction = "OUTGOING",
            timestampMillis = 1761700000000L,
            durationSeconds = 310L,
            callingMode = "BUSINESS_VERIFIED",
            verifiedCallerIdStatus = "PROVIDER_APPROVED",
            verifiedLegalNameUsed = "Prayagi Store & Services",
            crmSyncStatus = "SYNCED",
            crmContactId = "crm_101",
            aiSummaryOverview = "Confirmed Q4 delivery schedule and bioscience reagent order.",
            aiDiscussionPoints = listOf("Cold-chain packaging verified", "Invoice terms approved"),
            aiActionItems = listOf("Dispatch batch #402", "Email GST receipt")
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { repo.createCallLog(record) }
        assertTrue("Expected call log creation to succeed: ${result.exceptionOrNull()}", result.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCallLogs().first { list -> list.any { it.id == uniqueCallId } }
        }
        val fetched = emitted.first { it.id == uniqueCallId }
        assertEquals("PROVIDER_APPROVED", fetched.verifiedCallerIdStatus)
        assertEquals(310L, fetched.durationSeconds)
    }

    @Test
    fun createAndObserveCrmContact_andLockIdentity_succeedsForOwner() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = NetraFirestoreRepository(firestore, auth)
        val uniqueCrmId = "crm_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val crm = CrmContactRecord(
            id = uniqueCrmId,
            userId = uid,
            fullName = "Dr. Aarav Mehta",
            companyName = "Apex BioLabs Pvt Ltd",
            phoneNumber = "+919876543210",
            email = "aarav@apexbiolabs.in",
            stage = "VIP_PARTNER",
            totalCalls = 3L,
            totalDurationSeconds = 640L,
            verifiedOutboundCallsCount = 3L,
            lastInteractionTimestamp = 1761700000000L,
            latestAiSummary = "Confirmed reagent supply schedule.",
            openActionItems = listOf("Send GST invoice"),
            notes = "Key institutional client"
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repo.createCrmContact(crm) }
        assertTrue(createResult.isSuccess)

        val identity = VerifiedBusinessIdentity(
            id = "primary_identity",
            userId = uid,
            registrationType = "GSTIN",
            registrationNumberMasked = "27AABCP****1Z5",
            verifiedLegalName = "Prayagi Store & Services",
            verifiedTradeName = "Prayagi Smart Services",
            verificationSource = "GSTN Official Registry",
            applicantAuthorized = true,
            lockStatus = "LOCKED_ACTIVE",
            outboundNumber = "+919820011223",
            outboundNumberVerified = true,
            callerIdProviderStatus = "PROVIDER_APPROVED"
        )
        val idResult = withTimeout(DEFAULT_TIMEOUT_MS) { repo.saveBusinessIdentity(identity, null) }
        assertTrue(idResult.isSuccess)

        val auditResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.appendAuditEvent(
                IdentityAuditEvent(
                    userId = uid,
                    eventType = "IDENTITY_LOCKED",
                    source = "GSTN Official Registry",
                    matchedLegalName = "Prayagi Store & Services",
                    previousStatus = "UNVERIFIED",
                    newStatus = "LOCKED_ACTIVE",
                    failureReason = ""
                )
            )
        }
        assertTrue(auditResult.isSuccess)
    }

    @Test
    fun getCallLogById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = NetraFirestoreRepository(firestore, auth)
        val uniqueCallId = "call_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createCallLog(
                CallLogRecord(
                    id = uniqueCallId,
                    userId = aliceUid,
                    phoneNumber = "+919876543210",
                    contactName = "Private Contact",
                    direction = "OUTGOING",
                    timestampMillis = 1761700000000L,
                    durationSeconds = 120L,
                    callingMode = "BUSINESS_VERIFIED",
                    verifiedCallerIdStatus = "PROVIDER_APPROVED",
                    verifiedLegalNameUsed = "Prayagi Store & Services",
                    crmSyncStatus = "SYNCED",
                    crmContactId = "crm_1",
                    aiSummaryOverview = "Confidential summary"
                )
            )
        }
        assertTrue(createRes.isSuccess)

        signInTestUser(BOB_EMAIL)
        val bobRepo = NetraFirestoreRepository(firestore, auth)
        val bobAttempt = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getCallLogById(aliceUid, uniqueCallId)
        }
        assertTrue(bobAttempt.isFailure)
        val fsEx = bobAttempt.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun getCallLogById_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = NetraFirestoreRepository(firestore, auth)
        val uniqueCallId = "call_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createCallLog(
                CallLogRecord(
                    id = uniqueCallId,
                    userId = aliceUid,
                    phoneNumber = "+919876543210",
                    contactName = "Private Contact",
                    direction = "OUTGOING",
                    timestampMillis = 1761700000000L,
                    durationSeconds = 90L
                )
            )
        }

        auth.signOut()
        val unauthAttempt = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.getCallLogById(aliceUid, uniqueCallId)
        }
        assertTrue(unauthAttempt.isFailure)
        val fsEx = unauthAttempt.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@prayagi.in"
        const val BOB_EMAIL = "bob@prayagi.in"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3500L
    }
}

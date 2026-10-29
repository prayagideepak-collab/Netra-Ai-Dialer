package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CallDirection
import com.example.model.CallLogRecord
import com.example.model.OfficialRegistrationType
import com.example.model.PowerTier
import com.example.model.PowerTierEvaluator
import com.example.model.RegistrationValidators
import com.example.model.VerifiedBusinessIdentity
import com.example.model.VerifiedCallerIdStatus
import com.example.model.canActivateBrandedAnswering
import com.example.model.canPlaceVerifiedBusinessCall
import com.example.model.isVerifiedBusinessCall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Netra Dialer`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Netra Dialer", appName)
    }

    @Test
    fun `official business identity lock enforces verified record and representation authority`() {
        val pendingIdentity = VerifiedBusinessIdentity(
            lockedLegalBusinessName = "Prayagi Store & Services",
            registrationType = OfficialRegistrationType.GSTIN.name,
            registrationNumber = "07ABCDE1234F1Z5",
            recordVerificationPassed = true,
            authorityVerificationPassed = false,
            businessNumberVerified = true
        )
        assertFalse(pendingIdentity.canActivateBrandedAnswering)
        assertFalse(pendingIdentity.canPlaceVerifiedBusinessCall)

        val verifiedIdentity = pendingIdentity.copy(authorityVerificationPassed = true)
        assertTrue(verifiedIdentity.canActivateBrandedAnswering)
        assertTrue(verifiedIdentity.canPlaceVerifiedBusinessCall)
    }

    @Test
    fun `call log verified business caller id status and formatting work accurately`() {
        val verifiedCall = CallLogRecord(
            id = "call-1",
            contactName = "Aarav Mehta",
            phoneNumber = "+91 98110 44210",
            direction = CallDirection.OUTGOING.name,
            durationSeconds = 195,
            verifiedCallerIdStatus = VerifiedCallerIdStatus.PROVIDER_APPROVED.name,
            usedBusinessLine = true
        )
        assertTrue(verifiedCall.isVerifiedBusinessCall)
        assertEquals("3m 15s", RegistrationValidators.formatDuration(verifiedCall.durationSeconds))

        val personalCall = verifiedCall.copy(
            verifiedCallerIdStatus = VerifiedCallerIdStatus.PERSONAL_UNBRANDED.name,
            usedBusinessLine = false
        )
        assertFalse(personalCall.isVerifiedBusinessCall)
    }

    @Test
    fun `adaptive power tier evaluator enforces survival critical saver and thermal drop`() {
        assertEquals(
            PowerTier.SURVIVAL,
            PowerTierEvaluator.evaluateTier(
                batteryPercent = 4,
                isCharging = false,
                isBatterySaverOn = true,
                isThermalThrottled = false
            )
        )
        assertEquals(
            PowerTier.SAVER,
            PowerTierEvaluator.evaluateTier(
                batteryPercent = 80,
                isCharging = false,
                isBatterySaverOn = false,
                isThermalThrottled = true
            )
        )
    }
}

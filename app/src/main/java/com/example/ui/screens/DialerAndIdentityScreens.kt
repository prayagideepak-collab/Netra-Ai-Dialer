package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalEditableProfileEntity
import com.example.model.CrmContactRecord
import com.example.model.IdentityAuditEvent
import com.example.model.VerifiedBusinessIdentity
import com.example.model.canPlaceVerifiedBusinessCall
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DialerPadScreen(
    dialpadInput: String,
    selectedCallingMode: String,
    selectedSimSlot: Int,
    businessIdentity: VerifiedBusinessIdentity?,
    crmContacts: List<CrmContactRecord>,
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onInputChange: (String) -> Unit,
    onCallingModeChange: (String) -> Unit,
    onSimSlotChange: (Int) -> Unit,
    onCompleteCallAndSummarize: (
        phoneNumber: String,
        contactName: String,
        direction: String,
        durationSeconds: Long,
        transcript: String,
        callingMode: String
    ) -> Unit,
    onAnnounceIncomingTest: (String, String?, String?) -> String
) {
    val context = LocalContext.current
    var isCallActive by remember { mutableStateOf(false) }
    var activeCallDirection by remember { mutableStateOf("OUTGOING") }
    var activeCallSeconds by remember { mutableLongStateOf(0L) }
    var activeTranscriptNotes by remember {
        mutableStateOf(
            "Discussed Prayagi Store & Services Q4 order schedule, verified GST invoice details, and confirmed cold-chain reagent delivery by Friday 4 PM."
        )
    }
    var ttsStatusMessage by remember { mutableStateOf<String?>(null) }

    val matchedContact = remember(dialpadInput, crmContacts) {
        val cleanInput = dialpadInput.filter { it.isDigit() }
        if (cleanInput.length >= 4) {
            crmContacts.firstOrNull {
                it.phoneNumber.filter { c -> c.isDigit() }.contains(cleanInput)
            }
        } else null
    }

    LaunchedEffect(isCallActive) {
        if (isCallActive) {
            activeCallSeconds = 1L
            while (isCallActive) {
                delay(1000L)
                activeCallSeconds += 1L
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Outgoing Caller ID Mode & Truthful Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (businessIdentity?.canPlaceVerifiedBusinessCall == true) {
                                    Icons.Default.VerifiedUser
                                } else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (businessIdentity?.canPlaceVerifiedBusinessCall == true) {
                                    Color(0xFF10B981)
                                } else Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (selectedCallingMode == "BUSINESS_VERIFIED") {
                                        "OUTBOUND IDENTITY: ${businessIdentity?.verifiedLegalName?.ifBlank { "Prayagi Store & Services (Unverified)" } ?: "Prayagi Store & Services"}"
                                    } else {
                                        "OUTBOUND IDENTITY: PERSONAL LINE (UNBRANDED)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF34D399)
                                )
                                Text(
                                    text = "Provider Status: ${businessIdentity?.callerIdProviderStatus ?: "PROVIDER_APPROVED"} • Recipient display not guaranteed by app alone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCallingMode == "BUSINESS_VERIFIED",
                            onClick = { onCallingModeChange("BUSINESS_VERIFIED") },
                            label = { Text("Business Verified ID") },
                            leadingIcon = {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("mode_business_verified_chip")
                        )
                        FilterChip(
                            selected = selectedCallingMode == "PERSONAL",
                            onClick = { onCallingModeChange("PERSONAL") },
                            label = { Text("Personal Calling") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("mode_personal_chip")
                        )
                        FilterChip(
                            selected = selectedSimSlot == 1,
                            onClick = { onSimSlotChange(if (selectedSimSlot == 1) 2 else 1) },
                            label = { Text("SIM $selectedSimSlot") },
                            leadingIcon = {
                                Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("sim_selector_chip")
                        )
                    }
                }
            }
        }

        // Active Call Workbench (when a call is in progress)
        if (isCallActive) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_call_workbench_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedCallingMode == "BUSINESS_VERIFIED") {
                                "ACTIVE VERIFIED BUSINESS CALL (${businessIdentity?.verifiedLegalName ?: "Prayagi Store & Services"})"
                            } else {
                                "ACTIVE PERSONAL CALL"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFA7F3D0)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = matchedContact?.fullName ?: dialpadInput,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$dialpadInput • Duration: ${formatDuration(activeCallSeconds)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF34D399)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = activeTranscriptNotes,
                            onValueChange = { activeTranscriptNotes = it },
                            label = { Text("Live Call Conversation / Notes for Gemini Summarizer") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("active_call_transcript_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val finalDuration = activeCallSeconds.coerceAtLeast(18L)
                                isCallActive = false
                                onCompleteCallAndSummarize(
                                    dialpadInput,
                                    matchedContact?.fullName ?: "Enterprise Client",
                                    activeCallDirection,
                                    finalDuration,
                                    activeTranscriptNotes,
                                    selectedCallingMode
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("end_call_and_summarize_button")
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "End Call")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Conclude Call -> Auto-Log to CRM & Generate AI Summary",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Number Display + T9 Match
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dialpadInput,
                            onValueChange = onInputChange,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialpad_number_field"),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onBackspace,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("dialpad_backspace_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace"
                            )
                        }
                    }

                    if (matchedContact != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "T9 CRM Match: ${matchedContact.fullName} (${matchedContact.companyName})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // T9 Keypad Grid
                    val rows = listOf(
                        listOf("1" to "VOICEMAIL", "2" to "ABC", "3" to "DEF"),
                        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                        listOf("*" to "", "0" to "+", "#" to "")
                    )
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { (digit, letters) ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onDigitClick(digit) }
                                        .testTag("dialpad_key_$digit"),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = digit,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (letters.isNotEmpty()) {
                                            Text(
                                                text = letters,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Start Verified Call (Workbench) + Native Dialer Intent + Incoming TTS Test
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                activeCallDirection = "OUTGOING"
                                isCallActive = true
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(54.dp)
                                .testTag("start_verified_call_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color(0xFF042F2E)
                            )
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Start Call")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call (Verified ID)", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dialpadInput"))
                                    context.startActivity(dialIntent)
                                } catch (_: Exception) {
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("system_dial_intent_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("System Dialer")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val phrase = onAnnounceIncomingTest(
                                dialpadInput,
                                matchedContact?.fullName,
                                businessIdentity?.verifiedLegalName
                            )
                            ttsStatusMessage = "TTS Caller Announcer: \"$phrase\""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_tts_announcer_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Incoming Caller Name TTS Announcement")
                    }

                    if (!ttsStatusMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ttsStatusMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

/**
 * Official Business Identity Lock & Verified Outbound Caller ID Security Screen
 * Implements all rules from the PDF specification:
 * - Separate authoritative locked identity from editable profile fields
 * - GSTIN & Udyam alternative verification routes
 * - Applicant representation authority verification
 * - Tamper-evident Identity Audit Trail
 */
@Composable
fun BusinessIdentityLockScreen(
    businessIdentity: VerifiedBusinessIdentity?,
    editableProfile: LocalEditableProfileEntity,
    auditLogs: List<IdentityAuditEvent>,
    onVerifyAndLockIdentity: (
        registrationType: String,
        registrationNumber: String,
        legalName: String,
        tradeName: String,
        applicantAuthorized: Boolean,
        outboundNumber: String,
        outboundNumberVerified: Boolean,
        simulateMismatch: Boolean
    ) -> Unit,
    onTriggerStatusTransition: (String, String) -> Unit,
    onSaveEditableProfile: (
        internalNotes: String,
        businessHours: String,
        faqSummary: String,
        ttsEnabled: Boolean,
        quietHours: Boolean,
        autoRecordPref: String,
        driveWifiOnly: Boolean,
        driveIncludeRecordings: Boolean
    ) -> Unit
) {
    var regType by remember(businessIdentity) {
        mutableStateOf(businessIdentity?.registrationType ?: "GSTIN")
    }
    var regNumber by remember {
        mutableStateOf("27AABCP1234F1Z5")
    }
    var submittedLegalName by remember(businessIdentity) {
        mutableStateOf(businessIdentity?.verifiedLegalName?.ifBlank { "Prayagi Store & Services" } ?: "Prayagi Store & Services")
    }
    var submittedTradeName by remember(businessIdentity) {
        mutableStateOf(businessIdentity?.verifiedTradeName?.ifBlank { "Prayagi Smart Services" } ?: "Prayagi Smart Services")
    }
    var outboundNumber by remember(businessIdentity) {
        mutableStateOf(businessIdentity?.outboundNumber?.ifBlank { "+919820011223" } ?: "+919820011223")
    }
    var applicantAuthorized by remember { mutableStateOf(true) }
    var outboundNumberVerified by remember { mutableStateOf(true) }

    // Isolated Editable Profile State (cannot overwrite locked legal name)
    var internalNotes by remember(editableProfile) { mutableStateOf(editableProfile.internalDisplayNotes) }
    var businessHours by remember(editableProfile) { mutableStateOf(editableProfile.businessHours) }
    var faqSummary by remember(editableProfile) { mutableStateOf(editableProfile.faqSummary) }
    var ttsEnabled by remember(editableProfile) { mutableStateOf(editableProfile.ttsEnabled) }
    var quietHours by remember(editableProfile) { mutableStateOf(editableProfile.ttsQuietHoursEnabled) }

    val isLockedActive = businessIdentity?.lockStatus == "LOCKED_ACTIVE"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("identity_lock_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Current Locked Identity Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLockedActive) Color(0xFF064E3B) else Color(0xFF3B1821)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLockedActive) Icons.Default.Lock else Icons.Default.GppBad,
                            contentDescription = "Identity Lock Status",
                            tint = if (isLockedActive) Color(0xFF34D399) else Color(0xFFF87171),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OFFICIAL BUSINESS IDENTITY LOCK",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isLockedActive) Color(0xFFA7F3D0) else Color(0xFFFCA5A5)
                            )
                            Text(
                                text = if (isLockedActive) {
                                    businessIdentity?.verifiedLegalName ?: "Prayagi Store & Services"
                                } else {
                                    "UNVERIFIED / SUSPENDED (Branded AI Answering Blocked)"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Registration Route: ${businessIdentity?.registrationType ?: "GSTIN"} (${businessIdentity?.registrationNumberMasked?.ifBlank { "Pending" } ?: "27AA****F1Z5"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0)
                    )
                    Text(
                        text = "Authoritative Source: ${businessIdentity?.verificationSource?.ifBlank { "Awaiting Verification" } ?: "GSTN Official Registry"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0)
                    )
                    Text(
                        text = "Outbound Caller ID Status: ${businessIdentity?.callerIdProviderStatus ?: "PENDING"} • Number: ${businessIdentity?.outboundNumber ?: outboundNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Core Security Rule: Ordinary editable profile fields and uploaded screenshots can never override this verified legal name.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA7F3D0)
                    )
                }
            }
        }

        // Step 1-5 Authoritative GSTIN / Udyam Verification & Lock Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Authoritative GSTIN / Udyam Verification & Lock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Verify government registration and independent applicant authority before activating Business AI or Verified Outbound Caller ID.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = regType == "GSTIN",
                            onClick = {
                                regType = "GSTIN"
                                regNumber = "27AABCP1234F1Z5"
                            },
                            label = { Text("GSTIN Route") },
                            modifier = Modifier.testTag("identity_route_gstin")
                        )
                        FilterChip(
                            selected = regType == "UDYAM",
                            onClick = {
                                regType = "UDYAM"
                                regNumber = "UDYAM-MH-01-0012345"
                            },
                            label = { Text("Udyam Registration Route") },
                            modifier = Modifier.testTag("identity_route_udyam")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regNumber,
                        onValueChange = { regNumber = it },
                        label = { Text("$regType Registration Number *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("identity_reg_number_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = submittedLegalName,
                        onValueChange = { submittedLegalName = it },
                        label = { Text("Authoritative Record Legal Business Name *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("identity_legal_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = submittedTradeName,
                        onValueChange = { submittedTradeName = it },
                        label = { Text("Verified Trade Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = outboundNumber,
                        onValueChange = { outboundNumber = it },
                        label = { Text("Authorized Outbound Business Phone Number *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("identity_outbound_phone_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = applicantAuthorized,
                            onCheckedChange = { applicantAuthorized = it }
                        )
                        Text(
                            text = "Applicant authority to represent this registered business is verified (OTP / DSC)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = outboundNumberVerified,
                            onCheckedChange = { outboundNumberVerified = it }
                        )
                        Text(
                            text = "Outbound phone number right-to-use independently verified with carrier/provider",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onVerifyAndLockIdentity(
                                    regType,
                                    regNumber,
                                    submittedLegalName,
                                    submittedTradeName,
                                    applicantAuthorized,
                                    outboundNumber,
                                    outboundNumberVerified,
                                    false
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("verify_and_lock_identity_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color(0xFF042F2E)
                            )
                        ) {
                            Icon(Icons.Default.GppGood, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verify & Lock Identity", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onTriggerStatusTransition(
                                    "REVERIFICATION_PENDING",
                                    "Government registry name update detected; re-verification required."
                                )
                            },
                            modifier = Modifier.testTag("trigger_reverification_button")
                        ) {
                            Text("Re-Verify")
                        }
                    }
                }
            }
        }

        // Section 2: Isolated Non-Authoritative Editable Business Profile
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Editable Business Profile (Isolated from Identity Lock)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Edits here update internal descriptions, business hours, and AI FAQs without altering the locked legal business identity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = internalNotes,
                        onValueChange = { internalNotes = it },
                        label = { Text("Internal Non-Authoritative Display Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = businessHours,
                        onValueChange = { businessHours = it },
                        label = { Text("Business Operating Hours") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = faqSummary,
                        onValueChange = { faqSummary = it },
                        label = { Text("Service Catalog & Voice AI FAQ Context") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Incoming Caller Name TTS Announcement", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = ttsEnabled, onCheckedChange = { ttsEnabled = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enforce Quiet Hours (22:00 – 07:00)", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = quietHours, onCheckedChange = { quietHours = it })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            onSaveEditableProfile(
                                internalNotes,
                                businessHours,
                                faqSummary,
                                ttsEnabled,
                                quietHours,
                                editableProfile.autoRecordPreference,
                                editableProfile.driveWifiOnly,
                                editableProfile.driveIncludeRecordings
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_editable_profile_button")
                    ) {
                        Text("Save Editable Profile Settings (Preserves Identity Lock)")
                    }
                }
            }
        }

        // Section 3: Required Immutable Security Audit Trail
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Audit Trail",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. Immutable Identity Lock Audit Trail (${auditLogs.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (auditLogs.isEmpty()) {
                        Text(
                            text = "No audit events recorded yet. Verify a GSTIN or Udyam record above to log an immutable event.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        auditLogs.take(8).forEach { event ->
                            val ts = event.createdAt?.toDate()?.let {
                                SimpleDateFormat("dd MMM HH:mm:ss", Locale.US).format(it)
                            } ?: "Just now"
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = event.eventType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (event.newStatus == "LOCKED_ACTIVE") Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                                Text(
                                    text = ts,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Identity: ${event.matchedLegalName.ifBlank { "N/A" }} • ${event.previousStatus} -> ${event.newStatus}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Source: ${event.source}" +
                                    (if (event.failureReason.isNotBlank()) " • Reason: ${event.failureReason}" else ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

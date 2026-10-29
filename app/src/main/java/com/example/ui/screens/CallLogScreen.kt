package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CallLogDirectionFilter
import com.example.model.CallLogRecord
import com.example.model.CallLogSortOption
import com.example.model.CallLogVerifiedFilter
import com.example.model.isVerifiedBusinessCall
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CallLogScreen(
    callLogs: List<CallLogRecord>,
    sortOption: CallLogSortOption,
    verifiedFilter: CallLogVerifiedFilter,
    directionFilter: CallLogDirectionFilter,
    searchQuery: String,
    summarizingCallId: String?,
    onSortChange: (CallLogSortOption) -> Unit,
    onVerifiedFilterChange: (CallLogVerifiedFilter) -> Unit,
    onDirectionFilterChange: (CallLogDirectionFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRegenerateAiSummary: (CallLogRecord) -> Unit,
    onSimulateVerifiedOutgoingCall: () -> Unit,
    onSeedSampleData: () -> Unit,
    onOpenCrmContact: (String) -> Unit
) {
    val totalOutgoing = callLogs.count { it.direction == "OUTGOING" }
    val verifiedOutgoing = callLogs.count { it.direction == "OUTGOING" && it.isVerifiedBusinessCall }
    val totalDurationSec = callLogs.sumOf { it.durationSeconds }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("call_log_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Summary Telemetry Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VERIFIED CALL LOG & AI SUMMARY",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "Outgoing Caller ID & CRM Audit",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = onSimulateVerifiedOutgoingCall,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color(0xFF042F2E)
                            ),
                            modifier = Modifier.testTag("log_verified_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneCallback,
                                contentDescription = "Simulate Outgoing Verified Call",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Call + AI", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricPill(
                            modifier = Modifier.weight(1f),
                            label = "Verified Outgoing",
                            value = "$verifiedOutgoing / $totalOutgoing",
                            accent = Color(0xFF10B981)
                        )
                        MetricPill(
                            modifier = Modifier.weight(1f),
                            label = "Total Duration",
                            value = formatDuration(totalDurationSec),
                            accent = Color(0xFF38BDF8)
                        )
                        MetricPill(
                            modifier = Modifier.weight(1f),
                            label = "CRM Logged",
                            value = "${callLogs.count { it.crmSyncStatus == "SYNCED" }} Synced",
                            accent = Color(0xFFA78BFA)
                        )
                    }
                }
            }
        }

        // Search, Sort, and Filter Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("call_log_search_input"),
                        placeholder = { Text("Search by number, contact, legal name, or AI summary...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search call logs")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort criteria",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SORT BY (Date, Time, Duration, Verified ID):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CallLogSortOption.entries.forEach { option ->
                            FilterChip(
                                selected = sortOption == option,
                                onClick = { onSortChange(option) },
                                label = { Text(option.label) },
                                modifier = Modifier.testTag("sort_chip_${option.name.lowercase()}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.22f),
                                    selectedLabelColor = Color(0xFF34D399)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter criteria",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FILTER BY VERIFIED CALLER ID & DIRECTION:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CallLogVerifiedFilter.entries.forEach { vf ->
                            FilterChip(
                                selected = verifiedFilter == vf,
                                onClick = { onVerifiedFilterChange(vf) },
                                label = { Text(vf.label) },
                                modifier = Modifier.testTag("filter_verified_${vf.name.lowercase()}"),
                                leadingIcon = if (vf == CallLogVerifiedFilter.VERIFIED_BUSINESS_ONLY) {
                                    {
                                        Icon(
                                            Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                        CallLogDirectionFilter.entries.forEach { df ->
                            if (df != CallLogDirectionFilter.ALL) {
                                FilterChip(
                                    selected = directionFilter == df,
                                    onClick = {
                                        onDirectionFilterChange(
                                            if (directionFilter == df) CallLogDirectionFilter.ALL else df
                                        )
                                    },
                                    label = { Text(df.label) },
                                    modifier = Modifier.testTag("filter_direction_${df.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        }

        if (callLogs.isEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Empty call log",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Matching Call Logs Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Load sample verified enterprise calls for Prayagi Store & Services or place a simulated call to see Verified Business Caller ID telemetry, CRM auto-logging, and Gemini post-call summaries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onSeedSampleData,
                            modifier = Modifier.testTag("seed_sample_logs_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color(0xFF042F2E)
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Enterprise Call Logs & CRM Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(callLogs, key = { it.id }) { call ->
                CallLogItemCard(
                    call = call,
                    isSummarizing = summarizingCallId == call.id,
                    onRegenerateSummary = { onRegenerateAiSummary(call) },
                    onOpenCrm = { onOpenCrmContact(call.phoneNumber) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun CallLogItemCard(
    call: CallLogRecord,
    isSummarizing: Boolean,
    onRegenerateSummary: () -> Unit,
    onOpenCrm: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val dateFormatted = remember(call.timestampMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(call.timestampMillis))
    }
    val timeFormatted = remember(call.timestampMillis) {
        SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date(call.timestampMillis))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("call_log_card_${call.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (call.isVerifiedBusinessCall) Color(0xFF10B981).copy(alpha = 0.16f)
                            else Color(0xFF334155).copy(alpha = 0.4f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val directionIcon = when (call.direction) {
                        "OUTGOING" -> Icons.AutoMirrored.Filled.CallMade
                        "INCOMING" -> Icons.AutoMirrored.Filled.CallReceived
                        else -> Icons.AutoMirrored.Filled.CallMissed
                    }
                    Icon(
                        imageVector = directionIcon,
                        contentDescription = call.direction,
                        tint = if (call.isVerifiedBusinessCall) Color(0xFF10B981) else Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = call.contactName.ifBlank { call.phoneNumber },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = call.direction,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    Text(
                        text = "${call.phoneNumber} • $dateFormatted at $timeFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatDuration(call.durationSeconds),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand call details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verified Business Caller ID Status Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VerifiedCallerIdBadge(
                    status = call.verifiedCallerIdStatus,
                    legalName = call.verifiedLegalNameUsed
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.16f),
                    modifier = Modifier.clickable { onOpenCrm() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "CRM Sync Status",
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CRM: ${call.crmSyncStatus}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFC7D2FE)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Truthful Recipient Display Disclosure per PDF Sec 2.3
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Caller ID telemetry note",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = callerIdTruthfulMeaning(call.verifiedCallerIdStatus, call.verifiedLegalNameUsed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // AI-Powered Call Summarization Section
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Call Summary",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "GEMINI POST-CALL SUMMARY & ACTION ITEMS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF38BDF8)
                                    )
                                }

                                if (isSummarizing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF38BDF8)
                                    )
                                } else {
                                    AssistChip(
                                        onClick = onRegenerateSummary,
                                        label = { Text("Re-Summarize", style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.testTag("resummarize_button_${call.id}"),
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = Color(0xFF0F172A)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = call.aiSummaryOverview.ifBlank { "Tap Re-Summarize to generate an AI call summary." },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            if (call.aiDiscussionPoints.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Key Discussion Points:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                call.aiDiscussionPoints.forEach { point ->
                                    Text(
                                        text = "• $point",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            if (call.aiActionItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Action Items (Synced to CRM):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF10B981)
                                )
                                call.aiActionItems.forEach { item ->
                                    Row(
                                        modifier = Modifier.padding(top = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerifiedCallerIdBadge(status: String, legalName: String) {
    val (bgColor, textColor, icon, label) = when (status) {
        "PROVIDER_APPROVED" -> Quad(
            Color(0xFF10B981).copy(alpha = 0.18f),
            Color(0xFF34D399),
            Icons.Default.VerifiedUser,
            "Verified Business ID: ${legalName.ifBlank { "Approved" }}"
        )
        "IDENTITY_AND_NUMBER_VERIFIED" -> Quad(
            Color(0xFF0EA5E9).copy(alpha = 0.18f),
            Color(0xFF38BDF8),
            Icons.Default.VerifiedUser,
            "Identity & Number Verified"
        )
        "REGISTRATION_PENDING" -> Quad(
            Color(0xFFF59E0B).copy(alpha = 0.18f),
            Color(0xFFFBBF24),
            Icons.Default.Warning,
            "Caller-ID Approval Pending"
        )
        "PERSONAL_UNBRANDED" -> Quad(
            Color(0xFF64748B).copy(alpha = 0.22f),
            Color(0xFFCBD5E1),
            Icons.Default.Person,
            "Personal Line (Unbranded)"
        )
        else -> Quad(
            Color(0xFFEF4444).copy(alpha = 0.18f),
            Color(0xFFF87171),
            Icons.Default.Warning,
            "Unverified / Normal SIM Fallback"
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetricPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.65f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
}

private fun callerIdTruthfulMeaning(status: String, legalName: String): String {
    return when (status) {
        "PROVIDER_APPROVED" ->
            "Provider Approved ($legalName): Carrier/provider accepted configuration; recipient display depends on recipient carrier & dialer."
        "IDENTITY_AND_NUMBER_VERIFIED" ->
            "Business Identity & Outbound Number Verified ($legalName): Ready for carrier CNAM/VoIP route."
        "REGISTRATION_PENDING" ->
            "Caller-ID Registration Pending: Business identity locked; waiting on carrier/telephony approval."
        "PERSONAL_UNBRANDED" ->
            "Personal Calling Mode: Business identity intentionally omitted for personal call separation."
        else ->
            "Provider Rejected / Unavailable: Placed via normal authorized calling without spoofing an identity."
    }
}

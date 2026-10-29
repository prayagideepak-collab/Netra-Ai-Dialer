package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.model.CallLogRecord
import com.example.model.CrmContactRecord
import com.example.model.isVerifiedBusinessCall
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrmWorkspaceScreen(
    crmContacts: List<CrmContactRecord>,
    callLogs: List<CallLogRecord>,
    highlightedPhone: String?,
    onAddContact: (String, String, String, String, String, String) -> Unit,
    onPlaceVerifiedCallToContact: (CrmContactRecord) -> Unit,
    onSeedSampleData: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStageFilter by remember { mutableStateOf("ALL") }

    val filteredContacts = remember(crmContacts, selectedStageFilter, highlightedPhone) {
        val list = if (selectedStageFilter == "ALL") {
            crmContacts
        } else {
            crmContacts.filter { it.stage == selectedStageFilter }
        }
        if (!highlightedPhone.isNullOrBlank()) {
            list.sortedByDescending {
                it.phoneNumber.takeLast(10) == highlightedPhone.filter { c -> c.isDigit() }.takeLast(10)
            }
        } else list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("crm_contacts_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
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
                                text = "NETRA ENTERPRISE CRM INTEGRATION",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "Automatic Call & Verified ID Sync",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("add_crm_contact_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add CRM Contact", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Contact")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Every completed call automatically updates the matching CRM Contact Record with Caller ID, Verified Business status, Call Duration, and Gemini AI Action Items.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "VIP_PARTNER", "ACTIVE_CLIENT", "LEAD", "SUPPORT").forEach { stage ->
                            FilterChip(
                                selected = selectedStageFilter == stage,
                                onClick = { selectedStageFilter = stage },
                                label = { Text(stage.replace("_", " ")) },
                                modifier = Modifier.testTag("crm_stage_chip_${stage.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        if (filteredContacts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No CRM Contact Records Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Load sample enterprise accounts or place an outgoing call to automatically create and sync CRM records.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onSeedSampleData,
                            modifier = Modifier.testTag("crm_seed_button")
                        ) {
                            Text("Load Enterprise CRM Contacts")
                        }
                    }
                }
            }
        } else {
            items(filteredContacts, key = { it.id }) { contact ->
                val contactCalls = callLogs.filter {
                    it.crmContactId == contact.id ||
                        it.phoneNumber.filter { c -> c.isDigit() }.takeLast(10) ==
                        contact.phoneNumber.filter { c -> c.isDigit() }.takeLast(10)
                }
                CrmContactCard(
                    contact = contact,
                    linkedCalls = contactCalls,
                    onPlaceVerifiedCall = { onPlaceVerifiedCallToContact(contact) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    if (showAddDialog) {
        AddCrmContactDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, company, phone, email, stage, notes ->
                onAddContact(name, company, phone, email, stage, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CrmContactCard(
    contact: CrmContactRecord,
    linkedCalls: List<CallLogRecord>,
    onPlaceVerifiedCall: () -> Unit
) {
    val lastCallFormatted = remember(contact.lastInteractionTimestamp) {
        if (contact.lastInteractionTimestamp > 0) {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(contact.lastInteractionTimestamp))
        } else "No calls yet"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crm_contact_card_${contact.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = contact.companyName,
                        tint = Color(0xFF818CF8)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = contact.stage.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${contact.companyName} • ${contact.phoneNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onPlaceVerifiedCall,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("crm_call_button_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = "Place Verified Business Call",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call + Log")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CRM Call Telemetry Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CrmStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Total Calls",
                    value = "${contact.totalCalls} calls"
                )
                CrmStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Verified ID Calls",
                    value = "${contact.verifiedOutboundCallsCount} verified"
                )
                CrmStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Total Talk Time",
                    value = formatDuration(contact.totalDurationSeconds)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Latest AI Call Summary attached to CRM Contact
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LATEST AI SUMMARY & ACTION ITEMS ($lastCallFormatted)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = contact.latestAiSummary.ifBlank { "No call summary recorded yet." },
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (contact.openActionItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        contact.openActionItems.forEach { item ->
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA7F3D0)
                                )
                            }
                        }
                    }
                }
            }

            if (linkedCalls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AUTOMATICALLY LOGGED CALL HISTORY (${linkedCalls.size}):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                linkedCalls.take(3).forEach { call ->
                    val ts = SimpleDateFormat("dd MMM hh:mm a", Locale.US).format(Date(call.timestampMillis))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = if (call.isVerifiedBusinessCall) Color(0xFF10B981) else Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${call.direction} • $ts • ${formatDuration(call.durationSeconds)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = call.verifiedCallerIdStatus.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (call.isVerifiedBusinessCall) Color(0xFF34D399) else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CrmStatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.55f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AddCrmContactDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("+91") }
    var email by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf("ACTIVE_CLIENT") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add CRM Contact Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("crm_input_name")
                )
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company / Organization") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("crm_input_company")
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("crm_input_phone")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Account / Bioscience Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && phoneNumber.isNotBlank()) {
                        onSave(fullName, companyName, phoneNumber, email, stage, notes)
                    }
                },
                modifier = Modifier.testTag("crm_save_contact_confirm")
            ) {
                Text("Save to CRM")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

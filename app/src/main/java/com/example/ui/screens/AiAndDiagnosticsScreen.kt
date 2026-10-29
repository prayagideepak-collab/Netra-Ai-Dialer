package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalRecordingEntity
import com.example.data.local.LocalSpamRuleEntity
import com.example.telephony.AdaptivePowerController
import com.example.viewmodel.AiTaskState
import com.example.viewmodel.BannerGenState
import com.example.viewmodel.ChatMessageItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAndDiagnosticsScreen(
    chatMessages: List<ChatMessageItem>,
    aiTaskState: AiTaskState,
    bannerGenState: BannerGenState,
    spamRules: List<LocalSpamRuleEntity>,
    recordings: List<LocalRecordingEntity>,
    powerTelemetry: AdaptivePowerController.DevicePowerTelemetry,
    wakeupsThisHour: Int,
    networkCallsThisHour: Int,
    onSendChatMessage: (String, String) -> Unit,
    onAnalyzeBitmap: (Bitmap, String) -> Unit,
    onGenerateCallerBanner: (String, String, Boolean) -> Unit,
    onAddSpamRule: (String, String, String, Boolean, Boolean) -> Unit,
    onSetBatteryOverride: (Int?) -> Unit,
    onRequestGoogleDriveBackup: () -> Unit
) {
    val context = LocalContext.current
    var chatInput by remember { mutableStateOf("") }
    var selectedModelTier by remember { mutableStateOf("GENERAL_FLASH") } // GENERAL_FLASH, FAST_LITE, HIGH_THINKING

    // Branded Caller Card Image Generator state
    val aspectRatios = listOf("1:1", "2:3", "3:2", "3:4", "4:3", "9:16", "16:9", "21:9")
    var selectedAspectRatio by remember { mutableStateOf("16:9") }
    var useStudioProModel by remember { mutableStateOf(false) }
    var bannerPrompt by remember {
        mutableStateOf("Verified Business Caller ID card for Prayagi Store & Services with emerald security shield and modern dark telecom aesthetic")
    }

    // Spam rule form state
    var spamPhoneInput by remember { mutableStateOf("+911408889900") }
    var spamLabelInput by remember { mutableStateOf("Reported Telemarketer") }

    // Zero-permission Android Photo Picker for Document/Label Image Analysis
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val stream = context.contentResolver.openInputStream(uri)
                val decoded = android.graphics.BitmapFactory.decodeStream(stream)
                stream?.close()
                if (decoded != null) {
                    onAnalyzeBitmap(
                        decoded,
                        "Extract business details, registration identifiers, or bioscience reagent specifications from this image."
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("ai_and_diagnostics_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // 1. Multi-Turn Gemini Voice AI & Bioscience Assistant
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Gemini Intelligence & Bioscience Advisor",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GEMINI VOICE AI & BIOSCIENCE ADVISOR",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "Multi-Turn Chat • Search & Maps • High Thinking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedModelTier == "GENERAL_FLASH",
                            onClick = { selectedModelTier = "GENERAL_FLASH" },
                            label = { Text("gemini-3.8-flash (Search/Maps)") },
                            modifier = Modifier.testTag("model_chip_flash")
                        )
                        FilterChip(
                            selected = selectedModelTier == "FAST_LITE",
                            onClick = { selectedModelTier = "FAST_LITE" },
                            label = { Text("gemini-3.5-flash-lite (Bolt Fast)") },
                            modifier = Modifier.testTag("model_chip_lite")
                        )
                        FilterChip(
                            selected = selectedModelTier == "HIGH_THINKING",
                            onClick = { selectedModelTier = "HIGH_THINKING" },
                            label = { Text("gemini-4-argon (High Thinking)") },
                            modifier = Modifier.testTag("model_chip_argon")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Conversation thread
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B0F17), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chatMessages.takeLast(5).forEach { msg ->
                            val isUser = msg.sender == "user"
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isUser) Color(0xFF1E293B) else Color(0xFF064E3B).copy(alpha = 0.65f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${if (isUser) "You" else "Netra AI"} • ${msg.modelBadge}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF38BDF8)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Ask about CRM calls, cold-chain bioscience specs, or caller ID...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_chat_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    onSendChatMessage(chatInput, selectedModelTier)
                                    chatInput = ""
                                }
                            },
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("ai_chat_send_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color(0xFF042F2E)
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }

        // 2. Multimodal Document & Label Image Scanner (gemini-4-argon)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Analyze Image",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "MULTIMODAL IMAGE & CERTIFICATE ANALYZER (gemini-4-argon)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "Inspect Business Cards, Invoices & Lab Labels",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_photo_analyze_button")
                        ) {
                            Text("Upload Photo")
                        }

                        OutlinedButton(
                            onClick = {
                                val sampleBitmap = createSampleInspectionBitmap()
                                onAnalyzeBitmap(
                                    sampleBitmap,
                                    "Verify the invoice & reagent lot details on this Prayagi Store & Services dispatch slip."
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("analyze_sample_slip_button")
                        ) {
                            Text("Scan Dispatch Slip")
                        }
                    }

                    when (aiTaskState) {
                        is AiTaskState.Loading -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(aiTaskState.label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        is AiTaskState.Success -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = aiTaskState.resultText,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        is AiTaskState.Error -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Error: ${aiTaskState.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        AiTaskState.Idle -> {}
                    }
                }
            }
        }

        // 3. Branded Caller ID Card Studio with Aspect Ratio Control (gemini-nano-banana-2.1)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = "Aspect Ratio Studio",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "BRANDED CALLER ID BANNER STUDIO",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFA78BFA)
                            )
                            Text(
                                text = "Control Image Aspect Ratios (1:1 to 21:9)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        aspectRatios.forEach { ratio ->
                            FilterChip(
                                selected = selectedAspectRatio == ratio,
                                onClick = { selectedAspectRatio = ratio },
                                label = { Text(ratio) },
                                modifier = Modifier.testTag("aspect_ratio_chip_$ratio")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bannerPrompt,
                        onValueChange = { bannerPrompt = it },
                        label = { Text("Caller Card Visual Prompt") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !useStudioProModel,
                            onClick = { useStudioProModel = false },
                            label = { Text("gemini-nano-banana-2.1") }
                        )
                        FilterChip(
                            selected = useStudioProModel,
                            onClick = { useStudioProModel = true },
                            label = { Text("gemini-3-pro-image") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onGenerateCallerBanner(bannerPrompt, selectedAspectRatio, useStudioProModel)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_caller_banner_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Verified Caller Card ($selectedAspectRatio)")
                    }

                    when (bannerGenState) {
                        is BannerGenState.Loading -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Synthesizing caller card image (may take 30–60s)...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        is BannerGenState.Success -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Image(
                                bitmap = bannerGenState.bitmap.asImageBitmap(),
                                contentDescription = "Generated Verified Caller Card",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        is BannerGenState.Error -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Image Generation Error: ${bannerGenState.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        BannerGenState.Idle -> {}
                    }
                }
            }
        }

        // 4. Hybrid Offline Spam Protection & Call Recording Manager
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Spam Protection",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HYBRID OFFLINE SPAM SHIELD & CALL RECORDER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = spamPhoneInput,
                            onValueChange = { spamPhoneInput = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = spamLabelInput,
                            onValueChange = { spamLabelInput = it },
                            label = { Text("Label") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onAddSpamRule(spamPhoneInput, spamLabelInput, "SPAM", true, false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f).testTag("block_number_button")
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Block Spam")
                        }
                        OutlinedButton(
                            onClick = {
                                onAddSpamRule(spamPhoneInput, spamLabelInput, "ALLOW_LIST", false, true)
                            },
                            modifier = Modifier.weight(1f).testTag("whitelist_number_button")
                        ) {
                            Text("Allow-List")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    spamRules.take(4).forEach { rule ->
                        Text(
                            text = "• ${rule.phoneNumber} — ${rule.label} (${if (rule.isWhitelisted) "WHITELISTED" else "BLOCKED"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (rule.isWhitelisted) Color(0xFF34D399) else Color(0xFFF87171)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Call Recording Status",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Encrypted Call Recordings (${recordings.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = powerTelemetry.callRecordingHardwareStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. Ultra-Low Power 24/7 Architecture & Release Gate Verification
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "24/7 Power Budget",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ULTRA-LOW POWER 24/7 DIAGNOSTICS & DRIVE BACKUP",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = powerTelemetry.activeTier.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = powerTelemetry.activeTier.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Battery: ${powerTelemetry.batteryPercent}% • Wakeups/hr: $wakeupsThisHour/${powerTelemetry.activeTier.maxWakeupsPerHour} • Network Calls/hr: $networkCallsThisHour/${powerTelemetry.activeTier.maxNetworkCallsPerHour}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF38BDF8)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Release Gate Battery Simulation (100%, 25%, 12%, 4%):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(null to "Live", 100 to "100%", 25 to "25%", 12 to "12%", 4 to "4%").forEach { (pct, label) ->
                            FilterChip(
                                selected = (pct == null && powerTelemetry.batteryPercent == 82) || powerTelemetry.batteryPercent == pct,
                                onClick = { onSetBatteryOverride(pct) },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onRequestGoogleDriveBackup,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drive_backup_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Encrypted Google Drive & Cloud Backup Now")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

private fun createSampleInspectionBitmap(): Bitmap {
    val bmp = Bitmap.createBitmap(480, 260, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.drawColor(android.graphics.Color.rgb(15, 23, 42))
    val paint = Paint().apply {
        color = android.graphics.Color.rgb(52, 211, 153)
        textSize = 24f
        isAntiAlias = true
    }
    canvas.drawText("PRAYAGI STORE & SERVICES", 24f, 50f, paint)
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 18f
    canvas.drawText("GSTIN: 27AABCP1234F1Z5 (Verified)", 24f, 90f, paint)
    canvas.drawText("Client: Apex BioLabs Pvt Ltd", 24f, 130f, paint)
    canvas.drawText("Lot #B-409 Sterile Buffer (2-8C)", 24f, 170f, paint)
    canvas.drawText("Caller ID Route: Provider Approved", 24f, 210f, paint)
    return bmp
}

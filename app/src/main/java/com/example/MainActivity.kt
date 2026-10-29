package com.example

import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.NetraAuthGateScreen
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.local.NetraLocalDatabase
import com.example.data.repository.NetraFirestoreRepository
import com.example.telephony.AdaptivePowerController
import com.example.telephony.CallerAnnouncerManager
import com.example.ui.screens.AiAndDiagnosticsScreen
import com.example.ui.screens.BusinessIdentityLockScreen
import com.example.ui.screens.CallLogScreen
import com.example.ui.screens.CrmWorkspaceScreen
import com.example.ui.screens.DialerPadScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.NetraDialerViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NetraRootAppNavigation()
                }
            }
        }
    }
}

@Composable
fun NetraRootAppNavigation() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        NetraAuthGateScreen(onAuthenticated = {})
    } else {
        NetraAuthenticatedWorkspace(currentUserId = user.uid)
    }
}

@Composable
fun NetraAuthenticatedWorkspace(currentUserId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val announcer = remember { CallerAnnouncerManager(context) }

    DisposableEffect(Unit) {
        onDispose { announcer.shutdown() }
    }

    val viewModel: NetraDialerViewModel = viewModel(
        key = currentUserId,
        factory = NetraDialerViewModel.provideFactory(
            repository = NetraFirestoreRepository(
                FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
            ),
            localDao = NetraLocalDatabase.getInstance(context).dao(),
            currentUserId = currentUserId
        )
    )

    val callLogs by viewModel.filteredCallLogs.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val verifiedFilter by viewModel.verifiedFilter.collectAsStateWithLifecycle()
    val directionFilter by viewModel.directionFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val summarizingCallId by viewModel.summarizingCallId.collectAsStateWithLifecycle()
    val crmContacts by viewModel.crmContacts.collectAsStateWithLifecycle()
    val businessIdentity by viewModel.businessIdentity.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val editableProfile by viewModel.editableProfile.collectAsStateWithLifecycle()
    val spamRules by viewModel.spamRules.collectAsStateWithLifecycle()
    val recordings by viewModel.recordings.collectAsStateWithLifecycle()
    val dialpadInput by viewModel.dialpadInput.collectAsStateWithLifecycle()
    val selectedCallingMode by viewModel.selectedCallingMode.collectAsStateWithLifecycle()
    val selectedSimSlot by viewModel.selectedSimSlot.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val aiTaskState by viewModel.aiTaskState.collectAsStateWithLifecycle()
    val bannerGenState by viewModel.bannerGenState.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val batteryOverride by viewModel.batteryOverridePercent.collectAsStateWithLifecycle()
    val wakeupsThisHour by viewModel.wakeupsCountThisHour.collectAsStateWithLifecycle()
    val networkCallsThisHour by viewModel.networkCallsThisHour.collectAsStateWithLifecycle()

    val powerTelemetry = remember(batteryOverride) {
        AdaptivePowerController.readTelemetry(context, batteryOverride)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var highlightedCrmPhone by remember { mutableStateOf<String?>(null) }

    // Role request launcher for Default Dialer Role
    val dialerRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    if (selectedTab != 0) {
        BackHandler {
            selectedTab = 0
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Netra Verified Logo",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "NETRA AI DIALER",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = businessIdentity?.verifiedLegalName?.ifBlank { "Prayagi Store & Services" }
                                    ?: "Prayagi Store & Services",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF34D399),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssistChip(
                            onClick = { selectedTab = 4 },
                            label = {
                                Text(
                                    text = "${powerTelemetry.batteryPercent}% • ${powerTelemetry.activeTier.badgeText}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.BatteryStd,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFA7F3D0)
                            )
                        )

                        if (!powerTelemetry.isDefaultDialerRoleHeld && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            AssistChip(
                                onClick = {
                                    try {
                                        val rm = context.getSystemService(RoleManager::class.java)
                                        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                                            dialerRoleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER))
                                        }
                                    } catch (_: Exception) {
                                    }
                                },
                                label = { Text("Set Default", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("request_default_dialer_chip")
                            )
                        }

                        IconButton(
                            onClick = {
                                signOut(
                                    credentialManager = credentialManager,
                                    onSignOutComplete = {},
                                    scope = scope
                                )
                            },
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                if (!statusBanner.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF064E3B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = statusBanner!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA7F3D0),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearStatusBanner() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss notification",
                                    tint = Color(0xFFA7F3D0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = Color(0xFF0F172A)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Call Logs") },
                    label = { Text("Call Logs") },
                    modifier = Modifier.testTag("nav_tab_call_logs")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Dialpad, contentDescription = "Dialer") },
                    label = { Text("Dialer") },
                    modifier = Modifier.testTag("nav_tab_dialer")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Business, contentDescription = "CRM Sync") },
                    label = { Text("CRM") },
                    modifier = Modifier.testTag("nav_tab_crm")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Identity Lock") },
                    label = { Text("Identity Lock") },
                    modifier = Modifier.testTag("nav_tab_identity")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI & Shield") },
                    label = { Text("AI & Shield") },
                    modifier = Modifier.testTag("nav_tab_ai_hub")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> CallLogScreen(
                    callLogs = callLogs,
                    sortOption = sortOption,
                    verifiedFilter = verifiedFilter,
                    directionFilter = directionFilter,
                    searchQuery = searchQuery,
                    summarizingCallId = summarizingCallId,
                    onSortChange = viewModel::setSortOption,
                    onVerifiedFilterChange = viewModel::setVerifiedFilter,
                    onDirectionFilterChange = viewModel::setDirectionFilter,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onRegenerateAiSummary = { call ->
                        viewModel.generatePostCallSummaryForCall(
                            callId = call.id,
                            phoneNumber = call.phoneNumber,
                            contactName = call.contactName,
                            durationSeconds = call.durationSeconds,
                            verifiedStatus = call.verifiedCallerIdStatus,
                            conversationTranscript = "Follow-up review of ${call.contactName} call regarding Prayagi Store & Services delivery, verified billing, and technical support."
                        )
                    },
                    onSimulateVerifiedOutgoingCall = {
                        viewModel.logCompletedCallWithCrmAndAiSummary(
                            phoneNumber = "+919876543210",
                            contactName = "Dr. Aarav Mehta (Apex BioLabs)",
                            direction = "OUTGOING",
                            durationSeconds = 265L,
                            conversationTranscript = "Confirmed outbound Verified Business Caller ID for Prayagi Store & Services, approved Q4 bioscience reagent lot #409 delivery, and scheduled invoice reconciliation.",
                            requestedMode = "BUSINESS_VERIFIED"
                        )
                    },
                    onSeedSampleData = viewModel::seedSampleEnterpriseData,
                    onOpenCrmContact = { phone ->
                        highlightedCrmPhone = phone
                        selectedTab = 2
                    }
                )

                1 -> DialerPadScreen(
                    dialpadInput = dialpadInput,
                    selectedCallingMode = selectedCallingMode,
                    selectedSimSlot = selectedSimSlot,
                    businessIdentity = businessIdentity,
                    crmContacts = crmContacts,
                    onDigitClick = viewModel::appendDialpadDigit,
                    onBackspace = viewModel::backspaceDialpad,
                    onInputChange = viewModel::updateDialpadInput,
                    onCallingModeChange = viewModel::setCallingMode,
                    onSimSlotChange = viewModel::setSimSlot,
                    onCompleteCallAndSummarize = { phone, name, dir, dur, notes, mode ->
                        viewModel.logCompletedCallWithCrmAndAiSummary(
                            phoneNumber = phone,
                            contactName = name,
                            direction = dir,
                            durationSeconds = dur,
                            conversationTranscript = notes,
                            requestedMode = mode
                        )
                        selectedTab = 0
                    },
                    onAnnounceIncomingTest = { phone, contactName, verifiedBiz ->
                        announcer.announceCallerIfPermitted(
                            phoneNumber = phone,
                            savedContactName = contactName,
                            verifiedBusinessName = verifiedBiz,
                            isSpamSuspected = false,
                            ttsEnabled = editableProfile.ttsEnabled,
                            quietHoursEnabled = editableProfile.ttsQuietHoursEnabled
                        )
                    }
                )

                2 -> CrmWorkspaceScreen(
                    crmContacts = crmContacts,
                    callLogs = callLogs,
                    highlightedPhone = highlightedCrmPhone,
                    onAddContact = viewModel::createOrUpdateManualCrmContact,
                    onPlaceVerifiedCallToContact = { contact ->
                        viewModel.logCompletedCallWithCrmAndAiSummary(
                            phoneNumber = contact.phoneNumber,
                            contactName = contact.fullName,
                            direction = "OUTGOING",
                            durationSeconds = 210L,
                            conversationTranscript = "Outbound verified business call to ${contact.fullName} at ${contact.companyName}. Discussed order status, SLA milestones, and next action items.",
                            requestedMode = "BUSINESS_VERIFIED"
                        )
                    },
                    onSeedSampleData = viewModel::seedSampleEnterpriseData
                )

                3 -> BusinessIdentityLockScreen(
                    businessIdentity = businessIdentity,
                    editableProfile = editableProfile,
                    auditLogs = auditLogs,
                    onVerifyAndLockIdentity = viewModel::verifyAndLockBusinessIdentity,
                    onTriggerStatusTransition = viewModel::triggerIdentityStatusTransition,
                    onSaveEditableProfile = viewModel::saveEditableBusinessProfile
                )

                4 -> AiAndDiagnosticsScreen(
                    chatMessages = chatMessages,
                    aiTaskState = aiTaskState,
                    bannerGenState = bannerGenState,
                    spamRules = spamRules,
                    recordings = recordings,
                    powerTelemetry = powerTelemetry,
                    wakeupsThisHour = wakeupsThisHour,
                    networkCallsThisHour = networkCallsThisHour,
                    onSendChatMessage = viewModel::sendChatMessage,
                    onAnalyzeBitmap = viewModel::analyzeDocumentBitmap,
                    onGenerateCallerBanner = viewModel::generateBrandedCallerBanner,
                    onAddSpamRule = viewModel::addOrUpdateSpamRule,
                    onSetBatteryOverride = viewModel::setBatteryOverride,
                    onRequestGoogleDriveBackup = {
                        viewModel.saveEditableBusinessProfile(
                            internalNotes = editableProfile.internalDisplayNotes,
                            businessHours = editableProfile.businessHours,
                            faqSummary = editableProfile.faqSummary,
                            ttsEnabled = editableProfile.ttsEnabled,
                            quietHours = editableProfile.ttsQuietHoursEnabled,
                            autoRecordPref = editableProfile.autoRecordPreference,
                            driveWifiOnly = editableProfile.driveWifiOnly,
                            driveIncludeRecordings = editableProfile.driveIncludeRecordings
                        )
                    }
                )
            }
        }
    }
}

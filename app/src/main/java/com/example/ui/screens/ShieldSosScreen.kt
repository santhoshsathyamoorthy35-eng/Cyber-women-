package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TrustedContact
import com.example.domain.CrimeMapEngine
import com.example.service.LocationTracker
import com.example.ui.components.CyberCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.dialPhoneNumber
import com.example.ui.components.shareLocationIntent
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.CommandNavy
import com.example.ui.theme.CriticalRose
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HairlineBright
import com.example.ui.theme.PanelNavy
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun ShieldSosScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val trustedContacts by viewModel.trustedContacts.collectAsStateWithLifecycle()
    val sosHistory by viewModel.mergedSosHistory.collectAsStateWithLifecycle()
    val sosCountdown by viewModel.sosCountdownSec.collectAsStateWithLifecycle()
    val activeSosId by viewModel.activeSosId.collectAsStateWithLifecycle()
    val deviceLoc by viewModel.deviceLocation.collectAsStateWithLifecycle()
    val shakeEnabled by viewModel.shakeToSosEnabled.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var editingContact by remember { mutableStateOf<TrustedContact?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var customMsgDraft by remember(userProfile?.customSosMessage) {
        mutableStateOf(
            userProfile?.customSosMessage
                ?: "EMERGENCY SOS: I need immediate help. Please check my location."
        )
    }
    var fakeCallCaller by remember { mutableStateOf<String?>(null) }

    val locPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            viewModel.refreshDeviceLocation(context)
        }
    }

    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            holdProgress = 0f
            val steps = 15
            for (i in 1..steps) {
                if (!isHolding) break
                delay(100L)
                holdProgress = i / steps.toFloat()
            }
            if (isHolding && holdProgress >= 1f) {
                isHolding = false
                holdProgress = 0f
                viewModel.triggerSosNow(context, "hold")
            }
        } else {
            holdProgress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. SOS TRIGGER COMMAND CARD
            item {
                CyberCard(
                    containerColor = Color(0xFF1D0E1B),
                    borderColor = CriticalRose.copy(alpha = 0.7f)
                ) {
                    Text("THE SHIELD · EMERGENCY SOS", style = MaterialTheme.typography.labelMedium, color = CriticalRose)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when {
                            sosCountdown != null -> "Triggering SOS log in ${sosCountdown}s..."
                            activeSosId != null -> "Active SOS Session · Live GPS Recording"
                            else -> "Hold button 1.5s for instant SOS or tap for 5s countdown"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(CriticalRose, Color(0xFF8C0823)))
                                )
                                .pointerInput(sosCountdown, activeSosId) {
                                    detectTapGestures(
                                        onTap = {
                                            if (sosCountdown == null && activeSosId == null) {
                                                viewModel.startSosCountdown(context, "button")
                                            }
                                        },
                                        onPress = {
                                            if (sosCountdown == null && activeSosId == null) {
                                                isHolding = true
                                                tryAwaitRelease()
                                                isHolding = false
                                            }
                                        }
                                    )
                                }
                                .testTag("shield_big_sos_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Shield, contentDescription = "SOS Trigger", tint = Color.White, modifier = Modifier.size(38.dp))
                                Text(
                                    text = if (sosCountdown != null) "${sosCountdown}s" else "SOS",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (isHolding && holdProgress > 0f) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { holdProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = CriticalRose
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { dialPhoneNumber(context, "112") },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("shield_dial_112_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CriticalRose)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call 112 Now", color = Color.White)
                        }
                        if (sosCountdown != null || activeSosId != null) {
                            Button(
                                onClick = {
                                    if (sosCountdown != null) viewModel.cancelSosCountdown(context)
                                    else viewModel.resolveActiveSos(context)
                                },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("shield_resolve_cancel_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald, contentColor = DeepObsidian)
                            ) {
                                Text(if (sosCountdown != null) "Cancel (${sosCountdown}s)" else "Mark Safe")
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    shareLocationIntent(
                                        context,
                                        deviceLoc.lat,
                                        deviceLoc.lon,
                                        CrimeMapEngine.nearestAreaLabel(deviceLoc.lat, deviceLoc.lon),
                                        customMsgDraft
                                    )
                                },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("shield_share_sos_intent_button"),
                                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.ShareLocation, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Alert", color = CyberCyan)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Coordinates: ${String.format(Locale.US, "%.5f, %.5f", deviceLoc.lat, deviceLoc.lon)} " +
                            if (deviceLoc.isLiveGps) "(Live GPS ±${deviceLoc.accuracyMeters.toInt()}m)" else "(Default reference — tap Sync GPS)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (!LocationTracker.hasLocationPermission(context)) {
                        TextButton(
                            onClick = {
                                locPermLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        ) {
                            Text("Grant GPS Permission for Exact Coordinates", color = CyberCyan)
                        }
                    }
                }
            }

            // 2. FAKE CALL & SHAKE-TO-SOS TOOLS
            item {
                CyberCard {
                    SectionHeader(
                        title = "Personal Safety Tools",
                        subtitle = "Discreet Fake Call & Shake Sensor Trigger"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = AmberWarn)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Shake Phone to Trigger SOS", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text("Starts 5s countdown on sharp shake", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = shakeEnabled,
                            onCheckedChange = { viewModel.setShakeToSos(it) },
                            modifier = Modifier.testTag("shake_to_sos_switch")
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { fakeCallCaller = "Amma" },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("fake_call_amma_button"),
                            border = BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fake Call: Amma", color = SafeEmerald)
                        }
                        OutlinedButton(
                            onClick = { fakeCallCaller = "Police Control 112" },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("fake_call_police_button"),
                            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fake Call: Police", color = CyberCyan)
                        }
                    }
                }
            }

            // 3. TRUSTED CONTACTS CRUD
            item {
                SectionHeader(
                    title = "Trusted Emergency Contacts (${trustedContacts.size})",
                    subtitle = "Linked to SOS logs & quick one-tap dialing",
                    trailing = {
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("add_trusted_contact_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan, contentColor = DeepObsidian)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Contact", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (trustedContacts.isEmpty()) {
                item {
                    CyberCard(containerColor = CommandNavy) {
                        Text(
                            text = "No trusted contacts added yet. Add family or close friends for quick calling and SOS logging.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(trustedContacts, key = { it.id }) { contact ->
                    CyberCard(
                        containerColor = CommandNavy,
                        borderColor = if (contact.isPrimary) CyberCyan.copy(alpha = 0.6f) else HairlineBorder
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(contact.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    if (contact.isPrimary) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = CyberCyan.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                "PRIMARY",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CyberCyan,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text("${contact.phone} · ${contact.relationship}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Row {
                                IconButton(
                                    onClick = { dialPhoneNumber(context, contact.phone) },
                                    modifier = Modifier.testTag("call_contact_${contact.id}")
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call ${contact.name}", tint = SafeEmerald)
                                }
                                IconButton(
                                    onClick = { editingContact = contact },
                                    modifier = Modifier.testTag("edit_contact_${contact.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit ${contact.name}", tint = CyberCyan)
                                }
                                IconButton(
                                    onClick = { viewModel.deleteTrustedContact(contact.id) },
                                    modifier = Modifier.testTag("delete_contact_${contact.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete ${contact.name}", tint = CriticalRose)
                                }
                            }
                        }
                    }
                }
            }

            // 4. CUSTOM SOS MESSAGE
            item {
                CyberCard {
                    Text("Custom SOS Alert Message", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customMsgDraft,
                        onValueChange = { customMsgDraft = it },
                        modifier = Modifier.fillMaxWidth().testTag("custom_sos_message_input"),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.updateCustomSosMessage(customMsgDraft) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("save_custom_sos_message_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan, contentColor = DeepObsidian)
                    ) {
                        Text("Save Custom SOS Message", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 5. SOS EVENT HISTORY & STATUS LIFECYCLE
            item {
                SectionHeader(
                    title = "Emergency History (${sosHistory.size})",
                    subtitle = "Cloud & Offline-Queued SOS Audit Trail"
                )
            }

            items(sosHistory, key = { it.id }) { ev ->
                CyberCard(containerColor = CommandNavy) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(ev.locationLabel.ifBlank { "Puducherry" }, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        val badgeColor = when (ev.status) {
                            "resolved" -> SafeEmerald
                            "cancelled" -> AmberWarn
                            "queued_local" -> CyberCyan
                            else -> CriticalRose
                        }
                        Text(ev.status.uppercase(), style = MaterialTheme.typography.labelSmall, color = badgeColor)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "Trigger: %s · %.4f, %.4f", ev.triggerType, ev.lat, ev.lon),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(ev.deliveryNote, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }

    if (showAddDialog || editingContact != null) {
        TrustedContactDialog(
            initial = editingContact,
            onSave = { name, phone, rel, primary ->
                val cur = editingContact
                if (cur == null) {
                    viewModel.addTrustedContact(name, phone, rel, primary)
                } else {
                    viewModel.updateTrustedContact(
                        cur.copy(name = name, phone = phone, relationship = rel, isPrimary = primary)
                    )
                }
                showAddDialog = false
                editingContact = null
            },
            onDismiss = {
                showAddDialog = false
                editingContact = null
            }
        )
    }

    fakeCallCaller?.let { callerName ->
        FakeCallOverlayDialog(
            callerName = callerName,
            onDismiss = { fakeCallCaller = null }
        )
    }
}

@Composable
private fun TrustedContactDialog(
    initial: TrustedContact?,
    onSave: (String, String, String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var relationship by remember { mutableStateOf(initial?.relationship ?: "Family") }
    var isPrimary by remember { mutableStateOf(initial?.isPrimary ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PanelNavy,
        title = { Text(if (initial == null) "Add Trusted Contact" else "Edit Contact", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("contact_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("contact_phone_input")
                )
                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship (Family / Friend)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("contact_relationship_input")
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPrimary, onCheckedChange = { isPrimary = it })
                    Text("Set as Primary Emergency Contact", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, relationship, isPrimary) },
                modifier = Modifier.testTag("save_contact_dialog_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan, contentColor = DeepObsidian)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun FakeCallOverlayDialog(
    callerName: String,
    onDismiss: () -> Unit
) {
    var answered by remember { mutableStateOf(false) }
    var elapsedSec by remember { mutableStateOf(0) }

    LaunchedEffect(answered) {
        if (answered) {
            while (true) {
                delay(1000L)
                elapsedSec += 1
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF070C14)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = if (answered) {
                            String.format(Locale.US, "%02d:%02d · Simulated Call Active", elapsedSec / 60, elapsedSec % 60)
                        } else {
                            "Incoming Call..."
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = CircleShape,
                        color = ShieldCyan.copy(alpha = 0.2f),
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(callerName.take(1), style = MaterialTheme.typography.displaySmall, color = ShieldCyan)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(callerName, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    Text("Mobile · India", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp).testTag("fake_call_decline_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CriticalRose)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Fake Call", tint = Color.White)
                    }
                    if (!answered) {
                        Button(
                            onClick = { answered = true },
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp).testTag("fake_call_answer_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Answer Fake Call", tint = DeepObsidian)
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.GoogleAuthManager
import com.example.ui.components.CyberCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.CommandNavy
import com.example.ui.theme.CriticalRose
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.HairlineBright
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    displayName: String,
    email: String?,
    viewModel: MainViewModel,
    onOpenShieldSos: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val shakeEnabled by viewModel.shakeToSosEnabled.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val badges by viewModel.earnedBadges.collectAsStateWithLifecycle()
    val sosHistory by viewModel.mergedSosHistory.collectAsStateWithLifecycle()

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
            item {
                CyberCard(borderColor = HairlineBright) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ShieldCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = "User Profile", tint = ShieldCyan, modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(displayName.ifBlank { "Guardian User" }, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                            if (!email.isNullOrBlank()) {
                                Text(email, style = MaterialTheme.typography.bodySmall, color = CyberCyan)
                            }
                            Text("Sector: $selectedCity · ${badges.size} Badges · ${sosHistory.size} SOS Logs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                GoogleAuthManager.signOut(context as? Activity ?: context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("sign_out_button"),
                        border = BorderStroke(1.dp, CriticalRose.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CriticalRose)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out")
                    }
                }
            }

            item {
                CyberCard {
                    SectionHeader("Safety & Localization Settings")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Language: ${language.displayLabel}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text("Switch between English & Tamil (தமிழ்)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = language == AppLanguage.TA,
                            onCheckedChange = { ta -> viewModel.setLanguage(if (ta) AppLanguage.TA else AppLanguage.EN) },
                            modifier = Modifier.testTag("profile_language_switch")
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Shake-to-SOS Trigger", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text("Automatically initiates 5s SOS countdown on sharp shake", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = shakeEnabled,
                            onCheckedChange = { viewModel.setShakeToSos(it) },
                            modifier = Modifier.testTag("profile_shake_switch")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onOpenShieldSos) {
                        Text("Manage Trusted Contacts & Emergency History →", color = CyberCyan)
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Command Notification Feed (${notifications.size})",
                    trailing = {
                        if (notifications.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearNotifications() }) {
                                Text("Clear", color = AmberWarn)
                            }
                        }
                    }
                )
            }

            if (notifications.isEmpty()) {
                item {
                    CyberCard(containerColor = CommandNavy) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = TextSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("No active notifications in feed.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(notifications, key = { it.id }) { n ->
                    val col = when (n.level) {
                        "red" -> CriticalRose
                        "gold" -> AmberWarn
                        "green" -> SafeEmerald
                        else -> CyberCyan
                    }
                    CyberCard(containerColor = CommandNavy, borderColor = col.copy(alpha = 0.45f)) {
                        Text(n.message, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }

            item {
                CyberCard(containerColor = CommandNavy) {
                    Text("Transparency & Architecture Disclosure", style = MaterialTheme.typography.titleSmall, color = AmberWarn)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = AppStrings.disclaimerSimulation(language) +
                            "\n\nEmergency SMS/WhatsApp dispatch and live municipal CCTV streams require external carrier/government gateways; use the built-in 112 Dialer and Share Location tools for immediate response.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

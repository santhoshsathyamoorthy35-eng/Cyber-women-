package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.dialPhoneNumber
import com.example.ui.theme.CommandNavy
import com.example.ui.theme.CriticalRose
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.PanelNavy
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MeenaAiViewModel
import com.example.util.AppLanguage

@Composable
fun MeenaAiScreen(
    mainViewModel: MainViewModel,
    meenaViewModel: MeenaAiViewModel,
    onBack: () -> Unit,
    onOpenShieldSos: () -> Unit
) {
    val context = LocalContext.current
    val language by mainViewModel.language.collectAsStateWithLifecycle()
    val selectedCity by mainViewModel.selectedCity.collectAsStateWithLifecycle()
    val shieldZones by mainViewModel.shieldZones.collectAsStateWithLifecycle()
    val messages by meenaViewModel.messages.collectAsStateWithLifecycle()
    val isStreaming by meenaViewModel.isStreaming.collectAsStateWithLifecycle()
    val dangerActive by meenaViewModel.dangerAlertActive.collectAsStateWithLifecycle()
    val errorBanner by meenaViewModel.errorBanner.collectAsStateWithLifecycle()

    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(language) {
        meenaViewModel.ensureWelcome(language)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val topRiskSummary = remember(shieldZones) {
        val top = shieldZones.firstOrNull()
        if (top != null) "${top.areaName}: ${top.riskLevel.label} (${top.riskScore}/100) during ${top.activeTimeWindow}"
        else "Normal monitoring"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .imePadding()
    ) {
        Surface(
            color = CommandNavy,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("meena_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Image(
                        painter = painterResource(id = R.drawable.img_meena_avatar_1794777566719),
                        contentDescription = "Meena AI Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(38.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Meena AI Companion", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("Gemini 3.8 Flash · Bilingual Safety & Rights", style = MaterialTheme.typography.labelSmall, color = CyberPurple)
                    }
                }
                FilterChip(
                    selected = language == AppLanguage.TA,
                    onClick = {
                        val next = if (language == AppLanguage.EN) AppLanguage.TA else AppLanguage.EN
                        mainViewModel.setLanguage(next)
                    },
                    label = { Text(if (language == AppLanguage.EN) "EN" else "தமிழ்") },
                    modifier = Modifier.testTag("meena_language_chip")
                )
            }
        }

        if (dangerActive) {
            Surface(
                color = Color(0xFF3E0A16),
                border = BorderStroke(1.dp, CriticalRose),
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("IMMEDIATE SAFETY ESCALATION", style = MaterialTheme.typography.labelMedium, color = CriticalRose)
                    Text("If you are in danger right now, dial 112 or activate Shield SOS immediately.", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { dialPhoneNumber(context, "112") },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("meena_danger_112_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CriticalRose)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call 112", color = Color.White)
                        }
                        OutlinedButton(
                            onClick = onOpenShieldSos,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("meena_danger_sos_button"),
                            border = BorderStroke(1.dp, CriticalRose)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = CriticalRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shield SOS", color = TextPrimary)
                        }
                    }
                }
            }
        }

        if (!errorBanner.isNullOrBlank()) {
            Surface(
                color = PanelNavy,
                border = BorderStroke(1.dp, CriticalRose.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(errorBanner!!, style = MaterialTheme.typography.bodySmall, color = CriticalRose, modifier = Modifier.weight(1f))
                    IconButton(onClick = { meenaViewModel.retryLastPrompt() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry Meena AI", tint = CyberCyan)
                    }
                }
            }
        }

        val quickPrompts = if (language == AppLanguage.TA) {
            listOf(
                "புதுச்சேரி மகளிர் உதவி எண்கள் என்ன?",
                "POCSO சட்டம் 2012 உரிமைகள்",
                "சைபர் மிரட்டலை 1930-ல் புகார் செய்வது எப்படி?"
            )
        } else {
            listOf(
                "What are my rights under Domestic Violence Act?",
                "Puducherry women police helplines",
                "Explain the current Shield Protocol risk advisory",
                "How do I report online harassment to 1930?"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { qp ->
                FilterChip(
                    selected = false,
                    onClick = { meenaViewModel.sendMessage(qp, language, selectedCity, topRiskSummary) },
                    label = { Text(qp) }
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().widthIn(max = 820.dp).align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            msg.isUser -> ShieldCyan.copy(alpha = 0.22f)
                            msg.isError -> CriticalRose.copy(alpha = 0.14f)
                            else -> PanelNavy
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                msg.isUser -> ShieldCyan.copy(alpha = 0.5f)
                                msg.isError -> CriticalRose.copy(alpha = 0.5f)
                                else -> HairlineBorder
                            }
                        ),
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!msg.isUser) {
                                Text("MEENA", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = msg.text.ifBlank { "..." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (msg.isError) CriticalRose else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Surface(
            color = CommandNavy,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Ask Meena about safety, laws, or helplines...") },
                    modifier = Modifier.weight(1f).testTag("meena_chat_input"),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val txt = draft
                        draft = ""
                        meenaViewModel.sendMessage(txt, language, selectedCity, topRiskSummary)
                    },
                    enabled = !isStreaming && draft.isNotBlank(),
                    modifier = Modifier.size(52.dp).testTag("meena_send_button"),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan, contentColor = DeepObsidian)
                ) {
                    if (isStreaming) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DeepObsidian, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send to Meena AI")
                    }
                }
            }
        }
    }
}

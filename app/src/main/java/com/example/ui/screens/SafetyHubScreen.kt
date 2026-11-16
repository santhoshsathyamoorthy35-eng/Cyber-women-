package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HelplineRegion
import com.example.data.model.LearningModule
import com.example.domain.CrimeMapEngine
import com.example.domain.SafeHerCatalog
import com.example.ui.components.CyberCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.dialPhoneNumber
import com.example.ui.components.shareLocationIntent
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.CommandNavy
import com.example.ui.theme.CriticalRose
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HairlineBright
import com.example.ui.theme.PanelNavy
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.util.AppLanguage
import java.util.Locale

private enum class HubTab(val label: String) {
    HELPLINES("Helplines"),
    ORGS("Orgs & NGOs"),
    LEARN("Learn & Quiz"),
    LAWS("Laws & Stories"),
    TOOLS("Checklist & Tools")
}

@Composable
fun SafetyHubScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val earnedBadges by viewModel.earnedBadges.collectAsStateWithLifecycle()
    val checklistItems by viewModel.checklistItems.collectAsStateWithLifecycle()
    val deviceLoc by viewModel.deviceLocation.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(HubTab.HELPLINES) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf<HelplineRegion?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    var orgFilter by remember { mutableStateOf("All") }
    var openModule by remember { mutableStateOf<LearningModule?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        Surface(
            color = CommandNavy,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text("SAFEHER · SAFETY HUB & EMPOWERMENT", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("Verified National, Puducherry & Tamil Nadu Directories · Legal Rights · Training", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HubTab.entries.forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag("hub_tab_${tab.name}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ShieldCyan.copy(alpha = 0.24f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                HubTab.HELPLINES -> {
                    val categories = remember {
                        listOf("All") + SafeHerCatalog.HELPLINES.map { it.category }.distinct()
                    }
                    val filtered = remember(searchQuery, selectedRegion, selectedCategory) {
                        SafeHerCatalog.HELPLINES.filter { h ->
                            (selectedRegion == null || h.region == selectedRegion) &&
                                (selectedCategory == "All" || h.category == selectedCategory) &&
                                (searchQuery.isBlank() ||
                                    h.name.contains(searchQuery, ignoreCase = true) ||
                                    h.number.contains(searchQuery, ignoreCase = true) ||
                                    h.description.contains(searchQuery, ignoreCase = true))
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().widthIn(max = 840.dp).align(Alignment.TopCenter),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Search helplines, Protection Officers, Sakhi OSCs...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("helplines_search_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedRegion == null,
                                    onClick = { selectedRegion = null },
                                    label = { Text("All Regions (${SafeHerCatalog.HELPLINES.size})") }
                                )
                                HelplineRegion.entries.forEach { reg ->
                                    FilterChip(
                                        selected = selectedRegion == reg,
                                        onClick = { selectedRegion = reg },
                                        label = { Text(if (language == AppLanguage.TA) reg.labelTa else reg.labelEn) },
                                        modifier = Modifier.testTag("helpline_region_${reg.name}")
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categories.forEach { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }

                        items(filtered, key = { it.id }) { h ->
                            CyberCard(containerColor = CommandNavy) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(h.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, modifier = Modifier.weight(1f, fill = false))
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${h.region.labelEn} · ${h.category}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyberCyan
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(h.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { dialPhoneNumber(context, h.number) },
                                        modifier = Modifier.heightIn(min = 48.dp).testTag("dial_helpline_${h.id}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (h.isEmergencyStrip) CriticalRose else ShieldCyan,
                                            contentColor = if (h.isEmergencyStrip) Color.White else DeepObsidian
                                        )
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call ${h.number}", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(h.number.take(12), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                HubTab.ORGS -> {
                    val filteredOrgs = remember(orgFilter, searchQuery) {
                        SafeHerCatalog.ORGANIZATIONS.filter { org ->
                            (orgFilter == "All" || org.badge.equals(orgFilter, ignoreCase = true)) &&
                                (searchQuery.isBlank() || org.name.contains(searchQuery, ignoreCase = true) || org.location.contains(searchQuery, ignoreCase = true))
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().widthIn(max = 840.dp).align(Alignment.TopCenter),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Search Gov departments & NGOs...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("All", "Gov", "NGO").forEach { tag ->
                                    FilterChip(selected = orgFilter == tag, onClick = { orgFilter = tag }, label = { Text(tag) })
                                }
                            }
                        }

                        items(filteredOrgs, key = { it.id }) { org ->
                            CyberCard(containerColor = CommandNavy) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(org.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, modifier = Modifier.weight(1f))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (org.badge == "Gov") ShieldCyan.copy(alpha = 0.2f) else SafeEmerald.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${org.badge} · ${org.region.labelEn.substringBefore(" (")}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (org.badge == "Gov") CyberCyan else SafeEmerald,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(org.location, style = MaterialTheme.typography.labelMedium, color = AmberWarn)
                                Text(org.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }

                HubTab.LEARN -> {
                    val badgeIds = earnedBadges.map { it.badgeId }.toSet()
                    var quizIndex by remember { mutableIntStateOf(0) }
                    var quizScore by remember { mutableIntStateOf(0) }
                    var quizFeedback by remember { mutableStateOf<String?>(null) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().widthIn(max = 840.dp).align(Alignment.TopCenter),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            CyberCard(borderColor = SafeEmerald.copy(alpha = 0.5f)) {
                                Text("Earned Empowerment Badges (${badgeIds.size} / 7)", style = MaterialTheme.typography.titleMedium, color = SafeEmerald)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SafeHerCatalog.LEARNING_MODULES.forEach { m ->
                                        val earned = m.badgeId in badgeIds
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (earned) SafeEmerald.copy(alpha = 0.2f) else CommandNavy,
                                            border = BorderStroke(1.dp, if (earned) SafeEmerald else HairlineBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(m.emoji)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    m.tag,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (earned) SafeEmerald else TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            SectionHeader("Interactive Learning Modules", "Tap any lesson to explore visual diagrams & safety rules")
                        }

                        items(SafeHerCatalog.LEARNING_MODULES, key = { it.id }) { module ->
                            val isEarned = module.badgeId in badgeIds
                            CyberCard(
                                containerColor = CommandNavy,
                                borderColor = if (isEarned) SafeEmerald.copy(alpha = 0.55f) else HairlineBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { openModule = module }
                                    .testTag("learning_module_${module.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(module.emoji, style = MaterialTheme.typography.headlineMedium)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (language == AppLanguage.TA) module.titleTa else module.titleEn,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary
                                        )
                                        Text(module.shortDescEn, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    if (isEarned) {
                                        Icon(Icons.Default.Verified, contentDescription = "Completed", tint = SafeEmerald)
                                    }
                                }
                            }
                        }

                        // INTERACTIVE QUIZ
                        item {
                            val questions = SafeHerCatalog.QUIZ_QUESTIONS
                            CyberCard(borderColor = CyberPurple.copy(alpha = 0.6f)) {
                                Text("Safety Knowledge Quiz (Good Touch / Bad Touch / Cyber)", style = MaterialTheme.typography.titleMedium, color = CyberCyan)
                                Spacer(modifier = Modifier.height(6.dp))
                                if (quizIndex < questions.size) {
                                    val q = questions[quizIndex]
                                    Text("Question ${quizIndex + 1} of ${questions.size} · Score: $quizScore", style = MaterialTheme.typography.labelMedium, color = AmberWarn)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${q.sceneEmoji}  ${if (language == AppLanguage.TA) q.questionTa else q.questionEn}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Good Touch", "Bad Touch", "Not Safe").forEach { choice ->
                                            OutlinedButton(
                                                onClick = {
                                                    val correct = choice == q.correctLabel
                                                    if (correct) quizScore += 1
                                                    quizFeedback = (if (correct) "Correct! " else "Review: ") + q.explanationEn
                                                },
                                                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("quiz_choice_${choice.replace(" ", "_")}"),
                                                contentPadding = PaddingValues(4.dp)
                                            ) {
                                                Text(choice, style = MaterialTheme.typography.labelSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                    quizFeedback?.let { fb ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(fb, style = MaterialTheme.typography.bodySmall, color = SafeEmerald)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                quizFeedback = null
                                                val next = quizIndex + 1
                                                quizIndex = next
                                                if (next >= questions.size && quizScore >= 5) {
                                                    viewModel.earnBadge("badge_quiz_master")
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                        ) {
                                            Text(if (quizIndex == questions.lastIndex) "Finish Quiz" else "Next Question")
                                        }
                                    }
                                } else {
                                    Text("Quiz Completed! Final Score: $quizScore / ${questions.size}", style = MaterialTheme.typography.titleMedium, color = SafeEmerald)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            quizIndex = 0
                                            quizScore = 0
                                            quizFeedback = null
                                        }
                                    ) {
                                        Text("Retake Quiz")
                                    }
                                }
                            }
                        }
                    }
                }

                HubTab.LAWS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().widthIn(max = 840.dp).align(Alignment.TopCenter),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { SectionHeader("Know Your Legal Rights (India / TN / Puducherry)") }
                        items(SafeHerCatalog.LEGAL_RIGHTS, key = { it.id }) { right ->
                            CyberCard(containerColor = CommandNavy) {
                                Text(right.title, style = MaterialTheme.typography.titleSmall, color = CyberCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(right.summary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                right.quickHelpline?.let { num ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(onClick = { dialPhoneNumber(context, num) }) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp), tint = SafeEmerald)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Helpline: $num", color = SafeEmerald)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            SectionHeader("Real-World Awareness Case Studies")
                        }
                        items(SafeHerCatalog.CASE_STUDIES, key = { it.id }) { cs ->
                            CyberCard(containerColor = PanelNavy) {
                                Text(cs.tag, style = MaterialTheme.typography.labelSmall, color = AmberWarn)
                                Text(cs.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(cs.summary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }

                HubTab.TOOLS -> {
                    val checklistEntries = listOf(
                        "chk_112" to "Saved 112 & 181 on speed dial",
                        "chk_contacts" to "Added at least 2 trusted contacts in Shield SOS",
                        "chk_share" to "Tested sharing live GPS link with family",
                        "chk_pants" to "Reviewed PANTS & Swimsuit safety rules",
                        "chk_cyber" to "Enabled 2FA on phone & social accounts"
                    )
                    var feedbackText by remember { mutableStateOf("") }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().widthIn(max = 840.dp).align(Alignment.TopCenter),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            CyberCard {
                                SectionHeader("Personal Safety Checklist")
                                Spacer(modifier = Modifier.height(6.dp))
                                checklistEntries.forEach { (id, label) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleChecklistItem(id) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = id in checklistItems,
                                            onCheckedChange = { viewModel.toggleChecklistItem(id) }
                                        )
                                        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        item {
                            CyberCard(containerColor = CommandNavy) {
                                Text("Share Current Safety Location", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text(
                                    text = String.format(Locale.US, "%.5f, %.5f", deviceLoc.lat, deviceLoc.lon),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberCyan
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        shareLocationIntent(
                                            context,
                                            deviceLoc.lat,
                                            deviceLoc.lon,
                                            CrimeMapEngine.nearestAreaLabel(deviceLoc.lat, deviceLoc.lon)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                ) {
                                    Text("Share Location Link")
                                }
                            }
                        }

                        item {
                            CyberCard {
                                Text("Send Feedback to CYBER WOMEN", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = feedbackText,
                                    onValueChange = { feedbackText = it },
                                    label = { Text("Suggestions, missing helplines, or safety feedback") },
                                    modifier = Modifier.fillMaxWidth().testTag("feedback_message_input"),
                                    minLines = 2
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.submitFeedback(feedbackText) { ok ->
                                            if (ok) feedbackText = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("submit_feedback_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan, contentColor = DeepObsidian)
                                ) {
                                    Text("Submit Feedback", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    openModule?.let { mod ->
        LearningModuleSheet(
            module = mod,
            language = language,
            onMarkCompleted = {
                viewModel.earnBadge(mod.badgeId)
                openModule = null
            },
            onDismiss = { openModule = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearningModuleSheet(
    module: LearningModule,
    language: AppLanguage,
    onMarkCompleted: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PanelNavy) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${module.emoji} ${if (language == AppLanguage.TA) module.titleTa else module.titleEn}",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            SafetyModuleDiagram(diagramType = module.diagramType)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CommandNavy,
                border = BorderStroke(1.dp, HairlineBright)
            ) {
                Text(
                    text = "Meena Tip: ${if (language == AppLanguage.TA) module.meenaTipTa else module.meenaTipEn}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyberCyan,
                    modifier = Modifier.padding(12.dp)
                )
            }
            module.keyPointsEn.forEach { pt ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(pt, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AmberWarn.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, AmberWarn.copy(alpha = 0.5f))
            ) {
                Text(module.rememberBox, style = MaterialTheme.typography.bodySmall, color = AmberWarn, modifier = Modifier.padding(12.dp))
            }
            Button(
                onClick = onMarkCompleted,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("complete_module_badge_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SafeEmerald, contentColor = DeepObsidian)
            ) {
                Text("Complete Lesson & Earn Badge", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

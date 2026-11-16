package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.data.model.ShieldRiskLevel
import com.example.domain.SafeHerCatalog
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.CommandNavy
import com.example.ui.theme.CriticalRose
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HairlineBright
import com.example.ui.theme.HighOrange
import com.example.ui.theme.PanelNavy
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppLanguage
import java.util.Locale

fun dialPhoneNumber(context: Context, rawNumber: String) {
    val firstNumber = rawNumber.split("/").firstOrNull()?.trim() ?: rawNumber
    val cleaned = firstNumber.replace(Regex("[^0-9+]"), "")
    if (cleaned.isBlank()) {
        Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_DIAL, "tel:$cleaned".toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No phone dialer app available", Toast.LENGTH_SHORT).show()
    }
}

fun shareLocationIntent(context: Context, lat: Double, lon: Double, label: String, customPrefix: String = "") {
    val mapsUrl = String.format(Locale.US, "https://maps.google.com/?q=%.5f,%.5f", lat, lon)
    val text = buildString {
        if (customPrefix.isNotBlank()) {
            append(customPrefix.trim())
            append("\n\n")
        }
        append("CYBER WOMEN Shield Location Update ($label)\n")
        append(String.format(Locale.US, "Coordinates: %.5f, %.5f\n", lat, lon))
        append("Map: $mapsUrl")
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "CYBER WOMEN Safety Location")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(sendIntent, "Share Live Location")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

fun triggerQuickExit(context: Context) {
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(browserIntent)
    } catch (_: Exception) {
        // Best-effort exit to neutral browser
    }
}

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = HairlineBorder,
    containerColor: Color = PanelNavy,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
fun RiskPillBadge(
    level: ShieldRiskLevel,
    score: Int? = null,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (level) {
        ShieldRiskLevel.CRITICAL -> Color(0x33FF3B5C) to CriticalRose
        ShieldRiskLevel.HIGH -> Color(0x33FF7A3C) to HighOrange
        ShieldRiskLevel.MODERATE -> Color(0x33FFCC4D) to AmberWarn
        ShieldRiskLevel.LOW -> Color(0x3326C281) to SafeEmerald
        ShieldRiskLevel.INSUFFICIENT_DATA -> Color(0x338EA0C4) to TextSecondary
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.55f))
    ) {
        val text = if (score != null && level != ShieldRiskLevel.INSUFFICIENT_DATA) {
            "${level.label} · $score/100"
        } else {
            level.label
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmergencyDialStrip(
    context: Context,
    modifier: Modifier = Modifier
) {
    val emergencyList = SafeHerCatalog.HELPLINES.filter { it.isEmergencyStrip }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        emergencyList.forEach { item ->
            val isTopEmergency = item.number == "112" || item.number == "181"
            val accent = if (isTopEmergency) CriticalRose else CyberCyan
            Surface(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { dialPhoneNumber(context, item.number) }
                    .testTag("emergency_strip_${item.number}"),
                shape = RoundedCornerShape(12.dp),
                color = if (isTopEmergency) CriticalRose.copy(alpha = 0.16f) else CommandNavy,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Dial ${item.number}",
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = item.number,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.name.substringBefore(" (").take(18),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickExitAndLangBar(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onQuickExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onToggleLanguage)
                .testTag("language_toggle_button"),
            shape = RoundedCornerShape(999.dp),
            color = PanelNavy,
            border = BorderStroke(1.dp, HairlineBright)
        ) {
            Text(
                text = if (language == AppLanguage.EN) "EN / தமிழ்" else "தமிழ் / EN",
                style = MaterialTheme.typography.labelMedium,
                color = CyberCyan,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        FilledTonalButton(
            onClick = onQuickExit,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("quick_exit_button"),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = CriticalRose.copy(alpha = 0.2f),
                contentColor = CriticalRose
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Quick Exit to safe page",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Quick Exit",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun BrandShieldBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(ShieldCyan, CyberCyan)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "CYBER WOMEN Emblem",
            tint = CommandNavy,
            modifier = Modifier.size(20.dp)
        )
    }
}

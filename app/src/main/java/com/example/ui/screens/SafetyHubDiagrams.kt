package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.CommandNavy
import com.example.ui.theme.CriticalRose
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.SoftPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SafetyModuleDiagram(diagramType: String, modifier: Modifier = Modifier) {
    when (diagramType) {
        "swimsuit" -> SwimsuitRuleDiagram(modifier)
        "pants" -> PantsFiveCardsDiagram(modifier)
        "cyber" -> CyberShieldDiagram(modifier)
        "circle" -> SafetyCircleDiagram(modifier)
        else -> BoundariesBannerDiagram(diagramType, modifier)
    }
}

@Composable
private fun SwimsuitRuleDiagram(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CommandNavy,
        border = BorderStroke(1.dp, SoftPink.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Swimsuit Private Boundary Rule", style = MaterialTheme.typography.labelMedium, color = SoftPink)
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                val cx = size.width / 2f
                // Head
                drawCircle(color = CyberCyan, radius = 16f, center = Offset(cx, 24f), style = Stroke(3f))
                // Torso covered by swimsuit zone
                drawRoundRect(
                    color = SoftPink.copy(alpha = 0.25f),
                    topLeft = Offset(cx - 26f, 44f),
                    size = Size(52f, 46f)
                )
                drawRoundRect(
                    color = SoftPink,
                    topLeft = Offset(cx - 26f, 44f),
                    size = Size(52f, 46f),
                    style = Stroke(2.5f)
                )
                // Arms (safe high-five / pat zone)
                drawLine(SafeEmerald, Offset(cx - 26f, 48f), Offset(cx - 56f, 70f), strokeWidth = 4f)
                drawLine(SafeEmerald, Offset(cx + 26f, 48f), Offset(cx + 56f, 70f), strokeWidth = 4f)
            }
            Text(
                text = "Pink Zone = Swimsuit Private Areas · Say NO, Move Away, Tell a Trusted Adult",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PantsFiveCardsDiagram(modifier: Modifier = Modifier) {
    val letters = listOf(
        "P" to "Privates are Private",
        "A" to "Always Yours",
        "N" to "No Means No",
        "T" to "Talk About Secrets",
        "S" to "Speak Up (1098)"
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        letters.forEach { (ch, label) ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = CommandNavy,
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(ch, style = MaterialTheme.typography.titleLarge, color = CyberCyan, fontWeight = FontWeight.Bold)
                    Text(label, style = MaterialTheme.typography.labelSmall, color = TextPrimary, maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun CyberShieldDiagram(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CommandNavy,
        border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Golden Hour Cyber Shield · Dial 1930", style = MaterialTheme.typography.labelMedium, color = CyberPurple)
            Spacer(modifier = Modifier.height(6.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                val cx = size.width / 2f
                drawRoundRect(
                    color = ShieldCyan,
                    topLeft = Offset(cx - 28f, 10f),
                    size = Size(56f, 70f),
                    style = Stroke(3f)
                )
                drawCircle(color = SafeEmerald.copy(alpha = 0.25f), radius = 38f, center = Offset(cx, 45f))
                drawCircle(color = SafeEmerald, radius = 38f, center = Offset(cx, 45f), style = Stroke(2f))
            }
            Text("Screenshot Evidence · Block Stranger · Never Share OTP or Address", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun SafetyCircleDiagram(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CommandNavy,
        border = BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("5-Person Trusted Safety Circle", style = MaterialTheme.typography.labelMedium, color = SafeEmerald)
            Spacer(modifier = Modifier.height(6.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(96.dp)) {
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(color = ShieldCyan.copy(alpha = 0.25f), radius = 42f, center = c)
                drawCircle(color = CyberCyan, radius = 26f, center = c, style = Stroke(2.5f))
                drawCircle(color = SafeEmerald, radius = 10f, center = c)
            }
            Text("Parents · Grandparents · Teacher/Counsellor · Police (112/1098)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun BoundariesBannerDiagram(type: String, modifier: Modifier = Modifier) {
    val accent = if (type == "consent") AmberWarn else CriticalRose
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CommandNavy,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (type == "consent") "NO · GO · TELL Protocol" else "SAFE vs UNSAFE Secrets",
                style = MaterialTheme.typography.labelMedium,
                color = accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (type == "consent") {
                    "1. Say NO clearly  2. Go to a safe lit place  3. Tell a trusted adult right away."
                } else {
                    "Threats or secrets that make you feel scared must ALWAYS be shared with a trusted adult."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
        }
    }
}

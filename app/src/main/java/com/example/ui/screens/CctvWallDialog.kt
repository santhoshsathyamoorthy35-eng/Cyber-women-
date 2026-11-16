package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CrimeMapFilters
import com.example.data.model.DetectionStatus
import com.example.data.model.LiveDetection
import com.example.data.model.MapLayersState
import com.example.domain.CrimeMapEngine
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
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CctvWallDialog(
    detections: List<LiveDetection>,
    onAcknowledge: (String) -> Unit,
    onDispatch: (String) -> Unit,
    onSpawnTest: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val activeCams = listOf("CAM-01", "CAM-03", "CAM-05", "CAM-07")
    val camSpecs = activeCams.mapNotNull { id -> CrimeMapEngine.CAMERAS.find { it.id == id } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = CommandNavy,
            border = BorderStroke(1.dp, HairlineBright)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = CyberCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("4-UP CCTV MATRIX", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text("SIMULATED AI FEED · DEMO OVERLAY", style = MaterialTheme.typography.labelSmall, color = AmberWarn)
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_cctv_wall_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close CCTV Matrix", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                camSpecs.chunked(2).forEach { rowCams ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCams.forEach { cam ->
                            val activeDet = detections.firstOrNull {
                                it.camId == cam.id && it.status != DetectionStatus.RESOLVED
                            }
                            CctvTileCard(
                                camId = cam.id,
                                camName = cam.name,
                                detection = activeDet,
                                onAck = { activeDet?.let { onAcknowledge(it.id) } },
                                onDispatch = { activeDet?.let { onDispatch(it.id) } },
                                onSimulate = { onSpawnTest(cam.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CctvTileCard(
    camId: String,
    camName: String,
    detection: LiveDetection?,
    onAck: () -> Unit,
    onDispatch: () -> Unit,
    onSimulate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "cctv_$camId")
    val animPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animPhase_$camId"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = DeepObsidian,
        border = BorderStroke(
            1.dp,
            if (detection != null) Color(detection.colorHex) else HairlineBorder
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(camId, style = MaterialTheme.typography.labelMedium, color = CyberCyan)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(CriticalRose)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("REC SIM", style = MaterialTheme.typography.labelSmall, color = CriticalRose)
                }
            }
            Text(
                text = camName,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF07111F))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Simulated night-vision street scene + scanline
                    for (y in 0 until size.height.toInt() step 8) {
                        drawLine(
                            color = CyberCyan.copy(alpha = 0.06f),
                            start = Offset(0f, y.toFloat()),
                            end = Offset(size.width, y.toFloat()),
                            strokeWidth = 1f
                        )
                    }
                    // Moving pedestrian/vehicle bounding boxes
                    val px = (0.2f + 0.55f * ((animPhase + (camId.hashCode() and 7) * 0.11f) % 1f)) * size.width
                    val py = size.height * 0.48f + sin(animPhase * 6.28f) * 8f
                    drawRect(
                        color = SafeEmerald.copy(alpha = 0.7f),
                        topLeft = Offset(px, py),
                        size = Size(24f, 34f),
                        style = Stroke(1.5f)
                    )
                    if (detection != null) {
                        val detColor = Color(detection.colorHex)
                        val dx = size.width * 0.45f + cos(animPhase * 6.28f) * 14f
                        val dy = size.height * 0.32f
                        drawRect(
                            color = detColor,
                            topLeft = Offset(dx, dy),
                            size = Size(46f, 44f),
                            style = Stroke(2.5f)
                        )
                    }
                    val scanY = animPhase * size.height
                    drawLine(
                        color = CyberCyan.copy(alpha = 0.35f),
                        start = Offset(0f, scanY),
                        end = Offset(size.width, scanY),
                        strokeWidth = 2f
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            if (detection != null) {
                Text(
                    text = "${detection.type} (${(detection.confidence * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(detection.colorHex),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = onAck,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                    ) {
                        Text("Ack", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    }
                    Button(
                        onClick = onDispatch,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CriticalRose)
                    ) {
                        Text("Dispatch", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onSimulate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp)
                        .testTag("cctv_simulate_$camId"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(6.dp)
                ) {
                    Text("Simulate Event", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrimeFilterBottomSheet(
    filters: CrimeMapFilters,
    onUpdateFilters: ((CrimeMapFilters) -> CrimeMapFilters) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val windows = listOf(7, 30, 90, 180, 365)
    val crimeTypes = listOf("all") + CrimeMapEngine.CRIME_WEIGHTS.keys.toList()
    val stations = listOf("all") + CrimeMapEngine.STATIONS.map { it.name }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PanelNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text("Filters · Historical Crime Dataset", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Time Window", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                windows.forEach { d ->
                    FilterChip(
                        selected = filters.daysWindow == d,
                        onClick = { onUpdateFilters { it.copy(daysWindow = d) } },
                        label = { Text("${d}d") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Minimum Severity: ${filters.minSeverity}/5", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
            Slider(
                value = filters.minSeverity.toFloat(),
                onValueChange = { v -> onUpdateFilters { it.copy(minSeverity = v.roundToInt()) } },
                valueRange = 1f..5f,
                steps = 3
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Crime Category", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
            crimeTypes.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { ct ->
                        FilterChip(
                            selected = filters.crimeType == ct,
                            onClick = { onUpdateFilters { it.copy(crimeType = ct) } },
                            label = { Text(if (ct == "all") "All" else ct, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Police Station Jurisdiction", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
            stations.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { st ->
                        FilterChip(
                            selected = filters.station == st,
                            onClick = { onUpdateFilters { it.copy(station = st) } },
                            label = { Text(if (st == "all") "All Stations" else st.replace(" PS", ""), maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) {
                    Text("Reset", color = TextPrimary)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan, contentColor = DeepObsidian)
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapLayersBottomSheet(
    layers: MapLayersState,
    onUpdateLayers: ((MapLayersState) -> MapLayersState) -> Unit,
    onDismiss: () -> Unit
) {
    val entries = listOf(
        Triple("Shield Time-Risk Zones", layers.shieldTimeZones) { l: MapLayersState, v: Boolean -> l.copy(shieldTimeZones = v) },
        Triple("3D Risk Towers (Hex)", layers.riskTowers3D) { l: MapLayersState, v: Boolean -> l.copy(riskTowers3D = v) },
        Triple("Vertical Data Beams", layers.dataBeams) { l: MapLayersState, v: Boolean -> l.copy(dataBeams = v) },
        Triple("Crime Heatmap", layers.heatmap) { l: MapLayersState, v: Boolean -> l.copy(heatmap = v) },
        Triple("Incident Points", layers.incidentPoints) { l: MapLayersState, v: Boolean -> l.copy(incidentPoints = v) },
        Triple("Hotspot Clusters", layers.hotspotClusters) { l: MapLayersState, v: Boolean -> l.copy(hotspotClusters = v) },
        Triple("7-Day Risk Grid", layers.predictedRisk7Day) { l: MapLayersState, v: Boolean -> l.copy(predictedRisk7Day = v) },
        Triple("Emerging Surge Alerts", layers.emergingAlerts) { l: MapLayersState, v: Boolean -> l.copy(emergingAlerts = v) },
        Triple("24 CCTV Cameras", layers.cctvNetwork) { l: MapLayersState, v: Boolean -> l.copy(cctvNetwork = v) },
        Triple("Camera FOV Cones", layers.cameraFovCones) { l: MapLayersState, v: Boolean -> l.copy(cameraFovCones = v) },
        Triple("Live AI Detections", layers.liveDetections) { l: MapLayersState, v: Boolean -> l.copy(liveDetections = v) },
        Triple("Police Stations", layers.policeStations) { l: MapLayersState, v: Boolean -> l.copy(policeStations = v) },
        Triple("Patrol VRP Routes", layers.patrolRoutes) { l: MapLayersState, v: Boolean -> l.copy(patrolRoutes = v) },
        Triple("My GPS Location", layers.userLocation) { l: MapLayersState, v: Boolean -> l.copy(userLocation = v) }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PanelNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text("3D Map Layers & Overlays", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            entries.forEach { (label, checked, updater) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { v -> onUpdateLayers { updater(it, v) } }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

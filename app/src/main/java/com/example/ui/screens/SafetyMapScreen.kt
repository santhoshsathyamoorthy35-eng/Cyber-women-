package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DetectionStatus
import com.example.domain.CrimeMapEngine
import com.example.ui.components.CyberCard
import com.example.ui.components.RiskPillBadge
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
import com.example.ui.viewmodel.SensitivityLevel
import kotlin.math.roundToInt

private enum class MapSubTab(val label: String) {
    COMMAND("3D Map"),
    LIVE_WATCH("Live Watch"),
    ANALYTICS("Analytics"),
    PATROL_VRP("Patrol VRP"),
    REPORTS("Reports")
}

@Composable
fun SafetyMapScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val filteredIncidents by viewModel.filteredIncidents.collectAsStateWithLifecycle()
    val hotspots by viewModel.hotspots.collectAsStateWithLifecycle()
    val emergingHotspots by viewModel.emergingHotspots.collectAsStateWithLifecycle()
    val predictedRisk by viewModel.predictedRiskGrid.collectAsStateWithLifecycle()
    val shieldZones by viewModel.shieldZones.collectAsStateWithLifecycle()
    val patrolRoutes by viewModel.patrolRoutes.collectAsStateWithLifecycle()
    val detections by viewModel.detections.collectAsStateWithLifecycle()
    val totalDetCount by viewModel.totalDetectionCount.collectAsStateWithLifecycle()
    val detRunning by viewModel.detRunning.collectAsStateWithLifecycle()
    val detSensitivity by viewModel.detSensitivity.collectAsStateWithLifecycle()
    val autoFocus by viewModel.autoFocusDetection.collectAsStateWithLifecycle()
    val autoDispatch by viewModel.autoDispatchDetection.collectAsStateWithLifecycle()
    val cctvWallOpen by viewModel.cctvWallOpen.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val layers by viewModel.layers.collectAsStateWithLifecycle()
    val camera3D by viewModel.camera3D.collectAsStateWithLifecycle()
    val deviceLoc by viewModel.deviceLocation.collectAsStateWithLifecycle()
    val focusPoint by viewModel.mapFocusPoint.collectAsStateWithLifecycle()
    val stationStats by viewModel.stationStats.collectAsStateWithLifecycle()
    val patrolUnits by viewModel.patrolUnitCount.collectAsStateWithLifecycle()
    val patrolHorizon by viewModel.patrolHorizonMin.collectAsStateWithLifecycle()
    val patrolPriority by viewModel.patrolPriority.collectAsStateWithLifecycle()
    val citizenReports by viewModel.mergedCitizenReports.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(MapSubTab.COMMAND) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showLayersSheet by remember { mutableStateOf(false) }
    var inspectTarget by remember { mutableStateOf<MapInspectTarget?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        // TOP SUB-MODE TABS + ACTION ICONS
        Surface(
            color = CommandNavy,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CRIMEMAP 3D · SHIELD INTEL", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            text = "${filteredIncidents.size} records · ${hotspots.size} hotspots · ${detections.count { it.status == DetectionStatus.NEW }} active alerts",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }
                    Row {
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier.testTag("map_filter_button")
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter Incidents", tint = TextPrimary)
                        }
                        IconButton(
                            onClick = { showLayersSheet = true },
                            modifier = Modifier.testTag("map_layers_button")
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = "Toggle Map Layers", tint = CyberCyan)
                        }
                        IconButton(
                            onClick = {
                                val csv = viewModel.buildCsvExport()
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "CrimeMap Puducherry Filtered Export CSV")
                                    putExtra(Intent.EXTRA_TEXT, csv.take(120_000))
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export CrimeMap CSV"))
                            },
                            modifier = Modifier.testTag("map_export_csv_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = SafeEmerald)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MapSubTab.entries.forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag("map_subtab_${tab.name}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ShieldCyan.copy(alpha = 0.24f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }
            }
        }

        // BODY CONTENT
        when (selectedTab) {
            MapSubTab.COMMAND -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    SafetyMapCanvas(
                        incidents = filteredIncidents,
                        hotspots = hotspots,
                        emergingHotspots = emergingHotspots,
                        predictedRisk = predictedRisk,
                        shieldZones = shieldZones,
                        patrolRoutes = patrolRoutes,
                        detections = detections,
                        layers = layers,
                        camera3D = camera3D,
                        deviceLocation = deviceLoc,
                        focusPoint = focusPoint,
                        onFocusConsumed = { viewModel.clearMapFocus() },
                        onSelectTarget = { inspectTarget = it }
                    )

                    // Bottom 3D Camera Control Bar
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = CommandNavy.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, HairlineBright)
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CamToggleChip("ORBIT", camera3D.orbit) { viewModel.updateCamera3D { it.copy(orbit = !it.orbit) } }
                            CamToggleChip("3D TOWERS", camera3D.buildings3d) { viewModel.updateCamera3D { it.copy(buildings3d = !it.buildings3d) } }
                            CamToggleChip("TILT", camera3D.tilted) { viewModel.updateCamera3D { it.copy(tilted = !it.tilted) } }
                            CamToggleChip("RADAR", camera3D.radarSweep) { viewModel.updateCamera3D { it.copy(radarSweep = !it.radarSweep) } }
                            CamToggleChip("FOV", camera3D.camFov) { viewModel.updateCamera3D { it.copy(camFov = !it.camFov) } }
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.setCctvWallOpen(true) },
                                label = { Text("4-UP CCTV", color = AmberWarn) }
                            )
                        }
                    }

                    // Floating Inspect Card
                    inspectTarget?.let { target ->
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(12.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = PanelNavy.copy(alpha = 0.96f),
                            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.7f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (target) {
                                            is MapInspectTarget.HotspotTarget -> "Hotspot · ${target.cluster.nearestAreaName}"
                                            is MapInspectTarget.StationTarget -> "Station · ${target.station.name}"
                                            is MapInspectTarget.CameraTarget -> "${target.camera.id} · ${target.camera.name}"
                                            is MapInspectTarget.DetectionTarget -> "AI Alert · ${target.detection.type}"
                                            is MapInspectTarget.ShieldZoneTarget -> "Shield Zone · ${target.zone.areaName}"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    IconButton(onClick = { inspectTarget = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close inspection", tint = TextSecondary)
                                    }
                                }
                                when (target) {
                                    is MapInspectTarget.HotspotTarget -> {
                                        Text(
                                            text = "Risk ${target.cluster.risk}/100 · ${target.cluster.count} cases · Peak ${CrimeMapEngine.formatHour(target.cluster.peakHour)} (${target.cluster.peakDay}) · Top: ${target.cluster.topCrime}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    is MapInspectTarget.StationTarget -> {
                                        Text(
                                            text = "Puducherry Police Jurisdiction Hub · (${String.format("%.4f, %.4f", target.station.lat, target.station.lon)})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    is MapInspectTarget.CameraTarget -> {
                                        Text(
                                            text = "${target.camera.type} Camera · Bearing ${target.camera.bearing.toInt()}° · FOV ${target.camera.fov.toInt()}° · Range ${target.camera.rangeMeters.toInt()}m",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    is MapInspectTarget.DetectionTarget -> {
                                        Text(
                                            text = "${target.detection.camId} · ${target.detection.ipc} · Conf ${(target.detection.confidence * 100).toInt()}% · Status: ${target.detection.status}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AmberWarn
                                        )
                                    }
                                    is MapInspectTarget.ShieldZoneTarget -> {
                                        RiskPillBadge(level = target.zone.riskLevel, score = target.zone.riskScore)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(target.zone.advisorySummary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            MapSubTab.LIVE_WATCH -> {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        CyberCard(borderColor = AmberWarn.copy(alpha = 0.55f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("LIVE AI CCTV WATCHDOG (SIMULATED)", style = MaterialTheme.typography.labelMedium, color = AmberWarn)
                                    Text("$totalDetCount total alerts · 24 camera nodes", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.toggleDetectionEngine() },
                                        modifier = Modifier.testTag("toggle_det_engine_button")
                                    ) {
                                        Icon(
                                            if (detRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Toggle AI stream",
                                            tint = CyberCyan
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (detRunning) "Pause" else "Resume", color = CyberCyan)
                                    }
                                    Button(
                                        onClick = { viewModel.setCctvWallOpen(true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan, contentColor = DeepObsidian),
                                        modifier = Modifier.testTag("open_cctv_wall_button")
                                    ) {
                                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("4-Up Wall", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SensitivityLevel.entries.forEach { lvl ->
                                    FilterChip(
                                        selected = detSensitivity == lvl,
                                        onClick = { viewModel.setDetectionSensitivity(lvl) },
                                        label = { Text(lvl.label) }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Auto-Focus Map", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Switch(checked = autoFocus, onCheckedChange = { viewModel.setAutoFocusDetection(it) })
                                Text("Auto-Dispatch", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Switch(checked = autoDispatch, onCheckedChange = { viewModel.setAutoDispatchDetection(it) })
                            }
                        }
                    }

                    items(detections, key = { it.id }) { det ->
                        CyberCard(
                            containerColor = CommandNavy,
                            borderColor = Color(det.colorHex).copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${det.camId} · ${det.type}", style = MaterialTheme.typography.titleSmall, color = Color(det.colorHex))
                                Text("Sev ${det.severity}/5 · ${(det.confidence * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${det.camName} · ${det.ipc} · ${det.nearestStation} · Status: ${det.status}" +
                                    (det.dispatchedUnit?.let { " ($it)" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.focusMapOn(det.lat, det.lon)
                                        selectedTab = MapSubTab.COMMAND
                                    },
                                    modifier = Modifier.heightIn(min = 42.dp)
                                ) {
                                    Text("Locate", color = CyberCyan)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.acknowledgeDetection(det.id) },
                                    modifier = Modifier.heightIn(min = 42.dp)
                                ) {
                                    Text("Ack", color = TextPrimary)
                                }
                                Button(
                                    onClick = { viewModel.dispatchNearestUnit(det.id) },
                                    modifier = Modifier.heightIn(min = 42.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRose)
                                ) {
                                    Text("Dispatch", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            MapSubTab.ANALYTICS -> {
                val avgSev = if (filteredIncidents.isNotEmpty()) {
                    filteredIncidents.map { it.severity }.average()
                } else 0.0
                val topCrime = filteredIncidents.groupingBy { it.crime }.eachCount().maxByOrNull { it.value }?.key ?: "N/A"
                val breakdown = filteredIncidents.groupingBy { it.crime }.eachCount().entries.sortedByDescending { it.value }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KpiCard("Total Records", "${filteredIncidents.size}", ShieldCyan, Modifier.weight(1f))
                            KpiCard("Hotspots", "${hotspots.size}", CriticalRose, Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KpiCard("Avg Severity", String.format("%.2f / 5", avgSev), AmberWarn, Modifier.weight(1f))
                            KpiCard("Top Category", topCrime, SafeEmerald, Modifier.weight(1f))
                        }
                    }

                    item {
                        CyberCard {
                            Text("Crime Distribution Donut", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Canvas(modifier = Modifier.size(130.dp)) {
                                    val total = maxOf(1, breakdown.sumOf { it.value }).toFloat()
                                    var start = -90f
                                    breakdown.forEach { entry ->
                                        val sweep = (entry.value / total) * 360f
                                        val col = Color(CrimeMapEngine.CRIME_COLORS[entry.key] ?: 0xFFFFCC4DL)
                                        drawArc(
                                            color = col,
                                            startAngle = start,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            style = Stroke(width = 24f, cap = StrokeCap.Butt)
                                        )
                                        start += sweep
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    breakdown.take(6).forEach { entry ->
                                        val col = Color(CrimeMapEngine.CRIME_COLORS[entry.key] ?: 0xFFFFCC4DL)
                                        Text("${entry.key}: ${entry.value}", style = MaterialTheme.typography.bodySmall, color = col)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Station Leaderboard", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }

                    items(stationStats) { row ->
                        CyberCard(containerColor = CommandNavy) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(row.stationName, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text("${row.caseCount} cases", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
                            }
                            Text(
                                text = "Avg Severity ${row.avgSeverity}/5 · Top: ${row.topCrime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            MapSubTab.PATROL_VRP -> {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        CyberCard {
                            Text("Patrol Route Optimizer (VRP)", style = MaterialTheme.typography.titleMedium, color = CyberCyan)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Units: $patrolUnits · Horizon: ${patrolHorizon}m · Priority: $patrolPriority", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Slider(
                                value = patrolUnits.toFloat(),
                                onValueChange = { viewModel.updatePatrolConfig(units = it.roundToInt()) },
                                valueRange = 1f..CrimeMapEngine.STATIONS.size.toFloat(),
                                steps = CrimeMapEngine.STATIONS.size - 2
                            )
                            Slider(
                                value = patrolHorizon.toFloat(),
                                onValueChange = { viewModel.updatePatrolConfig(horizonMin = it.roundToInt()) },
                                valueRange = 30f..180f,
                                steps = 4
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = patrolPriority == "risk",
                                    onClick = { viewModel.updatePatrolConfig(priority = "risk") },
                                    label = { Text("Risk Priority") }
                                )
                                FilterChip(
                                    selected = patrolPriority == "severity",
                                    onClick = { viewModel.updatePatrolConfig(priority = "severity") },
                                    label = { Text("Density Priority") }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.runPatrolOptimization()
                                    selectedTab = MapSubTab.COMMAND
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("run_patrol_vrp_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan, contentColor = DeepObsidian)
                            ) {
                                Text("Optimize & Show Routes on 3D Map", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(patrolRoutes, key = { it.unit }) { route ->
                        CyberCard(containerColor = CommandNavy, borderColor = Color(route.colorHex).copy(alpha = 0.6f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${route.unit} · ${route.station}", style = MaterialTheme.typography.titleSmall, color = Color(route.colorHex))
                                Text("${route.stops} stops · ${route.timeUsedMinutes}m", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = route.path.joinToString(" → ") { it.label },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            MapSubTab.REPORTS -> {
                var cat by remember { mutableStateOf("Snatching") }
                var desc by remember { mutableStateOf("") }
                var sev by remember { mutableIntStateOf(3) }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        CyberCard {
                            Text("Report Incident / Unsafe Zone", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text("Logs to your cloud safety reports and overlays on CrimeMap.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CrimeMapEngine.CRIME_WEIGHTS.keys.take(6).forEach { c ->
                                    FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) })
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Severity: $sev / 5", style = MaterialTheme.typography.labelMedium, color = AmberWarn)
                            Slider(value = sev.toFloat(), onValueChange = { sev = it.roundToInt() }, valueRange = 1f..5f, steps = 3)
                            OutlinedTextField(
                                value = desc,
                                onValueChange = { desc = it },
                                label = { Text("Describe location or concern") },
                                modifier = Modifier.fillMaxWidth().testTag("citizen_report_description_input"),
                                minLines = 2
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.submitCitizenReport(cat, desc, sev)
                                    desc = ""
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("submit_citizen_report_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan, contentColor = DeepObsidian)
                            ) {
                                Text("Submit Safety Report", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(citizenReports, key = { it.id }) { rep ->
                        CyberCard(containerColor = CommandNavy) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${rep.category} · Sev ${rep.severity}/5", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text(rep.status.uppercase(), style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                            }
                            Text(rep.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("Nearest Station: ${rep.nearestStation}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            if (rep.status == "received") {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(onClick = { viewModel.acknowledgeCitizenReport(rep.id) }) {
                                    Text("Mark Acknowledged", color = SafeEmerald)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (cctvWallOpen) {
        CctvWallDialog(
            detections = detections,
            onAcknowledge = { viewModel.acknowledgeDetection(it) },
            onDispatch = { viewModel.dispatchNearestUnit(it) },
            onSpawnTest = { camId -> viewModel.spawnManualDetection(camId) },
            onDismiss = { viewModel.setCctvWallOpen(false) }
        )
    }

    if (showFilterSheet) {
        CrimeFilterBottomSheet(
            filters = filters,
            onUpdateFilters = { viewModel.updateFilters(it) },
            onReset = { viewModel.resetFilters() },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showLayersSheet) {
        MapLayersBottomSheet(
            layers = layers,
            onUpdateLayers = { viewModel.updateLayers(it) },
            onDismiss = { showLayersSheet = false }
        )
    }
}

@Composable
private fun CamToggleChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = active,
        onClick = onClick,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ShieldCyan.copy(alpha = 0.25f),
            selectedLabelColor = CyberCyan
        )
    )
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    CyberCard(modifier = modifier, containerColor = CommandNavy) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

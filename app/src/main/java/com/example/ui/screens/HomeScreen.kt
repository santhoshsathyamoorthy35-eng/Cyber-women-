package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ShieldRiskLevel
import com.example.domain.CrimeMapEngine
import com.example.domain.SafeHerCatalog
import com.example.service.LocationTracker
import com.example.ui.components.BrandShieldBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.EmergencyDialStrip
import com.example.ui.components.QuickExitAndLangBar
import com.example.ui.components.RiskPillBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.dialPhoneNumber
import com.example.ui.components.shareLocationIntent
import com.example.ui.components.triggerQuickExit
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
import com.example.util.AppStrings
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenMap: () -> Unit,
    onOpenMapFocus: (Double, Double) -> Unit,
    onOpenShieldSos: () -> Unit,
    onOpenMeenaAi: () -> Unit,
    onOpenSafetyHub: () -> Unit
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val deviceLoc by viewModel.deviceLocation.collectAsStateWithLifecycle()
    val shieldZones by viewModel.shieldZones.collectAsStateWithLifecycle()
    val localAdvisory by viewModel.currentLocationShieldAdvisory.collectAsStateWithLifecycle()
    val shieldHour by viewModel.shieldHour.collectAsStateWithLifecycle()
    val shieldDayName by viewModel.shieldDayName.collectAsStateWithLifecycle()
    val detections by viewModel.detections.collectAsStateWithLifecycle()
    val sosCountdown by viewModel.sosCountdownSec.collectAsStateWithLifecycle()
    val activeSosId by viewModel.activeSosId.collectAsStateWithLifecycle()
    val trustedContacts by viewModel.trustedContacts.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            viewModel.refreshDeviceLocation(context)
        }
    }

    LaunchedEffect(Unit) {
        if (LocationTracker.hasLocationPermission(context)) {
            viewModel.refreshDeviceLocation(context)
        }
    }

    val cities = listOf("Puducherry", "Chennai", "Coimbatore", "Madurai")
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHoldingSos by remember { mutableStateOf(false) }

    LaunchedEffect(isHoldingSos) {
        if (isHoldingSos) {
            holdProgress = 0f
            val steps = 15
            for (i in 1..steps) {
                if (!isHoldingSos) break
                delay(100L)
                holdProgress = i / steps.toFloat()
            }
            if (isHoldingSos && holdProgress >= 1f) {
                isHoldingSos = false
                holdProgress = 0f
                viewModel.triggerSosNow(context, triggerType = "hold")
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
                .widthIn(max = 840.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. TOP BRAND BAR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        BrandShieldBadge()
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CYBER WOMEN",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = AppStrings.brandTagline(language),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    QuickExitAndLangBar(
                        language = language,
                        onToggleLanguage = {
                            val next = if (language == AppLanguage.EN) AppLanguage.TA else AppLanguage.EN
                            viewModel.setLanguage(next)
                        },
                        onQuickExit = { triggerQuickExit(context) }
                    )
                }
            }

            // 2. EMERGENCY DIAL STRIP (Direct 112, 181, 14490, 1091, 1930...)
            item {
                EmergencyDialStrip(context = context)
            }

            // 3. CITY & GPS LOCK BAR
            item {
                CyberCard(containerColor = CommandNavy, borderColor = HairlineBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Current GPS Coordinate",
                                tint = if (deviceLoc.isLiveGps) SafeEmerald else AmberWarn,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (deviceLoc.isLiveGps) {
                                        "Live GPS · ${String.format(Locale.US, "%.4f, %.4f", deviceLoc.lat, deviceLoc.lon)} (±${deviceLoc.accuracyMeters.toInt()}m)"
                                    } else {
                                        "Sector Reference · $selectedCity (${String.format(Locale.US, "%.4f, %.4f", deviceLoc.lat, deviceLoc.lon)})"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = localAdvisory?.areaName ?: selectedCity,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                if (LocationTracker.hasLocationPermission(context)) {
                                    viewModel.refreshDeviceLocation(context)
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("home_refresh_gps_button")
                        ) {
                            Icon(Icons.Default.GpsFixed, contentDescription = "Lock GPS", tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (deviceLoc.isLiveGps) "Sync" else "Enable GPS", color = CyberCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cities.forEach { city ->
                            FilterChip(
                                selected = selectedCity == city,
                                onClick = { viewModel.setSelectedCity(city) },
                                label = { Text(city) },
                                modifier = Modifier.testTag("city_chip_$city"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ShieldCyan.copy(alpha = 0.22f),
                                    selectedLabelColor = CyberCyan
                                )
                            )
                        }
                    }
                }
            }

            // 4. THE SHIELD SOS HERO CARD
            item {
                val sosBg by animateColorAsState(
                    targetValue = if (sosCountdown != null || activeSosId != null) {
                        Color(0xFF3E0A16)
                    } else {
                        Color(0xFF1A1024)
                    },
                    label = "sosBg"
                )
                CyberCard(
                    containerColor = sosBg,
                    borderColor = CriticalRose.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "THE SHIELD · EMERGENCY SOS",
                                style = MaterialTheme.typography.labelMedium,
                                color = CriticalRose
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    sosCountdown != null -> "Triggering SOS in ${sosCountdown}s..."
                                    activeSosId != null -> "SOS Session Active · Recording Location"
                                    else -> AppStrings.sosHoldHint(language)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${trustedContacts.size} Trusted Contact(s) linked · 112 Direct Dial",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        // Press-and-hold 1.5s OR quick-tap SOS emblem
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(CriticalRose, Color(0xFF9E0B29))
                                    )
                                )
                                .pointerInput(sosCountdown, activeSosId) {
                                    detectTapGestures(
                                        onTap = {
                                            if (sosCountdown == null && activeSosId == null) {
                                                viewModel.startSosCountdown(context, "button")
                                            } else {
                                                onOpenShieldSos()
                                            }
                                        },
                                        onPress = {
                                            if (sosCountdown == null && activeSosId == null) {
                                                isHoldingSos = true
                                                tryAwaitRelease()
                                                isHoldingSos = false
                                            }
                                        }
                                    )
                                }
                                .testTag("home_sos_trigger_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Activate Shield SOS",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = if (sosCountdown != null) "${sosCountdown}s" else "SOS",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (isHoldingSos && holdProgress > 0f) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { holdProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CriticalRose,
                            trackColor = PanelNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (sosCountdown != null || activeSosId != null) {
                            Button(
                                onClick = {
                                    if (sosCountdown != null) {
                                        viewModel.cancelSosCountdown(context)
                                    } else {
                                        viewModel.resolveActiveSos(context)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("home_sos_cancel_or_resolve_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SafeEmerald,
                                    contentColor = DeepObsidian
                                )
                            ) {
                                Text(if (sosCountdown != null) "Cancel Countdown" else "I Am Safe (Resolve)")
                            }
                        } else {
                            Button(
                                onClick = { dialPhoneNumber(context, "112") },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("home_call_112_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CriticalRose,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(AppStrings.callEmergency112(language))
                            }
                        }
                        OutlinedButton(
                            onClick = onOpenShieldSos,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("home_manage_shield_button"),
                            border = BorderStroke(1.dp, CriticalRose.copy(alpha = 0.6f))
                        ) {
                            Text("Contacts & Tools", color = TextPrimary)
                        }
                    }
                }
            }

            // 5. SHIELD PROTOCOL: TIME-AWARE HISTORICAL VULNERABILITY PREDICTOR
            item {
                CyberCard(borderColor = ShieldCyan.copy(alpha = 0.5f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.shieldProtocolTitle(language),
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberCyan
                            )
                            Text(
                                text = "Window: $shieldDayName · ${CrimeMapEngine.formatHour((shieldHour - 1 + 24) % 24)} – ${CrimeMapEngine.formatHour((shieldHour + 2) % 24)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        TextButton(
                            onClick = { viewModel.resetShieldTimeToNow() },
                            modifier = Modifier.testTag("shield_reset_time_button")
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyberCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Now", color = CyberCyan)
                        }
                    }

                    // Interactive Time-of-Day Simulator Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = CrimeMapEngine.formatHour(shieldHour),
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberWarn,
                            modifier = Modifier.width(56.dp)
                        )
                        Slider(
                            value = shieldHour.toFloat(),
                            onValueChange = { h ->
                                viewModel.setShieldSimulationTime(h.roundToInt(), shieldDayName)
                            },
                            valueRange = 0f..23f,
                            steps = 22,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shield_hour_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = ShieldCyan
                            )
                        )
                    }

                    // Transparent disclosure chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CommandNavy,
                        border = BorderStroke(1.dp, HairlineBright),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "HISTORICAL AGGREGATE RISK · SYNTHETIC BASELINE (Past patterns only — does not predict individual crimes)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val topZones = shieldZones.take(3)
                    topZones.forEach { zone ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenMapFocus(zone.lat, zone.lon) }
                                .testTag("shield_zone_card_${zone.areaName}"),
                            shape = RoundedCornerShape(12.dp),
                            color = CommandNavy,
                            border = BorderStroke(1.dp, HairlineBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = zone.areaName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    RiskPillBadge(level = zone.riskLevel, score = zone.riskScore)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${zone.matchingWindowIncidents} past records in window · Dominant: ${zone.dominantCrime} · ${zone.distanceKm} km away",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onOpenMap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("home_open_map_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShieldCyan,
                            contentColor = DeepObsidian
                        )
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open CrimeMap 3D Command & Shield Zones", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 6. LIVE AI CCTV DETECTION TEASER + MEENA AI CARD
            item {
                val latestDet = detections.firstOrNull()
                if (latestDet != null) {
                    CyberCard(
                        containerColor = CommandNavy,
                        borderColor = AmberWarn.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { onOpenMapFocus(latestDet.lat, latestDet.lon) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Simulated CCTV Alert",
                                    tint = AmberWarn,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "LIVE AI WATCHDOG (SIMULATED) · ${latestDet.camId}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AmberWarn
                                    )
                                    Text(
                                        text = "${latestDet.type} at ${latestDet.camName} (${(latestDet.confidence * 100).toInt()}% conf)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                            Icon(Icons.Default.Radar, contentDescription = "View on Map", tint = CyberCyan)
                        }
                    }
                }
            }

            item {
                CyberCard(
                    borderColor = CyberPurple.copy(alpha = 0.55f),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(onClick = onOpenMeenaAi)
                        .testTag("home_meena_ai_card")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_meena_avatar_1794777566719),
                            contentDescription = "Meena AI Companion Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ask Meena AI Companion",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyberPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = AppStrings.meenaWelcome(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 7. NEARBY SAFE ZONES & QUICK TOOLS
            item {
                SectionHeader(
                    title = "Nearest Safe Havens",
                    subtitle = "24/7 Police Stations & Sakhi One Stop Centres",
                    trailing = {
                        TextButton(
                            onClick = onOpenSafetyHub,
                            modifier = Modifier.testTag("home_view_all_safe_zones")
                        ) {
                            Text("All Helplines", color = CyberCyan)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                val sortedZones = remember(deviceLoc.lat, deviceLoc.lon) {
                    SafeHerCatalog.SAFE_ZONES.sortedBy { sz ->
                        if (sz.lat != null && sz.lon != null) {
                            CrimeMapEngine.haversineKm(deviceLoc.lat, deviceLoc.lon, sz.lat, sz.lon)
                        } else 999.0
                    }.take(3)
                }
                sortedZones.forEach { sz ->
                    val distKm = if (sz.lat != null && sz.lon != null) {
                        CrimeMapEngine.haversineKm(deviceLoc.lat, deviceLoc.lon, sz.lat, sz.lon)
                    } else null
                    CyberCard(
                        containerColor = CommandNavy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sz.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text(
                                    text = "${sz.zoneType} · ${sz.details}" +
                                        (if (distKm != null) String.format(Locale.US, " · %.1f km", distKm) else ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { dialPhoneNumber(context, sz.phone) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("safe_zone_dial_${sz.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SafeEmerald.copy(alpha = 0.2f),
                                    contentColor = SafeEmerald
                                )
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Dial ${sz.name}", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(sz.phone.take(11))
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        shareLocationIntent(
                            context,
                            deviceLoc.lat,
                            deviceLoc.lon,
                            localAdvisory?.areaName ?: selectedCity
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("home_share_location_button"),
                    border = BorderStroke(1.dp, HairlineBright)
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Current GPS Location via Apps", color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

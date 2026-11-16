package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.data.model.Camera3DSettings
import com.example.data.model.CctvCamera
import com.example.data.model.EmergingHotspot
import com.example.data.model.GeoPoint
import com.example.data.model.HotspotCluster
import com.example.data.model.IncidentRecord
import com.example.data.model.LiveDetection
import com.example.data.model.MapLayersState
import com.example.data.model.PatrolRoute
import com.example.data.model.PoliceStation
import com.example.data.model.PredictedRiskCell
import com.example.data.model.ShieldRiskLevel
import com.example.data.model.ShieldZonePrediction
import com.example.domain.CrimeMapEngine
import com.example.service.DeviceLocationState
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.CriticalRose
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.HighOrange
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.ShieldCyan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asinh
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

sealed interface MapInspectTarget {
    data class HotspotTarget(val cluster: HotspotCluster) : MapInspectTarget
    data class StationTarget(val station: PoliceStation) : MapInspectTarget
    data class CameraTarget(val camera: CctvCamera) : MapInspectTarget
    data class DetectionTarget(val detection: LiveDetection) : MapInspectTarget
    data class ShieldZoneTarget(val zone: ShieldZonePrediction) : MapInspectTarget
}

private object SlippyTileCache {
    private val cache = LruCache<String, ImageBitmap>(64)
    private val pending = HashSet<String>()
    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    fun get(key: String): ImageBitmap? = cache.get(key)

    suspend fun fetchTile(z: Int, x: Int, y: Int): ImageBitmap? {
        val key = "$z/$x/$y"
        cache.get(key)?.let { return it }
        synchronized(pending) {
            if (key in pending) return null
            pending.add(key)
        }
        return withContext(Dispatchers.IO) {
            try {
                val subdomains = listOf("a", "b", "c")
                val s = subdomains[abs(x + y) % subdomains.size]
                val url = "https://$s.basemaps.cartocdn.com/dark_all/$z/$x/$y.png"
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "CyberWomenAndroid/1.0")
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bytes = resp.body?.bytes()
                        if (bytes != null) {
                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            val img = bmp?.asImageBitmap()
                            if (img != null) {
                                cache.put(key, img)
                            }
                            img
                        } else null
                    } else null
                }
            } catch (_: Exception) {
                null
            } finally {
                synchronized(pending) { pending.remove(key) }
            }
        }
    }
}

@Composable
fun SafetyMapCanvas(
    incidents: List<IncidentRecord>,
    hotspots: List<HotspotCluster>,
    emergingHotspots: List<EmergingHotspot>,
    predictedRisk: List<PredictedRiskCell>,
    shieldZones: List<ShieldZonePrediction>,
    patrolRoutes: List<PatrolRoute>,
    detections: List<LiveDetection>,
    layers: MapLayersState,
    camera3D: Camera3DSettings,
    deviceLocation: DeviceLocationState,
    focusPoint: GeoPoint?,
    onFocusConsumed: () -> Unit,
    onSelectTarget: (MapInspectTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    var centerLat by remember { mutableStateOf(11.9340) }
    var centerLon by remember { mutableStateOf(79.8260) }
    var zoomLevel by remember { mutableFloatStateOf(13.2f) }
    var bearingDeg by remember { mutableFloatStateOf(-12f) }
    var tileTrigger by remember { mutableIntStateOf(0) }
    var canvasSize by remember { mutableStateOf(Size(1f, 1f)) }

    LaunchedEffect(focusPoint) {
        if (focusPoint != null) {
            centerLat = focusPoint.lat.coerceIn(11.83, 12.01)
            centerLon = focusPoint.lon.coerceIn(79.73, 79.89)
            zoomLevel = max(zoomLevel, 14.3f)
            onFocusConsumed()
        }
    }

    // Orbit rotation when camera3D.orbit is enabled
    LaunchedEffect(camera3D.orbit) {
        while (isActive && camera3D.orbit) {
            delay(32L)
            bearingDeg = (bearingDeg + 0.55f) % 360f
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mapRadar")
    val radarPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPhase"
    )
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulsePhase"
    )

    val tileScope = androidx.compose.runtime.rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .pointerInput(camera3D.tilted) {
                detectTransformGestures { _, pan, zoomChange, rotationChange ->
                    val newZoom = (zoomLevel + (zoomChange - 1f) * 2.2f).coerceIn(11.2f, 16.5f)
                    zoomLevel = newZoom
                    bearingDeg = (bearingDeg + rotationChange) % 360f

                    val pixelsPerWorld = 256.0 * 2.0.pow(newZoom.toDouble())
                    val pitchSquash = if (camera3D.tilted) 0.68 else 1.0
                    val rad = Math.toRadians(-bearingDeg.toDouble())
                    val dx = pan.x.toDouble()
                    val dy = pan.y.toDouble() / pitchSquash
                    val rotX = dx * cos(rad) - dy * sin(rad)
                    val rotY = dx * sin(rad) + dy * cos(rad)

                    val dLon = -(rotX / pixelsPerWorld) * 360.0
                    val dLat = (rotY / pixelsPerWorld) * 360.0 * cos(Math.toRadians(centerLat))
                    centerLat = (centerLat + dLat).coerceIn(11.83, 12.01)
                    centerLon = (centerLon + dLon).coerceIn(79.73, 79.89)
                }
            }
            .pointerInput(hotspots, detections, shieldZones, zoomLevel, centerLat, centerLon, bearingDeg, camera3D.tilted) {
                detectTapGestures { tapOffset ->
                    val project = { lat: Double, lon: Double ->
                        projectGeoToScreen(
                            lat = lat,
                            lon = lon,
                            centerLat = centerLat,
                            centerLon = centerLon,
                            zoom = zoomLevel,
                            bearingDeg = bearingDeg,
                            tilted = camera3D.tilted,
                            width = canvasSize.width,
                            height = canvasSize.height
                        )
                    }
                    // Check nearest clickable target within 38px
                    var bestDist = 44f
                    var picked: MapInspectTarget? = null

                    detections.take(12).forEach { det ->
                        val p = project(det.lat, det.lon)
                        val d = (p - tapOffset).getDistance()
                        if (d < bestDist) {
                            bestDist = d
                            picked = MapInspectTarget.DetectionTarget(det)
                        }
                    }
                    CrimeMapEngine.CAMERAS.forEach { cam ->
                        val p = project(cam.lat, cam.lon)
                        val d = (p - tapOffset).getDistance()
                        if (d < bestDist) {
                            bestDist = d
                            picked = MapInspectTarget.CameraTarget(cam)
                        }
                    }
                    CrimeMapEngine.STATIONS.forEach { st ->
                        val p = project(st.lat, st.lon)
                        val d = (p - tapOffset).getDistance()
                        if (d < bestDist) {
                            bestDist = d
                            picked = MapInspectTarget.StationTarget(st)
                        }
                    }
                    shieldZones.forEach { sz ->
                        val p = project(sz.lat, sz.lon)
                        val d = (p - tapOffset).getDistance()
                        if (d < bestDist) {
                            bestDist = d
                            picked = MapInspectTarget.ShieldZoneTarget(sz)
                        }
                    }
                    hotspots.forEach { hs ->
                        val p = project(hs.lat, hs.lon)
                        val d = (p - tapOffset).getDistance()
                        if (d < bestDist) {
                            bestDist = d
                            picked = MapInspectTarget.HotspotTarget(hs)
                        }
                    }
                    picked?.let(onSelectTarget)
                }
            }
            .testTag("safety_map_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            canvasSize = size
            val _forceRedraw = tileTrigger // Subscribe to fetched tile arrivals

            val project = { lat: Double, lon: Double ->
                projectGeoToScreen(
                    lat = lat,
                    lon = lon,
                    centerLat = centerLat,
                    centerLon = centerLon,
                    zoom = zoomLevel,
                    bearingDeg = bearingDeg,
                    tilted = camera3D.tilted,
                    width = size.width,
                    height = size.height
                )
            }

            // 1. VECTOR BASEMAP + CARTO DARK MATTER TILES
            drawRect(Color(0xFF060A13))
            drawCartoTilesIfAvailable(
                centerLat = centerLat,
                centerLon = centerLon,
                zoom = zoomLevel,
                bearingDeg = bearingDeg,
                tilted = camera3D.tilted,
                onRequestTile = { z, x, y ->
                    tileScope.launch {
                        val img = SlippyTileCache.fetchTile(z, x, y)
                        if (img != null) tileTrigger++
                    }
                }
            )
            drawPuducherryVectorGrid(project)

            // 2. HEATMAP LAYER
            if (layers.heatmap) {
                val sample = if (incidents.size > 700) incidents.filterIndexed { i, _ -> i % 2 == 0 } else incidents
                for (inc in sample) {
                    val p = project(inc.lat, inc.lon)
                    if (p.x !in -60f..(size.width + 60f) || p.y !in -60f..(size.height + 60f)) continue
                    val radius = (18f + inc.severity * 4f) * (zoomLevel / 13f)
                    val color = when {
                        inc.severity >= 5 -> CriticalRose
                        inc.severity >= 3 -> HighOrange
                        else -> AmberWarn
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
                            center = p,
                            radius = radius
                        ),
                        radius = radius,
                        center = p
                    )
                }
            }

            // 3. 7-DAY PREDICTED RISK GRID
            if (layers.predictedRisk7Day) {
                predictedRisk.take(90).forEach { cell ->
                    val p = project(cell.lat, cell.lon)
                    if (p.x in -50f..(size.width + 50f) && p.y in -50f..(size.height + 50f)) {
                        val c = Color(CrimeMapEngine.riskColorHex(cell.risk))
                        drawCircle(
                            color = c.copy(alpha = 0.20f),
                            radius = 18f,
                            center = p
                        )
                    }
                }
            }

            // 4. SHIELD PROTOCOL TIME-VULNERABLE ZONES
            if (layers.shieldTimeZones) {
                shieldZones.forEach { sz ->
                    val p = project(sz.lat, sz.lon)
                    val c = when (sz.riskLevel) {
                        ShieldRiskLevel.CRITICAL -> CriticalRose
                        ShieldRiskLevel.HIGH -> HighOrange
                        ShieldRiskLevel.MODERATE -> AmberWarn
                        else -> SafeEmerald
                    }
                    val ringR = (22f + sz.riskScore * 0.26f) * (0.9f + pulsePhase * 0.2f)
                    drawCircle(
                        color = c.copy(alpha = 0.14f),
                        radius = ringR,
                        center = p
                    )
                    drawCircle(
                        color = c.copy(alpha = 0.72f),
                        radius = ringR,
                        center = p,
                        style = Stroke(
                            width = 2.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        )
                    )
                }
            }

            // 5. INCIDENT POINTS
            if (layers.incidentPoints) {
                val points = if (incidents.size > 1100) incidents.take(1100) else incidents
                for (inc in points) {
                    val p = project(inc.lat, inc.lon)
                    if (p.x !in -20f..(size.width + 20f) || p.y !in -20f..(size.height + 20f)) continue
                    val col = Color(CrimeMapEngine.CRIME_COLORS[inc.crime] ?: 0xFFFFCC4DL)
                    val r = if (inc.isSynthetic) 2.5f + inc.severity * 0.6f else 6.5f
                    drawCircle(color = col.copy(alpha = if (inc.isSynthetic) 0.72f else 1f), radius = r, center = p)
                }
            }

            // 6. 3D EXTRUDED HEXAGONAL RISK TOWERS & DATA BEAMS
            if (camera3D.buildings3d && (layers.riskTowers3D || layers.dataBeams)) {
                val pitchFactor = if (camera3D.tilted) 1.0f else 0.32f
                hotspots.forEach { hs ->
                    val base = project(hs.lat, hs.lon)
                    val c = Color(CrimeMapEngine.riskColorHex(hs.risk))
                    val heightPx = (24f + hs.risk * 1.15f) * pitchFactor * (zoomLevel / 13f)
                    if (layers.riskTowers3D) {
                        drawHexTower3D(base = base, radius = 12f, height = heightPx, color = c)
                    }
                    if (layers.dataBeams) {
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(c.copy(alpha = 0.85f), c.copy(alpha = 0.05f)),
                                startY = base.y,
                                endY = base.y - heightPx - 36f
                            ),
                            start = base,
                            end = Offset(base.x, base.y - heightPx - 36f),
                            strokeWidth = 2.2f
                        )
                    }
                }
            }

            // 7. HOTSPOT CLUSTER RINGS & EMERGING ALERTS
            if (layers.hotspotClusters) {
                hotspots.forEach { hs ->
                    val p = project(hs.lat, hs.lon)
                    val c = Color(CrimeMapEngine.riskColorHex(hs.risk))
                    drawCircle(color = c.copy(alpha = 0.85f), radius = 15f, center = p, style = Stroke(2.5f))
                    drawCircle(color = c, radius = 5.5f, center = p)
                }
            }
            if (layers.emergingAlerts) {
                emergingHotspots.forEach { em ->
                    val p = project(em.lat, em.lon)
                    drawCircle(
                        color = CriticalRose,
                        radius = 18f + pulsePhase * 10f,
                        center = p,
                        style = Stroke(2.5f)
                    )
                }
            }

            // 8. PATROL VRP ROUTES
            if (layers.patrolRoutes) {
                patrolRoutes.forEach { route ->
                    val col = Color(route.colorHex)
                    val pts = route.path.map { project(it.lat, it.lon) }
                    for (i in 0 until pts.size - 1) {
                        drawLine(
                            color = col.copy(alpha = 0.9f),
                            start = pts[i],
                            end = pts[i + 1],
                            strokeWidth = 3.2f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }
                    pts.forEachIndexed { idx, pt ->
                        drawCircle(color = col, radius = if (idx == 0) 6f else 4.5f, center = pt)
                    }
                }
            }

            // 9. 24 CCTV CAMERAS & FOV CONES
            if (layers.cctvNetwork) {
                CrimeMapEngine.CAMERAS.forEach { cam ->
                    val p = project(cam.lat, cam.lon)
                    if (layers.cameraFovCones && camera3D.camFov) {
                        drawCameraFovCone(
                            origin = p,
                            bearingDeg = cam.bearing - bearingDeg,
                            fovDeg = cam.fov,
                            coneRadius = (cam.rangeMeters * 0.24f) * (zoomLevel / 13f)
                        )
                    }
                    drawCircle(color = DeepObsidian, radius = 6.5f, center = p)
                    drawCircle(color = CyberCyan, radius = 5f, center = p)
                    drawCircle(color = Color.White, radius = 2f, center = p)
                }
            }

            // 10. LIVE AI DETECTION RADAR RINGS
            if (layers.liveDetections) {
                detections.take(10).forEach { det ->
                    val p = project(det.lat, det.lon)
                    val col = Color(det.colorHex)
                    val r1 = 12f + pulsePhase * 24f
                    drawCircle(
                        color = col.copy(alpha = (1f - pulsePhase) * 0.85f),
                        radius = r1,
                        center = p,
                        style = Stroke(3f)
                    )
                    drawCircle(color = col, radius = 7f, center = p)
                }
            }

            // 11. POLICE STATIONS
            if (layers.policeStations) {
                CrimeMapEngine.STATIONS.forEach { st ->
                    val p = project(st.lat, st.lon)
                    drawRect(
                        color = ShieldCyan,
                        topLeft = Offset(p.x - 8f, p.y - 8f),
                        size = Size(16f, 16f)
                    )
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(p.x - 8f, p.y - 8f),
                        size = Size(16f, 16f),
                        style = Stroke(1.8f)
                    )
                }
            }

            // 12. RADAR SWEEP OVERLAY
            if (camera3D.radarSweep) {
                val center = project(11.9340, 79.8260)
                val sweepLen = min(size.width, size.height) * 0.42f
                val rad = Math.toRadians(radarPhase.toDouble())
                val end = Offset(
                    center.x + (cos(rad) * sweepLen).toFloat(),
                    center.y + (sin(rad) * sweepLen * (if (camera3D.tilted) 0.68 else 1.0)).toFloat()
                )
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(CyberCyan.copy(alpha = 0.55f), Color.Transparent),
                        start = center,
                        end = end
                    ),
                    start = center,
                    end = end,
                    strokeWidth = 3f
                )
            }

            // 13. USER GPS MARKER
            if (layers.userLocation) {
                val up = project(deviceLocation.lat, deviceLocation.lon)
                drawCircle(
                    color = SafeEmerald.copy(alpha = 0.25f * (1f - pulsePhase * 0.5f)),
                    radius = 18f + pulsePhase * 12f,
                    center = up
                )
                drawCircle(color = Color.White, radius = 8f, center = up)
                drawCircle(color = SafeEmerald, radius = 5.5f, center = up)
            }
        }
    }
}

private fun projectGeoToScreen(
    lat: Double,
    lon: Double,
    centerLat: Double,
    centerLon: Double,
    zoom: Float,
    bearingDeg: Float,
    tilted: Boolean,
    width: Float,
    height: Float
): Offset {
    val worldSize = 256.0 * 2.0.pow(zoom.toDouble())
    val (wx, wy) = latLonToWorld(lat, lon, worldSize)
    val (cx, cy) = latLonToWorld(centerLat, centerLon, worldSize)

    val dx = wx - cx
    val dy = wy - cy
    val rad = Math.toRadians(bearingDeg.toDouble())
    val rx = dx * cos(rad) - dy * sin(rad)
    val ry = dx * sin(rad) + dy * cos(rad)

    val pitchScale = if (tilted) 0.68 else 1.0
    return Offset(
        x = (width / 2f + rx).toFloat(),
        y = (height / 2f + ry * pitchScale).toFloat()
    )
}

private fun latLonToWorld(lat: Double, lon: Double, worldSize: Double): Pair<Double, Double> {
    val x = (lon + 180.0) / 360.0 * worldSize
    val sinLat = sin(Math.toRadians(lat)).coerceIn(-0.9999, 0.9999)
    val y = (0.5 - ln((1 + sinLat) / (1 - sinLat)) / (4.0 * PI)) * worldSize
    return x to y
}

private fun DrawScope.drawCartoTilesIfAvailable(
    centerLat: Double,
    centerLon: Double,
    zoom: Float,
    bearingDeg: Float,
    tilted: Boolean,
    onRequestTile: (Int, Int, Int) -> Unit
) {
    val z = zoom.roundToInt().coerceIn(11, 16)
    val n = 1 shl z
    val cx = ((centerLon + 180.0) / 360.0 * n).toInt().coerceIn(0, n - 1)
    val latRad = Math.toRadians(centerLat)
    val cy = ((1.0 - asinh(tan(latRad)) / PI) / 2.0 * n).toInt().coerceIn(0, n - 1)

    for (tx in (cx - 1)..(cx + 1)) {
        for (ty in (cy - 1)..(cy + 1)) {
            if (tx !in 0 until n || ty !in 0 until n) continue
            val tileKey = "$z/$tx/$ty"
            val img = SlippyTileCache.get(tileKey)
            if (img == null) {
                onRequestTile(z, tx, ty)
                continue
            }
            val lonW = tx.toDouble() / n * 360.0 - 180.0
            val lonE = (tx + 1).toDouble() / n * 360.0 - 180.0
            val latN = Math.toDegrees(kotlin.math.atan(kotlin.math.sinh(PI * (1.0 - 2.0 * ty.toDouble() / n))))
            val latS = Math.toDegrees(kotlin.math.atan(kotlin.math.sinh(PI * (1.0 - 2.0 * (ty + 1).toDouble() / n))))

            val tl = projectGeoToScreen(latN, lonW, centerLat, centerLon, zoom, bearingDeg, tilted, size.width, size.height)
            val br = projectGeoToScreen(latS, lonE, centerLat, centerLon, zoom, bearingDeg, tilted, size.width, size.height)
            val minX = min(tl.x, br.x).roundToInt()
            val minY = min(tl.y, br.y).roundToInt()
            val w = max(1, abs(br.x - tl.x).roundToInt())
            val h = max(1, abs(br.y - tl.y).roundToInt())
            drawImage(
                image = img,
                dstOffset = IntOffset(minX, minY),
                dstSize = IntSize(w, h),
                alpha = 0.72f
            )
        }
    }
}

private fun DrawScope.drawPuducherryVectorGrid(project: (Double, Double) -> Offset) {
    val gridColor = Color(0xFF1A2842)
    var lat = 11.85
    while (lat <= 11.99) {
        val p1 = project(lat, 79.75)
        val p2 = project(lat, 79.87)
        drawLine(gridColor, p1, p2, strokeWidth = 1f)
        lat += 0.015
    }
    var lon = 79.76
    while (lon <= 79.87) {
        val p1 = project(11.85, lon)
        val p2 = project(11.99, lon)
        drawLine(gridColor, p1, p2, strokeWidth = 1f)
        lon += 0.015
    }
    // Bay of Bengal coastline highlight along east Puducherry
    val coast = listOf(
        project(11.860, 79.824),
        project(11.905, 79.832),
        project(11.931, 79.837),
        project(11.965, 79.858),
        project(11.985, 79.865)
    )
    for (i in 0 until coast.size - 1) {
        drawLine(
            color = CyberCyan.copy(alpha = 0.35f),
            start = coast[i],
            end = coast[i + 1],
            strokeWidth = 3f
        )
    }
}

private fun DrawScope.drawHexTower3D(
    base: Offset,
    radius: Float,
    height: Float,
    color: Color
) {
    val topCenter = Offset(base.x, base.y - height)
    val basePts = (0 until 6).map { i ->
        val a = Math.toRadians((i * 60).toDouble())
        Offset(
            base.x + (cos(a) * radius).toFloat(),
            base.y + (sin(a) * radius * 0.58f).toFloat()
        )
    }
    val topPts = basePts.map { Offset(it.x, it.y - height) }

    // Draw front faces of prism
    for (i in 0 until 6) {
        val next = (i + 1) % 6
        if (basePts[i].y >= base.y - 1f || basePts[next].y >= base.y - 1f) {
            val side = Path().apply {
                moveTo(basePts[i].x, basePts[i].y)
                lineTo(basePts[next].x, basePts[next].y)
                lineTo(topPts[next].x, topPts[next].y)
                lineTo(topPts[i].x, topPts[i].y)
                close()
            }
            drawPath(side, color = color.copy(alpha = 0.42f))
            drawPath(side, color = color.copy(alpha = 0.78f), style = Stroke(1.2f))
        }
    }

    val cap = Path().apply {
        moveTo(topPts[0].x, topPts[0].y)
        for (i in 1 until 6) lineTo(topPts[i].x, topPts[i].y)
        close()
    }
    drawPath(cap, color = color.copy(alpha = 0.72f))
    drawPath(cap, color = Color.White.copy(alpha = 0.65f), style = Stroke(1.2f))
    drawCircle(color = Color.White, radius = 2.2f, center = topCenter)
}

private fun DrawScope.drawCameraFovCone(
    origin: Offset,
    bearingDeg: Float,
    fovDeg: Float,
    coneRadius: Float
) {
    val startDeg = bearingDeg - 90f - fovDeg / 2f
    val path = Path().apply {
        moveTo(origin.x, origin.y)
        val steps = 8
        for (i in 0..steps) {
            val ang = Math.toRadians((startDeg + fovDeg * i / steps).toDouble())
            lineTo(
                origin.x + (cos(ang) * coneRadius).toFloat(),
                origin.y + (sin(ang) * coneRadius * 0.72f).toFloat()
            )
        }
        close()
    }
    drawPath(path, color = CyberCyan.copy(alpha = 0.14f))
    drawPath(path, color = CyberCyan.copy(alpha = 0.35f), style = Stroke(1f))
}

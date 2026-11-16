package com.example.domain

import com.example.data.model.CrimeBounds
import com.example.data.model.CrimeMapFilters
import com.example.data.model.CctvCamera
import com.example.data.model.DetectionStatus
import com.example.data.model.DetectionTypeSpec
import com.example.data.model.EmergingHotspot
import com.example.data.model.HotspotCluster
import com.example.data.model.IncidentRecord
import com.example.data.model.LiveDetection
import com.example.data.model.PatrolRoute
import com.example.data.model.PatrolStop
import com.example.data.model.PoliceStation
import com.example.data.model.PredictedRiskCell
import com.example.data.model.ShieldRiskLevel
import com.example.data.model.ShieldZonePrediction
import com.example.data.model.StaticHotspot
import com.example.data.model.StationStatRow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object CrimeMapEngine {

    val BOUNDS = CrimeBounds(
        latMin = 11.850,
        latMax = 11.985,
        lonMin = 79.760,
        lonMax = 79.865
    )

    const val GRID_STEP = 0.0025

    val CRIME_WEIGHTS: Map<String, Double> = linkedMapOf(
        "Theft" to 0.22,
        "Snatching" to 0.14,
        "Assault" to 0.09,
        "Traffic Violation" to 0.16,
        "Cyber Crime" to 0.08,
        "Drug Offence" to 0.07,
        "Public Nuisance" to 0.11,
        "Burglary" to 0.07,
        "Missing Person" to 0.06
    )

    val CRIME_COLORS: Map<String, Long> = mapOf(
        "Theft" to 0xFFFFCC4DL,
        "Snatching" to 0xFFFF7A3CL,
        "Assault" to 0xFFFF3B5CL,
        "Traffic Violation" to 0xFF3BA3FFL,
        "Cyber Crime" to 0xFFA06BFFL,
        "Drug Offence" to 0xFFFF4FA3L,
        "Public Nuisance" to 0xFF8EA0C4L,
        "Burglary" to 0xFFFFB03AL,
        "Missing Person" to 0xFF8ECBFFL
    )

    val STATIONS: List<PoliceStation> = listOf(
        PoliceStation("Grand Bazaar PS", 11.9330, 79.8290),
        PoliceStation("Orleanpet PS", 11.9400, 79.8150),
        PoliceStation("Mudaliarpet PS", 11.9250, 79.8050),
        PoliceStation("Lawspet PS", 11.9470, 79.8180),
        PoliceStation("Villianur PS", 11.9130, 79.7600),
        PoliceStation("Bahour PS", 11.8730, 79.7900),
        PoliceStation("Reddiarpalayam PS", 11.9280, 79.8220),
        PoliceStation("Kalapet PS", 11.9650, 79.8550)
    )

    val HOTSPOTS: List<StaticHotspot> = listOf(
        StaticHotspot("Goubert Market", 11.9340, 79.8300, 1.00, listOf("Theft", "Snatching", "Public Nuisance")),
        StaticHotspot("Puducherry Bus Stand", 11.9370, 79.8260, 0.95, listOf("Theft", "Snatching", "Traffic Violation")),
        StaticHotspot("Rock Beach", 11.9310, 79.8360, 0.90, listOf("Public Nuisance", "Snatching", "Assault")),
        StaticHotspot("MG Road", 11.9360, 79.8330, 0.85, listOf("Theft", "Traffic Violation")),
        StaticHotspot("Railway Station", 11.9270, 79.8280, 0.80, listOf("Theft", "Snatching")),
        StaticHotspot("Botanical Garden", 11.9300, 79.8330, 0.55, listOf("Public Nuisance")),
        StaticHotspot("Lawspet Junction", 11.9470, 79.8180, 0.75, listOf("Traffic Violation", "Assault")),
        StaticHotspot("Kalapet Beach", 11.9650, 79.8550, 0.70, listOf("Public Nuisance", "Drug Offence")),
        StaticHotspot("Mudaliarpet Market", 11.9250, 79.8050, 0.78, listOf("Theft", "Burglary")),
        StaticHotspot("Villianur Bypass", 11.9130, 79.7600, 0.60, listOf("Traffic Violation", "Assault")),
        StaticHotspot("Reddiarpalayam", 11.9280, 79.8220, 0.68, listOf("Burglary", "Theft")),
        StaticHotspot("Auroville Road", 11.9650, 79.8200, 0.45, listOf("Traffic Violation"))
    )

    private val LOCATION_TYPES = listOf(
        "market", "beach", "bus stand", "residential", "highway",
        "school zone", "tourist spot", "commercial", "park", "slum cluster"
    )

    private val MODUS = listOf(
        "night-entry", "two-wheeler", "crowd-cover", "repeat-offender",
        "opportunistic", "organized"
    )

    val CAMERAS: List<CctvCamera> = listOf(
        CctvCamera("CAM-01", "Goubert Market North", 11.9352, 79.8296, 200f, 72f, 150f, "PTZ"),
        CctvCamera("CAM-02", "Goubert Market South", 11.9328, 79.8305, 20f, 65f, 130f, "Fixed"),
        CctvCamera("CAM-03", "Bus Stand Main Gate", 11.9378, 79.8252, 250f, 80f, 160f, "PTZ"),
        CctvCamera("CAM-04", "Bus Stand Platform 3", 11.9364, 79.8270, 150f, 60f, 100f, "Fixed"),
        CctvCamera("CAM-05", "Rock Beach Promenade", 11.9320, 79.8368, 340f, 90f, 180f, "PTZ"),
        CctvCamera("CAM-06", "Rock Beach Parking", 11.9302, 79.8350, 60f, 70f, 120f, "Fixed"),
        CctvCamera("CAM-07", "MG Road Junction", 11.9368, 79.8336, 180f, 75f, 140f, "PTZ"),
        CctvCamera("CAM-08", "MG Road Retail Strip", 11.9350, 79.8324, 270f, 62f, 110f, "Fixed"),
        CctvCamera("CAM-09", "Railway Station Entry", 11.9278, 79.8286, 120f, 70f, 135f, "PTZ"),
        CctvCamera("CAM-10", "Railway Yard Gate", 11.9258, 79.8270, 300f, 58f, 100f, "Fixed"),
        CctvCamera("CAM-11", "Lawspet Junction", 11.9476, 79.8176, 220f, 78f, 145f, "PTZ"),
        CctvCamera("CAM-12", "Lawspet Market Road", 11.9460, 79.8194, 40f, 64f, 115f, "Fixed"),
        CctvCamera("CAM-13", "Kalapet Beach East", 11.9658, 79.8562, 330f, 85f, 170f, "PTZ"),
        CctvCamera("CAM-14", "Kalapet Highway Point", 11.9636, 79.8536, 190f, 68f, 150f, "Fixed"),
        CctvCamera("CAM-15", "Mudaliarpet Market", 11.9256, 79.8056, 10f, 76f, 140f, "PTZ"),
        CctvCamera("CAM-16", "Mudaliarpet Cross", 11.9242, 79.8040, 230f, 60f, 105f, "Fixed"),
        CctvCamera("CAM-17", "Villianur Bypass", 11.9136, 79.7606, 90f, 82f, 175f, "PTZ"),
        CctvCamera("CAM-18", "Reddiarpalayam Chowk", 11.9286, 79.8226, 160f, 70f, 130f, "Fixed"),
        CctvCamera("CAM-19", "Botanical Garden Gate", 11.9306, 79.8336, 290f, 66f, 120f, "Fixed"),
        CctvCamera("CAM-20", "Auroville Road Junction", 11.9654, 79.8206, 200f, 74f, 150f, "PTZ"),
        CctvCamera("CAM-21", "Orleanpet Signal", 11.9406, 79.8156, 30f, 68f, 125f, "Fixed"),
        CctvCamera("CAM-22", "Bahour Road Entry", 11.8736, 79.7906, 130f, 72f, 140f, "PTZ"),
        CctvCamera("CAM-23", "Grand Bazaar Lane 4", 11.9338, 79.8288, 310f, 60f, 100f, "Fixed"),
        CctvCamera("CAM-24", "Beach Road Roundabout", 11.9332, 79.8352, 45f, 88f, 165f, "PTZ")
    )

    val DETECTION_TYPES: List<DetectionTypeSpec> = listOf(
        DetectionTypeSpec("Physical Assault", 5, "IPC 323/325", 0xFFFF3B5CL, 0.16),
        DetectionTypeSpec("Group Fight", 5, "IPC 147/148", 0xFFFF3B5CL, 0.10),
        DetectionTypeSpec("Weapon Detected", 5, "Arms Act 25", 0xFFFF3B5CL, 0.06),
        DetectionTypeSpec("Chain Snatching", 4, "IPC 392", 0xFFFF7A3CL, 0.13),
        DetectionTypeSpec("Robbery In Progress", 5, "IPC 392/397", 0xFFFF3B5CL, 0.07),
        DetectionTypeSpec("Person Down / Fall", 4, "Accident", 0xFFFF7A3CL, 0.09),
        DetectionTypeSpec("Vehicle Collision", 4, "MVA 184", 0xFFFF7A3CL, 0.11),
        DetectionTypeSpec("Crowd Surge", 3, "Preventive", 0xFFFFCC4DL, 0.09),
        DetectionTypeSpec("Suspicious Loitering", 2, "IPC 268", 0xFFFFCC4DL, 0.12),
        DetectionTypeSpec("Vandalism", 2, "IPC 425", 0xFFFFCC4DL, 0.07)
    )

    private val HOUR_WEIGHTS = doubleArrayOf(
        0.3, 0.2, 0.15, 0.1, 0.1, 0.15, 0.3, 0.4, 0.6, 0.7, 0.8, 0.9,
        0.9, 0.85, 0.8, 0.85, 0.95, 1.1, 1.3, 1.4, 1.4, 1.3, 1.1, 0.8
    )

    private val PATROL_COLORS = longArrayOf(
        0xFFFF3B5CL, 0xFF3BA3FFL, 0xFF26C281L, 0xFFFFCC4DL, 0xFFA06BFFL,
        0xFFFF7A3CL, 0xFF4FD1C5L, 0xFFFF4FA3L, 0xFFFFB03AL, 0xFF8ECBFFL,
        0xFFC3FF4DL, 0xFFFF5E5EL, 0xFF5E8FFFL, 0xFF7FFFBFL, 0xFFFFB0E0L
    )

    private val WEEKDAYS = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    private class MulberryRng(initialSeed: Int = 1337) {
        private var state: Int = initialSeed

        fun next(): Double {
            state += 0x6D2B79F5
            var t = state
            t = (t xor (t ushr 15)) * (t or 1)
            t = t xor (t + ((t xor (t ushr 7)) * (t or 61)))
            val unsigned = (t xor (t ushr 14)).toLong() and 0xFFFFFFFFL
            return unsigned.toDouble() / 4294967296.0
        }

        fun gauss(): Double {
            val u = max(next(), 1e-9)
            val v = max(next(), 1e-9)
            return sqrt(-2.0 * ln(u)) * cos(2.0 * Math.PI * v)
        }
    }

    fun generateSeededIncidents(count: Int = 4200, referenceNowMs: Long = System.currentTimeMillis()): List<IncidentRecord> {
        val rng = MulberryRng(1337)
        val out = ArrayList<IncidentRecord>(count)
        val crimeKeys = CRIME_WEIGHTS.keys.toList()
        val cal = Calendar.getInstance()
        val totalHotspotWeight = HOTSPOTS.sumOf { it.weight }
        val totalHourWeight = HOUR_WEIGHTS.sum()

        for (i in 0 until count) {
            var lat: Double
            var lon: Double
            val pool: List<String>

            if (rng.next() < 0.75) {
                var r = rng.next() * totalHotspotWeight
                var picked = HOTSPOTS.last()
                for (h in HOTSPOTS) {
                    r -= h.weight
                    if (r <= 0.0) {
                        picked = h
                        break
                    }
                }
                lat = picked.lat + rng.gauss() * 0.0035
                lon = picked.lon + rng.gauss() * 0.0035
                pool = picked.crimes
            } else {
                lat = BOUNDS.latMin + rng.next() * (BOUNDS.latMax - BOUNDS.latMin)
                lon = BOUNDS.lonMin + rng.next() * (BOUNDS.lonMax - BOUNDS.lonMin)
                pool = crimeKeys
            }

            lat = lat.coerceIn(BOUNDS.latMin, BOUNDS.latMax)
            lon = lon.coerceIn(BOUNDS.lonMin, BOUNDS.lonMax)

            var hrPick = rng.next() * totalHourWeight
            var hour = 23
            for (h in 0..23) {
                hrPick -= HOUR_WEIGHTS[h]
                if (hrPick <= 0.0) {
                    hour = h
                    break
                }
            }

            val daysAgo = floor(rng.next() * 365.0).toInt()
            val minute = floor(rng.next() * 60.0).toInt()
            cal.timeInMillis = referenceNowMs - daysAgo * 86_400_000L
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val crime = if (rng.next() < 0.7 && pool.isNotEmpty()) {
                pool[floor(rng.next() * pool.size).toInt().coerceIn(0, pool.lastIndex)]
            } else {
                pickWeightedCrime(rng)
            }

            val sevRoll = rng.next()
            val sev = when {
                sevRoll < 0.15 -> 1
                sevRoll < 0.45 -> 2
                sevRoll < 0.75 -> 3
                sevRoll < 0.93 -> 4
                else -> 5
            }

            val stRoll = rng.next()
            val status = when {
                stRoll < 0.40 -> "Reported"
                stRoll < 0.80 -> "Investigating"
                else -> "Closed"
            }

            val locType = LOCATION_TYPES[floor(rng.next() * LOCATION_TYPES.size).toInt().coerceIn(0, LOCATION_TYPES.lastIndex)]
            val mo = MODUS[floor(rng.next() * MODUS.size).toInt().coerceIn(0, MODUS.lastIndex)]
            val dayName = WEEKDAYS[(cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)]

            out.add(
                IncidentRecord(
                    id = i + 1,
                    crime = crime,
                    severity = sev,
                    caseNo = "PDY/2025/${(i + 1).toString().padStart(5, '0')}",
                    station = nearestStationName(lat, lon),
                    timestampMs = cal.timeInMillis,
                    hourOfDay = hour,
                    dayOfWeek = dayName,
                    lat = lat,
                    lon = lon,
                    locationType = locType,
                    status = status,
                    modusOperandi = mo,
                    isSynthetic = true
                )
            )
        }
        return out
    }

    private fun pickWeightedCrime(rng: MulberryRng): String {
        val total = CRIME_WEIGHTS.values.sum()
        var r = rng.next() * total
        for ((k, w) in CRIME_WEIGHTS) {
            r -= w
            if (r <= 0.0) return k
        }
        return "Theft"
    }

    fun nearestStation(lat: Double, lon: Double): PoliceStation {
        var best = STATIONS.first()
        var bestDist = Double.MAX_VALUE
        for (s in STATIONS) {
            val d = (s.lat - lat).pow(2) + (s.lon - lon).pow(2)
            if (d < bestDist) {
                bestDist = d
                best = s
            }
        }
        return best
    }

    fun nearestStationName(lat: Double, lon: Double): String = nearestStation(lat, lon).name

    fun nearestAreaLabel(lat: Double, lon: Double): String {
        var best = HOTSPOTS.first()
        var bestDist = Double.MAX_VALUE
        for (h in HOTSPOTS) {
            val d = haversineKm(lat, lon, h.lat, h.lon)
            if (d < bestDist) {
                bestDist = d
                best = h
            }
        }
        return if (bestDist <= 1.8) best.name else nearestStationName(lat, lon).replace(" PS", " Sector")
    }

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dp = Math.toRadians(lat2 - lat1)
        val dl = Math.toRadians(lon2 - lon1)
        val x = sin(dp / 2).pow(2) + cos(p1) * cos(p2) * sin(dl / 2).pow(2)
        return 2.0 * r * asin(min(1.0, sqrt(x)))
    }

    fun riskColorHex(risk: Int): Long = when {
        risk >= 75 -> 0xFFFF3B5CL
        risk >= 50 -> 0xFFFF7A3CL
        risk >= 25 -> 0xFFFFCC4DL
        else -> 0xFF26C281L
    }

    fun applyFilters(
        incidents: List<IncidentRecord>,
        filters: CrimeMapFilters,
        nowMs: Long = System.currentTimeMillis()
    ): List<IncidentRecord> {
        val cutoff = nowMs - filters.daysWindow * 86_400_000L
        return incidents.filter { p ->
            p.timestampMs >= cutoff &&
                (filters.crimeType == "all" || p.crime == filters.crimeType) &&
                (filters.station == "all" || p.station == filters.station) &&
                p.severity >= filters.minSeverity
        }
    }

    fun detectHotspots(
        list: List<IncidentRecord>,
        minCell: Int = 6,
        minCluster: Int = 8,
        nowMs: Long = System.currentTimeMillis()
    ): List<HotspotCluster> {
        if (list.size < 20) return emptyList()

        data class Cell(val i: Int, val j: Int, val pts: MutableList<IncidentRecord> = mutableListOf())

        val cellMap = HashMap<String, Cell>()
        for (p in list) {
            val i = floor((p.lat - BOUNDS.latMin) / GRID_STEP).toInt()
            val j = floor((p.lon - BOUNDS.lonMin) / GRID_STEP).toInt()
            val key = "$i|$j"
            cellMap.getOrPut(key) { Cell(i, j) }.pts.add(p)
        }

        val denseCells = cellMap.values.filter { it.pts.size >= minCell }
        if (denseCells.isEmpty()) return emptyList()

        val cellIndex = denseCells.associateBy { "${it.i}|${it.j}" }
        val visited = HashSet<String>()
        val clusters = mutableListOf<HotspotCluster>()

        for (start in denseCells) {
            val sk = "${start.i}|${start.j}"
            if (!visited.add(sk)) continue

            val queue = ArrayDeque<Cell>()
            queue.add(start)
            val members = mutableListOf<Cell>()

            while (queue.isNotEmpty()) {
                val cur = queue.removeFirst()
                members.add(cur)
                for (di in -1..1) {
                    for (dj in -1..1) {
                        val nk = "${cur.i + di}|${cur.j + dj}"
                        if (visited.contains(nk)) continue
                        val neighbor = cellIndex[nk]
                        if (neighbor != null) {
                            visited.add(nk)
                            queue.add(neighbor)
                        }
                    }
                }
            }

            val allPts = members.flatMap { it.pts }
            if (allPts.size < minCluster) continue

            val latC = allPts.sumOf { it.lat } / allPts.size
            val lonC = allPts.sumOf { it.lon } / allPts.size
            val hours = HashMap<Int, Int>()
            val dow = HashMap<String, Int>()
            val ctypes = HashMap<String, Int>()
            var sevSum = 0.0
            var recent14Count = 0

            for (p in allPts) {
                hours[p.hourOfDay] = (hours[p.hourOfDay] ?: 0) + 1
                dow[p.dayOfWeek] = (dow[p.dayOfWeek] ?: 0) + 1
                ctypes[p.crime] = (ctypes[p.crime] ?: 0) + 1
                sevSum += p.severity
                if (nowMs - p.timestampMs < 14 * 86_400_000L) recent14Count++
            }

            val peakHour = hours.maxByOrNull { it.value }?.key ?: 19
            val peakDay = dow.maxByOrNull { it.value }?.key ?: "Friday"
            val topCrime = ctypes.maxByOrNull { it.value }?.key ?: "Theft"
            val avgSev = sevSum / allPts.size
            val recentBoost = min(20.0, recent14Count * 1.2)
            val risk = min(100, (allPts.size * 1.5 + avgSev * 7.0 + recentBoost).roundToInt())

            val top5Crimes = ctypes.entries.sortedByDescending { it.value }.take(5)
                .associate { it.key to it.value }

            clusters.add(
                HotspotCluster(
                    lat = latC,
                    lon = lonC,
                    count = allPts.size,
                    peakHour = peakHour,
                    peakDay = peakDay,
                    topCrime = topCrime,
                    avgSeverity = (avgSev * 100).roundToInt() / 100.0,
                    risk = risk,
                    topCrimesBreakdown = top5Crimes,
                    nearestAreaName = nearestAreaLabel(latC, lonC)
                )
            )
        }

        clusters.sortByDescending { it.risk }
        return clusters
    }

    fun emergingHotspots(
        incidents: List<IncidentRecord>,
        nowMs: Long = System.currentTimeMillis()
    ): List<EmergingHotspot> {
        val recentCutoff = nowMs - 7 * 86_400_000L
        val baseCutoff = nowMs - 35 * 86_400_000L
        val rC = HashMap<String, Int>()
        val bC = HashMap<String, Int>()

        for (p in incidents) {
            val key = "${(p.lat / GRID_STEP).roundToInt()}|${(p.lon / GRID_STEP).roundToInt()}"
            if (p.timestampMs >= recentCutoff) {
                rC[key] = (rC[key] ?: 0) + 1
            } else if (p.timestampMs >= baseCutoff) {
                bC[key] = (bC[key] ?: 0) + 1
            }
        }

        val out = mutableListOf<EmergingHotspot>()
        for ((key, recentCount) in rC) {
            if (recentCount < 3) continue
            val basePerWeek = (bC[key] ?: 0) / 4.0
            val growth = (recentCount - basePerWeek) / max(basePerWeek, 1.0)
            if (growth >= 0.5) {
                val parts = key.split("|")
                val lat = parts[0].toInt() * GRID_STEP
                val lon = parts[1].toInt() * GRID_STEP
                out.add(
                    EmergingHotspot(
                        lat = lat,
                        lon = lon,
                        recentCount = recentCount,
                        baselinePerWeek = (basePerWeek * 10).roundToInt() / 10.0,
                        growthPercent = (growth * 100).roundToInt(),
                        nearestAreaName = nearestAreaLabel(lat, lon)
                    )
                )
            }
        }
        out.sortByDescending { it.growthPercent }
        return out.take(20)
    }

    fun predictRiskGrid(
        incidents: List<IncidentRecord>,
        nowMs: Long = System.currentTimeMillis()
    ): List<PredictedRiskCell> {
        val cellMap = HashMap<String, MutableList<IncidentRecord>>()
        for (p in incidents) {
            val key = "${(p.lat / GRID_STEP).roundToInt()}|${(p.lon / GRID_STEP).roundToInt()}"
            cellMap.getOrPut(key) { mutableListOf() }.add(p)
        }

        val out = mutableListOf<PredictedRiskCell>()
        for ((key, pts) in cellMap) {
            if (pts.size < 3) continue
            var recentW = 0.0
            var olderW = 0.0
            var sevSum = 0.0
            val hourHist = IntArray(24)
            for (p in pts) {
                val ageDays = max(0.0, (nowMs - p.timestampMs) / 86_400_000.0)
                val w = kotlin.math.exp(-ageDays / 90.0)
                if (ageDays <= 14.0) recentW += w else olderW += w
                sevSum += p.severity * w
                hourHist[p.hourOfDay.coerceIn(0, 23)]++
            }
            val trend = (recentW + 0.001) / (olderW / 6.0 + 0.001)
            val base = pts.size * 1.1
            val score = min(
                100,
                (base + min(trend, 3.0) * 14.0 + recentW * 3.0 + (sevSum / max(pts.size, 1)) * 2.0).roundToInt()
            )
            if (score < 8) continue
            val parts = key.split("|")
            val lat = parts[0].toInt() * GRID_STEP
            val lon = parts[1].toInt() * GRID_STEP
            val peakHour = hourHist.indices.maxByOrNull { hourHist[it] } ?: 19
            val endHour = (peakHour + 2) % 24
            out.add(
                PredictedRiskCell(
                    lat = lat,
                    lon = lon,
                    risk = score,
                    historicalCount = pts.size,
                    reason = "${pts.size} historical · trend ×${String.format(Locale.US, "%.2f", trend)}",
                    peakHourWindow = "${formatHour(peakHour)}–${formatHour(endHour)}",
                    nearestAreaName = nearestAreaLabel(lat, lon)
                )
            )
        }
        out.sortByDescending { it.risk }
        return out.take(350)
    }

    /**
     * SHIELD PROTOCOL: Historical Aggregate Time-Window & Spatial Vulnerability Assessment.
     * Detects places that showed elevated historical incident concentration around the given hour (+/- 2h)
     * and weekday without claiming individual-crime prediction.
     */
    fun computeShieldTimeVulnerableZones(
        incidents: List<IncidentRecord>,
        currentHour: Int,
        currentDayName: String,
        referenceLat: Double = 11.9330,
        referenceLon: Double = 79.8260
    ): List<ShieldZonePrediction> {
        if (incidents.isEmpty()) return emptyList()

        val windowLabel = "${formatHour((currentHour - 1 + 24) % 24)} – ${formatHour((currentHour + 2) % 24)}"

        return HOTSPOTS.map { spot ->
            val areaIncidents = incidents.filter { p ->
                haversineKm(spot.lat, spot.lon, p.lat, p.lon) <= 0.85
            }
            val windowIncidents = areaIncidents.filter { p ->
                circularHourDiff(p.hourOfDay, currentHour) <= 2
            }

            if (areaIncidents.size < 5) {
                ShieldZonePrediction(
                    areaName = spot.name,
                    lat = spot.lat,
                    lon = spot.lon,
                    distanceKm = (haversineKm(referenceLat, referenceLon, spot.lat, spot.lon) * 10).roundToInt() / 10.0,
                    riskScore = 0,
                    riskLevel = ShieldRiskLevel.INSUFFICIENT_DATA,
                    matchingWindowIncidents = windowIncidents.size,
                    totalAreaIncidents = areaIncidents.size,
                    activeTimeWindow = windowLabel,
                    dominantCrime = spot.crimes.firstOrNull() ?: "General",
                    advisorySummary = "Insufficient historical data in this sector for this window.",
                    peakDays = currentDayName,
                    isSyntheticBaseline = areaIncidents.all { it.isSynthetic }
                )
            } else {
                val avgSev = windowIncidents.takeIf { it.isNotEmpty() }?.map { it.severity }?.average()
                    ?: areaIncidents.map { it.severity }.average()
                val dayMatchCount = windowIncidents.count { it.dayOfWeek.equals(currentDayName, ignoreCase = true) }
                val windowRatio = windowIncidents.size.toDouble() / max(1, areaIncidents.size)
                val rawScore = (
                    (windowIncidents.size * 0.85) +
                        (windowRatio * 100.0 * 0.45) +
                        (avgSev * 6.5) +
                        (dayMatchCount * 1.4)
                    ).roundToInt().coerceIn(5, 98)

                val level = when {
                    rawScore >= 75 -> ShieldRiskLevel.CRITICAL
                    rawScore >= 55 -> ShieldRiskLevel.HIGH
                    rawScore >= 30 -> ShieldRiskLevel.MODERATE
                    else -> ShieldRiskLevel.LOW
                }

                val topCrime = (windowIncidents.ifEmpty { areaIncidents })
                    .groupingBy { it.crime }
                    .eachCount()
                    .maxByOrNull { it.value }?.key ?: (spot.crimes.firstOrNull() ?: "Theft")

                val topDays = areaIncidents.groupingBy { it.dayOfWeek }
                    .eachCount()
                    .entries.sortedByDescending { it.value }
                    .take(2)
                    .joinToString(", ") { it.key.take(3) }

                val advisory = when (level) {
                    ShieldRiskLevel.CRITICAL, ShieldRiskLevel.HIGH ->
                        "Elevated historical incident concentration (${windowIncidents.size} past reports) during $windowLabel ($topCrime). Stay on lit main roads & share live location."
                    ShieldRiskLevel.MODERATE ->
                        "Moderate historical activity around $windowLabel ($topCrime). Stay alert and keep Shield SOS ready."
                    ShieldRiskLevel.LOW ->
                        "Relatively lower historical incident concentration during $windowLabel based on past records."
                    ShieldRiskLevel.INSUFFICIENT_DATA ->
                        "Insufficient historical data."
                }

                ShieldZonePrediction(
                    areaName = spot.name,
                    lat = spot.lat,
                    lon = spot.lon,
                    distanceKm = (haversineKm(referenceLat, referenceLon, spot.lat, spot.lon) * 10).roundToInt() / 10.0,
                    riskScore = rawScore,
                    riskLevel = level,
                    matchingWindowIncidents = windowIncidents.size,
                    totalAreaIncidents = areaIncidents.size,
                    activeTimeWindow = windowLabel,
                    dominantCrime = topCrime,
                    advisorySummary = advisory,
                    peakDays = topDays.ifBlank { currentDayName.take(3) },
                    isSyntheticBaseline = areaIncidents.all { it.isSynthetic }
                )
            }
        }.sortedByDescending { it.riskScore }
    }

    fun evaluatePointVulnerability(
        incidents: List<IncidentRecord>,
        lat: Double,
        lon: Double,
        currentHour: Int,
        currentDayName: String
    ): ShieldZonePrediction {
        val windowLabel = "${formatHour((currentHour - 1 + 24) % 24)} – ${formatHour((currentHour + 2) % 24)}"
        val nearby = incidents.filter { haversineKm(lat, lon, it.lat, it.lon) <= 0.9 }
        val inBounds = lat in (BOUNDS.latMin - 0.05)..(BOUNDS.latMax + 0.05) &&
            lon in (BOUNDS.lonMin - 0.05)..(BOUNDS.lonMax + 0.05)

        if (!inBounds || nearby.size < 4) {
            return ShieldZonePrediction(
                areaName = if (inBounds) nearestAreaLabel(lat, lon) else "Outside Puducherry Coverage",
                lat = lat,
                lon = lon,
                distanceKm = 0.0,
                riskScore = 0,
                riskLevel = ShieldRiskLevel.INSUFFICIENT_DATA,
                matchingWindowIncidents = 0,
                totalAreaIncidents = nearby.size,
                activeTimeWindow = windowLabel,
                dominantCrime = "Insufficient Data",
                advisorySummary = "Insufficient local historical records for this location. Stay aware of surroundings and dial 112 in an emergency.",
                peakDays = currentDayName.take(3),
                isSyntheticBaseline = true
            )
        }

        val winPts = nearby.filter { circularHourDiff(it.hourOfDay, currentHour) <= 2 }
        val avgSev = (winPts.ifEmpty { nearby }).map { it.severity }.average()
        val ratio = winPts.size.toDouble() / max(1, nearby.size)
        val score = ((winPts.size * 0.9) + (ratio * 45.0) + (avgSev * 6.5)).roundToInt().coerceIn(8, 97)
        val level = when {
            score >= 75 -> ShieldRiskLevel.CRITICAL
            score >= 55 -> ShieldRiskLevel.HIGH
            score >= 30 -> ShieldRiskLevel.MODERATE
            else -> ShieldRiskLevel.LOW
        }
        val topCrime = (winPts.ifEmpty { nearby }).groupingBy { it.crime }.eachCount().maxByOrNull { it.value }?.key ?: "Theft"

        return ShieldZonePrediction(
            areaName = nearestAreaLabel(lat, lon),
            lat = lat,
            lon = lon,
            distanceKm = 0.0,
            riskScore = score,
            riskLevel = level,
            matchingWindowIncidents = winPts.size,
            totalAreaIncidents = nearby.size,
            activeTimeWindow = windowLabel,
            dominantCrime = topCrime,
            advisorySummary = "Within 0.9 km: ${winPts.size} past records matched $windowLabel ($topCrime).",
            peakDays = currentDayName.take(3),
            isSyntheticBaseline = nearby.all { it.isSynthetic }
        )
    }

    fun optimizePatrols(
        incidents: List<IncidentRecord>,
        nUnits: Int,
        horizonMinutes: Int,
        priority: String
    ): List<PatrolRoute> {
        val grid = predictRiskGrid(incidents).take(50)
        val clampedUnits = nUnits.coerceIn(1, STATIONS.size)
        val visited = HashSet<Int>()
        val routes = mutableListOf<PatrolRoute>()

        for (i in 0 until clampedUnits) {
            val st = STATIONS[i]
            var curLat = st.lat
            var curLon = st.lon
            var tLeft = horizonMinutes.toDouble()
            val path = mutableListOf(
                PatrolStop(st.lat, st.lon, 0, 0, st.name)
            )

            while (tLeft > 8.0) {
                var bestIdx = -1
                var bestScore = Double.MAX_VALUE
                for (j in grid.indices) {
                    if (visited.contains(j)) continue
                    val g = grid[j]
                    val d = haversineKm(curLat, curLon, g.lat, g.lon)
                    val score = when (priority) {
                        "severity" -> d / max(g.historicalCount, 1)
                        else -> d / max(g.risk, 1)
                    }
                    if (score < bestScore) {
                        bestScore = score
                        bestIdx = j
                    }
                }
                if (bestIdx < 0) break
                val g = grid[bestIdx]
                val d = haversineKm(curLat, curLon, g.lat, g.lon)
                val tMin = (d / 25.0) * 60.0
                if (tMin > tLeft) break
                visited.add(bestIdx)
                tLeft -= tMin
                path.add(PatrolStop(g.lat, g.lon, g.risk, path.size, g.nearestAreaName))
                curLat = g.lat
                curLon = g.lon
            }

            routes.add(
                PatrolRoute(
                    unit = "PCR-${(i + 1).toString().padStart(2, '0')}",
                    station = st.name,
                    stops = path.size - 1,
                    timeUsedMinutes = (horizonMinutes - tLeft).roundToInt(),
                    colorHex = PATROL_COLORS[i % PATROL_COLORS.size],
                    path = path
                )
            )
        }
        return routes
    }

    fun spawnSimulatedDetection(
        seqNumber: Int,
        forcedCamId: String? = null,
        random: Random = Random.Default
    ): LiveDetection {
        val online = CAMERAS.filter { it.online }
        val cam = if (forcedCamId != null) {
            CAMERAS.find { it.id == forcedCamId } ?: online.first()
        } else {
            online[random.nextInt(online.size)]
        }

        val totalW = DETECTION_TYPES.sumOf { it.weight }
        var r = random.nextDouble() * totalW
        var spec = DETECTION_TYPES.first()
        for (d in DETECTION_TYPES) {
            r -= d.weight
            if (r <= 0.0) {
                spec = d
                break
            }
        }

        val dist = (0.35 + random.nextDouble() * 0.55) * cam.rangeMeters
        val ang = Math.toRadians(cam.bearing + (random.nextDouble() - 0.5) * cam.fov * 0.85)
        val earthR = 6371000.0
        val dx = sin(ang) * dist
        val dy = cos(ang) * dist
        val lat = cam.lat + Math.toDegrees(dy / earthR)
        val lon = cam.lon + Math.toDegrees(dx / (earthR * cos(Math.toRadians(cam.lat))))
        val year = Calendar.getInstance().get(Calendar.YEAR)

        return LiveDetection(
            id = "DET-${seqNumber.toString().padStart(4, '0')}",
            camId = cam.id,
            camName = cam.name,
            lat = lat,
            lon = lon,
            type = spec.name,
            severity = spec.severity,
            ipc = spec.ipc,
            colorHex = spec.colorHex,
            confidence = ((0.74 + random.nextDouble() * 0.25) * 100).roundToInt() / 100.0,
            bornAtMs = System.currentTimeMillis(),
            status = DetectionStatus.NEW,
            caseNo = "PDY/$year/${(1000 + seqNumber).toString().padStart(5, '0')}",
            nearestStation = nearestStationName(lat, lon),
            seed = random.nextDouble() * 10.0
        )
    }

    fun computeStationStats(filtered: List<IncidentRecord>): List<StationStatRow> {
        val grouped = filtered.groupBy { it.station }
        return grouped.entries.map { (station, list) ->
            val topCrime = list.groupingBy { it.crime }.eachCount().maxByOrNull { it.value }?.key ?: "Theft"
            val avgSev = list.map { it.severity }.average()
            StationStatRow(
                stationName = station.replace(" PS", ""),
                caseCount = list.size,
                avgSeverity = (avgSev * 10).roundToInt() / 10.0,
                topCrime = topCrime
            )
        }.sortedByDescending { it.caseCount }
    }

    fun buildIncidentsCsv(incidents: List<IncidentRecord>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val sb = StringBuilder()
        sb.appendLine("case_no,id,crime,severity,station,datetime,lat,lon,location,status,modus_operandi,source")
        for (p in incidents) {
            val source = if (p.isSynthetic) "Synthetic-FIR-Model" else "User-Report"
            sb.appendLine(
                listOf(
                    p.caseNo,
                    p.id.toString(),
                    p.crime,
                    p.severity.toString(),
                    p.station,
                    sdf.format(Date(p.timestampMs)),
                    String.format(Locale.US, "%.5f", p.lat),
                    String.format(Locale.US, "%.5f", p.lon),
                    p.locationType,
                    p.status,
                    p.modusOperandi,
                    source
                ).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
            )
        }
        return sb.toString()
    }

    private fun circularHourDiff(a: Int, b: Int): Int {
        val d = abs(a - b)
        return min(d, 24 - d)
    }

    fun formatHour(h: Int): String {
        val norm = ((h % 24) + 24) % 24
        val amPm = if (norm < 12) "AM" else "PM"
        val h12 = when {
            norm == 0 -> 12
            norm > 12 -> norm - 12
            else -> norm
        }
        return "$h12 $amPm"
    }
}

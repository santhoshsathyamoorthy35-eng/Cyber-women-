package com.example.data.model

data class GeoPoint(
    val lat: Double,
    val lon: Double
)

data class CrimeBounds(
    val latMin: Double = 11.850,
    val latMax: Double = 11.985,
    val lonMin: Double = 79.760,
    val lonMax: Double = 79.865
)

data class PoliceStation(
    val name: String,
    val lat: Double,
    val lon: Double
)

data class StaticHotspot(
    val name: String,
    val lat: Double,
    val lon: Double,
    val weight: Double,
    val crimes: List<String>
)

data class CctvCamera(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val bearing: Float,
    val fov: Float,
    val rangeMeters: Float,
    val type: String,
    val online: Boolean = true
)

data class DetectionTypeSpec(
    val name: String,
    val severity: Int,
    val ipc: String,
    val colorHex: Long,
    val weight: Double
)

data class IncidentRecord(
    val id: Int,
    val crime: String,
    val severity: Int,
    val caseNo: String,
    val station: String,
    val timestampMs: Long,
    val hourOfDay: Int,
    val dayOfWeek: String,
    val lat: Double,
    val lon: Double,
    val locationType: String,
    val status: String,
    val modusOperandi: String,
    val isSynthetic: Boolean = true
)

data class HotspotCluster(
    val lat: Double,
    val lon: Double,
    val count: Int,
    val peakHour: Int,
    val peakDay: String,
    val topCrime: String,
    val avgSeverity: Double,
    val risk: Int,
    val topCrimesBreakdown: Map<String, Int>,
    val nearestAreaName: String
)

data class EmergingHotspot(
    val lat: Double,
    val lon: Double,
    val recentCount: Int,
    val baselinePerWeek: Double,
    val growthPercent: Int,
    val nearestAreaName: String
)

data class PredictedRiskCell(
    val lat: Double,
    val lon: Double,
    val risk: Int,
    val historicalCount: Int,
    val reason: String,
    val peakHourWindow: String,
    val nearestAreaName: String
)

enum class ShieldRiskLevel(val label: String) {
    LOW("LOW"),
    MODERATE("MODERATE"),
    HIGH("HIGH"),
    CRITICAL("CRITICAL"),
    INSUFFICIENT_DATA("INSUFFICIENT DATA")
}

data class ShieldZonePrediction(
    val areaName: String,
    val lat: Double,
    val lon: Double,
    val distanceKm: Double,
    val riskScore: Int,
    val riskLevel: ShieldRiskLevel,
    val matchingWindowIncidents: Int,
    val totalAreaIncidents: Int,
    val activeTimeWindow: String,
    val dominantCrime: String,
    val advisorySummary: String,
    val peakDays: String,
    val isSyntheticBaseline: Boolean = true
)

data class PatrolStop(
    val lat: Double,
    val lon: Double,
    val risk: Int,
    val stopIndex: Int,
    val label: String
)

data class PatrolRoute(
    val unit: String,
    val station: String,
    val stops: Int,
    val timeUsedMinutes: Int,
    val colorHex: Long,
    val path: List<PatrolStop>
)

enum class DetectionStatus {
    NEW,
    ACKNOWLEDGED,
    DISPATCHED,
    RESOLVED
}

data class LiveDetection(
    val id: String,
    val camId: String,
    val camName: String,
    val lat: Double,
    val lon: Double,
    val type: String,
    val severity: Int,
    val ipc: String,
    val colorHex: Long,
    val confidence: Double,
    val bornAtMs: Long,
    val status: DetectionStatus,
    val caseNo: String,
    val nearestStation: String,
    val dispatchedUnit: String? = null,
    val seed: Double
)

data class CrimeMapFilters(
    val daysWindow: Int = 90,
    val crimeType: String = "all",
    val station: String = "all",
    val minSeverity: Int = 1,
    val searchQuery: String = ""
)

data class MapLayersState(
    val heatmap: Boolean = true,
    val incidentPoints: Boolean = true,
    val hotspotClusters: Boolean = true,
    val predictedRisk7Day: Boolean = true,
    val emergingAlerts: Boolean = false,
    val policeStations: Boolean = true,
    val patrolRoutes: Boolean = false,
    val userLocation: Boolean = true,
    val dataBeams: Boolean = true,
    val riskTowers3D: Boolean = true,
    val cctvNetwork: Boolean = true,
    val cameraFovCones: Boolean = true,
    val liveDetections: Boolean = true,
    val shieldTimeZones: Boolean = true
)

data class Camera3DSettings(
    val orbit: Boolean = false,
    val buildings3d: Boolean = true,
    val tilted: Boolean = true,
    val radarSweep: Boolean = true,
    val camFov: Boolean = true
)

data class StationStatRow(
    val stationName: String,
    val caseCount: Int,
    val avgSeverity: Double,
    val topCrime: String
)

data class MapNotificationItem(
    val id: String,
    val message: String,
    val level: String, // "red", "gold", "green", "blue"
    val timestampMs: Long
)

package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.CachedReportEntity
import com.example.data.local.CachedSosEntity
import com.example.data.local.CyberWomenDao
import com.example.data.local.PreferencesStore
import com.example.data.local.QuizBadgeEntity
import com.example.data.model.Camera3DSettings
import com.example.data.model.CitizenReport
import com.example.data.model.CrimeMapFilters
import com.example.data.model.DetectionStatus
import com.example.data.model.EmergingHotspot
import com.example.data.model.GeoPoint
import com.example.data.model.HotspotCluster
import com.example.data.model.IncidentRecord
import com.example.data.model.LiveDetection
import com.example.data.model.MapLayersState
import com.example.data.model.MapNotificationItem
import com.example.data.model.PatrolRoute
import com.example.data.model.PredictedRiskCell
import com.example.data.model.ShieldZonePrediction
import com.example.data.model.SosEvent
import com.example.data.model.StationStatRow
import com.example.data.model.TrustedContact
import com.example.data.model.UserProfile
import com.example.data.remote.FirestoreRepository
import com.example.domain.CrimeMapEngine
import com.example.service.DeviceLocationState
import com.example.service.LocationSharingService
import com.example.service.LocationTracker
import com.example.util.AppLanguage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

enum class SensitivityLevel(val label: String, val intervalMs: Long) {
    LOW("Low", 6000L),
    MEDIUM("Medium", 3500L),
    HIGH("High", 1800L)
}

class MainViewModel(
    private val firestoreRepo: FirestoreRepository,
    private val dao: CyberWomenDao,
    private val prefs: PreferencesStore,
    private val currentUserId: String,
    private val initialDisplayName: String,
    private val initialEmail: String?
) : ViewModel() {

    private val syntheticIncidents: List<IncidentRecord> = CrimeMapEngine.generateSeededIncidents()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _trustedContacts = MutableStateFlow<List<TrustedContact>>(emptyList())
    val trustedContacts: StateFlow<List<TrustedContact>> = _trustedContacts.asStateFlow()

    private val _cloudSosEvents = MutableStateFlow<List<SosEvent>>(emptyList())
    private val _cloudReports = MutableStateFlow<List<CitizenReport>>(emptyList())

    val cachedSosEvents: StateFlow<List<CachedSosEntity>> = dao.observeCachedSos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cachedReports: StateFlow<List<CachedReportEntity>> = dao.observeCachedReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val mergedSosHistory: StateFlow<List<SosEvent>> = combine(
        _cloudSosEvents,
        cachedSosEvents
    ) { cloud, local ->
        val cloudIds = cloud.map { it.id }.toSet()
        val localFallbacks = local
            .filter { it.id !in cloudIds }
            .map { entity ->
                SosEvent(
                    id = entity.id,
                    userId = currentUserId,
                    status = entity.status,
                    triggerType = entity.triggerType,
                    lat = entity.lat,
                    lon = entity.lon,
                    locationLabel = entity.locationLabel,
                    message = entity.message,
                    deliveryNote = entity.deliveryNote
                )
            }
        cloud + localFallbacks
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val mergedCitizenReports: StateFlow<List<CitizenReport>> = combine(
        _cloudReports,
        cachedReports
    ) { cloud, local ->
        val cloudIds = cloud.map { it.id }.toSet()
        val localFallbacks = local
            .filter { it.id !in cloudIds }
            .map { entity ->
                CitizenReport(
                    id = entity.id,
                    userId = currentUserId,
                    category = entity.category,
                    description = entity.description,
                    lat = entity.lat,
                    lon = entity.lon,
                    nearestStation = entity.nearestStation,
                    severity = entity.severity.toLong(),
                    status = entity.status
                )
            }
        cloud + localFallbacks
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val language: StateFlow<AppLanguage> = prefs.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AppLanguage.EN)

    val selectedCity: StateFlow<String> = prefs.selectedCityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "Puducherry")

    val shakeToSosEnabled: StateFlow<Boolean> = prefs.shakeToSosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val checklistItems: StateFlow<Set<String>> = prefs.checklistItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

    val earnedBadges: StateFlow<List<QuizBadgeEntity>> = dao.observeBadges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val deviceLocation: StateFlow<DeviceLocationState> = LocationTracker.location

    // ==================== CRIME MAP & SHIELD STATE ====================
    private val _filters = MutableStateFlow(CrimeMapFilters())
    val filters: StateFlow<CrimeMapFilters> = _filters.asStateFlow()

    private val _layers = MutableStateFlow(MapLayersState())
    val layers: StateFlow<MapLayersState> = _layers.asStateFlow()

    private val _camera3D = MutableStateFlow(Camera3DSettings())
    val camera3D: StateFlow<Camera3DSettings> = _camera3D.asStateFlow()

    private val _mapFocusPoint = MutableStateFlow<GeoPoint?>(null)
    val mapFocusPoint: StateFlow<GeoPoint?> = _mapFocusPoint.asStateFlow()

    private val calNow = Calendar.getInstance()
    private val _shieldHour = MutableStateFlow(calNow.get(Calendar.HOUR_OF_DAY))
    val shieldHour: StateFlow<Int> = _shieldHour.asStateFlow()

    private val weekdays = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    private val _shieldDayName = MutableStateFlow(weekdays[(calNow.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)])
    val shieldDayName: StateFlow<String> = _shieldDayName.asStateFlow()

    val allIncidents: StateFlow<List<IncidentRecord>> = mergedCitizenReports
        .combine(MutableStateFlow(syntheticIncidents)) { userReports, baseline ->
            val userIncidents = userReports.mapIndexed { idx, rep ->
                val ts = rep.createdAt?.toDate()?.time ?: System.currentTimeMillis()
                val c = Calendar.getInstance().apply { timeInMillis = ts }
                IncidentRecord(
                    id = 100_000 + idx,
                    crime = rep.category.ifBlank { "Public Nuisance" },
                    severity = rep.severity.toInt().coerceIn(1, 5),
                    caseNo = "USR/${rep.id.takeLast(6).uppercase()}",
                    station = rep.nearestStation.ifBlank { CrimeMapEngine.nearestStationName(rep.lat, rep.lon) },
                    timestampMs = ts,
                    hourOfDay = c.get(Calendar.HOUR_OF_DAY),
                    dayOfWeek = weekdays[(c.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)],
                    lat = rep.lat,
                    lon = rep.lon,
                    locationType = "citizen-report",
                    status = rep.status,
                    modusOperandi = "community-alert",
                    isSynthetic = false
                )
            }
            baseline + userIncidents
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), syntheticIncidents)

    val filteredIncidents: StateFlow<List<IncidentRecord>> = combine(
        allIncidents,
        _filters
    ) { all, f ->
        CrimeMapEngine.applyFilters(all, f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val hotspots: StateFlow<List<HotspotCluster>> = filteredIncidents
        .combine(MutableStateFlow(Unit)) { list, _ -> CrimeMapEngine.detectHotspots(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val emergingHotspots: StateFlow<List<EmergingHotspot>> = allIncidents
        .combine(MutableStateFlow(Unit)) { list, _ -> CrimeMapEngine.emergingHotspots(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val predictedRiskGrid: StateFlow<List<PredictedRiskCell>> = allIncidents
        .combine(MutableStateFlow(Unit)) { list, _ -> CrimeMapEngine.predictRiskGrid(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val stationStats: StateFlow<List<StationStatRow>> = filteredIncidents
        .combine(MutableStateFlow(Unit)) { list, _ -> CrimeMapEngine.computeStationStats(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val shieldZones: StateFlow<List<ShieldZonePrediction>> = combine(
        allIncidents,
        _shieldHour,
        _shieldDayName,
        deviceLocation
    ) { incidents, hr, day, loc ->
        CrimeMapEngine.computeShieldTimeVulnerableZones(incidents, hr, day, loc.lat, loc.lon)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val currentLocationShieldAdvisory: StateFlow<ShieldZonePrediction?> = combine(
        allIncidents,
        _shieldHour,
        _shieldDayName,
        deviceLocation
    ) { incidents, hr, day, loc ->
        CrimeMapEngine.evaluatePointVulnerability(incidents, loc.lat, loc.lon, hr, day)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    // ==================== PATROL VRP STATE ====================
    private val _patrolUnitCount = MutableStateFlow(5)
    val patrolUnitCount: StateFlow<Int> = _patrolUnitCount.asStateFlow()

    private val _patrolHorizonMin = MutableStateFlow(60)
    val patrolHorizonMin: StateFlow<Int> = _patrolHorizonMin.asStateFlow()

    private val _patrolPriority = MutableStateFlow("risk")
    val patrolPriority: StateFlow<String> = _patrolPriority.asStateFlow()

    private val _patrolRoutes = MutableStateFlow<List<PatrolRoute>>(emptyList())
    val patrolRoutes: StateFlow<List<PatrolRoute>> = _patrolRoutes.asStateFlow()

    // ==================== LIVE CCTV AI DETECTION TICKER ====================
    private val _detRunning = MutableStateFlow(true)
    val detRunning: StateFlow<Boolean> = _detRunning.asStateFlow()

    private val _detSensitivity = MutableStateFlow(SensitivityLevel.MEDIUM)
    val detSensitivity: StateFlow<SensitivityLevel> = _detSensitivity.asStateFlow()

    private val _autoFocusDetection = MutableStateFlow(false)
    val autoFocusDetection: StateFlow<Boolean> = _autoFocusDetection.asStateFlow()

    private val _autoDispatchDetection = MutableStateFlow(false)
    val autoDispatchDetection: StateFlow<Boolean> = _autoDispatchDetection.asStateFlow()

    private val _detections = MutableStateFlow<List<LiveDetection>>(emptyList())
    val detections: StateFlow<List<LiveDetection>> = _detections.asStateFlow()

    private val _totalDetectionCount = MutableStateFlow(0)
    val totalDetectionCount: StateFlow<Int> = _totalDetectionCount.asStateFlow()

    private val _cctvWallOpen = MutableStateFlow(false)
    val cctvWallOpen: StateFlow<Boolean> = _cctvWallOpen.asStateFlow()

    private val _notifications = MutableStateFlow<List<MapNotificationItem>>(emptyList())
    val notifications: StateFlow<List<MapNotificationItem>> = _notifications.asStateFlow()

    // ==================== SOS COUNTDOWN & STATUS ====================
    private val _sosCountdownSec = MutableStateFlow<Int?>(null)
    val sosCountdownSec: StateFlow<Int?> = _sosCountdownSec.asStateFlow()

    private val _activeSosId = MutableStateFlow<String?>(null)
    val activeSosId: StateFlow<String?> = _activeSosId.asStateFlow()

    private val _statusToast = MutableStateFlow<String?>(null)
    val statusToast: StateFlow<String?> = _statusToast.asStateFlow()

    private var detectionSeq = 0
    private var detectionTickerJob: Job? = null
    private var sosCountdownJob: Job? = null

    init {
        ensureProfileAndObserve()
        runPatrolOptimization()
        startDetectionTicker()
        // Spawn two initial detections immediately so Live Watch feels alive
        spawnManualDetection("CAM-01")
        spawnManualDetection("CAM-05")
        pushNotification("Shield Protocol active · Puducherry 3D Command initialized", "blue")
    }

    private fun ensureProfileAndObserve() {
        viewModelScope.launch {
            firestoreRepo.ensureUserProfile(currentUserId, initialDisplayName, initialEmail)
        }
        viewModelScope.launch {
            firestoreRepo.observeUserProfile(currentUserId).collect { _userProfile.value = it }
        }
        viewModelScope.launch {
            firestoreRepo.observeTrustedContacts(currentUserId).collect { _trustedContacts.value = it }
        }
        viewModelScope.launch {
            firestoreRepo.observeSosEvents(currentUserId).collect { _cloudSosEvents.value = it }
        }
        viewModelScope.launch {
            firestoreRepo.observeCitizenReports(currentUserId).collect { _cloudReports.value = it }
        }
    }

    // ==================== LANGUAGE & PREFERENCES ====================
    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            prefs.setLanguage(lang)
            firestoreRepo.updateUserPreferences(currentUserId, preferredLanguage = lang.code)
        }
    }

    fun setSelectedCity(city: String) {
        viewModelScope.launch {
            prefs.setSelectedCity(city)
            firestoreRepo.updateUserPreferences(currentUserId, selectedCity = city)
            pushNotification("Monitoring sector switched to $city", "blue")
        }
    }

    fun setShakeToSos(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setShakeToSos(enabled)
            firestoreRepo.updateUserPreferences(currentUserId, shakeToSosEnabled = enabled)
            showToast(if (enabled) "Shake-to-SOS enabled" else "Shake-to-SOS disabled")
        }
    }

    fun toggleChecklistItem(itemId: String) {
        viewModelScope.launch { prefs.toggleChecklistItem(itemId) }
    }

    fun updateCustomSosMessage(message: String) {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            firestoreRepo.updateUserPreferences(currentUserId, customSosMessage = trimmed)
                .onSuccess { showToast("Custom SOS message saved") }
                .onFailure { showToast("Could not save SOS message: ${it.localizedMessage}") }
        }
    }

    fun refreshDeviceLocation(context: Context) {
        viewModelScope.launch {
            val loc = LocationTracker.fetchCurrentLocation(context)
            if (loc.isLiveGps) {
                showToast("GPS locked (${loc.accuracyMeters.toInt()}m accuracy)")
            }
        }
    }

    // ==================== FILTERS, LAYERS & 3D CONTROLS ====================
    fun updateFilters(transform: (CrimeMapFilters) -> CrimeMapFilters) {
        _filters.value = transform(_filters.value)
    }

    fun resetFilters() {
        _filters.value = CrimeMapFilters()
    }

    fun updateLayers(transform: (MapLayersState) -> MapLayersState) {
        _layers.value = transform(_layers.value)
    }

    fun updateCamera3D(transform: (Camera3DSettings) -> Camera3DSettings) {
        _camera3D.value = transform(_camera3D.value)
    }

    fun focusMapOn(lat: Double, lon: Double) {
        _mapFocusPoint.value = GeoPoint(lat, lon)
    }

    fun clearMapFocus() {
        _mapFocusPoint.value = null
    }

    fun setShieldSimulationTime(hour: Int, dayName: String) {
        _shieldHour.value = ((hour % 24) + 24) % 24
        _shieldDayName.value = dayName
    }

    fun resetShieldTimeToNow() {
        val c = Calendar.getInstance()
        _shieldHour.value = c.get(Calendar.HOUR_OF_DAY)
        _shieldDayName.value = weekdays[(c.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)]
    }

    // ==================== PATROL VRP ====================
    fun updatePatrolConfig(units: Int? = null, horizonMin: Int? = null, priority: String? = null) {
        units?.let { _patrolUnitCount.value = it.coerceIn(1, CrimeMapEngine.STATIONS.size) }
        horizonMin?.let { _patrolHorizonMin.value = it.coerceIn(30, 180) }
        priority?.let { _patrolPriority.value = it }
    }

    fun runPatrolOptimization() {
        val routes = CrimeMapEngine.optimizePatrols(
            incidents = allIncidents.value,
            nUnits = _patrolUnitCount.value,
            horizonMinutes = _patrolHorizonMin.value,
            priority = _patrolPriority.value
        )
        _patrolRoutes.value = routes
        _layers.value = _layers.value.copy(patrolRoutes = true)
    }

    // ==================== LIVE DETECTION ENGINE ====================
    private fun startDetectionTicker() {
        detectionTickerJob?.cancel()
        detectionTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(_detSensitivity.value.intervalMs)
                if (_detRunning.value) {
                    spawnManualDetection()
                }
            }
        }
    }

    fun toggleDetectionEngine() {
        _detRunning.value = !_detRunning.value
        pushNotification(
            if (_detRunning.value) "AI Watchdog Stream resumed" else "AI Watchdog Stream paused",
            "gold"
        )
    }

    fun setDetectionSensitivity(level: SensitivityLevel) {
        _detSensitivity.value = level
        startDetectionTicker()
    }

    fun setAutoFocusDetection(enabled: Boolean) {
        _autoFocusDetection.value = enabled
    }

    fun setAutoDispatchDetection(enabled: Boolean) {
        _autoDispatchDetection.value = enabled
    }

    fun setCctvWallOpen(open: Boolean) {
        _cctvWallOpen.value = open
    }

    fun spawnManualDetection(forcedCamId: String? = null) {
        detectionSeq += 1
        var det = CrimeMapEngine.spawnSimulatedDetection(detectionSeq, forcedCamId)
        if (_autoDispatchDetection.value && det.severity >= 4) {
            val idx = CrimeMapEngine.STATIONS.indexOfFirst { it.name == det.nearestStation }
                .takeIf { it >= 0 } ?: 0
            det = det.copy(
                status = DetectionStatus.DISPATCHED,
                dispatchedUnit = "PCR-${(idx + 1).toString().padStart(2, '0')}"
            )
        }
        val updated = (listOf(det) + _detections.value).take(25)
        _detections.value = updated
        _totalDetectionCount.value += 1
        if (det.severity >= 4) {
            pushNotification("AI Detect [${det.camId}]: ${det.type} (Sev ${det.severity}/5)", "red")
        }
        if (_autoFocusDetection.value) {
            focusMapOn(det.lat, det.lon)
        }
    }

    fun acknowledgeDetection(detId: String) {
        _detections.value = _detections.value.map {
            if (it.id == detId) it.copy(status = DetectionStatus.ACKNOWLEDGED) else it
        }
        pushNotification("Detection $detId acknowledged by operator", "blue")
    }

    fun dispatchNearestUnit(detId: String) {
        _detections.value = _detections.value.map {
            if (it.id == detId) {
                val idx = CrimeMapEngine.STATIONS.indexOfFirst { st -> st.name == it.nearestStation }
                    .takeIf { i -> i >= 0 } ?: 0
                val unit = "PCR-${(idx + 1).toString().padStart(2, '0')}"
                pushNotification("Dispatched $unit to ${it.camName} (${it.type})", "green")
                it.copy(status = DetectionStatus.DISPATCHED, dispatchedUnit = unit)
            } else it
        }
    }

    fun resolveDetection(detId: String) {
        _detections.value = _detections.value.map {
            if (it.id == detId) it.copy(status = DetectionStatus.RESOLVED) else it
        }
    }

    fun clearDetections() {
        _detections.value = emptyList()
    }

    // ==================== TRUSTED CONTACTS CRUD ====================
    fun addTrustedContact(name: String, phone: String, relationship: String, isPrimary: Boolean) {
        if (name.isBlank() || phone.isBlank()) {
            showToast("Name and phone number are required")
            return
        }
        viewModelScope.launch {
            val priority = (_trustedContacts.value.size + 1).coerceIn(1, 10)
            firestoreRepo.addTrustedContact(currentUserId, name, phone, relationship, priority, isPrimary)
                .onSuccess { showToast("Trusted contact saved") }
                .onFailure { showToast("Failed to save contact: ${it.localizedMessage}") }
        }
    }

    fun updateTrustedContact(contact: TrustedContact) {
        viewModelScope.launch {
            firestoreRepo.updateTrustedContact(currentUserId, contact)
                .onSuccess { showToast("Contact updated") }
                .onFailure { showToast("Update failed: ${it.localizedMessage}") }
        }
    }

    fun deleteTrustedContact(contactId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteTrustedContact(currentUserId, contactId)
                .onSuccess { showToast("Contact removed") }
                .onFailure { showToast("Could not remove contact: ${it.localizedMessage}") }
        }
    }

    // ==================== SHIELD SOS TRIGGER & LIFECYCLE ====================
    fun startSosCountdown(context: Context, triggerType: String = "button") {
        if (_sosCountdownSec.value != null || _activeSosId.value != null) return
        sosCountdownJob?.cancel()
        sosCountdownJob = viewModelScope.launch {
            for (sec in 5 downTo 1) {
                _sosCountdownSec.value = sec
                delay(1000L)
            }
            _sosCountdownSec.value = null
            triggerSosNow(context, triggerType)
        }
    }

    fun cancelSosCountdown(context: Context) {
        sosCountdownJob?.cancel()
        _sosCountdownSec.value = null
        val activeId = _activeSosId.value
        if (activeId != null) {
            viewModelScope.launch {
                val note = "SOS cancelled by user."
                firestoreRepo.updateSosEventStatus(currentUserId, activeId, "cancelled", note)
                dao.upsertSos(
                    CachedSosEntity(
                        id = activeId,
                        status = "cancelled",
                        triggerType = "button",
                        lat = deviceLocation.value.lat,
                        lon = deviceLocation.value.lon,
                        locationLabel = CrimeMapEngine.nearestAreaLabel(deviceLocation.value.lat, deviceLocation.value.lon),
                        message = _userProfile.value?.customSosMessage ?: "SOS Emergency",
                        deliveryNote = note,
                        timestampMs = System.currentTimeMillis()
                    )
                )
                _activeSosId.value = null
                runCatching { LocationSharingService.stop(context) }
                showToast("SOS Alert Cancelled")
            }
        }
    }

    fun triggerSosNow(context: Context, triggerType: String = "button") {
        sosCountdownJob?.cancel()
        _sosCountdownSec.value = null
        viewModelScope.launch {
            val freshLoc = LocationTracker.fetchCurrentLocation(context)
            val areaLabel = CrimeMapEngine.nearestAreaLabel(freshLoc.lat, freshLoc.lon)
            val contacts = _trustedContacts.value
            val contactIds = contacts.map { it.id }
            val customMsg = _userProfile.value?.customSosMessage
                ?: "EMERGENCY SOS: I need immediate help. Please check my location."
            val note = if (contacts.isEmpty()) {
                "Logged in Shield SOS history (no trusted contacts added yet). Call 112 directly for emergency response."
            } else {
                "Logged to cloud SOS history with ${contacts.size} trusted contact(s) selected. Automatic SMS gateway is not active — use Call 112 or Share Location directly."
            }

            // Always persist local Room copy first for offline resilience
            val tempLocalId = "sos_local_${UUID.randomUUID().toString().take(12)}"
            dao.upsertSos(
                CachedSosEntity(
                    id = tempLocalId,
                    status = "queued_local",
                    triggerType = triggerType,
                    lat = freshLoc.lat,
                    lon = freshLoc.lon,
                    locationLabel = areaLabel,
                    message = customMsg,
                    deliveryNote = "Queued locally; syncing to cloud SOS log...",
                    timestampMs = System.currentTimeMillis()
                )
            )

            val res = firestoreRepo.createSosEvent(
                userId = currentUserId,
                status = "logged_cloud",
                triggerType = triggerType,
                lat = freshLoc.lat,
                lon = freshLoc.lon,
                accuracyMeters = freshLoc.accuracyMeters.toDouble(),
                locationLabel = areaLabel,
                message = customMsg,
                contactIds = contactIds,
                deliveryNote = note
            )
            res.onSuccess { cloudId ->
                _activeSosId.value = cloudId
                dao.upsertSos(
                    CachedSosEntity(
                        id = cloudId,
                        status = "logged_cloud",
                        triggerType = triggerType,
                        lat = freshLoc.lat,
                        lon = freshLoc.lon,
                        locationLabel = areaLabel,
                        message = customMsg,
                        deliveryNote = note,
                        timestampMs = System.currentTimeMillis()
                    )
                )
                pushNotification("SOS Logged ($areaLabel) · Call 112 for immediate emergency response", "red")
            }.onFailure { err ->
                _activeSosId.value = tempLocalId
                showToast("Offline SOS queued locally (${err.localizedMessage}). Call 112 directly!")
                pushNotification("Offline SOS queued locally at $areaLabel", "red")
            }

            if (LocationTracker.hasLocationPermission(context)) {
                runCatching { LocationSharingService.start(context) }
            }
        }
    }

    fun resolveActiveSos(context: Context) {
        val activeId = _activeSosId.value ?: return
        viewModelScope.launch {
            val note = "Marked safe & resolved by user."
            firestoreRepo.updateSosEventStatus(currentUserId, activeId, "resolved", note)
            dao.upsertSos(
                CachedSosEntity(
                    id = activeId,
                    status = "resolved",
                    triggerType = "button",
                    lat = deviceLocation.value.lat,
                    lon = deviceLocation.value.lon,
                    locationLabel = CrimeMapEngine.nearestAreaLabel(deviceLocation.value.lat, deviceLocation.value.lon),
                    message = _userProfile.value?.customSosMessage ?: "SOS Resolved",
                    deliveryNote = note,
                    timestampMs = System.currentTimeMillis()
                )
            )
            _activeSosId.value = null
            runCatching { LocationSharingService.stop(context) }
            showToast("SOS session marked safe & resolved")
        }
    }

    // ==================== CITIZEN REPORTS & FEEDBACK ====================
    fun submitCitizenReport(
        category: String,
        description: String,
        severity: Int,
        lat: Double = deviceLocation.value.lat,
        lon: Double = deviceLocation.value.lon
    ) {
        if (description.isBlank()) {
            showToast("Please describe the incident before submitting")
            return
        }
        viewModelScope.launch {
            val station = CrimeMapEngine.nearestStationName(lat, lon)
            val localId = "rep_local_${UUID.randomUUID().toString().take(10)}"
            dao.upsertReport(
                CachedReportEntity(
                    id = localId,
                    category = category,
                    description = description.trim(),
                    lat = lat,
                    lon = lon,
                    nearestStation = station,
                    severity = severity,
                    status = "received",
                    timestampMs = System.currentTimeMillis()
                )
            )
            firestoreRepo.createCitizenReport(
                userId = currentUserId,
                category = category,
                description = description.trim(),
                lat = lat,
                lon = lon,
                nearestStation = station,
                severity = severity
            ).onSuccess {
                pushNotification("Citizen safety report logged near $station", "green")
                showToast("Safety report submitted")
            }.onFailure {
                showToast("Report saved locally; cloud sync pending")
            }
        }
    }

    fun acknowledgeCitizenReport(reportId: String) {
        viewModelScope.launch {
            firestoreRepo.acknowledgeCitizenReport(currentUserId, reportId)
                .onSuccess { showToast("Report acknowledged") }
                .onFailure { showToast("Could not update report") }
        }
    }

    fun submitFeedback(message: String, onDone: (Boolean) -> Unit) {
        if (message.trim().isEmpty()) {
            showToast("Please write feedback before submitting")
            onDone(false)
            return
        }
        viewModelScope.launch {
            firestoreRepo.submitFeedback(currentUserId, message)
                .onSuccess {
                    showToast("Thank you! Feedback submitted.")
                    onDone(true)
                }
                .onFailure {
                    showToast("Could not send feedback: ${it.localizedMessage}")
                    onDone(false)
                }
        }
    }

    fun earnBadge(badgeId: String) {
        viewModelScope.launch {
            dao.earnBadge(QuizBadgeEntity(badgeId = badgeId, earnedAtMs = System.currentTimeMillis()))
        }
    }

    // ==================== NOTIFICATIONS & CSV EXPORT ====================
    fun pushNotification(message: String, level: String = "blue") {
        val item = MapNotificationItem(
            id = UUID.randomUUID().toString().take(8),
            message = message,
            level = level,
            timestampMs = System.currentTimeMillis()
        )
        _notifications.value = (listOf(item) + _notifications.value).take(30)
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    fun buildCsvExport(): String = CrimeMapEngine.buildIncidentsCsv(filteredIncidents.value)

    fun showToast(msg: String?) {
        _statusToast.value = msg
    }

    override fun onCleared() {
        detectionTickerJob?.cancel()
        sosCountdownJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(
            firestoreRepo: FirestoreRepository,
            dao: CyberWomenDao,
            prefs: PreferencesStore,
            userId: String,
            displayName: String,
            email: String?
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    firestoreRepo = firestoreRepo,
                    dao = dao,
                    prefs = prefs,
                    currentUserId = userId,
                    initialDisplayName = displayName,
                    initialEmail = email
                )
            }
        }
    }
}

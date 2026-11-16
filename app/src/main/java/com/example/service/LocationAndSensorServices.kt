package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.abs
import kotlin.math.sqrt

data class DeviceLocationState(
    val lat: Double = 11.9330,
    val lon: Double = 79.8260,
    val accuracyMeters: Float = 0f,
    val isLiveGps: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis()
)

object LocationTracker {
    private val _location = MutableStateFlow(DeviceLocationState())
    val location: StateFlow<DeviceLocationState> = _location.asStateFlow()

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(context: Context): DeviceLocationState {
        if (!hasLocationPermission(context)) return _location.value
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)
            val loc: Location? = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                ?: client.lastLocation.await()
            if (loc != null) {
                val updated = DeviceLocationState(
                    lat = loc.latitude,
                    lon = loc.longitude,
                    accuracyMeters = loc.accuracy,
                    isLiveGps = true,
                    timestampMs = System.currentTimeMillis()
                )
                _location.value = updated
                updated
            } else {
                _location.value
            }
        } catch (_: Exception) {
            _location.value
        }
    }

    internal fun publishLocation(loc: Location) {
        _location.value = DeviceLocationState(
            lat = loc.latitude,
            lon = loc.longitude,
            accuracyMeters = loc.accuracy,
            isLiveGps = true,
            timestampMs = System.currentTimeMillis()
        )
    }
}

class LocationSharingService : Service() {

    private var callback: LocationCallback? = null

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        ensureChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        if (LocationTracker.hasLocationPermission(this)) {
            val client = LocationServices.getFusedLocationProviderClient(this)
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 8000L)
                .setMinUpdateIntervalMillis(4000L)
                .build()
            val cb = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { LocationTracker.publishLocation(it) }
                }
            }
            callback = cb
            client.requestLocationUpdates(request, cb, Looper.getMainLooper())
        }
        return START_STICKY
    }

    override fun onDestroy() {
        callback?.let {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(it)
        }
        super.onDestroy()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Shield SOS Live Location",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CYBER WOMEN · Shield Location Active")
            .setContentText("Recording live coordinates for active SOS session.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "cyber_women_sos_location"
        const val NOTIFICATION_ID = 4091
        const val ACTION_STOP = "com.example.STOP_SOS_LOCATION"

        fun start(context: Context) {
            val intent = Intent(context, LocationSharingService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationSharingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}

class ShakeDetector(
    context: Context,
    private val onShakeTriggered: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastTriggerMs = 0L

    fun start() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values ?: return
        val gX = values[0] / SensorManager.GRAVITY_EARTH
        val gY = values[1] / SensorManager.GRAVITY_EARTH
        val gZ = values[2] / SensorManager.GRAVITY_EARTH
        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)
        val totalDelta = abs(gForce - 1.0f)
        val now = System.currentTimeMillis()
        if (totalDelta > 1.85f && now - lastTriggerMs > 3000L) {
            lastTriggerMs = now
            onShakeTriggered()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

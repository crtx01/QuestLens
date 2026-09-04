package dev.questlens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Optional vendor sensor listener. Never installs an input filter or creates an overlay window. */
internal class HeadsetTapShortcut(private val context: Context) : SensorEventListener {
    private val sensors = context.getSystemService(SensorManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val main = Handler(Looper.getMainLooper())
    private var registered = false
    private var closed = false
    private var gate = TapEventGate(SystemClock.elapsedRealtimeNanos())

    fun start() {
        scope.launch { AutonomousTap.state.collect { syncRegistration() } }
        AutonomousTap.prepare(context) { syncRegistration() }
    }

    private fun syncRegistration() {
        if (closed) return
        val wanted = isEnabled(context) && Settings.canDrawOverlays(context)
        if (wanted == registered) return
        if (!wanted) {
            sensors.unregisterListener(this)
            main.removeCallbacksAndMessages(null)
            registered = false
            Log.i(FrameRepository.TAG, "QUESTLENS_HEADSET_TAP_DISABLED")
            return
        }
        val sensor = findSensor(context) ?: return
        gate = TapEventGate(SystemClock.elapsedRealtimeNanos())
        try {
            registered = sensors.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, main)
            Log.i(FrameRepository.TAG, "QUESTLENS_HEADSET_TAP_REGISTERED=$registered")
        } catch (e: Exception) {
            Log.e(FrameRepository.TAG, "QUESTLENS_ERROR headset_tap_register", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (closed || !registered || !isEnabled(context)) return
        if (event.sensor.stringType != SENSOR_TYPE || event.values.getOrNull(1) != 1f) return
        val nowNs = SystemClock.elapsedRealtimeNanos()
        // Prime even while visible: the first callback is a retained sensor value.
        val accepted = gate.accept(event.timestamp, nowNs)
        Log.i(FrameRepository.TAG, "QUESTLENS_HEADSET_TAP_EVENT sensorNs=${event.timestamp} receivedNs=$nowNs accepted=$accepted panel=${PanelVisibility.visible}")
        if (!accepted) return
        if (FrameRepository.state.value.phase !in setOf(Phase.CAPTURE_ACTIVE, Phase.FROZEN_VIEWER)) return
        if (!Settings.canDrawOverlays(context)) {
            syncRegistration()
            return
        }
        if (PanelVisibility.visible) {
            FrameRepository.background()
            context.getSystemService(ActivityManager::class.java).appTasks.forEach { it.finishAndRemoveTask() }
            Log.i(FrameRepository.TAG, "QUESTLENS_HEADSET_TAP_CLOSED")
        } else openPanel()
    }

    private fun openPanel() {
        if (closed || !isEnabled(context) || !Settings.canDrawOverlays(context) || PanelVisibility.visible) return
        try {
            FrameRepository.foreground(preferLatest = true)
            context.startActivity(Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
            Log.i(FrameRepository.TAG, "QUESTLENS_HEADSET_TAP_OPEN_REQUESTED")
            main.postDelayed({
                if (!closed && !PanelVisibility.visible) {
                    FrameRepository.background()
                    Log.w(FrameRepository.TAG, "QUESTLENS_HEADSET_TAP_OPEN_NOT_VISIBLE capture_resumed")
                }
            }, 2000)
        } catch (e: Exception) {
            FrameRepository.background()
            Log.e(FrameRepository.TAG, "QUESTLENS_ERROR headset_tap_open", e)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun close() {
        closed = true
        sensors.unregisterListener(this)
        registered = false
        scope.cancel()
        main.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SENSOR_TYPE = "oculus.sensor.doubletap"
        private fun findSensor(context: Context): Sensor? = context.getSystemService(SensorManager::class.java)
            .getSensorList(Sensor.TYPE_ALL).firstOrNull { it.stringType == SENSOR_TYPE }
        fun isAvailable(context: Context) = findSensor(context) != null
        fun isEnabled(context: Context) = AutonomousTap.requested(context) && AutonomousTap.state.value.ready
        fun setEnabled(context: Context, enabled: Boolean) {
            if (enabled) AutonomousTap.prepare(context, setup = true)
            else AutonomousTap.disable(context)
        }
    }
}

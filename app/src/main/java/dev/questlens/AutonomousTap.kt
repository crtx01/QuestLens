package dev.questlens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

internal data class TapSetupState(val busy: Boolean = false, val ready: Boolean = false,
    val message: String = "Configure the shortcut once, then use it on your headset.")

/** Serializes preparation and restoration; saved preference never implies successful preparation. */
internal object AutonomousTap {
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val mutableState = MutableStateFlow(TapSetupState())
    val state = mutableState.asStateFlow()
    private const val ENABLED = "autonomous_tap_enabled"
    private const val ENROLLED = "local_adb_enrolled"
    private fun preferences(context: Context) = context.getSharedPreferences("shortcuts", Context.MODE_PRIVATE)
    fun requested(context: Context) = preferences(context).getBoolean(ENABLED, false)

    @Synchronized
    fun prepare(context: Context, setup: Boolean = false, complete: () -> Unit = {}) {
        if (mutableState.value.busy) return
        val app = context.applicationContext
        if (!setup && !requested(app)) { complete(); return }
        mutableState.value = TapSetupState(busy = true, message = "Preparing your headset shortcut…")
        worker.execute {
            val adb = LocalAdb(app)
            try {
                check(Settings.canDrawOverlays(app)) { "Allow window opening to use this shortcut." }
                check(HeadsetTapShortcut.isAvailable(app)) { "The double-tap sensor is unavailable." }
                check(app.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
                    "Initial setup permission is missing. Follow the QuestLens installation guide."
                }
                check(Settings.Global.putInt(app.contentResolver, "adb_wifi_enabled", 1)) {
                    "The system could not enable local debugging."
                }
                if (setup && !preferences(app).getBoolean(ENROLLED, false)) {
                    mutableState.value = TapSetupState(busy = true,
                        message = "Accept the debugging prompt on your headset and select ALWAYS ALLOW.")
                    adb.withConnection(initialAuthorization = true) {
                        check(adb.sensorState(it) in listOf("active", "idle")) { "Unexpected local response." }
                    }
                    preferences(app).edit().putBoolean(ENROLLED, true).apply()
                }
                // Deliberately require TLS here, even if TCP 5555 is currently available.
                // Otherwise setup could appear successful while still depending on a PC after reboot.
                adb.withConnection(initialAuthorization = false) { adb.suspendNativeTap(it) }
                preferences(app).edit().putBoolean(ENABLED, true).apply()
                mutableState.value = TapSetupState(ready = true,
                    message = "Shortcut ready. Double-tap your headset to open or close the lens.")
                Log.i(FrameRepository.TAG, "QUESTLENS_LOCAL_TAP_READY transport=tls")
            } catch (e: Exception) {
                mutableState.value = TapSetupState(message = e.message ?: "Shortcut setup failed. You can still use the volume shortcut.")
                Log.w(FrameRepository.TAG, "QUESTLENS_LOCAL_TAP_FAILED ${e.javaClass.simpleName}: ${e.message}")
            } finally {
                adb.close()
                main.post { complete() }
            }
        }
    }

    @Synchronized
    fun disable(context: Context, complete: () -> Unit = {}) {
        if (mutableState.value.busy) return
        val app = context.applicationContext
        preferences(app).edit().putBoolean(ENABLED, false).apply()
        mutableState.value = TapSetupState(busy = true, message = "Restoring the system shortcut…")
        worker.execute {
            val adb = LocalAdb(app)
            try {
                Settings.Global.putInt(app.contentResolver, "adb_wifi_enabled", 1)
                adb.withConnection(initialAuthorization = false) { adb.restoreNativeTap(it) }
                mutableState.value = TapSetupState(message = "QuestLens shortcut is off. System shortcut restored.")
            } catch (e: Exception) {
                mutableState.value = TapSetupState(message = "QuestLens shortcut is off. Restore the system shortcut again, or restart your headset.")
                Log.w(FrameRepository.TAG, "QUESTLENS_LOCAL_TAP_RESTORE_FAILED ${e.javaClass.simpleName}")
            } finally {
                adb.close()
                main.post { complete() }
            }
        }
    }
}

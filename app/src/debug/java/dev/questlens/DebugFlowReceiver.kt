package dev.questlens

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log

/** ADB-only harness, protected by DUMP and absent from release. No headset input interception. */
class DebugFlowReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "dev.questlens.debug.ENABLE_PREPARED_TAP" -> {
                if (!android.provider.Settings.canDrawOverlays(context)) {
                    resultCode = 4
                    return
                }
                HeadsetTapShortcut.setEnabled(context, true)
                Log.i(FrameRepository.TAG, "QUESTLENS_LOCAL_TAP_SETUP_REQUESTED")
            }
            "dev.questlens.debug.DISABLE_TAP" -> {
                HeadsetTapShortcut.setEnabled(context, false)
                Log.i(FrameRepository.TAG, "QUESTLENS_HEADSET_TAP_DISABLED_BY_SETUP")
            }
            "dev.questlens.debug.OPEN" -> {
                if (FrameRepository.state.value.phase != Phase.CAPTURE_ACTIVE) {
                    resultCode = 2
                    Log.w(FrameRepository.TAG, "QUESTLENS_DEBUG_OPEN_REJECTED capture_not_active")
                    return
                }
                try {
                    FrameRepository.foreground(preferLatest = true)
                    context.startActivity(Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
                    Log.i(FrameRepository.TAG, "QUESTLENS_DEBUG_OPEN_REQUESTED")
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!PanelVisibility.visible) {
                            FrameRepository.background()
                            Log.w(FrameRepository.TAG, "QUESTLENS_DEBUG_OPEN_NOT_VISIBLE capture_resumed")
                        }
                    }, 2000)
                } catch (e: Exception) {
                    FrameRepository.background()
                    resultCode = 3
                    Log.e(FrameRepository.TAG, "QUESTLENS_ERROR debug_open", e)
                }
            }
            "dev.questlens.debug.CLOSE" -> {
                FrameRepository.background()
                context.getSystemService(ActivityManager::class.java).appTasks.forEach { it.finishAndRemoveTask() }
                Log.i(FrameRepository.TAG, "QUESTLENS_DEBUG_CLOSE_REQUESTED")
            }
        }
    }
}

package dev.questlens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

/** System shortcut target: opens a panel without an AccessibilityService or input filter. */
class LaunchPanelActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            FrameRepository.foreground(preferLatest = true)
            Log.i(FrameRepository.TAG, "QUESTLENS_PANEL_SHORTCUT_INVOKED")
            startActivity(Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        } catch (e: Exception) {
            FrameRepository.background()
            Log.e(FrameRepository.TAG, "QUESTLENS_ERROR panel_shortcut", e)
        } finally {
            finish()
        }
    }
}

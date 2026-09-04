package dev.questlens

import android.graphics.Bitmap

enum class Phase { IDLE, REQUEST_CAPTURE_PERMISSION, STARTING, CAPTURE_ACTIVE, FROZEN_VIEWER, STOPPING }

data class FrozenFrame(val bitmap: Bitmap, val timestampNs: Long, val receivedAtMs: Long)

data class AppState(
    val phase: Phase = Phase.IDLE,
    val frame: FrozenFrame? = null,
    val message: String = "Capture is off.",
    val historyIndex: Int = -1,
    val historyCount: Int = 0,
    val frameAgeMs: Long = 0,
)

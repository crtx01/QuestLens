package dev.questlens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.io.ByteArrayOutputStream

/** All ownership transfers and bitmap writes are serialized under this object's monitor. */
object FrameRepository {
    const val TAG = "QuestLens"
    private val mutableState = MutableStateFlow(AppState())
    val state = mutableState.asStateFlow()
    private var latest: Bitmap? = null
    private var packed: ByteBuffer? = null
    private var timestampNs = 0L
    private var receivedAt = 0L
    private var lastLog = 0L
    private var foreground = false
    private var active = false
    private var blackFrame = false
    private val history = TemporalHistory()
    private var lastHistoryAt = 0L
    private var openedAt = 0L

    @Synchronized fun requestPermission(): Boolean {
        if (mutableState.value.phase != Phase.IDLE) return false
        mutableState.value = AppState(Phase.REQUEST_CAPTURE_PERMISSION, message = "Allow capture in the system prompt.")
        return true
    }

    @Synchronized fun starting() {
        mutableState.value = AppState(Phase.STARTING, message = "Starting capture…")
    }

    @Synchronized fun started() {
        active = true
        mutableState.value = AppState(Phase.CAPTURE_ACTIVE,
            message = "Capture is running.\nReturn to your game.")
    }

    @Synchronized fun foreground(preferLatest: Boolean = false) {
        if (foreground) return
        foreground = true
        Log.i(TAG, "QUESTLENS_ACTIVITY_FOREGROUND")
        if (!active) return
        openedAt = SystemClock.elapsedRealtime()
        if (!preferLatest && history.size > 0 && openedAt - receivedAt <= 3000) {
            showHistory(history.atOrBefore(openedAt - 2000))
            return
        }
        showLatest()
    }

    private fun showLatest() {
        val bitmap = latest
        val age = (openedAt - receivedAt).coerceAtLeast(0)
        if (bitmap != null && age <= 3000) {
            // Immutable UI snapshot: never mutate/recycle it while Compose/RenderThread can use it.
            val copy = bitmap.copy(Bitmap.Config.ARGB_8888, false)
            mutableState.value = AppState(Phase.FROZEN_VIEWER,
                FrozenFrame(copy, timestampNs, receivedAt),
                if (blackFrame) "Dark image: this may be the scene or content blocked by the game."
                else "", historyIndex = history.size, historyCount = history.size + 1, frameAgeMs = age)
            Log.i(TAG, "QUESTLENS_FRAME_FROZEN ${copy.width}x${copy.height} timestampNs=$timestampNs ageMs=$age bytes=${copy.allocationByteCount}")
        } else {
            mutableState.value = AppState(Phase.CAPTURE_ACTIVE,
                message = "No recent image.\nReturn to your game, wait a moment and reopen the lens.")
            Log.w(TAG, "QUESTLENS_ERROR no_recent_frame ageMs=$age")
        }
    }

    @Synchronized fun previousFrame() {
        val index = mutableState.value.historyIndex - 1
        if (active && foreground && index in 0 until history.size) showHistory(index)
    }

    @Synchronized fun nextFrame() {
        val index = mutableState.value.historyIndex + 1
        if (!active || !foreground) return
        if (index in 0 until history.size) showHistory(index)
        else if (index == history.size) showLatest()
    }

    private fun showHistory(index: Int) {
        val entry = history[index]
        val bitmap = BitmapFactory.decodeByteArray(entry.bytes, 0, entry.bytes.size) ?: return
        mutableState.value = AppState(Phase.FROZEN_VIEWER,
            FrozenFrame(bitmap, entry.timestampNs, entry.receivedAt),
            historyIndex = index, historyCount = history.size + 1,
            frameAgeMs = (openedAt - entry.receivedAt).coerceAtLeast(0))
        Log.i(TAG, "QUESTLENS_FRAME_FROZEN history=$index/${history.size} timestampNs=${entry.timestampNs} ageMs=${openedAt - entry.receivedAt} historyBytes=${history.byteCount}")
    }

    @Synchronized fun background() {
        foreground = false
        Log.i(TAG, "QUESTLENS_ACTIVITY_BACKGROUND")
        if (active) mutableState.value = AppState(Phase.CAPTURE_ACTIVE,
            message = "Capture is running.\nReturn to your game.")
        // Dropping the only repository reference lets the UI snapshot be GC'd safely.
        history.clear()
        lastHistoryAt = 0L
    }

    @Synchronized fun accept(image: Image) {
        if (!active || foreground) return
        val now = SystemClock.elapsedRealtime()
        if (receivedAt != 0L && now - receivedAt < 200) return // Up to 5 copies/s; drain all other images.
        val crop = image.cropRect
        val w = crop.width()
        val h = crop.height()
        require(w in 1..4096 && h in 1..4096) { "Unexpected image size ${w}x$h" }
        if (latest?.width != w || latest?.height != h) {
            latest?.recycle() // Private mutable working bitmap, never exposed to UI.
            latest = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            packed = ByteBuffer.allocateDirect(w * h * 4)
        }
        val plane = image.planes[0]
        val buffer = checkNotNull(packed)
        packRgba(plane.buffer, buffer, w, h, plane.rowStride, plane.pixelStride, crop.left, crop.top)
        checkNotNull(latest).copyPixelsFromBuffer(buffer)
        timestampNs = image.timestamp
        receivedAt = now
        if (lastHistoryAt == 0L || now - lastHistoryAt >= 500) {
            lastHistoryAt = now
            val output = ByteArrayOutputStream(256 * 1024)
            if (checkNotNull(latest).compress(Bitmap.CompressFormat.JPEG, 98, output)) {
                history.add(EncodedFrame(output.toByteArray(), timestampNs, now))
            }
        }
        // A dark sample is a diagnostic, never proof of DRM or a reason to bypass protection.
        blackFrame = (0..15).all { i ->
            val pixel = checkNotNull(latest).getPixel((i % 4) * (w - 1) / 3, (i / 4) * (h - 1) / 3)
            (pixel and 0x00FFFFFF) == 0
        }
        if (lastLog == 0L || now - lastLog >= 30_000) {
            lastLog = now
            Log.i(TAG, "QUESTLENS_FRAME_RECEIVED ${w}x$h rowStride=${plane.rowStride} pixelStride=${plane.pixelStride} timestampNs=$timestampNs bitmapBytes=${latest?.allocationByteCount} stagingBytes=${buffer.capacity()} dark=$blackFrame")
            if (blackFrame) Log.w(TAG, "QUESTLENS_ERROR dark_sample: scene or protected content; no bypass")
        }
    }

    @Synchronized fun checkDelivery() {
        if (active && !foreground && (receivedAt == 0L || SystemClock.elapsedRealtime() - receivedAt > 8000)) {
            Log.w(TAG, "QUESTLENS_ERROR no_frames_for_8s: check compositor/game capture support")
        }
    }

    @Synchronized fun stopping() {
        active = false
        mutableState.value = AppState(Phase.STOPPING, message = "Stopping capture…")
    }

    @Synchronized fun stopped(message: String = "Capture is off.") {
        active = false
        latest?.recycle()
        latest = null
        packed = null
        history.clear()
        lastHistoryAt = 0L
        timestampNs = 0L
        receivedAt = 0L
        lastLog = 0L
        mutableState.value = AppState(message = message)
    }
}

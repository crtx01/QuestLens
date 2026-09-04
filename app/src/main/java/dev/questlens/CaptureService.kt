package dev.questlens

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Log
import android.view.Surface

class CaptureService : Service() {
    private lateinit var thread: HandlerThread
    private lateinit var worker: Handler
    private val main = Handler(Looper.getMainLooper())
    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var surface: Surface? = null
    private var accepted = false
    private var headsetTap: HeadsetTapShortcut? = null
    private var cleaned = false
    private var endMessage = "Capture is off."
    private val callback = object : MediaProjection.Callback() {
        override fun onStop() {
            Log.i(FrameRepository.TAG, "QUESTLENS_PROJECTION_ONSTOP")
            finishCapture("Capture ended by the system.\nStart capture again to allow a new session.")
        }
        override fun onCapturedContentResize(width: Int, height: Int) {
            Log.i(FrameRepository.TAG, "Captured content ${width}x$height; fixed Surface ${WIDTH}x$HEIGHT (no resize)")
        }
    }
    private val watchdog = object : Runnable {
        override fun run() {
            if (cleaned) return
            FrameRepository.checkDelivery()
            worker.postDelayed(this, 30_000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        thread = HandlerThread("QuestLensCapture", Process.THREAD_PRIORITY_BACKGROUND).apply { start() }
        worker = Handler(thread.looper)
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "QuestLens capture", NotificationManager.IMPORTANCE_LOW))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP) {
            FrameRepository.stopping()
            worker.post { finishCapture("Capture is off.") }
            return START_NOT_STICKY
        }
        if (accepted) return START_NOT_STICKY
        @Suppress("DEPRECATION")
        val consent = intent?.getParcelableExtra<Intent>(CONSENT)
        if (intent?.action != START || consent == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        accepted = true // Never consume this consent Intent twice, even on duplicate starts.
        try {
            val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val stop = PendingIntent.getService(this, 1, Intent(this, CaptureService::class.java).setAction(STOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notification = Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_questlens).setContentTitle("QuestLens: capture running")
                .setContentText("Local capture, no audio. Select to magnify.")
                .setContentIntent(open).setOngoing(true)
                .addAction(Notification.Action.Builder(null, "END SESSION", stop).build()).build()
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
            headsetTap = HeadsetTapShortcut(this).also { it.start() }
            worker.post { startCapture(consent) }
        } catch (e: Exception) {
            Log.e(FrameRepository.TAG, "QUESTLENS_ERROR startForeground", e)
            worker.post { finishCapture("Could not start capture.\nPlease try again.") }
        }
        return START_NOT_STICKY // A killed session requires fresh system consent.
    }

    private fun startCapture(consent: Intent) {
        if (cleaned) return
        try {
            val session = checkNotNull(getSystemService(MediaProjectionManager::class.java)
                .getMediaProjection(Activity.RESULT_OK, consent))
            projection = session
            session.registerCallback(callback, worker)
            val images = ImageReader.newInstance(WIDTH, HEIGHT, PixelFormat.RGBA_8888, 2)
            reader = images
            surface = images.surface
            images.setOnImageAvailableListener({ source ->
                if (!cleaned) {
                    try {
                        // Always acquire and close, including while the viewer is frozen.
                        source.acquireLatestImage()?.use { FrameRepository.accept(it) }
                    } catch (e: Exception) {
                        Log.e(FrameRepository.TAG, "QUESTLENS_ERROR ImageReader/copy", e)
                        finishCapture("Could not receive the image.\nStart a new capture session.")
                    }
                }
            }, worker)
            display = session.createVirtualDisplay("QuestLens", WIDTH, HEIGHT,
                resources.configuration.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null, worker)
            checkNotNull(display) { "createVirtualDisplay returned null" }
            FrameRepository.started()
            Log.i(FrameRepository.TAG, "QUESTLENS_CAPTURE_STARTED surface=${WIDTH}x$HEIGHT density=${resources.configuration.densityDpi} sdk=${Build.VERSION.SDK_INT} model=${Build.MODEL}")
            worker.postDelayed(watchdog, 8000)
        } catch (e: Exception) {
            Log.e(FrameRepository.TAG, "QUESTLENS_ERROR MediaProjection/createVirtualDisplay", e)
            finishCapture("Capture failed.\nStart a new capture session.")
        }
    }

    private fun finishCapture(message: String) {
        if (cleaned) return
        endMessage = message
        FrameRepository.stopping()
        cleanup()
        main.post {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun cleanup() {
        if (cleaned) return
        cleaned = true
        worker.removeCallbacks(watchdog)
        fun release(name: String, action: () -> Unit) {
            try { action() } catch (e: Exception) { Log.e(FrameRepository.TAG, "QUESTLENS_ERROR cleanup $name", e) }
        }
        release("listener") { reader?.setOnImageAvailableListener(null, null) }
        release("display") { display?.release() }
        display = null
        release("surface") { surface?.release() }
        surface = null
        release("reader") { reader?.close() }
        reader = null
        release("callback") { projection?.unregisterCallback(callback) }
        release("projection") { projection?.stop() }
        projection = null
        Log.i(FrameRepository.TAG, "QUESTLENS_CAPTURE_STOPPED")
    }

    override fun onDestroy() {
        headsetTap?.close()
        headsetTap = null
        FrameRepository.stopping()
        worker.post {
            cleanup()
            FrameRepository.stopped(endMessage)
            thread.quitSafely()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val START = "dev.questlens.START"
        const val STOP = "dev.questlens.STOP"
        const val CONSENT = "consent"
        private const val CHANNEL = "capture"
        // Stable MVP target. Horizon compositor, not the Android panel, supplies this surface.
        const val WIDTH = 1920
        const val HEIGHT = 1080
    }
}

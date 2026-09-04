package dev.questlens

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    private var supportVisible by mutableStateOf(false)
    private val supportPages by lazy {
        listOf(
            SupportPage("PATREON", getString(R.string.support_patreon_url), "Support development monthly on Patreon."),
            SupportPage("PAYPAL", getString(R.string.support_paypal_url), "Make an optional contribution through PayPal.")
        ).filter { it.url.isNotBlank() }
    }
    private var setupGuideVisible by mutableStateOf(false)
    private var setupPermission by mutableStateOf(false)
    private var tapSetupVisible by mutableStateOf(false)
    private var tapEnabled by mutableStateOf(false)
    private var tapPermission by mutableStateOf(false)
    private var tapAvailable by mutableStateOf(false)
    private val consent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            FrameRepository.starting()
            try {
                startForegroundService(Intent(this, CaptureService::class.java)
                    .setAction(CaptureService.START).putExtra(CaptureService.CONSENT, result.data))
            } catch (e: Exception) {
                Log.e(FrameRepository.TAG, "QUESTLENS_ERROR startForegroundService", e)
                FrameRepository.stopped("Could not start capture.\nPlease try again.")
            }
        } else FrameRepository.stopped("Capture was not allowed.\nSelect START CAPTURE to try again.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupGuideVisible = !AutonomousTap.requested(this) &&
            !getPreferences(MODE_PRIVATE).getBoolean("setup_guide_seen", false)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFFFEB3B),
                onPrimary = Color.Black, background = Color.Black, surface = Color.Black,
                onSurface = Color.White, onBackground = Color.White)) {
                val state by FrameRepository.state.collectAsStateWithLifecycle()
                val tapState by AutonomousTap.state.collectAsStateWithLifecycle()
                BackHandler {
                    when {
                        supportVisible -> supportVisible = false
                        setupGuideVisible -> finishGuide()
                        tapSetupVisible -> tapSetupVisible = false
                        else -> closePanel()
                    }
                }
                QuestLensScreen(state, tapEnabled, tapState, ::startConsent,
                    { startService(Intent(this, CaptureService::class.java).setAction(CaptureService.STOP)) },
                    ::closePanel, { tapSetupVisible = true }, supportPages.isNotEmpty(), { supportVisible = true })
                if (supportVisible) SupportDialog(supportPages, ::openSupportPage, { supportVisible = false })
                if (tapSetupVisible) ShortcutSettings(tapAvailable, tapEnabled, tapPermission, tapState,
                    { tapSetupVisible = false }, ::openBackgroundLaunchSettings, ::openPanelShortcutSettings,
                    { tapSetupVisible = false; setupGuideVisible = true }) { enabled ->
                    val refresh = { tapEnabled = AutonomousTap.requested(this); Unit }
                    if (enabled) AutonomousTap.prepare(this, setup = true, complete = refresh)
                    else AutonomousTap.disable(this, complete = refresh)
                }
                if (setupGuideVisible) FirstRunGuide(setupPermission, tapPermission, tapState,
                    ::openBackgroundLaunchSettings, {
                        AutonomousTap.prepare(this, setup = true) { tapEnabled = AutonomousTap.requested(this) }
                    }, ::finishGuide)
            }
        }
    }

    private fun finishGuide() {
        getPreferences(MODE_PRIVATE).edit().putBoolean("setup_guide_seen", true).apply()
        setupGuideVisible = false
    }

    private fun openSupportPage(url: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        catch (e: Exception) {
            Toast.makeText(this, "Could not open the support page. Please try again later.", Toast.LENGTH_LONG).show()
            Log.w(FrameRepository.TAG, "QUESTLENS_SUPPORT_BROWSER_UNAVAILABLE")
        }
    }

    private fun closePanel() {
        // Remove the 2D task instead of leaving a floating panel competing with the game.
        FrameRepository.background()
        finishAndRemoveTask()
    }

    private fun openPanelShortcutSettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setPackage("com.android.settings"))
        } catch (e: Exception) {
            Log.e(FrameRepository.TAG, "QUESTLENS_ERROR shortcut_settings", e)
            Toast.makeText(this, "Shortcut settings are unavailable on this headset.", Toast.LENGTH_LONG).show()
        }
    }

    private fun openBackgroundLaunchSettings() {
        try {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                .setPackage("com.android.settings"))
        } catch (e: Exception) {
            Log.e(FrameRepository.TAG, "QUESTLENS_ERROR background_launch_settings", e)
            Toast.makeText(this, "Window permission settings are unavailable. You can still use the volume shortcut.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        tapEnabled = AutonomousTap.requested(this)
        tapPermission = Settings.canDrawOverlays(this)
        tapAvailable = HeadsetTapShortcut.isAvailable(this)
        setupPermission = checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
    }

    private fun startConsent() {
        if (!FrameRepository.requestPermission()) return
        try {
            consent.launch(getSystemService(MediaProjectionManager::class.java).createScreenCaptureIntent())
        } catch (e: Exception) {
            Log.e(FrameRepository.TAG, "QUESTLENS_ERROR consent", e)
            FrameRepository.stopped("Capture permission is unavailable.\nPlease try again.")
        }
    }

    override fun onStart() {
        super.onStart()
        PanelVisibility.visible = true
        try { FrameRepository.foreground() }
        catch (e: Exception) {
            Log.e(FrameRepository.TAG, "QUESTLENS_ERROR freeze", e)
        }
    }

    override fun onStop() {
        PanelVisibility.visible = false
        // Focus loss/onPause alone does not mean a Horizon panel was minimized.
        if (!isChangingConfigurations) FrameRepository.background()
        super.onStop()
    }
}

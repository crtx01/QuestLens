package dev.questlens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuestLensScreen(state: AppState, tapRequested: Boolean, tap: TapSetupState,
                            start: () -> Unit, stop: () -> Unit, close: () -> Unit, settings: () -> Unit,
                            contact: () -> Unit, supportAvailable: Boolean, support: () -> Unit) {
    var zoom by remember(state.frame) { mutableFloatStateOf(1f) }
    var resetKey by remember(state.frame) { mutableIntStateOf(0) }
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.frame != null) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("QUESTLENS", fontSize = 24.sp)
                    Text("FROZEN IMAGE", fontSize = 20.sp, color = Color.LightGray)
                }
                ZoomViewer(state.frame, zoom, { zoom = it }, resetKey, Modifier.weight(1f).fillMaxWidth())
                if (state.message.isNotEmpty()) Text(state.message, fontSize = 20.sp, color = Color.Yellow)
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HelpButton("−", "Zoom out one step. You can also click the image with the left trigger.", { zoom = steppedZoom(zoom, -1) }, enabled = zoom > 1f)
                    Text(String.format(Locale.US, "%.1f×", zoom), fontSize = 28.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp))
                    HelpButton("+", "Zoom in one step, up to 8×. You can also click the image with the right trigger.", { zoom = steppedZoom(zoom, 1) }, enabled = zoom < 8f)
                    HelpButton("RESET", "Return to 1× and center the image.", { zoom = 1f; resetKey++ }, secondary = true)
                    HelpButton("BACK TO GAME", "Close the window and keep capture running. You can also double-tap your headset.", close)
                    HelpButton("END SESSION", "Stop capture and return to the start screen.", stop, secondary = true)
                }
            } else {
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("QUESTLENS", fontSize = 40.sp)
                    Text(state.message, fontSize = 26.sp)
                    when (state.phase) {
                        Phase.IDLE -> {
                            HelpButton("START CAPTURE", "Allow capture for this session, then return to your game. Images stay on your headset.", start)
                            HelpButton("SETTINGS", "Set up or restore the headset shortcut. You only need to configure it once.", settings, secondary = true)
                            HelpButton("CONTACT US", "Email questions, feedback or accessibility suggestions to the QuestLens team.", contact, secondary = true)
                            if (supportAvailable) HelpButton("SUPPORT US", "Optional support for c0rtex and QuestLens development.", support, secondary = true)
                        }
                        Phase.CAPTURE_ACTIVE -> {
                            if (tapRequested) Text(if (tap.ready) "Double-tap your headset to open or close the lens."
                                else tap.message, fontSize = 22.sp, color = if (tap.ready) Color.White else Color.Yellow)
                            HelpButton("RETURN TO GAME", "Close this panel, open your game and use your shortcut when you need to read.", close)
                            HelpButton("END SESSION", "Stop capture and return to the start screen.", stop, secondary = true)
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
internal fun ShortcutSettings(available: Boolean, enabled: Boolean, allowed: Boolean, setup: TapSetupState,
                             dismiss: () -> Unit, permission: () -> Unit, volumeSettings: () -> Unit,
                             setupGuide: () -> Unit,
                             setEnabled: (Boolean) -> Unit) {
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("SETTINGS", fontSize = 28.sp) },
        text = {
            Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("HEADSET SHORTCUT", fontSize = 24.sp)
                Text("Double-tap the side of your headset to open the lens. Double-tap again to return to your game.", fontSize = 22.sp)
                Text("This replaces the passthrough tap shortcut and may also disable the headset action button. Keep Wi-Fi enabled. Use RESTORE below to undo it.", fontSize = 20.sp)
                if (!available) Text("This headset does not expose the required sensor.", fontSize = 22.sp)
                if (!allowed) HelpButton("ALLOW WINDOW", "Allow QuestLens to open its window while you are in a game.", permission, enabled = available)
                Text(setup.message, fontSize = 22.sp, color = Color.Yellow)
                HelpButton(if (enabled) "RECONNECT" else "SET UP SHORTCUT",
                    "Connect locally on this headset. During first-time setup, accept the system debugging prompts and select Always allow.",
                    { setEnabled(true) }, enabled = available && allowed && !setup.busy)
                HelpButton("RESTORE PASSTHROUGH SHORTCUT", "Turn off the QuestLens gesture and restore the system gesture.",
                    { setEnabled(false) }, enabled = !setup.busy, secondary = true)
                HorizontalDivider()
                HelpButton("SETUP GUIDE", "Review first-time setup, permissions and controls.", setupGuide, secondary = true)
                HelpButton("VOLUME SHORTCUT", "Optional alternative: hold both headset volume buttons to open the lens. Opens Android shortcut settings.",
                    volumeSettings, secondary = true)
            }
        },
        confirmButton = { HelpButton("DONE", "Close settings and return to QuestLens.", dismiss) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HelpButton(label: String, help: String, action: () -> Unit,
                        secondary: Boolean = false, enabled: Boolean = true) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    var focused by remember { mutableStateOf(false) }
    val tooltip = rememberTooltipState(isPersistent = true)
    LaunchedEffect(hovered, focused, enabled) {
        if (hovered || focused) { delay(500); tooltip.show() }
        else tooltip.dismiss()
    }
    TooltipBox(modifier = Modifier.hoverable(interaction),
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        state = tooltip, enableUserInput = false, focusable = false,
        tooltip = { PlainTooltip { Text(help, fontSize = 20.sp, modifier = Modifier.widthIn(max = 360.dp)) } }) {
        Button(onClick = { tooltip.dismiss(); action() },
            modifier = Modifier.heightIn(min = 64.dp).widthIn(min = 76.dp).onFocusChanged { focused = it.isFocused },
            colors = ButtonDefaults.buttonColors(containerColor = if (secondary) Color(0xFF303030) else Color(0xFFFFEB3B),
                contentColor = if (secondary) Color.White else Color.Black),
            enabled = enabled, interactionSource = interaction,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            Text(label, fontSize = 24.sp)
        }
    }
}

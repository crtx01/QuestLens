package dev.questlens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Only shown automatically to a new, unconfigured installation. Never appears over captured text. */
@Composable
internal fun FirstRunGuide(setupAllowed: Boolean, windowAllowed: Boolean, setup: TapSetupState,
                          allowWindow: () -> Unit, connect: () -> Unit, done: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val title = listOf("WELCOME TO QUESTLENS", "ONE-TIME SETUP", "ALLOW THE SHORTCUT", "READY TO READ")[page]
    AlertDialog(onDismissRequest = done,
        title = { Text("${page + 1}/4 · $title", fontSize = 26.sp) },
        text = {
            key(page) {
                Column(Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (page) {
                        0 -> {
                            GuideText("Freeze small text in your game, magnify it in a 2D window, then return to playing.")
                            GuideText("Images stay in headset memory. This is a frozen-image magnifier, not live zoom.")
                            GuideText("The optional double-tap shortcut needs setup once. After that, the app prepares it locally when you start capture. Keep Wi-Fi enabled.")
                            GuideText("This replaces the passthrough tap shortcut and may disable the physical action button. You can restore it in Settings.")
                        }
                        1 -> {
                            GuideText("Connect your developer-enabled headset to your computer. Run the included Install-QuestLens.ps1 script, or run these ADB commands once after installing:")
                            SelectionContainer {
                                Text("adb shell pm grant dev.questlens android.permission.WRITE_SECURE_SETTINGS\n\nadb tcpip 5555",
                                    fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                            }
                            Text(if (setupAllowed) "Setup permission granted." else "Waiting for setup permission.",
                                fontSize = 22.sp, color = if (setupAllowed) Color.White else Color.Yellow)
                            GuideText("The computer is for this initial authorization. Normal sessions use a connection inside the headset. After a restart, begin a new capture session; full restart recovery is still being tested.")
                        }
                        2 -> {
                            GuideText("1. Allow QuestLens to open its window while a game is running.")
                            if (!windowAllowed) HelpButton("ALLOW WINDOW", "Open the system permission screen and allow displaying over other apps.", allowWindow)
                            else GuideText("Window permission granted.")
                            GuideText("2. Select CONNECT below. In the headset prompts, allow wireless debugging on this network and allow the QuestLens debugging key. Select Always allow in both prompts.")
                            HelpButton("CONNECT", "Authorize this app's own local connection. No computer key is copied.", connect,
                                enabled = setupAllowed && windowAllowed && !setup.busy)
                            Text(setup.message, fontSize = 22.sp, color = Color.Yellow)
                            GuideText("If a prompt takes too long, accept it and select CONNECT again. You can also use the optional volume shortcut in Settings.")
                        }
                        3 -> {
                            GuideText("START CAPTURE → allow the system prompt → RETURN TO GAME.")
                            GuideText("Double-tap the side of your headset to open the lens. Double-tap again to close it.")
                            GuideText("Point at the image: right-trigger click zooms in, left-trigger click zooms out. Hold either trigger and drag to move the image.")
                            GuideText("Point at a button for help. END SESSION stops capture. You must allow capture again for each new session.")
                            GuideText("To undo the headset gesture: END SESSION → SETTINGS → RESTORE PASSTHROUGH SHORTCUT.")
                        }
                    }
                }
            }
        },
        confirmButton = { HelpButton(if (page == 3) "DONE" else "NEXT", "Continue through setup.", { if (page == 3) done() else page++ }) },
        dismissButton = { HelpButton(if (page == 0) "LATER" else "BACK", "Go back. The guide is also available in Settings.",
            { if (page == 0) done() else page-- }, secondary = true) })
}

@Composable
private fun GuideText(text: String) { Text(text, fontSize = 22.sp) }

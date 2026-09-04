package dev.questlens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class SupportPage(val name: String, val url: String, val help: String)

@Composable
internal fun SupportDialog(pages: List<SupportPage>, open: (String) -> Unit, dismiss: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("SUPPORT US", fontSize = 28.sp) },
        text = {
            Column(Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("Support c0rtex and the development of QuestLens.", fontSize = 24.sp)
                Text("Help us maintain updates and create new accessibility tools for people with low vision.", fontSize = 22.sp)
                Text("Support is optional. Every app feature is available without donating.", fontSize = 22.sp)
                pages.forEach { page -> HelpButton(page.name, page.help, { open(page.url) }) }
                Text("Opens the provider's website in your browser. QuestLens does not collect payment details.", fontSize = 20.sp)
            }
        },
        confirmButton = { HelpButton("BACK", "Return to QuestLens.", dismiss, secondary = true) })
}

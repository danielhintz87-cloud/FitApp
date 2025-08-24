package com.example.fitapp.ui.dialogs

import androidx.compose.material.AlertDialog
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

@Composable
fun ModelPickerDialog(
    currentChat: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("KI‑Modell (Coach)") },
        text = {
            Column {
                ModelRadio("openai", currentChat == "openai", onPick)
                ModelRadio("deepseek", currentChat == "deepseek", onPick)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Schließen") }
        }
    )
}

@Composable
private fun ModelRadio(id: String, selected: Boolean, onPick: (String) -> Unit) {
    Row {
        RadioButton(selected = selected, onClick = { onPick(id) })
        Text(id.uppercase())
    }
}

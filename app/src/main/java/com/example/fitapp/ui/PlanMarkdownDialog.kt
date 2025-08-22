package com.example.fitapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.halilibo.compose.richtext.ui.material3.RichText
import com.halilibo.compose.richtext.markdown.Markdown

@Composable
fun PlanMarkdownDialog(markdown: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } },
        title = { Text("Plan-Details") },
        text = {
            Box(Modifier.heightIn(min = 0.dp, max = 420.dp)) {
                RichText { Markdown(markdown) }
            }
        }
    )
}

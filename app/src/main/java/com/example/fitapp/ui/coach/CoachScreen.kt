package com.example.fitapp.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitapp.ai.AppAi
import androidx.compose.foundation.layout.FlowRow

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(
    vm: CoachViewModel = viewModel()
) {
    val msgs by vm.messages.collectAsState()
    val busy by vm.busy.collectAsState()
    var input by remember { mutableStateOf("") }
    val fm = LocalFocusManager.current

    var providerOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Coach", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                actions = {
                    IconButton(onClick = { providerOpen = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Provider wählen")
                    }
                    IconButton(onClick = { vm.clear() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Verlauf löschen")
                    }
                    IconButton(onClick = { /* Reserve für Drei-Punkte Menü */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mehr")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Frag den Coach …") },
                    singleLine = true,
                    enabled = !busy
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        input.trim().takeIf { it.isNotEmpty() }?.let {
                            vm.send(it); input = ""; fm.clearFocus()
                        }
                    },
                    enabled = !busy
                ) { Text(if (busy) "…" else "Senden") }
            }
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {

            // Prompt-Chips (Shortcuts)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestChip("Tagesplan (Abnehmen)") { vm.send("Erstelle mir einen kompakten Tagesplan zum Abnehmen (Sport + Ernährung).") }
                SuggestChip("20‑Min HIIT") { vm.send("Gib mir ein 20‑Minuten HIIT-Workout ohne Geräte, tabellarisch.") }
                SuggestChip("Low‑Carb Rezepte") { vm.send("Nenne 3 Low‑Carb Rezepte als Markdown mit Zutatenliste (• bullets) und Anweisungen.") }
                SuggestChip("Kardio‑Woche") { vm.send("Plane eine Woche Kardio (3 Einheiten, 30 Min). Steigern über die Woche.") }
            }

            // Chat-Verlauf
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(msgs.asReversed(), key = { it.id }) { m ->
                    MessageBubble(
                        msg = m,
                        isMe = m.author is Author.Me,
                        onSaveRecipe = { vm.saveAiAsRecipe(m) },
                        onAddToShopping = { vm.parseIngredientsToShopping(m) }
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    if (providerOpen) {
        ProviderSheet(
            onClose = { providerOpen = false },
            onChoose = { AppAi.currentProvider = it }
        )
    }
}

@Composable
private fun SuggestChip(text: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) }
    )
}

@Composable
private fun MessageBubble(
    msg: ChatMessage,
    isMe: Boolean,
    onSaveRecipe: () -> Unit,
    onAddToShopping: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomEnd = if (isMe) 2.dp else 16.dp,
                        bottomStart = if (isMe) 16.dp else 2.dp
                    )
                )
                .background(if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
                .fillMaxWidth(0.9f)
        ) {
            Text(
                msg.text,
                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )

            if (!isMe) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onSaveRecipe) { Text("Als Rezept speichern") }
                    OutlinedButton(onClick = onAddToShopping) { Text("Zutaten → Einkauf") }
                }
            }
        }
    }
}


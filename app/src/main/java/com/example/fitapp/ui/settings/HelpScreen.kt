package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hilfe & Support") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(contentPadding)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Welcome section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Willkommen bei FitApp!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hier finden Sie Antworten auf häufige Fragen und Hilfe bei der Nutzung der App.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            // FAQ items
            items(faqItems) { faq ->
                HelpFaqItem(faq = faq)
            }

            // Contact section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Row {
                            Icon(
                                Icons.AutoMirrored.Filled.ContactSupport,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weitere Hilfe benötigt?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Falls Sie weitere Fragen haben, können Sie uns über die Einstellungen kontaktieren oder die Community-Foren besuchen.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

/**
 * FAQ Item Component
 */
@Composable
private fun HelpFaqItem(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier =
            Modifier
                .fillMaxWidth(),
        onClick = { expanded = !expanded },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Weniger anzeigen" else "Mehr anzeigen",
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * FAQ data class
 */
data class FaqItem(
    val question: String,
    val answer: String,
    val category: String = "Allgemein",
)

/**
 * FAQ content
 */
private val faqItems =
    listOf(
        FaqItem(
            question = "Wie starte ich mein erstes Training?",
            answer = "Gehen Sie zum Training-Tab und wählen Sie 'Neues Training starten'. Sie können aus vorgefertigten Plänen wählen oder ein eigenes Training erstellen. Folgen Sie den Anweisungen für jede Übung.",
        ),
        FaqItem(
            question = "Wie verbinde ich Health Connect?",
            answer = "Gehen Sie zu Einstellungen > Health Connect. Tippen Sie auf 'Verbinden' und gewähren Sie die benötigten Berechtigungen. Die App synchronisiert dann automatisch Ihre Gesundheitsdaten.",
        ),
        FaqItem(
            question = "Wie verwende ich den Barcode-Scanner?",
            answer = "Im Ernährung-Tab können Sie den Barcode-Scanner öffnen. Richten Sie die Kamera auf den Barcode eines Lebensmittels. Die Nährwerte werden automatisch ausgefüllt.",
        ),
        FaqItem(
            question = "Wie funktioniert die Sprachsteuerung?",
            answer = "Bei der Einkaufsliste können Sie das Mikrofon-Symbol antippen und Ihre Einkaufsliste diktieren. Sagen Sie z.B. '2 Kilo Äpfel, 500 Gramm Hackfleisch, eine Packung Milch'.",
        ),
        FaqItem(
            question = "Warum funktioniert der Audio-Trainer nicht?",
            answer = "Stellen Sie sicher, dass Sie der App die Mikrofon-Berechtigung erteilt haben. Prüfen Sie auch Ihre Lautstärke-Einstellungen und ob andere Apps den Audio-Fokus blockieren.",
        ),
        FaqItem(
            question = "Wie kann ich meine Daten sichern?",
            answer = "Die App synchronisiert automatisch mit Health Connect wenn verbunden. Für zusätzliche Sicherheit können Sie in den Einstellungen einen Export Ihrer Daten durchführen.",
        ),
        FaqItem(
            question = "Wie berechnet die App den Kalorienverbrauch?",
            answer = "Der Kalorienverbrauch wird basierend auf MET-Werten, Ihrem Gewicht, der Trainingsintensität und -dauer berechnet. Bei verbundenem Health Connect werden auch Herzfrequenzdaten berücksichtigt.",
        ),
    )

package com.example.fitapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitapp.services.*
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    var workoutRemindersEnabled by remember { mutableStateOf(true) }
    var nutritionRemindersEnabled by remember { mutableStateOf(true) }
    var waterRemindersEnabled by remember { mutableStateOf(true) }
    var achievementNotificationsEnabled by remember { mutableStateOf(true) }
    var dailyMotivationEnabled by remember { mutableStateOf(true) }
    var workoutTime by remember { mutableStateOf(LocalTime.of(18, 0)) }
    var quietHoursEnabled by remember { mutableStateOf(false) }
    var quietHoursStart by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var quietHoursEnd by remember { mutableStateOf(LocalTime.of(8, 0)) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Benachrichtigungen") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Workout Reminders Section
            NotificationSectionCard(
                title = "Workout-Erinnerungen",
                icon = Icons.Default.FitnessCenter
            ) {
                SwitchPreference(
                    title = "Workout-Erinnerungen",
                    subtitle = "Erhalte Benachrichtigungen für geplante Workouts",
                    checked = workoutRemindersEnabled,
                    onCheckedChange = { enabled ->
                        workoutRemindersEnabled = enabled
                        if (enabled) {
                            WorkoutReminderWorker.scheduleWorkoutReminder(
                                context,
                                "Training",
                                workoutTime
                            )
                        } else {
                            WorkoutReminderWorker.cancelWorkoutReminder(context, "Training")
                        }
                    }
                )
                
                if (workoutRemindersEnabled) {
                    TimePreference(
                        title = "Workout-Zeit",
                        subtitle = "Wann möchtest du normalerweise trainieren?",
                        time = workoutTime,
                        onTimeChange = { newTime ->
                            workoutTime = newTime
                            WorkoutReminderWorker.scheduleWorkoutReminder(
                                context,
                                "Training",
                                newTime
                            )
                        }
                    )
                }
            }
            
            // Nutrition Reminders Section
            NotificationSectionCard(
                title = "Ernährungs-Erinnerungen",
                icon = Icons.Default.Restaurant
            ) {
                SwitchPreference(
                    title = "Mahlzeit-Erinnerungen",
                    subtitle = "Erinnerungen für Frühstück, Mittagessen, Abendessen",
                    checked = nutritionRemindersEnabled,
                    onCheckedChange = { enabled ->
                        nutritionRemindersEnabled = enabled
                        if (enabled) {
                            NutritionReminderWorker.scheduleMealReminders(context)
                        } else {
                            NutritionReminderWorker.cancelMealReminders(context)
                        }
                    }
                )
                
                SwitchPreference(
                    title = "Wasser-Erinnerungen",
                    subtitle = "Alle 2 Stunden an Wasserzufuhr erinnern",
                    checked = waterRemindersEnabled,
                    onCheckedChange = { enabled ->
                        waterRemindersEnabled = enabled
                        if (enabled) {
                            WaterReminderWorker.scheduleWaterReminders(context)
                        } else {
                            WaterReminderWorker.cancelWaterReminders(context)
                        }
                    }
                )
            }
            
            // Achievement & Motivation Section
            NotificationSectionCard(
                title = "Erfolge & Motivation",
                icon = Icons.Default.EmojiEvents
            ) {
                SwitchPreference(
                    title = "Erfolg-Benachrichtigungen",
                    subtitle = "Sofortige Benachrichtigung bei neuen Erfolgen",
                    checked = achievementNotificationsEnabled,
                    onCheckedChange = { achievementNotificationsEnabled = it }
                )
                
                SwitchPreference(
                    title = "Tägliche Motivation",
                    subtitle = "Motivierende Nachrichten jeden Morgen",
                    checked = dailyMotivationEnabled,
                    onCheckedChange = { enabled ->
                        dailyMotivationEnabled = enabled
                        if (enabled) {
                            DailyMotivationWorker.scheduleWork(context)
                        } else {
                            DailyMotivationWorker.cancelWork(context)
                        }
                    }
                )
            }
            
            // Quiet Hours Section
            NotificationSectionCard(
                title = "Ruhezeiten",
                icon = Icons.Default.Bedtime
            ) {
                SwitchPreference(
                    title = "Ruhezeiten aktivieren",
                    subtitle = "Keine Benachrichtigungen während dieser Zeit",
                    checked = quietHoursEnabled,
                    onCheckedChange = { quietHoursEnabled = it }
                )
                
                if (quietHoursEnabled) {
                    TimePreference(
                        title = "Ruhezeit Start",
                        subtitle = "Beginn der Ruhezeit",
                        time = quietHoursStart,
                        onTimeChange = { quietHoursStart = it }
                    )
                    
                    TimePreference(
                        title = "Ruhezeit Ende",
                        subtitle = "Ende der Ruhezeit",
                        time = quietHoursEnd,
                        onTimeChange = { quietHoursEnd = it }
                    )
                }
            }
            
            // Preview Section
            NotificationSectionCard(
                title = "Vorschau",
                icon = Icons.Default.Preview
            ) {
                Button(
                    onClick = {
                        SmartNotificationManager.showWorkoutReminder(
                            context,
                            "Test-Training",
                            "jetzt"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Notifications, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Workout-Erinnerung testen")
                }
                
                Spacer(Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        SmartNotificationManager.showWaterReminder(
                            context,
                            1200,
                            2000
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.WaterDrop, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Wasser-Erinnerung testen")
                }
            }
        }
    }
}

@Composable
private fun NotificationSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            content()
        }
    }
}

@Composable
private fun SwitchPreference(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun TimePreference(
    title: String,
    subtitle: String,
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        TextButton(
            onClick = { showTimePicker = true }
        ) {
            Text("${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}")
        }
    }
    
    if (showTimePicker) {
        // Note: TimePicker would need a custom implementation or library
        // For now, we'll use a simple dialog
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(title) },
            text = {
                Column {
                    Text("Aktuelle Zeit: ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}")
                    Text("Zeit-Picker würde hier implementiert werden", 
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("OK")
                }
            }
        )
    }
}
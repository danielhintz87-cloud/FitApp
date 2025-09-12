package com.example.fitapp.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.example.fitapp.services.*
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
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

    // Permission state
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true // No permission needed for Android < 13
            },
        )
    }

    // Permission launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasNotificationPermission = isGranted
        }

    // Helper function to check/request permission before enabling notifications
    fun requestPermissionIfNeeded(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onGranted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Benachrichtigungen") },
                navigationIcon = {
                    IconButton(onClick = { onBack?.invoke() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Workout Reminders Section
            NotificationSectionCard(
                title = "Workout-Erinnerungen",
                icon = Icons.Default.FitnessCenter,
            ) {
                SwitchPreference(
                    title = "Workout-Erinnerungen",
                    subtitle = "Erhalte Benachrichtigungen für geplante Workouts",
                    checked = workoutRemindersEnabled && hasNotificationPermission,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestPermissionIfNeeded {
                                workoutRemindersEnabled = true
                                WorkoutReminderWorker.scheduleWorkoutReminder(
                                    context,
                                    "Training",
                                    workoutTime,
                                )
                            }
                        } else {
                            workoutRemindersEnabled = false
                            WorkoutReminderWorker.cancelWorkoutReminder(context, "Training")
                        }
                    },
                    showPermissionWarning = !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
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
                                newTime,
                            )
                        },
                    )
                }
            }

            // Nutrition Reminders Section
            NotificationSectionCard(
                title = "Ernährungs-Erinnerungen",
                icon = Icons.Default.Restaurant,
            ) {
                SwitchPreference(
                    title = "Mahlzeit-Erinnerungen",
                    subtitle = "Erinnerungen für Frühstück, Mittagessen, Abendessen",
                    checked = nutritionRemindersEnabled && hasNotificationPermission,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestPermissionIfNeeded {
                                nutritionRemindersEnabled = true
                                NutritionReminderWorker.scheduleMealReminders(context)
                            }
                        } else {
                            nutritionRemindersEnabled = false
                            NutritionReminderWorker.cancelMealReminders(context)
                        }
                    },
                    showPermissionWarning = !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                )

                SwitchPreference(
                    title = "Wasser-Erinnerungen",
                    subtitle = "Alle 2 Stunden an Wasserzufuhr erinnern",
                    checked = waterRemindersEnabled && hasNotificationPermission,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestPermissionIfNeeded {
                                waterRemindersEnabled = true
                                WaterReminderWorker.scheduleWaterReminders(context)
                            }
                        } else {
                            waterRemindersEnabled = false
                            WaterReminderWorker.cancelWaterReminders(context)
                        }
                    },
                    showPermissionWarning = !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                )
            }

            // Achievement & Motivation Section
            NotificationSectionCard(
                title = "Erfolge & Motivation",
                icon = Icons.Default.EmojiEvents,
            ) {
                SwitchPreference(
                    title = "Erfolg-Benachrichtigungen",
                    subtitle = "Sofortige Benachrichtigung bei neuen Erfolgen",
                    checked = achievementNotificationsEnabled && hasNotificationPermission,
                    onCheckedChange = { achievementNotificationsEnabled = it },
                    showPermissionWarning = !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                )

                SwitchPreference(
                    title = "Tägliche Motivation",
                    subtitle = "Motivierende Nachrichten jeden Morgen",
                    checked = dailyMotivationEnabled && hasNotificationPermission,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestPermissionIfNeeded {
                                dailyMotivationEnabled = true
                                DailyMotivationWorker.scheduleWork(context)
                            }
                        } else {
                            dailyMotivationEnabled = false
                            DailyMotivationWorker.cancelWork(context)
                        }
                    },
                    showPermissionWarning = !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                )
            }

            // Quiet Hours Section
            NotificationSectionCard(
                title = "Ruhezeiten",
                icon = Icons.Default.Bedtime,
            ) {
                SwitchPreference(
                    title = "Ruhezeiten aktivieren",
                    subtitle = "Keine Benachrichtigungen während dieser Zeit",
                    checked = quietHoursEnabled,
                    onCheckedChange = { quietHoursEnabled = it },
                )

                if (quietHoursEnabled) {
                    TimePreference(
                        title = "Ruhezeit Start",
                        subtitle = "Beginn der Ruhezeit",
                        time = quietHoursStart,
                        onTimeChange = { quietHoursStart = it },
                    )

                    TimePreference(
                        title = "Ruhezeit Ende",
                        subtitle = "Ende der Ruhezeit",
                        time = quietHoursEnd,
                        onTimeChange = { quietHoursEnd = it },
                    )
                }
            }

            // Preview Section
            NotificationSectionCard(
                title = "Vorschau",
                icon = Icons.Default.Preview,
            ) {
                Button(
                    onClick = {
                        requestPermissionIfNeeded {
                            SmartNotificationManager.showWorkoutReminder(
                                context,
                                "Test-Training",
                                "jetzt",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasNotificationPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU,
                ) {
                    Icon(Icons.Default.Notifications, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Workout-Erinnerung testen")
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        requestPermissionIfNeeded {
                            SmartNotificationManager.showWaterReminder(
                                context,
                                1200,
                                2000,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasNotificationPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU,
                ) {
                    Icon(Icons.Default.WaterDrop, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Wasser-Erinnerung testen")
                }

                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "💡 Tipp: Aktiviere die Test-Buttons durch Tippen darauf - sie fragen nach der benötigten Berechtigung.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
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
    onCheckedChange: (Boolean) -> Unit,
    showPermissionWarning: Boolean = false,
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }

        if (showPermissionWarning) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Benachrichtigungserlaubnis erforderlich",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePreference(
    title: String,
    subtitle: String,
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        TextButton(
            onClick = { showTimePicker = true },
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
                    Text(
                        "Aktuelle Zeit: ${time.hour.toString().padStart(
                            2,
                            '0',
                        )}:${time.minute.toString().padStart(2, '0')}",
                    )
                    Text(
                        "Zeit-Picker würde hier implementiert werden",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("OK")
                }
            },
        )
    }
}

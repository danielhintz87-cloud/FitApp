package com.example.fitapp.wear.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.example.fitapp.shared.WearPersonalRecord
import com.example.fitapp.wear.presentation.viewmodel.WearWorkoutViewModel

@Composable
fun ProgressWearScreen(
    onNavigateBack: () -> Unit,
    viewModel: WearWorkoutViewModel = viewModel()
) {
    val progressData by viewModel.progressData.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        TimeText()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ScalingLazyColumn(
            state = rememberScalingLazyListState(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Progress summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Progress",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Text(
                            text = "Your Progress",
                            style = MaterialTheme.typography.title3,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Weekly stats
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "This Week",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProgressStatItem(
                                icon = Icons.Default.FitnessCenter,
                                value = "${progressData.weeklyWorkouts}",
                                label = "Workouts",
                                color = Color.Blue
                            )
                            
                            ProgressStatItem(
                                icon = Icons.Default.LocalFireDepartment,
                                value = "${progressData.weeklyCaloriesBurned}",
                                label = "Calories",
                                color = Color.Orange
                            )
                        }
                    }
                }
            }
            
            // Streak information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (progressData.currentStreak > 0) 
                        MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                    else MaterialTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Streak",
                            tint = if (progressData.currentStreak > 0) Color.Orange else MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Text(
                            text = "${progressData.currentStreak}",
                            style = MaterialTheme.typography.title1,
                            fontWeight = FontWeight.Bold,
                            color = if (progressData.currentStreak > 0) Color.Orange else MaterialTheme.colors.onSurface
                        )
                        
                        Text(
                            text = if (progressData.currentStreak == 1) "Day Streak" else "Days Streak",
                            style = MaterialTheme.typography.caption1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Personal records
            if (progressData.personalRecords.isNotEmpty()) {
                item {
                    Text(
                        text = "Personal Records",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                
                items(progressData.personalRecords.take(3)) { record ->
                    PersonalRecordCard(record = record)
                }
            }
            
            // Total workouts
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Workouts",
                                style = MaterialTheme.typography.body2
                            )
                            Text(
                                text = "${progressData.totalWorkouts}",
                                style = MaterialTheme.typography.title2,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.primary
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Total",
                            tint = MaterialTheme.colors.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Last workout
            if (progressData.lastWorkoutDate.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Last Workout",
                                style = MaterialTheme.typography.caption1,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = progressData.lastWorkoutDate,
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Back button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CompactButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
fun ProgressStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption2
        )
    }
}

@Composable
fun PersonalRecordCard(record: WearPersonalRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = record.exerciseName,
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${record.value} ${record.unit}",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.primary
                )
            }
            
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Personal Record",
                tint = Color.Yellow,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
package com.example.fitapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitapp.data.db.SocialChallengeEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialChallengesScreen(
    modifier: Modifier = Modifier,
    viewModel: SocialChallengesViewModel = viewModel()
) {
    val context = LocalContext.current
    val challenges by viewModel.challenges.collectAsState()
    val userParticipations by viewModel.userParticipations.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadChallenges()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Social Challenges",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { viewModel.refreshChallenges() }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(viewModel.categories) { category ->
                FilterChip(
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category.replaceFirstChar { it.uppercase() }) },
                    selected = viewModel.selectedCategory.value == category
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Challenges list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(challenges) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    isParticipating = userParticipations.any { it.challengeId == challenge.id },
                    onJoin = { viewModel.joinChallenge(challenge.id) },
                    onLeave = { viewModel.leaveChallenge(challenge.id) }
                )
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: SocialChallengeEntity,
    isParticipating: Boolean,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with difficulty and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DifficultyChip(difficulty = challenge.difficulty)
                StatusChip(status = challenge.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title and description
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Challenge details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChallengeDetailItem(
                    icon = Icons.Default.Flag,
                    label = "Ziel",
                    value = "${challenge.targetValue.toInt()} ${challenge.unit}"
                )
                
                ChallengeDetailItem(
                    icon = Icons.Default.Schedule,
                    label = "Dauer",
                    value = "${challenge.duration} Tage"
                )
                
                ChallengeDetailItem(
                    icon = Icons.Default.People,
                    label = "Teilnehmer",
                    value = challenge.currentParticipants.toString()
                )
            }
            
            // Dates
            val startDate = try {
                LocalDate.parse(challenge.startDate).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            } catch (e: Exception) { challenge.startDate }
            
            val endDate = try {
                LocalDate.parse(challenge.endDate).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            } catch (e: Exception) { challenge.endDate }
            
            Text(
                text = "Von $startDate bis $endDate",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            // Reward
            challenge.reward?.let { reward ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Belohnung: $reward",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action button
            if (challenge.status == "active" || challenge.status == "upcoming") {
                if (isParticipating) {
                    OutlinedButton(
                        onClick = onLeave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Challenge verlassen")
                    }
                } else {
                    Button(
                        onClick = onJoin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Challenge beitreten")
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DifficultyChip(
    difficulty: String,
    modifier: Modifier = Modifier
) {
    val (color, emoji) = when (difficulty) {
        "beginner" -> MaterialTheme.colorScheme.primary to "🟢"
        "intermediate" -> MaterialTheme.colorScheme.secondary to "🟡"
        "advanced" -> MaterialTheme.colorScheme.tertiary to "🟠"
        "expert" -> MaterialTheme.colorScheme.error to "🔴"
        else -> MaterialTheme.colorScheme.outline to "⭐"
    }
    
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = "$emoji ${difficulty.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.labelSmall
            ) 
        },
        modifier = modifier
    )
}

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (status) {
        "upcoming" -> MaterialTheme.colorScheme.primary to "Geplant"
        "active" -> MaterialTheme.colorScheme.tertiary to "Aktiv"
        "completed" -> MaterialTheme.colorScheme.outline to "Beendet"
        "cancelled" -> MaterialTheme.colorScheme.error to "Abgebrochen"
        else -> MaterialTheme.colorScheme.outline to status
    }
    
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            ) 
        },
        modifier = modifier
    )
}

@Composable
private fun LazyRow(
    horizontalArrangement: Arrangement.Horizontal,
    modifier: Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = horizontalArrangement,
        modifier = modifier,
        content = content
    )
}
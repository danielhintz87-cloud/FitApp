package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.*
import com.example.fitapp.data.repo.PersonalMotivationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SocialChallengeManager(
    private val context: Context,
    private val repository: PersonalMotivationRepository,
) {
    companion object {
        // Challenge categories
        const val CATEGORY_FITNESS = "fitness"
        const val CATEGORY_NUTRITION = "nutrition"
        const val CATEGORY_WEIGHT_LOSS = "weight_loss"
        const val CATEGORY_ENDURANCE = "endurance"
        const val CATEGORY_STRENGTH = "strength"

        // Challenge types
        const val TYPE_PUBLIC = "public"
        const val TYPE_PRIVATE = "private"
        const val TYPE_FEATURED = "featured"

        // Challenge status
        const val STATUS_UPCOMING = "upcoming"
        const val STATUS_ACTIVE = "active"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_CANCELLED = "cancelled"

        // Participation status
        const val PARTICIPATION_ACTIVE = "active"
        const val PARTICIPATION_COMPLETED = "completed"
        const val PARTICIPATION_QUIT = "quit"
        const val PARTICIPATION_FAILED = "failed"

        // Difficulty levels
        const val DIFFICULTY_BEGINNER = "beginner"
        const val DIFFICULTY_INTERMEDIATE = "intermediate"
        const val DIFFICULTY_ADVANCED = "advanced"
        const val DIFFICULTY_EXPERT = "expert"
    }

    fun getAllChallenges(): Flow<List<SocialChallengeEntity>> = repository.allSocialChallengesFlow()

    fun getActiveChallenges(): Flow<List<SocialChallengeEntity>> = repository.challengesByStatusFlow(STATUS_ACTIVE)

    fun getChallengesByCategory(category: String): Flow<List<SocialChallengeEntity>> =
        repository.challengesByCategoryFlow(category)

    fun getOfficialChallenges(): Flow<List<SocialChallengeEntity>> = repository.officialChallengesFlow()

    fun getUserParticipations(userId: String): Flow<List<ChallengeParticipationEntity>> =
        repository.participationsByUserFlow(userId)

    /**
     * Initialize default social challenges for new users
     */
    suspend fun initializeDefaultChallenges() {
        val existingChallenges = repository.allSocialChallengesFlow().first()
        if (existingChallenges.isNotEmpty()) return

        val defaultChallenges = createDefaultChallenges()
        defaultChallenges.forEach { challenge ->
            repository.insertSocialChallenge(challenge)
        }
    }

    /**
     * Join a public challenge
     */
    suspend fun joinChallenge(
        challengeId: Long,
        userId: String,
        userName: String? = null,
    ): Boolean {
        val challenge = repository.getSocialChallenge(challengeId) ?: return false

        // Check if challenge is joinable
        if (challenge.status != STATUS_ACTIVE && challenge.status != STATUS_UPCOMING) {
            return false
        }

        // Check if user already joined
        val existingParticipation = repository.getUserParticipation(challengeId, userId)
        if (existingParticipation != null) {
            return false
        }

        // Check participant limit
        if (challenge.maxParticipants != null &&
            challenge.currentParticipants >= challenge.maxParticipants
        ) {
            return false
        }

        // Create participation record
        val participation =
            ChallengeParticipationEntity(
                challengeId = challengeId,
                userId = userId,
                userName = userName,
                status = PARTICIPATION_ACTIVE,
            )

        repository.insertChallengeParticipation(participation)

        // Update participant count
        repository.updateParticipantCount(challengeId, challenge.currentParticipants + 1)

        // Send notification
        SmartNotificationManager.showChallengeJoined(context, challenge)

        return true
    }

    /**
     * Leave a challenge (quit)
     */
    suspend fun leaveChallenge(
        challengeId: Long,
        userId: String,
    ) {
        val participation = repository.getUserParticipation(challengeId, userId) ?: return
        val challenge = repository.getSocialChallenge(challengeId) ?: return

        // Update participation status
        repository.updateParticipationStatus(participation.id, PARTICIPATION_QUIT, null)

        // Decrease participant count
        repository.updateParticipantCount(challengeId, maxOf(0, challenge.currentParticipants - 1))
    }

    /**
     * Log progress for a challenge
     */
    suspend fun logProgress(
        challengeId: Long,
        userId: String,
        value: Double,
        description: String? = null,
        source: String = "manual",
    ) {
        val participation = repository.getUserParticipation(challengeId, userId) ?: return
        val challenge = repository.getSocialChallenge(challengeId) ?: return

        if (participation.status != PARTICIPATION_ACTIVE) return

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Insert progress log
        val progressLog =
            ChallengeProgressLogEntity(
                participationId = participation.id,
                logDate = today,
                value = value,
                description = description,
                source = source,
            )

        repository.insertChallengeProgressLog(progressLog)

        // Update participation progress
        val totalProgress = repository.getTotalProgress(participation.id) ?: 0.0
        val progressPercentage = (totalProgress / challenge.targetValue * 100.0).coerceIn(0.0, 100.0)

        repository.updateParticipationProgress(
            participation.id,
            totalProgress,
            progressPercentage,
            today,
        )

        // Check if challenge is completed
        if (totalProgress >= challenge.targetValue) {
            completeChallenge(challengeId, userId)
        }

        // Update leaderboard
        updateLeaderboard(challengeId)
    }

    /**
     * Complete a challenge for a user
     */
    private suspend fun completeChallenge(
        challengeId: Long,
        userId: String,
    ) {
        val participation = repository.getUserParticipation(challengeId, userId) ?: return
        val challenge = repository.getSocialChallenge(challengeId) ?: return

        val now = System.currentTimeMillis() / 1000

        // Update participation status
        repository.updateParticipationStatus(participation.id, PARTICIPATION_COMPLETED, now)

        // Award badge if specified
        challenge.reward?.let { reward ->
            awardChallengeBadge(userId, challenge, reward)
        }

        // Send celebration notification
        SmartNotificationManager.showChallengeCompleted(context, challenge)
    }

    /**
     * Award a badge for completing a challenge
     */
    private suspend fun awardChallengeBadge(
        userId: String,
        challenge: SocialChallengeEntity,
        reward: String,
    ) {
        val badge =
            SocialBadgeEntity(
                title = reward,
                description = "Earned by completing ${challenge.title}",
                category = "challenge",
                badgeType =
                    when (challenge.difficulty) {
                        DIFFICULTY_BEGINNER -> "bronze"
                        DIFFICULTY_INTERMEDIATE -> "silver"
                        DIFFICULTY_ADVANCED -> "gold"
                        DIFFICULTY_EXPERT -> "platinum"
                        else -> "bronze"
                    },
                iconName = "emoji_events",
                rarity =
                    when (challenge.difficulty) {
                        DIFFICULTY_BEGINNER -> "common"
                        DIFFICULTY_INTERMEDIATE -> "rare"
                        DIFFICULTY_ADVANCED -> "epic"
                        DIFFICULTY_EXPERT -> "legendary"
                        else -> "common"
                    },
                requirements = "Complete ${challenge.title}",
                challengeId = challenge.id,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() / 1000,
                progress = 100.0,
            )

        repository.insertSocialBadge(badge)
    }

    /**
     * Update leaderboard for a challenge
     */
    private suspend fun updateLeaderboard(challengeId: Long) {
        val participations = repository.participationsByChallengeFlow(challengeId).first()
        val sortedParticipations = participations.sortedByDescending { it.currentProgress }

        // Clear existing leaderboard
        repository.clearLeaderboard(challengeId)

        // Create new leaderboard entries
        sortedParticipations.forEachIndexed { index, participation ->
            val rank = index + 1
            val badge =
                when (rank) {
                    1 -> "🥇 Gold"
                    2 -> "🥈 Silver"
                    3 -> "🥉 Bronze"
                    else -> null
                }

            val entry =
                LeaderboardEntryEntity(
                    challengeId = challengeId,
                    userId = participation.userId,
                    userName = participation.userName,
                    rank = rank,
                    score = participation.currentProgress,
                    completionTime = participation.completedAt,
                    badge = badge,
                )

            repository.insertLeaderboardEntry(entry)

            // Update rank in participation
            repository.updateParticipationRank(participation.id, rank)
        }
    }

    /**
     * Get leaderboard for a challenge
     */
    fun getChallengeLeaderboard(challengeId: Long): Flow<List<LeaderboardEntryEntity>> =
        repository.leaderboardByChallengeFlow(challengeId)

    /**
     * Track workout completion for challenges
     */
    suspend fun trackWorkoutForChallenges(userId: String) {
        val activeParticipations =
            repository.participationsByUserFlow(userId).first()
                .filter { it.status == PARTICIPATION_ACTIVE }

        for (participation in activeParticipations) {
            val challenge = repository.getSocialChallenge(participation.challengeId) ?: continue

            // Check if this challenge tracks workouts
            if (challenge.targetMetric == "workouts") {
                logProgress(
                    challengeId = challenge.id,
                    userId = userId,
                    value = 1.0,
                    description = "Completed workout",
                    source = "workout",
                )
            }
        }
    }

    /**
     * Track nutrition logging for challenges
     */
    suspend fun trackNutritionForChallenges(
        userId: String,
        calories: Int,
    ) {
        val activeParticipations =
            repository.participationsByUserFlow(userId).first()
                .filter { it.status == PARTICIPATION_ACTIVE }

        for (participation in activeParticipations) {
            val challenge = repository.getSocialChallenge(participation.challengeId) ?: continue

            // Check if this challenge tracks calories
            if (challenge.targetMetric == "calories") {
                logProgress(
                    challengeId = challenge.id,
                    userId = userId,
                    value = calories.toDouble(),
                    description = "Logged nutrition",
                    source = "nutrition",
                )
            }
        }
    }

    private fun createDefaultChallenges(): List<SocialChallengeEntity> {
        val today = LocalDate.now()
        val weekStart = today.plusDays(1)
        val monthStart = today.plusDays(7)

        return listOf(
            // Beginner 7-day workout challenge
            SocialChallengeEntity(
                title = "7-Tage Workout Challenge",
                description = "Absolviere 7 Workouts in 7 Tagen",
                category = CATEGORY_FITNESS,
                challengeType = TYPE_PUBLIC,
                targetMetric = "workouts",
                targetValue = 7.0,
                unit = "Workouts",
                duration = 7,
                startDate = weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate = weekStart.plusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE),
                status = STATUS_UPCOMING,
                reward = "Workout Warrior",
                difficulty = DIFFICULTY_BEGINNER,
                isOfficial = true,
            ),
            // Intermediate calorie challenge
            SocialChallengeEntity(
                title = "14-Tage Kalorienziel Challenge",
                description = "Erreiche 14 Tage lang dein tägliches Kalorienziel",
                category = CATEGORY_NUTRITION,
                challengeType = TYPE_PUBLIC,
                targetMetric = "calories",
                targetValue = 14000.0, // 14 days * ~1000 calories under goal
                unit = "kcal",
                duration = 14,
                startDate = weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate = weekStart.plusDays(13).format(DateTimeFormatter.ISO_LOCAL_DATE),
                status = STATUS_UPCOMING,
                reward = "Nutrition Master",
                difficulty = DIFFICULTY_INTERMEDIATE,
                isOfficial = true,
            ),
            // Advanced 30-day consistency challenge
            SocialChallengeEntity(
                title = "30-Tage Konsistenz Challenge",
                description = "Trainiere 30 Tage ohne Unterbrechung",
                category = CATEGORY_FITNESS,
                challengeType = TYPE_FEATURED,
                targetMetric = "workouts",
                targetValue = 30.0,
                unit = "Workouts",
                duration = 30,
                startDate = monthStart.format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate = monthStart.plusDays(29).format(DateTimeFormatter.ISO_LOCAL_DATE),
                status = STATUS_UPCOMING,
                reward = "Consistency Champion",
                difficulty = DIFFICULTY_ADVANCED,
                isOfficial = true,
            ),
            // Weight loss challenge
            SocialChallengeEntity(
                title = "5kg in 4 Wochen Challenge",
                description = "Verliere 5kg in 4 Wochen auf gesunde Weise",
                category = CATEGORY_WEIGHT_LOSS,
                challengeType = TYPE_PUBLIC,
                targetMetric = "weight_loss",
                targetValue = 5.0,
                unit = "kg",
                duration = 28,
                startDate = monthStart.format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate = monthStart.plusDays(27).format(DateTimeFormatter.ISO_LOCAL_DATE),
                status = STATUS_UPCOMING,
                reward = "Weight Loss Hero",
                difficulty = DIFFICULTY_EXPERT,
                isOfficial = true,
                maxParticipants = 50,
            ),
        )
    }
}

package com.example.fitapp.services

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced Motivation Psychology with Behavioral Science Integration
 * Based on latest fitness psychology research and commercial app strategies
 */
@Singleton
class AdvancedMotivationAI @Inject constructor(
    private val context: Context
) {
    
    /**
     * Advanced Motivation Psychology Data Classes
     */
    enum class MotivationType {
        STREAK_PROTECTION,      // Loss aversion - protect existing streaks
        MOMENTUM_BUILDING,      // Capitalize on existing progress
        FRESH_START,           // Clean slate motivation
        CONSISTENCY_BUILDING,   // Habit formation focus
        ACHIEVEMENT_CELEBRATION, // Success reinforcement
        PROGRESS_ACCELERATION,  // Growth mindset activation
        CHALLENGE_REFRAME,     // Difficulty as opportunity
        STEADY_PROGRESS;       // Sustainable pace emphasis
        
        fun getDefaultMessage(): String = when (this) {
            STREAK_PROTECTION -> "Schütze deinen wertvollen Streak! 🛡️"
            MOMENTUM_BUILDING -> "Dein Momentum ist stark - nutze es! 🚀"
            FRESH_START -> "Jeder Moment ist ein Neuanfang! 🌱"
            CONSISTENCY_BUILDING -> "Kleine Schritte, große Erfolge! 👣"
            ACHIEVEMENT_CELEBRATION -> "Du bist auf dem Weg zur Greatness! ⭐"
            PROGRESS_ACCELERATION -> "Zeit, das nächste Level zu erreichen! 📈"
            CHALLENGE_REFRAME -> "Herausforderungen machen dich stärker! 💎"
            STEADY_PROGRESS -> "Konstanz ist deine Superpower! 🎯"
        }
        
        fun getEveningMessage(): String = when (this) {
            STREAK_PROTECTION -> "Ein starkes Training sichert deinen Streak für morgen!"
            MOMENTUM_BUILDING -> "Perfekte Zeit, um den Tag mit Power zu beenden!"
            FRESH_START -> "Heute Abend beginnt deine neue Fitness-Routine!"
            CONSISTENCY_BUILDING -> "Regelmäßiges Abendtraining formt starke Gewohnheiten!"
            ACHIEVEMENT_CELEBRATION -> "Kröne deinen erfolgreichen Tag mit einem Workout!"
            PROGRESS_ACCELERATION -> "Abends trainieren = maximale Regeneration über Nacht!"
            CHALLENGE_REFRAME -> "Herausforderungen am Abend stärken den Charakter!"
            STEADY_PROGRESS -> "Gleichmäßiges Training am Abend optimiert deinen Schlaf!"
        }
    }
    
    enum class EnergyLevel {
        HIGH, MEDIUM, LOW;
        
        val morningBoost: String get() = when (this) {
            HIGH -> "⚡ Deine Energie ist fantastisch!"
            MEDIUM -> "🌅 Perfekte Startenergie!"
            LOW -> "🌿 Sanft in den Tag starten!"
        }
        
        val workoutIntensity: String get() = when (this) {
            HIGH -> "intensives HIIT oder Krafttraining"
            MEDIUM -> "moderates Ganzkörpertraining"
            LOW -> "sanftes Yoga oder Mobility"
        }
    }
    
    enum class IntensityPreference {
        HIIT_LOVER, STRENGTH_FOCUSED, CARDIO_ENTHUSIAST, BALANCED_APPROACH, GENTLE_MOVEMENT;
        
        fun getWorkoutSuggestion(): String = when (this) {
            HIIT_LOVER -> "ein explosives HIIT-Training"
            STRENGTH_FOCUSED -> "kraftvolles Strength-Training" 
            CARDIO_ENTHUSIAST -> "energiegeladenes Cardio"
            BALANCED_APPROACH -> "ausgewogenes Full-Body Training"
            GENTLE_MOVEMENT -> "entspanntes Mobility-Training"
        }
        
        fun getTimeOptimizedSuggestion(minutes: Int): String = when (this) {
            HIIT_LOVER -> if (minutes < 20) "Quickfire HIIT-Blast" else "Full Power HIIT-Session"
            STRENGTH_FOCUSED -> if (minutes < 30) "Express Strength Circuit" else "Complete Strength Training"
            CARDIO_ENTHUSIAST -> if (minutes < 25) "Cardio Power Boost" else "Extended Cardio Adventure"
            BALANCED_APPROACH -> if (minutes < 20) "Total Body Express" else "Comprehensive Workout"
            GENTLE_MOVEMENT -> if (minutes < 15) "Quick Mobility Flow" else "Deep Stretch & Recovery"
        }
    }
    
    data class MotivationProfile(
        val primaryType: MotivationType,
        val secondaryType: MotivationType,
        val energyLevel: EnergyLevel,
        val preferredIntensity: IntensityPreference,
        val personalityTraits: List<PersonalityTrait> = emptyList()
    )
    
    enum class PersonalityTrait {
        COMPETITIVE,        // Loves challenges and comparisons
        COMMUNITY_DRIVEN,   // Motivated by social connections
        DATA_FOCUSED,       // Driven by metrics and progress
        ROUTINE_ORIENTED,   // Prefers consistency and habits
        VARIETY_SEEKER,     // Needs diverse workouts
        ACHIEVEMENT_HUNTER  // Collects badges and milestones
    }
    
    /**
     * Behavioral Economics Principles Integration
     */
    data class BehavioralTrigger(
        val type: TriggerType,
        val intensity: TriggerIntensity,
        val message: String,
        val actionPrompt: String
    )
    
    enum class TriggerType {
        LOSS_AVERSION,      // "Don't lose your streak!"
        SOCIAL_PROOF,       // "75% of users your age train in the morning"
        ANCHORING,          // "Your best week had 4 workouts"
        ENDOWMENT_EFFECT,   // "Your earned achievements"
        PRESENT_BIAS        // "Immediate benefits of today's workout"
    }
    
    enum class TriggerIntensity {
        SUBTLE, MODERATE, STRONG, URGENT
    }
    
    /**
     * Generate Advanced Motivational Message with Psychology Integration
     */
    fun generateAdvancedMotivation(
        userStats: UserStats,
        activityContext: ActivityContext,
        timeContext: TimeContext
    ): AdvancedMotivationalMessage {
        
        val profile = analyzeMotivationProfile(userStats, activityContext)
        val triggers = identifyBehavioralTriggers(userStats, timeContext)
        val personalizedContent = buildPersonalizedContent(profile, triggers, timeContext)
        
        return AdvancedMotivationalMessage(
            profile = profile,
            triggers = triggers,
            content = personalizedContent,
            actionButtons = generateSmartActions(profile, timeContext),
            visualElements = generateVisualCues(profile),
            psychologyPrinciples = identifyActivePrinciples(triggers)
        )
    }
    
    private fun analyzeMotivationProfile(userStats: UserStats, context: ActivityContext): MotivationProfile {
        val streakMotivation = when {
            userStats.currentStreak >= 7 -> MotivationType.STREAK_PROTECTION
            userStats.currentStreak >= 3 -> MotivationType.MOMENTUM_BUILDING
            userStats.currentStreak == 0 -> MotivationType.FRESH_START
            else -> MotivationType.CONSISTENCY_BUILDING
        }
        
        val progressMotivation = when {
            userStats.weeklyProgress > 80 -> MotivationType.ACHIEVEMENT_CELEBRATION
            userStats.weeklyProgress > 50 -> MotivationType.PROGRESS_ACCELERATION
            userStats.weeklyProgress < 30 -> MotivationType.CHALLENGE_REFRAME
            else -> MotivationType.STEADY_PROGRESS
        }
        
        val energyLevel = estimateEnergyLevel(context)
        val intensityPref = analyzeIntensityPreference(userStats)
        val personality = analyzePersonalityTraits(userStats)
        
        return MotivationProfile(
            primaryType = streakMotivation,
            secondaryType = progressMotivation,
            energyLevel = energyLevel,
            preferredIntensity = intensityPref,
            personalityTraits = personality
        )
    }
    
    private fun identifyBehavioralTriggers(userStats: UserStats, timeContext: TimeContext): List<BehavioralTrigger> {
        val triggers = mutableListOf<BehavioralTrigger>()
        
        // Loss Aversion for Streaks
        if (userStats.currentStreak > 3) {
            triggers.add(BehavioralTrigger(
                type = TriggerType.LOSS_AVERSION,
                intensity = TriggerIntensity.STRONG,
                message = "🔥 Dein ${userStats.currentStreak}-Tage-Streak ist wertvoll - schütze ihn!",
                actionPrompt = "Streak sichern"
            ))
        }
        
        // Social Proof
        triggers.add(BehavioralTrigger(
            type = TriggerType.SOCIAL_PROOF,
            intensity = TriggerIntensity.MODERATE,
            message = "💪 83% der erfolgreichen FitApp-Nutzer trainieren ${timeContext.getOptimalTimeFrame()}",
            actionPrompt = "Zur erfolgreichen Gruppe gehören"
        ))
        
        // Anchoring Effect
        if (userStats.bestWeekWorkouts > userStats.currentWeekWorkouts) {
            triggers.add(BehavioralTrigger(
                type = TriggerType.ANCHORING,
                intensity = TriggerIntensity.MODERATE,
                message = "📈 Deine beste Woche hatte ${userStats.bestWeekWorkouts} Workouts - das kannst du wieder schaffen!",
                actionPrompt = "Neuen Rekord aufstellen"
            ))
        }
        
        return triggers
    }
    
    private fun buildPersonalizedContent(
        profile: MotivationProfile, 
        triggers: List<BehavioralTrigger>,
        timeContext: TimeContext
    ): String {
        val baseMessage = generateBaseMessage(profile, timeContext)
        val triggerEnhancement = triggers.firstOrNull()?.message ?: ""
        val personalityBoost = generatePersonalityBoost(profile.personalityTraits)
        
        return "$baseMessage $triggerEnhancement $personalityBoost".trim()
    }
    
    private fun generateBaseMessage(profile: MotivationProfile, timeContext: TimeContext): String {
        return when (timeContext.period) {
            TimePeriod.MORNING -> buildMorningMessage(profile)
            TimePeriod.MIDDAY -> buildMiddayMessage(profile)
            TimePeriod.AFTERNOON -> buildAfternoonMessage(profile)
            TimePeriod.EVENING -> buildEveningMessage(profile)
            TimePeriod.NIGHT -> buildNightMessage(profile)
        }
    }
    
    private fun buildMorningMessage(profile: MotivationProfile): String {
        val energyBoost = profile.energyLevel.morningBoost
        val motivationType = profile.primaryType.getDefaultMessage()
        return "$energyBoost $motivationType Perfekter Start mit ${profile.preferredIntensity.getWorkoutSuggestion()}!"
    }
    
    private fun buildMiddayMessage(profile: MotivationProfile): String {
        return when (profile.energyLevel) {
            EnergyLevel.HIGH -> "⚡ Mittagspower! Nutze deine Energie für ${profile.preferredIntensity.getTimeOptimizedSuggestion(20)}!"
            EnergyLevel.MEDIUM -> "🎯 Mittagspause optimal nutzen! ${profile.preferredIntensity.getTimeOptimizedSuggestion(15)} = 200% besserer Nachmittag!"
            EnergyLevel.LOW -> "🌿 Sanftes Mittagstraining kann Wunder wirken! ${profile.preferredIntensity.getTimeOptimizedSuggestion(10)} für neue Energie!"
        }
    }
    
    private fun buildAfternoonMessage(profile: MotivationProfile): String {
        return "🏃‍♀️ Nachmittagsenergie perfekt für ${profile.preferredIntensity.getWorkoutSuggestion()}! Stress abbauen und Endorphine aktivieren!"
    }
    
    private fun buildEveningMessage(profile: MotivationProfile): String {
        return "🌆 Feierabend-Training für perfekten Tagesabschluss! ${profile.secondaryType.getEveningMessage()}"
    }
    
    private fun buildNightMessage(profile: MotivationProfile): String {
        return "🌙 Späte Stunde, aber nie zu spät für deine Gesundheit! Sanftes ${profile.preferredIntensity.getTimeOptimizedSuggestion(10)} oder Meditation?"
    }
    
    private fun generatePersonalityBoost(traits: List<PersonalityTrait>): String {
        return traits.firstOrNull()?.let { trait ->
            when (trait) {
                PersonalityTrait.COMPETITIVE -> "🏆 Zeig allen, was echter Champion-Spirit bedeutet!"
                PersonalityTrait.COMMUNITY_DRIVEN -> "👥 Deine Fitness-Community glaubt an dich!"
                PersonalityTrait.DATA_FOCUSED -> "📊 Deine Statistiken zeigen: Du bist auf dem richtigen Weg!"
                PersonalityTrait.ROUTINE_ORIENTED -> "⏰ Deine Routine ist deine Stärke - halte sie aufrecht!"
                PersonalityTrait.VARIETY_SEEKER -> "🎨 Heute etwas Neues ausprobieren für maximale Motivation!"
                PersonalityTrait.ACHIEVEMENT_HUNTER -> "🏅 Neue Achievements warten auf dich!"
            }
        } ?: ""
    }
    
    // Helper functions with mock implementations
    private fun estimateEnergyLevel(context: ActivityContext): EnergyLevel = EnergyLevel.MEDIUM
    private fun analyzeIntensityPreference(stats: UserStats): IntensityPreference = IntensityPreference.BALANCED_APPROACH
    private fun analyzePersonalityTraits(stats: UserStats): List<PersonalityTrait> = listOf(PersonalityTrait.DATA_FOCUSED)
    private fun generateSmartActions(profile: MotivationProfile, timeContext: TimeContext): List<String> = 
        listOf("💪 Jetzt starten", "⏰ In 30 Min", "🎯 Workout anpassen")
    private fun generateVisualCues(profile: MotivationProfile): List<String> = listOf("🔥", "💪", "⭐")
    private fun identifyActivePrinciples(triggers: List<BehavioralTrigger>): List<String> = 
        triggers.map { it.type.name }
}

/**
 * Supporting Data Classes
 */
data class UserStats(
    val currentStreak: Int = 0,
    val weeklyProgress: Int = 0,
    val bestWeekWorkouts: Int = 0,
    val currentWeekWorkouts: Int = 0,
    val hiitWorkoutsCompleted: Int = 0,
    val strengthWorkouts: Int = 0,
    val cardioMinutes: Int = 0,
    val mobilitySessionsCompleted: Int = 0
)

data class ActivityContext(
    val daysSinceLastWorkout: Int = 0,
    val recentPerformanceScore: Int = 75,
    val availableTime: Int = 30
)

data class TimeContext(
    val period: TimePeriod,
    val availableMinutes: Int = 30
) {
    fun getOptimalTimeFrame(): String = when (period) {
        TimePeriod.MORNING -> "morgens"
        TimePeriod.MIDDAY -> "mittags"
        TimePeriod.AFTERNOON -> "nachmittags"
        TimePeriod.EVENING -> "abends"
        TimePeriod.NIGHT -> "spät abends"
    }
}

enum class TimePeriod {
    MORNING, MIDDAY, AFTERNOON, EVENING, NIGHT
}

data class AdvancedMotivationalMessage(
    val profile: AdvancedMotivationAI.MotivationProfile,
    val triggers: List<AdvancedMotivationAI.BehavioralTrigger>,
    val content: String,
    val actionButtons: List<String>,
    val visualElements: List<String>,
    val psychologyPrinciples: List<String>
)

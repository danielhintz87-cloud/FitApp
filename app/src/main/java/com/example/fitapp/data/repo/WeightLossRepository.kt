package com.example.fitapp.data.repo

import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.BMIHistoryEntity
import com.example.fitapp.data.db.BehavioralCheckInEntity
import com.example.fitapp.data.db.ProgressPhotoEntity
import com.example.fitapp.data.db.WeightLossProgramEntity
import com.example.fitapp.domain.ActivityLevel
import com.example.fitapp.domain.BMICalculator
import com.example.fitapp.domain.BMIResult
import com.example.fitapp.domain.WeightLossMilestone
import com.example.fitapp.domain.WeightLossProgram
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository for BMI and weight loss program management
 */
class WeightLossRepository(private val database: AppDatabase) {
    // BMI History Management

    suspend fun saveBMIHistory(bmiHistory: BMIHistoryEntity): Long {
        return database.bmiHistoryDao().insert(bmiHistory)
    }

    suspend fun updateBMIHistory(bmiHistory: BMIHistoryEntity) {
        database.bmiHistoryDao().update(bmiHistory)
    }

    suspend fun deleteBMIHistory(id: Long) {
        database.bmiHistoryDao().delete(id)
    }

    suspend fun getBMIHistoryByDate(date: String): BMIHistoryEntity? {
        return database.bmiHistoryDao().getByDate(date)
    }

    suspend fun getAllBMIHistory(): List<BMIHistoryEntity> {
        return database.bmiHistoryDao().getAll()
    }

    fun getAllBMIHistoryFlow(): Flow<List<BMIHistoryEntity>> {
        return database.bmiHistoryDao().getAllFlow()
    }

    suspend fun getRecentBMIHistory(limit: Int): List<BMIHistoryEntity> {
        return database.bmiHistoryDao().getRecent(limit)
    }

    suspend fun getBMIHistoryByDateRange(
        startDate: String,
        endDate: String,
    ): List<BMIHistoryEntity> {
        return database.bmiHistoryDao().getByDateRange(startDate, endDate)
    }

    /**
     * Calculate and save BMI result
     */
    suspend fun calculateAndSaveBMI(
        heightCm: Float,
        weightKg: Float,
        date: String = LocalDate.now().toString(),
        notes: String? = null,
    ): BMIResult {
        val bmiResult = BMICalculator.calculateBMIResult(heightCm, weightKg)

        val bmiHistory =
            BMIHistoryEntity(
                date = date,
                height = heightCm,
                weight = weightKg,
                bmi = bmiResult.bmi,
                category = bmiResult.category.name,
                notes = notes,
            )

        // Check if entry for date exists and update or insert
        val existing = getBMIHistoryByDate(date)
        if (existing != null) {
            updateBMIHistory(bmiHistory.copy(id = existing.id))
        } else {
            saveBMIHistory(bmiHistory)
        }

        return bmiResult
    }

    // Weight Loss Program Management

    suspend fun saveWeightLossProgram(program: WeightLossProgramEntity): Long {
        return database.weightLossProgramDao().insert(program)
    }

    suspend fun updateWeightLossProgram(program: WeightLossProgramEntity) {
        database.weightLossProgramDao().update(program)
    }

    suspend fun deleteWeightLossProgram(id: Long) {
        database.weightLossProgramDao().delete(id)
    }

    suspend fun getWeightLossProgramById(id: Long): WeightLossProgramEntity? {
        return database.weightLossProgramDao().getById(id)
    }

    suspend fun getActiveWeightLossProgram(): WeightLossProgramEntity? {
        return database.weightLossProgramDao().getActiveProgram()
    }

    fun getActiveWeightLossProgramFlow(): Flow<WeightLossProgramEntity?> {
        return database.weightLossProgramDao().getActiveProgramFlow()
    }

    suspend fun getAllWeightLossPrograms(): List<WeightLossProgramEntity> {
        return database.weightLossProgramDao().getAll()
    }

    fun getAllWeightLossProgramsFlow(): Flow<List<WeightLossProgramEntity>> {
        return database.weightLossProgramDao().getAllFlow()
    }

    suspend fun deactivateAllPrograms() {
        database.weightLossProgramDao().deactivateAllPrograms()
    }

    /**
     * Create a comprehensive weight loss program
     */
    suspend fun createWeightLossProgram(
        currentWeight: Float,
        targetWeight: Float,
        heightCm: Float,
        ageYears: Int,
        isMale: Boolean,
        timeframeWeeks: Int,
        activityLevel: ActivityLevel,
    ): WeightLossProgram {
        val weightLossGoal = currentWeight - targetWeight
        val weeklyWeightLossGoal = weightLossGoal / timeframeWeeks

        // Ensure safe weight loss rate (max 1kg per week)
        val safeWeeklyGoal = minOf(weeklyWeightLossGoal, 1.0f)

        val bmr = BMICalculator.calculateBMR(currentWeight, heightCm, ageYears, isMale)
        val dailyCalorieTarget = BMICalculator.calculateDailyCalorieTarget(bmr, activityLevel, safeWeeklyGoal)
        val macroTargets = BMICalculator.calculateMacroTargets(dailyCalorieTarget)

        // Calculate milestones (every 2.5kg or 25% of goal)
        val milestoneInterval = maxOf(2.5f, weightLossGoal * 0.25f)
        val milestones = generateWeightLossMilestones(currentWeight, targetWeight, milestoneInterval, timeframeWeeks)

        // Recommended exercise based on activity level
        val exerciseMinutes =
            when (activityLevel) {
                ActivityLevel.SEDENTARY -> 30
                ActivityLevel.LIGHTLY_ACTIVE -> 45
                ActivityLevel.MODERATELY_ACTIVE -> 60
                ActivityLevel.VERY_ACTIVE -> 75
                ActivityLevel.EXTRA_ACTIVE -> 90
            }

        return WeightLossProgram(
            dailyCalorieTarget = dailyCalorieTarget,
            macroTargets = macroTargets,
            weeklyWeightLossGoal = safeWeeklyGoal,
            recommendedExerciseMinutes = exerciseMinutes,
            milestones = milestones,
        )
    }

    private fun generateWeightLossMilestones(
        currentWeight: Float,
        targetWeight: Float,
        interval: Float,
        timeframeWeeks: Int,
    ): List<WeightLossMilestone> {
        val milestones = mutableListOf<WeightLossMilestone>()
        val totalWeightLoss = currentWeight - targetWeight
        var weight = currentWeight
        val startDate = LocalDate.now()

        var milestoneCount = 1
        while (weight > targetWeight + interval) {
            weight -= interval
            val weeksToMilestone = (milestoneCount * timeframeWeeks * interval / totalWeightLoss).toInt()
            val estimatedDate = startDate.plusWeeks(weeksToMilestone.toLong())

            milestones.add(
                WeightLossMilestone(
                    targetWeight = weight,
                    estimatedDate = estimatedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    description = "Meilenstein $milestoneCount: ${weight.toInt()}kg erreichen",
                ),
            )
            milestoneCount++
        }

        // Add final target
        val finalDate = startDate.plusWeeks(timeframeWeeks.toLong())
        milestones.add(
            WeightLossMilestone(
                targetWeight = targetWeight,
                estimatedDate = finalDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                description = "Zielgewicht erreicht: ${targetWeight.toInt()}kg",
            ),
        )

        return milestones
    }

    // Behavioral Check-ins Management

    suspend fun saveBehavioralCheckIn(checkIn: BehavioralCheckInEntity): Long {
        return database.behavioralCheckInDao().insert(checkIn)
    }

    suspend fun updateBehavioralCheckIn(checkIn: BehavioralCheckInEntity) {
        database.behavioralCheckInDao().update(checkIn)
    }

    suspend fun deleteBehavioralCheckIn(id: Long) {
        database.behavioralCheckInDao().delete(id)
    }

    suspend fun getBehavioralCheckInById(id: Long): BehavioralCheckInEntity? {
        return database.behavioralCheckInDao().getById(id)
    }

    suspend fun getAllBehavioralCheckIns(): List<BehavioralCheckInEntity> {
        return database.behavioralCheckInDao().getAll()
    }

    fun getAllBehavioralCheckInsFlow(): Flow<List<BehavioralCheckInEntity>> {
        return database.behavioralCheckInDao().getAllFlow()
    }

    suspend fun getRecentBehavioralCheckIns(limit: Int): List<BehavioralCheckInEntity> {
        return database.behavioralCheckInDao().getRecent(limit)
    }

    suspend fun getBehavioralCheckInsByDateRange(
        startTimestamp: Long,
        endTimestamp: Long,
    ): List<BehavioralCheckInEntity> {
        return database.behavioralCheckInDao().getByDateRange(startTimestamp, endTimestamp)
    }

    // Progress Photos Management

    suspend fun saveProgressPhoto(photo: ProgressPhotoEntity): Long {
        return database.progressPhotoDao().insert(photo)
    }

    suspend fun updateProgressPhoto(photo: ProgressPhotoEntity) {
        database.progressPhotoDao().update(photo)
    }

    suspend fun deleteProgressPhoto(id: Long) {
        database.progressPhotoDao().delete(id)
    }

    suspend fun getProgressPhotoById(id: Long): ProgressPhotoEntity? {
        return database.progressPhotoDao().getById(id)
    }

    suspend fun getAllProgressPhotos(): List<ProgressPhotoEntity> {
        return database.progressPhotoDao().getAll()
    }

    fun getAllProgressPhotosFlow(): Flow<List<ProgressPhotoEntity>> {
        return database.progressPhotoDao().getAllFlow()
    }

    suspend fun getRecentProgressPhotos(limit: Int): List<ProgressPhotoEntity> {
        return database.progressPhotoDao().getRecent(limit)
    }

    suspend fun getProgressPhotosByDateRange(
        startTimestamp: Long,
        endTimestamp: Long,
    ): List<ProgressPhotoEntity> {
        return database.progressPhotoDao().getByDateRange(startTimestamp, endTimestamp)
    }

    // Analytics methods for Enhanced Analytics Dashboard
    suspend fun getWeightHistoryForPeriod(days: Int): List<BMIHistoryEntity> {
        return getAllBMIHistory().takeLast(days)
    }

    fun weightHistoryFlow(days: Int): Flow<List<BMIHistoryEntity>> =
        flow {
            val history = getWeightHistoryForPeriod(days)
            emit(history)
        }

    fun weightTrendFlow(): Flow<List<BMIHistoryEntity>> {
        return getAllBMIHistoryFlow()
    }
}

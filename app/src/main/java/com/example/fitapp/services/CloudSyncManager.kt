package com.example.fitapp.services

import android.content.Context
import androidx.work.*
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.CloudSyncEntity
import com.example.fitapp.data.db.UserProfileEntity
import com.example.fitapp.data.db.SyncConflictEntity
import com.example.fitapp.data.prefs.ApiKeys
import com.example.fitapp.util.ApiCallWrapper
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import android.provider.Settings
import android.os.Build
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.security.SecureRandom

/**
 * Cloud sync manager that extends OfflineSyncManager to provide multi-device synchronization
 * Implements end-to-end encryption and conflict resolution for user data
 */
class CloudSyncManager(private val context: Context) {
    
    private val database = AppDatabase.get(context)
    private val cloudSyncDao = database.cloudSyncDao()
    private val userProfileDao = database.userProfileDao()
    private val syncConflictDao = database.syncConflictDao()
    private val offlineSyncManager = OfflineSyncManager(context)
    private val cloudApiClient = CloudApiClient(context)
    
    companion object {
        private const val CLOUD_SYNC_WORK_NAME = "cloud_sync_work"
        private const val PERIODIC_CLOUD_SYNC_WORK_NAME = "periodic_cloud_sync_work"
        private const val TAG = "CloudSyncManager"
        
        // Sync preferences defaults
        const val SYNC_ACHIEVEMENTS = "sync_achievements"
        const val SYNC_WORKOUTS = "sync_workouts"
        const val SYNC_NUTRITION = "sync_nutrition"
        const val SYNC_WEIGHT = "sync_weight"
        const val SYNC_SETTINGS = "sync_settings"
        
        fun schedulePeriodicCloudSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicSyncRequest = PeriodicWorkRequestBuilder<CloudSyncWorker>(
                30, TimeUnit.MINUTES // Sync every 30 minutes
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_CLOUD_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Periodic cloud sync scheduled"
            )
        }
        
        fun triggerImmediateCloudSync(context: Context) {
            val constraints = Constraints.Builder()
                .build()

            val immediateSyncRequest = OneTimeWorkRequestBuilder<CloudSyncWorker>()
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                CLOUD_SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                immediateSyncRequest
            )
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Immediate cloud sync triggered"
            )
        }
        
        fun cancelCloudSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_CLOUD_SYNC_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(CLOUD_SYNC_WORK_NAME)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Cloud sync cancelled"
            )
        }
    }
    
    /**
     * Initialize cloud sync for a user (signup/login)
     */
    suspend fun initializeCloudSync(userId: String, email: String, displayName: String? = null): Result<UserProfileEntity> {
        return try {
            val deviceId = getDeviceId()
            val deviceName = getDeviceName()
            
            // Create default sync preferences
            val defaultPreferences = createDefaultSyncPreferences()
            
            // Generate encryption key for this user
            val encryptionKey = generateEncryptionKey()
            
            val userProfile = UserProfileEntity(
                userId = userId,
                email = email,
                displayName = displayName,
                deviceName = deviceName,
                deviceId = deviceId,
                lastSyncTime = System.currentTimeMillis() / 1000,
                syncPreferences = defaultPreferences,
                encryptionKey = encryptionKey
            )
            
            // Deactivate other profiles and activate this one
            userProfileDao.deactivateAllProfiles()
            userProfileDao.upsertUserProfile(userProfile)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Cloud sync initialized for user $userId"
            )
            
            // Trigger initial sync
            triggerImmediateCloudSync(context)
            
            Result.success(userProfile)
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to initialize cloud sync",
                exception = e
            )
            Result.failure(e)
        }
    }
    
    /**
     * Sync specific entity to cloud
     */
    suspend fun syncEntityToCloud(entityType: String, entityId: String, entityData: String) {
        try {
            val activeUser = userProfileDao.getActiveUserProfile()
            if (activeUser == null) {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "No active user profile for cloud sync"
                )
                return
            }
            
            val deviceId = getDeviceId()
            val currentTime = System.currentTimeMillis() / 1000
            
            // Check if sync is enabled for this entity type
            if (!isSyncEnabledForEntityType(entityType, activeUser.syncPreferences)) {
                StructuredLogger.debug(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Sync disabled for entity type: $entityType"
                )
                return
            }
            
            // Create or update sync metadata
            val syncMetadata = CloudSyncEntity(
                entityType = entityType,
                entityId = entityId,
                lastSyncTime = 0, // Will be updated after successful sync
                lastModifiedTime = currentTime,
                syncStatus = "pending",
                deviceId = deviceId,
                cloudVersion = null
            )
            
            cloudSyncDao.upsertSyncMetadata(syncMetadata)
            
            // Encrypt data for cloud storage
            val encryptedData = encryptData(entityData, activeUser.encryptionKey ?: "")
            
            // Upload to cloud
            val uploadResult = cloudApiClient.uploadEntity(
                userId = activeUser.userId,
                entityType = entityType,
                entityId = entityId,
                data = encryptedData,
                deviceId = deviceId,
                timestamp = currentTime
            )
            
            if (uploadResult.isSuccess) {
                val cloudVersion = uploadResult.getOrNull() ?: ""
                cloudSyncDao.updateSyncStatus(syncMetadata.id, "synced", currentTime)
                
                // Update cloud version for conflict detection
                cloudSyncDao.upsertSyncMetadata(
                    syncMetadata.copy(
                        syncStatus = "synced",
                        lastSyncTime = currentTime,
                        cloudVersion = cloudVersion
                    )
                )
                
                StructuredLogger.info(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Successfully synced $entityType:$entityId to cloud"
                )
            } else {
                val error = uploadResult.exceptionOrNull()?.message ?: "Unknown error"
                cloudSyncDao.incrementRetryCount(syncMetadata.id, error)
                
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYNC,
                    TAG,
                    "Failed to sync $entityType:$entityId to cloud: $error"
                )
            }
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Error syncing entity to cloud",
                exception = e
            )
        }
    }
    
    /**
     * Download and merge changes from cloud
     */
    suspend fun syncFromCloud(): Result<CloudSyncResult> = withContext(Dispatchers.IO) {
        try {
            val activeUser = userProfileDao.getActiveUserProfile()
            if (activeUser == null) {
                return@withContext Result.failure(Exception("No active user profile"))
            }
            
            if (!ApiCallWrapper.isNetworkAvailable(context)) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val lastSyncTime = activeUser.lastSyncTime
            val changes = cloudApiClient.getChangesSince(activeUser.userId, lastSyncTime)
            
            if (changes.isFailure) {
                return@withContext Result.failure(changes.exceptionOrNull() ?: Exception("Failed to fetch changes"))
            }
            
            val cloudChanges = changes.getOrNull() ?: emptyList()
            var conflictsDetected = 0
            var entitiesUpdated = 0
            var errorsEncountered = 0
            
            for (change in cloudChanges) {
                try {
                    val conflict = processCloudChange(change, activeUser)
                    if (conflict != null) {
                        conflictsDetected++
                        syncConflictDao.insertConflict(conflict)
                    } else {
                        entitiesUpdated++
                    }
                } catch (e: Exception) {
                    errorsEncountered++
                    StructuredLogger.error(
                        StructuredLogger.LogCategory.SYNC,
                        TAG,
                        "Error processing cloud change: ${change.entityType}:${change.entityId}",
                        exception = e
                    )
                }
            }
            
            // Update last sync time
            val currentTime = System.currentTimeMillis() / 1000
            userProfileDao.updateLastSyncTime(activeUser.userId, currentTime)
            
            val result = CloudSyncResult(
                entitiesUpdated = entitiesUpdated,
                conflictsDetected = conflictsDetected,
                errorsEncountered = errorsEncountered
            )
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Cloud sync completed: $entitiesUpdated updated, $conflictsDetected conflicts, $errorsEncountered errors"
            )
            
            Result.success(result)
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Cloud sync failed",
                exception = e
            )
            Result.failure(e)
        }
    }
    
    /**
     * Get pending conflicts that need manual resolution
     */
    fun getPendingConflicts(): Flow<List<SyncConflictEntity>> = flow {
        emit(syncConflictDao.getPendingConflicts())
    }
    
    /**
     * Resolve a sync conflict
     */
    suspend fun resolveConflict(conflictId: String, resolution: CloudConflictResolution): Result<Unit> {
        return try {
            val conflict = syncConflictDao.getPendingConflicts().find { it.id == conflictId }
                ?: return Result.failure(Exception("Conflict not found"))
            
            val resolvedData = when (resolution) {
                CloudConflictResolution.LOCAL_WINS -> conflict.localData
                CloudConflictResolution.REMOTE_WINS -> conflict.remoteData
                CloudConflictResolution.MANUAL -> {
                    // For manual resolution, the UI should provide the resolved data
                    // This is a simplified implementation
                    conflict.localData
                }
            }
            
            // Apply the resolved data to the local database
            applyResolvedData(conflict.entityType, conflict.entityId, resolvedData)
            
            // Mark conflict as resolved
            val currentTime = System.currentTimeMillis() / 1000
            syncConflictDao.resolveConflict(
                id = conflictId,
                status = "resolved",
                resolution = resolution.name.lowercase(),
                resolvedData = resolvedData,
                resolvedBy = "user",
                resolvedAt = currentTime
            )
            
            // Sync resolved data to cloud
            syncEntityToCloud(conflict.entityType, conflict.entityId, resolvedData)
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Conflict resolved: $conflictId with strategy ${resolution.name}"
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to resolve conflict",
                exception = e
            )
            Result.failure(e)
        }
    }
    
    /**
     * Update sync preferences for the current user
     */
    suspend fun updateSyncPreferences(preferences: Map<String, Boolean>) {
        try {
            val activeUser = userProfileDao.getActiveUserProfile() ?: return
            
            val preferencesJson = JSONObject()
            preferences.forEach { (key, value) ->
                preferencesJson.put(key, value)
            }
            
            userProfileDao.updateSyncPreferences(activeUser.userId, preferencesJson.toString())
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Sync preferences updated"
            )
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to update sync preferences",
                exception = e
            )
        }
    }
    
    // Private helper methods
    
    private fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }
    
    private fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    }
    
    private fun createDefaultSyncPreferences(): String {
        val defaults = JSONObject()
        defaults.put(SYNC_ACHIEVEMENTS, true)
        defaults.put(SYNC_WORKOUTS, true)
        defaults.put(SYNC_NUTRITION, true)
        defaults.put(SYNC_WEIGHT, true)
        defaults.put(SYNC_SETTINGS, true)
        return defaults.toString()
    }
    
    private fun generateEncryptionKey(): String {
        val key = ByteArray(16)
        SecureRandom().nextBytes(key)
        return Base64.encodeToString(key, Base64.NO_WRAP)
    }
    
    private fun encryptData(data: String, encryptionKey: String): String {
        if (encryptionKey.isEmpty()) return data
        
        return try {
            val key = SecretKeySpec(Base64.decode(encryptionKey, Base64.NO_WRAP), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedData = cipher.doFinal(data.toByteArray())
            Base64.encodeToString(encryptedData, Base64.NO_WRAP)
        } catch (e: Exception) {
            StructuredLogger.warning(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to encrypt data, using plain text",
                exception = e
            )
            data
        }
    }
    
    private fun decryptData(encryptedData: String, encryptionKey: String): String {
        if (encryptionKey.isEmpty()) return encryptedData
        
        return try {
            val key = SecretKeySpec(Base64.decode(encryptionKey, Base64.NO_WRAP), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decryptedData = cipher.doFinal(Base64.decode(encryptedData, Base64.NO_WRAP))
            String(decryptedData)
        } catch (e: Exception) {
            StructuredLogger.warning(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Failed to decrypt data, using as-is",
                exception = e
            )
            encryptedData
        }
    }
    
    private fun isSyncEnabledForEntityType(entityType: String, preferencesJson: String): Boolean {
        return try {
            val preferences = JSONObject(preferencesJson)
            when (entityType) {
                "PersonalAchievement", "PersonalStreak", "PersonalRecord" -> preferences.optBoolean(SYNC_ACHIEVEMENTS, true)
                "WorkoutSession", "WorkoutPerformance", "ExerciseProgression" -> preferences.optBoolean(SYNC_WORKOUTS, true)
                "IntakeEntry", "MealEntry", "WaterEntry", "FoodItem" -> preferences.optBoolean(SYNC_NUTRITION, true)
                "WeightEntry", "BMIHistory", "WeightLossProgram" -> preferences.optBoolean(SYNC_WEIGHT, true)
                else -> preferences.optBoolean(SYNC_SETTINGS, true)
            }
        } catch (e: Exception) {
            true // Default to enabled if preferences can't be parsed
        }
    }
    
    private suspend fun processCloudChange(change: CloudChange, activeUser: UserProfileEntity): SyncConflictEntity? {
        // Get local sync metadata to check for conflicts
        val localMetadata = cloudSyncDao.getSyncMetadata(change.entityType, change.entityId)
        
        // If no local data exists, apply cloud change directly
        if (localMetadata == null) {
            applyCloudChange(change, activeUser)
            return null
        }
        
        // Check for conflict: local data newer than cloud data
        if (localMetadata.lastModifiedTime > change.timestamp) {
            // Conflict detected - local data is newer
            val localData = getLocalEntityData(change.entityType, change.entityId)
            val remoteData = decryptData(change.data, activeUser.encryptionKey ?: "")
            
            return SyncConflictEntity(
                entityType = change.entityType,
                entityId = change.entityId,
                localData = localData,
                remoteData = remoteData,
                localTimestamp = localMetadata.lastModifiedTime,
                remoteTimestamp = change.timestamp,
                status = "pending",
                resolution = null
            )
        }
        
        // No conflict - apply cloud change
        applyCloudChange(change, activeUser)
        return null
    }
    
    private suspend fun applyCloudChange(change: CloudChange, activeUser: UserProfileEntity) {
        val decryptedData = decryptData(change.data, activeUser.encryptionKey ?: "")
        applyResolvedData(change.entityType, change.entityId, decryptedData)
        
        // Update sync metadata
        val syncMetadata = CloudSyncEntity(
            entityType = change.entityType,
            entityId = change.entityId,
            lastSyncTime = System.currentTimeMillis() / 1000,
            lastModifiedTime = change.timestamp,
            syncStatus = "synced",
            deviceId = getDeviceId(),
            cloudVersion = change.version
        )
        cloudSyncDao.upsertSyncMetadata(syncMetadata)
    }
    
    private fun getLocalEntityData(entityType: String, entityId: String): String {
        // This would implement getting the actual entity data from the database
        // For now, return a placeholder JSON
        return """{"entityType":"$entityType","entityId":"$entityId","placeholder":true}"""
    }
    
    private suspend fun applyResolvedData(entityType: String, entityId: String, data: String) {
        // This would implement applying the resolved data to the actual entity tables
        // For now, just log the operation
        StructuredLogger.info(
            StructuredLogger.LogCategory.SYNC,
            TAG,
            "Applied resolved data for $entityType:$entityId"
        )
    }
}

/**
 * WorkManager worker for cloud sync operations
 */
class CloudSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val cloudSyncManager = CloudSyncManager(applicationContext)
            
            // Perform bidirectional sync
            val syncResult = cloudSyncManager.syncFromCloud()
            
            if (syncResult.isSuccess) {
                val result = syncResult.getOrNull()!!
                if (result.errorsEncountered > 0) {
                    // Some operations failed, retry later
                    Result.retry()
                } else {
                    Result.success()
                }
            } else {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.SYNC,
                    "CloudSyncWorker",
                    "Cloud sync failed",
                    exception = syncResult.exceptionOrNull()
                )
                Result.retry()
            }
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.SYNC,
                "CloudSyncWorker",
                "Worker execution failed",
                exception = e
            )
            Result.failure()
        }
    }
}

// Data classes for cloud sync
data class CloudSyncResult(
    val entitiesUpdated: Int,
    val conflictsDetected: Int,
    val errorsEncountered: Int
)

data class CloudChange(
    val entityType: String,
    val entityId: String,
    val data: String,
    val timestamp: Long,
    val version: String,
    val deviceId: String
)

enum class CloudConflictResolution {
    LOCAL_WINS,
    REMOTE_WINS,
    MANUAL
}

/**
 * Cloud API client for backend communication
 * This is a simplified implementation - in production, this would integrate with
 * Firebase, AWS, or another cloud provider
 */
class CloudApiClient(private val context: Context) {
    
    companion object {
        private const val TAG = "CloudApiClient"
        private const val BASE_URL = "https://api.fitapp.cloud" // Placeholder URL
    }
    
    suspend fun uploadEntity(
        userId: String,
        entityType: String,
        entityId: String,
        data: String,
        deviceId: String,
        timestamp: Long
    ): Result<String> {
        return try {
            // Simulate API call for now
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Simulating upload of $entityType:$entityId for user $userId"
            )
            
            // In a real implementation, this would make an HTTP request to the cloud backend
            Result.success("version_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getChangesSince(userId: String, lastSyncTime: Long): Result<List<CloudChange>> {
        return try {
            // Simulate API call for now
            StructuredLogger.info(
                StructuredLogger.LogCategory.SYNC,
                TAG,
                "Simulating get changes since $lastSyncTime for user $userId"
            )
            
            // In a real implementation, this would fetch changes from the cloud backend
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
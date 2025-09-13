package com.example.fitapp.services

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.SyncOperationEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class SyncQueueTest {

    private lateinit var database: AppDatabase
    private lateinit var syncQueue: SyncQueue

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        syncQueue = SyncQueue(ApplicationProvider.getApplicationContext())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun `addOperation should persist sync operation`() = runTest {
        // Given
        val operation = SyncOperation(
            id = "test-op-1",
            type = SyncOperationType.NUTRITION_ENTRY,
            data = mapOf("mealId" to "123", "calories" to "500"),
            timestamp = System.currentTimeMillis() / 1000
        )

        // When
        syncQueue.addOperation(operation)

        // Then
        val dao = database.syncOperationDao()
        val storedEntity = dao.getById("test-op-1")
        assertNotNull(storedEntity)
        assertEquals("NUTRITION_ENTRY", storedEntity?.operationType)
        assertEquals("pending", storedEntity?.status)
    }

    @Test
    fun `getPendingOperations should return operations ready for execution`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis() / 1000
        val readyOperation = SyncOperationEntity(
            id = "ready-op",
            operationType = "WORKOUT_COMPLETION",
            operationData = "workoutId=456",
            timestamp = currentTime - 100,
            status = "pending",
            createdAt = currentTime - 100
        )
        
        val futureOperation = SyncOperationEntity(
            id = "future-op", 
            operationType = "ACHIEVEMENT_UPDATE",
            operationData = "achievementId=789",
            timestamp = currentTime - 50,
            status = "failed",
            nextRetryAt = currentTime + 3600, // 1 hour in future
            createdAt = currentTime - 50
        )

        database.syncOperationDao().insert(readyOperation)
        database.syncOperationDao().insert(futureOperation)

        // When
        val pendingOps = syncQueue.getPendingOperations()

        // Then
        assertEquals(1, pendingOps.size)
        assertEquals("ready-op", pendingOps[0].id)
        assertEquals(SyncOperationType.WORKOUT_COMPLETION, pendingOps[0].type)
    }

    @Test
    fun `incrementRetryCount should increase retry count and set next retry time`() = runTest {
        // Given
        val operation = SyncOperationEntity(
            id = "retry-op",
            operationType = "NUTRITION_ENTRY",
            operationData = "mealId=999",
            timestamp = System.currentTimeMillis() / 1000,
            retryCount = 1,
            status = "failed",
            createdAt = System.currentTimeMillis() / 1000
        )
        
        database.syncOperationDao().insert(operation)

        // When
        syncQueue.incrementRetryCount("retry-op")

        // Then
        val updatedEntity = database.syncOperationDao().getById("retry-op")
        assertNotNull(updatedEntity)
        assertEquals(2, updatedEntity?.retryCount)
        assertEquals("failed", updatedEntity?.status)
        assertNotNull(updatedEntity?.nextRetryAt)
        assertTrue((updatedEntity?.nextRetryAt ?: 0) > System.currentTimeMillis() / 1000)
    }

    @Test
    fun `removeOperation should mark operation as completed`() = runTest {
        // Given
        val operation = SyncOperationEntity(
            id = "complete-op",
            operationType = "WEIGHT_ENTRY",
            operationData = "weight=75.5",
            timestamp = System.currentTimeMillis() / 1000,
            status = "processing",
            createdAt = System.currentTimeMillis() / 1000
        )
        
        database.syncOperationDao().insert(operation)

        // When
        syncQueue.removeOperation("complete-op")

        // Then
        val updatedEntity = database.syncOperationDao().getById("complete-op")
        assertNotNull(updatedEntity)
        assertEquals("completed", updatedEntity?.status)
        assertNotNull(updatedEntity?.completedAt)
    }

    @Test
    fun `markOperationPermanentlyFailed should set failed status with error message`() = runTest {
        // Given
        val operation = SyncOperationEntity(
            id = "failed-op",
            operationType = "CLOUD_SYNC_UP",
            operationData = "entityType=WorkoutSession",
            timestamp = System.currentTimeMillis() / 1000,
            status = "processing",
            createdAt = System.currentTimeMillis() / 1000
        )
        
        database.syncOperationDao().insert(operation)
        val errorMessage = "Network unreachable after 3 retries"

        // When
        syncQueue.markOperationPermanentlyFailed("failed-op", errorMessage)

        // Then
        val updatedEntity = database.syncOperationDao().getById("failed-op")
        assertNotNull(updatedEntity)
        assertEquals("failed", updatedEntity?.status)
        assertEquals(errorMessage, updatedEntity?.errorMessage)
    }
}
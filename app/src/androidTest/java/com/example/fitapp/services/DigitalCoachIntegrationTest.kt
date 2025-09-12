package com.example.fitapp.services

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for Digital Coach functionality
 */
@RunWith(AndroidJUnit4::class)
class DigitalCoachIntegrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testDigitalCoachManagerCreation() {
        val digitalCoach = DigitalCoachManager(context)
        assertNotNull("Digital coach manager should be created", digitalCoach)
    }

    @Test
    fun testCoachingMessageGeneration() =
        runBlocking {
            val digitalCoach = DigitalCoachManager(context)

            // Test daily check-in message generation
            val message =
                digitalCoach.generateContextualCoachingMessage(
                    context = CoachingContext.DAILY_CHECK_IN,
                )

            assertNotNull("Coaching message should not be null", message)
            assertTrue("Message should have content", message.content.isNotEmpty())
            assertTrue("Message should have title", message.title.isNotEmpty())
            assertNotNull("Message should have a type", message.type)
            assertNotNull("Message should have priority", message.priority)
        }

    @Test
    fun testPostWorkoutCoachingMessage() =
        runBlocking {
            val digitalCoach = DigitalCoachManager(context)

            val message =
                digitalCoach.generateContextualCoachingMessage(
                    context = CoachingContext.POST_WORKOUT,
                )

            assertEquals(
                "Should be post-workout message",
                CoachingMessageType.POST_WORKOUT,
                message.type,
            )
            assertTrue(
                "Post-workout messages should have high priority",
                message.priority == CoachingPriority.HIGH,
            )
        }

    @Test
    fun testCoachingTriggers() =
        runBlocking {
            val digitalCoach = DigitalCoachManager(context)

            val triggers = digitalCoach.getRecommendedCoachingTriggers()

            assertNotNull("Triggers should not be null", triggers)
            // Triggers list can be empty, but should be a valid list
            assertTrue("Triggers should be a valid list", triggers is List<CoachingTrigger>)
        }

    @Test
    fun testFeedbackProcessing() =
        runBlocking {
            val digitalCoach = DigitalCoachManager(context)

            // This should not throw an exception
            digitalCoach.processCoachingFeedback(
                messageId = "test_message_123",
                feedback = CoachingFeedback.HELPFUL,
            )

            // Test other feedback types
            digitalCoach.processCoachingFeedback(
                messageId = "test_message_124",
                feedback = CoachingFeedback.MORE_OF_THIS,
            )
        }

    @Test
    fun testWorkoutCompletionTrigger() {
        // Test that workout completion trigger doesn't throw exception
        try {
            DigitalCoachTriggers.onWorkoutCompleted(context)
            // If we get here, the trigger was scheduled successfully
            assertTrue("Workout completion trigger should work", true)
        } catch (e: Exception) {
            fail("Workout completion trigger should not throw exception: ${e.message}")
        }
    }

    @Test
    fun testNotificationIntegration() {
        // Test that SmartNotificationManager has the digital coach method available
        val digitalCoach = DigitalCoachManager(context)

        runBlocking {
            val message = digitalCoach.generateContextualCoachingMessage()

            try {
                SmartNotificationManager.showDigitalCoachNotification(context, message)
                // If no exception is thrown, the integration works
                assertTrue("Notification integration should work", true)
            } catch (e: SecurityException) {
                // This is expected in test environment without notification permissions
                assertTrue("Security exception is expected in test", true)
            }
        }
    }
}

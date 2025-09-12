package com.example.fitapp.data.prefs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Simple test to verify Proto DataStore implementation compiles and works
 */
class ProtoDataStoreBasicTest {
    @Test
    fun `test proto message creation`() =
        runTest {
            // Test that we can create UserPreferencesProto instances
            val proto =
                UserPreferencesProto.newBuilder()
                    .addSelectedEquipment("Dumbbell")
                    .addSelectedEquipment("Barbell")
                    .setNotificationsEnabled(true)
                    .setDefaultRestTimeSeconds(60)
                    .setUserName("Test User")
                    .setAge(25)
                    .setDailyCalorieGoal(2000)
                    .setThemeMode("dark")
                    .build()

            // Verify the proto was created correctly
            assertEquals(2, proto.selectedEquipmentCount)
            assertEquals("Dumbbell", proto.getSelectedEquipment(0))
            assertEquals("Barbell", proto.getSelectedEquipment(1))
            assertTrue(proto.notificationsEnabled)
            assertEquals(60, proto.defaultRestTimeSeconds)
            assertEquals("Test User", proto.userName)
            assertEquals(25, proto.age)
            assertEquals(2000, proto.dailyCalorieGoal)
            assertEquals("dark", proto.themeMode)
        }

    @Test
    fun `test proto serialization`() =
        runTest {
            // Test that proto can be serialized and deserialized
            val original =
                UserPreferencesProto.newBuilder()
                    .addSelectedEquipment("Kettlebell")
                    .setNotificationsEnabled(false)
                    .setUserName("Serialization Test")
                    .build()

            // Serialize to bytes
            val bytes = original.toByteArray()
            assertNotNull(bytes)
            assertTrue(bytes.isNotEmpty())

            // Deserialize from bytes
            val deserialized = UserPreferencesProto.parseFrom(bytes)

            // Verify deserialized proto matches original
            assertEquals(original.selectedEquipmentCount, deserialized.selectedEquipmentCount)
            assertEquals(original.getSelectedEquipment(0), deserialized.getSelectedEquipment(0))
            assertEquals(original.notificationsEnabled, deserialized.notificationsEnabled)
            assertEquals(original.userName, deserialized.userName)
        }

    @Test
    fun `test default instance`() =
        runTest {
            // Test that default instance works correctly
            val defaultProto = UserPreferencesProto.getDefaultInstance()

            // Verify default values
            assertEquals(0, defaultProto.selectedEquipmentCount)
            assertFalse(defaultProto.notificationsEnabled)
            assertEquals(0, defaultProto.defaultRestTimeSeconds)
            assertEquals("", defaultProto.userName)
            assertEquals(0, defaultProto.age)
            assertEquals(0, defaultProto.dailyCalorieGoal)
            assertEquals("", defaultProto.themeMode)
        }

    @Test
    fun `test serializer interface`() =
        runTest {
            // Test that our serializer has the correct default value
            val serializer = UserPreferencesSerializer
            val defaultValue = serializer.defaultValue

            assertNotNull(defaultValue)
            assertEquals(UserPreferencesProto.getDefaultInstance(), defaultValue)
        }
}

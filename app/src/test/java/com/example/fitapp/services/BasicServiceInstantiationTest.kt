package com.example.fitapp.services

import android.content.Context
import com.example.fitapp.data.db.AppDatabase
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Basic test to verify core services can be instantiated
 * This establishes a baseline for test coverage
 */
class BasicServiceInstantiationTest {
    @Test
    fun `should instantiate MacroCalculator`() {
        val context = mock(Context::class.java)
        val macroCalculator = MacroCalculator(context)
        assertNotNull("MacroCalculator should be instantiable", macroCalculator)
    }

    @Test
    fun `should instantiate SmartRestTimer`() {
        val context = mock(Context::class.java)
        val smartRestTimer = SmartRestTimer(context)
        assertNotNull("SmartRestTimer should be instantiable", smartRestTimer)
    }

    @Test
    fun `should instantiate CookingModeManager`() {
        val database = mock(AppDatabase::class.java)
        val cookingModeManager = CookingModeManager(database)
        assertNotNull("CookingModeManager should be instantiable", cookingModeManager)
    }
}

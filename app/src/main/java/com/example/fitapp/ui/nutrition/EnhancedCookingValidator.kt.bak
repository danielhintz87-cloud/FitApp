package com.example.fitapp.ui.nutrition

import android.content.Context
import android.util.Log
import com.example.fitapp.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Validator for Enhanced Cooking Features Integration
 * 
 * Ensures all components of the enhanced cooking system work together
 * and validates the system state for optimal user experience.
 */
object EnhancedCookingValidator {
    
    private const val TAG = "EnhancedCookingValidator"
    
    /**
     * Validate that all enhanced cooking components are properly integrated
     */
    fun validateIntegration(context: Context): ValidationResult {
        val results = mutableListOf<ValidationCheck>()
        
        try {
            // 1. Database Integration
            results.add(validateDatabaseIntegration(context))
            
            // 2. AI Integration
            results.add(validateAIIntegration())
            
            // 3. Timer System
            results.add(validateTimerSystem(context))
            
            // 4. Permissions
            results.add(validatePermissions(context))
            
            val allPassed = results.all { it.passed }
            val summary = "Enhanced Cooking Features: ${results.count { it.passed }}/${results.size} checks passed"
            
            Log.i(TAG, summary)
            
            return ValidationResult(
                overallSuccess = allPassed,
                summary = summary,
                checks = results
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Validation failed with exception", e)
            return ValidationResult(
                overallSuccess = false,
                summary = "Validation failed: ${e.message}",
                checks = results
            )
        }
    }
    
    private fun validateDatabaseIntegration(context: Context): ValidationCheck {
        return try {
            val db = AppDatabase.get(context)
            
            // Check that DAOs are accessible
            val cookingSessionDao = db.cookingSessionDao()
            val cookingTimerDao = db.cookingTimerDao()
            val savedRecipeDao = db.savedRecipeDao()
            
            // Verify database version
            val version = db.openHelper.readableDatabase.version
            val expectedVersion = 11
            
            if (version >= expectedVersion) {
                ValidationCheck(
                    name = "Database Integration",
                    passed = true,
                    message = "Database v$version ready, all DAOs accessible"
                )
            } else {
                ValidationCheck(
                    name = "Database Integration",
                    passed = false,
                    message = "Database version $version < required $expectedVersion"
                )
            }
        } catch (e: Exception) {
            ValidationCheck(
                name = "Database Integration",
                passed = false,
                message = "Database validation failed: ${e.message}"
            )
        }
    }
    
    private fun validateAIIntegration(): ValidationCheck {
        return try {
            // Check that AI cooking classes exist and are accessible
            val cookingRequestClass = Class.forName("com.example.fitapp.ai.CookingAssistanceRequest")
            val cookingAssistanceClass = Class.forName("com.example.fitapp.ai.CookingAssistance")
            
            // Verify WeightLossAI extensions exist
            val weightLossAIClass = Class.forName("com.example.fitapp.ai.WeightLossAI")
            
            ValidationCheck(
                name = "AI Integration",
                passed = true,
                message = "All AI cooking assistance classes accessible"
            )
        } catch (e: Exception) {
            ValidationCheck(
                name = "AI Integration",
                passed = false,
                message = "AI integration validation failed: ${e.message}"
            )
        }
    }
    
    private fun validateTimerSystem(context: Context): ValidationCheck {
        return try {
            // Verify timer manager can be instantiated
            val timerManager = CookingTimerManager(context)
            
            // Check that timer formatting works
            val formattedTime = timerManager.formatTime(65) // Should be "1:05"
            val expectedFormat = "1:05"
            
            if (formattedTime == expectedFormat) {
                ValidationCheck(
                    name = "Timer System",
                    passed = true,
                    message = "Timer manager functional, formatting correct"
                )
            } else {
                ValidationCheck(
                    name = "Timer System",
                    passed = false,
                    message = "Timer formatting incorrect: got '$formattedTime', expected '$expectedFormat'"
                )
            }
        } catch (e: Exception) {
            ValidationCheck(
                name = "Timer System",
                passed = false,
                message = "Timer system validation failed: ${e.message}"
            )
        }
    }
    
    private fun validatePermissions(context: Context): ValidationCheck {
        return try {
            // Check WAKE_LOCK permission is declared (not necessarily granted)
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName, 
                android.content.pm.PackageManager.GET_PERMISSIONS
            )
            
            val hasWakeLockPermission = packageInfo.requestedPermissions?.contains(
                android.Manifest.permission.WAKE_LOCK
            ) == true
            
            if (hasWakeLockPermission) {
                ValidationCheck(
                    name = "Permissions",
                    passed = true,
                    message = "WAKE_LOCK permission properly declared"
                )
            } else {
                ValidationCheck(
                    name = "Permissions",
                    passed = false,
                    message = "WAKE_LOCK permission not declared in manifest"
                )
            }
        } catch (e: Exception) {
            ValidationCheck(
                name = "Permissions",
                passed = false,
                message = "Permission validation failed: ${e.message}"
            )
        }
    }
    
    /**
     * Validate system asynchronously and log results
     */
    fun validateAsync(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = validateIntegration(context)
            
            if (result.overallSuccess) {
                Log.i(TAG, "✅ Enhanced Cooking Features fully validated")
            } else {
                Log.w(TAG, "⚠️ Enhanced Cooking Features validation issues found")
                result.checks.filter { !it.passed }.forEach { check ->
                    Log.w(TAG, "❌ ${check.name}: ${check.message}")
                }
            }
        }
    }
}

/**
 * Result of validation check
 */
data class ValidationResult(
    val overallSuccess: Boolean,
    val summary: String,
    val checks: List<ValidationCheck>
)

/**
 * Individual validation check result
 */
data class ValidationCheck(
    val name: String,
    val passed: Boolean,
    val message: String
)
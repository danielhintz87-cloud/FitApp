package com.example.fitapp.navigation

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit4.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitapp.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkTest {

    @Test
    fun deepLink_fitappScheme_launchesMainActivity() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        
        // Create an intent with fitapp:// scheme
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("fitapp://dashboard")
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
            setPackage(context.packageName)
        }

        // Launch activity with the intent
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            // Verify activity is launched
            scenario.onActivity { activity ->
                assert(activity != null) { "MainActivity should be launched by deeplink" }
                assert(!activity.isFinishing) { "Activity should not be finishing" }
            }
        }
    }

    @Test
    fun deepLink_fitappNutrition_launchesMainActivity() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        
        // Create an intent with fitapp://nutrition scheme
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("fitapp://nutrition")
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
            setPackage(context.packageName)
        }

        // Launch activity with the intent
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            // Verify activity is launched
            scenario.onActivity { activity ->
                assert(activity != null) { "MainActivity should be launched by nutrition deeplink" }
                assert(!activity.isFinishing) { "Activity should not be finishing" }
            }
        }
    }

    @Test
    fun deepLink_fitappTraining_launchesMainActivity() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        
        // Create an intent with fitapp://training scheme
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("fitapp://training")
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
            setPackage(context.packageName)
        }

        // Launch activity with the intent
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            // Verify activity is launched
            scenario.onActivity { activity ->
                assert(activity != null) { "MainActivity should be launched by training deeplink" }
                assert(!activity.isFinishing) { "Activity should not be finishing" }
            }
        }
    }

    @Test
    fun deepLink_fitappAnalytics_launchesMainActivity() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        
        // Create an intent with fitapp://analytics scheme
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("fitapp://analytics")
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
            setPackage(context.packageName)
        }

        // Launch activity with the intent
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            // Verify activity is launched
            scenario.onActivity { activity ->
                assert(activity != null) { "MainActivity should be launched by analytics deeplink" }
                assert(!activity.isFinishing) { "Activity should not be finishing" }
            }
        }
    }

    @Test
    fun deepLink_fitappSettings_launchesMainActivity() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        
        // Create an intent with fitapp://settings scheme
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("fitapp://settings")
            addCategory(Intent.CATEGORY_BROWSABLE)
            addCategory(Intent.CATEGORY_DEFAULT)
            setPackage(context.packageName)
        }

        // Launch activity with the intent
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            // Verify activity is launched
            scenario.onActivity { activity ->
                assert(activity != null) { "MainActivity should be launched by settings deeplink" }
                assert(!activity.isFinishing) { "Activity should not be finishing" }
            }
        }
    }

    @Test 
    fun intent_filter_hasCorrectConfiguration() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageManager = context.packageManager
        
        // Get the MainActivity component
        val componentName = android.content.ComponentName(context.packageName, "${context.packageName}.MainActivity")
        
        // Get activity info
        val activityInfo = packageManager.getActivityInfo(componentName, android.content.pm.PackageManager.GET_META_DATA)
        
        // Verify activity is exported (required for deeplinks)
        assert(activityInfo.exported) { "MainActivity must be exported to handle deeplinks" }
    }
}
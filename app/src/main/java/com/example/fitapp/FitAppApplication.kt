package com.example.fitapp

import android.app.Application
import android.util.Log

class FitAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("FitApp", "Application initialized")
    }
}
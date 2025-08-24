package com.example.fitapp.data.ai

import android.content.Context
import java.util.Properties

object KeyLoader {
    fun get(context: Context, key: String): String? {
        return try {
            val props = Properties().apply {
                context.assets.open("local.properties").use { load(it) }
            }
            props.getProperty(key)
        } catch (e: Exception) {
            null
        }
    }
}

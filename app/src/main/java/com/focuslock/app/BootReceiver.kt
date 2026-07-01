package com.focuslock.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.longPreferencesKey
import com.focuslock.app.data.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    companion object {
        val END_TIME = longPreferencesKey("end_time")
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = runBlocking { context.dataStore.data.first() }
            val end = prefs[END_TIME] ?: 0L
            if (System.currentTimeMillis() < end) {
                context.startService(Intent(context, BlockingService::class.java))
                context.startService(Intent(context, OverlayService::class.java))
            }
        }
    }
}

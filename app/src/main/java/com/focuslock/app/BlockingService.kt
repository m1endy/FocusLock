package com.focuslock.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.focuslock.app.data.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BlockingService : AccessibilityService() {

    companion object {
        val BLOCKED_APPS = stringPreferencesKey("blocked_apps")
        val END_TIME = longPreferencesKey("end_time")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        serviceInfo = info
        startForeground(1, createNotification())
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        val prefs = runBlocking { dataStore.data.first() }
        val blocked = prefs[BLOCKED_APPS]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
        val end = prefs[END_TIME] ?: 0L
        if (pkg in blocked && System.currentTimeMillis() < end) {
            startService(Intent(this, OverlayService::class.java))
        }
    }

    override fun onInterrupt() {}

    private fun createNotification(): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel("focuslock", "FocusLock", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        return NotificationCompat.Builder(this, "focuslock")
            .setContentTitle("FocusLock активен")
            .setContentText("Блокировка приложений включена")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}

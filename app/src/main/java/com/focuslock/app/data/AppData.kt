package com.focuslock.app.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager

data class AppInfo(val packageName: String, val appName: String)

fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { it.packageName != context.packageName }
        .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName == "com.android.settings" }
        .map { AppInfo(it.packageName, pm.getApplicationLabel(it).toString()) }
        .sortedBy { it.appName.lowercase() }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val services = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    return services.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
}

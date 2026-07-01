package com.focuslock.app.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "focus_lock")

data class AppInfo(val packageName: String, val appName: String, val isSystem: Boolean)

class FocusViewModel : ViewModel() {
    var apps by mutableStateOf<List<AppInfo>>(emptyList())
    var selectedPackages by mutableStateOf<Set<String>>(emptySet())
    var blockedPackages by mutableStateOf<Set<String>>(emptySet())
    var endTime by mutableStateOf(0L)
    var isActive by mutableStateOf(false)
    var searchQuery by mutableStateOf("")

    fun loadApps(context: Context) {
        val pm = context.packageManager
        val selfPackage = context.packageName
        apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != selfPackage }
            .map {
                AppInfo(
                    packageName = it.packageName,
                    appName = pm.getApplicationLabel(it).toString(),
                    isSystem = (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .filter { !it.isSystem || it.packageName == "com.android.settings" }
            .sortedBy { it.appName.lowercase() }
    }

    fun loadState(context: Context) {
        viewModelScope.launch {
            val prefs = context.dataStore.data.first()
            blockedPackages = prefs[BLOCKED_APPS]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
            endTime = prefs[END_TIME] ?: 0L
            isActive = System.currentTimeMillis() < endTime && blockedPackages.isNotEmpty()
            selectedPackages = blockedPackages.toSet()
        }
    }

    fun saveBlocking(context: Context, durationMinutes: Int) {
        viewModelScope.launch {
            val end = System.currentTimeMillis() + durationMinutes * 60_000L
            context.dataStore.edit {
                it[BLOCKED_APPS] = selectedPackages.joinToString(",")
                it[END_TIME] = end
            }
            endTime = end
            blockedPackages = selectedPackages.toSet()
            isActive = true
        }
    }

    fun clearBlocking(context: Context) {
        viewModelScope.launch {
            context.dataStore.edit {
                it.remove(BLOCKED_APPS)
                it.remove(END_TIME)
            }
            endTime = 0L
            blockedPackages = emptySet()
            isActive = false
        }
    }

    fun toggleApp(packageName: String) {
        selectedPackages = if (packageName in selectedPackages) selectedPackages - packageName
        else selectedPackages + packageName
    }

    companion object {
        val BLOCKED_APPS = stringPreferencesKey("blocked_apps")
        val END_TIME = longPreferencesKey("end_time")
    }
}

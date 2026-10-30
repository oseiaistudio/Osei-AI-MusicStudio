package com.osei.aimusicstudio.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages local studio preferences, including explicit Local-Only Mode vs Cloud Sync.
 *
 * Default configuration:
 * - [isCloudSyncEnabled] defaults to `false` (Local-Only Mode).
 * - When false, all database operations remain strictly confined to local Room SQLite.
 * - Zero network calls to Cloud Firestore are made.
 * - Cloud synchronization can be explicitly enabled by the user in Studio Settings.
 */
class StudioPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isCloudSyncEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_CLOUD_SYNC_ENABLED, DEFAULT_CLOUD_SYNC_ENABLED)
    )
    val isCloudSyncEnabled: StateFlow<Boolean> = _isCloudSyncEnabled.asStateFlow()

    fun setCloudSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CLOUD_SYNC_ENABLED, enabled).apply()
        _isCloudSyncEnabled.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "osei_studio_prefs"
        private const val KEY_CLOUD_SYNC_ENABLED = "cloud_sync_enabled"
        const val DEFAULT_CLOUD_SYNC_ENABLED = false // Local-Only Mode by default (Budget GH₵0)
    }
}

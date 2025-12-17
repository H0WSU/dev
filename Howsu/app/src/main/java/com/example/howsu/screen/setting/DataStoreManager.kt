package com.example.howsu.screen.setting

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
    }

    // 알림 설정값 가져오기 (기본값 true)
    val isNotificationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[NOTIFICATION_ENABLED] ?: true }

    // 알림 설정값 저장하기
    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }
}
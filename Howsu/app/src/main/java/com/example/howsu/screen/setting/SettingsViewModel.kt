package com.example.howsu.screen.setting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)

    // UI에서 관찰할 수 있도록 LiveData로 변환해서 노출
    val isNotificationEnabled = dataStoreManager.isNotificationEnabled.asLiveData()

    // 스위치를 누를 때 호출할 함수
    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setNotificationEnabled(enabled)
        }
    }
}
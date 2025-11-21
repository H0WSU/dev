package com.example.howsu.screen.famliy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class NicknameRegisterViewModel : ViewModel() {
    // 화면이 전환되어도 유지되어야 할 데이터
    var nickname by mutableStateOf("")
    var profileImageUrl by mutableStateOf<String?>(null)
}
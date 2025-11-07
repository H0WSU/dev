package com.example.howsu // (1. 본인 패키지 이름)

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.howsu.navigation.AppNavigation // (2. 새로 만들 파일 Import)
import com.example.howsu.ui.theme.HowsuTheme
import com.kakao.sdk.common.util.Utility

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val keyHash = Utility.getKeyHash(this)
        Log.d("MyKeyHash", "Key Hash: $keyHash")
        setContent {
            HowsuTheme {
                AppNavigation()
            }
        }
    }
}
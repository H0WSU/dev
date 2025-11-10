package com.example.howsu // (1. 본인 패키지 이름)

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.howsu.navigation.AppNavigation
import com.example.howsu.ui.theme.HowsuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HowsuTheme {
                AppNavigation()
            }
        }
    }
}
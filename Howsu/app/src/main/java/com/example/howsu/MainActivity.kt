package com.example.howsu

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.howsu.navigation.AppNavigation
import com.example.howsu.ui.theme.HowsuTheme

class MainActivity : ComponentActivity() {

    // ★ 1. 권한 요청 결과 처리기 (알림 허용 눌렀는지 확인용)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 권한 허용됨
        } else {
            // 권한 거부됨 (필요시 설정 화면으로 유도)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        askNotificationPermission()

        enableEdgeToEdge()
        setContent {
            HowsuTheme {
                AppNavigation()
            }
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "투두 알림"
            val descriptionText = "할 일 알림을 받습니다."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("todo_channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // ★ 4. 알림 권한 요청 (Android 13 이상 필수)
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // 이미 권한 있음
            } else {
                // 권한 없음 -> 팝업 띄우기
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
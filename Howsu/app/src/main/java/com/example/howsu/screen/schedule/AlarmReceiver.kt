package com.example.howsu.screen.schedule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.howsu.R // (R 임포트 경로 확인)

class AlarmReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getStringExtra("SCHEDULE_ID") ?: return
        val title = intent.getStringExtra("SCHEDULE_TITLE") ?: "예약된 일정"

        Log.d("AlarmReceiver", "알림 수신: $scheduleId - $title")

        // 알림을 표시
        sendNotification(context, scheduleId, title)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(context: Context, scheduleId: String, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "SCHEDULE_NOTIFICATIONS"

        // (Android 8.0 이상) 알림 채널 생성
        val channel = NotificationChannel(
            channelId,
            "일정 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "등록된 일정에 대한 알림"
        }
        notificationManager.createNotificationChannel(channel)

        // 알림 생성
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ★ TODO: 알림 아이콘으로 변경
            .setContentTitle("오늘의 일정 🐾")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // 클릭 시 자동 삭제
            .build()

        // 알림 표시 (ID는 scheduleId의 해시코드)
        notificationManager.notify(scheduleId.hashCode(), notification)
    }
}
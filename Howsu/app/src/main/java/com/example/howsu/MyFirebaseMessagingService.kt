package com.example.howsu

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 메시지를 받았을 때 실행됨
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 알림 내용이 있으면 화면에 띄우기
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    // 토큰이 갱신될 때 실행됨 (앱 지웠다 깔거나 오래되면 바뀜)
    override fun onNewToken(token: String) {
        // 여기서 뷰모델 등을 통해 DB에 새 토큰을 저장해야 함
        // (일단 로그만 찍고, 실제 저장은 로그인 직후에 하는 게 안전함)
        android.util.Log.d("FCM", "New token: $token")
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "todo_channel_id"
        val channelName = "투두 알림"

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ★ 알림 아이콘 (없으면 앱 기본 아이콘 사용)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 안드로이드 8.0(Oreo) 이상은 채널이 필요함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
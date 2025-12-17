package com.example.howsu

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.howsu.screen.setting.DataStoreManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 메시지를 받았을 때 실행됨
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [추가] 알림 설정 체크
        val dataStoreManager = DataStoreManager(applicationContext)
        val isEnabled = runBlocking { dataStoreManager.isNotificationEnabled.first() }

        if (!isEnabled) {
            Log.d("FCM_TEST", "알림 설정이 꺼져 있어 표시하지 않음")
            return
        }

        Log.d("FCM_TEST", "메시지 수신됨! ID: ${remoteMessage.messageId}")

        // 1. 알림(Notification) 부분 확인
        remoteMessage.notification?.let {
            Log.d("FCM_TEST", "알림 내용: ${it.title} - ${it.body}")
            showNotification(it.title, it.body)
        }

        // 2. 데이터(Data) 부분 확인 (서버에서 데이터만 보낼 때)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM_TEST", "데이터 내용: ${remoteMessage.data}")
            // 혹시 notification이 비어있고 data만 왔다면 여기서 띄워야 함
            if (remoteMessage.notification == null) {
                val title = remoteMessage.data["title"] ?: "알림"
                val body = remoteMessage.data["body"] ?: "내용 확인"
                showNotification(title, body)
            }
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
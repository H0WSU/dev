package com.example.howsu.screen.family

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NicknameRegisterViewModel : ViewModel() {
    var nickname by mutableStateOf("")
    var profileImageUrl by mutableStateOf<String?>(null)

    // isLoading 변수는 이제 필요 없음 (바로 넘길 거니까)

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    // @OptIn 어노테이션: GlobalScope 사용 경고 무시
    @OptIn(DelicateCoroutinesApi::class)
    fun saveNicknameToFirebase(onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        // 1. [핵심] 저장 결과를 기다리지 않고, 일단 화면부터 넘겨 버림
        // (다음 화면에서는 로컬 URI인 profileImageUrl을 보여주면 됨)
        onSuccess()

        // 2. [핵심] 화면이 꺼져도 죽지 않는 'GlobalScope'에서 몰래 업로드
        GlobalScope.launch {
            try {
                var downloadUrl = profileImageUrl

                // 이미지 업로드
                if (profileImageUrl != null && profileImageUrl!!.startsWith("content://")) {
                    val imageUri = Uri.parse(profileImageUrl)
                    val storageRef = storage.reference.child("profile_images/$uid.jpg")
                    storageRef.putFile(imageUri).await()
                    downloadUrl = storageRef.downloadUrl.await().toString()
                }

                // DB 저장
                val userData = hashMapOf(
                    "name" to nickname,
                    "profileImageUrl" to (downloadUrl ?: "")
                )

                db.collection("users").document(uid)
                    .set(userData, SetOptions.merge())
                    .await()

                println("백그라운드 저장 완료: $nickname")

            } catch (e: Exception) {
                e.printStackTrace()
                // 주의: 이미 화면이 넘어갔으므로 여기서 에러가 나도 사용자에게 알려줄 방법이 마땅치 않음
                println("백그라운드 저장 실패 ㅠㅠ")
            }
        }
    }
}
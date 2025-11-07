package com.example.howsu.screen.login

import com.google.firebase.functions.ktx.functions
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Firebase 로그인 상태를 UI에 알리기 위한 클래스
sealed class FirebaseLoginState {
    object Idle : FirebaseLoginState()
    object Loading : FirebaseLoginState()
    object Success : FirebaseLoginState() // 로그인 성공
    data class Error(val message: String) : FirebaseLoginState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val functions = Firebase.functions

    private val _loginState = MutableStateFlow<FirebaseLoginState>(FirebaseLoginState.Idle)
    val loginState = _loginState.asStateFlow()

    /**
     * JoinScreen에서 받은 Google Credential로 Firebase에 최종 로그인
     */
    fun signInWithGoogleCredential(credential: AuthCredential) {
        _loginState.value = FirebaseLoginState.Loading
        viewModelScope.launch {
            try {
                // Firebase SDK에 로그인 요청
                auth.signInWithCredential(credential).await()

                // 성공!
                _loginState.value = FirebaseLoginState.Success

            } catch (e: Exception) {
                // 실패
                _loginState.value = FirebaseLoginState.Error(e.message ?: "Firebase 로그인 실패")
            }
        }
    }

    // (AuthViewModel.kt 안에 추가)
    fun startKakaoLogin(context: Context) {
        viewModelScope.launch {
            try {
                val kakaoToken = if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                    UserApiClient.instance.loginWithKakaoTalk(context).await()
                } else {
                    UserApiClient.instance.loginWithKakaoAccount(context).await()
                }

                // ★ 2. 받은 토큰으로 Cloud Function 호출
                sendKakaoTokenToBackend(kakaoToken.accessToken)

            } catch (e: Exception) {
                _loginState.value = FirebaseLoginState.Error(e.message ?: "카카오 로그인 실패")
            }
        }
    }

    /**
     * 2. [안드로이드] '카카오 토큰'을 Cloud Function으로 전송
     */
    private fun sendKakaoTokenToBackend(kakaoAccessToken: String) {
        _loginState.value = FirebaseLoginState.Loading

        val data = hashMapOf("token" to kakaoAccessToken)

        functions
            .getHttpsCallable("kakaoLogin") // ★ 3. `index.ts`에 만든 함수 이름
            .call(data)
            .continueWith { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                return@continueWith task.result?.data as Map<String, Any>
            }
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (task.isSuccessful) {
                        try {
                            // ★ 4. Cloud Function이 돌려준 '커스텀 토큰'
                            val firebaseCustomToken = task.result["firebaseToken"] as String

                            // ★ 5. '커스텀 토큰'으로 Firebase에 최종 로그인
                            auth.signInWithCustomToken(firebaseCustomToken).await()
                            _loginState.value = FirebaseLoginState.Success
                        } catch (e: Exception) {
                            _loginState.value = FirebaseLoginState.Error(e.message ?: "커스텀 토큰 로그인 실패")
                        }
                    } else {
                        _loginState.value = FirebaseLoginState.Error(task.exception?.message ?: "Cloud Function 호출 실패")
                    }
                }
            }
    }
}
package com.example.howsu.screen.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
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
    private val functions: FirebaseFunctions = Firebase.functions("asia-northeast3")

    // ★★★ ID 연동 로직 추가 ★★★
    private val _currentUserId = MutableStateFlow<String?>(auth.currentUser?.uid)
    val currentUserId = _currentUserId.asStateFlow()

    private val _loginState = MutableStateFlow<FirebaseLoginState>(FirebaseLoginState.Idle)
    val loginState = _loginState.asStateFlow()

    init {
        // Firebase 인증 상태 리스너 등록
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserId.value = firebaseAuth.currentUser?.uid
        }
    }

    /**
     * 이메일/비밀번호로 신규 회원가입
     */
    fun signUpWithEmailPassword(email: String, password: String) {
        _loginState.value = FirebaseLoginState.Loading
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _loginState.value = FirebaseLoginState.Success
                _currentUserId.value = auth.currentUser?.uid // ID 업데이트
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email sign-up failed", e)
                _loginState.value = FirebaseLoginState.Error(e.message ?: "이메일 회원가입 실패")
            }
        }
    }

    /**
     * 이메일/비밀번호로 기존 회원 로그인
     */
    fun signInWithEmailPassword(email: String, password: String) {
        _loginState.value = FirebaseLoginState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _loginState.value = FirebaseLoginState.Success
                _currentUserId.value = auth.currentUser?.uid // ID 업데이트
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email sign-in failed", e)
                _loginState.value = FirebaseLoginState.Error(e.message ?: "이메일 로그인 실패")
            }
        }
    }

    fun signInWithGoogleCredential(credential: AuthCredential) {
        _loginState.value = FirebaseLoginState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                _loginState.value = FirebaseLoginState.Success
                _currentUserId.value = auth.currentUser?.uid // ID 업데이트
            } catch (e: Exception) {
                _loginState.value = FirebaseLoginState.Error(e.message ?: "Firebase 로그인 실패")
            }
        }
    }

    fun startKakaoLogin(context: Context) {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("AuthViewModel", "카카오 로그인 실패", error)
                _loginState.value = FirebaseLoginState.Error(error.message ?: "카카오 로그인 실패")
            } else if (token != null) {
                Log.d("AuthViewModel", "카카오 로그인 성공: ${token.accessToken}")
                sendKakaoTokenToBackend(token.accessToken)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    /**
     * [안드로이드] '카카오 토큰'을 Cloud Function으로 전송
     */
    private fun sendKakaoTokenToBackend(kakaoAccessToken: String) {
        _loginState.value = FirebaseLoginState.Loading

        val data = hashMapOf("token" to kakaoAccessToken)

        functions
            .getHttpsCallable("kakaoLogin")
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
                            val firebaseCustomToken = task.result["firebaseToken"] as String
                            auth.signInWithCustomToken(firebaseCustomToken).await()
                            _loginState.value = FirebaseLoginState.Success
                            _currentUserId.value = auth.currentUser?.uid // ID 업데이트
                        } catch (e: Exception) {
                            _loginState.value = FirebaseLoginState.Error(e.message ?: "커스텀 토큰 로그인 실패")
                        }
                    } else {
                        _loginState.value = FirebaseLoginState.Error(task.exception?.message ?: "Cloud Function 호출 실패")
                    }
                }
            }
    }

    fun loginWithNaverToken(naverAccessToken: String) {
        _loginState.value = FirebaseLoginState.Loading

        viewModelScope.launch {
            try {
                val data = hashMapOf(
                    "naverAccessToken" to naverAccessToken
                )

                val result = functions
                    .getHttpsCallable("verifyNaverToken")
                    .call(data)
                    .await()

                @Suppress("UNCHECKED_CAST")
                val resultMap = result.data as? Map<String, String>
                val firebaseCustomToken = resultMap?.get("firebaseCustomToken")

                if (firebaseCustomToken == null) {
                    Log.e("AuthViewModel", "Firebase Custom Token이 비어있습니다. (Naver)")
                    _loginState.value = FirebaseLoginState.Error("네이버 로그인 중 오류가 발생했습니다.")
                    return@launch
                }

                auth.signInWithCustomToken(firebaseCustomToken).await()
                _loginState.value = FirebaseLoginState.Success
                _currentUserId.value = auth.currentUser?.uid // ID 업데이트

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Cloud Function(verifyNaverToken) 호출 실패", e)
                _loginState.value = FirebaseLoginState.Error(e.message ?: "네이버 로그인 실패")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _loginState.value = FirebaseLoginState.Idle
        _currentUserId.value = null // ID를 null로 설정
        Log.d("AuthViewModel", "로그아웃 성공.")
    }

    /**
     * [회원 탈퇴] 현재 로그인된 Firebase 계정을 삭제합니다.
     */
    fun deleteUserAndLogout() {
        val user = auth.currentUser
        if (user == null) {
            _loginState.value = FirebaseLoginState.Error("로그인된 사용자가 없습니다.")
            return
        }

        _loginState.value = FirebaseLoginState.Loading
        viewModelScope.launch {
            try {
                user.delete().await()
                _loginState.value = FirebaseLoginState.Idle
                _currentUserId.value = null // ID를 null로 설정
                Log.d("AuthViewModel", "회원 탈퇴 및 계정 삭제 성공.")

            } catch (e: Exception) {
                Log.e("AuthViewModel", "회원 탈퇴 실패", e)
                if (e.message?.contains("REQUIRES_RECENT_LOGIN") == true) {
                    _loginState.value = FirebaseLoginState.Error("보안을 위해 재로그인이 필요합니다.")
                } else {
                    _loginState.value = FirebaseLoginState.Error(e.message ?: "회원 탈퇴 실패")
                }
            }
        }
    }
}
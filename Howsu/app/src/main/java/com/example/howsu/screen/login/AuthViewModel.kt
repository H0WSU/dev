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

    private val _loginState = MutableStateFlow<FirebaseLoginState>(FirebaseLoginState.Idle)
    val loginState = _loginState.asStateFlow()

    /**
     * 이메일/비밀번호로 신규 회원가입
     */
    fun signUpWithEmailPassword(email: String, password: String) {
        _loginState.value = FirebaseLoginState.Loading
        viewModelScope.launch {
            try {
                // Firebase SDK에 회원가입 요청
                auth.createUserWithEmailAndPassword(email, password).await()

                // 성공! (회원가입 성공 시 자동으로 로그인됩니다)
                _loginState.value = FirebaseLoginState.Success

            } catch (e: Exception) {
                // 실패 (예: 이미 사용 중인 이메일, 비밀번호 형식 오류 등)
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
                // Firebase SDK에 로그인 요청
                auth.signInWithEmailAndPassword(email, password).await()

                // 성공!
                _loginState.value = FirebaseLoginState.Success

            } catch (e: Exception) {
                // 실패 (예: 잘못된 이메일, 틀린 비밀번호 등)
                Log.e("AuthViewModel", "Email sign-in failed", e)
                _loginState.value = FirebaseLoginState.Error(e.message ?: "이메일 로그인 실패")
            }
        }
    }
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

    fun startKakaoLogin(context: Context) {
        // 1. 카카오톡이 설치되어 있는지 확인
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("AuthViewModel", "카카오 로그인 실패", error)
                _loginState.value = FirebaseLoginState.Error(error.message ?: "카카오 로그인 실패")
            } else if (token != null) {
                Log.d("AuthViewModel", "카카오 로그인 성공: ${token.accessToken}")
                // ★ 2. 받은 토큰으로 Cloud Function 호출
                sendKakaoTokenToBackend(token.accessToken)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡으로 로그인
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            // 카카오계정(웹뷰)으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
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

    fun loginWithNaverToken(naverAccessToken: String) {
        // 1. UI 상태를 로딩 중으로 변경
        _loginState.value = FirebaseLoginState.Loading

        viewModelScope.launch {
            try {
                // 2. Cloud Function에 전달할 데이터 맵 생성
                //    (index.js에서 'naverAccessToken' 키로 받기로 약속했음)
                val data = hashMapOf(
                    "naverAccessToken" to naverAccessToken
                )

                // 3. 배포된 "verifyNaverToken" Cloud Function 호출
                val result = functions
                    .getHttpsCallable("verifyNaverToken")
                    .call(data)
                    .await()

                // 4. 함수 실행 결과 (JSON) 파싱
                //    (index.js에서 'firebaseCustomToken' 키로 반환하기로 약속했음)
                @Suppress("UNCHECKED_CAST")
                val resultMap = result.data as? Map<String, String>
                val firebaseCustomToken = resultMap?.get("firebaseCustomToken")

                if (firebaseCustomToken == null) {
                    Log.e("AuthViewModel", "Firebase Custom Token이 비어있습니다. (Naver)")
                    _loginState.value = FirebaseLoginState.Error("네이버 로그인 중 오류가 발생했습니다.")
                    return@launch
                }

                // 5. Firebase 커스텀 토큰으로 Firebase에 최종 로그인
                auth.signInWithCustomToken(firebaseCustomToken).await()
                _loginState.value = FirebaseLoginState.Success // 성공!

            } catch (e: Exception) {
                // Cloud Function 호출 실패 또는 기타 에러
                Log.e("AuthViewModel", "Cloud Function(verifyNaverToken) 호출 실패", e)
                _loginState.value = FirebaseLoginState.Error(e.message ?: "네이버 로그인 실패")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        // 로그아웃 시 UI가 로그인 화면으로 돌아가도록
        _loginState.value = FirebaseLoginState.Idle // 상태 초기화
    }
}
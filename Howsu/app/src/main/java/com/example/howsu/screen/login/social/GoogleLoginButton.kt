package com.example.howsu.screen.login.social

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.howsu.R
import com.example.howsu.screen.login.AuthViewModel
import com.example.howsu.screen.login.FirebaseLoginState
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun GoogleLoginButton(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel?, // ViewModel을 주입받음
    webClientId: String,
    onLoginSuccess: () -> Unit,
    onLoginError: (String) -> Unit
) {
    val context = LocalContext.current
    val oneTapClient = remember { Identity.getSignInClient(context) }

    // 1. 구글 로그인 결과 처리 런처
    val googleLoginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                viewModel?.signInWithGoogleCredential(firebaseCredential)
            }
        } catch (e: ApiException) {
            Log.e("GoogleLoginButton", "Google Sign-In failed: ${e.message}")
            onLoginError("Google Sign-In failed")
        }
    }

    // 2. ViewModel 상태 관찰 (로그인 성공/실패 시 콜백 호출)
    val loginState by viewModel?.loginState?.collectAsState()
        ?: remember { mutableStateOf(FirebaseLoginState.Idle) }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is FirebaseLoginState.Success -> onLoginSuccess()
            is FirebaseLoginState.Error -> onLoginError(state.message)
            else -> {}
        }
    }

    // 3. UI (버튼 모양)
    // onClick에 람다 로직을 직접 구현하여 빨간 줄(unstable lambda) 경고를 해결
    SocialLoginButton(
        iconRes = R.drawable.ic_google,
        modifier = modifier,
        onClick = {
            if (viewModel == null) {
                onLoginError("ViewModel is not available")
                // ★ 람다의 이름을 SocialLoginButton으로 변경
                return@SocialLoginButton
            }

            val signInRequest = GetSignInIntentRequest.builder()
                .setServerClientId(webClientId)
                .build()

            oneTapClient.getSignInIntent(signInRequest)
                .addOnSuccessListener { pendingIntent ->
                    googleLoginLauncher.launch(
                        IntentSenderRequest.Builder(pendingIntent).build()
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("GoogleLoginButton", "Google OneTap Intent 실패: ${e.message}")
                    onLoginError("Google OneTap Intent failed")
                }
        }
    )
}
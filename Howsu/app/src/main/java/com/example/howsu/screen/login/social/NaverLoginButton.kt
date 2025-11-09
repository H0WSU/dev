// NaverLoginButton.kt
package com.example.howsu.screen.login.social

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.howsu.R
import com.example.howsu.screen.login.AuthViewModel
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback

@Composable
fun NaverLoginButton(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel?
) {
    val context = LocalContext.current

    val oauthLoginCallback = object : OAuthLoginCallback {
        override fun onSuccess() {
            val naverAccessToken = NaverIdLoginSDK.getAccessToken()
            if (naverAccessToken != null) {
                Log.d("NaverLogin", "Naver Access Token: $naverAccessToken")

                viewModel?.loginWithNaverToken(naverAccessToken)
            } else {
                Log.e("NaverLogin", "Naver Access Token is null")
            }
        }

        override fun onFailure(httpStatus: Int, message: String) {
            Log.e("NaverLogin", "네이버 로그인 실패 - $message")
        }

        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    SocialLoginButton(
        iconRes = R.drawable.ic_naver,
        modifier = modifier,
        onClick = {
            Log.d("NaverLoginButton", "Naver login clicked")
            NaverIdLoginSDK.authenticate(context, oauthLoginCallback)
        }
    )
}
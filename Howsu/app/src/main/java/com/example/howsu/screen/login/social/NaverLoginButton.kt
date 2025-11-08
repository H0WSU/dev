package com.example.howsu.screen.login.social

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.howsu.R
import com.example.howsu.screen.login.AuthViewModel

@Composable
fun NaverLoginButton(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel?
) {
    SocialLoginButton(
        iconRes = R.drawable.ic_naver,
        modifier = modifier,
        onClick = {
            // TODO: 네이버 로그인 로직 (Cloud Function 필요)
            Log.d("NaverLoginButton", "Naver login clicked")
        }
    )
}
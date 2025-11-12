package com.example.howsu.screen.login.social

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.howsu.R
import com.example.howsu.screen.login.AuthViewModel

@Composable
fun KakaoLoginButton(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel?
) {
    val context = LocalContext.current

    SocialLoginButton(
        iconRes = R.drawable.ic_kakao,
        modifier = modifier,
        onClick = {
            viewModel?.startKakaoLogin(context)
        }
    )
}
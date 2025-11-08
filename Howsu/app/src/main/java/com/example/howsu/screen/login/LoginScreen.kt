package com.example.howsu.screen.login

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.screen.login.social.GoogleLoginButton
import com.example.howsu.screen.login.social.KakaoLoginButton
import com.example.howsu.screen.login.social.NaverLoginButton
import com.example.howsu.ui.theme.HowsuTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel? = null // ★ 1. ViewModel 주입
) {
    val vm = authViewModel ?: androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>()
    // ★ 2. Firebase 콘솔에서 복사한 '웹 클라이언트 ID'
    val WEB_CLIENT_ID = "400269215891-ui7tvovededsotn89cg4prdvkj87v7ul.apps.googleusercontent.com"

    Scaffold(
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "로그인" 타이틀
            Text(
                text = "로그인",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 이메일 입력 필드
            LoginTextField(
                label = "이메일",
                placeholder = "이메일을 입력해 주세요",
                value = "",
                onValueChange = { }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 입력 필드
            PasswordTextField(
                label = "비밀번호",
                value = "rnldudnjl!@",
                onValueChange = { }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 체크박스 및 비밀번호 찾기
            LoginOptionsRow()
            Spacer(modifier = Modifier.height(32.dp))

            // 로그인하기 버튼
            Button(
                onClick = { /* TODO: 이메일/비번 로그인 로직 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("로그인하기", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))

            // "또는" 구분선
            OrDivider()
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NaverLoginButton(
                    modifier = Modifier.weight(1f),
                    viewModel = vm
                )

                GoogleLoginButton(
                    modifier = Modifier.weight(1f),
                    viewModel = vm,
                    webClientId = WEB_CLIENT_ID,
                    onLoginSuccess = { Log.d("LoginScreen", "Firebase 로그인 성공!") },
                    onLoginError = { message -> Log.e("LoginScreen", "로그인 실패: $message") }
                )

                KakaoLoginButton(
                    modifier = Modifier.weight(1f),
                    viewModel = vm
                )
            }

            // 하단 가입 안내 텍스트
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = buildAnnotatedString {
                    append("가입한 적이 없나요? ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("가입하기")
                    }
                },
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clickable { navController.navigate("join") }
            )
        }
    }
}

// --- 일반 텍스트 필드 (이메일) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // 높이 고정
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF0F0F0), // 배경색
                unfocusedContainerColor = Color(0xFFF0F0F0),
                disabledContainerColor = Color(0xFFF0F0F0),
                focusedIndicatorColor = Color.Transparent, // 밑줄 제거
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

// --- 비밀번호 텍스트 필드 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("비밀번호", fontSize = 14.sp, color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "비밀번호 보기/숨기기")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF0F0F0),
                unfocusedContainerColor = Color(0xFFF0F0F0),
                disabledContainerColor = Color(0xFFF0F0F0),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

// --- 체크박스 및 비밀번호 기억 안나요 ---
@Composable
private fun LoginOptionsRow() {
    var isChecked by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { isChecked = !isChecked } // Row 클릭 시 토글
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.Black,
                    uncheckedColor = Color.Gray
                ),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("비밀번호 기억하기", fontSize = 13.sp)
        }
        Text(
            text = "비밀번호가 기억나지 않아요!",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.clickable { /* TODO: 비밀번호 찾기 */ }
        )
    }
}

// --- "또는" 구분선 ---
@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = Color.LightGray,
            thickness = 1.dp
        )
        Text(
            text = " 또는 ",
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.Gray,
            fontSize = 12.sp
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = Color.LightGray,
            thickness = 1.dp
        )
    }
}

// --- ★ 10. SocialLoginButtons 함수 수정됨
@Composable
private fun SocialLoginButtons(
    onGoogleClick: () -> Unit,
    onNaverClick: () -> Unit,
    onKakaoClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SocialLoginButton(
            iconRes = R.drawable.ic_naver, // (TODO: 네이버 아이콘으로 변경)
            modifier = Modifier.weight(1f),
            onClick = onNaverClick
        )
        SocialLoginButton(
            iconRes = R.drawable.ic_google, // (구글 아이콘)
            modifier = Modifier.weight(1f),
            // ★ 1. onClick에 람다를 직접 전달
            onClick = onGoogleClick
        )
        SocialLoginButton(
            iconRes = R.drawable.ic_kakao, // (TODO: 카카오 아이콘으로 변경)
            modifier = Modifier.weight(1f),
            onClick = onKakaoClick
        )
    }
}

@Composable
private fun SocialLoginButton(
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit // onClick 파라미터 추가
) {
    Surface(
        modifier = modifier
            .height(60.dp)
            .clickable(onClick = onClick), // clickable에 onClick 연결
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)), // (테두리)
        shape = RoundedCornerShape(12.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp) // 이미지와 테두리 간격
        )
    }
}


// --- 미리보기 ---
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        LoginScreen(navController = navController)
    }
}
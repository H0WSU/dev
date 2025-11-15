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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel? = null
) {
    val vm = authViewModel ?: androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>()
    val WEB_CLIENT_ID = "400269215891-ui7tvovededsotn89cg4prdvkj87v7ul.apps.googleusercontent.com"

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- ★ 5. 오류 메시지 상태 변수 (팝업창용) ---
    var dialogError by remember { mutableStateOf<String?>(null) }
    val showDialog = dialogError != null

    val loginState by vm.loginState.collectAsState()

    // --- ★ 6. 자동 로그인 로직 (기존) ---
    // (이 화면이 처음 뜰 때 1번만 실행됨)
    LaunchedEffect(key1 = Unit) {
        if (Firebase.auth.currentUser != null) {
            Log.d("LoginScreen", "자동 로그인 확인.")
            navController.navigate("todo") {
                popUpTo("auth_graph") { inclusive = true }
            }
        }
    }

    // --- ★ 7. 로그인 상태에 따른 팝업창 및 화면 이동 로직 ---
    LaunchedEffect(loginState) {
        when (loginState) {
            is FirebaseLoginState.Success -> {
                Log.d("LoginScreen", "Firebase 로그인 성공!")
                navController.navigate("todo") { popUpTo("auth_graph") { inclusive = true } }
            }
            is FirebaseLoginState.Error -> {
                val message = (loginState as FirebaseLoginState.Error).message
                Log.e("LoginScreen", "Firebase 로그인 실패: $message")

                // (수정) 팝업창 오류 메시지(dialogError)로 설정
                dialogError = when {
                    message.contains("INVALID_LOGIN_CREDENTIALS") -> "이메일 또는 비밀번호가 틀렸습니다"
                    message.contains("invalid-email") -> "가입되지 않았거나 유효하지 않은 이메일입니다"
                    message.contains("user-not-found") -> "가입되지 않은 이메일입니다" // (혹시 모를 구 버전)
                    message.contains("wrong-password") -> "비밀번호가 틀렸습니다" // (혹시 모를 구 버전)
                    else -> "로그인에 실패했습니다 ($message)"
                }
            }
            is FirebaseLoginState.Loading -> {
                dialogError = null // 로딩 시작 시 기존 오류 메시지 제거
            }
            else -> {}
        }
    }

    // --- ★ 8. 팝업창(AlertDialog) Composable ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { dialogError = null }, // 바깥쪽 클릭 시 닫힘
            title = { Text(text = "알림") },
            text = { Text(text = dialogError ?: "알 수 없는 오류") },
            confirmButton = {
                TextButton(
                    onClick = { dialogError = null } // '확인' 버튼 클릭 시 닫힘
                ) {
                    Text("확인")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()), // ★ 스크롤 가능하게
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
                value = email,
                onValueChange = { email = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 입력 필드
            PasswordTextField(
                label = "비밀번호",
                value = password,
                onValueChange = { password = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 체크박스 및 비밀번호 찾기
            LoginOptionsRow()
            Spacer(modifier = Modifier.height(32.dp))

            // 로그인하기 버튼
            Button(
                onClick = {
                    // ★ 9. 버튼 클릭 시 팝업창 오류 설정
                    if (email.isBlank() || password.isBlank()) {
                        dialogError = "이메일과 비밀번호를 모두 입력해 주세요."
                    } else {
                        vm.signInWithEmailPassword(email, password)
                    }
                },
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
                    onLoginError = { message ->
                        Log.e("LoginScreen", "로그인 실패: $message")
                        dialogError = "구글 로그인 실패: $message" // ★ 팝업창
                    }
                )

                KakaoLoginButton(
                    modifier = Modifier.weight(1f),
                    viewModel = vm
                )
            }

            // 하단 가입 안내 텍스트
            Spacer(modifier = Modifier.weight(1f, fill = false)) // ★ 스크롤 대응
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
                    .padding(vertical = 24.dp) // ★ 상하 패딩
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
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
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

// --- 체크박스 및 비밀번호 기억 안 나요 ---
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
            modifier = Modifier.clickable { isChecked = !isChecked }
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

// --- (SocialLoginButtons, SocialLoginButton 함수는 원본과 동일) ---
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
            iconRes = R.drawable.ic_naver,
            modifier = Modifier.weight(1f),
            onClick = onNaverClick
        )
        SocialLoginButton(
            iconRes = R.drawable.ic_google,
            modifier = Modifier.weight(1f),
            onClick = onGoogleClick
        )
        SocialLoginButton(
            iconRes = R.drawable.ic_kakao,
            modifier = Modifier.weight(1f),
            onClick = onKakaoClick
        )
    }
}

@Composable
private fun SocialLoginButton(
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(60.dp)
            .clickable(onClick = onClick),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
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
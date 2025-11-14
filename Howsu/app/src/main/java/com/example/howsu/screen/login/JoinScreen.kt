package com.example.howsu.screen.login // (1. 본인 패키지 이름 확인)

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog // ★ 1. 팝업창을 위해 import
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
import androidx.compose.material3.TextButton // ★ 2. 팝업창 버튼을 위해 import
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
import com.example.howsu.screen.login.social.SocialLoginButton
import com.example.howsu.ui.theme.HowsuTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinScreen(
    navController: NavController,
    authViewModel: AuthViewModel? = null
) {
    val WEB_CLIENT_ID = "400269215891-ui7tvovededsotn89cg4prdvkj87v7ul.apps.googleusercontent.com"

    val vm = authViewModel ?: androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>()
    val loginState by vm.loginState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }

    // --- ★ 3. 오류 메시지 상태를 두 개로 분리 ---
    // (1) 비밀번호 불일치 오류 (필드 아래 표시용)
    var passwordConfirmError by remember { mutableStateOf<String?>(null) }
    // (2) 그 외 모든 오류 (팝업창 표시용)
    var dialogError by remember { mutableStateOf<String?>(null) }

    // 팝업창을 보여줄지 여부
    val showDialog = dialogError != null

    // --- ★ 4. Firebase 오류(서버 오류)를 팝업창으로 설정 ---
    LaunchedEffect(loginState) {
        when (loginState) {
            is FirebaseLoginState.Success -> {
                Log.d("JoinScreen", "Firebase 회원가입 및 로그인 성공!")
                // TODO: (중요) 회원가입 성공 시 홈 화면으로 이동
                navController.navigate("todo") { popUpTo("auth_graph") { inclusive = true } }
            }

            is FirebaseLoginState.Error -> {
                val message = (loginState as FirebaseLoginState.Error).message
                Log.e("JoinScreen", "Firebase 로그인 실패: $message")
                // (수정) 팝업창 오류 메시지(dialogError)로 설정
                dialogError = when {
                    message.contains("email address is already in use") -> "이미 가입된 이메일입니다"
                    message.contains("WEAK_PASSWORD") -> "비밀번호는 6자 이상이어야 합니다"
                    else -> "회원가입에 실패했습니다 ($message)"
                }
            }

            is FirebaseLoginState.Loading -> {
                dialogError = null // 로딩 시작 시 기존 오류 메시지 제거
                passwordConfirmError = null
            }

            else -> {}
        }
    }

    // --- ★ 5. 팝업창(AlertDialog) Composable ---
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
        topBar = {
            JoinTopBar(onBackClick = { navController.popBackStack() })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()), // ★ 스크롤 가능하게
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

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
                onValueChange = {
                    password = it
                    passwordConfirmError = null // 입력 시 비밀번호 오류 메시지 숨김
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 재입력 필드
            PasswordTextField(
                label = "비밀번호 재입력",
                value = passwordConfirm,
                onValueChange = {
                    passwordConfirm = it
                    passwordConfirmError = null // 입력 시 비밀번호 오류 메시지 숨김
                }
            )

            // --- ★ 6. 비밀번호 불일치 오류 메시지 (필드 바로 아래) ---
            passwordConfirmError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }
            // ---

            Spacer(modifier = Modifier.height(16.dp))

            // 약관 동의 체크박스
            TermsAndConditionsCheckbox(
                isChecked = isChecked,
                onCheckedChange = { isChecked = it }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 지금 가입하기 버튼
            Button(
                onClick = {
                    // ★ 7. 오류 검사 로직 수정
                    passwordConfirmError = null // 이전 오류 초기화
                    dialogError = null

                    when {
                        email.isBlank() || password.isBlank() || passwordConfirm.isBlank() -> {
                            dialogError = "모든 항목을 입력해 주세요" // (팝업창)
                        }

                        !isChecked -> {
                            dialogError = "약관에 동의해 주세요" // (팝업창)
                        }

                        password != passwordConfirm -> {
                            passwordConfirmError = "비밀번호가 일치하지 않습니다" // (필드 아래)
                        }

                        else -> {
                            // 모든 검사 통과 시 회원가입 시도
                            vm.signUpWithEmailPassword(email, password)
                        }
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
                Text("지금 가입하기", fontWeight = FontWeight.Medium, fontSize = 14.sp)
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
                    viewModel = authViewModel
                )

                GoogleLoginButton(
                    modifier = Modifier.weight(1f),
                    viewModel = vm,
                    webClientId = WEB_CLIENT_ID,
                    onLoginSuccess = {
                        Log.d("JoinScreen", "Firebase 로그인 성공!")
                        navController.navigate("todo") { popUpTo("auth_graph") { inclusive = true } }
                    },
                    onLoginError = { message ->
                        Log.e("JoinScreen", "로그인 실패: $message")
                        dialogError = "구글 로그인 실패: $message"
                    }
                )

                KakaoLoginButton(
                    modifier = Modifier.weight(1f),
                    viewModel = authViewModel
                )
            }

            // 하단 로그인 안내 텍스트
            Spacer(modifier = Modifier.weight(1f, fill = false)) // ★ (수정) weight 수정
            Text(
                text = buildAnnotatedString {
                    append("이미 아이디가 있나요? ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("로그인하기")
                    }
                },
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(vertical = 24.dp) // ★ (수정) 상하 패딩 추가
                    .clickable { navController.navigate("login") }
            )
        }
    }
}

@Composable
private fun JoinTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterStart)
                .border(
                    BorderStroke(0.1.dp, Color.LightGray),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.Black
            )
        }
        Text(
            text = "가입하기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


@Composable
private fun TermsAndConditionsCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Black,
                uncheckedColor = Color.Gray
            ),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "약관에 동의합니다",
            fontSize = 13.sp
        )
    }
}

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


@Preview(showBackground = true)
@Composable
fun JoinScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        JoinScreen(navController = navController)
    }
}
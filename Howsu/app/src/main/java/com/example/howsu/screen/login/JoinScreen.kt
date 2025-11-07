package com.example.howsu.screen.login // (1. 본인 패키지 이름 확인)

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.howsu.ui.theme.HowsuTheme
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinScreen(
    navController: NavController,
    authViewModel: AuthViewModel? = null // ★ 1. ViewModel 주입
) {
    // ★ 2. Firebase 콘솔에서 복사한 '웹 클라이언트 ID'
    // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
    val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com" // ★ 본인 ID로 교체 ★
    // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

    val context = LocalContext.current
    val oneTapClient = remember { Identity.getSignInClient(context) }

    // ★ 3. 구글 로그인 창을 띄우고 결과를 받아올 런처
    val googleLoginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        // ★ 4. 구글 로그인 결과 처리
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken // 구글 ID 토큰
            if (idToken != null) {
                // ★ 5. 구글 토큰으로 Firebase Credential 생성
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                // ★ 6. ViewModel에 Credential을 넘겨 Firebase 로그인 요청
                authViewModel?.signInWithGoogleCredential(firebaseCredential)
            }
        } catch (e: ApiException) {
            Log.e("JoinScreen", "Google Sign-In failed: ${e.message}")
        }
    }

    // ★ 7. ViewModel의 Firebase 로그인 상태를 관찰
    val loginState by authViewModel?.loginState?.collectAsState()
        ?: remember { mutableStateOf(FirebaseLoginState.Idle) }
    LaunchedEffect(loginState) {
        when (loginState) {
            is FirebaseLoginState.Success -> {
                Log.d("JoinScreen", "Firebase 로그인 성공!")
                // TODO: 로그인 성공! 홈 화면으로 이동
                // navController.navigate("home") { popUpTo("auth_graph") }
            }
            is FirebaseLoginState.Error -> {
                Log.e("JoinScreen", "Firebase 로그인 실패: ${(loginState as FirebaseLoginState.Error).message}")
                // TODO: 사용자에게 에러 토스트 메시지 표시
            }
            else -> {} // Idle 또는 Loading
        }
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp)) // 상단 여백

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

            // 비밀번호 재입력 필드
            PasswordTextField(
                label = "비밀번호 재입력",
                value = "",
                onValueChange = { }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 약관 동의 체크박스
            TermsAndConditionsCheckbox()
            Spacer(modifier = Modifier.height(32.dp))

            // 지금 가입하기 버튼
            Button(
                onClick = { /* TODO: 회원가입 로직 */ },
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

            // ★ 8. 소셜 로그인 버튼들에 onClick 이벤트 연결
            SocialLoginButtons(
                onGoogleClick = {
                    if (authViewModel == null) return@SocialLoginButtons
                    // ★ 9. 구글 로그인 버튼 클릭 시
                    val signInRequest = GetSignInIntentRequest.builder()
                        .setServerClientId(WEB_CLIENT_ID) // ★ 2번의 그 ID
                        .build()

                    oneTapClient.getSignInIntent(signInRequest)
                        .addOnSuccessListener { pendingIntent ->
                            googleLoginLauncher.launch(
                                IntentSenderRequest.Builder(pendingIntent).build()
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e("JoinScreen", "Google OneTap Intent 실패: ${e.message}")
                        }
                },
                onNaverClick = {
                    // TODO: 네이버 로그인 로직 (Cloud Function 필요)
                },
                onKakaoClick = {
                    // TODO: 카카오 로그인 로직 (Cloud Function 필요)
                }
            )


            // 하단 로그인 안내 텍스트
            Spacer(modifier = Modifier.weight(1f))
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
                    .padding(bottom = 24.dp)
                    .clickable { navController.navigate("login") }
            )
        }
    }
}

// --- 상단 바 (뒤로가기 버튼 + 가입하기 타이틀) ---
@Composable
private fun JoinTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp) // 상단바 영역 패딩
            .height(40.dp), // 상단바 높이 고정 (디자인 일관성)
        contentAlignment = Alignment.Center
    ) {
        // 좌측 뒤로가기 버튼
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterStart) // 왼쪽에 정렬
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

        // 중앙 "가입하기" 타이틀
        Text(
            text = "가입하기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center) // 중앙 정렬
        )
    }
}


// --- 약관 동의 체크박스 ---
@Composable
private fun TermsAndConditionsCheckbox() {
    var isChecked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isChecked = !isChecked }, // Row 클릭 시 체크박스 토글
        verticalAlignment = Alignment.CenterVertically
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
        Text(
            text = "약관에 동의합니다", // (원본 코드의 "동의하겠습니다"에서 수정)
            fontSize = 13.sp
        )
    }
}

// --- LoginScreen에서 재활용하는 컴포넌트들 ---

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
            onClick = onGoogleClick
        )
        SocialLoginButton(
            iconRes = R.drawable.ic_kakao, // (TODO: 카카오 아이콘으로 변경)
            modifier = Modifier.weight(1f),
            onClick = onKakaoClick
        )
    }
}

// --- ★ 11. SocialLoginButton 함수 수정됨
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
fun JoinScreenPreview() { // (이름 변경)
    HowsuTheme {
        val navController = rememberNavController()
        JoinScreen(navController = navController) // (이름 변경)
    }
}
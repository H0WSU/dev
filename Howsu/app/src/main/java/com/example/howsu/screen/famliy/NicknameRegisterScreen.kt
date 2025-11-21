package com.example.howsu.screen.famliy

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun NicknameRegisterScreen(
    navController: NavHostController,
    onNicknameComplete: (String, String?) -> Unit = { nick, img -> },
    // ViewModel 주입 (기본값으로 내부 생성)
    viewModel: NicknameRegisterViewModel = viewModel()
) {
    // ViewModel 상태 사용 (화면 갔다가 돌아와도 유지됨)
    val nickname = viewModel.nickname
    val profileImageUrl = viewModel.profileImageUrl

    // 갤러리 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.profileImageUrl = uri.toString()
        }
    }

    val isNextEnabled = nickname.isNotBlank()

    Scaffold(
        topBar = {
            NicknameRegisterTopBar(
                onBack = {
                    // 뒤로가기 시 로그아웃 및 로그인 화면으로 이동
                    Firebase.auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        },
        bottomBar = {
            NicknameRegisterBottomBar(
                enabled = isNextEnabled,
                onNext = {
                    // 여기서 DB에 닉네임 저장
                    viewModel.saveNicknameToFirebase()

                    // 다음 화면으로 이동
                    onNicknameComplete(nickname, profileImageUrl)
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // 프로필 이미지 컴포넌트
            DoubleRingProfileImage(
                imageUrl = profileImageUrl,
                onClick = { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "사용할 닉네임을 입력해 주세요",
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { viewModel.nickname = it }, // ViewModel 값 업데이트
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "닉네임 입력하기",
                        color = Color(0xFFBDBDBD),
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor = Color.Black
                )
            )
        }
    }
}

/* -----------------------------------------------------------------------
   UI Components
   ----------------------------------------------------------------------- */

@Composable
fun NicknameRegisterTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        Text(
            text = "닉네임 등록하기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로 가기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NicknameRegisterBottomBar(enabled: Boolean, onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 60.dp)
    ) {
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) Color.Black else Color(0xFFD6D6D6),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFD6D6D6),
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = enabled
        ) {
            Text(text = "계속하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun DoubleRingProfileImage(imageUrl: String?, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "profilePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Box(
        modifier = Modifier
            .size(280.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(280.dp).scale(pulseScale).border(1.dp, Color(0xFFF5F5F5), CircleShape))
        Box(modifier = Modifier.size(220.dp).scale(pulseScale).border(1.dp, Color(0xFFF5F5F5), CircleShape))
        Box(modifier = Modifier.size(160.dp).clip(CircleShape).background(Color(0xFFEBEBEB)), contentAlignment = Alignment.Center) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(model = imageUrl, contentDescription = "Profile Image", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }
        Box(modifier = Modifier.size(160.dp)) {
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).offset(4.dp, 4.dp).size(40.dp),
                shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 3.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Image, "사진 변경", tint = Color.Black, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun NicknameRegisterScreenPreview() {
    val navController = rememberNavController()
    NicknameRegisterScreen(navController = navController)
}
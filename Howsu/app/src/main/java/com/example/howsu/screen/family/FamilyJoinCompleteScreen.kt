package com.example.howsu.screen.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

// --- [신규] 가족 참여 완료 화면 ---
@Composable
fun FamilyJoinCompleteScreen(
    navController: NavHostController,
    familyName: String,
    encodedProfileUrl: String?
) {
    // URL 디코딩 (null 문자열 처리 포함)
    val decodedProfileUrl = remember(encodedProfileUrl) {
        if (encodedProfileUrl != null && encodedProfileUrl != "null") {
            URLDecoder.decode(encodedProfileUrl, StandardCharsets.UTF_8.toString())
        } else {
            null
        }
    }

    Scaffold(
        bottomBar = {
            // "시작하기" 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 36.dp) // 하단 여백 추가
            ) {
                Button(
                    onClick = {
                        // 홈 화면으로 이동하며 백스택 정리
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                        // 또는 펫 등록이 필요하면 "register_pet"으로 이동
                        // navController.navigate("register_pet") { popUpTo("register_pet") { inclusive = true } }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("시작하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 중앙 정렬
        ) {
            // 가족 이름 표시
            Text(
                text = "$familyName 가족",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 사용자 프로필 + 애니메이션 (기존 컴포저블 재사용)
            DisplayDoubleRingProfile(imageUrl = decodedProfileUrl)

            Spacer(modifier = Modifier.height(40.dp))

            // 참여 완료 메시지
            Text(
                text = "참여 완료!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun FamilyJoinCompleteScreenPreview() {
    val navController = rememberNavController()

    FamilyJoinCompleteScreen(
        navController = navController,
        familyName = "루비네",
        encodedProfileUrl = null // 프로필 이미지가 없는 경우 테스트
    )
}
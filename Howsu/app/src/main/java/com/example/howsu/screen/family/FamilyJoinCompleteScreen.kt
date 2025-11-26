package com.example.howsu.screen.family

import androidx.activity.compose.BackHandler
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
import com.example.howsu.screen.todo.ContentBlack
import com.example.howsu.screen.todo.YellowBox
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun getFormattedFamilyName(name: String): String {
    if (name.isBlank()) return "우리 가족"
    val lastChar = name.last()
    // 한글이 아니면 그냥 "네 가족" 붙임
    if (lastChar < '가' || lastChar > '힣') return "${name}네 가족"

    // 받침 유무 확인 ((문자코드 - 0xAC00) % 28 > 0 이면 받침 있음)
    val hasBatchim = (lastChar.code - 0xAC00) % 28 > 0
    return if (hasBatchim) "${name}이네 가족" else "${name}네 가족"
}


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

    BackHandler(enabled = true) {
        // 텅 비워두면 뒤로 가기가 막힘
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
                        // ★ [수정] 관계 설정 화면으로 이동 (여기선 popUpTo만 살짝)
                        navController.navigate("set_relationship") {
                            // 완료 화면을 스택에서 지워버림 (다시 못 돌아오게)
                            popUpTo("family_join_complete") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YellowBox, // ★ 색상 적용 (노랑)
                        contentColor = ContentBlack // ★ 색상 적용 (검정)
                    )
                ) {
                    Text("계속하기", fontWeight = FontWeight.Medium, fontSize = 15.sp)
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
                text = getFormattedFamilyName(familyName),
                fontSize = 24.sp,
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
                fontSize = 27.sp,
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
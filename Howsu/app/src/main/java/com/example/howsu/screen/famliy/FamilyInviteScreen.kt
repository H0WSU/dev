package com.example.howsu.screen.family

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage

// ★ 삭제됨: FamilyInviteViewModel (이제 필요 없음)

@Composable
fun FamilyInviteScreen(
    navController: NavHostController,
    familyNameInput: String,
    invitedFamilyId: String, // ★ 핵심: 이전 화면에서 생성해서 넘겨준 진짜 ID
    userProfileUrl: String? = null
) {
    // 1. 한글 받침 처리
    val displayName = getFamilyNameWithSuffix(familyNameInput)

    // ★ 수정됨: ViewModel이나 random 생성 없이, 넘겨받은 invitedFamilyId를 바로 사용합니다.

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            InviteTopBar(onBack = { navController.popBackStack() })
        },
        bottomBar = {
            InviteBottomBar(
                onComplete = {
                    // 완료 시 반려동물 등록 화면 등으로 이동
                    navController.navigate("register_pet") // 경로 이름 주의 ("pet_register_screen" -> "register_pet")
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
            Spacer(modifier = Modifier.height(40.dp))

            // --- QR 카드 영역 ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // 1. 카드 배경
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp, bottom = 40.dp, start = 24.dp, end = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 가족 이름
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // QR 코드 이미지
                        QrCodePlaceholder(modifier = Modifier.size(180.dp))
                    }
                }

                // 2. 겹쳐지는 프로필 사진
                ProfileImageCircle(
                    imageUrl = userProfileUrl,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- "또는" 구분선 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
                Text(
                    text = "또는",
                    color = Color(0xFFBDBDBD),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- 아이디 복사 섹션 ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        // ★ 수정됨: invitedFamilyId를 복사
                        clipboardManager.setText(AnnotatedString(invitedFamilyId))
                        Toast.makeText(context, "아이디가 복사되었습니다", Toast.LENGTH_SHORT).show()
                    }
                    .background(Color.White)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "아이디 복사하기",
                        color = Color(0xFF757575),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        // ★ 수정됨: 넘겨받은 ID 표시
                        text = invitedFamilyId,
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- 로직 함수 ---
fun getFamilyNameWithSuffix(name: String): String {
    if (name.isBlank()) return "우리 가족"
    val lastChar = name.last()
    if (lastChar < '가' || lastChar > '힣') return "${name}네 가족"
    val hasBatchim = (lastChar.code - 0xAC00) % 28 > 0
    return if (hasBatchim) "${name}이네 가족" else "${name}네 가족"
}

// --- UI 컴포넌트 (변경 없음) ---
@Composable
fun InviteTopBar(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp).height(40.dp)) {
        Text("가족 초대하기", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
        IconButton(onClick = onBack, modifier = Modifier.size(39.dp).align(Alignment.CenterStart)) {
            Icon(Icons.Default.ArrowBack, "뒤로 가기", modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun InviteBottomBar(onComplete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 60.dp)) {
        Button(
            onClick = onComplete, modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp), enabled = true
        ) { Text("완료하기", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}

@Composable
fun ProfileImageCircle(imageUrl: String?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(CircleShape).background(Color(0xFFEBEBEB)).border(4.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
        if (!imageUrl.isNullOrBlank()) { AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
    }
}

@Composable
fun QrCodePlaceholder(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val pixelSize = size.width / 25
        drawRect(color = Color.Black, size = Size(pixelSize * 7, pixelSize * 7), topLeft = Offset(0f, 0f))
        drawRect(color = Color.Black, size = Size(pixelSize * 7, pixelSize * 7), topLeft = Offset(size.width - pixelSize * 7, 0f))
        drawRect(color = Color.Black, size = Size(pixelSize * 7, pixelSize * 7), topLeft = Offset(0f, size.height - pixelSize * 7))
        for (i in 0..200) {
            val x = (0..24).random() * pixelSize
            val y = (0..24).random() * pixelSize
            drawRect(color = Color.Black, topLeft = Offset(x, y), size = Size(pixelSize * 0.8f, pixelSize * 0.8f))
        }
    }
}

// ★ Preview 수정: invitedFamilyId 파라미터 추가
@Preview(showBackground = true, heightDp = 800)
@Composable
fun FamilyInvitePreview() {
    val navController = rememberNavController()
    FamilyInviteScreen(navController, familyNameInput = "자몽", invitedFamilyId = "sda@1234")
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun FamilyInvitePreviewNoBatchim() {
    val navController = rememberNavController()
    FamilyInviteScreen(navController, familyNameInput = "루비", invitedFamilyId = "test@5678")
}
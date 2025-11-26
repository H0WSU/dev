package com.example.howsu.screen.family

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.howsu.screen.pet.component.RelationChip
import com.example.howsu.screen.todo.ContentBlack
import com.example.howsu.screen.todo.YellowBox

@Composable
fun SetRelationshipScreen(
    navController: NavHostController,
    viewModel: SetRelationshipViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    val relations = listOf("언니", "누나", "형", "오빠", "엄마", "아빠", "이모", "삼촌")

    Scaffold(
        topBar = {
            // ★ 닉네임 화면과 동일한 탑바 스타일
            SetRelationshipTopBar(
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            SetRelationshipBottomBar(
                enabled = uiState.relation.isNotBlank(),
                onNext = {
                    viewModel.saveRelationship {
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }

                            // (선택사항) 애니메이션 깔끔하게
                            launchSingleTop = true
                        }
                    }
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
            Spacer(modifier = Modifier.height(50.dp)) // 상단 여백 좀 더 줌

            // ★ 프로필 애니메이션 적용 (닉네임 화면과 동일)
            DisplayPetProfileWithPulse(imageUrl = uiState.profilePetImageUrl)

            Spacer(modifier = Modifier.height(40.dp))

            // 텍스트 & 선택 칩 영역
            val petName = uiState.petName.ifBlank { "우리 아이" }
            val particle = getSubjectParticle(petName) // 조사 처리 (은/는)

            Text(
                text = "$petName$particle 나를",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium, // Medium 폰트 적용
                color = Color(0xFF424242)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    RelationChip(
                        text = if (uiState.relation.isNotBlank()) uiState.relation else "선택",
                        onClick = { showMenu = true }
                    )

                    // 드롭다운 메뉴
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        relations.forEach { label ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.updateRelation(label)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "(으)로 생각해요",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium, // Medium 폰트 적용
                    color = Color(0xFF424242)
                )
            }
        }
    }
}

// --- 받침 처리 함수 (은/는) ---
fun getSubjectParticle(name: String): String {
    if (name.isBlank()) return "는"
    val lastChar = name.last()
    if (lastChar < '가' || lastChar > '힣') return "는"

    val hasBatchim = (lastChar.code - 0xAC00) % 28 > 0

    // 받침이 있으면 '이는', 없으면 '는'
    return if (hasBatchim) "이는" else "는"
}

// --- [디자인] 탑바 ---
@Composable
fun SetRelationshipTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        // 뒤로가기는 여기서 필요 없을 수도 있지만(가입 완료 후니까), 혹시 몰라 넣어둠
        // 필요 없으면 IconButton 부분 삭제하거나 invisible 처리 가능
        // (보통 완료 후엔 뒤로가기보단 그냥 진행하는 게 맞음)
        // 일단 비워두거나 타이틀만 중앙 정렬

        Text(
            text = "관계 등록하기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// --- [디자인] 하단 버튼 ---
@Composable
fun SetRelationshipBottomBar(enabled: Boolean, onNext: () -> Unit) {
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
                containerColor = if (enabled) YellowBox else Color(0xFFD6D6D6),
                contentColor = ContentBlack,
                disabledContainerColor = Color(0xFFD6D6D6),
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = enabled
        ) {
            Text(text = "시작하기", fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}

// --- [디자인] 펄스 애니메이션 프로필 (펫 버전) ---
@Composable
fun DisplayPetProfileWithPulse(imageUrl: String?) {
    val infiniteTransition = rememberInfiniteTransition(label = "profilePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(280.dp).scale(pulseScale).border(1.dp, Color(0xFFF5F5F5), CircleShape))
        Box(modifier = Modifier.size(220.dp).scale(pulseScale).border(1.dp, Color(0xFFF5F5F5), CircleShape))

        // 펫 이미지 영역
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0xFFEBEBEB)),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Pet Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
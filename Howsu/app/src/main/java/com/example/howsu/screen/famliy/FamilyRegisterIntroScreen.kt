package com.example.howsu.screen.family

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// -------------------------------------------------------------------
// 메인 화면 (로직 및 내비게이션 처리)
// -------------------------------------------------------------------
@Composable
fun FamilyRegisterIntroScreen(
    navController: NavHostController,
    userNickname: String = "이구역의짱",
    userProfileUrl: String? = null,
    viewModel: FamilyRegisterViewModel = viewModel()
) {
    // ViewModel 상태 사용
    val regState = viewModel.regState
    val selectedTab = viewModel.selectedTab

    // 버튼 활성화 로직
    val isNextEnabled = when (regState) {
        FamilyRegState.NONE -> false
        FamilyRegState.SKIP -> true
        FamilyRegState.PRE_DO_IT -> true
        FamilyRegState.DO_IT -> {
            when (selectedTab) {
                FamilyTab.CREATE -> viewModel.inputFamilyName.isNotBlank()
                FamilyTab.JOIN -> viewModel.inputFamilyId.isNotBlank()
            }
        }
    }

    // [공통] 참여 로직 함수 (검색 버튼 & 계속하기 버튼에서 같이 사용)
    val handleJoin = {
        if (viewModel.joinFamily()) {
            // 1. 참여한 가족 이름 (실제로는 DB에서 가져온 값이어야 함)
            val joinedFamilyName = "루비네" // 임시 값

            // 2. 프로필 URL 인코딩 (URL 파라미터로 넘기기 위해 필수)
            val encodedUrl = if (userProfileUrl != null) {
                URLEncoder.encode(userProfileUrl, StandardCharsets.UTF_8.toString())
            } else {
                "null"
            }

            // 3. 참여 완료 화면으로 이동
            navController.navigate("family_join_complete/$joinedFamilyName?profileUrl=$encodedUrl")
        }
    }

    FamilyRegisterContent(
        userNickname = userNickname,
        userProfileUrl = userProfileUrl,
        regState = regState,
        selectedTab = selectedTab,
        // ViewModel 변수 연결
        familyName = viewModel.inputFamilyName,
        familyId = viewModel.inputFamilyId,
        isNextEnabled = isNextEnabled,

        onBack = {
            if (regState == FamilyRegState.DO_IT) {
                viewModel.regState = FamilyRegState.PRE_DO_IT
            } else {
                navController.popBackStack()
            }
        },
        // 값 변경 이벤트 연결
        onRegStateChange = { viewModel.regState = it },
        onTabChange = { viewModel.selectedTab = it },
        onNameChange = { viewModel.inputFamilyName = it },
        onIdChange = { viewModel.inputFamilyId = it },

        // ★ [추가] 검색 아이콘 클릭 시 동작 연결
        onJoinAction = { handleJoin() },

        // 하단 [계속하기] 버튼 클릭 로직
        onNext = {
            when (regState) {
                FamilyRegState.PRE_DO_IT -> {
                    viewModel.regState = FamilyRegState.DO_IT
                }

                FamilyRegState.DO_IT -> {
                    if (selectedTab == FamilyTab.CREATE) {
                        // A. [가족 생성]
                        viewModel.createSharedFamily()
                        val name = viewModel.inputFamilyName
                        val id = viewModel.createdFamilyId
                        navController.navigate("family_invite_screen/$name/$id")
                    } else {
                        // B. [가족 참여] -> 공통 함수 호출
                        handleJoin()
                    }
                }

                FamilyRegState.SKIP -> {
                    // C. [안 할래요] -> 1인 가족 생성
                    viewModel.createSoloFamily(userNickname)
                    navController.navigate("register_pet")
                }

                else -> {}
            }
        }
    )
}

// -------------------------------------------------------------------
// UI 콘텐츠
// -------------------------------------------------------------------
@Composable
fun FamilyRegisterContent(
    userNickname: String,
    userProfileUrl: String?,
    regState: FamilyRegState,
    selectedTab: FamilyTab,
    familyName: String,
    familyId: String,
    isNextEnabled: Boolean,
    onBack: () -> Unit,
    onRegStateChange: (FamilyRegState) -> Unit,
    onTabChange: (FamilyTab) -> Unit,
    onNameChange: (String) -> Unit,
    onIdChange: (String) -> Unit,
    onNext: () -> Unit,
    // ★ [추가] 검색 버튼 클릭 콜백
    onJoinAction: () -> Unit
) {
    Scaffold(
        topBar = { FamilyRegisterTopBar(onBack = onBack) },
        bottomBar = { FamilyRegisterBottomBar(enabled = isNextEnabled, onNext = onNext) },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // 프로필 이미지 (애니메이션 포함)
            DisplayDoubleRingProfile(imageUrl = userProfileUrl)

            Spacer(modifier = Modifier.height(24.dp))

            // 닉네임 & 타이틀
            Text(
                text = "$userNickname 님!",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (regState == FamilyRegState.DO_IT) "소중한 가족을 등록해 볼까요?" else "가족 등록을 하시겠습니까?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF616161)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 선택 영역 vs 폼 영역
            if (regState != FamilyRegState.DO_IT) {
                InitialSelectionButtons(
                    currentState = regState,
                    onSelect = onRegStateChange
                )
            } else {
                FamilyFormContent(
                    selectedTab = selectedTab,
                    onTabChange = onTabChange,
                    familyName = familyName,
                    onNameChange = onNameChange,
                    familyId = familyId,
                    onIdChange = onIdChange,
                    // ★ [연결] 검색 버튼 클릭 시 실행
                    onJoinClick = onJoinAction
                )
            }
        }
    }
}

/* -----------------------------------------------------------------------
   하위 컴포넌트들
   ----------------------------------------------------------------------- */

@Composable
fun InitialSelectionButtons(
    currentState: FamilyRegState,
    onSelect: (FamilyRegState) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SelectableActionBox(
            text = "할래요",
            isSelected = currentState == FamilyRegState.PRE_DO_IT,
            onClick = { onSelect(FamilyRegState.PRE_DO_IT) }
        )
        SelectableActionBox(
            text = "안 할래요",
            isSelected = currentState == FamilyRegState.SKIP,
            onClick = { onSelect(FamilyRegState.SKIP) }
        )
    }
}

@Composable
fun SelectableActionBox(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) Color.Black else Color(0xFFEAEAEA)
    val textColor = if (isSelected) Color.Black else Color(0xFFBDBDBD)
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    Box(
        modifier = Modifier
            .width(150.dp)
            .height(52.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
    }
}

@Composable
fun FamilyFormContent(
    selectedTab: FamilyTab,
    onTabChange: (FamilyTab) -> Unit,
    familyName: String,
    onNameChange: (String) -> Unit,
    familyId: String,
    onIdChange: (String) -> Unit,
    // ★ [추가] 검색 아이콘 클릭 이벤트
    onJoinClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabButton("생성하기", selectedTab == FamilyTab.CREATE, Modifier.weight(1f)) { onTabChange(FamilyTab.CREATE) }
            TabButton("참여하기", selectedTab == FamilyTab.JOIN, Modifier.weight(1f)) { onTabChange(FamilyTab.JOIN) }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (selectedTab == FamilyTab.CREATE) {
            Text(
                text = "가족 별칭을 입력해 주세요",
                fontSize = 16.sp, color = Color(0xFF757575), textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = familyName, onValueChange = onNameChange, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예) 루비", color = Color(0xFFBDBDBD)) },
                singleLine = true, shape = RoundedCornerShape(12.dp), colors = familyInputColors(),
                trailingIcon = { Text("가족", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(end = 16.dp)) }
            )
        } else {
            Text(
                text = "가족 아이디를 입력해 주세요",
                fontSize = 16.sp, color = Color(0xFF757575), textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = familyId, onValueChange = onIdChange, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("아이디로 참여하기", color = Color(0xFFBDBDBD)) },
                singleLine = true, shape = RoundedCornerShape(12.dp), colors = familyInputColors(),
                trailingIcon = {
                    // ★ [수정] 검색 아이콘 클릭 시 onJoinClick 실행
                    IconButton(
                        onClick = {
                            if (familyId.isNotBlank()) {
                                onJoinClick()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Search, "검색", tint = Color.Black, modifier = Modifier.padding(end = 8.dp))
                    }
                }
            )
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val containerColor = if (isSelected) Color.White else Color(0xFFFAFAFA)
    val borderColor = if (isSelected) Color.Black else Color(0xFFEEEEEE)
    val contentColor = if (isSelected) Color.Black else Color(0xFFBDBDBD)
    Button(
        onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor), elevation = null
    ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
}

@Composable
fun familyInputColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
    focusedBorderColor = Color.Black, unfocusedBorderColor = Color(0xFFE0E0E0), cursorColor = Color.Black
)

@Composable
fun FamilyRegisterTopBar(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp).height(40.dp)) {
        Text("가족 등록하기", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
        IconButton(onClick = onBack, modifier = Modifier.size(39.dp).align(Alignment.CenterStart)) {
            Icon(Icons.Default.ArrowBack, "뒤로 가기", modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun FamilyRegisterBottomBar(enabled: Boolean, onNext: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 60.dp)) {
        Button(
            onClick = onNext, modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) Color.Black else Color(0xFFD6D6D6),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFD6D6D6), disabledContentColor = Color.White
            ), shape = RoundedCornerShape(12.dp), enabled = enabled
        ) { Text("계속하기", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}

@Composable
fun DisplayDoubleRingProfile(imageUrl: String?) {
    val infiniteTransition = rememberInfiniteTransition(label = "profilePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale"
    )
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(200.dp).scale(pulseScale).border(1.dp, Color(0xFFF5F5F5), CircleShape))
        Box(modifier = Modifier.size(160.dp).scale(pulseScale).border(1.dp, Color(0xFFF5F5F5), CircleShape))
        Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFEBEBEB)), contentAlignment = Alignment.Center) {
            if (!imageUrl.isNullOrBlank()) { AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun PreviewFamilyRegister() {
    val navController = rememberNavController()
    FamilyRegisterIntroScreen(navController = navController)
}
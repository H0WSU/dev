package com.example.howsu.screen.family

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// -------------------------------------------------------------------
// 메인 화면
// -------------------------------------------------------------------
@Composable
fun FamilyRegisterIntroScreen(
    navController: NavHostController,
    userNickname: String = "이구역의짱",
    userProfileUrl: String? = null,
    viewModel: FamilyRegisterViewModel = viewModel()
) {
    val regState = viewModel.regState
    val selectedTab = viewModel.selectedTab
    val context = LocalContext.current

    // ★ QR 스캔 결과 처리 런처
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            // 스캔된 내용(가족ID)을 입력창에 자동으로 넣음
            viewModel.inputFamilyId = result.contents
            Toast.makeText(context, "스캔 완료: ${result.contents}", Toast.LENGTH_SHORT).show()
        }
    }

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

    // 공통 참여 로직
    val handleJoin = {
        if (viewModel.joinFamily()) {
            val joinedFamilyName = "루비네" // 실제로는 DB에서 가져온 이름
            val encodedUrl = if (userProfileUrl != null) {
                URLEncoder.encode(userProfileUrl, StandardCharsets.UTF_8.toString())
            } else {
                "null"
            }
            navController.navigate("family_join_complete/$joinedFamilyName?profileUrl=$encodedUrl")
        }
    }

    FamilyRegisterContent(
        userNickname = userNickname,
        userProfileUrl = userProfileUrl,
        regState = regState,
        selectedTab = selectedTab,
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
        onRegStateChange = { viewModel.regState = it },
        onTabChange = { viewModel.selectedTab = it },
        onNameChange = { viewModel.inputFamilyName = it },
        onIdChange = { viewModel.inputFamilyId = it },

        onJoinAction = { handleJoin() },

        // ★ QR 아이콘 클릭 시 카메라 실행
        onQrScanClick = {
            val options = ScanOptions()
            options.setPrompt("QR 코드를 사각형 안에 비춰 주세요")
            options.setBeepEnabled(false)

            // 아까 맨 밑에 만든 클래스를 쓰겠다고 지정
            options.setCaptureActivity(PortraitCaptureActivity::class.java)

            // 방향 잠금 (이제 세로로 고정됨)
            options.setOrientationLocked(true)

            scanLauncher.launch(options)
        },

        onNext = {
            when (regState) {
                FamilyRegState.PRE_DO_IT -> viewModel.regState = FamilyRegState.DO_IT
                FamilyRegState.DO_IT -> {
                    if (selectedTab == FamilyTab.CREATE) {
                        viewModel.createSharedFamily()
                        val name = viewModel.inputFamilyName
                        val id = viewModel.createdFamilyId
                        navController.navigate("family_invite_screen/$name/$id")
                    } else {
                        handleJoin()
                    }
                }
                FamilyRegState.SKIP -> {
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
    onJoinAction: () -> Unit,
    onQrScanClick: () -> Unit
) {
    Scaffold(
        topBar = {
            FamilyRegisterTopBar(
                onBack = onBack,
                // 참여하기 탭일 때만 QR 아이콘 표시
                showQrIcon = (regState == FamilyRegState.DO_IT && selectedTab == FamilyTab.JOIN),
                onQrClick = onQrScanClick
            )
        },
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
            DisplayDoubleRingProfile(imageUrl = userProfileUrl)
            Spacer(modifier = Modifier.height(24.dp))
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

            if (regState != FamilyRegState.DO_IT) {
                InitialSelectionButtons(currentState = regState, onSelect = onRegStateChange)
            } else {
                FamilyFormContent(
                    selectedTab = selectedTab,
                    onTabChange = onTabChange,
                    familyName = familyName,
                    onNameChange = onNameChange,
                    familyId = familyId,
                    onIdChange = onIdChange,
                    onJoinClick = onJoinAction
                )
            }
        }
    }
}

@Composable
fun FamilyRegisterTopBar(onBack: () -> Unit, showQrIcon: Boolean, onQrClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp).height(40.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.size(39.dp).align(Alignment.CenterStart)) {
            Icon(Icons.Default.ArrowBack, "뒤로 가기", modifier = Modifier.size(24.dp))
        }
        Text("가족 등록하기", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))

        // QR 아이콘 표시 여부 확인
        if (showQrIcon) {
            IconButton(onClick = onQrClick, modifier = Modifier.size(39.dp).align(Alignment.CenterEnd)) {
                Icon(Icons.Default.QrCodeScanner, "QR 스캔", modifier = Modifier.size(24.dp), tint = Color.Black)
            }
        }
    }
}

@Composable
fun InitialSelectionButtons(currentState: FamilyRegState, onSelect: (FamilyRegState) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SelectableActionBox("할래요", currentState == FamilyRegState.PRE_DO_IT) { onSelect(FamilyRegState.PRE_DO_IT) }
        SelectableActionBox("안 할래요", currentState == FamilyRegState.SKIP) { onSelect(FamilyRegState.SKIP) }
    }
}

@Composable
fun SelectableActionBox(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) Color.Black else Color(0xFFEAEAEA)
    val textColor = if (isSelected) Color.Black else Color(0xFFBDBDBD)
    Box(
        modifier = Modifier.width(150.dp).height(52.dp).border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(30.dp)).clip(RoundedCornerShape(30.dp)).clickable { onClick() }.background(Color.White),
        contentAlignment = Alignment.Center
    ) { Text(text, fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp) }
}

@Composable
fun FamilyFormContent(selectedTab: FamilyTab, onTabChange: (FamilyTab) -> Unit, familyName: String, onNameChange: (String) -> Unit, familyId: String, onIdChange: (String) -> Unit, onJoinClick: () -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TabButton("생성하기", selectedTab == FamilyTab.CREATE, Modifier.weight(1f)) { onTabChange(FamilyTab.CREATE) }
            TabButton("참여하기", selectedTab == FamilyTab.JOIN, Modifier.weight(1f)) { onTabChange(FamilyTab.JOIN) }
        }
        Spacer(modifier = Modifier.height(32.dp))
        if (selectedTab == FamilyTab.CREATE) {
            Text("가족 별칭을 입력해 주세요", fontSize = 16.sp, color = Color(0xFF757575), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            OutlinedTextField(value = familyName, onValueChange = onNameChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("예) 루비", color = Color(0xFFBDBDBD)) }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = familyInputColors(), trailingIcon = { Text("가족", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(end = 16.dp)) })
        } else {
            Text("가족 아이디를 입력해 주세요", fontSize = 16.sp, color = Color(0xFF757575), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            OutlinedTextField(value = familyId, onValueChange = onIdChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("아이디로 참여하기", color = Color(0xFFBDBDBD)) }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = familyInputColors(), trailingIcon = { IconButton(onClick = { if (familyId.isNotBlank()) onJoinClick() }) { Icon(Icons.Default.Search, "검색", tint = Color.Black, modifier = Modifier.padding(end = 8.dp)) } })
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(26.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color.White else Color(0xFFFAFAFA), contentColor = if (isSelected) Color.Black else Color(0xFFBDBDBD)), border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isSelected) Color.Black else Color(0xFFEEEEEE)), elevation = null) { Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
}

@Composable
fun familyInputColors() = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = Color.Black, unfocusedBorderColor = Color(0xFFE0E0E0), cursorColor = Color.Black)

@Composable
fun FamilyRegisterBottomBar(enabled: Boolean, onNext: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 60.dp)) {
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = if (enabled) Color.Black else Color(0xFFD6D6D6), contentColor = Color.White, disabledContainerColor = Color(0xFFD6D6D6), disabledContentColor = Color.White), shape = RoundedCornerShape(12.dp), enabled = enabled) { Text("계속하기", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}

@Composable
fun DisplayDoubleRingProfile(imageUrl: String?) {
    val infiniteTransition = rememberInfiniteTransition(label = "profilePulse")
    val pulseScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.05f, animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(200.dp).scale(pulseScale).border(1.dp, Color(0xFFF5F5F5), CircleShape))
        Box(modifier = Modifier.size(160.dp).scale(pulseScale).border(1.dp, Color(0xFFF5F5F5), CircleShape))
        Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFEBEBEB)), contentAlignment = Alignment.Center) { if (!imageUrl.isNullOrBlank()) { AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
    }
}

class PortraitCaptureActivity : CaptureActivity()

@Preview(showBackground = true, heightDp = 800)
@Composable
fun PreviewFamilyRegister() {
    val navController = rememberNavController()
    FamilyRegisterIntroScreen(navController = navController)
}
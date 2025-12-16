package com.example.howsu.screen.mypage

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.howsu.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.howsu.screen.family.PortraitCaptureActivity // ⭐️ QR 스캔을 위해 필요
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDetailScreen(
    navController: NavHostController,
    viewModel: FamilyDetailScreenViewModel = viewModel()
){
    // 상태 관찰
    val familyId by viewModel.familyId.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val familyName by viewModel.familyName.collectAsState()
    val joinStatus by viewModel.joinStatus.collectAsState()

    // 가입 상태
    var inputFamilyCode by remember { mutableStateOf("") }

    // UI 피드백 위함
    val context = LocalContext.current

    // --- 가족 참여 성공 시 네비게이션 로직 ---
    val navigateToComplete = { joinedName: String, profileUrl: String? ->
        val encodedUrl = if (profileUrl != null) {
            URLEncoder.encode(profileUrl, StandardCharsets.UTF_8.toString())
        } else {
            "null"
        }
        // 경로: family_join_complete/{familyName}?profileUrl={profileUrl}
        navController.navigate("family_join_complete/$joinedName?profileUrl=$encodedUrl") {
            // 마이페이지에서 참여했으므로 이전 화면(가족 정보)으로 돌아가지 않도록 설정
            popUpTo("family_info") { inclusive = true }
        }
    }

    // --- [수동 참여] 버튼 클릭 액션 ---
    val handleManualJoin = { code: String ->
        viewModel.joinFamily(
            targetFamilyId = code,
            onSuccess = { realName, profileUrl ->
                Toast.makeText(context, "가족 가입 성공!", Toast.LENGTH_SHORT).show()
                inputFamilyCode = "" // 입력 필드 초기화
                // 네비게이션 수행
                navigateToComplete(realName, profileUrl)
            }
            // onFailure는 뷰모델의 _joinStatus로 처리되므로, 별도 처리 불필요
        )
    }

    // --- [QR 스캔 참여] 런처 설정 ---
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val scannedId = result.contents
            Toast.makeText(context, "QR 인식됨", Toast.LENGTH_SHORT).show()

            // 뷰모델 함수 호출 (QR 스캔 결과로 바로 가입 시도)
            viewModel.joinFamily(
                targetFamilyId = scannedId,
                onSuccess = { realName, profileUrl ->
                    Toast.makeText(context, "QR 가입 성공!", Toast.LENGTH_SHORT).show()
                    navigateToComplete(realName, profileUrl)
                }
                // onFailure는 뷰모델의 _joinStatus로 처리
            )
        }
    }


    // 가입 상태 변화 시 토스트 메시지 및 네비게이션 처리
    LaunchedEffect(joinStatus) {
        when(val status = joinStatus){
            is JoinStatus.Success -> {
                // Success 시 네비게이션은 콜백(onSuccess)에서 처리
                viewModel.resetJoinStatus() // 상태 초기화
            }
            is JoinStatus.Error -> {
                Toast.makeText(context, "가입 실패 : ${status.message}", Toast.LENGTH_SHORT).show()
                viewModel.resetJoinStatus() // 실패 시 상태 초기화
            }
            else -> {

            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = "가족 정보 확인하기",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "되돌아가기")
                    }
                },
                actions = {

                    // ⭐QR 스캔 버튼 추가
                    IconButton(onClick = {
                        val options = ScanOptions()
                        options.setPrompt("가족 초대 QR 코드를 스캔하세요")
                        options.setBeepEnabled(false)
                        options.setOrientationLocked(true)
                        options.setCaptureActivity(PortraitCaptureActivity::class.java)
                        scanLauncher.launch(options)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.qr),
                            contentDescription = "QR로 가족 참여",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.padding(10.dp)
            )
        }
    ){ paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item{
                FamilyIdSearchBar(
                    inputFamilyCode = inputFamilyCode,
                    onFamilyCodeChange = { inputFamilyCode = it },
                    onJoinClick = {
                        // 뷰모델의 가족 가입 함수 호출
                        handleManualJoin(inputFamilyCode)
                    },
                    isLoading = joinStatus is JoinStatus.Loading
                )
            }

            item {
                if (familyMembers.isNotEmpty()) {
                    // ★ [추가] 이름 뒤에 '네'/'이네' 붙이는 로직
                    val displayName = remember(familyName) {
                        if (familyName.isBlank()) ""
                        else {
                            val lastChar = familyName.last()
                            val hasBatchim = if (lastChar.code in 0xAC00..0xD7A3) {
                                (lastChar.code - 0xAC00) % 28 > 0
                            } else {
                                false
                            }
                            if (hasBatchim) "${familyName}이네 가족" else "${familyName}네 가족"
                        }
                    }

                    Text(
                        text = displayName, // ★ 수정됨 ("${familyName}" -> displayName)
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
            }

            items(items = familyMembers, key = { it.userId }) { member ->
                FamilyMemberCard(
                    member = member,
                    viewModel = viewModel
                )
            }
        }

    }
}

@Composable
fun FamilyMemberCard(
    member: DisplayFamilyMember,
    viewModel: FamilyDetailScreenViewModel
) {
    //val isRemovable = !member.isManager

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 각 가족 구성원의 프로필
            val painter = rememberAsyncImagePainter(
                model = member.profileImageUrl
                    ?: "URL_for_default_profile_image" // 실제 기본 이미지 URL로 대체
            )

            Image(
                painter = painter,
                contentDescription = "${member.nickName}의 프로필",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray) // 로딩 중/이미지 없을 때 배경
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. 각 가족 구성원의 nickname 및 relationship
            Column(
                modifier = Modifier.weight(1f) // 남은 공간을 차지하도록 설정
            ) {
                // 각 가족 구성원의 nickname
                Text(
                    text = member.nickName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                // 각 가족 구성원의 relationship
                Text(
                    text = "${member.relationship}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. 가족 구성원 삭제 버튼
            // 방장이거나 본인 계정인 경우에만 표시하도록 로직 추가 필요
            IconButton(
                onClick = { viewModel.removeFamilyMember(member.userId)},
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "구성원 삭제",
                    tint = Color.Red.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun FamilyIdSearchBar(
    inputFamilyCode: String,
    onFamilyCodeChange: (String) -> Unit,
    onJoinClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            // 1. 가로는 여전히 넓게(20dp), 바깥 위아래 간격은 적당히(10dp)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .border(
                width = 2.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(15.dp)
            ),
        shape = RoundedCornerShape(15.dp), // Surface 모양도 둥글게 일치시킴
        color = Color.White
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputFamilyCode,
                onValueChange = onFamilyCodeChange,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 12.dp, bottom = 12.dp),
                textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),

                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (inputFamilyCode.isEmpty()) {
                            Text(
                                text = "아이디로 참여하기",
                                color = Color.Gray.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            IconButton(
                onClick = onJoinClick,
                enabled = inputFamilyCode.isNotBlank() && !isLoading,
                modifier = Modifier.size(40.dp) // 버튼 크기도 살짝 키워서 균형 맞춤
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "가족 참여",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}
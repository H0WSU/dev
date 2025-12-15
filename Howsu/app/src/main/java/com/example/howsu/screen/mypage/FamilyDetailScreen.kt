package com.example.howsu.screen.mypage

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
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

    // 가입 상태 변화 시 토스트 메시지 및 네비게이션 처리
    LaunchedEffect(joinStatus) {
        when(val status = joinStatus){
            is JoinStatus.Success -> {
                Toast.makeText(context, "가족 가입 성공", Toast.LENGTH_SHORT).show()
                inputFamilyCode = ""
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
                    // 가족 초대 QR 생성 화면으로 이동하는 버튼 추가
                    IconButton(onClick = {
                        val id = familyId // familyId 상태 값 사용

                        if (id.isNotBlank()) {
                            // 1. familyName을 URL 인코딩
                            val encodedFamilyName = URLEncoder.encode(familyName, StandardCharsets.UTF_8.toString())

                            // 2. 올바른 경로로 navigate (familyId는 이미 인코딩 불필요)
                            // profileUrl은 임시로 null 대신 "null" 문자열로 전달 (NavHost의 정의에 따름)
                            val route = "invite_family/$encodedFamilyName/$id?profileUrl=null&isFromMypage=true"

                            navController.navigate(route)
                        }
                    }) {
                        // QR 코드 스캐너 아이콘을 사용하여 '초대'의 의미를 전달
                        Icon(
                            Icons.Filled.QrCodeScanner, // QR 아이콘 사용
                            contentDescription = "가족 초대",
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
                        // 뷰모델의 가족 가입 함수 호출 (familyCode만 전달)
                        viewModel.joinFamily(inputFamilyCode)
                    },
                    isLoading = joinStatus is JoinStatus.Loading
                )
            }

            item {
                if (familyMembers.isNotEmpty()) {
                    Text(
                        text = "${familyName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
            }

            // ⭐️ 수정 후 올바르게 참조됨
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
        .padding(horizontal = 40.dp, vertical = 20.dp)
        .border(
            width = 2.dp, // 테두리 두께, 원하는 대로 조절
            color = Color.LightGray, // 테두리 색상, 원하는 색상으로 변경 가능
            shape = RoundedCornerShape(15.dp) // 둥근 모서리 모양 지정
        ),
        shape = MaterialTheme.shapes.medium,
        color = Color.White
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputFamilyCode,
                onValueChange = onFamilyCodeChange,
                singleLine = true,
                modifier = Modifier
                    .weight(1f) // 남은 공간 채우기
                    .padding(vertical = 12.dp),
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
                        innerTextField() // 실제 입력 필드
                    }
                }
            )
            IconButton(
                onClick = onJoinClick,
                enabled = inputFamilyCode.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    // 로딩 중일 때 로딩 인디케이터 표시
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    // 검색 아이콘을 가입 버튼으로 사용
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "가족 참여",
                    )
                }
            }
        }
    }
}
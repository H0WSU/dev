package com.example.howsu.screen.mypage

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton


// ----------------------------------------------------
// 프로필 영역
// ----------------------------------------------------
@Composable
fun Profile(
    profileImageUrl: String? = null,
    username: String,
    email: String,
    onEditClick: () -> Unit,
){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .border(
                width = 1.dp, // 테두리 두께, 원하는 대로 조절
                color = Color.LightGray, // 테두리 색상, 원하는 색상으로 변경 가능
                shape = RoundedCornerShape(15.dp) // 둥근 모서리 모양 지정
            ),
        shape = MaterialTheme.shapes.medium,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp, // 테두리 두께
                        color = Color.LightGray,
                        shape = CircleShape // 원형 테두리
                    )
            ){
                // 프로필 이미지 로드 로직
                AsyncImage(
                    model = profileImageUrl, // 로드할 이미지 URL
                    contentDescription = "프로필 이미지",
                    modifier = Modifier.fillMaxSize(),
                    // 이미지가 원형 Box에 꽉 차도록 설정
                    contentScale = ContentScale.Crop,

                )
                if (profileImageUrl.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "기본 프로필",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = username,  // 닉네임
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = email,  // 이메일
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // 수정 버튼
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Create, contentDescription = "내 정보 수정")
            }
        }
    }
}

// ----------------------------------------------------
// mypage 전체 화면
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MypageScreen(
    navController: NavHostController,
    viewModel: MypageViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
){
    val myFamilyName by viewModel.familyName.collectAsState()
    val myFamilyId by viewModel.familyId.collectAsState()

    // 뷰모델에서 프로필 URL 관찰
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val myProfileUrl by viewModel.myProfileUrl.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "마이페이지",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile")}) {
                        Icon(Icons.Filled.Settings, contentDescription = "설정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {MyBottomNavigationBar(navController = navController)},
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = {
                    navController.navigate("create_todo")
                },
                onScheduleClick = {
                    navController.navigate("create_schedule")
                },
                onFeedCreateClick = {
                    navController.navigate("create_feed")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Profile(
                username = userName,
                email = userEmail,
                profileImageUrl = myProfileUrl,
                onEditClick = { navController.navigate("edit_profile") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                ContentItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "반려동물 추가") },
                    text = "반려동물 추가하기",
                    onClick = { navController.navigate("register_pet") }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))

                ContentItem(
                    icon = { Icon(Icons.Default.People, contentDescription = "가족 정보") },
                    text = "가족 정보 확인하기",
                    onClick = { navController.navigate("family_info") }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))

                ContentItem(
                    icon = {
                        Icon(
                            Icons.Default.GroupAdd,
                            contentDescription = "가족 초대"
                        )
                    },
                    text = "가족 초대하기",
                    onClick = {
                        // URL 인코딩 처리 (URL에 특수문자가 있어서) 
                        val encodedUrl = if (myProfileUrl != null) {
                            java.net.URLEncoder.encode(myProfileUrl, java.nio.charset.StandardCharsets.UTF_8.toString())
                        } else {
                            "null"
                        }

                        // 경로에 profileUrl 추가해서 이동
                        navController.navigate("invite_family/$myFamilyName/$myFamilyId?profileUrl=$encodedUrl&isFromMypage=true")
                    }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))

                ContentItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "공지사항") },
                    text = "공지사항",
                    onClick = { navController.navigate("notice") }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))

                ContentItem(
                    icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "자주 묻는 질문") },
                    text = "자주 묻는 질문",
                    onClick = { navController.navigate("faq") }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))

                /*ContentItem(
                    icon = { Icon(Icons.Default.Task, contentDescription = "문의하기") }, // Task 아이콘 가정
                    text = "문의하기",
                    onClick = { navController.navigate("contact") }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))*/
            }

        }

    }
}


// ----------------------------------------------------
// 본문 영역
// ----------------------------------------------------
@Composable
fun ContentItem(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable (onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "세부 내용으로 이동",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

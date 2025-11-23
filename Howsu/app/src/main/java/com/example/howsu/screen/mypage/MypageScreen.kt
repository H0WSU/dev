package com.example.howsu.screen.mypage

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton


// ----------------------------------------------------
// mypage 전체 화면
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MypageScreen(
    navController: NavHostController,
){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .padding(7.dp),
                        text = "마이페이지",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /*설정 화면 이동*/ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "내 정보 수정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
                username = "이구역의짱",
                email = "abc123@gmail.com",
                onEditClick = { navController.navigate("edit_profile") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                ContentItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "반려동물 추가") },
                    text = "반려동물 추가하기",
                    onClick = { navController.navigate("add_pet") }
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
                    }, // GroupAdd 아이콘 가정
                    text = "가족 초대하기",
                    onClick = { navController.navigate("invite_family") }
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

                ContentItem(
                    icon = { Icon(Icons.Default.Task, contentDescription = "문의하기") }, // Task 아이콘 가정
                    text = "문의하기",
                    onClick = { navController.navigate("contact") }
                )
                Divider(modifier = Modifier.padding(horizontal = 25.dp))
            }

        }

    }
}

// ----------------------------------------------------
// 프로필 영역
// ----------------------------------------------------
@Composable
fun Profile(
    //profileImageUrl: String,
    username: String,
    email: String,
    onEditClick: () -> Unit,
){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ){
                // 이미지
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = username,  // 닉네임
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = email,  // 이메일
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

// ----------------------------------------------------
// Preview 함수
// ----------------------------------------------------

@Preview(showBackground = true)
@Composable
fun MypageScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        MypageScreen(
            navController = navController,
        )
    }
}
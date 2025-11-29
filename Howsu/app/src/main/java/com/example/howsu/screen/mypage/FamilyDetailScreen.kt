package com.example.howsu.screen.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDetailScreen(
    navController: NavHostController,
    viewModel: FamilyDetailScreenViewModel = viewModel()
){
    // ⭐️ familyId 관찰하여 사용하는 것이 좋습니다.
    val familyId by viewModel.familyId.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()  // 상태 관찰
    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("가족 정보 확인하기", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "취소")
                    }
                },
                actions = {
                    // 가족 초대 가능한 QR인식 창으로 넘어가는 버튼 만들것임
                }
            )
        }
    ){ paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item{/* 부여된 고유 가족 아이디로 가족 초대 할 수 있는 창*/}

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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
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
                    color = MaterialTheme.colorScheme.primary
                )

                // 각 가족 구성원의 relationship
                Text(
                    text = "(${member.relationship})",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. 가족 구성원 삭제 버튼
            // 방장이거나 본인 계정인 경우에만 표시하도록 로직 추가 필요
            IconButton(
                onClick = { /* TODO: 가족 구성원 삭제 로직 (ViewModel 함수 호출) */ },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "구성원 삭제",
                    tint = Color.Red.copy(alpha = 0.6f)
                )
            }
        }
    }
}
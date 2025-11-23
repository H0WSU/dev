package com.example.howsu.screen.mypage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar


data class Notice(
    val id: Int,
    val title: String,
    val date: String,
    val content: String
)

val sampleNotices = listOf(  // sample data
    Notice(
        id = 1,
        title = "[필독] 서비스 점검 및 업데이트 안내 (11/25)",
        date = "2025.11.20",
        content = """
            안녕하세요, HowSu 팀입니다.

            보다 안정적이고 향상된 서비스를 제공하기 위해 정기 시스템 점검 및 업데이트를 진행할 예정입니다.
            이용에 불편함이 없도록 최선을 다하겠습니다.

            1. 점검 일시:
            2025년 11월 25일 (월) 02:00 ~ 05:00 (총 3시간)

            2. 점검 내용:
            * 서버 안정화 작업
            * 새로운 기능 추가 (예: 다크 모드 지원)
            * 일부 버그 수정

            3. 유의사항:
            점검 시간 동안 앱 접속 및 일부 서비스 이용이 일시적으로 제한될 수 있습니다.
            점검 시간은 작업 진행 상황에 따라 다소 변경될 수 있습니다.

            이용에 불편을 드려 죄송하며, 항상 더 나은 서비스를 제공하기 위해 노력하겠습니다.

            감사합니다.
            HowSu 드림
        """.trimIndent()
    ),
    Notice(
        id = 2,
        title = "신규 반려동물 등록 기능 개선 사항 안내",
        date = "2025.11.15",
        content = "신규 반려동물 등록 시 사진 업로드 속도가 개선되었으며, 품종 검색 기능이 더욱 정확해졌습니다."
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavHostController,
){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "공지사항",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.popBackStack()}){  // 전 페이지로 이동
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {MyBottomNavigationBar(navController = navController)}
    ){ paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
        ) {
            items(sampleNotices) { notice ->
                ExpandableNoticeItem(notice = notice)
            }
        }
    }
}

@Composable
fun ExpandableNoticeItem(notice: Notice) {
    var isExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{ isExpanded = !isExpanded }
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ){
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ){
            Column(modifier = Modifier.weight(1f)){  // 제목 첫 줄만
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = if(isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = notice.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant

                )
            }

            val icon = if(isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
            Icon(
                imageVector = icon,
                contentDescription = if(isExpanded) "닫기" else "펼치기",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        if(isExpanded){
            Spacer(modifier = Modifier.padding(8.dp))
            Divider()
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
    Divider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        NotificationScreen(
            navController = navController
        )
    }
}
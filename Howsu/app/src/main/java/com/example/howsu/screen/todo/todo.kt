package com.example.howsu.screen.todo

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.howsu.R
import com.example.howsu.ui.theme.HowsuTheme

data class Task(
    val id: Int,
    val title: String,
    val date: String,
    val isChecked: Boolean
)

data class TodoGroup(
    val id: Int,
    val assigneeName: String,
    @DrawableRes val assigneeProfileRes: Int? = null,
    val tasks: List<Task>
)

val dummyTodoGroups = listOf(
    TodoGroup(
        id = 1,
        assigneeName = "언니(이)가",
        assigneeProfileRes = null,
        tasks = listOf(
            Task(101, "츄르 사 오기", "2025. 10. 29", false)
        )
    ),
    TodoGroup(
        id = 2,
        assigneeName = "엄마(이)가",
        assigneeProfileRes = null,
        tasks = listOf(
            Task(102, "목욕시키기", "2025. 10. 28", false)
        )
    )
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen() {
    Scaffold(
        // --- 상단 앱 바 ---
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Todo",
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = { /* 캘린더 클릭 */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.date_under),
                            contentDescription = "캘린더"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        // --- 하단 네비게이션 바 ---
        bottomBar = {
            var selectedItem by remember { mutableStateOf(0) }

            val items: List<Pair<String, Any>> = listOf(
                "Home" to R.drawable.home_under,
                "Calendar" to R.drawable.date_under,
                "Todo" to R.drawable.todo_under,
                "Feed" to R.drawable.feed_under,
                "Profile" to R.drawable.user_under,
            )

            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        // (수정됨!) when 구문으로 아이콘 타입(Int, ImageVector) 검사
                        icon = {
                            val label = item.first
                            when (val iconData = item.second) {
                                // 1. 타입이 ImageVector일 경우 (Add, Profile)
                                is ImageVector -> {
                                    Icon(iconData, contentDescription = label)
                                }
                                // 2. 타입이 Int일 경우 (Home, Calendar, Tasks)
                                is Int -> {
                                    Icon(
                                        painter = painterResource(id = iconData),
                                        contentDescription = label
                                    )
                                }
                            }
                        },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        },
        // --- 플로팅 액션 버튼 ---
        floatingActionButton = {
            FloatingActionButton(onClick = { /* 새 할 일 추가 */ }) {
                Icon(Icons.Default.Add, contentDescription = "할 일 추가")
            }
        }
    ) { innerPadding ->
        // --- 6. 본문 (할 일 목록) ---
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyTodoGroups) { group ->
                TodoGroupCard(group = group)
            }
        }
    }
}

// --- 7. 할 일 그룹 카드 Composable ---

@Composable
fun TodoGroupCard(group: TodoGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 헤더 (담당자)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.assigneeName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))

                if (group.assigneeProfileRes != null) {
                    Image(
                        painter = painterResource(id = group.assigneeProfileRes),
                        contentDescription = "${group.assigneeName} 프로필",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // null일 때는(이미지가 없을 때) 32dp짜리 빈 공간만 표시
                    Spacer(modifier = Modifier.size(32.dp))
                }

                IconButton(onClick = { /* '...' 메뉴 클릭 */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "더보기",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 작업 목록
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.tasks.forEach { task ->
                    TaskItemRow(task = task)
                }
            }
        }
    }
}

// --- 8. 개별 할 일 항목 Composable ---

@Composable
fun TaskItemRow(task: Task) {
    var isChecked by remember { mutableStateOf(task.isChecked) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = task.title,
            modifier = Modifier.padding(start = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = task.date,
            fontSize = 12.sp,
            color = Color.LightGray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodoScreenPreview() {
    HowsuTheme {
        TodoScreen()
    }
}
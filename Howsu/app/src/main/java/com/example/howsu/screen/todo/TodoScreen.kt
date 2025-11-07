package com.example.howsu.screen.todo

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
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
fun TodoScreen(navController: NavHostController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                            contentDescription = "캘린더",
                            modifier = Modifier.size(24.dp)
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
            MyBottomNavigationBar(navController = navController)
        },

        // --- 플로팅 버튼 ---
        floatingActionButton = {
            MyFloatingActionButton(
                onClick = {
                    // TODO: '+ 버튼' 클릭 시 할 일 (예: 새 화면으로 이동)
                    navController.navigate("create_todo")
                }
            )
        }
    ) { innerPadding ->
        // --- 6. 본문 (할 일 목록) ---
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(dummyTodoGroups) { group ->
                TodoGroupCard(group = group)
            }
        }
    }
}
@Composable
fun TodoGroupCard(group: TodoGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            // --- 1. 헤더 (담당자 정보) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 4.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    // 텍스트 스타일 분리
                    text = buildAnnotatedString {
                        // "언니" (SemiBold, 14sp)
                        val mainName = group.assigneeName.substringBefore("(", group.assigneeName)
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        ) {
                            append(mainName)
                        }

                        // "(이)가" (Medium, 10sp)
                        if (group.assigneeName.contains("(")) {
                            val particle = "(${group.assigneeName.substringAfter("(", "")}"
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            ) {
                                append(particle)
                            }
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.weight(1f))

                // 프로필 사진: 34dp, 원형, 테두리
                if (group.assigneeProfileRes != null) {
                    Image(
                        painter = painterResource(id = group.assigneeProfileRes),
                        contentDescription = "${group.assigneeName} 프로필",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp) // 1. 크기 34dp
                            .clip(CircleShape) // 2. 원형으로 자르기
                            .border( // 3. 테두리 추가
                                BorderStroke(1.dp, Color.LightGray), // TODO: 테두리 색상 지정
                                CircleShape
                            )
                    )
                } else {
                    // 이미지가 없을 때의 Placeholder도 동일하게
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .border(
                                BorderStroke(1.dp, Color.LightGray), // TODO: 테두리 색상 지정
                                CircleShape
                            )
                    )
                }

                // 점 3개 아이콘
                IconButton(onClick = { /* '...' 메뉴 클릭 */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "더보기",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(1.dp))

            // --- 투두 목록 ---
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.tasks.forEach { task ->
                    TaskItemRow(task = task) // (아래 TaskItemRow 수정)
                }
            }
        }
    }
}


@Composable
fun TaskItemRow(task: Task) {
    var isChecked by remember { mutableStateOf(task.isChecked) }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
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
        // Todo 텍스트: Medium, 12sp
        Text(
            text = task.title,
            modifier = Modifier
                .padding(start = 0.5.dp)
                .weight(0.5f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.width(10.dp))

        // 날짜: Medium, 10sp
        Text(
            text = task.date,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color.Black // TODO: 날짜 색상 지정
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodoScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()

        // TodoScreen에 생성한 navController 전달
        TodoScreen(navController = navController)
    }
}
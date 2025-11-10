package com.example.howsu.screen.todo

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
import androidx.compose.foundation.lazy.items // ★ 1. 'items' (List용)를 임포트
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
// ★ 2. 'data.model' 패키지에서 Task와 TodoGroup을 임포트
import com.example.howsu.data.model.Task
import com.example.howsu.data.model.TodoGroup
import com.example.howsu.ui.theme.HowsuTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    navController: NavHostController,
    viewModel: TodoViewModel = viewModel()
) {
    // ViewModel로부터 실시간 데이터 목록(List<TodoGroup>) 가져오기
    val todoGroups by viewModel.todoGroups.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // --- 상단 앱 바 ---
        topBar = {
            TopAppBar(
                title = { Text("Todo", fontWeight = FontWeight.Medium, fontSize = 24.sp) },
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
                onTodoClick = {
                    navController.navigate("create_todo")
                },
                onScheduleClick = {
                    navController.navigate("create_schedule")
                }
            )
        }
    ) { innerPadding ->
        // --- 본문 (할 일 목록) ---
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(todoGroups) { group ->
                TodoGroupCard(
                    group = group,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}


@Composable
fun TodoGroupCard(
    group: TodoGroup,
    navController: NavHostController,
    viewModel: TodoViewModel
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ... (Text, Spacer 부분은 동일) ...
                Text(
                    text = buildAnnotatedString {
                        val mainName = group.assigneeName.substringBefore("(", group.assigneeName)
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) {
                            append(mainName)
                        }
                        if (group.assigneeName.contains("(")) {
                            val particle = "(${group.assigneeName.substringAfter("(", "")}"
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp)) {
                                append(particle)
                            }
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))

                // ▼▼▼▼▼ ★ 여기가 수정된 부분입니다 ★ ▼▼▼▼▼

                // 1. 'var' 변수를 'val' 지역 변수로 복사 (스마트 캐스트 문제 해결)
                val profileRes = group.assigneeProfileRes

                // 2. 'val' 변수(profileRes)를 사용
                if (profileRes != null) {
                    Image(
                        painter = painterResource(id = profileRes), // ★ 'val' 변수 사용
                        contentDescription = "${group.assigneeName} 프로필",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .border(BorderStroke(1.dp, Color.LightGray), CircleShape)
                    )
                } else {
                    // Placeholder
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .border(BorderStroke(1.dp, Color.LightGray), CircleShape)
                    )
                }
                // ▲▲▲▲▲ ★ 수정 끝 ★ ▲▲▲▲▲

                // ... (Box와 DropdownMenu 부분은 동일) ...
                Box {
                    IconButton(onClick = {
                        isMenuExpanded = true
                    }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "더보기",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = {
                            isMenuExpanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text("수정하기") },
                            onClick = {
                                navController.navigate("edit_todo/${group.id}")
                                isMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제하기") },
                            onClick = {
                                viewModel.deleteGroup(group.id)
                                isMenuExpanded = false
                            }
                        )
                    }
                }
            } // --- Row 끝 ---

            Spacer(modifier = Modifier.height(1.dp))

            // --- (Column과 tasks.forEach 부분은 동일) ---
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.tasks.forEach { task ->
                    TaskItemRow(task = task)
                }
            }
        }
    }
}

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
            modifier = Modifier
                .padding(start = 0.5.dp)
                .weight(0.5f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = task.date,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodoScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        TodoScreen(navController = navController)
    }
}
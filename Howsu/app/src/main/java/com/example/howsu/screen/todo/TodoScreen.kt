package com.example.howsu.screen.todo

// import androidx.compose.material.icons.filled.ArrowBackIosNew // ★ 2. (삭제)
// import androidx.compose.material.icons.filled.ArrowForwardIos // ★ 3. (삭제)
import androidx.compose.foundation.background
import androidx.compose.foundation.border // ★ (신규) border 임포트
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.data.model.Task
import com.example.howsu.data.model.TodoGroup
import com.example.howsu.ui.theme.HowsuTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    navController: NavHostController,
    viewModel: TodoViewModel = viewModel()
) {
    val todoGroups by viewModel.todoGroups.collectAsState(initial = emptyList())
    val selectedDate by viewModel.selectedDate.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.selectDateFromPicker(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Todo", fontWeight = FontWeight.Medium, fontSize = 24.sp) },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
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
        bottomBar = {
            MyBottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = { navController.navigate("create_todo") },
                onScheduleClick = { navController.navigate("create_schedule") },
                onFeedCreateClick = { navController.navigate("create_feed") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val today = LocalDate.now() // ★ (신규) 오늘 날짜 가져오기

            CalendarWeekRow(
                selectedDate = selectedDate,
                today = today, // ★ (신규) 오늘 날짜 전달
                onDateChange = viewModel::onDateChange,
                onWeekDaySelected = viewModel::onWeekDaySelected
            )

            DailyHeader(selectedDate = selectedDate)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(todoGroups, key = { it.documentId }) { group ->
                    TodoGroupCard(
                        group = group,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ★★★ 5. (수정) 주간 캘린더 (스와이프 + 동그라미 + 테두리) ★★★
@Composable
fun CalendarWeekRow(
    selectedDate: LocalDate,
    today: LocalDate, // ★ (신규) '오늘' 날짜 받기
    onDateChange: (days: Long) -> Unit, // (Boolean -> Long)
    onWeekDaySelected: (LocalDate) -> Unit
) {
    val startOfWeek = selectedDate.with(DayOfWeek.SUNDAY)
    val weekDates = List(7) { i -> startOfWeek.plusDays(i.toLong()) }

    val dayFormatter = DateTimeFormatter.ofPattern("d", Locale.KOREAN)
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.KOREAN)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            // ★ (신규) 스와이프 제스처 추가
            .pointerInput(Unit) {
                var totalDragAmount = 0f // 1. 드래그 거리를 저장할 변수

                detectHorizontalDragGestures(
                    // 2. 드래그하는 동안 x축 이동 거리를 totalDragAmount에 누적
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDragAmount += dragAmount
                    },
                    // 3. 드래그가 끝나면(손가락을 떼면)
                    onDragEnd = {
                        if (totalDragAmount > 50) { // ★ 오른쪽으로 50px 이상
                            onDateChange(-7) // 이전 주 (7일 빼기)
                        } else if (totalDragAmount < -50) { // ★ 왼쪽으로 50px 이상
                            onDateChange(7) // 다음 주 (7일 더하기)
                        }
                        totalDragAmount = 0f // 4. 거리 리셋
                    },
                    // 5. 드래그가 취소돼도 리셋
                    onDragCancel = {
                        totalDragAmount = 0f
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround // ★ (수정) SpaceEvenly -> SpaceAround
    ) {
        // ★ (삭제) < 버튼

        weekDates.forEach { date ->
            val isSelected = date == selectedDate
            val isToday = date == today // ★ (신규) 오늘인지 확인

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f) // ★ 동일 너비
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onWeekDaySelected(date) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                // "월"
                Text(
                    text = date.format(dayOfWeekFormatter),
                    fontSize = 12.sp,
                    color = Color.Gray // ★ 선택 여부와 관계없이 회색
                )
                Spacer(modifier = Modifier.height(4.dp))

                // "17" (★ 수정 - 동그라미/테두리)
                Box(
                    modifier = Modifier
                        .size(32.dp) // 동그라미 크기
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.Black else Color.Transparent // ★ 선택 시 검은 배경
                        )
                        .then(
                            // ★ (신규) 오늘이고, 선택되지 않았을 때만 테두리
                            if (isToday && !isSelected) {
                                Modifier.border(1.5.dp, Color.Black, CircleShape)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.format(dayFormatter),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else Color.Black // ★ 선택 시 흰색
                    )
                }
            }
        }

        // ★ (삭제) > 버튼
    }
}

// (기존) 날짜 헤더 (변경 없음)
@Composable
fun DailyHeader(selectedDate: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern("M월 d일 E요일", Locale.KOREAN)
    val isToday = selectedDate == LocalDate.now()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedDate.format(formatter) + if (isToday) " (오늘)" else "",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// (기존) TodoGroupCard (변경 없음)
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
                val assigneeName = group.assigneeName ?: ""
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) {
                            append(assigneeName)
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp)) {
                            append("(이)가")
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))

                OverlappingPetIcons(
                    petNames = group.petNames,
                    color = Color.Black,
                    modifier = Modifier.height(34.dp)
                )

                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "더보기",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("수정하기") },
                            onClick = {
                                navController.navigate("edit_todo/${group.documentId}")
                                isMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제하기") },
                            onClick = {
                                viewModel.deleteGroup(group.documentId)
                                isMenuExpanded = false
                            }
                        )
                    }
                }
            } // --- Row 끝 ---

            Spacer(modifier = Modifier.height(1.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.tasks.forEach { task ->
                    TaskItemRow(
                        task = task,
                        onCheckedChange = { isChecked ->
                            viewModel.onTaskCheckedChange(group.documentId, task.id, isChecked)
                        }
                    )
                }
            }
        }
    }
}

// ★★★ 6. (수정) TaskItemRow (Row 전체 클릭) ★★★
@Composable
fun TaskItemRow(
    task: Task,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable { onCheckedChange(!task.isChecked) }, // ★ (신규) Row 전체 클릭
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = task.title ?: "",
            modifier = Modifier
                .padding(start = 0.5.dp)
                .weight(0.5f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            maxLines = 1,
            textDecoration = if (task.isChecked) TextDecoration.LineThrough else TextDecoration.None
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = task.date ?: "",
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

// ★★★ 7. (수정) 펫 아이콘 (3개 + N개)
@Composable
private fun OverlappingPetIcons(
    petNames: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (petNames.isEmpty()) {
        return
    }

    val displayNames = petNames.take(3) // ★ 최대 3개만 표시
    val remaining = (petNames.size - displayNames.size).coerceAtLeast(0)

    // (아이콘 32dp, 겹침 12dp -> (3-1)*20 + 32 = 72dp)
    // (남은 숫자(+N) 너비 24dp)
    val width = (32 + (displayNames.size - 1) * 20 + (if (remaining > 0) 24 else 0)).dp
    val overlap = 20.dp // 겹치는 너비 (32dp 아이콘 기준)

    Box(
        modifier = modifier
            .width(width)
            .height(32.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // (수정) 뒤에서부터 그림 (Z-index)
        displayNames.reversed().forEachIndexed { index, name ->
            PetIconCircle(
                petName = name,
                color = color.copy(alpha = 1f - (index * 0.2f)),
                modifier = Modifier
                    .padding(start = ((displayNames.size - 1) - index) * overlap)
                    .size(32.dp)
            )
        }

        // "+N" 텍스트 (4개 이상일 때)
        if (remaining > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remaining",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

// (기존) 펫 아이콘 헬퍼
@Composable
private fun PetIconCircle(petName: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = petName,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}
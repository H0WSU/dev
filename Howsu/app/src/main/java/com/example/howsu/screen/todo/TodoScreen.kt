package com.example.howsu.screen.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.howsu.R
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.data.model.Task
import com.example.howsu.data.model.TodoGroup
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    navController: NavHostController,
    viewModel: TodoViewModel = viewModel()
) {
    val todoGroups by viewModel.todoGroups.collectAsState(initial = emptyList())
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentWeekStart by viewModel.currentWeekStart.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resetToToday()
        // ★ 추가: 화면 들어올 때마다 데이터 다시 불러오기
        viewModel.fetchTodoGroups()
    }

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
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFFFFC848)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = { MyBottomNavigationBar(navController = navController) },
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
            val today = LocalDate.now()

            CalendarWeekRow(
                selectedDate = selectedDate,
                currentWeekStart = currentWeekStart,
                today = today,
                onWeekSwipe = viewModel::onWeekSwipe,
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

// ★★★ 핵심 수정: 터치와 스와이프 완벽 분리 및 UI 수정 ★★★
@Composable
fun CalendarWeekRow(
    selectedDate: LocalDate,
    currentWeekStart: LocalDate,
    today: LocalDate,
    onWeekSwipe: (days: Long) -> Unit,
    onWeekDaySelected: (LocalDate) -> Unit
) {
    val startOfWeek = currentWeekStart
    val weekDates = List(7) { i -> startOfWeek.plusDays(i.toLong()) }
    val dayFormatter = DateTimeFormatter.ofPattern("d", Locale.KOREAN)
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.KOREAN)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .pointerInput(Unit) {
                // ★★★ 터치 분리 로직 ★★★
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // 드래그가 끝났을 때, 이동 거리가 충분히 길어야만(100px) 주 이동 실행
                        if (totalDrag < -100f) onWeekSwipe(7)
                        else if (totalDrag > 100f) onWeekSwipe(-7)
                        totalDrag = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        totalDrag += dragAmount

                        // ★ 중요: 움직임이 30px 이상일 때만 이벤트를 '소비(consume)'함
                        // 즉, 살짝 움직이는 건(30px 미만) 클릭으로 통과되고,
                        // 크게 움직여야만 스와이프로 인식됨.
                        if (abs(totalDrag) > 30f) {
                            change.consume()
                        }
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        weekDates.forEach { date ->
            val isSelected = date == selectedDate
            val isToday = date == today

            // ★ UI 로직:
            // 1. 오늘 날짜(isToday) = 검은색 채워진 동그라미 + 흰색 글씨
            // 2. 선택된 날짜(isSelected)이면서 오늘이 아님 = 검은색 테두리 + 검은색 글씨
            // 3. 그 외 = 투명 + 검은색 글씨

            val backgroundColor = if (isToday) Color(0xFFFFC848) else Color.Transparent
            val borderColor = if (isSelected && !isToday) Color(0xFFFFC848) else Color.Transparent
            val textColor = if (isToday) Color.White else Color(0xFF505050)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onWeekDaySelected(date) } // 클릭은 여기서 처리
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = date.format(dayOfWeekFormatter),
                    fontSize = 12.sp,
                    color = Color(0xFF505050)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .border(1.5.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.format(dayFormatter),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }
        }
    }
}

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
        // 1. 날짜 텍스트
        Text(
            text = selectedDate.format(formatter),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF505050)
        )

        // 2. "오늘" 뱃지 (오늘 날짜일 때만 표시)
        if (isToday) {
            Spacer(modifier = Modifier.width(8.dp)) // 간격

            Surface(
                color = Color(0xFFFFC848), // 포인트 컬러 (노랑)
                shape = RoundedCornerShape(12.dp), // 둥근 모서리
                modifier = Modifier.height(22.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "오늘",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 한글 받침 확인 함수
fun hasBatchim(text: String): Boolean {
    if (text.isEmpty()) return false
    val lastChar = text.last()

    // 한글 유니코드 범위: 가(0xAC00) ~ 힣(0xD7A3)
    if (lastChar < '\uAC00' || lastChar > '\uD7A3') return false

    // (글자 - 0xAC00) % 28 의 결과가 0보다 크면 받침이 있는 것
    return (lastChar.code - 0xAC00) % 28 > 0
}
@Composable
fun TodoGroupCard(
    group: TodoGroup,
    navController: NavHostController,
    viewModel: TodoViewModel
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    // ★ 공통 색상 정의 (진한 회색)
    val contentColor = Color(0xFF505050)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFD676) // 노란색 카드
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp), // 여백
                verticalAlignment = Alignment.CenterVertically
            ) {
                // [삭제됨] 프로필 사진 Box 제거 완료

                val assigneeName = group.assigneeName ?: ""
                val particle = if (hasBatchim(assigneeName)) "이" else "가"

                // 이름 텍스트
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)) {
                            append(assigneeName)
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp)) {
                            append(particle)
                        }
                    },
                    color = contentColor
                )

                Spacer(Modifier.weight(1f))

                // ★ 펫 아이콘은 유지 (나중에 펫 사진 보여줄 곳)
                OverlappingPetIcons(
                    petNames = group.petNames,
                    color = contentColor,
                    modifier = Modifier.height(34.dp)
                )

                // 더보기 메뉴
                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기",
                            tint = contentColor
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
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 할 일 목록
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.tasks.forEach { task ->
                    TaskItemRow(
                        task = task,
                        onCheckedChange = { isChecked ->
                            viewModel.onTaskCheckedChange(group.documentId, task.id, isChecked)
                        },
                        contentColor = contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItemRow(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    contentColor: Color = Color(0xFF505050) // 기본값 설정
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                // ★ [수정됨] 체크 안 됐을 때 테두리 색상 변경
                uncheckedColor = contentColor
            )
        )

        Text(
            text = task.title ?: "",
            modifier = Modifier
                .weight(1f)
                .padding(start = 3.dp),
            // ★ [수정됨] 할 일 텍스트 색상 변경
            color = contentColor,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            maxLines = 1,
            textDecoration = if (task.isChecked) TextDecoration.LineThrough else TextDecoration.None
        )

        Text(
            text = task.date ?: "",
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            // 날짜는 조금 연하게 유지하거나, 원하시면 contentColor로 바꾸셔도 됩니다.
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// --- (기존 펫 아이콘 관련 코드 유지) ---
@Composable
private fun OverlappingPetIcons(
    petNames: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (petNames.isEmpty()) return
    val displayNames = petNames.take(3)
    val remaining = (petNames.size - displayNames.size).coerceAtLeast(0)
    val width = (32 + (displayNames.size - 1) * 20 + (if (remaining > 0) 24 else 0)).dp
    val overlap = 20.dp

    Box(
        modifier = modifier.width(width).height(32.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        displayNames.reversed().forEachIndexed { index, name ->
            PetIconCircle(
                petName = name,
                color = color.copy(alpha = 1f - (index * 0.2f)),
                modifier = Modifier.padding(start = ((displayNames.size - 1) - index) * overlap).size(32.dp)
            )
        }
        if (remaining > 0) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd).size(24.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+$remaining", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
private fun PetIconCircle(petName: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(CircleShape).background(Color.Gray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = Icons.Default.Pets, contentDescription = petName, tint = color, modifier = Modifier.size(20.dp))
    }
}
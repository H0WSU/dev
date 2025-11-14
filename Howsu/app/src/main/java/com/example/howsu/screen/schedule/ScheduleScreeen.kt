package com.example.howsu.screen.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.ui.theme.HowsuTheme
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavHostController,
    viewModel: ScheduleViewModel = viewModel()
) {
    val schedules by viewModel.schedules.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

    val refreshTrigger = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh_needed", false) // "refresh_needed" 키를 관찰
        ?.collectAsState()

    LaunchedEffect(key1 = refreshTrigger?.value) {
        if (refreshTrigger?.value == true) {
            // 2. 신호가 true이면, 현재 선택된 날짜의 일정을 강제로 새로고침
            // (ViewModel에서 onDateSelected가 날짜에 맞는 일정을 로드한다고 가정)
            viewModel.onDateSelected(selectedDate.dayOfMonth)

            // 3. (중요) 신호를 받았으니 다시 false로 리셋
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_needed", false)
        }
    }

    Scaffold(
        containerColor = Color(0xFFE8E8E8),
        topBar = {
            TopAppBar(
                title = {
                    CalendarHeader(
                        yearMonth = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                        // modifier = Modifier.clickable { showMonthPicker = true },
                        onPrevClick = { viewModel.onMonthChange(false) },
                        onNextClick = { viewModel.onMonthChange(true) },
                        onMonthArrowClick = { showMonthPicker = true }

                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            MyBottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = {
                    navController.navigate("create_todo")
                },
                onScheduleClick = {
                    navController.navigate("create_schedule")
                }
            )
        },
    ) { innerPadding ->
        if (showMonthPicker) {
            MonthYearPickerDialog(
                initialYear = currentMonth.year,
                initialMonth = currentMonth.monthValue,
                onDismiss = { showMonthPicker = false },
                onConfirm = { year, month ->
                    // (이 함수는 ViewModel에 새로 만들어야 합니다)
                    viewModel.onMonthYearChange(year, month)
                    showMonthPicker = false
                }
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- 1. 캘린더 (변경 없음) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    CalendarMonthView(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate.dayOfMonth,
                        onDateClick = viewModel::onDateSelected
                    )
                }
            }

            // --- 2. "XX월 XX일의 일정" (변경 없음) ---
            stickyHeader {
                val formatter = DateTimeFormatter.ofPattern("M월 d일의 일정")
                ScheduleTitle(title = selectedDate.format(formatter))
            }

            if (schedules.isEmpty()) {
                // --- 일정이 없을 때 ---
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp), // 헤더 밑으로 여백
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "등록된 일정이 없습니다.",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // --- 일정이 있을 때 (시간순으로 정렬된 목록) ---
                items(schedules, key = { it.id }) { schedule -> // ViewModel이 이미 시간순 정렬

                    val cardColor = try {
                        Color(android.graphics.Color.parseColor(schedule.color))
                    } catch (e: Exception) {
                        Color.Black
                    }

                    // 1. schedule.isAllDay를 확인
                    val timeString = if (schedule.isAllDay) {
                        "하루 종일"
                    } else {
                        // 2. '하루 종일'이 아닐 때만 시간 포맷팅
                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        val zoneId = ZoneId.systemDefault()

                        val startTime = schedule.startDate.toDate().toInstant()
                            .atZone(zoneId)
                            .format(formatter)
                        val endTime = schedule.endDate.toDate().toInstant()
                            .atZone(zoneId)
                            .format(formatter)

                        "$startTime - $endTime"
                    }

                    // ★ 카드에 좌우/상하 패딩을 줘서 리스트처럼 보이게 함
                    ScheduleItemCard(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        icon = Icons.Default.Pets,
                        title = schedule.title,
                        time = timeString, // ★ 수정된 timeString 변수 사용
                        petTag = schedule.petNames.firstOrNull() ?: "",
                        color = cardColor,
                        onClick = {
                            navController.navigate("schedule_detail/${schedule.id}")
                        }
                    )
                }
            }

            // --- 4. 하단 여백 (변경 없음) ---
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// --- 캘린더 헤더 ---
@Composable
fun CalendarHeader(
    yearMonth: String,
    modifier: Modifier = Modifier,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onMonthArrowClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(), // modifier는 Row 전체에 적용
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: onPrevClick, onNextClick을 화살표 버튼에 연결
        Text(
            text = yearMonth,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "월 변경",
            modifier = Modifier
                .size(20.dp)
                .clickable { onMonthArrowClick() }
        )
    }
}

// --- 캘린더 뷰 (월) ---
// --- 캘린더 뷰 (월) ---
@Composable
fun CalendarMonthView(
    selectedDate: Int,
    modifier: Modifier = Modifier,
    onDateClick: (Int) -> Unit,
    currentMonth: YearMonth,
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 일요일(0) ~ 토요일(6)
    val daysInMonth = currentMonth.lengthOfMonth()

    // 캘린더 그리드를 채울 날짜 목록 (null은 빈 칸)
    val dates = buildList {
        repeat(firstDayOfWeek) { add(null) } // 첫째 날 전까지 빈 칸 추가
        (1..daysInMonth).forEach { add(it) } // 실제 날짜 추가
    }.chunked(7) // 7일 단위로 자르기
    // --- ▲▲▲ ---

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // (요일 헤더는 동일)
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // (날짜 그리드 생성 로직)
        dates.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f) // 각 날짜가 1의 비율을 가짐
                            .height(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) { // day가 null이 아닐 때만 (빈 칸이 아닐 때)
                            val isSelected = day == selectedDate
                            val containerColor = if (isSelected) Color.Black else Color.Transparent
                            val contentColor = if (isSelected) Color.White else Color.Black

                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(containerColor)
                                    .clickable { onDateClick(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 14.sp,
                                    color = contentColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                } // week.forEach 끝

                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f).height(32.dp))
                    }
                }
                // --- ★ 수정 끝 ---

            } // Row 끝
            Spacer(modifier = Modifier.height(4.dp))
        } // dates.forEach 끝
    }
}
// --- "오늘의 일정" 타이틀 ---
@Composable
fun ScheduleTitle(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFE8E8E8)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp)
        )
    }
}

// --- ★ (삭제됨) HourSlot 함수 ---
// 이 함수는 더 이상 사용되지 않으므로 파일에서 삭제해도 됩니다.
// @Composable
// fun HourSlot( ... ) { ... }


// --- 일정 아이템 카드 ---
@Composable
fun ScheduleItemCard(
    icon: ImageVector,
    title: String,
    time: String,
    petTag: String,
    color: Color,
    modifier: Modifier = Modifier, // ★ modifier를 파라미터로 받도록 함
    onClick: () -> Unit
) {
    Card(
        modifier = modifier // ★ 전달받은 modifier 사용 (패딩 적용)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = time,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if(petTag.isNotEmpty()) {
                    PetTag(name = petTag)
                }
            }
        }
    }
}

// --- 반려동물 태그 ---
@Composable
fun PetTag(name: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = Color.LightGray.copy(alpha = 0.5f)
    ) {
        Text(
            text = name,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MonthYearPickerDialog(
    initialYear: Int,
    initialMonth: Int,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("날짜 선택") },
        text = {
            Column {
                // 1. 연도 선택기
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ArrowBackIosNew, "이전 년도")
                    }
                    Text(
                        text = "$selectedYear 년",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.ArrowForwardIos, "다음 년도")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 2. 월 선택기 (4x3 그리드)
                (1..12).chunked(4).forEach { monthRow ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        monthRow.forEach { month ->
                            val isSelected = (selectedYear == initialYear && month == selectedMonth)
                            TextButton(onClick = { selectedMonth = month }) {
                                Text(
                                    text = "${month}월",
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear, selectedMonth) }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

// --- 7. 미리보기 ---
@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun ScheduleScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        ScheduleScreen(navController = navController)
    }
}
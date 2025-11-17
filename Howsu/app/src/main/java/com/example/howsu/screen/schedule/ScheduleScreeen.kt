package com.example.howsu.screen.schedule

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Event // ★ (신규) '하루 종일' 아이콘
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.data.model.Schedule
import com.example.howsu.ui.theme.HowsuTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavHostController,
    viewModel: ScheduleViewModel = viewModel()
) {
    // --- ViewModel 상태 ---
    val schedules by viewModel.schedules.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val monthSchedules by viewModel.monthSchedules.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

    // --- 캘린더 확장/축소 상태 ---
    var isCalendarExpanded by remember { mutableStateOf(false) }

    // --- 새로고침 로직 ---
    val refreshTrigger = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh_needed", false)
        ?.collectAsState()

    LaunchedEffect(key1 = refreshTrigger?.value) {
        if (refreshTrigger?.value == true) {
            viewModel.refreshAllSchedules()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_needed", false)
        }
    }

    // --- NestedScrollConnection 로직 ---
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0 && isCalendarExpanded) {
                    isCalendarExpanded = false
                    return available.consume()
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0 && !isCalendarExpanded) {
                    isCalendarExpanded = true
                    return available.consume()
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (available.y < 0 && isCalendarExpanded) {
                    isCalendarExpanded = false
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y > 0 && !isCalendarExpanded) {
                    isCalendarExpanded = true
                    return available
                }
                return Velocity.Zero
            }

            private fun Offset.consume() = this
        }
    }

    // --- 화면 구조 ---
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    CalendarHeader(
                        yearMonth = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                        onPrevClick = { viewModel.onMonthChange(false) },
                        onNextClick = { viewModel.onMonthChange(true) },
                        onMonthArrowClick = { showMonthPicker = true }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { MyBottomNavigationBar(navController = navController) },
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = { navController.navigate("create_todo") },
                onScheduleClick = { navController.navigate("create_schedule") },
                onFeedCreateClick = { navController.navigate("feed_create") }
            )
        },
    ) { innerPadding ->
        if (showMonthPicker) {
            MonthYearPickerDialog(
                initialYear = currentMonth.year,
                initialMonth = currentMonth.monthValue,
                onDismiss = { showMonthPicker = false },
                onConfirm = { year, month ->
                    viewModel.onMonthYearChange(year, month)
                    showMonthPicker = false
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(nestedScrollConnection)
        ) {

            // --- 1. 캘린더 영역 ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .animateContentSize(animationSpec = tween(100))
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            val y = dragAmount // .y 제거
                            if (y < -10 && isCalendarExpanded) {
                                isCalendarExpanded = false
                            } else if (y > 10 && !isCalendarExpanded) {
                                isCalendarExpanded = true
                            }
                        }
                    }
            ) {
                if (isCalendarExpanded) {
                    // --- 디자인 B (상세 뷰) ---
                    DetailedCalendarMonthView(
                        selectedDate = selectedDate.dayOfMonth,
                        onDateClick = viewModel::onDateSelected,
                        currentMonth = currentMonth,
                        monthSchedules = monthSchedules
                    )
                } else {
                    // --- 디자인 A (심플 뷰) ---
                    SimpleCalendarMonthView(
                        selectedDate = selectedDate.dayOfMonth,
                        onDateClick = viewModel::onDateSelected,
                        currentMonth = currentMonth,
                        monthSchedules = monthSchedules
                    )
                }
            }

            // --- 2. 구분선 ---
            Divider(color = Color.Gray.copy(alpha = 0.1f), thickness = 8.dp)

            // --- 3. 하단 일정 목록 (LazyColumn) ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {

                item {
                    DayHeader(selectedDate = selectedDate)
                }

                // ★★★ (수정) 목록 로직 변경 ---
                if (schedules.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("등록된 일정이 없습니다.", fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                } else {
                    // 1. '하루 종일' 일정
                    val allDaySchedules = schedules.filter { it.isAllDay }
                    items(allDaySchedules, key = { "all-day-${it.id}" }) { schedule ->
                        AllDayScheduleItem(
                            schedule = schedule,
                            onClick = {
                                navController.navigate("schedule_detail/${schedule.id}")
                            }
                        )
                    }

                    // 2. '시간 지정' 일정
                    val timedSchedules = schedules.filter { !it.isAllDay }

                    // '하루 종일'과 '시간 지정' 사이에 간격
                    if (allDaySchedules.isNotEmpty() && timedSchedules.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    items(timedSchedules, key = { "timed-${it.id}" }) { schedule ->
                        DayScheduleItem(
                            schedule = schedule,
                            onClick = {
                                navController.navigate("schedule_detail/${schedule.id}")
                            }
                        )
                        Divider(
                            color = Color.Gray.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                // ★★★ (수정 끝) ---

                item {
                    Spacer(modifier = Modifier.height(100.dp)) // FAB 여백
                }
            }
        }
    }
}


// ======================================================================
// Composable 함수들
// ======================================================================

// --- 디자인 A (심플 뷰: 선 표시) ---
@Composable
fun SimpleCalendarMonthView(
    selectedDate: Int,
    modifier: Modifier = Modifier,
    onDateClick: (Int) -> Unit,
    currentMonth: YearMonth,
    monthSchedules: Map<Int, List<Schedule>>
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()

    val dates = buildList {
        repeat(firstDayOfWeek) { add(null) }
        (1..daysInMonth).forEach { add(it) }
    }.chunked(7)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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

        dates.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp), // 셀의 고정 높이
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (day != null) {
                            val schedulesForDay = monthSchedules[day] ?: emptyList()
                            val isSelected = day == selectedDate

                            val (containerColor, contentColor) = if (isSelected) {
                                Color.Black to Color.White
                            } else {
                                Color.Transparent to Color.Black
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDateClick(day) }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // 1. 날짜 텍스트
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(containerColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 14.sp,
                                        color = contentColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                // 2. 일정 표시 선 (최대 2개)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    schedulesForDay.take(2).forEach { schedule ->
                                        val indicatorColor = try {
                                            Color(android.graphics.Color.parseColor(schedule.color))
                                        } catch (e: Exception) { Color.Gray }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.7f)
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(indicatorColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f).height(50.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}


// --- 디자인 B (상세 뷰: 텍스트 표시 + 구분선) ---
@Composable
fun DetailedCalendarMonthView(
    selectedDate: Int,
    modifier: Modifier = Modifier,
    onDateClick: (Int) -> Unit,
    currentMonth: YearMonth,
    monthSchedules: Map<Int, List<Schedule>>
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()

    val dates = buildList {
        repeat(firstDayOfWeek) { add(null) }
        (1..daysInMonth).forEach { add(it) }
    }.chunked(7)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // --- 1. 요일 헤더 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
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

        // --- 2. 날짜 그리드 ---
        Column {
            dates.forEach { week ->
                // 각 주(week)가 시작하기 전에 구분선 추가
                Divider(
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                // --- 한 주의 날짜들 ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    week.forEach { day ->

                        // ★★★ (수정) 날짜 셀 전체에 테두리 적용
                        val isSelected = day == selectedDate
                        val borderModifier = if (isSelected) {
                            Modifier.border(2.dp, Color(0xFF34A853), RoundedCornerShape(8.dp))
                        } else Modifier

                        // 날짜 셀
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 80.dp) // 최소 높이
                                .then(borderModifier) // ★★★ (수정) 테두리 적용
                                .clickable { if (day != null) onDateClick(day) }
                                .padding(2.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (day != null) {
                                val schedulesForDay = monthSchedules[day] ?: emptyList()

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 1. 날짜 텍스트
                                    Text(
                                        text = day.toString(),
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color(0xFF34A853) else Color.Black,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 2. 일정 제목/선 표시
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        schedulesForDay.filter { it.isAllDay }.take(2).forEach {
                                            ScheduleTagItem(schedule = it) // '하루 종일' 일정
                                        }
                                        schedulesForDay.filter { !it.isAllDay }.take(2).forEach {
                                            ScheduleBarItem(schedule = it) // '시간 지정' 일정
                                        }
                                    }
                                }
                            }
                        } // Box (날짜 셀)
                    } // week.forEach 끝

                    // (빈 칸 채우기)
                    if (week.size < 7) {
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.weight(1f).heightIn(min = 80.dp))
                        }
                    }
                } // Row 끝
            } // dates.forEach 끝
        } // Column 끝
    }
}

/**
 * '하루 종일' 일정을 위한 태그(Tag) Composable
 */
@Composable
private fun ScheduleTagItem(schedule: Schedule) {
    val scheduleColor = try {
        Color(android.graphics.Color.parseColor(schedule.color))
    } catch (e: Exception) { Color.Gray }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheduleColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = schedule.title,
            fontSize = 10.sp,
            color = scheduleColor.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * '시간 지정' 일정을 위한 바(Bar) Composable (시간 텍스트 제거)
 */
@Composable
private fun ScheduleBarItem(schedule: Schedule) {
    val scheduleColor = try {
        Color(android.graphics.Color.parseColor(schedule.color))
    } catch (e: Exception) { Color.Black }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .background(scheduleColor, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = schedule.title,
            fontSize = 10.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevClick) {
            Icon(Icons.Default.ArrowBackIosNew, "이전 달", modifier = Modifier.size(18.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onMonthArrowClick() }
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = yearMonth,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "월 변경",
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.ArrowForwardIos, "다음 달", modifier = Modifier.size(18.dp))
        }
    }
}

// --- "XX일 X요일" 헤더 ---
@Composable
fun DayHeader(selectedDate: LocalDate) {
    val dayOfMonth = selectedDate.dayOfMonth
    val dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(text = "$dayOfMonth", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${dayOfWeek}요일",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 3.dp)
        )
    }
}

// --- 일정 아이템 (하단 목록용 - '시간 지정' 전용) ---
@Composable
fun DayScheduleItem(
    schedule: Schedule,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val zoneId = ZoneId.systemDefault()

    // '하루 종일' 체크는 이제 상위에서 필터링되므로, 항상 시간 표시
    val startTimeString = schedule.startDate.toDate().toInstant()
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("H:mm"))

    val formatterAmPm = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)
    val start = schedule.startDate.toDate().toInstant().atZone(zoneId).format(formatterAmPm)
    val end = schedule.endDate.toDate().toInstant().atZone(zoneId).format(formatterAmPm)
    val timeRangeString = "$start - $end"

    val scheduleColor = try {
        Color(android.graphics.Color.parseColor(schedule.color))
    } catch (e: Exception) { Color.Black }

    // val hasPet = schedule.petNames.isNotEmpty() // OverlappingPetIcons가 알아서 처리

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = startTimeString, // '종일' 텍스트가 필요 없음
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.width(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .background(scheduleColor, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = schedule.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeRangeString,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        // ★★★ (수정) 펫 아이콘을 새 컴포저블로 대체
        OverlappingPetIcons(
            petNames = schedule.petNames,
            color = scheduleColor
        )
    }
}

// --- ★★★ (신규) '하루 종일' 일정 아이템 ---
@Composable
fun AllDayScheduleItem(
    schedule: Schedule,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scheduleColor = try {
        Color(android.graphics.Color.parseColor(schedule.color))
    } catch (e: Exception) { Color.Black }

    // 선택한 색상의 연한 버전
    val lightBackgroundColor = scheduleColor.copy(alpha = 0.2f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp) // 리스트 항목 간 패딩
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = lightBackgroundColor),
        elevation = CardDefaults.cardElevation(0.dp) // 그림자 제거
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 캘린더 아이콘 (사진 참조)
            Icon(
                imageVector = Icons.Filled.Event, // 캘린더 모양 아이콘
                contentDescription = "하루 종일",
                tint = scheduleColor // 아이콘은 진한 색상
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.8f) // 너무 진하지 않게
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "하루 종일",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            // 펫 아이콘 (겹치는)
            OverlappingPetIcons(
                petNames = schedule.petNames,
                color = scheduleColor
            )
        }
    }
}


// --- ★★★ (신규) 겹치는 펫 아이콘 ---
@Composable
fun OverlappingPetIcons(
    petNames: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (petNames.isEmpty()) {
        Spacer(modifier = modifier.width(40.dp)) // 펫이 없으면 공간만 차지
        return
    }

    Box(
        modifier = modifier
            // 펫이 2개 이상이면 64dp, 1개면 40dp
            .width(if (petNames.size > 1) 64.dp else 40.dp)
            .height(40.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Icon 2 (오른쪽, 뒤에)
        if (petNames.size > 1) {
            PetIconCircle(
                petName = petNames[1],
                color = color.copy(alpha = 0.7f), // 뒤에 아이콘은 살짝 연하게
                modifier = Modifier.padding(start = 24.dp) // 24dp 겹치게
            )
        }

        // Icon 1 (왼쪽, 위에)
        PetIconCircle(
            petName = petNames[0],
            color = color,
            modifier = Modifier // ★★★ (수정) 흰색 테두리(border) 제거
        )
    }
}

// --- ★★★ (신규) 펫 아이콘 원형 헬퍼 ---
@Composable
private fun PetIconCircle(petName: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = petName,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}


// --- 월/년 선택 다이얼로그 ---
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ArrowBackIosNew, "이전 년도")
                    }
                    Text(text = "$selectedYear 년", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.ArrowForwardIos, "다음 년도")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                (1..12).chunked(4).forEach { monthRow ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        monthRow.forEach { month ->
                            val isSelected = (month == selectedMonth)
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
        confirmButton = { TextButton(onClick = { onConfirm(selectedYear, selectedMonth) }) { Text("확인") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

// --- 미리보기 ---
@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun ScheduleScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        ScheduleScreen(navController = navController)
    }
}
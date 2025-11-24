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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
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

val YellowCustom = Color(0xFFFFDF37)
val TextBlack = Color(0xFF121212)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavHostController,
    viewModel: ScheduleViewModel = viewModel()
) {
    val schedules by viewModel.schedules.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val monthSchedules by viewModel.monthSchedules.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }
    var isCalendarExpanded by remember { mutableStateOf(false) }
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

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    CalendarHeader(
                        yearMonth = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                        onMonthArrowClick = { showMonthPicker = true }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = { viewModel.onMonthChange(false) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "이전 달",
                            modifier = Modifier.size(18.dp),
                            tint = TextBlack
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onMonthChange(true) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "다음 달",
                            modifier = Modifier.size(18.dp),
                            tint = TextBlack
                        )
                    }
                }
            )
        },
        bottomBar = { MyBottomNavigationBar(navController = navController) },
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = { navController.navigate("create_todo") },
                onScheduleClick = { navController.navigate("create_schedule") },
                onFeedCreateClick = { navController.navigate("create_feed") }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .animateContentSize(animationSpec = tween(100))
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            val y = dragAmount
                            if (y < -10 && isCalendarExpanded) {
                                isCalendarExpanded = false
                            } else if (y > 10 && !isCalendarExpanded) {
                                isCalendarExpanded = true
                            }
                        }
                    }
            ) {
                if (isCalendarExpanded) {
                    DetailedCalendarMonthView(
                        selectedDate = selectedDate.dayOfMonth,
                        onDateClick = viewModel::onDateSelected,
                        currentMonth = currentMonth,
                        monthSchedules = monthSchedules
                    )
                } else {
                    SimpleCalendarMonthView(
                        selectedDate = selectedDate.dayOfMonth,
                        onDateClick = viewModel::onDateSelected,
                        currentMonth = currentMonth,
                        monthSchedules = monthSchedules
                    )
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.1f), thickness = 8.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                item {
                    DayHeader(selectedDate = selectedDate)
                }
                if (schedules.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("등록된 일정이 없습니다", fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                } else {
                    val allDaySchedules = schedules.filter { it.isAllDay }
                    items(allDaySchedules, key = { "all-day-${it.id}" }) { schedule ->
                        AllDayScheduleItem(
                            schedule = schedule,
                            onClick = {
                                navController.navigate("schedule_detail/${schedule.id}")
                            }
                        )
                    }
                    val timedSchedules = schedules.filter { !it.isAllDay }
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
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}


// ======================================================================
// Composable 함수들
// ======================================================================

// ★★★ [수정] SimpleCalendarMonthView (투두 스타일 적용: 노란색/테두리)
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
    val today = LocalDate.now()

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
                            .height(50.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (day != null) {
                            val schedulesForDay = monthSchedules[day] ?: emptyList()

                            // ★★★ 날짜 로직 수정
                            val currentDate = currentMonth.atDay(day)
                            val isToday = currentDate == today
                            val isSelected = day == selectedDate

                            // 스타일 결정 (투두 스타일)
                            val containerColor = if (isToday) YellowCustom else Color.Transparent
                            val borderColor = if (isSelected && !isToday) YellowCustom else Color.Transparent
                            val contentColor = TextBlack // 글자는 항상 검정

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDateClick(day) }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(containerColor)
                                        .border(
                                            width = 1.dp,
                                            color = borderColor,
                                            shape = CircleShape
                                        ),
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    schedulesForDay.forEach { schedule ->
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


// ★★★ [수정] DetailedCalendarMonthView (펼친 뷰도 스타일 통일)
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
    val today = LocalDate.now()

    val dates = buildList {
        repeat(firstDayOfWeek) { add(null) }
        (1..daysInMonth).forEach { add(it) }
    }.chunked(7)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
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

        Column {
            dates.forEach { week ->
                Divider(
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    week.forEach { day ->
                        val isSelected = day == selectedDate

                        // ★★★ 선택된 날짜 테두리 색상: 초록색 -> 노란색 변경
                        val borderModifier = if (isSelected) {
                            Modifier.border(2.dp, YellowCustom, RoundedCornerShape(8.dp))
                        } else Modifier

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 80.dp)
                                .then(borderModifier)
                                .clickable { if (day != null) onDateClick(day) }
                                .padding(2.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (day != null) {
                                val schedulesForDay = monthSchedules[day] ?: emptyList()
                                val currentDate = currentMonth.atDay(day)
                                val isToday = currentDate == today

                                // ★★★ 오늘 날짜면 숫자 뒤에 노란 원 표시
                                val numBgColor = if (isToday) YellowCustom else Color.Transparent

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 숫자 표시 부분
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp) // 숫자 배경 크기
                                            .clip(CircleShape)
                                            .background(numBgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 14.sp,
                                            // 오늘이면 검정, 아니면 검정 (일관성)
                                            color = TextBlack,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(1.dp)
                                    ) {
                                        schedulesForDay.filter { it.isAllDay }.forEach {
                                            ScheduleTagItem(schedule = it)
                                        }
                                        schedulesForDay.filter { !it.isAllDay }.forEach {
                                            ScheduleBarItem(schedule = it)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (week.size < 7) {
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.weight(1f).heightIn(min = 80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleTagItem(schedule: Schedule) {
    val scheduleColor = try {
        Color(android.graphics.Color.parseColor(schedule.color))
    } catch (e: Exception) { Color.Gray }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheduleColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = schedule.title,
            fontSize = 9.sp,
            color = scheduleColor.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

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
                .height(12.dp)
                .background(scheduleColor, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = schedule.title,
            fontSize = 9.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun CalendarHeader(
    yearMonth: String,
    modifier: Modifier = Modifier,
    onMonthArrowClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable { onMonthArrowClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = yearMonth,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack,
            textAlign = TextAlign.Center
        )
    }
}

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
        Text(text = "$dayOfMonth", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextBlack)
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

@Composable
fun DayScheduleItem(
    schedule: Schedule,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    val formatterAmPm = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)
    val start = schedule.startDate.toDate().toInstant().atZone(zoneId).format(formatterAmPm)
    val end = schedule.endDate.toDate().toInstant().atZone(zoneId).format(formatterAmPm)
    val startTimeString = schedule.startDate.toDate().toInstant().atZone(zoneId).format(DateTimeFormatter.ofPattern("H:mm"))

    val timeRangeString = "$start - $end"

    val scheduleColor = try {
        Color(android.graphics.Color.parseColor(schedule.color))
    } catch (e: Exception) { Color.Black }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = startTimeString,
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
                color = TextBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeRangeString,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        OverlappingPetIcons(
            petNames = schedule.petNames,
            petUrls = schedule.petProfileUrls,
            color = scheduleColor
        )
    }
}

@Composable
fun AllDayScheduleItem(
    schedule: Schedule,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scheduleColor = try {
        Color(android.graphics.Color.parseColor(schedule.color))
    } catch (e: Exception) { Color.Black }

    val lightBackgroundColor = scheduleColor.copy(alpha = 0.2f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = lightBackgroundColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Event,
                contentDescription = "하루 종일",
                tint = scheduleColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextBlack.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "하루 종일",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            OverlappingPetIcons(
                petNames = schedule.petNames,
                petUrls = schedule.petProfileUrls,
                color = scheduleColor
            )
        }
    }
}


@Composable
fun OverlappingPetIcons(
    petNames: List<String>,
    petUrls: List<String?>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (petNames.isEmpty()) {
        return
    }

    val displayCount = petNames.take(3).size
    val remaining = (petNames.size - displayCount).coerceAtLeast(0)
    val iconSize = 32.dp
    val overlap = 12.dp

    // 안전한 dp 계산
    val totalWidth = (iconSize + (displayCount - 1) * (iconSize - overlap) + (if (remaining > 0) 24.dp else 0.dp))

    Box(
        modifier = modifier
            .width(totalWidth)
            .height(iconSize),
        contentAlignment = Alignment.CenterStart
    ) {
        for (index in 0 until displayCount) {
            val reverseIndex = (displayCount - 1) - index
            val name = petNames.getOrNull(reverseIndex) ?: ""
            val url = petUrls.getOrNull(reverseIndex)

            PetIconCircle(
                petName = name,
                imageUrl = url,
                color = color.copy(alpha = 1f - (reverseIndex * 0.2f)),
                modifier = Modifier
                    .padding(start = index * (iconSize - overlap)) // 안전 계산
                    .size(iconSize)
                    .zIndex(index.toFloat())
            )
        }

        if (remaining > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+$remaining", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
private fun PetIconCircle(
    petName: String,
    imageUrl: String?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.6f))
            .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            coil.compose.AsyncImage(
                model = imageUrl,
                contentDescription = petName,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = petName,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
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
                                    color = if (isSelected) YellowCustom else Color.Gray,
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

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun ScheduleScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        ScheduleScreen(navController = navController)
    }
}
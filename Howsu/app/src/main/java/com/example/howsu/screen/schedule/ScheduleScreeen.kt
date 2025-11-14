package com.example.howsu.screen.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Scaffold(
        containerColor = Color(0xFFE8E8E8),
        topBar = {
            TopAppBar(
                title = {
                    CalendarHeader(
                        yearMonth = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                        modifier = Modifier,
                        onPrevClick = { viewModel.onMonthChange(false) },
                        onNextClick = { viewModel.onMonthChange(true) }
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

            // --- ★ 3. (핵심 수정) 일정 목록 또는 빈 화면 표시 ---
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

                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    val zoneId = ZoneId.systemDefault()

                    val startTime = schedule.startDate.toDate().toInstant()
                        .atZone(zoneId)
                        .format(formatter)
                    val endTime = schedule.endDate.toDate().toInstant()
                        .atZone(zoneId)
                        .format(formatter)

                    // ★ 카드에 좌우/상하 패딩을 줘서 리스트처럼 보이게 함
                    ScheduleItemCard(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        icon = Icons.Default.Pets,
                        title = schedule.title,
                        time = "$startTime - $endTime",
                        petTag = schedule.petNames.firstOrNull() ?: "",
                        color = cardColor,
                        onClick = {
                            navController.navigate("schedule_detail/${schedule.id}")
                        }
                    )
                }
            }
            // --- ★ 수정 끝 ---

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
    onNextClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
                .clickable { /* TODO: 월 선택 팝업 */ }
        )
    }
}

// --- 캘린더 뷰 (월) ---
@Composable
fun CalendarMonthView(
    selectedDate: Int,
    modifier: Modifier = Modifier,
    onDateClick: (Int) -> Unit
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    // (임의의 날짜 데이터 - TODO: ViewModel에서 currentMonth 기준으로 생성해야 함)
    val dates = (listOf(null, null, null, null, null, null, 1) +
            (2..30).toList() +
            listOf(null, null, null, null, null, null))
        .chunked(7)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
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
                            .height(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
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
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
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

// --- 7. 미리보기 ---
@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun ScheduleScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        ScheduleScreen(navController = navController)
    }
}
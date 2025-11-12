package com.example.howsu.screen.schedule // (1. 본인 패키지 이름 확인)

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.ui.theme.HowsuTheme

// --- 1. 메인 화면: Scaffold 뼈대 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavHostController
) {
    Scaffold(
        containerColor = Color(0xFFE8E8E8), // 화면 배경색

        // --- 상단 앱 바 ---
        topBar = {
            TopAppBar(
                title = {
                    // LazyColumn에 있던 헤더를 TopAppBar의 title로 이동
                    CalendarHeader(
                        year = "2025",
                        month = "11월",
                        modifier = Modifier // TopAppBar가 패딩을 관리하므로 별도 패딩 불필요
                    )
                },
                // TopAppBar 배경색을 화면과 통일
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },

        // --- 하단 네비게이션 바 ---
        bottomBar = {
            MyBottomNavigationBar(navController = navController)
        },

        // --- 플로팅 버튼 ---
        floatingActionButton = {
            // ★ 이렇게 사용합니다.
            MyFloatingActionButton(
                onTodoClick = {
                    // 'TODO' 버튼 눌렀을 때 할 일 (예: 화면 이동)
                    navController.navigate("create_todo")
                },
                onScheduleClick = {
                    // '일정' 버튼 눌렀을 때 할 일
                    navController.navigate("create_schedule") // (예시)
                }
            )
        },
    ) { innerPadding ->

        // --- 6. 본문 (할 일 목록) ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Scaffold의 패딩(TopBar, BottomBar 높이) 적용
        ) {
            // --- 캘린더 뷰 (월) ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                        // .padding(horizontal = 8.dp), // 캘린더 좌우 여백
                    shape = RoundedCornerShape(
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    ),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    CalendarMonthView(
                        selectedDate = 15 // (디자인 예시: 15일 선택됨)
                    )
                }
            }

            // --- "오늘의 일정" 타이틀 (Sticky Header) ---
            stickyHeader {
                ScheduleTitle(title = "오늘의 일정")
            }

            // --- 타임라인 및 일정 ---
            val timeSlots = (8..20).toList() // 8시부터 20시
            items(timeSlots) { hour ->
                // 9시 슬롯에만 "병원 방문" 일정을 표시 (예시)
                val hasEvent = (hour == 9)

                HourSlot(
                    hour = hour,
                    hasEvent = hasEvent,
                    eventContent = {
                        if (hasEvent) {
                            ScheduleItemCard(
                                icon = Icons.Default.Pets, // '자몽' 아이콘 대신 임시 아이콘
                                title = "병원 방문",
                                time = "09:10 am - 09:50 am",
                                petTag = "자몽"
                            )
                        }
                    }
                )
            }

            // --- 하단 여백 ---
            // FAB와 BottomBar에 가려지지 않도록 마지막에 충분한 여백 추가
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// --- 캘린더 헤더 ---
@Composable
fun CalendarHeader(year: String, month: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 날짜 선택: Medium, 24
        Text(
            text = "${year}년 $month", // 수정된 부분: ${} 사용
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        // 드롭다운: 20*20
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "월 변경",
            modifier = Modifier.size(20.dp)
        )
    }
}

// --- 캘린더 뷰 (월) ---
@Composable
fun CalendarMonthView(selectedDate: Int, modifier: Modifier = Modifier) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    // (임의의 날짜 데이터 - 디자인 예시 기준)
    val dates = (listOf(null, null, null, null, null, null, 1) + // 1주차 (7칸)
            (2..30).toList() + // 2~30일 (29칸)
            // 42칸을 채우기 위해 6개의 null이 필요 (7 + 29 + 6 = 42)
            listOf(null, null, null, null, null, null)) // (null 5개 -> 6개로 수정)
        .chunked(7) // 7칸씩 6줄로 나눔

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 요일 헤더 (일, 월, 화...)
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

        // 날짜
        dates.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                           // .aspectRatio(1f), // 1:1 비율
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
    // 스크롤 시 배경색이 캘린더와 겹치지 않도록 Surface 사용
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
            // .padding(bottom = 5.dp),
        color = Color(0xFFE8E8E8) // 화면 배경색과 동일하게
    ) {
        Text(
            text = title,
            // 오늘의 일정: Semibold, 18
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp)
        )
    }
}

// --- 시간대별 슬롯 ---
@Composable
fun HourSlot(
    hour: Int,
    hasEvent: Boolean,
    modifier: Modifier = Modifier,
    eventContent: @Composable () -> Unit
) {
    // 1시간 단위를 100.dp 높이로 설정 (조정 가능)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            // 구분선 시간: Medium, 12
            Text(
                text = String.format("%d:00 am", hour),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.width(60.dp) // 시간 텍스트 영역 고정
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 8.dp)
            ) {
                // 구분선
                Divider(
                    color = Color.LightGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                )

                // 이벤트가 있으면 여기에 표시
                if (hasEvent) {
                    // 9:10 am 이므로 9시 슬롯에서 약간 아래에 배치
                    Box(modifier = Modifier.padding(top = 15.dp)) {
                        eventContent()
                    }
                }
            }
        }
    }
}

// --- 일정 아이템 카드 ---
@Composable
fun ScheduleItemCard(
    icon: ImageVector,
    title: String,
    time: String,
    petTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
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
                // 병원 방문 부분: Semibold, 12
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

                // 반려동물 태그
                PetTag(name = petTag)
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
        color = Color.LightGray.copy(alpha = 0.5f) // 반투명 회색
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
    HowsuTheme { // 본인 테마 Composable 사용
        val navController = rememberNavController()

        // 완성된 ScheduleScreen을 프리뷰에서 바로 호출
        ScheduleScreen(navController = navController)
    }
}
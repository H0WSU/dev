package com.example.howsu.screen.schedule

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.ui.theme.HowsuTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(
    navController: NavHostController,
    scheduleId: String?,
    viewModel: ScheduleViewModel = viewModel()
) {
    LaunchedEffect(key1 = scheduleId) {
        viewModel.loadScheduleDetails(scheduleId)
    }

    val refreshTrigger = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh_needed", false) // 1. "refresh_needed" 키를 관찰
        ?.collectAsState()

    LaunchedEffect(key1 = refreshTrigger?.value) {
        if (refreshTrigger?.value == true) {
            // 2. 신호가 true이면, 데이터를 강제로 새로고침
            viewModel.loadScheduleDetails(scheduleId)

            // 3. (중요) 신호를 받았으니 다시 false로 리셋
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_needed", false)
        }
    }

    val schedule by viewModel.selectedSchedule.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DetailTopBar(
                onBackClick = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_needed", true)
                    navController.popBackStack()
                },
                onEditClick = {
                    navController.navigate("edit_schedule/$scheduleId")
                }
            )
        },
        bottomBar = {
            DeleteScheduleBottomButton(
                onDeleteClick = {
                    if (scheduleId != null) {
                        viewModel.deleteSchedule(scheduleId) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refresh_needed", true)
                            navController.popBackStack()
                        }
                    } else {
                        Log.e("ScheduleDetailScreen", "scheduleId가 null이라 삭제할 수 없습니다.")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (schedule == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val scheduleData = schedule!!

        // 시간 포맷팅
        val dateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
        val timeFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
        val zoneId = ZoneId.systemDefault()

        val startDateStr = scheduleData.startDate.toDate().toInstant().atZone(zoneId)
            .format(dateFormatter)
        val startTimeStr = scheduleData.startDate.toDate().toInstant().atZone(zoneId)
            .format(timeFormatter)
        val endDateStr = scheduleData.endDate.toDate().toInstant().atZone(zoneId)
            .format(dateFormatter)
        val endTimeStr = scheduleData.endDate.toDate().toInstant().atZone(zoneId)
            .format(timeFormatter)

        // 색상 파싱
        val scheduleColor = try {
            Color(android.graphics.Color.parseColor(scheduleData.color))
        } catch (e: Exception) {
            Color(0xFF4285F4) // 파싱 실패 시 파란색
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            // ★ (수정) '수정하기' 화면과 동일하게 20.dp
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ★ (수정) '수정하기' 화면과 동일하게 1.dp
            Spacer(modifier = Modifier.height(1.dp))

            // --- 제목 --- (CreateScheduleScreen과 스타일 동일하게 맞춤)
            TextField(
                value = scheduleData.title,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("제목", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.mood),
                            contentDescription = "기분",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(scheduleColor)
                                .border(BorderStroke(1.dp, Color.LightGray), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.LightGray.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.5f),
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )

            // --- "하루 종일" (★ 수정됨) ---
            DetailAllDaySwitchRow(
                icon = Icons.Default.Schedule,
                title = "하루 종일",
                isChecked = scheduleData.isAllDay
            )

            // --- "날짜" (★ '하루 종일'이 아닐 때만 표시) ---
            if (!scheduleData.isAllDay) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp) // '수정하기'와 동일한 16dp
                ) {
                    // --- 1. 시작 날짜/시간 ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(startDateStr, fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(startTimeStr, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "에서",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // --- 3. 종료 날짜/시간 ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(endDateStr, fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(endTimeStr, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // --- "반복" (★ 수정됨) ---
            DetailSelectRow(
                icon = Icons.Default.Refresh,
                title = "일정 반복",
                value = "매일" // (TODO: scheduleData.repeatRule)
            )

            // --- "알림" (★ 수정됨) ---
            DetailSelectRow(
                icon = Icons.Default.Notifications,
                title = "일정 미리 알림",
                value = "1일 전 오후 5:00" // (TODO: scheduleData.alarmRule)
            )

            // --- "한 줄 메모" (★ 수정됨) ---
            DetailInfoColumn(
                icon = Icons.Default.Comment,
                title = "한 줄 메모"
            ) {
                OutlinedTextField(
                    value = scheduleData.memo,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false, // 비활성화 상태로 readOnly 스타일 적용
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(17.dp), // '수정하기'와 동일
                    placeholder = { Text(
                        "메모가 없습니다.",
                        fontWeight = FontWeight.Medium, // '수정하기'와 동일
                        fontSize = 13.sp // '수정하기'와 동일
                    ) },
                    maxLines = 3
                )
            }

            // --- "반료동물" (★ 수정됨) ---
            DetailInfoColumn(
                icon = Icons.Default.Pets,
                title = "반려동물" // '수정하기'에서는 "반려동물 선택"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // '수정하기'의 PetTagChip 대신 PetChip 사용 (스타일 다름)
                    scheduleData.petNames.forEach { petName ->
                        PetChip(name = petName)
                    }
                }
            }

            // ★ (수정) '수정하기' 화면과 동일하게 32.dp
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- TopBar (수정 없음) ---
@Composable
private fun DetailTopBar(onBackClick: () -> Unit, onEditClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.ArrowBack, "뒤로가기", modifier = Modifier.size(24.dp))
        }
        Text(
            "일정 자세히 보기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Edit, "수정하기", modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun DetailAllDaySwitchRow(
    icon: ImageVector,
    title: String,
    isChecked: Boolean
) {
    // 2. (신규) '수정' 화면과 동일한 색상 정의
    val customSwitchColors = SwitchDefaults.colors(
        checkedTrackColor = Color.Black,
        checkedThumbColor = Color.White,
        uncheckedTrackColor = Color.LightGray,
        uncheckedThumbColor = Color.White,
        uncheckedBorderColor = Color.LightGray,

        // ★ '자세히 보기'는 disabled 상태이므로 이 색상이 적용됨
        disabledCheckedTrackColor = Color.Black,
        disabledCheckedThumbColor = Color.White,
        disabledUncheckedTrackColor = Color.LightGray,
        disabledUncheckedThumbColor = Color.White,
        disabledUncheckedBorderColor = Color.LightGray
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // '수정'과 동일한 8dp
    ) {
        Icon(
            icon,
            title,
            modifier = Modifier.size(22.dp) // '수정'과 동일한 22dp
        )
        Spacer(modifier = Modifier.width(12.dp)) // '수정'과 동일한 12dp
        Text(
            title,
            fontSize = 14.sp, // '수정'과 동일한 14sp
            fontWeight = FontWeight.Bold // '수정'과 동일한 Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = null,
            enabled = false, // 자세히 보기이므로 비활성화
            modifier = Modifier.scale(0.8f), // '수정'과 동일한 스케일
            colors = customSwitchColors // 3. (신규) colors 속성 적용
        )
    }
}

// --- ★ (신규) "반복", "알림" Row ---
@Composable
private fun DetailSelectRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Column {
        Divider(color = Color.LightGray.copy(alpha = 0.5f)) // '수정'과 동일한 Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp) // '수정'과 동일한 16dp
        ) {
            Icon(
                icon,
                title,
                modifier = Modifier.size(22.dp) // '수정'과 동일한 22dp
            )
            Spacer(modifier = Modifier.width(12.dp)) // '수정'과 동일한 12dp
            Text(
                title,
                fontSize = 14.sp, // '수정'과 동일한 14sp
                fontWeight = FontWeight.Bold // '수정'과 동일한 Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                value,
                fontSize = 14.sp, // '수정'과 동일한 14sp
                fontWeight = FontWeight.Bold, // '수정'과 동일한 Bold
                color = Color.Gray
            )
            // '자세히 보기'에는 'v' 아이콘이 없으므로 생략
        }
    }
}

// --- ★ (신규) "메모", "반려동물" Column ---
@Composable
private fun DetailInfoColumn(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp) // '수정'과 동일
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                title,
                modifier = Modifier.size(22.dp) // '수정'과 동일
            )
            Spacer(modifier = Modifier.width(8.dp)) // '수정'과 동일
            Text(
                title,
                fontSize = 14.sp, // '수정'과 동일
                fontWeight = FontWeight.Bold // '수정'과 동일
            )
        }
        // '수정' (22dp + 8dp = 30dp)와 동일한 패딩
        Box(modifier = Modifier.padding(start = 30.dp)) {
            content()
        }
    }
}


// --- PetChip (수정 없음) ---
@Composable
private fun PetChip(name: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background), // TODO: 펫 이미지
                contentDescription = name,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
            Text(name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
    }
}

// --- BottomBar (수정 없음) ---
@Composable
private fun DeleteScheduleBottomButton(onDeleteClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 60.dp)
    ) {
        Button(
            onClick = onDeleteClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("삭제하기", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

// --- Preview (수정 없음) ---
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ScheduleDetailScreenPreview() {
    HowsuTheme {
        ScheduleDetailScreen(rememberNavController(), "123")
    }
}
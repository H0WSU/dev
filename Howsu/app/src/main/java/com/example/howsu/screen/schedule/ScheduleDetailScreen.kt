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
import androidx.compose.foundation.layout.navigationBarsPadding // ★ 1. (신규) 네비게이션 바 패딩
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
import androidx.compose.material3.ModalBottomSheet // ★ 2. (신규) 바텀 시트
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState // ★ 3. (신규) 바텀 시트 상태
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope // ★ 4. (신규) 코루틴 스코프
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.ui.theme.HowsuTheme
import kotlinx.coroutines.launch // ★ 5. (신규) 코루틴 launch
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
        ?.getStateFlow("refresh_needed", false)
        ?.collectAsState()

    LaunchedEffect(key1 = refreshTrigger?.value) {
        if (refreshTrigger?.value == true) {
            viewModel.loadScheduleDetails(scheduleId)
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_needed", false)
        }
    }

    val schedule by viewModel.selectedSchedule.collectAsState()

    // ★★★ 6. (수정) 바텀 시트 상태 관리
    var showDeleteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

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
                    // ★★★ 7. (수정) 삭제 버튼 클릭 시 시트 표시
                    if (scheduleId != null) {
                        showDeleteSheet = true
                    } else {
                        Log.e("ScheduleDetailScreen", "scheduleId가 null이라 삭제할 수 없습니다.")
                    }
                }
            )
        }
    ) { innerPadding ->

        // ★★★ 8. (수정) 바텀 시트 로직
        if (showDeleteSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDeleteSheet = false },
                sheetState = sheetState
            ) {
                // 바텀 시트의 내용물 (새로 만든 Composable)
                DeleteOptionsBottomSheet(
                    onConfirm = { deletionType ->
                        // 1. 시트를 닫음
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showDeleteSheet = false
                            }
                        }
                        // 2. 뷰모델 호출
                        viewModel.deleteSchedule(deletionType) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refresh_needed", true)
                            navController.popBackStack()
                        }
                    },
                    onDismiss = {
                        // '취소' 버튼 클릭 시
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showDeleteSheet = false
                            }
                        }
                    }
                )
            }
        }


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

        // --- (이하 기존 UI 코드는 변경 없음) ---
        val scheduleData = schedule!!
        val dateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
        val timeFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
        val zoneId = ZoneId.systemDefault()
        val startDateStr = scheduleData.startDate.toDate().toInstant().atZone(zoneId).format(dateFormatter)
        val startTimeStr = scheduleData.startDate.toDate().toInstant().atZone(zoneId).format(timeFormatter)
        val endDateStr = scheduleData.endDate.toDate().toInstant().atZone(zoneId).format(dateFormatter)
        val endTimeStr = scheduleData.endDate.toDate().toInstant().atZone(zoneId).format(timeFormatter)
        val scheduleColor = try {
            Color(android.graphics.Color.parseColor(scheduleData.color))
        } catch (e: Exception) { Color(0xFF4285F4) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(1.dp))

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

            DetailAllDaySwitchRow(
                icon = Icons.Default.Schedule,
                title = "하루 종일",
                isChecked = scheduleData.isAllDay
            )

            if (!scheduleData.isAllDay) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(endDateStr, fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(endTimeStr, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            DetailSelectRow(
                icon = Icons.Default.Refresh,
                title = "일정 반복",
                value = scheduleData.recurrenceRule
            )
            DetailSelectRow(
                icon = Icons.Default.Notifications,
                title = "일정 미리 알림",
                value = scheduleData.alarmRule
            )
            DetailInfoColumn(
                icon = Icons.Default.Comment,
                title = "한 줄 메모"
            ) {
                OutlinedTextField(
                    value = scheduleData.memo,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(17.dp),
                    placeholder = { Text(
                        "메모가 없습니다.",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ) },
                    maxLines = 3
                )
            }
            DetailInfoColumn(
                icon = Icons.Default.Pets,
                title = "반려동물"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    scheduleData.petNames.forEach { petName ->
                        PetChip(name = petName)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ★★★ 바텀 시트 전용 Content Composable ★★★
@Composable
fun DeleteOptionsBottomSheet(
    onConfirm: (DeletionType) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // 하단 네비게이션 바 겹침 방지
            .padding(top = 8.dp)
    ) {

        // (옵션 1: 단일 삭제)
        TextButton(
            onClick = { onConfirm(DeletionType.SINGLE) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text(
                "이 일정만 바로 삭제하기",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface, // ★ 기본 색상
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // (옵션 2: 이후 삭제 - 빨간색)
        TextButton(
            onClick = { onConfirm(DeletionType.FUTURE) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text(
                "이 일정과 이후 반복 일정 모두 삭제하기",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                textAlign = TextAlign.Start,
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // 하단 여백
    }
}

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
    val customSwitchColors = SwitchDefaults.colors(
        checkedTrackColor = Color.Black,
        checkedThumbColor = Color.White,
        uncheckedTrackColor = Color.LightGray,
        uncheckedThumbColor = Color.White,
        uncheckedBorderColor = Color.LightGray,
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
            .padding(vertical = 8.dp)
    ) {
        Icon(icon, title, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = null,
            enabled = false,
            modifier = Modifier.scale(0.8f),
            colors = customSwitchColors
        )
    }
}

@Composable
private fun DetailSelectRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Column {
        Divider(color = Color.LightGray.copy(alpha = 0.5f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Icon(icon, title, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

@Composable
private fun DetailInfoColumn(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, title, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.padding(start = 30.dp)) {
            content()
        }
    }
}


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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ScheduleDetailScreenPreview() {
    HowsuTheme {
        ScheduleDetailScreen(rememberNavController(), "123")
    }
}
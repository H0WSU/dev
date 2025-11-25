package com.example.howsu.screen.schedule

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.howsu.ui.theme.HowsuTheme
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ContentBlack = Color(0xFF121212)

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

    var showDeleteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.White,
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
        }
    ) { innerPadding ->

        if (showDeleteSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDeleteSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                DeleteOptionsBottomSheet(
                    onConfirm = { deletionType ->
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showDeleteSheet = false
                            }
                        }
                        viewModel.deleteSchedule(deletionType) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refresh_needed", true)
                            navController.popBackStack()
                        }
                    },
                    onDismiss = {
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
                CircularProgressIndicator(color = YellowCustom)
            }
            return@Scaffold
        }

        val scheduleData = schedule!!
        val dateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
        val timeFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
        val zoneId = ZoneId.systemDefault()

        val startInstant = scheduleData.startDate.toDate().toInstant().atZone(zoneId)
        val endInstant = scheduleData.endDate.toDate().toInstant().atZone(zoneId)

        val startDateStr = startInstant.format(dateFormatter)
        val startTimeStr = startInstant.format(timeFormatter)
        val endDateStr = endInstant.format(dateFormatter)
        val endTimeStr = endInstant.format(timeFormatter)

        val scheduleColor = try {
            Color(android.graphics.Color.parseColor(scheduleData.color))
        } catch (e: Exception) { Color(0xFF4285F4) }

        val dividerColor = Color.LightGray.copy(alpha = 0.5f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(1.dp))

                // 1. 제목 필드
                TextField(
                    value = scheduleData.title,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ContentBlack
                    ),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(scheduleColor)
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
                        disabledTrailingIconColor = ContentBlack,
                        focusedTrailingIconColor = ContentBlack,
                        unfocusedTrailingIconColor = ContentBlack
                    ),
                    maxLines = 1
                )

                // 2. 하루 종일 스위치
                DetailAllDaySwitchRow(
                    icon = Icons.Default.Schedule,
                    title = "하루 종일",
                    isChecked = scheduleData.isAllDay
                )

                // 3. 시간 표시
                if (!scheduleData.isAllDay) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(startDateStr, fontSize = 14.sp, color = ContentBlack.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(startTimeStr, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ContentBlack)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "에서",
                            modifier = Modifier.size(20.dp),
                            tint = ContentBlack
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(endDateStr, fontSize = 14.sp, color = ContentBlack.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(endTimeStr, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ContentBlack)
                        }
                    }
                }

                // 4. 일정 반복
                DetailSelectRow(
                    icon = Icons.Default.Refresh,
                    title = "일정 반복",
                    value = scheduleData.recurrenceRule
                )

                // 5. 일정 미리 알림
                DetailSelectRow(
                    icon = Icons.Default.Notifications,
                    title = "일정 미리 알림",
                    value = scheduleData.alarmRule
                )

                Divider(color = dividerColor)

                // 6. 한 줄 메모
                DetailInfoSection(
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
                            "메모가 없습니다",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color.Gray
                        ) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ContentBlack,
                            unfocusedBorderColor = ContentBlack,
                            disabledBorderColor = ContentBlack,
                            disabledTextColor = ContentBlack,
                            cursorColor = ContentBlack,
                            focusedTextColor = ContentBlack,
                            unfocusedTextColor = ContentBlack,
                            disabledContainerColor = Color.Transparent
                        )
                    )
                }

                Divider(color = dividerColor)

                // 7. 반려동물 섹션
                if (scheduleData.petNames.isNotEmpty()) {
                    DetailInfoSection(
                        icon = Icons.Default.Pets,
                        title = "반려동물"
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            scheduleData.petNames.zip(scheduleData.petProfileUrls).forEach { (petName, petUrl) ->
                                PetChip(name = petName, imageUrl = petUrl)
                            }
                        }
                    }
                }
            }

            // 삭제 버튼
            DeleteScheduleBottomButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                onDeleteClick = {
                    if (scheduleId != null) {
                        showDeleteSheet = true
                    } else {
                        Log.e("ScheduleDetailScreen", "scheduleId가 null이라 삭제할 수 없습니다.")
                    }
                }
            )
        }
    }
}

// --- Composable Functions ---

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
            modifier = Modifier.size(39.dp).align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.ArrowBack, "뒤로가기", modifier = Modifier.size(24.dp), tint = ContentBlack)
        }
        Text(
            "일정 자세히 보기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center),
            color = ContentBlack
        )
        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(39.dp).align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Edit, "수정하기", modifier = Modifier.size(24.dp), tint = ContentBlack)
        }
    }
}

@Composable
private fun DetailInfoSection(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, title, modifier = Modifier.size(22.dp), tint = ContentBlack)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ContentBlack)
        }
        content()
    }
}

@Composable
private fun DetailAllDaySwitchRow(
    icon: ImageVector,
    title: String,
    isChecked: Boolean
) {
    // ★ CreateScreen과 동일하게 padding(vertical = 8.dp) 복구
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(icon, title, modifier = Modifier.size(22.dp), tint = ContentBlack)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ContentBlack)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = null,
            enabled = false,
            modifier = Modifier.scale(0.8f),
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color(0xFFFFDF37),
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.LightGray,
                disabledCheckedTrackColor = Color(0xFFFFDF37),
                disabledCheckedThumbColor = Color.White,
                disabledUncheckedTrackColor = Color.LightGray,
                disabledUncheckedThumbColor = Color.White,
                disabledUncheckedBorderColor = Color.LightGray
            )
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
        // ★ CreateScreen과 동일하게 padding(vertical = 16.dp) 복구
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Icon(icon, title, modifier = Modifier.size(22.dp), tint = ContentBlack)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ContentBlack)
            Spacer(modifier = Modifier.weight(1f))

            // ★ CreateScreen에는 화살표 아이콘이 있어 글자가 안쪽에 있었으므로,
            // 여기서도 화살표가 없는 대신 padding(end)를 주어 위치를 맞춤
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(end = 4.dp) // 약간 안쪽으로 들어오게
            )
        }
    }
}

@Composable
private fun PetChip(name: String, imageUrl: String?) {
    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(16.dp),
        color = YellowCustom,
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!imageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.size(24.dp).clip(CircleShape)
                )
            } else {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.howsu.R.drawable.ic_launcher_background),
                    contentDescription = name,
                    modifier = Modifier.size(24.dp).clip(CircleShape)
                )
            }
            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ContentBlack)
        }
    }
}

@Composable
private fun DeleteScheduleBottomButton(
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Button(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowCustom,
                contentColor = ContentBlack
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("삭제하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun DeleteOptionsBottomSheet(
    onConfirm: (DeletionType) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 20.dp)
    ) {
        TextButton(
            onClick = { onConfirm(DeletionType.SINGLE) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
        ) {
            Text(
                "이 일정만 바로 삭제하기",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                textAlign = TextAlign.Start,
                color = ContentBlack,
                fontSize = 16.sp
            )
        }
        TextButton(
            onClick = { onConfirm(DeletionType.FUTURE) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text(
                "이 일정과 이후 반복 일정 모두 삭제하기",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                textAlign = TextAlign.Start,
                color = Color.Red,
                fontSize = 16.sp
            )
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
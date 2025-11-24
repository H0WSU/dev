package com.example.howsu.screen.schedule

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.data.model.Pet
import com.example.howsu.ui.theme.HowsuTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleScreen(
    navController: NavHostController,
    scheduleId: String? = null,
    viewModel: CreateScheduleViewModel = viewModel()
) {
    // --- (기존 상태 구독 - 변경 없음) ---
    val allPets by viewModel.allPets.collectAsState()
    val selectedPets by viewModel.selectedPets.collectAsState()
    val isPetDropdownVisible by viewModel.isPetDropdownVisible.collectAsState()
    val title by viewModel.title.collectAsState()
    val memo by viewModel.memo.collectAsState()
    val isAllDay by viewModel.isAllDay.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val predefinedColors = viewModel.predefinedColors
    val isColorPickerVisible by viewModel.isColorPickerVisible.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val showTimePicker by viewModel.showTimePicker.collectAsState()
    val pickerTarget by viewModel.pickerTarget.collectAsState()
    val recurrenceRule by viewModel.recurrenceRule.collectAsState()
    val showRecurrencePicker by viewModel.showRecurrencePicker.collectAsState()
    val recurrenceOptions = viewModel.recurrenceOptions
    val alarmRule by viewModel.alarmRule.collectAsState()
    val showAlarmPicker by viewModel.showAlarmPicker.collectAsState()
    val alarmOptions = viewModel.alarmOptions
    val recurrenceEndDate by viewModel.recurrenceEndDate.collectAsState()
    val showRecurrenceEndDatePicker by viewModel.showRecurrenceEndDatePicker.collectAsState()

    // --- (기존 쉐이크 애니메이션 - 변경 없음) ---
    val scope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }
    fun triggerShake() {
        scope.launch {
            shakeOffset.animateTo(0f)
            repeat(3) {
                shakeOffset.animateTo(15f, tween(50))
                shakeOffset.animateTo(-15f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.initialize(scheduleId)
    }

    // --- (기존 다이얼로그 - 변경 없음) ---
    if (showDatePicker) {
        val targetMillis = if (pickerTarget == DateTimePickerTarget.START) startDate else endDate
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = targetMillis
        )
        DatePickerDialog(
            onDismissRequest = viewModel::onDatePickerDismissed,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDateSelected(datePickerState.selectedDateMillis)
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDatePickerDismissed) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val targetMillis = if (pickerTarget == DateTimePickerTarget.START) startDate else endDate
        val initialTime = Date(targetMillis)
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hours,
            initialMinute = initialTime.minutes
        )
        TimePickerDialog(
            onDismissRequest = viewModel::onTimePickerDismissed,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onTimeSelected(timePickerState.hour, timePickerState.minute)
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onTimePickerDismissed) { Text("취소") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showRecurrenceEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = recurrenceEndDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onRecurrenceEndDateSelected(null) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onRecurrenceEndDateSelected(datePickerState.selectedDateMillis)
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onRecurrenceEndDateSelected(null) }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CreateScheduleTopBar(
                title = if (scheduleId == null) "일정 생성하기" else "일정 수정하기",
                onCloseClick = { navController.popBackStack() }
            )
        },
        // ★★★ (수정) bottomBar 속성 제거
        // bottomBar = { ... }
    ) { innerPadding ->

        // ★★★ (신규) Box로 래핑하여 버튼을 띄움
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // TopAppBar의 패딩은 여전히 적용
        ) {
            // (기존) 스크롤 가능한 본문
            CreateScheduleContent (
                modifier = Modifier.fillMaxSize(), // ★ Box를 꽉 채우도록
                shakeOffset = shakeOffset.value,
                title = title,
                memo = memo,
                isAllDay = isAllDay,
                allPets = allPets,
                selectedPets = selectedPets,
                isPetDropdownVisible = isPetDropdownVisible,
                selectedColor = selectedColor,
                predefinedColors = predefinedColors,
                isColorPickerVisible = isColorPickerVisible,
                onColorSelected = viewModel::onColorSelected,
                onColorPickerClicked = viewModel::onColorPickerClicked,
                onColorPickerDismissed = viewModel::onColorPickerDismissed,
                onTitleChanged = viewModel::onTitleChanged,
                onMemoChanged = viewModel::onMemoChanged,
                onAllDayToggled = viewModel::onAllDayToggled,
                onPetDropdownClicked = viewModel::onPetDropdownClicked,
                onPetDropdownDismissed = viewModel::onPetDropdownDismissed,
                onPetSelected = viewModel::onPetSelected,
                onPetTagRemoved = viewModel::onPetTagRemoved,
                startDate = startDate,
                endDate = endDate,
                onDatePickerClicked = viewModel::onDatePickerClicked,
                onTimePickerClicked = viewModel::onTimePickerClicked,
                recurrenceRule = recurrenceRule,
                showRecurrencePicker = showRecurrencePicker,
                recurrenceOptions = recurrenceOptions,
                onRecurrenceClicked = viewModel::onRecurrenceClicked,
                onRecurrenceDismissed = viewModel::onRecurrenceDismissed,
                onRecurrenceSelected = viewModel::onRecurrenceSelected,
                alarmRule = alarmRule,
                showAlarmPicker = showAlarmPicker,
                alarmOptions = alarmOptions,
                onAlarmClicked = viewModel::onAlarmClicked,
                onAlarmDismissed = viewModel::onAlarmDismissed,
                onAlarmSelected = viewModel::onAlarmSelected,
                recurrenceEndDate = recurrenceEndDate,
                onRecurrenceEndDateClicked = viewModel::onRecurrenceEndDateClicked
            )

            // ★★★ (신규) 하단 버튼을 Box의 바닥에 정렬
            CreateScheduleBottomButton(
                modifier = Modifier.align(Alignment.BottomCenter), // ★ 여기에 배치
                onCreateClick = {
                    if (title.isBlank()) {
                        triggerShake()
                    } else {
                        viewModel.saveSchedule(context = context) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refresh_needed", true)
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    }
}

// (기존) 상단 바 - 변경 없음
@Composable
private fun CreateScheduleTopBar(
    title: String,
    onCloseClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ★★★ (수정) 하단 버튼 - modifier 파라미터 추가, 패딩 수정
@Composable
private fun CreateScheduleBottomButton(
    modifier: Modifier = Modifier, // ★ 1. modifier 파라미터 추가
    onCreateClick: () -> Unit
) {
    Column(
        modifier = modifier // ★ 2. 전달받은 modifier 사용 (align(BottomCenter))
            .fillMaxWidth()
            // ★ 3. 배경을 투명하게 (Scaffold 배경이 보이도록)
            .background(Color.Transparent)
            // ★ 4. (수정) 패딩 변경 (상단 공백 16dp, 하단 공백 32dp)
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 16.dp,
                bottom = 16.dp
            )
    ) {
        Button(
            onClick = onCreateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(Color.Black, Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("저장하기", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

// (기존) 섹션 래퍼 - 변경 없음
@Composable
private fun CreateScheduleSection(
    icon: Painter,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        content()
    }
}


// ★★★ (수정) 본문 (스크롤 영역) - 하단 패딩 추가
@Composable
private fun CreateScheduleContent(
    modifier: Modifier = Modifier,
    shakeOffset: Float,
    title: String,
    memo: String,
    isAllDay: Boolean,
    allPets: List<Pet>,
    selectedPets: List<Pet>,
    isPetDropdownVisible: Boolean,
    selectedColor: String,
    predefinedColors: List<String>,
    isColorPickerVisible: Boolean,
    onColorSelected: (String) -> Unit,
    onColorPickerClicked: () -> Unit,
    onColorPickerDismissed: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onMemoChanged: (String) -> Unit,
    onAllDayToggled: (Boolean) -> Unit,
    onPetDropdownClicked: () -> Unit,
    onPetDropdownDismissed: () -> Unit,
    onPetSelected: (Pet) -> Unit,
    onPetTagRemoved: (Pet) -> Unit,
    startDate: Long,
    endDate: Long,
    onDatePickerClicked: (DateTimePickerTarget) -> Unit,
    onTimePickerClicked: (DateTimePickerTarget) -> Unit,
    recurrenceRule: String,
    showRecurrencePicker: Boolean,
    recurrenceOptions: List<String>,
    onRecurrenceClicked: () -> Unit,
    onRecurrenceDismissed: () -> Unit,
    onRecurrenceSelected: (String) -> Unit,
    alarmRule: String,
    showAlarmPicker: Boolean,
    alarmOptions: List<String>,
    onAlarmClicked: () -> Unit,
    onAlarmDismissed: () -> Unit,
    onAlarmSelected: (String) -> Unit,
    recurrenceEndDate: Long?,
    onRecurrenceEndDateClicked: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("yyyy.MM.dd 까지", Locale.KOREAN)
    val recurrenceEndDateStr = recurrenceEndDate?.let { dateFormatter.format(Date(it)) } ?: "계속 반복"

    Column(
        modifier = modifier
            // .fillMaxSize() // ★ Box에서 이미 적용됨
            .verticalScroll(rememberScrollState())
            // ★ (수정) padding(horizontal = 24.dp) -> padding(...)
            .padding(
                start = 24.dp,
                end = 24.dp,
                // ★ (신규) 버튼 높이(56) + 상하패딩(16+32) = 104dp + 여유 16dp
                bottom = 120.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(1.dp))

        // --- (기존) 섹션 1: 제목 ---
        ScheduleTitleField(
            shakeOffset = shakeOffset,
            title = title,
            selectedColor = selectedColor,
            onTitleChanged = onTitleChanged,
            onColorPickerClicked = onColorPickerClicked,
            isColorPickerVisible = isColorPickerVisible,
            predefinedColors = predefinedColors,
            onColorPickerDismissed = onColorPickerDismissed,
            onColorSelected = onColorSelected
        )

        // --- (기존) 섹션 2: 하루 종일 ---
        AllDaySwitch(
            isChecked = isAllDay,
            onCheckedChange = onAllDayToggled
        )

        // --- (기존) 시간 선택 ---
        if (!isAllDay) {
            ScheduleTimePicker(
                startDate = startDate,
                endDate = endDate,
                onStartDateClick = { onDatePickerClicked(DateTimePickerTarget.START) },
                onStartTimeClick = { onTimePickerClicked(DateTimePickerTarget.START) },
                onEndDateClick = { onDatePickerClicked(DateTimePickerTarget.END) },
                onEndTimeClick = { onTimePickerClicked(DateTimePickerTarget.END) }
            )
        }

        // --- (기존) 일정 반복 ---
        Box(modifier = Modifier.fillMaxWidth()) {
            ScheduleSelectRow(
                icon = Icons.Default.Refresh,
                title = "일정 반복",
                value = recurrenceRule,
                onClick = onRecurrenceClicked
            )
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                DropdownMenu(
                    expanded = showRecurrencePicker,
                    onDismissRequest = onRecurrenceDismissed
                ) {
                    recurrenceOptions.forEach { rule ->
                        DropdownMenuItem(
                            text = { Text(rule) },
                            onClick = { onRecurrenceSelected(rule) }
                        )
                    }
                }
            }
        }

        // --- (기존) '반복 종료 날짜' UI ---
        if (recurrenceRule != "반복 안 함") {
            Box(modifier = Modifier.fillMaxWidth()) {
                ScheduleSelectRow(
                    icon = Icons.Default.Event,
                    title = "반복 종료",
                    value = recurrenceEndDateStr,
                    onClick = onRecurrenceEndDateClicked
                )
            }
        }

        // --- (기존) 일정 미리 알림 ---
        Box(modifier = Modifier.fillMaxWidth()) {
            ScheduleSelectRow(
                icon = Icons.Default.Notifications,
                title = "일정 미리 알림",
                value = alarmRule,
                onClick = onAlarmClicked
            )
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                DropdownMenu(
                    expanded = showAlarmPicker,
                    onDismissRequest = onAlarmDismissed
                ) {
                    alarmOptions.forEach { rule ->
                        DropdownMenuItem(
                            text = { Text(rule) },
                            onClick = { onAlarmSelected(rule) }
                        )
                    }
                }
            }
        }

        // --- (기존) 섹션 6: 한 줄 메모 ---
        CreateScheduleSection(
            icon = rememberVectorPainter(image = Icons.Default.Comment),
            title = "한 줄 메모"
        ) { ScheduleMemoField(memo = memo, onMemoChanged = onMemoChanged) }

        // --- (기존) 섹션 7: 반려동물 선택 ---
        CreateScheduleSection(
            icon = rememberVectorPainter(image = Icons.Default.Pets),
            title = "반려동물 선택"
        ) {
            PetSelector(
                allPets = allPets,
                selectedPets = selectedPets,
                isDropdownVisible = isPetDropdownVisible,
                onDropdownClicked = onPetDropdownClicked,
                onDropdownDismissed = onPetDropdownDismissed,
                onPetSelected = onPetSelected,
                onPetTagRemoved = onPetTagRemoved
            )
        }

        // (삭제) Spacer(modifier = Modifier.height(32.dp)) -> 상단 Column의 padding(bottom=120.dp)로 대체
    }
}

// --- (이하 나머지 Composable 함수들 - 변경 없음) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTitleField(
    shakeOffset: Float,
    title: String,
    selectedColor: String,
    onTitleChanged: (String) -> Unit,
    onColorPickerClicked: () -> Unit,
    isColorPickerVisible: Boolean,
    predefinedColors: List<String>,
    onColorPickerDismissed: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    TextField(
        value = title,
        onValueChange = onTitleChanged,
        placeholder = { Text("제목", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = shakeOffset
            },
        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
        trailingIcon = {
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.mood),
                        contentDescription = "이모티콘",
                        modifier = Modifier.size(24.dp).clickable { /* TODO */ }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(selectedColor)))
                            .clickable { onColorPickerClicked() }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                DropdownMenu(
                    expanded = isColorPickerVisible,
                    onDismissRequest = onColorPickerDismissed
                ) {
                    ColorPickerRow(
                        colors = predefinedColors,
                        selectedColor = selectedColor,
                        onColorSelected = onColorSelected
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.LightGray.copy(alpha = 0.5f),
            unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.5f)
        ),
        maxLines = 1
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPickerRow(
    colors: List<String>,
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .width(240.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        colors.forEach { colorHex ->
            val isSelected = (colorHex == selectedColor)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                    .clickable { onColorSelected(colorHex) }
                    .border(
                        BorderStroke(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                        ),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun AllDaySwitch(isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 8.dp)
    ) {
        Icon(Icons.Default.Schedule, "하루 종일", modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text("하루 종일", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.8f),
            colors = customSwitchColors
        )
    }
}

@Composable
private fun ScheduleTimePicker(
    startDate: Long,
    endDate: Long,
    onStartDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
    val timeFormatter = SimpleDateFormat("a hh:mm", Locale.KOREAN)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dateFormatter.format(Date(startDate)),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.clickable(onClick = onStartDateClick)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeFormatter.format(Date(startDate)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onStartTimeClick)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Icon(
            Icons.Default.ArrowForward,
            "에서",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = dateFormatter.format(Date(endDate)),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.clickable(onClick = onEndDateClick)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeFormatter.format(Date(endDate)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onEndTimeClick)
            )
        }
    }
}

@Composable
private fun ScheduleSelectRow(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Column {
        Divider(color = Color.LightGray.copy(alpha = 0.5f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp)
        ) {
            Icon(icon, title, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Icon(Icons.Default.KeyboardArrowDown, "선택", modifier = Modifier.size(20.dp), tint = Color.Gray)
        }
    }
}
@Composable
private fun ScheduleMemoField(memo: String, onMemoChanged: (String) -> Unit) {
    val maxChars = 20
    Column {
        OutlinedTextField(
            value = memo,
            onValueChange = onMemoChanged,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            placeholder = { Text("메모 입력하기", fontWeight = FontWeight.Medium, fontSize = 13.sp) },
            maxLines = 3,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${memo.length}/$maxChars",
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PetSelector(
    allPets: List<Pet>, selectedPets: List<Pet>, isDropdownVisible: Boolean,
    onDropdownClicked: () -> Unit, onDropdownDismissed: () -> Unit,
    onPetSelected: (Pet) -> Unit, onPetTagRemoved: (Pet) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(17.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onDropdownClicked
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ★★★ [수정] 선택된 펫이 있으면 겹친 아이콘 표시
                    if (selectedPets.isEmpty()) {
                        Image(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "펫 프로필",
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                        )
                    } else {
                        val firstPet = selectedPets.first()
                        if (!firstPet.profileImageUrl.isNullOrBlank()) {
                            coil.compose.AsyncImage(
                                model = firstPet.profileImageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp).clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Pets, null, modifier = Modifier.size(24.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (selectedPets.isEmpty()) "반려동물을 선택해 주세요"
                        else selectedPets.joinToString { it.name },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedPets.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowDown, "열기", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            DropdownMenu(
                expanded = isDropdownVisible,
                onDismissRequest = onDropdownDismissed,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                allPets.forEach { pet ->
                    DropdownMenuItem(text = { Text(pet.name) }, onClick = { onPetSelected(pet) })
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            selectedPets.forEach { pet ->
                PetTagChip(pet = pet, onRemoveClick = { onPetTagRemoved(pet) })
            }
        }
    }
}
@Composable
private fun PetTagChip(pet: Pet, onRemoveClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color.Gray) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(pet.name, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Close, "${pet.name} 삭제", modifier = Modifier
                .size(16.dp)
                .clickable(onClick = onRemoveClick))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CreateScheduleScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        CreateScheduleScreen(navController = navController)
    }
}
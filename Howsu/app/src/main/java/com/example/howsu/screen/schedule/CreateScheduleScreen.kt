package com.example.howsu.screen.schedule

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
    // --- ViewModel 상태 구독 ---
    val allPets by viewModel.allPets.collectAsState()
    val selectedPets by viewModel.selectedPets.collectAsState()
    val isPetDropdownVisible by viewModel.isPetDropdownVisible.collectAsState()
    val title by viewModel.title.collectAsState()
    val memo by viewModel.memo.collectAsState()
    val isAllDay by viewModel.isAllDay.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val predefinedColors = viewModel.predefinedColors
    val isColorPickerVisible by viewModel.isColorPickerVisible.collectAsState()

    // ★ 날짜/시간 피커 상태
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val showTimePicker by viewModel.showTimePicker.collectAsState()
    val pickerTarget by viewModel.pickerTarget.collectAsState()

    // ★ 반복/알림 상태
    val recurrenceRule by viewModel.recurrenceRule.collectAsState()
    val showRecurrencePicker by viewModel.showRecurrencePicker.collectAsState()
    val recurrenceOptions = viewModel.recurrenceOptions
    val alarmRule by viewModel.alarmRule.collectAsState()
    val showAlarmPicker by viewModel.showAlarmPicker.collectAsState()
    val alarmOptions = viewModel.alarmOptions

    LaunchedEffect(key1 = Unit) {
        viewModel.initialize(scheduleId)
    }

    // --- ★ (신규) 날짜/시간 선택 다이얼로그 ---
    // 날짜 선택기
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

    // 시간 선택기
    if (showTimePicker) {
        val targetMillis = if (pickerTarget == DateTimePickerTarget.START) startDate else endDate
        val initialTime = Date(targetMillis)
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hours,
            initialMinute = initialTime.minutes
        )
        TimePickerDialog( // (이 함수는 아래 13번에 새로 만듭니다)
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CreateScheduleTopBar(
                title = if (scheduleId == null) "일정 생성하기" else "일정 수정하기",
                onCloseClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CreateScheduleBottomButton(
                onCreateClick = {
                    viewModel.saveSchedule {
                        // '자세히 보기' 화면에 "refresh" 신호 전달
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("refresh_needed", true) // "refresh_needed" 키에 true 저장

                        navController.popBackStack() // 2. 그 다음에 뒤로 감
                    }
                }
            )
        }
    ) { innerPadding ->
        CreateScheduleContent (
            modifier = Modifier.padding(innerPadding),
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

            // ★ 상태 및 핸들러 전달
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
            onAlarmSelected = viewModel::onAlarmSelected
        )
    }
}

// 상단 바 (★ 닫기 버튼 테두리 제거됨)
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
            // .border(...) // ★ 테두리 삭제됨
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// 하단 버튼
@Composable
private fun CreateScheduleBottomButton(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 60.dp)
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

// --- 섹션 래퍼 ---
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


// 본문 (스크롤 영역)
@Composable
private fun CreateScheduleContent(
    modifier: Modifier = Modifier,
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

    // ★ 날짜/시간/반복/알림 파라미터
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
    onAlarmSelected: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(1.dp))

        // --- 섹션 1: 제목 ---
        ScheduleTitleField(
            title = title,
            selectedColor = selectedColor,
            onTitleChanged = onTitleChanged,
            onColorPickerClicked = onColorPickerClicked,
            isColorPickerVisible = isColorPickerVisible,
            predefinedColors = predefinedColors,
            onColorPickerDismissed = onColorPickerDismissed,
            onColorSelected = onColorSelected
        )

        // --- 섹션 2: 하루 종일 ---
        AllDaySwitch(
            isChecked = isAllDay,
            onCheckedChange = onAllDayToggled
        )

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

        Box(modifier = Modifier.fillMaxWidth()) { // 클릭 영역을 위한 Box
            ScheduleSelectRow(
                icon = Icons.Default.Refresh,
                title = "일정 반복",
                value = recurrenceRule,
                onClick = onRecurrenceClicked // 이 클릭으로 메뉴를 토글
            )

            // ★ 1. 메뉴를 오른쪽 정렬하기 위한 추가 Box
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                // ★ 2. DropdownMenu 자체의 modifier = Modifier.fillMaxWidth() 삭제
                DropdownMenu(
                    expanded = showRecurrencePicker,
                    onDismissRequest = onRecurrenceDismissed
                    // ★ modifier = Modifier.fillMaxWidth() 삭제!
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


        // --- ★ 섹션 5: 일정 미리 알림 (수정됨) ---
        Box(modifier = Modifier.fillMaxWidth()) { // 클릭 영역을 위한 Box
            ScheduleSelectRow(
                icon = Icons.Default.Notifications,
                title = "일정 미리 알림",
                value = alarmRule,
                onClick = onAlarmClicked // 이 클릭으로 메뉴를 토글
            )

            // ★ 1. 메뉴를 오른쪽 정렬하기 위한 추가 Box
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                // ★ 2. DropdownMenu 자체의 modifier = Modifier.fillMaxWidth() 삭제
                DropdownMenu(
                    expanded = showAlarmPicker,
                    onDismissRequest = onAlarmDismissed
                    // ★ modifier = Modifier.fillMaxWidth() 삭제!
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

        // --- 섹션 6: 한 줄 메모 ---
        CreateScheduleSection(
            icon = rememberVectorPainter(image = Icons.Default.Comment),
            title = "한 줄 메모"
        ) { ScheduleMemoField(memo = memo, onMemoChanged = onMemoChanged) }

        // --- 섹션 7: 반려동물 선택 ---
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

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- 본문 컴포넌트들 ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTitleField(
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
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), // 폰트 통일
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
                            .border(BorderStroke(1.dp, Color.LightGray), CircleShape)
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
            // 밑줄 색상 통일
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
    // 2. (신규) 스위치 색상 정의
    val customSwitchColors = SwitchDefaults.colors(
        // "On" 상태 (선택됨)
        checkedTrackColor = Color.Black,
        checkedThumbColor = Color.White,
        // "Off" 상태 (선택 안 됨)
        uncheckedTrackColor = Color.LightGray,
        uncheckedThumbColor = Color.White,
        uncheckedBorderColor = Color.LightGray,

        // (비활성화 상태도 똑같이 보이도록 설정 - DetailScreen용)
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
            colors = customSwitchColors // 3. (신규) colors 속성 적용
        )
    }
}

// --- ★ (수정됨) ScheduleTimePicker ---
@Composable
private fun ScheduleTimePicker(
    startDate: Long,
    endDate: Long,
    onStartDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    // Long(Millis) -> "M월 d일 (E)"
    val dateFormatter = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
    // Long(Millis) -> "오전/오후 hh:mm"
    val timeFormatter = SimpleDateFormat("a hh:mm", Locale.KOREAN)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // --- 시작 시간 ---
        Column(
            // modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally // ★ 1. 시작 Column 중앙 정렬
        ) {
            Text(
                text = dateFormatter.format(Date(startDate)),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.clickable(onClick = onStartDateClick) // 날짜 클릭
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeFormatter.format(Date(startDate)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onStartTimeClick) // 시간 클릭
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // ★ (신규) 왼쪽 스페이서

        Icon(
            Icons.Default.ArrowForward,
            "에서",
            modifier = Modifier
                .size(20.dp)
        )

        Spacer(modifier = Modifier.weight(1f)) // ★ (수정) 오른쪽 스페이서

        // --- 종료 시간 ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // ★ 2. 종료 Column 중앙 정렬
            // modifier = Modifier.weight(1f)
        ) {
            Text(
                text = dateFormatter.format(Date(endDate)),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.clickable(onClick = onEndDateClick) // 날짜 클릭
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeFormatter.format(Date(endDate)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onEndTimeClick) // 시간 클릭
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
                    Image(Icons.Default.AccountCircle, "펫 프로필", modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedPets.isEmpty()) "반려동물을 선택해 주세요" else selectedPets.joinToString { it.name },
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

// --- ★ (신규) TimePicker 다이얼로그 래퍼 ---
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
            shape = RoundedCornerShape(28.dp) // 다이얼로그 모서리
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TimePicker 본문 (시간 선택 휠)
                content()
                // 확인/취소 버튼
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
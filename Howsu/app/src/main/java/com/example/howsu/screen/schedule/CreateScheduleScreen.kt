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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import com.example.howsu.data.model.Pet
import com.example.howsu.ui.theme.HowsuTheme

// --- 1. 메인 화면: Scaffold 뼈대 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleScreen(
    navController: NavHostController,
    viewModel: CreateScheduleViewModel = viewModel()
) {
    val allPets by viewModel.allPets.collectAsState()
    val selectedPets by viewModel.selectedPets.collectAsState()
    val isPetDropdownVisible by viewModel.isPetDropdownVisible.collectAsState()

    val title by viewModel.title.collectAsState()
    val memo by viewModel.memo.collectAsState()
    val isAllDay by viewModel.isAllDay.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CreateScheduleTopBar(
                onCloseClick = { navController.popBackStack() } // 닫기
            )
        },
        
        // 생성 후에는 일정 화면으로 넘어가기
        bottomBar = {
            CreateScheduleBottomButton(
                onCreateClick = {
                    viewModel.createSchedule(
                        onComplete = {
                            navController.navigate("schedule") { // 1. "schedule" 스크린으로 이동
                                popUpTo("create_schedule") { // 2. 지금 화면("create_todo")은
                                    inclusive = true       //    스택에서 포함해서 제거
                                }
                            }
                        }
                    )
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

            onTitleChanged = viewModel::onTitleChanged,
            onMemoChanged = viewModel::onMemoChanged,
            onAllDayToggled = viewModel::onAllDayToggled,
            onPetDropdownClicked = viewModel::onPetDropdownClicked,
            onPetDropdownDismissed = viewModel::onPetDropdownDismissed,
            onPetSelected = viewModel::onPetSelected,
            onPetTagRemoved = viewModel::onPetTagRemoved
        )
    }
}

// 상단 바
@Composable
private fun CreateScheduleTopBar(onCloseClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding() // 상태 표시줄 띄워 주기
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        Text(
            text = "일정 생성하기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterEnd)
                .border(
                    BorderStroke(0.1.dp, Color.LightGray),
                    CircleShape
                )
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
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Button(
            onClick = onCreateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("저장하기", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

// --- 4. 섹션 래퍼 (변경 없음) ---
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
    onTitleChanged: (String) -> Unit,
    onMemoChanged: (String) -> Unit,
    onAllDayToggled: (Boolean) -> Unit,
    onPetDropdownClicked: () -> Unit,
    onPetDropdownDismissed: () -> Unit,
    onPetSelected: (Pet) -> Unit,
    onPetTagRemoved: (Pet) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(1.dp))

        // --- 섹션 1: 제목 (ViewModel과 연결) ---
        ScheduleTitleField(
            title = title,
            onTitleChanged = onTitleChanged
        )

        // --- 섹션 2: 하루 종일 (ViewModel과 연결) ---
        AllDaySwitch(
            isChecked = isAllDay,
            onCheckedChange = onAllDayToggled
        )

        // --- 섹션 3: 날짜/시간 선택 ---
        ScheduleTimePicker() // (TODO: ViewModel과 연결 필요)

        // --- 섹션 4: 일정 반복 ---
        ScheduleSelectRow(
            icon = Icons.Default.Refresh,
            title = "일정 반복",
            value = "반복 없음"
        ) { /* TODO: ViewModel과 연결 필요 */ }

        // --- 섹션 5: 일정 미리 알림 ---
        ScheduleSelectRow(
            icon = Icons.Default.Notifications,
            title = "일정 미리 알림",
            value = "설정 안 함"
        ) { /* TODO: ViewModel과 연결 필요 */ }

        // --- 섹션 6: 한 줄 메모 (ViewModel과 연결) ---
        CreateScheduleSection(
            icon = rememberVectorPainter(image = Icons.Default.Comment),
            title = "한 줄 메모"
        ) {
            ScheduleMemoField(
                memo = memo,
                onMemoChanged = onMemoChanged
            )
        }

        // --- 섹션 7: 반려동물 선택 (ViewModel과 연결) ---
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

// --- 6. 본문 컴포넌트들 (ViewModel과 연결되도록 수정) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTitleField(
    title: String,
    onTitleChanged: (String) -> Unit
) {
    var selectedColor by remember { mutableStateOf(Color(0xFF4285F4)) }

    TextField(
        value = title,
        onValueChange = onTitleChanged,
        placeholder = {
            Text("제목", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.mood),
                    contentDescription = "이모티콘",
                    modifier = Modifier.size(24.dp).clickable { /* TODO: 이모티콘 */ }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                        .clickable { /* TODO: 색상 피커 띄우기 */ }
                        .border(BorderStroke(1.dp, Color.LightGray), CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Gray,
            unfocusedIndicatorColor = Color.LightGray
        ),
        maxLines = 1
    )
}

@Composable
private fun AllDaySwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "하루 종일",
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("하루 종일", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.8f)
        )
    }
}

@Composable
private fun ScheduleTimePicker() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { /* TODO: 시작 시간 피커 */ }
        ) {
            Text("11월 1일 (토)", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text("오전 08:00", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "에서",
            modifier = Modifier.size(20.dp)
        )

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .weight(1f)
                .clickable { /* TODO: 종료 시간 피커 */ }
        ) {
            Text("11월 1일 (토)", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text("오전 09:00", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ScheduleSelectRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Column {
        Divider(color = Color.LightGray.copy(alpha = 0.5f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "선택",
                modifier = Modifier.size(20.dp),
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun ScheduleMemoField(
    memo: String,
    onMemoChanged: (String) -> Unit
) {
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
    allPets: List<Pet>,
    selectedPets: List<Pet>,
    isDropdownVisible: Boolean,
    onDropdownClicked: () -> Unit,
    onDropdownDismissed: () -> Unit,
    onPetSelected: (Pet) -> Unit,
    onPetTagRemoved: (Pet) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // --- 1. 드롭다운 버튼 ---
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
                    Image(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "펫 프로필",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedPets.isEmpty()) "반려동물을 선택해 주세요"
                        else selectedPets.joinToString { it.name },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedPets.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "열기",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- 2. 드롭다운 메뉴 ---
            DropdownMenu(
                expanded = isDropdownVisible,
                onDismissRequest = onDropdownDismissed,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                allPets.forEach { pet ->
                    DropdownMenuItem(
                        text = { Text(pet.name) },
                        onClick = { onPetSelected(pet) }
                    )
                }
            }
        }

        // --- 3. 펫 태그 (FlowRow) ---
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedPets.forEach { pet ->
                PetTagChip(
                    pet = pet,
                    onRemoveClick = { onPetTagRemoved(pet) }
                )
            }
        }
    }
}

@Composable
private fun PetTagChip(
    pet: Pet,
    onRemoveClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Gray // (임시 색상)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(pet.name, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "${pet.name} 삭제",
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onRemoveClick)
            )
        }
    }
}

// --- 7. 미리보기 ---
@Preview(showBackground = true)
@Composable
fun CreateScheduleScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        CreateScheduleScreen(navController = navController)
    }
}
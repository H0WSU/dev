package com.example.howsu.screen.todo

// import androidx.compose.foundation.border // ★ 1. (삭제) border 임포트
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.Pet
import com.example.howsu.ui.theme.HowsuTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTodoScreen(
    navController: NavHostController,
    viewModel: CreateTodoViewModel = viewModel(),
    documentId: String? = null
) {
    // --- (기존 State 구독) ---
    val familyMembers by viewModel.familyMembers.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()
    val taskTitle by viewModel.taskTitle.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isDatePickerVisible by viewModel.isDatePickerVisible.collectAsState()
    val allPets by viewModel.allPets.collectAsState()
    val selectedPets by viewModel.selectedPets.collectAsState()
    val isPetDropdownVisible by viewModel.isPetDropdownVisible.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

    // --- (기존 쉐이크 애니메이션) ---
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

    LaunchedEffect(key1 = documentId) {
        viewModel.initialize(documentId)
    }

    // --- (기존 다이얼로그) ---
    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CreateTodoTopBar(
                // ★★★ (수정) "할 일 수정하기"
                title = if (isEditMode) "할 일 수정하기" else "투두 생성하기",
                onCloseClick = { navController.popBackStack() }
            )
        },
        // (기존) bottomBar 제거
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CreateTodoContent(
                modifier = Modifier.fillMaxSize(),
                isEditMode = isEditMode,
                shakeOffset = shakeOffset.value,
                familyMembers = familyMembers,
                selectedMember = selectedMember,
                taskTitle = taskTitle,
                selectedDate = selectedDate,
                isDatePickerVisible = isDatePickerVisible,
                allPets = allPets,
                selectedPets = selectedPets,
                isPetDropdownVisible = isPetDropdownVisible,
                onMemberSelected = viewModel::onMemberSelected,
                onTaskTitleChanged = viewModel::onTaskTitleChanged,
                onDatePickerClicked = viewModel::onDatePickerClicked,
                onDateSelected = viewModel::onDateSelected,
                onDatePickerDismissed = viewModel::onDatePickerDismissed,
                onPetDropdownClicked = viewModel::onPetDropdownClicked,
                onPetDropdownDismissed = viewModel::onPetDropdownDismissed,
                onPetSelected = viewModel::onPetSelected,
                onPetTagRemoved = viewModel::onPetTagRemoved
            )

            CreateTodoBottomButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                // ★★★ (수정) "수정하기"로 변경
                buttonText = if (isEditMode) "수정하기" else "투두 생성 완료",
                onCreateClick = {
                    if (taskTitle.isBlank()) {
                        triggerShake()
                    } else {
                        viewModel.saveTodo(
                            onComplete = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            )
        }
    }
}

// ★★★ 2. (수정) 상단 바 (X 버튼 테두리 제거) ★★★
@Composable
private fun CreateTodoTopBar(title: String, onCloseClick: () -> Unit) {
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
            // ★ (삭제) .border(...)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// (기존) 하단 버튼 - 변경 없음
@Composable
private fun CreateTodoBottomButton(
    buttonText: String,
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
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
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(buttonText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

// (기존) 섹션 래퍼 - 변경 없음
@Composable
private fun CreateTodoSection(
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


// (기존) 본문 (스크롤 영역) - 변경 없음
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTodoContent(
    modifier: Modifier = Modifier,
    isEditMode: Boolean,
    shakeOffset: Float,
    familyMembers: List<FamilyMember>,
    selectedMember: FamilyMember?,
    taskTitle: String,
    selectedDate: Long,
    isDatePickerVisible: Boolean,
    allPets: List<Pet>,
    selectedPets: List<Pet>,
    isPetDropdownVisible: Boolean,
    onMemberSelected: (FamilyMember) -> Unit,
    onTaskTitleChanged: (String) -> Unit,
    onDatePickerClicked: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    onDatePickerDismissed: () -> Unit,
    onPetDropdownClicked: () -> Unit,
    onPetDropdownDismissed: () -> Unit,
    onPetSelected: (Pet) -> Unit,
    onPetTagRemoved: (Pet) -> Unit
) {
    // (기존) 날짜 선택 다이얼로그 (변경 없음)
    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = onDatePickerDismissed,
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = onDatePickerDismissed) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 104.dp
            ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.Person),
            title = "누가"
        ) {
            AssigneeSelector(
                members = familyMembers,
                selectedMember = selectedMember,
                onMemberSelected = onMemberSelected,
                enabled = true
            )
        }

        CreateTodoSection(
            icon = painterResource(id = R.drawable.date_under),
            title = "언제"
        ) {
            DatePickerField(
                selectedDateMillis = selectedDate,
                onClick = onDatePickerClicked
            )
        }

        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.CheckBox),
            title = "해야 할 일"
        ) {
            TaskTextField(
                text = taskTitle,
                onValueChange = onTaskTitleChanged,
                shakeOffset = shakeOffset
            )
        }

        CreateTodoSection(
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
                onPetTagRemoved = onPetTagRemoved,
                enabled = true // ★★★ (수정) 수정 모드에서도 펫은 추가/삭제 가능
            )
        }
    }
}

// (기존) '누가' 섹션 - 변경 없음
@Composable
private fun AssigneeSelector(
    members: List<FamilyMember>,
    selectedMember: FamilyMember?,
    onMemberSelected: (FamilyMember) -> Unit,
    enabled: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        members.forEach { member ->
            AssigneeItem(
                member = member,
                isSelected = member.userId == selectedMember?.userId,
                onClick = { if (enabled) onMemberSelected(member) },
                enabled = enabled
            )
        }
    }
}

@Composable
private fun AssigneeItem(
    member: FamilyMember,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val alpha = if (enabled) 1f else 0.4f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = (if (isSelected) Color.Black else Color.LightGray).copy(alpha = alpha),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.LightGray.copy(alpha = alpha)
            )
        }
        Text(
            text = member.relationship,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = (if (isSelected) Color.Black else Color.Gray).copy(alpha = alpha)
        )
    }
}

// (기존) '언제' 섹션 - 변경 없음
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    selectedDateMillis: Long,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
    val dateString = formatter.format(Date(selectedDateMillis))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(id = R.drawable.calendar), contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Column {
                Text("date", fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = dateString,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}


// (기존) '해야 할 일' 섹션 - 변경 없음
@Composable
private fun TaskTextField(
    text: String,
    onValueChange: (String) -> Unit,
    shakeOffset: Float
) {
    val maxChars = 20
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = shakeOffset
                },
            shape = RoundedCornerShape(17.dp),
            placeholder = { Text("해야 할 일을 입력해 주세요", fontWeight = FontWeight.Medium, fontSize = 13.sp) },
            maxLines = 3,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${text.length}/$maxChars",
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

// ★★★ 3. (수정) '반려동물 선택' 섹션 (수정 모드 UI 수정) ★★★
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PetSelector(
    allPets: List<Pet>,
    selectedPets: List<Pet>,
    isDropdownVisible: Boolean,
    onDropdownClicked: () -> Unit,
    onDropdownDismissed: () -> Unit,
    onPetSelected: (Pet) -> Unit,
    onPetTagRemoved: (Pet) -> Unit,
    enabled: Boolean // (★ enabled는 이제 '드롭다운 활성화' 여부)
) {
    // (수정) 수정 모드일 때는 alpha를 적용하지 않음
    val alpha = if (enabled) 1f else 0.4f

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(17.dp),
                color = MaterialTheme.colorScheme.surfaceVariant, // (수정) 항상 불투명
                onClick = { if (enabled) onDropdownClicked() } // (수정) 생성 모드일 때만 클릭 가능
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedPets.isEmpty()) {
                        Image(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "펫 프로필",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .graphicsLayer { this.alpha = alpha }
                        )
                    } else {
                        OverlappingPetIcons(
                            petNames = selectedPets.map { it.name },
                            color = Color.Black, // (수정) 항상 불투명
                            modifier = Modifier.height(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ★ (수정) 텍스트가 펫 이름 또는 플레이스홀더를 표시하도록
                    Text(
                        text = if (selectedPets.isEmpty()) {
                            "반려동물을 선택해 주세요"
                        } else {
                            // (수정) 수정 모드일 때도 펫 이름이 보이도록
                            selectedPets.joinToString { it.name }
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        // ★ (수정) 펫이 있으면 활성화된 색상, 비활성화(enabled=false) 시 반투명
                        color = (if (selectedPets.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface)
                            .copy(alpha = if (enabled) 1f else 0.4f)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // ★ (수정) 수정 모드(enabled=false)일 때 화살표 숨김
                    if (enabled) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "열기",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            DropdownMenu(
                expanded = isDropdownVisible && enabled,
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

        // ★ (수정) 수정 모드일 때도 태그가 보이도록 (단, 삭제는 안 됨)
        if (selectedPets.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedPets.forEach { pet ->
                    PetTagChip(
                        pet = pet,
                        onRemoveClick = { onPetTagRemoved(pet) },
                        enabled = enabled // ★ '생성' 모드일 때만 삭제 가능
                    )
                }
            }
        }
    }
}

// (기존) 펫 태그 칩 - 변경 없음
@Composable
private fun PetTagChip(
    pet: Pet,
    onRemoveClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Gray.copy(alpha = if (enabled) 1f else 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(pet.name, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))

            if (enabled) { // ★ 생성 모드일 때만 X 아이콘 표시
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
}

// (기존) 겹치는 펫 아이콘 - 3개 + N개
@Composable
private fun OverlappingPetIcons(
    petNames: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (petNames.isEmpty()) {
        Spacer(modifier = modifier.width(32.dp).height(32.dp))
        return
    }

    val displayNames = petNames.take(3)
    val remaining = (petNames.size - displayNames.size).coerceAtLeast(0)

    val width = (32 + (displayNames.size - 1) * 20 + (if (remaining > 0) 24 else 0)).dp
    val overlap = 20.dp

    Box(
        modifier = modifier
            .width(width)
            .height(32.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        displayNames.reversed().forEachIndexed { index, name ->
            PetIconCircle(
                petName = name,
                color = color.copy(alpha = 1f - (index * 0.2f)),
                modifier = Modifier
                    .padding(start = ((displayNames.size - 1) - index) * overlap)
                    .size(32.dp)
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
                Text(
                    text = "+$remaining",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

// (기존) 펫 아이콘 헬퍼 - private
@Composable
private fun PetIconCircle(petName: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = petName,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

// (기존) 미리보기 - 변경 없음
@Preview(showBackground = true)
@Composable
fun CreateTodoScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        CreateTodoScreen(navController = navController)
    }
}
package com.example.howsu.screen.todo

// --- (필요한 Import) ---
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.Pet
import com.example.howsu.ui.theme.HowsuTheme

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- 1. 메인 화면: Scaffold 뼈대 (ViewModel 주입) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTodoScreen(
    navController: NavHostController,
    viewModel: CreateTodoViewModel = viewModel()
) {
    // ViewModel의 State들을 수집(collect)
    val familyMembers by viewModel.familyMembers.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()
    val taskTitle by viewModel.taskTitle.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState() // Long 타입
    val isDatePickerVisible by viewModel.isDatePickerVisible.collectAsState()
    val allPets by viewModel.allPets.collectAsState()
    val selectedPets by viewModel.selectedPets.collectAsState()
    val isPetDropdownVisible by viewModel.isPetDropdownVisible.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CreateTodoTopBar(
                onCloseClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CreateTodoBottomButton(
                onCreateClick = {
                    viewModel.createTodo(
                        onComplete = {
                            navController.navigate("todo") { // 1. "todo" 스크린으로 이동
                                popUpTo("create_todo") { // 2. 지금 화면("create_todo")은
                                    inclusive = true       //    스택에서 포함해서 제거
                                }
                            }
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        // Content에 모든 State와 이벤트 핸들러 전달
        CreateTodoContent(
            modifier = Modifier.padding(innerPadding),
            familyMembers = familyMembers,
            selectedMember = selectedMember,
            taskTitle = taskTitle,
            selectedDate = selectedDate, // Long 타입 전달
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
    }
}

// --- 2. 상단 바 (변경 없음) ---
@Composable
private fun CreateTodoTopBar(onCloseClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        Text(
            text = "투두 생성하기",
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

// --- 3. 하단 버튼 (변경 없음) ---
@Composable
private fun CreateTodoBottomButton(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
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
            Text("투두 생성 완료", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}


// --- 4. 본문 (스크롤 영역) (파라미터 타입 변경) ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreateTodoContent(
    modifier: Modifier = Modifier,
    familyMembers: List<FamilyMember>,
    selectedMember: FamilyMember?,
    taskTitle: String,
    selectedDate: Long, // (변경) LocalDate -> Long
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
    // 날짜 선택 다이얼로그
    if (isDatePickerVisible) {
        // ViewModel의 Long 값(selectedDate)을 초기값으로 설정
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = onDatePickerDismissed,
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = onDatePickerDismissed) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 섹션 1: 누가 (변경 없음)
        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.Person),
            title = "누가"
        ) {
            AssigneeSelector(
                members = familyMembers,
                selectedMember = selectedMember,
                onMemberSelected = onMemberSelected
            )
        }

        // 섹션 2: 언제 (수정됨)
        CreateTodoSection(
            icon = painterResource(id = R.drawable.date_under),
            title = "언제"
        ) {
            DatePickerField(
                selectedDateMillis = selectedDate, // Long 값 전달
                onClick = onDatePickerClicked
            )
        }

        // 섹션 3: 해야 할 일 (변경 없음)
        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.CheckBox),
            title = "해야 할 일"
        ) {
            TaskTextField(
                text = taskTitle,
                onValueChange = onTaskTitleChanged
            )
        }

        // 섹션 4: 반려동물 선택 (변경 없음)
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
                onPetTagRemoved = onPetTagRemoved
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- 5. 섹션 템플릿 (변경 없음) ---
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

// --- 6. '누가' 섹션 (변경 없음) ---
@Composable
private fun AssigneeSelector(
    members: List<FamilyMember>,
    selectedMember: FamilyMember?,
    onMemberSelected: (FamilyMember) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        members.forEach { member ->
            AssigneeItem(
                member = member,
                isSelected = member.userId == selectedMember?.userId,
                onClick = { onMemberSelected(member) }
            )
        }
    }
}

@Composable
private fun AssigneeItem(
    member: FamilyMember,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color.Black else Color.LightGray,
                    shape = CircleShape
                )
        ) {
            // TODO: Coil 라이브러리 (AsyncImage)
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.LightGray
            )
        }
        Text(
            text = member.relationship,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.Gray
        )
    }
}

// --- 7. '언제' 섹션 (Long -> String 변환 로직 추가) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    selectedDateMillis: Long, // (변경) ViewModel의 Long State
    onClick: () -> Unit
) {
    // (추가) Long 값을 "yyyy년 MM월 dd일" 형식으로 변환
    val formatter = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
    val dateString = formatter.format(Date(selectedDateMillis))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick // 클릭 시 DatePicker 띄우기
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(id = R.drawable.calendar), contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Column {
                Text("date", fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = dateString, // (변경) 포맷된 날짜 문자열 표시
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}


// --- 8. '해야 할 일' 섹션 (변경 없음) ---
@Composable
private fun TaskTextField(
    text: String,
    onValueChange: (String) -> Unit
) {
    val maxChars = 20
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
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

// --- 9. '반려동물 선택' 섹션 (변경 없음) ---
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
                onClick = onDropdownClicked // 클릭 시 드롭다운 열기
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        imageVector = Icons.Default.AccountCircle, // TODO: 펫 이미지
                        contentDescription = "펫 프로필",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "반려동물을 선택해 주세요",
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

// --- (추가) 펫 태그 칩 (변경 없음) ---
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


// --- 10. 미리보기 (변경 없음) ---
@Preview(showBackground = true)
@Composable
fun CreateTodoScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        CreateTodoScreen(navController = navController)
    }
}
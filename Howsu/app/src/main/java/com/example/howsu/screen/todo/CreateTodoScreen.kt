package com.example.howsu.screen.todo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
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

val ContentBlack = Color(0xFF121212)
val YellowBox = Color(0xFFFFDF37)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTodoScreen(
    navController: NavHostController,
    viewModel: CreateTodoViewModel = viewModel(),
    documentId: String? = null
) {
    val familyMembers by viewModel.familyMembers.collectAsState()
    val selectedMembers by viewModel.selectedMembers.collectAsState()
    val taskTitle by viewModel.taskTitle.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isDatePickerVisible by viewModel.isDatePickerVisible.collectAsState()
    val allPets by viewModel.allPets.collectAsState()
    val selectedPets by viewModel.selectedPets.collectAsState()
    val isPetDropdownVisible by viewModel.isPetDropdownVisible.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

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

    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = viewModel::onDatePickerDismissed,
            confirmButton = {
                TextButton(onClick = { viewModel.onDateSelected(datePickerState.selectedDateMillis) }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDatePickerDismissed) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CreateTodoTopBar(
                title = if (isEditMode) "투두 수정하기" else "투두 생성하기",
                onCloseClick = { navController.popBackStack() }
            )
        }
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
                selectedMembers = selectedMembers,
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
                buttonText = if (isEditMode) "수정하기" else "저장하기",
                onCreateClick = {
                    if (taskTitle.isBlank()) {
                        triggerShake()
                    } else {
                        viewModel.saveTodo(onComplete = { navController.popBackStack() })
                    }
                }
            )
        }
    }
}

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
            modifier = Modifier.align(Alignment.Center),
            color = ContentBlack // ★ 색상 적용
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
                modifier = Modifier.size(24.dp),
                tint = ContentBlack // ★ 색상 적용
            )
        }
    }
}

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
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Button(
            onClick = onCreateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowBox, // ★ 색상 적용 (노랑)
                contentColor = ContentBlack // ★ 색상 적용 (검정)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(buttonText, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}

@Composable
private fun CreateTodoSection(
    icon: Painter,
    title: String,
    iconTint: Color = ContentBlack,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ContentBlack
            )
        }
        content()
    }
}

@Composable
private fun CreateTodoContent(
    modifier: Modifier = Modifier,
    isEditMode: Boolean,
    shakeOffset: Float,
    familyMembers: List<FamilyMember>,
    selectedMembers: List<FamilyMember>,
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
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 104.dp),
        // ★ [삭제] 전체 간격 자동 설정 제거 (개별 조절을 위해)
        // verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(34.dp)) // 맨 위 여백 (기존 10 + 24 느낌으로 조정)

        // 1. 누가
        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.Person),
            title = "누가"
        ) {
            AssigneeSelector(
                members = familyMembers,
                selectedMembers = selectedMembers,
                onMemberSelected = onMemberSelected,
                enabled = true
            )
        }

        // ★ 기본 간격
        Spacer(modifier = Modifier.height(24.dp))

        // 2. 언제
        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.DateRange), // 아이콘 수정됨
            title = "언제",
        ) {
            DatePickerField(
                selectedDateMillis = selectedDate,
                onClick = onDatePickerClicked
            )
        }

        // ★ 기본 간격
        Spacer(modifier = Modifier.height(24.dp))

        // 3. 해야 할 일
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

        // 여기가 줄이고 싶은 간격! (예: 12dp로 줄임)
        Spacer(modifier = Modifier.height(12.dp))

        // 4. 반려동물 선택
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
                enabled = true
            )
        }
    }
}
@Composable
private fun AssigneeSelector(
    members: List<FamilyMember>,
    selectedMembers: List<FamilyMember>,
    onMemberSelected: (FamilyMember) -> Unit,
    enabled: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        members.forEach { member ->
            val isSelected = selectedMembers.any { it.userId == member.userId }
            AssigneeItem(
                member = member,
                isSelected = isSelected,
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
                    color = (if (isSelected) Color(0xFFFFDF37) else Color.LightGray).copy(alpha = alpha),
                    shape = CircleShape
                )
        ) {
            if (!member.profileImageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = member.profileImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).graphicsLayer { this.alpha = alpha }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.LightGray.copy(alpha = alpha)
                )
            }
        }
        Text(
            text = member.relationship,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = (if (isSelected) ContentBlack else Color.Gray).copy(alpha = alpha) // ★ 색상 적용
        )
    }
}

@Composable
private fun DatePickerField(selectedDateMillis: Long, onClick: () -> Unit) {
    val formatter = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
    val dateString = formatter.format(Date(selectedDateMillis))

    // ★ 테두리 색상 정의
    val borderColor = Color(0xFF121212)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            // ★ 테두리 추가 (두께 1dp, 색상 0xFF121212)
            .border(1.dp, borderColor, RoundedCornerShape(17.dp)),
        shape = RoundedCornerShape(17.dp),
        color = Color.White, // ★ 배경 흰색
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.date_under),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
                tint = Color.Unspecified
            )
            Column {
                Text("date", fontSize = 10.sp, color = ContentBlack.copy(alpha = 0.7f))
                Text(
                    text = dateString,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = ContentBlack
                )
            }
        }
    }
}

@Composable
private fun TaskTextField(text: String, onValueChange: (String) -> Unit, shakeOffset: Float) {
    val maxChars = 20
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = shakeOffset },
            shape = RoundedCornerShape(17.dp),
            placeholder = {
                Text("해야 할 일을 입력해 주세요", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color.Gray)
            },
            maxLines = 3,
            // ★★★ [수정] 테두리 색상 변경 (0xFF121212)
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF121212),   // 포커스 됐을 때
                unfocusedBorderColor = Color(0xFF121212), // 평소 상태일 때 (회색 말고 검정으로 변경)
                cursorColor = Color(0xFF121212),
                focusedTextColor = Color(0xFF121212),
                unfocusedTextColor = Color(0xFF121212)
            )
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
    enabled: Boolean
) {
    val alpha = if (enabled) 1f else 0.4f
    // ★ 테두리 색상 정의
    val borderColor = Color(0xFF121212)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    // ★ 테두리 추가
                    .border(1.dp, borderColor, RoundedCornerShape(17.dp)),
                shape = RoundedCornerShape(17.dp),
                color = Color.White, // ★ 배경 흰색
                onClick = { if (enabled) onDropdownClicked() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedPets.isEmpty()) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "펫 프로필",
                            modifier = Modifier.size(32.dp).graphicsLayer { this.alpha = alpha },
                            tint = ContentBlack
                        )
                    } else {
                        // 펫 아이콘들
                        OverlappingPetIcons(
                            petNames = selectedPets.map { it.name },
                            petUrls = selectedPets.map { it.profileImageUrl },
                            color = ContentBlack,
                            modifier = Modifier.height(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (selectedPets.isEmpty()) "반려동물을 선택해 주세요" else selectedPets.joinToString { it.name },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ContentBlack.copy(alpha = if (enabled) 1f else 0.4f)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (enabled) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "열기",
                            tint = ContentBlack
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
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!pet.profileImageUrl.isNullOrBlank()) {
                                    coil.compose.AsyncImage(
                                        model = pet.profileImageUrl,
                                        contentDescription = null,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.size(24.dp).clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(pet.name, color = ContentBlack)
                            }
                        },
                        onClick = { onPetSelected(pet) }
                    )
                }
            }
        }

        if (selectedPets.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedPets.forEach { pet ->
                    PetTagChip(
                        pet = pet,
                        onRemoveClick = { onPetTagRemoved(pet) },
                        enabled = enabled
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlappingPetIcons(
    petNames: List<String>,
    petUrls: List<String?>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (petNames.isEmpty()) return

    val displayCount = petNames.take(3).size
    val remaining = (petNames.size - displayCount).coerceAtLeast(0)
    val width = (32 + (displayCount - 1) * 20 + (if (remaining > 0) 24 else 0)).dp
    val overlap = 20.dp

    Box(
        modifier = modifier.width(width).height(32.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        for (index in 0 until displayCount) {
            val reverseIndex = (displayCount - 1) - index
            val name = petNames.getOrNull(reverseIndex) ?: ""
            val url = petUrls.getOrNull(reverseIndex)

            PetIconCircle(
                petName = name,
                imageUrl = url,
                color = color.copy(alpha = 1f - (reverseIndex * 0.2f)),
                modifier = Modifier
                    .padding(start = index * overlap)
                    .size(32.dp)
                    .zIndex(index.toFloat())
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
                Text(text = "+$remaining", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
private fun PetIconCircle(
    petName: String,
    imageUrl: String?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            coil.compose.AsyncImage(
                model = imageUrl,
                contentDescription = petName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = petName,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PetTagChip(pet: Pet, onRemoveClick: () -> Unit, enabled: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(color=0xFFFFDF37).copy(alpha = if (enabled) 1f else 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(pet.name, fontSize = 12.sp, color = Color(0xFF121212))
            Spacer(modifier = Modifier.width(4.dp))
            if (enabled) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "${pet.name} 삭제",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onRemoveClick),
                    tint = Color(color=0xFF121212)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateTodoScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        CreateTodoScreen(navController = navController)
    }
}
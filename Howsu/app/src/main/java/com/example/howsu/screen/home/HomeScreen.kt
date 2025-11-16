package com.example.howsu.screen.home

// 필요한 모든 Compose 및 기타 Import
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.* // remember, mutableStateOf, collectAsState, getValue, setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import kotlin.math.absoluteValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.sp
import java.time.format.TextStyle
import android.R.style

// ----------------------------------------------------
// HomeScreen.kt
// UI 컴포넌트 정의 및 ViewModel 연결
// ----------------------------------------------------

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeScreenViewModel = viewModel(), // ViewModel 인스턴스 주입
    onTodoClick: () -> Unit = {},
    onScheduleClick: () -> Unit = {},
){
    // ViewModel의 상태를 수집하여 State로 변환
    val uiState by viewModel.uiState.collectAsState()

    Scaffold (
        topBar = { MyTopBar() },
        bottomBar = { MyBottomNavigationBar(navController = navController) },
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = onTodoClick,
                onScheduleClick = onScheduleClick
            )
        }
    ){ paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item{ Spacer(Modifier.height(24.dp)) }

            // 2. 반려동물 섹션
            item{
                PetSection(
                    pets = uiState.pets,
                    onPetClick = { pet ->
                        // TODO: 실제 내비게이션 로직 구현
                        // navController.navigate("pet_detail/${pet.id}") 형태로 구현해야 함
                        println("Navigate to Pet Detail for: ${pet.name}")
                    }
                )
                Spacer(Modifier.height(24.dp))
            }

            // 3. 가족 구성원 섹션
            item{
                FamilySection(
                    members = uiState.familyMembers,
                    showInviteDialog = uiState.showInviteDialog,
                    onOpenInviteDialog = { viewModel.onInviteDialogVisibilityChange(true) },
                    onDismissInviteDialog = { viewModel.onInviteDialogVisibilityChange(false) },
                    onInvite = viewModel::inviteFamilyMember
                )
                Spacer(Modifier.height(24.dp))
            }

            // 4. 일정 섹션
            item{
                ScheduleSection(scheduleDays = uiState.scheduleDays)
                Spacer(Modifier.height(24.dp))
            }

            // 5. 리마인더 목록
            item { Text("리마인더", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))}
            item{Spacer(Modifier.height(16.dp))}
            items(uiState.reminders) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onCheckedChange = { isChecked -> viewModel.onReminderCheckedChange(reminder, isChecked) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ----------------------------------------------------
// Preview 함수
// ----------------------------------------------------

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        // Preview에서는 ViewModel을 직접 생성자로 전달하지 않고 기본 함수를 사용하거나
        // Mock ViewModel을 사용하는 것이 일반적입니다. 여기서는 기본 설정으로 둡니다.
        HomeScreen(
            navController = navController,
            onTodoClick = {},
            onScheduleClick = {}
        )
    }
}

// ----------------------------------------------------
// 하위 컴포넌트들
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar() {
    CenterAlignedTopAppBar(
        navigationIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 20.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Profile Icon",
                        modifier = Modifier.fillMaxSize(0.7f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "자몽이 언니",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "이구역의짱",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.Black
                    )
                }
            }
        },
        title = { /* ... */ },
        actions = {
            IconButton(onClick = { /* 알림 클릭 */ }) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "알림",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Gray
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PetSection(
    pets: List<Pet>,
    onPetClick: (Pet) -> Unit // 👈 펫 클릭 이벤트 핸들러 추가
) {
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "반려동물",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.LightGray.copy(alpha = 0.5f)
            ) {
                Text(
                    pets.size.toString(),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        val pagerState = rememberPagerState(pageCount = { pets.size })

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 40.dp),
            modifier = Modifier.fillMaxWidth()
        ) { page ->

            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            val scale = lerp(0.85f, 1f, 1 - pageOffset)
            val alpha = lerp(0.4f, 1f, 1 - pageOffset)
            val zIndex = lerp(-1f, 1f, 1 - pageOffset)

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.alpha = alpha
                    }
                    .zIndex(zIndex)
            ) {
                PetCard(
                    pet = pets[page],
                    onViewDetail = onPetClick // 이벤트 연결
                )
            }
        }
    }
}


@Composable
fun PetCard(
    pet: Pet,
    onViewDetail: (Pet) -> Unit // 클릭 이벤트 핸들러 추가
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = "Pet Icon",
                            modifier = Modifier.fillMaxSize(0.7f),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(pet.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("${pet.age}세 | ${pet.gender}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(
                onClick = { onViewDetail(pet)}, // 상세 정보 보기 이벤트 호출
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("펫 정보 보기", color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


@Composable
fun FamilySection(
    members: List<FamilyMember>,
    showInviteDialog: Boolean,
    onOpenInviteDialog: () -> Unit,
    onDismissInviteDialog: () -> Unit,
    onInvite: (email: String) -> Unit
) {
    Column {
        Text(
            "가족 구성원",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            members.forEach { member ->
                FamilyMemberItem(member = member)
            }
            // Add New 버튼
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onOpenInviteDialog() },
                    color = Color.LightGray.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Add, contentDescription = "add new", tint = Color.DarkGray)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("add new", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showInviteDialog) {
        FamilyInvitationDialog(
            onDismissRequest = onDismissInviteDialog,
            onInvite = onInvite
        )
    }
}

@Composable
fun FamilyMemberItem(member: FamilyMember) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(60.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "${member.name} icon",
                    modifier = Modifier.fillMaxSize(0.7f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            member.name,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (member.isUser) FontWeight.Bold else FontWeight.Normal)
        )
    }
}

@Composable
fun ScheduleSection(scheduleDays: List<ScheduleDay>) {
    Column {
        Text(
            "일정",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            scheduleDays.forEach { day ->
                ScheduleDayItem(day = day)
            }
        }
    }
}

@Composable
fun ScheduleDayItem(day: ScheduleDay) {
    val containerColor = if (day.isSelected) Color.Black else Color.LightGray.copy(alpha = 0.5f)
    val contentColor = if (day.isSelected) Color.White else Color.Black

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day.dayOfWeek, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(containerColor)
                .clickable { /* 날짜 선택 이벤트 */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.Gray.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Checkbox(
                checked = reminder.isDone,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                reminder.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            reminder.date,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

// ----------------------------------------------------
// 가족 초대 다이얼로그
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyInvitationDialog(
    onDismissRequest: () -> Unit,
    onInvite: (email: String) -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        // 👈 크기 조절이 일어나는 위치
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)), // shape를 modifier에 적용

        title = {
            Text(
                "가족 구성원 초대",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        },
        // 다이얼로그의 내용 (이메일 입력 필드와 버튼)
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. 이메일 입력 필드
                TextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        isError = false
                    },
                    placeholder = {
                        Text(
                            text ="초대할 이메일을 입력해 주세요.",
                            style = MaterialTheme.typography.bodySmall)
                    },
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    // 디자인에 맞게 배경색 및 형태 커스텀
                    colors = TextFieldDefaults.colors(
                        // 배경색 지정
                        focusedContainerColor = Color.LightGray.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.LightGray.copy(alpha = 0.5f),
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.5f),

                        // 하단 밑줄(Indicator)을 제거하기 위해 투명하게 설정
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    // 너비를 버튼 공간을 제외하고 채움
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp) // 높이 지정 (버튼과 맞추기)
                )

                Spacer(Modifier.width(8.dp))

                // 2. 초대하기 버튼
                Button(
                    onClick = {
                        if (emailInput.contains("@") && emailInput.contains(".")) {
                            onInvite(emailInput)
                        } else {
                            isError = true
                        }
                    },
                    enabled = emailInput.isNotBlank() && !isError,
                    // 디자인에 맞는 색상 및 형태 적용
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black, // 검은색 배경
                        contentColor = Color.White // 흰색 텍스트
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(56.dp) // 높이 지정
                ) {
                    Text("초대하기", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }

            // 유효성 검사 오류 메시지
            if (isError) {
                Text(
                    text = "유효한 이메일 주소를 입력해 주세요.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        },
        // 취소/확인
        confirmButton = { /* 비워둠 */ },
        dismissButton = { /* 비워둠 */ },
        shape = RoundedCornerShape(16.dp)
    )
}
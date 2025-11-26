package com.example.howsu.screen.home

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.screen.todo.CalendarWeekRow
import com.example.howsu.screen.todo.TodoGroupCard
import com.example.howsu.screen.todo.TodoViewModel
import java.time.LocalDate
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeScreenViewModel = viewModel(),
    todoViewModel: TodoViewModel = viewModel(),
){
    val uiState by viewModel.uiState.collectAsState()

    val todoGroups by todoViewModel.todoGroups.collectAsState(initial = emptyList())
    val selectedDate by todoViewModel.selectedDate.collectAsState()
    val currentWeekStart by todoViewModel.currentWeekStart.collectAsState()

    // ... (permissionLauncher 및 LaunchedEffect 유지) ...

    Scaffold (
        topBar = { MyTopBar() },
        bottomBar = { MyBottomNavigationBar(navController = navController) },
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = { navController.navigate("create_todo") },
                onScheduleClick = { navController.navigate("create_schedule") },
                onFeedCreateClick = { navController.navigate("create_feed") }
            )
        }
    ){ paddingValues ->

        // 좁은 패딩 값 정의
        val NarrowPadding = 26.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)   // 수동 패딩 적용 위함
        ) {
            item{ Spacer(Modifier.height(24.dp)) }

            // 1. 반려동물 섹션
            item{
                Column(modifier = Modifier.padding(horizontal = NarrowPadding)) {
                    PetSection(
                        pets = uiState.pets,
                        onPetClick = { pet ->
                            println("Navigate to Pet Detail for: ${pet.name}")
                        }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // 2. 가족 구성원 섹션
            item{
                Column(modifier = Modifier.padding(horizontal = NarrowPadding)) {
                    FamilySection(
                        members = uiState.familyMembers,
                        showInviteDialog = uiState.showInviteDialog,
                        onOpenInviteDialog = { viewModel.onInviteDialogVisibilityChange(true) },
                        onDismissInviteDialog = { viewModel.onInviteDialogVisibilityChange(false) },
                        onInvite = viewModel::inviteFamilyMember
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // 3. 일정 섹션
            item {
                Column(modifier = Modifier.padding(horizontal = 10.dp)) {   // 별도 패딩 지정
                    Text(
                        "이번 주 일정",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))

                    CalendarWeekRow(
                        selectedDate = selectedDate,
                        currentWeekStart = currentWeekStart,
                        today = LocalDate.now(),
                        onWeekSwipe = todoViewModel::onWeekSwipe,
                        onWeekDaySelected = todoViewModel::onWeekDaySelected
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // 4. 투두 리스트 (NarrowPadding 적용)
            item {
                Text(
                    "남은 할 일",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    // ★ 32.dp 패딩 적용
                    modifier = Modifier.padding(horizontal = NarrowPadding)
                )
                Spacer(Modifier.height(16.dp))
            }

            // [로직]
            val unfinishedGroups = todoGroups.mapNotNull { group ->
                val incompleteTasks = group.tasks.filter { !it.isChecked }

                if (incompleteTasks.isNotEmpty()) {
                    group.copy(tasks = incompleteTasks)
                } else {
                    null
                }
            }

            if (unfinishedGroups.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // ★ 32.dp 패딩 적용
                            .padding(horizontal = NarrowPadding)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "남은 할 일이 없어요! 👏",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(unfinishedGroups, key = { it.documentId }) { group ->
                    Box(modifier = Modifier.padding(horizontal = NarrowPadding)) {
                        TodoGroupCard(
                            group = group,
                            navController = navController,
                            viewModel = todoViewModel
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // 5. 리마인더 목록
            item {
                Spacer(Modifier.height(80.dp).padding(horizontal = NarrowPadding))
            }
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
        )
    }
}

// ----------------------------------------------------
// 하위 컴포넌트들
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar() {    // <수정 필요>
    CenterAlignedTopAppBar(
        navigationIcon = {
            // 기존 UserProfileHeader의 왼쪽 프로필 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                // TopAppBar의 기본 패딩을 고려하여 조절
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

@SuppressLint("RestrictedApi")
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

            // 애니메이션 효과
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
        }
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

/*@Composable
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
}*/
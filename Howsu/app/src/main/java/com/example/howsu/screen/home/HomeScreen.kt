package com.example.howsu.screen.home

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.data.model.FamilyMember
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
) {
    // ----------------------------------------------------
    // ViewModel 상태 구독
    // ----------------------------------------------------
    val uiState by viewModel.uiState.collectAsState()

    // Todo 관련 상태 (TodoViewModel 사용)
    val todoGroups by todoViewModel.todoGroups.collectAsState(initial = emptyList())
    val selectedDate by todoViewModel.selectedDate.collectAsState()
    val currentWeekStart by todoViewModel.currentWeekStart.collectAsState()

    Scaffold(
        // TopBar에 ViewModel에서 가져온 데이터 전달
        topBar = {
            MyTopBar(
                userName = uiState.myName,
                familyName = uiState.familyName,
                profileUrl = uiState.myProfileUrl
            )
        },
        bottomBar = { MyBottomNavigationBar(navController = navController) },
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = { navController.navigate("create_todo") },
                onScheduleClick = { navController.navigate("create_schedule") },
                onFeedCreateClick = { navController.navigate("create_feed") }
            )
        }
    ) { paddingValues ->

        val NarrowPadding = 26.dp

        if (uiState.isLoading) {
            // 로딩 중일 때 로딩 인디케이터 표시
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // 데이터 로드가 완료되었을 때 화면 그리기
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp) // 하단 여백 확보
            ) {
                item { Spacer(Modifier.height(24.dp)) }

                // 1. 반려동물 섹션
                item {
                    Column(modifier = Modifier.padding(horizontal = NarrowPadding)) {
                        PetSection(
                            pets = uiState.pets,
                            onPetClick = { petUi ->
                                // NavController를 사용하여 petId를 인자로 넘겨 상세 화면으로 이동
                                val petId = petUi.originalPet.petId
                                if (petId != null) {
                                    navController.navigate("pet_detail/$petId")
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // 2. 가족 구성원 섹션 (초대 로직 완전히 제거됨)
                item {
                    Column(modifier = Modifier.padding(horizontal = NarrowPadding)) {
                        FamilySection(
                            members = uiState.familyMembers
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // 3. 일정 섹션
                item {
                    Column(modifier = Modifier.padding(horizontal = 10.dp)) {
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

                // 4. 투두 리스트
                item {
                    Text(
                        "남은 할 일",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = NarrowPadding)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // 완료되지 않은 그룹 필터링 로직
                val unfinishedGroups = todoGroups.mapNotNull { group ->
                    val incompleteTasks = group.tasks.filter { !it.isChecked }
                    if (incompleteTasks.isNotEmpty()) group.copy(tasks = incompleteTasks) else null
                }

                if (unfinishedGroups.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = NarrowPadding, vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("남은 할 일이 없어요! 👏", color = Color.Gray)
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

                // 5. 리마인더 목록 (기존 코드에서 제거되었으므로 빈 공간만 남김)
                item {
                    Spacer(Modifier.height(80.dp).padding(horizontal = NarrowPadding))
                }
            }
        }
    }
}

// ----------------------------------------------------
// 하위 컴포넌트들
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(
    userName: String,
    familyName: String,
    profileUrl: String?
) {
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
                    // TODO: profileUrl이 있으면 Coil Image 등으로 교체
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Profile",
                        modifier = Modifier.fillMaxSize(0.7f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = familyName.ifEmpty { "내 정보" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = userName.ifEmpty { "로딩 중..." },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.Black
                    )
                }
            }
        },
        title = { /* Empty */ },
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
    pets: List<PetUiModel>,
    onPetClick: (PetUiModel) -> Unit
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

        if (pets.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("등록된 반려동물이 없어요 🐶", color = Color.Gray)
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { pets.size })

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(end = 40.dp),
                pageSpacing = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                // 애니메이션 효과
                val pageOffset = (
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        ).absoluteValue

                val scale = lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                val alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                ) {
                    PetCard(
                        petModel = pets[page],
                        onViewDetail = onPetClick
                    )
                }
            }
        }
    }
}


@Composable
fun PetCard(
    petModel: PetUiModel,
    onViewDetail: (PetUiModel) -> Unit
) {
    val pet = petModel.originalPet

    Card(
        modifier = Modifier
            .width(300.dp)
            .height(100.dp)
            .clickable { onViewDetail(petModel) }, // 카드 전체 클릭 가능하도록 수정
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)),
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    // TODO: pet.profileImageUrl 로드
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
                    Text(
                        "${petModel.ageText} | ${pet.gender ?: "성별미상"}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            // "펫 정보 보기" 버튼은 카드 클릭으로 대체하거나, 필요 시 다시 구현
            TextButton(
                onClick = { onViewDetail(petModel) },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.8f)),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("정보 보기", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


@Composable
fun FamilySection(
    members: List<FamilyMember>,
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
                    contentDescription = "${member.nickName} icon",
                    modifier = Modifier.fillMaxSize(0.7f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = member.nickName.ifEmpty { "이름없음" },
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            navController = rememberNavController(),
        )
    }
}
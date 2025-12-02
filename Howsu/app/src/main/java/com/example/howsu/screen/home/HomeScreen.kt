package com.example.howsu.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.howsu.common.HomeTopAppBar
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.screen.todo.CalendarWeekRow
import com.example.howsu.screen.todo.ContentBlack
import com.example.howsu.screen.todo.TodoGroupCard
import com.example.howsu.screen.todo.TodoViewModel
import com.example.howsu.screen.todo.YellowBox
import java.time.LocalDate

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeScreenViewModel = viewModel(),
    todoViewModel: TodoViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val todoGroups by todoViewModel.todoGroups.collectAsState(initial = emptyList())
    val selectedDate by todoViewModel.selectedDate.collectAsState()
    val currentWeekStart by todoViewModel.currentWeekStart.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            HomeTopAppBar(
                member = uiState.member,
                family = uiState.family,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 40.dp)
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item { Spacer(Modifier.height(12.dp)) }

                // 1. 반려동물 섹션
                item {
                    Column(modifier = Modifier.padding(horizontal = NarrowPadding)) {
                        PetSection(
                            pets = uiState.pets,
                            onPetClick = { petUi ->
                                val familyId = uiState.member.familyId
                                val petName = petUi.originalPet.name

                                if (familyId.isNotEmpty() && petName.isNotEmpty()) {
                                    navController.navigate("pet_detail/$familyId/$petName")
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // 2. 가족 구성원 섹션
                item {
                    Column(modifier = Modifier.padding(horizontal = NarrowPadding)) {
                        FamilySection(
                            members = uiState.familyMembers,
                            currentUserId = uiState.member.userId
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
            }
        }
    }
}

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
                shape = CircleShape,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("등록된 반려동물이 없어요 🐶", color = Color.Gray)
            }
        } else {
            // PagerState 설정
            val pagerState = rememberPagerState(pageCount = { pets.size })

            // Pager 구현
            HorizontalPager(
                state = pagerState,
                // 양옆의 카드가 살짝 보이거나 간격을 두고 싶다면 padding 조정
                contentPadding = PaddingValues(horizontal = 20.dp),
                pageSpacing = 35.dp, // 카드 간의 물리적 간격
                modifier = Modifier.fillMaxWidth()
            ) { page ->

                // 현재 페이지가 전체 중 어디인지 파악해서 넘겨줌
                PetCard(
                    petModel = pets[page],
                    page = page,
                    totalCount = pets.size,
                    onViewDetail = onPetClick
                )
            }
        }
    }
}

@Composable
fun PetCard(
    petModel: PetUiModel,
    page: Int,          // 현재 카드의 인덱스
    totalCount: Int,    // 전체 카드 개수
    onViewDetail: (PetUiModel) -> Unit
) {
    val pet = petModel.originalPet
    val darkCardColor = YellowBox
    val cardHeight = 120.dp

    // 그림자 설정
    val shadowOffsetX = 20.dp  // 그림자가 옆으로 밀리는 정도
    val shadowScaleStep = 0.05f // 뒤로 갈수록 작아지는 비율

    // 내 뒤에 몇 장 남았는가? (오른쪽 그림자용, 최대 2개)
    val rightShadowCount = (totalCount - 1 - page).coerceIn(0, 2)

    // 내 앞에 몇 장 있었는가? (왼쪽 그림자용, 최대 2개)
    val leftShadowCount = page.coerceIn(0, 2)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        contentAlignment = Alignment.Center // 중앙 정렬
    ) {
        // ---------------------------------------------------------
        // 1. 오른쪽 그림자 (다음 카드가 있을 때)
        // ---------------------------------------------------------
        // 뒤에 있는 것부터 그려야 하므로 역순 혹은 멀리 있는 것 먼저 그림
        // i=2 (가장 먼 그림자) -> i=1 (가까운 그림자)
        for (i in rightShadowCount downTo 1) {
            ShadowCard(
                baseColor = darkCardColor,
                direction = 1, // 1: 오른쪽
                index = i,
                offsetX = shadowOffsetX,
                scaleStep = shadowScaleStep,
                cardHeight = cardHeight
            )
        }

        // ---------------------------------------------------------
        // 2. 왼쪽 그림자 (이전 카드가 있을 때)
        // ---------------------------------------------------------
        for (i in leftShadowCount downTo 1) {
            ShadowCard(
                baseColor = darkCardColor,
                direction = -1, // -1: 왼쪽
                index = i,
                offsetX = shadowOffsetX,
                scaleStep = shadowScaleStep,
                cardHeight = cardHeight
            )
        }

        // ---------------------------------------------------------
        // 3. 메인 카드 (가장 위에 올라옴)
        // ---------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = darkCardColor),
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
                    // 프로필 이미지
                    Surface(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        val imageUrl = pet.profileImageUrl
                        if (imageUrl != null && imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Pet Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = "Default Pet",
                                    modifier = Modifier.fillMaxSize(0.7f),
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(15.dp))

                    // 텍스트 정보
                    Column {
                        Text(
                            text = pet.name,
                            color = ContentBlack,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${petModel.ageText} | ${petModel.displayGender ?: "성별미상"}",
                            color = ContentBlack.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 버튼
                TextButton(
                    onClick = { onViewDetail(petModel) },
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.White,
                        contentColor = ContentBlack
                    ),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text("펫 정보 보기", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// 그림자 역할을 하는 카드 컴포저블
@Composable
private fun ShadowCard(
    baseColor: Color,
    direction: Int, // 1 for Right, -1 for Left
    index: Int,     // 1번째 그림자, 2번째 그림자...
    offsetX: Dp,
    scaleStep: Float,
    cardHeight: Dp
) {
    // 투명도: 뒤로 갈수록 흐려지게 (선택사항, 원치 않으면 제거 가능)
    //val alpha = 1f - (index * 1f)

    // 깊이(index)에 따라 투명도를 명확하게 다르게 설정
    // 숫자가 클수록 더 진하게(불투명하게) 보임
    val distinctAlpha = when(index) {
        1 -> 0.5f  // 메인 카드 바로 뒤: 비교적 진함
        2 -> 0.2f  // 가장 뒤: 연함
        else -> 0.2f
    }

    // 크기: 뒤로 갈수록 작아지게
    val scale = 0.95f - (index * scaleStep)

    // 위치 이동: 방향 * 순서 * 간격
    val xOffset = offsetX * index * direction

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .graphicsLayer {
                translationX = xOffset.toPx() // X축 이동
                scaleX = scale                // 가로 크기 축소
                scaleY = scale                // 세로 크기 축소
            }
            .clip(RoundedCornerShape(16.dp))
            .background(baseColor.copy(alpha = 0.5f)) // 반투명 배경색
            .background(baseColor.copy(alpha = distinctAlpha)) // 그림자 색 수정
    )
}

// 그림자 효과를 위한 별도의 Box 컴포저블
@Composable
private fun ShadowBox(
    modifier: Modifier = Modifier,
    height: Dp,
    color: Color
) {
    Box(
        modifier = modifier
            .fillMaxWidth() // PetCard와 동일한 너비를 가질 수 있도록 설정
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
    )
}


// 2. <가족 구성원>
@Composable
fun FamilySection(
    members: List<FamilyMember>,
    currentUserId: String? = null
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
                val isCurrentUser = member.userId == currentUserId
                FamilyMemberItem(member = member, isCurrentUser = isCurrentUser)
            }
        }
    }
}

@Composable
fun FamilyMemberItem(
    member: FamilyMember,
    isCurrentUser: Boolean = false
) {

    val borderStroke = if(isCurrentUser){
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .size(60.dp)
                .let{ modifier ->
                    if(borderStroke != null){
                        modifier.border(border = borderStroke, shape = CircleShape)
                    } else{
                        modifier
                    }
                },
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
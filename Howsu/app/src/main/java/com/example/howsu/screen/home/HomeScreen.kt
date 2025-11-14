package com.example.howsu.screen.home

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
import androidx.compose.foundation.lazy.LazyRow // 👈 LazyRow 임포트
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton

// 임시 데이터 모델
data class Reminder(
    val text : String,
    val date: String,
    val isDone : Boolean
)
data class Pet(
    val name : String,
    val age : Int,
    val gender : String,
    val imageUrl: String = ""
)
data class FamilyMember(
    val name: String,
    val isUser: Boolean = false
)
data class ScheduleDay(
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val isSelected: Boolean
)


@Composable
fun HomeScreen(
    navController: NavHostController,
    onTodoClick: () -> Unit = {},
    onScheduleClick: () -> Unit = {},
){
    Scaffold (
        topBar = { MyTopBar() },
        bottomBar = { MyBottomNavigationBar(navController = navController) },
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = onTodoClick,       // HomeScreen의 파라미터를 전달
                onScheduleClick = onScheduleClick // HomeScreen의 파라미터를 전달
            )
        }
    ){ paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item{
                Spacer(Modifier.height(24.dp))
            }

            // 2. 반려동물 섹션
            item{
                PetSection(
                    pets = listOf(
                        Pet("자몽",7,"여아"),
                        Pet("두부", 2,"남아"),
                        Pet("코코", 5,"남아"),
                        Pet("복실", 1,"여아")
                    )
                )
                Spacer(Modifier.height(24.dp))
            }

            // 3. 가족 구성원 섹션
            item{
                FamilySection(
                    members = listOf(
                        FamilyMember("언니", isUser = true),
                        FamilyMember("엄마", isUser = false),
                    )
                )
                Spacer(Modifier.height(24.dp))
            }

            // 4. 일정 섹션
            item{
                ScheduleSection(
                    scheduleDays = listOf(
                        ScheduleDay("화", 13, false),
                        ScheduleDay("수", 14, false),
                        ScheduleDay("목", 15, true), // 오늘 날짜처럼 보이게 선택됨
                        ScheduleDay("금", 16, false),
                        ScheduleDay("토", 17, false),
                        ScheduleDay("일", 18, false),
                    )
                )
                Spacer(Modifier.height(24.dp))
            }

            // 5. 리마인더 목록
            item { Text("리마인더", style = MaterialTheme.typography.titleLarge) }
            items(
                listOf(
                    Reminder("츄르 사오기", "2025. 10. 28", false),
                    Reminder("병원 방문하기", "2025. 10. 28", false),
                    Reminder("목욕시키기", "2025. 10. 28", true)
                )
            ) { reminder ->
                ReminderItem(reminder)
            }
            item { Spacer(Modifier.height(80.dp)) } // FAB와의 간격 확보
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
        HomeScreen(
            navController = navController,
            onTodoClick = {},      // 빈 람다 전달
            onScheduleClick = {}   // 빈 람다 전달
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
            // 기존 UserProfileHeader의 왼쪽 프로필 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                // TopAppBar의 기본 패딩을 고려하여 조절
                modifier = Modifier.padding(start = 20.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp), // TopBar에 맞게 크기 조정
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Profile Icon",
                        modifier = Modifier.fillMaxSize(0.7f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp)) // 간격 조정
                Column {
                    Text(
                        text = "자몽이 언니",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "이구역의짱",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        },
        title = { /* 가운데 타이틀은 비워둠 */ },
        actions = {
            // 기존 UserProfileHeader의 오른쪽 알림 버튼
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

// ----------------------------------------------------
// 펫 카드 스크롤 가능
// ----------------------------------------------------
@Composable
fun PetSection(pets: List<Pet>) {
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

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(pets) { pet ->
                PetCard(pet = pet)
            }
        }
    }
}

@Composable
fun PetCard(pet: Pet) {
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
                // 👈 반려동물 이미지 대신 동물 아이콘 사용
                Surface(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color.White.copy(alpha = 0.15f) // 아이콘 배경색
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
                onClick = { /* 펫 정보 보기 클릭 */ },
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
fun FamilySection(members: List<FamilyMember>) {
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
                        .clickable { /* 새 멤버 추가 클릭 */ },
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
}

@Composable
fun FamilyMemberItem(member: FamilyMember) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(60.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
        ) {
            // 👈 가족 구성원 이미지 대신 사람 아이콘 사용
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
fun ReminderItem(reminder: Reminder) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Checkbox(
                checked = reminder.isDone,
                onCheckedChange = { /* 체크박스 상태 변경 */ },
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
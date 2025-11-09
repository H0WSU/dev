package com.example.howsu.screen.schedule // (1. 본인 패키지 이름 확인)

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.ui.theme.HowsuTheme

// --- 1. 메인 화면: Scaffold 뼈대 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleScreen(
    navController: NavHostController
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CreateScheduleTopBar(
                onCloseClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CreateScheduleBottomButton(
                onCreateClick = { /* TODO: 생성 완료 로직 */ }
            )
        }
    ) { innerPadding ->
        CreateScheduleContent(
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// --- 2. 상단 바 (제목 + X 버튼) ---
@Composable
private fun CreateScheduleTopBar(onCloseClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
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

// --- 3. 하단 버튼 (저장하기) ---
@Composable
private fun CreateScheduleBottomButton(onCreateClick: () -> Unit) {
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
            Text("저장하기", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

// --- 4. 섹션 래퍼 (ImageVector 버전으로 수정) ---
@Composable
private fun CreateScheduleSection(
    icon: ImageVector, // ★ 1. Painter -> ImageVector
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon, // ★ 2. painter = icon -> imageVector = icon
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


// --- 5. 본문 (스크롤 영역) ---
@Composable
private fun CreateScheduleContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(1.dp))

        // --- 섹션 1: 제목 ---
        ScheduleTitleField() // (수정됨: 색상 선택 동그라미)

        // --- 섹션 2: 하루 종일 ---
        AllDaySwitch()

        // --- 섹션 3: 날짜/시간 선택 ---
        ScheduleTimePicker() // (수정됨: 중앙 정렬)

        // --- 섹션 4: 일정 반복 ---
        ScheduleSelectRow(
            icon = Icons.Default.Refresh,
            title = "일정 반복",
            value = "반복 없음"
        ) { /* TODO: 일정 반복 클릭 */ }

        // --- 섹션 5: 일정 미리 알림 ---
        ScheduleSelectRow(
            icon = Icons.Default.Notifications,
            title = "일정 미리 알림",
            value = "설정 안 함"
        ) { /* TODO: 알림 클릭 */ }

        // --- 섹션 6: 한 줄 메모 (수정됨: Icons.Default.Comment) ---
        CreateScheduleSection(
            icon = Icons.Default.Comment,
            title = "한 줄 메모"
        ) {
            ScheduleMemoField()
        }

        // --- 섹션 7: 반려동물 선택 (수정됨: Icons.Default.Pets) ---
        CreateScheduleSection(
            icon = Icons.Default.Pets,
            title = "반려동물 선택"
        ) {
            PetSelector()
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- 6. 본문 컴포넌트들 ---

@OptIn(ExperimentalMaterial3Api::class) // (TextField에 필요)
@Composable
private fun ScheduleTitleField() {
    var text by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF4285F4)) } // (예: 파란색)

    // ★ 1. OutlinedTextField -> TextField로 변경
    TextField(
        value = text,
        onValueChange = { text = it },

        // ★ 2. placeholder 폰트/굵기 적용 (디자인과 동일하게)
        placeholder = {
            Text(
                "제목",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 1. 스마일 아이콘
                Icon(
                    painter = painterResource(id = R.drawable.mood), // (본인 아이콘 리소스)
                    contentDescription = "이모티콘",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { /* TODO: 이모티콘 */ }
                )
                Spacer(modifier = Modifier.width(10.dp))

                // 2. 색상 선택 동그라미
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(selectedColor) // (현재 선택된 색상)
                        .clickable { /* TODO: 색상 피커 띄우기 */ }
                        .border(BorderStroke(1.dp, Color.LightGray), CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp)) // (아이콘과 가장자리 여백)
            }
        },
        // ★ 3. 배경을 투명하게, 밑줄 색상만 지정 ★
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            // (디자인에 밑줄이 있으므로 밑줄 색상 지정)
            focusedIndicatorColor = Color.Gray,
            unfocusedIndicatorColor = Color.LightGray
        ),
        maxLines = 1
    )
}

@Composable
private fun AllDaySwitch() {
    var isChecked by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isChecked = !isChecked }
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
            onCheckedChange = { isChecked = it },
            modifier = Modifier.scale(0.8f)
        )
    }
}

@Composable
private fun ScheduleTimePicker() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // horizontalArrangement = Arrangement.SpaceBetween, // <-- 1. 이 줄을 삭제합니다.
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 시작 시간
        Column(
            modifier = Modifier
                .weight(1f) // <-- 2. 시작 시간에 weight(1f) 추가
                .clickable { /* TODO: 시작 시간 피커 */ }
        ) {
            Text("11월 1일 (토)", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text("오전 08:00", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        // 화살표
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "에서",
            modifier = Modifier
                .size(20.dp)
                // 3. 화살표 좌우에 원하는 고정 패딩을 줍니다.
                // .padding(horizontal = 5.dp)
        )

        // 종료 시간
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .weight(1f) // <-- 4. 종료 시간에 weight(1f) 추가
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
        Divider(color = Color.LightGray.copy(alpha = 0.5f)) // (구분선)
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
private fun ScheduleMemoField() {
    var text by remember { mutableStateOf("") }
    val maxChars = 20

    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= maxChars) text = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            placeholder = { Text("메모 입력하기", fontWeight = FontWeight.Medium, fontSize = 13.sp) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetSelector() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            // ★ 1. .fillMaxWidth()를 삭제해서 가로 폭이 줄어들게 함 ★
            modifier = Modifier,
            shape = RoundedCornerShape(15.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            onClick = { /* TODO: 드롭다운 메뉴 띄우기 */ }
        ) {
            Row(
                modifier = Modifier
                    // (상하 여백 8.dp로 줄임)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector = Icons.Default.AccountCircle, // (TODO: 펫 이미지)
                    contentDescription = "펫 프로필",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("자몽", fontSize = 14.sp, fontWeight = FontWeight.Medium)

                // ★ 2. 텍스트와 아이콘 사이에 고정 간격(8.dp) 추가 ★
                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 태그 ---
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // (자몽 칩, 레몬 칩 Surface...)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Gray
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("자몽", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "자몽 삭제",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { /* TODO: 자몽 삭제 로직 */ }
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Gray
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("레몬", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "레몬 삭제",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { /* TODO: 레몬 삭제 로직 */ }
                    )
                }
            }
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
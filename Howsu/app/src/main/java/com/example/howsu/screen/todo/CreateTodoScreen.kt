package com.example.howsu.screen.todo

// --- (필요한 Import) ---
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R
import com.example.howsu.ui.theme.HowsuTheme

// --- 1. 메인 화면: Scaffold 뼈대 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTodoScreen(
    navController: NavHostController
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CreateTodoTopBar(
                onCloseClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CreateTodoBottomButton(
                onCreateClick = { /* TODO: 생성 완료 로직 */ }
            )
        }
    ) { innerPadding ->
        CreateTodoContent(
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// --- 2. 상단 바 (제목 + X 버튼) ---
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
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterEnd)
                .border(
                    BorderStroke(0.1.dp, Color.LightGray), // 5. 1dp 두께의 연회색 테두리
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

// --- 3. 하단 버튼 (생성 완료) ---
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

// --- 4. 본문 (스크롤 영역) ---
@Composable
private fun CreateTodoContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 섹션 1: 누가
        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.Person),
            title = "누가"
        ) {
            AssigneeSelector() // (수정됨 - 선택 UI 반영)
        }

        // 섹션 2: 언제
        CreateTodoSection(
            icon = painterResource(id = R.drawable.date_under),
            title = "언제"
        ) {
            DatePickerField() // (수정됨 - 'date' 텍스트 추가)
        }

        // 섹션 3: 해야 할 일
        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.CheckBox),
            title = "해야 할 일"
        ) {
            TaskTextField() // (수정됨 - 0/20 카운터 밖으로)
        }

        // 섹션 4: 반려동물 선택
        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.Pets),
            title = "반려동물 선택"
        ) {
            PetSelector() // (수정됨 - UI 이미지와 동일하게)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- 5. 섹션 템플릿 (Painter 타입) ---
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

@Composable
private fun AssigneeSelector() {
    // '언니'가 선택되었다고 가정
    var selectedAssignee by remember { mutableStateOf("언니") }

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        AssigneeItem(
            name = "언니",
            isSelected = selectedAssignee == "언니",
            onClick = { selectedAssignee = "언니" }
        )
        AssigneeItem(
            name = "엄마",
            isSelected = selectedAssignee == "엄마",
            onClick = { selectedAssignee = "엄마" }
        )
    }
}

@Composable
private fun AssigneeItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border( // (선택 상태에 따라 테두리 변경)
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color.Black else Color.LightGray,
                    shape = CircleShape
                )
        ) // TODO: 안에 프로필 이미지
        Text(
            text = name,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),

            // 1. 선택되면 Bold, 아니면 Normal
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,

            // 2. 선택되면 진한 색, 아니면 연한 색
            color = if (isSelected) Color.Black else Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surfaceVariant, // (회색 배경)
        onClick = { /* TODO: 날짜 피커 띄우기 */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(id = R.drawable.calendar), contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Column {
                Text("date", fontSize = 10.sp, color = Color.Gray) // (이미지처럼 'date' 추가)
                Text("2025년 11월 3일", fontWeight = FontWeight.Medium, fontSize = 13.sp) // TODO: 실제 날짜 state
            }
        }
    }
}

@Composable
private fun TaskTextField() {
    var text by remember { mutableStateOf("") }
    val maxChars = 20

    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= maxChars) text = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            placeholder = { Text("해야 할 일을 입력해 주세요", fontWeight = FontWeight.Medium, fontSize = 13.sp) },
            maxLines = 3,
            // (trailingIcon 삭제)
        )
        Spacer(modifier = Modifier.height(2.dp))
        // (카운터를 밖으로 빼고 오른쪽 정렬)
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
    // (이 컴포넌트는 이미지와 똑같이 보이도록 Surface로 변경)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            color = MaterialTheme.colorScheme.surfaceVariant, // (회색 배경)
            onClick = { /* TODO: 드롭다운 메뉴 띄우기 */ }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. 펫 프로필 이미지 (임시)
                Image(
                    imageVector = Icons.Default.AccountCircle, // TODO: 펫 이미지
                    contentDescription = "펫 프로필",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // 2. 펫 이름
                Text("자몽", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                // 3. 드롭다운 화살표
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 2. 태그 (FlowRow 사용 추천) ---
// (간단히 Row로 임시 구현)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            // --- 1. "자몽" 칩 ---
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Gray // (나중에 MaterialTheme.colorScheme.secondaryContainer 등으로)
            ) {
                // Surface 안에 Row를 넣어 텍스트와 아이콘을 배치
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("자몽", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp)) // (텍스트와 X버튼 사이)
                    Icon(
                        imageVector = Icons.Default.Close, // (X 버튼 아이콘)
                        contentDescription = "자몽 삭제",
                        modifier = Modifier
                            .size(16.dp) // (아이콘 크기 작게)
                            .clickable { /* TODO: 자몽 삭제 로직 */ }
                    )
                }
            }

            // --- 2. "레몬" 칩 ---
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
fun CreateTodoScreenPreview() {
    HowsuTheme {
        val navController = rememberNavController()
        CreateTodoScreen(navController = navController)
    }
}
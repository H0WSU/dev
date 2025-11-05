package com.example.howsu.screen.todo

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
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
    navController: NavHostController // (화면 이동을 위해)
) {
    Scaffold(
        // (Scaffold 배경색을 지정해야 하단 버튼 영역이 잘 보임)
        containerColor = MaterialTheme.colorScheme.background,

        // --- 상단 바 ---
        topBar = {
            CreateTodoTopBar(
                onCloseClick = {
                    navController.popBackStack() // (X 버튼 클릭 시 뒤로 가기)
                }
            )
        },

        // --- 하단 버튼 ---
        bottomBar = {
            CreateTodoBottomButton(
                onCreateClick = {
                    // TODO: '투두 생성 완료' 로직 실행
                }
            )
        }
    ) { innerPadding -> // (상/하단 바를 피하기 위한 여백)

        // --- 본문 ---
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
        // "투두 생성하기": SemiBold, 16
        Text(
            text = "투두 생성하기",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        // X 버튼: 원 사이즈 39*39, 아이콘 24*24
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterEnd)
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
    // (버튼이 바닥에 딱 붙지 않게 여백을 줌)
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
            Text("투두 생성 완료", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// --- 4. 본문 (스크롤 영역) ---
@Composable
private fun CreateTodoContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // (스크롤 가능하게)
            .padding(horizontal = 24.dp), // (폼 전체 좌우 여백)
        verticalArrangement = Arrangement.spacedBy(24.dp) // (섹션 간 세로 간격)
    ) {
        Spacer(modifier = Modifier.height(16.dp)) // (상단 여백)

        // 섹션 1: 누가
        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.Person),
            title = "누가"
        ) {
            // (이미지 기반 임시 구현)
            AssigneeSelector()
        }

        // 섹션 2: 언제
        CreateTodoSection(
            icon = painterResource(id = R.drawable.date_under),
            title = "언제"
        ) {
            // (이미지 기반 임시 구현)
            DatePickerField()
        }

        // 섹션 3: 해야 할 일
        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.CheckCircle),
            title = "해야 할 일"
        ) {
            // (이미지 기반 임시 구현)
            TaskTextField()
        }

        // 섹션 4: 반려동물 선택
        CreateTodoSection(
            icon = rememberVectorPainter(image = Icons.Default.FavoriteBorder),
            title = "반려동물 선택"
        ) {
            // (이미지 기반 임시 구현)
            PetSelector()
        }

        Spacer(modifier = Modifier.height(32.dp)) // (하단 여백)
    }
}

// --- 5. 반복되는 섹션 (아이콘 + 제목 + 내용) ---
@Composable
private fun CreateTodoSection(
    icon: Painter,
    title: String,
    content: @Composable () -> Unit // (내용물을 밖에서 끼워넣음)
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp) // (제목과 내용물 사이 간격)
    ) {
        // --- 헤더 (아이콘 + 제목) ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 글자 앞 아이콘: 사이즈 22*22
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 섹션 제목: Bold, 14
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // --- 내용물 (텍스트필드, 날짜 선택기 등) ---
        content()
    }
}

// --- 6. 폼 내용물 (임시 플레이스홀더) ---
// (이 부분들은 나중에 실제 기능으로 채워야 합니다)

@Composable
private fun AssigneeSelector() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
            ) // TODO: 클릭 시 선택되도록
            Text("언니", fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.LightGray, CircleShape)
            ) // TODO: 클릭 시 선택되도록
            Text("엄마", fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant, // (회색 배경)
        onClick = { /* TODO: 날짜 피커 띄우기 */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("2025년 11월 3일") // TODO: 실제 날짜 state와 연결
        }
    }
}

@Composable
private fun TaskTextField() {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { if (it.length <= 20) text = it }, // (20자 제한)
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("해야 할 일을 입력해 주세요") },
        maxLines = 3,
        // (글자 수 카운터)
        trailingIcon = { Text("0/20") } // TODO: text.length
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetSelector() {
    // (임시로 TextField 모양만 흉내)
    var expanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = "자몽", // TODO: 선택된 펫 state
        onValueChange = { },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true, // (클릭해서 드롭다운 띄우기)
        leadingIcon = {
            // (임시 프로필 사진 아이콘)
            Image(
                imageVector = Icons.Default.AccountCircle, // TODO: 펫 이미지
                contentDescription = "펫 프로필",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        },
        trailingIcon = {
            // (드롭다운 화살표)
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
    )
    // TODO: 태그("자몽x", "레몬x") 부분은 별도 Composable로 구현 필요
}


// --- 7. 미리보기 ---
@Preview(showBackground = true)
@Composable
fun CreateTodoScreenPreview() {
    HowsuTheme { // (본인 앱 테마)
        val navController = rememberNavController()
        CreateTodoScreen(navController = navController)
    }
}
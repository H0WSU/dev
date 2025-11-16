package com.example.howsu.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.R

// ----------------------------------------------------
// 펫 상세 정보 스크린
// ----------------------------------------------------

private val DummyPetImage = R.drawable.jamong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    navController: NavHostController,
    pet: Pet // 표시할 펫 정보
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("반려동물 정보 보기", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = { /* 편집 버튼 클릭 */ }) {
                        Icon(Icons.Filled.Edit, contentDescription = "편집")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            // 👈 최상위 패딩 제거
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. 프로필 이미지 섹션 (패딩 불필요)
            item {
                Spacer(Modifier.height(32.dp))
                PetProfileImageSection(pet = pet)
                Spacer(Modifier.height(32.dp))
            }

            // 2. 이름 필드 (개별 패딩 적용)
            item {
                DetailField(
                    label = "이름",
                    value = pet.name,
                    modifier = Modifier.padding(horizontal = 24.dp) // 👈 패딩 적용
                )
                Spacer(Modifier.height(32.dp))
            }

            // 3. 성별 필드 (개별 패딩 적용)
            item {
                GenderSelectionSection(
                    selectedGender = pet.gender,
                    modifier = Modifier.padding(horizontal = 24.dp) // 👈 패딩 적용
                )
                Spacer(Modifier.height(32.dp))
            }

            // 4. 체중 필드 (개별 패딩 적용)
            item {
                WeightField(
                    value = 3.8f, // 임시 체중 값
                    modifier = Modifier.padding(horizontal = 24.dp) // 👈 패딩 적용
                )
                Spacer(Modifier.height(32.dp))
            }

            // 5. 생년월일/나이 필드 (개별 패딩 적용)
            item {
                BirthDateAgeSection(
                    birthDate = "2018년 7월 2일",
                    age = pet.age,
                    modifier = Modifier.padding(horizontal = 24.dp) // 👈 패딩 적용
                )
                Spacer(Modifier.height(50.dp))
            }
        }
    }
}

// ----------------------------------------------------
// 하위 컴포넌트
// ----------------------------------------------------

@Composable
fun PetProfileImageSection(pet: Pet) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(Color.LightGray.copy(alpha = 0.3f))
            .border(2.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // 실제 이미지 로딩 (현재는 임시)
        if (pet.imageUrl.isNotEmpty()) {
            // Coil/Glide 등을 사용하여 URL 이미지 로딩
        } else {
            Image(
                painter = painterResource(id = DummyPetImage),
                contentDescription = "Pet Profile Image",
                modifier = Modifier.fillMaxSize()
            )
        }

        // 카메라/갤러리 아이콘 (우측 하단 작은 버튼)
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .clickable { /* 이미지 변경 클릭 */ },
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_gallery),
                    contentDescription = "Gallery Icon",
                    modifier = Modifier.size(20.dp),
                    tint = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun DetailField(
    label: String,
    value: String,
    modifier: Modifier = Modifier // 👈 Modifier 파라미터 추가
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
        )
        Divider(
            color = Color.LightGray.copy(alpha = 0.7f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun GenderSelectionSection(
    selectedGender: String,
    modifier: Modifier = Modifier // 👈 Modifier 파라미터 추가
) {
    Column(modifier = modifier.fillMaxWidth()) { // 👈 받은 Modifier 사용
        Text(
            "성별",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GenderButton(
                label = "여아",
                isSelected = selectedGender == "여아",
                modifier = Modifier.weight(1f) // 👈 균등 분할
            )
            GenderButton(
                label = "남아",
                isSelected = selectedGender == "남아",
                modifier = Modifier.weight(1f) // 👈 균등 분할
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            )
            Spacer(Modifier.width(8.dp))
            Text("중성화했어요", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun GenderButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) Color.Black else Color.White
    val contentColor = if (isSelected) Color.White else Color.Black
    val borderColor = if (isSelected) Color.Black else Color.LightGray.copy(alpha = 0.5f)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier // 👈 외부에서 받은 Modifier 사용
            .height(50.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { /* 성별 선택 이벤트 */ },
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = contentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun WeightField(value: Float, modifier: Modifier = Modifier) { // 👈 Modifier 파라미터 추가
    Column(modifier = modifier.fillMaxWidth()) { // 👈 받은 Modifier 사용
        Text(
            "체중",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                modifier = Modifier.weight(1f)
            )
            Text(
                "kg",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray
            )
        }
        Divider(
            color = Color.LightGray.copy(alpha = 0.7f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun BirthDateAgeSection(birthDate: String, age: Int, modifier: Modifier = Modifier) { // 👈 Modifier 파라미터 추가
    Column(modifier = modifier.fillMaxWidth()) { // 👈 받은 Modifier 사용
        Text(
            "생년월일/나이",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
        )
        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth().height(70.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Birthday Icon",
                        tint = Color.Black
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(birthDate, style = MaterialTheme.typography.bodyLarge)
                }
                Text(
                    "${age}세",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// ----------------------------------------------------
// Preview
// ----------------------------------------------------

@Preview(showBackground = true)
@Composable
fun PetDetailScreenPreview() {
    MaterialTheme {
        PetDetailScreen(
            navController = rememberNavController(),
            pet = Pet(name = "자몽", age = 7, gender = "여아")
        )
    }
}
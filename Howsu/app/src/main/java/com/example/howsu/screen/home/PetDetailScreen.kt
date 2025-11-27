package com.example.howsu.screen.pet // 패키지명 확인 필요

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.howsu.data.model.Pet
import com.example.howsu.screen.home.PetDetailViewModel

// ----------------------------------------------------
// 펫 상세 정보 스크린
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    navController: NavHostController,
    viewModel: PetDetailViewModel = viewModel() // ViewModel 주입
) {
    val uiState by viewModel.uiState.collectAsState()
    val pet = uiState.pet

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
                    IconButton(
                        onClick = {
                            // 💡 수정된 부분: pet?.petId는 ViewModel에서 문서 ID로 채워져 있습니다.
                            pet?.petId?.let {
                                // "edit_pet/{petId}" 경로가 NavGraph에 정의되어 있어야 합니다.
                                navController.navigate("edit_pet/$it")
                            }
                        },
                        enabled = pet != null // 펫 정보가 있을 때만 편집 가능
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "편집")
                    }
                }
            )
        }
    ) { paddingValues ->

        // 로딩 중이거나 데이터가 없을 때 처리
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (pet == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("정보를 불러올 수 없습니다.")
            }
        } else {
            // 데이터가 있을 때 화면 그리기
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 프로필 이미지 섹션
                item {
                    Spacer(Modifier.height(32.dp))
                    PetProfileImageSection(pet = pet)
                    Spacer(Modifier.height(32.dp))
                }

                // 2. 이름 필드
                item {
                    DetailField(
                        label = "이름",
                        value = pet.name,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                // 3. 성별 필드
                item {
                    // PetDetailViewModel에서 이미 "남아"/"여아"로 변환된 값 사용
                    GenderSelectionSection(
                        selectedGender = pet.gender ?: "",
                        isNeutered = pet.isNeutered ?: false,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                // 4. 체중 필드
                item {
                    WeightField(
                        // String으로 저장된 weight를 Float로 변환 (예외처리 포함)
                        value = pet.weight?.toFloatOrNull() ?: 0.0f,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                // 5. 생년월일/나이 필드
                item {
                    val displayDate = if (!pet.birthdayExact.isNullOrEmpty()) {
                        pet.birthdayExact
                    } else {
                        "${pet.birthdayYearApprox}년 ${pet.birthdayMonthApprox}월 (추정)"
                    }

                    BirthDateAgeSection(
                        birthDate = displayDate ?: "-",
                        ageText = uiState.ageText,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(50.dp))
                }
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
        if (!pet.profileImageUrl.isNullOrEmpty()) {
            // TODO: Coil - AsyncImage(model = pet.profileImageUrl, ...) 사용 권장
            // 임시로 아이콘 표시
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.Gray
            )
        } else {
            // 기본 이미지
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.Gray
            )
        }

        // 갤러리 아이콘
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
                // R.drawable.ic_menu_gallery 대신 기본 아이콘 사용 가능
                Icon(
                    imageVector = Icons.Default.Edit, // 혹은 적절한 갤러리 아이콘
                    contentDescription = "Edit Profile",
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
    modifier: Modifier = Modifier
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
    isNeutered: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
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
            // 보여주기 전용이므로 클릭 이벤트 비활성화 혹은 뷰 모드 처리
            GenderButton(
                label = "여아",
                isSelected = selectedGender == "여아",
                modifier = Modifier.weight(1f)
            )
            GenderButton(
                label = "남아",
                isSelected = selectedGender == "남아",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if(isNeutered) Color.Black else Color.LightGray)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "중성화했어요",
                style = MaterialTheme.typography.bodySmall,
                color = if(isNeutered) Color.Gray else Color.LightGray
            )
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
        modifier = modifier
            .height(50.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
        // .clickable {} // 상세 보기 모드에서는 클릭 제거
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = contentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun WeightField(value: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
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
fun BirthDateAgeSection(birthDate: String, ageText: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
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
                    ageText, // "7세"
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
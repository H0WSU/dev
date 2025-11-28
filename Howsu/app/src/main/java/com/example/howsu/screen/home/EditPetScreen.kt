package com.example.howsu.screen.pet

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.howsu.data.model.Pet
import com.example.howsu.screen.home.EditPetViewModel


// ----------------------------------------------------
// 펫 정보 편집 스크린
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetScreen(
    navController: NavHostController,
    viewModel: EditPetViewModel = viewModel() // Edit ViewModel 주입
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("펫 정보 편집", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "취소")
                    }
                },
                actions = {
                    // 저장 버튼
                    /*IconButton(
                        onClick = {
                            // TODO: 입력 유효성 검사
                            viewModel.savePetDetail()
                            navController.popBackStack() // 저장 후 이전 화면으로 이동
                        },
                        enabled = !uiState.isLoading // 로딩 중이 아닐 때만 저장 가능
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "저장", tint = MaterialTheme.colorScheme.primary)
                    }*/

                    TextButton(   // 텍스트 버튼으로 수정
                        onClick = {
                            // TODO: 입력 유효성 검사
                            viewModel.savePetDetail()
                            navController.popBackStack() // 저장 후 이전 화면으로 이동
                        },
                        enabled = !uiState.isLoading // 로딩 중이 아닐 때만 저장 가능
                    ) {
                        Text(
                            text = "저장",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        // 로딩 중이거나 에러 처리
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("수정 중 오류가 발생했습니다.: ${uiState.error}")
            }
        } else {
            // 편집 가능한 데이터 바인딩
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 프로필 이미지 섹션 (편집 가능)
                item {
                    Spacer(Modifier.height(32.dp))
                    EditablePetProfileImageSection(pet = uiState.pet) {
                        // TODO: 이미지 선택 로직 실행 (갤러리/카메라)
                        // 수정 필요
                    }
                    Spacer(Modifier.height(32.dp))
                }

                // 2. 이름 필드 (편집 가능)
                item {
                    EditableDetailField(
                        label = "이름",
                        value = uiState.name,
                        onValueChange = viewModel::updateName,   // 이름 변경 위한 추가사항
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                // 3. 성별 필드 (편집 가능)
                item {
                    EditableGenderSelectionSection(
                        selectedGender = uiState.gender,
                        isNeutered = uiState.isNeutered,
                        onGenderSelect = viewModel::updateGender,
                        onNeuteredToggle = viewModel::toggleNeutered,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                // 4. 체중 필드 (편집 가능 - 클릭 시 다이얼로그)
                item {
                    EditableWeightField(
                        // Float 형태의 weight를 String으로 변환
                        value = uiState.weight,
                        onValueChange = viewModel::updateWeight,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                // 5. 생년월일/나이 필드 (편집 가능 - 클릭 시 다이얼로그/DatePicker)
                item {
                    val displayDate = if (!uiState.birthdayExact.isNullOrEmpty()) {
                        uiState.birthdayExact
                    } else {
                        "${uiState.birthdayYearApprox}년 ${uiState.birthdayMonthApprox}월 (추정)"
                    }

                    EditableBirthDateAgeSection(
                        birthDate = displayDate ?: "-",
                        ageText = uiState.ageText,
                        onClick = {
                            // TODO: DatePicker 다이얼로그 표시 또는 새 화면으로 이동하여 생년월일/추정 년월 수정
                        },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(50.dp))
                }
            }
        }
    }
}

// ----------------------------------------------------
// 하위 컴포넌트 (편집 가능 버전)
// ----------------------------------------------------

@Composable
fun EditablePetProfileImageSection(pet: Pet?, onImageClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(Color.LightGray.copy(alpha = 0.3f))
            .border(2.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onImageClick), // 이미지 변경 클릭 이벤트 추가
        contentAlignment = Alignment.Center
    ) {
        // ... (PetProfileImageSection과 동일한 이미지/아이콘 로직)
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = Color.Gray
        )

        // 갤러리 아이콘
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.BottomEnd)
                .padding(4.dp),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                // R.drawable.ic_menu_gallery 대신 기본 아이콘 사용 가능
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    modifier = Modifier.size(20.dp),
                    tint = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun EditableDetailField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
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
        // TextField로 변경
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
fun EditableGenderSelectionSection(
    selectedGender: String, // "남아" 또는 "여아"
    isNeutered: Boolean,
    onGenderSelect: (String) -> Unit,
    onNeuteredToggle: () -> Unit,
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
            // 클릭 이벤트 추가된 GenderButton
            EditableGenderButton(
                label = "여아",
                isSelected = selectedGender == "여아",
                onClick = { onGenderSelect("여아") },
                modifier = Modifier.weight(1f)
            )
            EditableGenderButton(
                label = "남아",
                isSelected = selectedGender == "남아",
                onClick = { onGenderSelect("남아") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        // 중성화 여부 토글
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onNeuteredToggle)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if(isNeutered) MaterialTheme.colorScheme.primary else Color.LightGray)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "중성화했어요",
                style = MaterialTheme.typography.bodySmall,
                color = if(isNeutered) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

@Composable
fun EditableGenderButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
    val contentColor = if (isSelected) Color.White else Color.Black
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
            .height(50.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick), // 클릭 이벤트 활성화
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = contentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EditableWeightField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "체중",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
        )
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            trailingIcon = { Text("kg", color = Color.DarkGray, style = MaterialTheme.typography.bodyLarge) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
            shape = RoundedCornerShape(8.dp),
            //label = { Text("예: 5.2") }
        )
    }
}

@Composable
fun EditableBirthDateAgeSection(birthDate: String, ageText: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "생년월일/나이",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
        )
        Spacer(Modifier.height(16.dp))

        // 클릭 이벤트 추가
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clickable(onClick = onClick) // 클릭 이벤트 활성화
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


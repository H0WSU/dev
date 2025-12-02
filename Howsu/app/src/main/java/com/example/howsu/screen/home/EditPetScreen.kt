package com.example.howsu.screen.pet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.howsu.screen.home.EditPetViewModel
import com.example.howsu.screen.todo.YellowBox


// ----------------------------------------------------
// 펫 정보 편집 스크린
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetScreen(
    familyId: String,
    petId: String,
    navController: NavHostController,
    viewModel: EditPetViewModel = viewModel() // Edit ViewModel 주입
) {
    val uiState by viewModel.uiState.collectAsState()

    // 1. 이미지 선택기 정의
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent() // 갤러리에서 콘텐츠를 가져오는 계약
    ) { uri: Uri? ->
        // 2. 결과 처리: URI가 있으면 ViewModel에 업데이트
        if (uri != null) {
            viewModel.updatePetProfileImgaeUri(uri)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("펫 정보 편집", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        // 편집 중이면 취소, 아니면 뒤로 가기
                        if (uiState.isEditing) viewModel.toggledEditMode(false) else navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "취소/뒤로 가기")
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        // 편집 모드: 저장 및 취소 버튼
                        TextButton(onClick = { viewModel.cancelEditing() }) {
                            Text("취소")
                        }
                        TextButton(
                            onClick = { viewModel.savePetProfile() },
                            enabled = !uiState.isLoading
                        ) {
                            Text("저장", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        // 보기 모드: 편집 버튼
                        IconButton(onClick = { viewModel.toggledEditMode(true) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "편집 모드 활성화")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
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
                    EditablePetProfileImageSection(
                        profileImageUrl = uiState.petprofileImageUrl,
                        newProfileImageUri = uiState.newPetprofileImageUri,
                        isEditing = uiState.isEditing, // isEditing 전달
                        onImageClick = {
                            // isEditing일 때만 실행
                            if (uiState.isEditing) {
                                imagePickerLauncher.launch("image/*")
                            }
                        }
                    )
                    Spacer(Modifier.height(32.dp))
                }

                // 2. 이름 필드 (편집 가능)
                item {
                    EditableDetailField(
                        label = "이름",
                        value = uiState.petname,
                        onValueChange = viewModel::updateName,
                        isEditing = uiState.isEditing, // isEditing 전달
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
                        isEditing = uiState.isEditing, // isEditing 전달
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                // 4. 체중 필드 (편집 가능)
                item {
                    EditableWeightField(
                        value = uiState.weight,
                        onValueChange = viewModel::updateWeight,
                        isEditing = uiState.isEditing, //  isEditing 전달
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                // 5. 생년월일/나이 필드 (편집 가능 - 클릭 시 다이얼로그/DatePicker)
                item {
                    val displayDate = if (!uiState.birthdayExact.isNullOrEmpty()) {
                        uiState.birthdayExact!!
                    } else if (!uiState.birthdayYearApprox.isNullOrEmpty()) {
                        "${uiState.birthdayYearApprox}년 ${uiState.birthdayMonthApprox.orEmpty()}월 (추정)"
                    } else {
                        "-"
                    }

                    EditableBirthDateAgeSection(
                        birthDate = displayDate,
                        ageText = uiState.ageText,
                        isEditing = uiState.isEditing, // isEditing 전달
                        onClick = {
                            // isEditing일 때만 DatePicker 호출
                            // TODO: DatePicker 다이얼로그 표시 로직
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
fun EditablePetProfileImageSection(
    profileImageUrl: String?,
    newProfileImageUri: Uri?,
    isEditing: Boolean,
    onImageClick: () -> Unit
) {
    val imageSource = if (newProfileImageUri != null) newProfileImageUri else profileImageUrl
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // 이미지와 테두리를 포함하는 Box
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.3f))
                .border(2.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                // isEditing일 때만 클릭 가능하도록 수정
                .clickable(
                    enabled = isEditing,
                    onClick = onImageClick,
                    interactionSource = interactionSource,
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            // 이미지 로직
            AsyncImage(
                model = imageSource,
                contentDescription = "펫 프로필 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // 수정: 이미지 URL이 없거나 비어있을 때 Icon을 표시
            if (imageSource == null || (imageSource is String && imageSource.isNullOrBlank())) {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = "기본 프로필",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }


        // 갤러리 아이콘 (편집 버튼): isEditing일 때만 표시
        if (isEditing) {
            Surface(
                onClick = onImageClick,
                shape = CircleShape,
                color = YellowBox,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd) // Box의 BottomEnd에 고정
                    .offset(x = (-4).dp, y = (-4).dp) // 프로필 테두리에 살짝 걸치도록 조정
                    .shadow(4.dp, shape = CircleShape),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "사진 변경",
                        modifier = Modifier.size(10.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun EditableDetailField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean, // isEditing 인자 추가
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
            enabled = isEditing, // 편집 모드일 때만 활성화
            readOnly = !isEditing, // 보기 모드일 때 읽기 전용
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.7f),
                disabledIndicatorColor = Color.LightGray.copy(alpha = 0.7f), // 비활성화 시 밑줄 색상 유지
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White, // 비활성화 시 배경색 유지
                disabledTextColor = MaterialTheme.colorScheme.onSurface, // 비활성화 시 글자색 유지
            ),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
fun EditableGenderSelectionSection(
    selectedGender: String,
    isNeutered: Boolean,
    onGenderSelect: (String) -> Unit,
    onNeuteredToggle: () -> Unit,
    isEditing: Boolean, // isEditing 인자 추가
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
        // 1. 성별 버튼 그룹
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 성별 버튼은 isEditing일 때만 클릭 가능해야 합니다.
            EditableGenderButton(
                label = "여아",
                isSelected = selectedGender == "여아",
                onClick = { if (isEditing) onGenderSelect("여아") }, // isEditing 검사
                modifier = Modifier.weight(1f)
            )
            EditableGenderButton(
                label = "남아",
                isSelected = selectedGender == "남아",
                onClick = { if (isEditing) onGenderSelect("남아") }, // isEditing 검사
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        // 2. 중성화 여부 토글
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                // isEditing일 때만 클릭 가능하도록 수정
                .clickable(enabled = isEditing, onClick = onNeuteredToggle)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if(isNeutered) YellowBox else Color.LightGray)
            )
            Spacer(Modifier.width(15.dp))
            Text(
                "중성화했어요",
                style = MaterialTheme.typography.bodyMedium,
                color = if(isNeutered) Color.Black else Color.Gray
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
    val containerColor = if (isSelected) YellowBox else Color.White
    val contentColor = Color.Black
    val borderColor = Color.Gray.copy(alpha = 0.5f)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
            .height(50.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            // 클릭 이벤트는 상위 함수에서 isEditing 검사, 여기서는 onClick 호출
            .clickable(onClick = onClick),
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
    isEditing: Boolean, // isEditing 인자 추가
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("체중", /* ... */)
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = isEditing, // 편집 모드일 때만 활성화
            readOnly = !isEditing, // 보기 모드일 때 읽기 전용
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            trailingIcon = { Text("kg", color = Color.DarkGray, style = MaterialTheme.typography.bodyLarge) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.7f),
                disabledIndicatorColor = Color.LightGray.copy(alpha = 0.7f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            shape = RoundedCornerShape(8.dp),
        )
    }
}

@Composable
fun EditableBirthDateAgeSection(
    birthDate: String,
    ageText: String,
    isEditing: Boolean, // isEditing 인자 추가
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("생년월일/나이", /* ... */)
        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                // isEditing일 때만 클릭 가능하도록 수정
                .clickable(enabled = isEditing, onClick = onClick)
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
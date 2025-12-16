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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.howsu.R
import com.example.howsu.screen.home.EditPetViewModel
import com.example.howsu.screen.todo.ContentBlack
import com.example.howsu.screen.todo.YellowBox
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


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
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.birthdayExact?.let {
            try {
                // "yyyy-MM-dd" 형식 문자열을 밀리초로 변환
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.time
            } catch (e: Exception) {
                null
            }
        }
    )
    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = "펫 정보 편집",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ) },
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
                        // 편집 모드일 때: 저장 및 취소 버튼
                        IconButton(onClick = { viewModel.cancelEditing() }){
                            Icon(Icons.Default.Close, contentDescription = "취소")
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
            // ★ 삭제 성공 시 자동 뒤로 가기
            if (uiState.isPetDeleted) {
                // 홈 화면으로 이동하거나, 펫 목록이 있는 이전 화면으로 돌아갑니다.
                // 펫 편집 화면은 펫 상세에서 왔으므로 2번 뒤로 가기 또는 특정 경로로 이동
                LaunchedEffect(Unit) {
                    navController.popBackStack() // 펫 상세 -> 펫 목록 화면으로 가정
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // TopBar 아래 영역에만 패딩 적용
                contentAlignment = Alignment.TopCenter
            ){
                // 편집 가능한 데이터 바인딩
                LazyColumn(
                    modifier = Modifier.fillMaxSize(), // Box의 남은 공간을 채움
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

                    // 5. 생년월일/나이 필드 (편집 가능)
                    item {
                        val displayDate = if (!uiState.birthdayExact.isNullOrEmpty()) {
                            uiState.birthdayExact!!
                        } else if (!uiState.birthdayYearApprox.isNullOrEmpty()) {
                            "${uiState.birthdayYearApprox}년 ${uiState.birthdayMonthApprox.orEmpty()}월 (추정)"
                        } else {
                            "-"
                        }

                        EditableBirthDateAgeSection(
                            birthdayExact = displayDate,
                            isEditing = uiState.isEditing,
                            onClick = {
                                // isEditing일 때만 DatePicker 호출
                                if (uiState.isEditing) {
                                    // 🌟 상태 변경: DatePicker 표시
                                    showDatePicker = true
                                }
                            },
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        // ★ 하단 고정 버튼 공간 확보를 위한 Spacer 추가
                        Spacer(Modifier.height(100.dp))
                    }
                } // End of LazyColumn

                // ★ 하단 고정 버튼 (Box의 BottomCenter에 배치)
                if (uiState.isEditing) {
                    SaveBottomButton(
                        onSaveClick = { viewModel.savePetProfile() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                } else {
                    DeleteBottomButton(
                        onDeleteClick = {
                            viewModel.deletePetProfile(
                                onSuccess = {
                                    // LaunchedEffect가 네비게이션을 처리
                                }
                            )
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            } // End of Box
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val formatted = formatter.format(Date(millis))

                            // ViewModel에 업데이트 요청
                            viewModel.updateBirthdayExact(formatted)
                        }
                        showDatePicker = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }
}

@Composable
private fun SaveBottomButton(
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit,
) {
    // Surface를 사용하여 배경과 그림자(elevation)를 제어하고 하단에 고정
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = 24.dp, // 양옆 패딩
                    vertical = 16.dp // 상하 패딩
                )
        ) {
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowBox,
                    contentColor = ContentBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("저장하기", fontWeight = FontWeight.Medium, fontSize = 15.sp)
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
            .size(180.dp), // 전체 Box 크기 (이미지+아이콘)
        contentAlignment = Alignment.Center
    ) {
        // 이미지와 테두리를 포함하는 Box
        Box(
            modifier = Modifier
                .size(170.dp) // 실제 이미지 크기
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.3f))
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
            if (imageSource == null || (imageSource is String && imageSource.isNullOrBlank())) {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = "기본 프로필",
                    modifier = Modifier.align(Alignment.Center),
                    tint = Color.Gray
                )
            }
        }

        // 갤러리 아이콘 (편집 버튼)
        if (isEditing) {
            Surface(
                onClick = onImageClick,
                shape = CircleShape,
                color = YellowBox,
                // ★ 수정됨: shadow 위치 변경 (Surface 안에 적용)
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    // ★ 수정됨: 위치 미세 조정 (너무 바깥으로 나가지 않게)
                    .offset(x = (-10).dp, y = (-10).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "사진 변경",
                        modifier = Modifier.size(15.dp), // 아이콘 크기 살짝 키움
                        tint = ContentBlack
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
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
        Spacer(Modifier.height(4.dp))
        // TextField로 변경
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
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
                    .background(if (isNeutered) YellowBox else Color.LightGray)
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
    // 스타일 로직: GenderChip과 동일하게 적용
    val borderColor = if (isSelected) YellowBox else Color(0xFFEAEAEA)
    val textColor = if (isSelected) Color.Black else Color(0xFFBDBDBD)
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    Surface(
        shape = RoundedCornerShape(30.dp), // 둥근 캡슐 모양
        color = Color.White,               // 배경은 항상 흰색
        modifier = modifier
            .height(52.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(30.dp)) // 선택 시 노란색 테두리
            .clickable(onClick = onClick),
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = textColor,       // 선택 안 되면 연한 회색, 선택 되면 검정
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
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
        Text(
            "체중",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
    birthdayExact: String,
    isEditing: Boolean, // isEditing 인자 추가
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayText = remember(birthdayExact) {
        if (birthdayExact.isBlank()) {
            "날짜를 선택해 주세요"
        } else {
            runCatching {
                val localDate = LocalDate.parse(birthdayExact) // yyyy-MM-dd
                val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
                localDate.format(formatter)
            }.getOrElse { "날짜를 선택해 주세요" }
        }
    }

    // 투두 DatePickerField 와 동일한 스타일
    val borderColor = YellowBox
    val contentBlack = Color(0xFF121212)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 24.dp
            )
            .border(1.5.dp, borderColor, RoundedCornerShape(17.dp)),
        shape = RoundedCornerShape(17.dp),
        color = Color.White,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.date_under), // 투두와 같은 아이콘 사용
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.padding(end = 8.dp)
            )

            Column {
                Text(
                    text = "date",
                    fontSize = 10.sp,
                    color = contentBlack.copy(alpha = 0.7f)
                )
                Text(
                    text = displayText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = contentBlack
                )
            }
        }
    }
}
@Composable
private fun DeleteBottomButton(
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
) {
    // Surface를 사용하여 배경과 그림자(elevation)를 제어하고 하단에 고정
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = 24.dp, // 양옆 패딩
                    vertical = 16.dp // 상하 패딩
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onDeleteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowBox,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("반려동물 삭제하기", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }
        }
    }
}
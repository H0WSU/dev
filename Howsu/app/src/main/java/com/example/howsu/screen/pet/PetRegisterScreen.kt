package com.example.howsu.screen.pet

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.howsu.Pet.PetRegisterViewModel
import com.example.howsu.R
import com.example.howsu.data.model.BirthdayInputType
import com.example.howsu.data.model.PetRegisterStep
import com.example.howsu.data.model.PetRegisterUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


/* -----------------------------------------------------------------------
   메인 화면
   ----------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetRegisterScreen(
    viewModel: PetRegisterViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // 이미지 선택 런처 (갤러리/드라이브 등)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val url = uri.toString()
            when (uiState.step) {
                PetRegisterStep.NICKNAME -> {
                    viewModel.updateUserProfileImage(url)  // 닉네임 단계 → 유저 이미지
                }
                PetRegisterStep.PHOTO_NAME,
                PetRegisterStep.GENDER_WEIGHT,
                PetRegisterStep.BIRTHDAY -> {
                    viewModel.updatePetProfileImage(url)   // 나머지 단계 → 펫 이미지
                }
            }
        }
    }

    val (title, stepIndex) = when (uiState.step) {
        PetRegisterStep.NICKNAME    -> "닉네임 등록하기" to 1
        PetRegisterStep.PHOTO_NAME  -> "반려동물 등록하기" to 2
        PetRegisterStep.GENDER_WEIGHT -> "반려동물 등록하기" to 3
        PetRegisterStep.BIRTHDAY    -> "반려동물 등록하기" to 4
    }

    val isNicknameStep = uiState.step == PetRegisterStep.NICKNAME
    val isLastStep = uiState.step == PetRegisterStep.BIRTHDAY

    // 닉네임 단계에서는 닉네임 필수, 그 외 단계는 ViewModel 로직 사용
    val nextButtonEnabled =
        if (isNicknameStep) uiState.nickName.isNotBlank()
        else viewModel.isNextEnabled()

    Scaffold(
        topBar = {
            PetRegisterTopBar(
                title = title,
                step = stepIndex,
                totalStep = 4,
                onBack = {
                    if (uiState.step == PetRegisterStep.NICKNAME) {
                        navController.popBackStack()
                    } else {
                        viewModel.previousStep()
                    }
                },
                showBack = uiState.step != PetRegisterStep.NICKNAME, // ← 닉네임 단계면 false
                onCloseClick = { showExitDialog = true }
            )
        },
        bottomBar = {
            PetRegisterBottomBar(
                enabled = nextButtonEnabled,                          // ← 여기만 수정
                isLastStep = uiState.step == PetRegisterStep.BIRTHDAY,
                showSkip = uiState.step != PetRegisterStep.NICKNAME,
                onNext = {
                    if (uiState.step == PetRegisterStep.BIRTHDAY) {
                        viewModel.submit { _ ->
                            navController.navigate("pet_register_complete")
                        }
                    } else {
                        viewModel.nextStep()
                    }
                },
                onSkip = { navController.navigate("home") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState.step) {
                PetRegisterStep.NICKNAME ->
                    NicknameStep(
                        state = uiState,
                        onNickname = viewModel::updateNickName,
                        onPickImage = {
                            imagePickerLauncher.launch("image/*")
                        }
                    )

                PetRegisterStep.PHOTO_NAME ->
                    PhotoNameStep(
                        state = uiState,
                        onName = viewModel::updatePetName,
                        onPickImage = {
                            imagePickerLauncher.launch("image/*")
                        }
                    )

                PetRegisterStep.GENDER_WEIGHT ->
                    GenderWeightStep(
                        state = uiState,
                        onGender = viewModel::updateGender,
                        onWeight = viewModel::updateWeight,
                        onNeuteredChanged = viewModel::updateNeutered
                    )

                PetRegisterStep.BIRTHDAY ->
                    BirthdayStep(
                        state = uiState,
                        onType = viewModel::updateBirthdayType,
                        onExact = viewModel::updateBirthdayExact,
                        onYear = viewModel::updateBirthdayYear,
                        onMonth = viewModel::updateBirthdayMonth,
                        onDatePickerClicked = { showDatePicker = true },
                        selectedDate = datePickerState.selectedDateMillis
                            ?: System.currentTimeMillis()
                    )
            }
        }
    }

    // 달력 다이얼로그
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
            DatePicker(state = datePickerState)
        }
    }

    // X 눌렀을 때 나가기 경고
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("등록을 그만하시겠어요?") },
            text = { Text("지금까지 입력한 내용은 저장되지 않고 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("나가기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("계속 작성할게요")
                }
            }
        )
    }
}

/* -----------------------------------------------------------------------
   공통 TopBar / BottomBar
   ----------------------------------------------------------------------- */

@Composable
fun PetRegisterTopBar(
    title: String,
    step: Int,
    totalStep: Int,
    onBack: () -> Unit,
    showBack: Boolean,              // ← 추가
    onCloseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(48.dp)
        ) {
            // 왼쪽 뒤로가기 버튼
            if (showBack) {          // ← 조건부로 표시
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(39.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        modifier = Modifier.size(39.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Step $step/$totalStep",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFFEDEDED))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(step.toFloat() / totalStep.toFloat())
                    .background(Color.Black)
            )
        }
    }
}

@Composable
fun PetRegisterBottomBar(
    enabled: Boolean,
    isLastStep: Boolean,
    showSkip: Boolean,          // ← 건너뛰기 보여줄지 여부 추가
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 60.dp)
    ) {
        Button(
            onClick = onNext,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) Color.Black else Color(0xFFE0E0E0),
                contentColor = if (enabled) Color.White else Color(0xFFBDBDBD)
            )
        ) {
            Text(
                text = if (isLastStep) "완료하기" else "계속하기",
                fontWeight = FontWeight.Medium, fontSize = 14.sp
            )
        }

        if (showSkip) {  // ← 여기서 조건부로 렌더링
            Spacer(modifier = Modifier.height(13.dp))

            Button(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Gray
                )
            ) {
                Text(
                    text = "나중에 등록하고 싶어요! 건너뛰기",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


/* -----------------------------------------------------------------------
   공통 프로필 원
   ----------------------------------------------------------------------- */

@Composable
fun PetProfileCircle(
    imageUrl: String?,
    size: Dp = 180.dp,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(size)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 프로필 원
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape) // 원 모양으로 잘라주기
                .background(Color(0xFFF5F5F5)) // 배경색(없어도 됨)
                .border(
                    width = 2.dp,
                    color = Color(0xFFEDEDED),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNullOrBlank()) {
                // 기본 상태일 때는 비워두거나 아이콘 넣기
                // 예시: 가운데에 작은 아이콘
                /* Icon(
                    imageVector = Icons.Outlined.Pets,
                    contentDescription = "기본 프로필",
                    tint = Color.LightGray,
                    modifier = Modifier.size(40.dp)
                ) */
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Pet Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize() // 원 안을 꽉 채우기
                )
            }
        }

        // 카메라 아이콘 배지
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(32.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color(0xFFE0E0E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = "사진 변경",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


/* -----------------------------------------------------------------------
   Step 0 : 닉네임 등록
   ----------------------------------------------------------------------- */

@Composable
fun NicknameStep(
    state: PetRegisterUiState,
    onNickname: (String) -> Unit,
    onPickImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        PetProfileCircle(
            imageUrl = state.profileUserImageUrl,
            size = 200.dp,
            onClick = onPickImage
        )

        Text(
            text = "사용할 닉네임을 입력해 주세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.nickName,
            onValueChange = onNickname,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("닉네임 입력하기") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

/* -----------------------------------------------------------------------
   Step 1 : 반려동물 이름 + 사진
   ----------------------------------------------------------------------- */

@Composable
fun PhotoNameStep(
    state: PetRegisterUiState,
    onName: (String) -> Unit,
    onPickImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        PetProfileCircle(
            imageUrl = state.profilePetImageUrl,
            size = 200.dp,
            onClick = onPickImage
        )

        Text(
            text = "아이의 이름을 입력해 주세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )

        OutlinedTextField(
            value = state.petName,
            onValueChange = onName,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("이름 입력하기") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

/* -----------------------------------------------------------------------
   Step 2 : 성별 & 몸무게
   ----------------------------------------------------------------------- */

@Composable
fun GenderWeightStep(
    state: PetRegisterUiState,
    onGender: (String) -> Unit,
    onWeight: (String) -> Unit,
    onNeuteredChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),   // ← 스크롤 가능
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        PetProfileCircle(
            imageUrl = state.profilePetImageUrl,
            size = 100.dp
        )

        Text(
            text = state.petName.ifBlank { "우리 아이" },
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "성별은 무엇인가요?",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenderChip(
                text = "여아",
                selected = state.gender == "FEMALE",
                onClick = { onGender("FEMALE") },
                modifier = Modifier.weight(1f)
            )
            GenderChip(
                text = "남아",
                selected = state.gender == "MALE",
                onClick = { onGender("MALE") },
                modifier = Modifier.weight(1f)
            )
        }

        // 중성화 체크 한 줄
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val current = state.isNeutered == true
                    onNeuteredChanged(!current)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = state.isNeutered == true,
                onClick = {
                    val current = state.isNeutered == true
                    onNeuteredChanged(!current)
                }
            )
            Text(
                text = "중성화했어요",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF828282)
            )
        }

        Text(
            text = "몸무게는 몇 kg 인가요?",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Start),
            fontSize = 16.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.weight,
                onValueChange = onWeight,
                modifier = Modifier.weight(1f),
                placeholder = { Text("예) 2.3") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Text(
                text = "kg",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // 여유만 조금 주기
    }
}

@Composable
fun GenderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) Color.Black else Color.White
    val border = if (selected) Color.Black else Color(0xFFE0E0E0)
    val textColor = if (selected) Color.White else Color.Black

    Box(
        modifier = modifier
            .height(44.dp)
            .border(1.dp, border, RoundedCornerShape(24.dp))
            .background(bg, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor)
    }
}

/* -----------------------------------------------------------------------
   Step 3 : 생년월일 (정확 / 대략)
   ----------------------------------------------------------------------- */

@Composable
fun BirthdayStep(
    state: PetRegisterUiState,
    onType: (BirthdayInputType) -> Unit,
    onExact: (String) -> Unit,
    onYear: (String) -> Unit,
    onMonth: (String) -> Unit,
    onDatePickerClicked: () -> Unit,
    selectedDate: Long
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        PetProfileCircle(
            imageUrl = state.profilePetImageUrl,
            size = 100.dp
        )

        Text(
            text = state.petName.ifBlank { "우리 아이" },
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "생년월일을 알고 있나요?",
            style = MaterialTheme.typography.bodyMedium
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = state.birthdayInputType == BirthdayInputType.EXACT,
                    onClick = { onType(BirthdayInputType.EXACT) }
                )
                Text(text = "정확히 알고 있어요")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = state.birthdayInputType == BirthdayInputType.APPROX,
                    onClick = { onType(BirthdayInputType.APPROX) }
                )
                Text(text = "대략만 알고 있어요")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (state.birthdayInputType) {
            BirthdayInputType.EXACT -> {
                DatePickerField(
                    selectedDateMillis = selectedDate,
                    onClick = onDatePickerClicked
                )
            }

            BirthdayInputType.APPROX -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.birthdayYearApprox,
                        onValueChange = onYear,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("년도 (예: 2021)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = state.birthdayMonthApprox,
                        onValueChange = onMonth,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("월 (1~12)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    selectedDateMillis: Long,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
    val dateString = formatter.format(Date(selectedDateMillis))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                Text("date", fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = dateString,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/* -----------------------------------------------------------------------
   프리뷰
   ----------------------------------------------------------------------- */

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun PetRegisterScreenPreview() {
    val navController = rememberNavController()
    val vm = PetRegisterViewModel()
    PetRegisterScreen(viewModel = vm, navController = navController)
}

@Preview(showBackground = true)
@Composable
fun PhotoNameStepPreview() {
    PhotoNameStep(
        state = PetRegisterUiState(
            step = PetRegisterStep.PHOTO_NAME
        ),
        onName = {},
        onPickImage = {}
    )
}

@Preview(showBackground = true)
@Composable
fun GenderWeightStepPreview() {
    GenderWeightStep(
        state = PetRegisterUiState(
            step = PetRegisterStep.GENDER_WEIGHT,
            petName = "자몽"
        ),
        onGender = {},
        onWeight = {},
        onNeuteredChanged = {}
    )
}

@Preview(showBackground = true)
@Composable
fun BirthdayStepPreview() {
    BirthdayStep(
        state = PetRegisterUiState(
            step = PetRegisterStep.BIRTHDAY,
            petName = "자몽",
            birthdayInputType = BirthdayInputType.EXACT
        ),
        onType = {},
        onExact = {},
        onYear = {},
        onMonth = {},
        onDatePickerClicked = {},
        selectedDate = System.currentTimeMillis()
    )
}

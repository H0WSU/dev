package com.example.howsu.screen.pet

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import com.example.howsu.screen.schedule.MonthYearPickerDialog
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale



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
            // 이제는 모든 스텝에서 펫 프로필 사진만 사용
            when (uiState.step) {
                PetRegisterStep.PHOTO_NAME,
                PetRegisterStep.GENDER_WEIGHT,
                PetRegisterStep.BIRTHDAY -> {
                    viewModel.updatePetProfileImage(url)
                }
            }
        }
    }

    // 제목 + 스텝 번호 (3단계)
    val (title, stepIndex) = when (uiState.step) {
        PetRegisterStep.PHOTO_NAME       -> "반려동물 등록하기" to 1
        PetRegisterStep.GENDER_WEIGHT    -> "반려동물 등록하기" to 2
        PetRegisterStep.BIRTHDAY         -> "반려동물 등록하기" to 3
    }

    val isLastStep = uiState.step == PetRegisterStep.BIRTHDAY

    // 다음 버튼 활성화 여부는 ViewModel 로직만 사용
    val nextButtonEnabled = viewModel.isNextEnabled()

    Scaffold(
        topBar = {
            PetRegisterTopBar(
                title = title,
                step = stepIndex,
                totalStep = 3,                       // 스텝 개수 3개로 변경
                onBack = {
                    if (uiState.step == PetRegisterStep.PHOTO_NAME) {
                        // 첫 단계에서는 이전 화면으로
                        navController.popBackStack()
                    } else {
                        // 그 외에는 이전 스텝으로 이동
                        viewModel.previousStep()
                    }
                },
                showBack = true,                    // 이제 항상 뒤로가기 표시
                onCloseClick = { showExitDialog = true }
            )
        },
        bottomBar = {
            PetRegisterBottomBar(
                enabled = nextButtonEnabled,
                isLastStep = isLastStep,
                showSkip = true,                    // 닉네임 스텝 없으니 항상 "건너뛰기" 보여줄지 여부는 선택
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
                        onDatePickerClicked = { showDatePicker = true }
                    )
            }
        }
    }

    // 달력 다이얼로그 (EXACT / APPROX 공통)
// 달력 다이얼로그
    if (showDatePicker) {

        if (uiState.birthdayInputType == BirthdayInputType.EXACT) {
            // 1) 정확히 알고 있어요 → 날짜 캘린더
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

        } else {
            // 2) 대략만 알고 있어요 → 년/월 다이얼로그 (Schedule 화면과 동일)

            val cal = Calendar.getInstance()
            val initialYear = uiState.birthdayYearApprox.toIntOrNull()
                ?: cal.get(Calendar.YEAR)
            val initialMonth = uiState.birthdayMonthApprox.toIntOrNull()
                ?: (cal.get(Calendar.MONTH) + 1)   // 0~11 → 1~12

            MonthYearPickerDialog(
                initialYear = initialYear,
                initialMonth = initialMonth,
                onDismiss = { showDatePicker = false },
                onConfirm = { year, month ->
                    viewModel.updateBirthdayYear(year.toString())
                    viewModel.updateBirthdayMonth(month.toString())
                    showDatePicker = false
                }
            )
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
            size = 120.dp
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BirthdayStep(
    state: PetRegisterUiState,
    onType: (BirthdayInputType) -> Unit,
    onExact: (String) -> Unit,
    onYear: (String) -> Unit,
    onMonth: (String) -> Unit,
    onDatePickerClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        PetProfileCircle(
            imageUrl = state.profilePetImageUrl,
            size = 120.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = state.petName.ifBlank { "우리 아이" },
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "생년월일을 알고 있나요?",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        /* ---------- 1) 정확히 알고 있어요 ---------- */
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onType(BirthdayInputType.EXACT)
                        onDatePickerClicked()          // 라디오 줄 전체 눌러도 달력 열기
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = state.birthdayInputType == BirthdayInputType.EXACT,
                    onClick = {
                        onType(BirthdayInputType.EXACT)
                        onDatePickerClicked()          // 동그라미만 눌러도 달력 열기
                    }
                )
                Text(text = "정확히 알고 있어요")
            }

            AnimatedVisibility(
                visible = state.birthdayInputType == BirthdayInputType.EXACT,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                DatePickerField(
                    birthdayExact = state.birthdayExact,   // ← 문자열 기준
                    onClick = onDatePickerClicked
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        /* ---------- 2) 제대로 알지 못해요 (대략) ---------- */
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onType(BirthdayInputType.APPROX)
                    onDatePickerClicked()   // ← 년/월 다이얼로그 열기
                }
            ) {
                RadioButton(
                    selected = state.birthdayInputType == BirthdayInputType.APPROX,
                    onClick = {
                        onType(BirthdayInputType.APPROX)
                        onDatePickerClicked()
                    }
                )
                Text(text = "대략만 알고 있어요")
            }

            AnimatedVisibility(
                visible = state.birthdayInputType == BirthdayInputType.APPROX,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ApproxYearMonthField(
                    year = state.birthdayYearApprox,
                    month = state.birthdayMonthApprox,
                    onClick = onDatePickerClicked   // 카드 눌러도 연/월 다이얼로그
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    birthdayExact: String,
    onClick: () -> Unit
) {
    // 표시용 문자열 계산
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
                    text = displayText,               // ← 여기
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ApproxYearMonthField(
    year: String,
    month: String,
    onClick: () -> Unit
) {
    val now = LocalDate.now()

    // 나이 계산
    val ageText = remember(year, month) {
        val y = year.toIntOrNull()
        val m = month.toIntOrNull()

        if (y == null || m == null || m !in 1..12) {
            null
        } else {
            var age = now.year - y
            if (now.monthValue < m) age--    // 생일 안 지난 경우 -1
            if (age < 0) age = 0
            "${age}살"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )

            Column {
                Text("date", fontSize = 10.sp, color = Color.Gray)

                if (year.isNotBlank() && month.isNotBlank()) {
                    Text(
                        text = "${year}년 ${month}월",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "생년월을 선택해 주세요",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (ageText != null) {
                Text(
                    text = ageText,
                    fontSize = 12.sp,
                    color = Color.Gray
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
    )
}

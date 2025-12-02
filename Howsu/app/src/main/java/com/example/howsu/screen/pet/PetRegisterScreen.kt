package com.example.howsu.screen.pet

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.howsu.Pet.PetRegisterViewModel
import com.example.howsu.data.model.BirthdayInputType
import com.example.howsu.data.model.PetRegisterStep
import com.example.howsu.screen.schedule.MonthYearPickerDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.howsu.data.model.PetRegisterUiState
import com.example.howsu.screen.pet.bottombar.PetRegisterBottomBar
import com.example.howsu.screen.pet.component.ImageSourceBottomSheet
import com.example.howsu.screen.pet.step.BirthdayStep
import com.example.howsu.screen.pet.step.GenderWeightStep
import com.example.howsu.screen.pet.step.PhotoNameStep
import com.example.howsu.screen.pet.component.RelationBottomSheet
import com.example.howsu.screen.pet.step.RelationshipStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetRegisterScreen(
    viewModel: PetRegisterViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showRelationSheet by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리 런처 (사진 1장만 선택)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updatePetProfileImage(it.toString())
        }
    }


    // 카메라 런처
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            viewModel.updatePetProfileImage(cameraImageUri.toString())
        }
    }

    // 카메라 여는 실제 함수
    fun startCamera() {
        val uri = createImageUri(context)
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    // 카메라 권한 요청 런처
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            // 필요하면 여기서 "권한이 필요합니다" 안내용 UI/Toast 추가 가능
        }
    }


    val (title, stepIndex) = when (uiState.step) {
        PetRegisterStep.PHOTO_NAME    -> "반려동물 등록하기" to 1
        PetRegisterStep.GENDER_WEIGHT -> "반려동물 등록하기" to 2
        PetRegisterStep.BIRTHDAY      -> "반려동물 등록하기" to 3
        PetRegisterStep.RELATIONSHIP  -> "반려동물 등록하기" to 4
    }

    val isLastStep = uiState.step == PetRegisterStep.RELATIONSHIP

    Scaffold(
        containerColor = Color.White,
        topBar = {
            PetRegisterTopBar(
                title = title,
                step = stepIndex,
                totalStep = 4,
                onBack = {
                    if (uiState.step == PetRegisterStep.PHOTO_NAME) {
                        navController.popBackStack()
                    } else {
                        viewModel.previousStep()
                    }
                },
                showBack = uiState.step != PetRegisterStep.PHOTO_NAME,
                onCloseClick = { showExitDialog = true }
            )
        },
        bottomBar = {
            PetRegisterBottomBar(
                enabled = viewModel.isNextEnabled(),
                isLastStep = isLastStep,
                showSkip = true,
                onNext = {
                    if (isLastStep) {
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
            val onPickImage: () -> Unit = { showImageSourceDialog = true }

            when (uiState.step) {

                PetRegisterStep.PHOTO_NAME -> PhotoNameStep(
                    state = uiState,
                    onName = viewModel::updatePetName,
                    onPickImage = onPickImage
                )

                PetRegisterStep.GENDER_WEIGHT -> GenderWeightStep(
                    state = uiState,
                    onGender = viewModel::updateGender,
                    onWeight = viewModel::updateWeight,
                    onNeuteredChanged = viewModel::updateNeutered
                )

                PetRegisterStep.BIRTHDAY -> BirthdayStep(
                    state = uiState,
                    onType = viewModel::updateBirthdayType,
                    onExact = viewModel::updateBirthdayExact,
                    onYear = viewModel::updateBirthdayYear,
                    onMonth = viewModel::updateBirthdayMonth,
                    onDatePickerClicked = { showDatePicker = true }
                )

                PetRegisterStep.RELATIONSHIP -> RelationshipStep(
                    state = uiState,
                    onRelationClick = { showRelationSheet = true }
                )
            }
        }
    }

    /* ------------------------- 생일 다이얼로그 ------------------------- */
    if (showDatePicker) {
        if (uiState.birthdayInputType == BirthdayInputType.EXACT) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val millis = datePickerState.selectedDateMillis
                            if (millis != null) {
                                val formatter =
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
            val cal = Calendar.getInstance()
            val initialYear = uiState.birthdayYearApprox.toIntOrNull()
                ?: cal.get(Calendar.YEAR)
            val initialMonth = uiState.birthdayMonthApprox.toIntOrNull()
                ?: (cal.get(Calendar.MONTH) + 1)

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

    /* ------------------------- 관계 바텀 시트 ------------------------- */
    if (showRelationSheet) {
        RelationBottomSheet(
            currentRelation = uiState.relation,
            onDismiss = { showRelationSheet = false },
            onConfirm = { selected ->
                viewModel.updateRelation(selected)
                showRelationSheet = false
            }
        )
    }

    /* ------------------------- 사진 촬영 / 앨범 선택 다이얼로그 ------------------------- */
    if (showImageSourceDialog) {
        ImageSourceBottomSheet(
            onDismiss = { showImageSourceDialog = false },
            onPickGallery = {
                showImageSourceDialog = false
                // GetContent 런처에 MIME 타입만 넘겨주면 됨
                galleryLauncher.launch("image/*")
            },
            onTakePhoto = {
                showImageSourceDialog = false

                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    startCamera()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        )
    }


    /* ------------------------- X 눌렀을 때 나가기 경고 -------------------- */
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

/** 카메라 촬영용 임시 파일 Uri 생성 */
private fun createImageUri(context: Context): Uri {
    val imageFile = File(
        context.cacheDir,
        "pet_${System.currentTimeMillis()}.jpg"
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}




@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "PetRegisterScreen 전체")
@Composable
fun PetRegisterScreenPreview() {
    val navController = rememberNavController()
    val vm = PetRegisterViewModel()
    PetRegisterScreen(viewModel = vm, navController = navController)
}

/* -----------------------------------------------------------------------
   Step 1 : 이름 + 사진
   ----------------------------------------------------------------------- */

@Preview(showBackground = true, name = "Step 1 - 이름/사진")
@Composable
fun PhotoNameStepPreview() {
    PhotoNameStep(
        state = PetRegisterUiState(
            step = PetRegisterStep.PHOTO_NAME,
            profilePetImageUrl = null,
            petName = ""
        ),
        onName = {},
        onPickImage = {}
    )
}

/* -----------------------------------------------------------------------
   Step 2 : 성별 + 몸무게
   ----------------------------------------------------------------------- */

@Preview(showBackground = true, name = "Step 2 - 성별/몸무게")
@Composable
fun GenderWeightStepPreview() {
    GenderWeightStep(
        state = PetRegisterUiState(
            step = PetRegisterStep.GENDER_WEIGHT,
            petName = "자몽",
            gender = "FEMALE",
            weight = "2.3",
            isNeutered = true
        ),
        onGender = {},
        onWeight = {},
        onNeuteredChanged = {}
    )
}

/* -----------------------------------------------------------------------
   Step 3 : 생년월일
   ----------------------------------------------------------------------- */

@Preview(showBackground = true, name = "Step 3 - 생년월일(정확)")
@Composable
fun BirthdayStepExactPreview() {
    BirthdayStep(
        state = PetRegisterUiState(
            step = PetRegisterStep.BIRTHDAY,
            petName = "자몽",
            birthdayInputType = BirthdayInputType.EXACT,
            birthdayExact = "2023-05-10"
        ),
        onType = {},
        onExact = {},
        onYear = {},
        onMonth = {},
        onDatePickerClicked = {}
    )
}

@Preview(showBackground = true, name = "Step 3 - 생년월일(대략)")
@Composable
fun BirthdayStepApproxPreview() {
    BirthdayStep(
        state = PetRegisterUiState(
            step = PetRegisterStep.BIRTHDAY,
            petName = "자몽",
            birthdayInputType = BirthdayInputType.APPROX,
            birthdayYearApprox = "2021",
            birthdayMonthApprox = "8"
        ),
        onType = {},
        onExact = {},
        onYear = {},
        onMonth = {},
        onDatePickerClicked = {}
    )
}

/* -----------------------------------------------------------------------
   Step 4 : 가족 관계
   ----------------------------------------------------------------------- */

@Preview(showBackground = true, name = "Step 4 - 관계 선택")
@Composable
fun RelationshipStepPreview() {
    RelationshipStep(
        state = PetRegisterUiState(
            step = PetRegisterStep.RELATIONSHIP,
            petName = "자몽",
            relation = "언니"
        ),
        onRelationClick = {}
    )
}

/* -----------------------------------------------------------------------
        등록 완료 화면
   ----------------------------------------------------------------------- */


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, name = "반려동물 등록 완료 화면")
@Composable
fun PetRegisterCompleteScreenPreview() {
    val dummyState = PetRegisterUiState(
        profilePetImageUrl = null,
        petName = "자몽이",
        nickName = "자몽이사랑",
        relation = "언니",
        gender = "FEMALE",
        isNeutered = true,
        weight = "2.3",
        birthdayInputType = BirthdayInputType.EXACT,
        birthdayExact = "2023-05-10"
    )

    PetRegisterCompleteScreen(
        uiState = dummyState,
        onAddMore = {},
        onFinish = {},
        onPickImage = {}
    )
}

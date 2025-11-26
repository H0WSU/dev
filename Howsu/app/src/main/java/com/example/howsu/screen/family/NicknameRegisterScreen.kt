package com.example.howsu.screen.family

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.howsu.screen.todo.ContentBlack
import com.example.howsu.screen.todo.YellowBox
import com.example.howsu.screen.pet.component.DoubleRingProfileImage
import com.example.howsu.screen.pet.component.ImageSourceBottomSheet
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File
import androidx.core.content.FileProvider

@Composable
fun NicknameRegisterScreen(
    navController: NavHostController,
    onNicknameComplete: (String, String?) -> Unit = { _, _ -> },
    viewModel: NicknameRegisterViewModel = viewModel()
) {
    val nickname = viewModel.nickname
    val profileImageUrl = viewModel.profileImageUrl
    val isNextEnabled = nickname.isNotBlank()

    val context = LocalContext.current

    // 사진 선택 관련 상태
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // 앨범 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult

        val uri = result.data?.data
        if (uri != null) {
            viewModel.profileImageUrl = uri.toString()
        }
    }

    // 카메라 런처
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            viewModel.profileImageUrl = cameraImageUri.toString()
        }
    }

    // 카메라 실제 실행 함수
    fun startCamera() {
        val uri = createImageUriForFamily(context)
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
            // 필요하면 "권한이 필요합니다" 안내 UI 추가 가능
        }
    }

    Scaffold(
        topBar = {
            NicknameRegisterTopBar(
                onBack = {
                    Firebase.auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        },
        bottomBar = {
            NicknameRegisterBottomBar(
                enabled = isNextEnabled,
                onNext = {
                    viewModel.saveNicknameToFirebase {
                        onNicknameComplete(nickname, viewModel.profileImageUrl)
                    }
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // 프로필 이미지 (눌렀을 때 bottom sheet 열기)
            DoubleRingProfileImage(
                imageUrl = profileImageUrl,
                onClick = { showImageSourceDialog = true }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "사용할 닉네임을 입력해 주세요",
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { viewModel.nickname = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "닉네임 입력하기",
                        color = Color(0xFFBDBDBD),
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor = Color.Black
                )
            )
        }

        // 사진 촬영 / 앨범 선택 BottomSheet
        if (showImageSourceDialog) {
            ImageSourceBottomSheet(
                onDismiss = { showImageSourceDialog = false },
                onPickGallery = {
                    showImageSourceDialog = false

                    // 이미지 선택용 Intent
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "image/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }

                    // 앱 선택창 강제 호출
                    val chooser = Intent.createChooser(intent, "사진 선택")

                    galleryLauncher.launch(chooser)
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
    }
}

/**
 * 가족 닉네임 화면용 카메라 임시 파일 Uri 생성
 * PetRegisterScreen 쪽 FileProvider 설정과 file_paths.xml 과 반드시 일치해야 함
 */
private fun createImageUriForFamily(context: Context): Uri {
    val imageFile = File(
        context.cacheDir,
        "family_${System.currentTimeMillis()}.jpg"
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@Composable
fun NicknameRegisterTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        Text(
            text = "닉네임 등록하기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로 가기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NicknameRegisterBottomBar(enabled: Boolean, onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 60.dp)
    ) {
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) YellowBox else Color(0xFFD6D6D6),
                contentColor = ContentBlack,
                disabledContainerColor = Color(0xFFD6D6D6),
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = enabled
        ) {
            Text(text = "계속하기", fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun NicknameRegisterScreenPreview() {
    val navController = rememberNavController()
    NicknameRegisterScreen(navController = navController)
}

package com.example.howsu.screen.mypage

// Dummy Icon for example purposes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage


@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: EditProfileViewModel = viewModel()
) {
    // UI 상태를 관찰
    val uiState by viewModel.uiState.collectAsState()

    // 로딩 중이거나 에러가 있을 때 처리
    if (uiState.isLoading) {
        // [TODO] 로딩 중 스피너 표시
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            EditProfileTopBar(
                navController = navController,
                isEditing = uiState.isEditing,
                onEditClick = { viewModel.toggledEditMode(true) },
                onSaveClick = { viewModel.saveProfile() },
                onCancelClick = { viewModel.cancelEditing() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 1. 프로필 이미지 영역
            ProfileImageArea(
                profileImageUrl = uiState.profileImageUrl,
                isEditing = uiState.isEditing,
                onImageClick = {
                    // [TODO] 이미지 선택기 (Launcher for result) 호출 로직
                    // 선택 후 viewModel.updateProfileImageUri(selectedUri) 호출
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. 사용자 정보 입력/표시 영역
            UserInfoFields(
                uiState = uiState,
                onNameChange = viewModel::updateName,
                // [TODO] 관계 변경 함수 추가 (만약 관계도 ViewModel 상태에 있다면)
                // onRelationChange = viewModel::updateRelation
            )
        }
    }
}

// ----------------------------------------------------------------------
// Compose Components
// ----------------------------------------------------------------------

/**
 * 상단 바 (TopBar)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileTopBar(
    navController: NavHostController,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    TopAppBar(
        title = { Text("내 정보 수정하기") },
        navigationIcon = {
            IconButton(onClick = {
                if (isEditing) onCancelClick() else navController.popBackStack()
            }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로 가기")
            }
        },
        actions = {
            if (isEditing) {
                // 편집 모드일 때: 저장 및 취소 버튼
                TextButton(onClick = onCancelClick) {
                    Text("취소")
                }
                TextButton(onClick = onSaveClick) {
                    Text("저장")
                }
            } else {
                // 보기 모드일 때: 편집 버튼
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "편집")
                }
            }
        }
    )
}

/**
 * 프로필 이미지 영역
 */
@Composable
fun ProfileImageArea(
    profileImageUrl: String?,
    isEditing: Boolean,
    onImageClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(200.dp),   // 프로필 크기 수정 1
        contentAlignment = Alignment.BottomEnd
    ) {
        // 프로필 이미지 표시
        AsyncImage(
            model = profileImageUrl,
            contentDescription = "프로필 사진",
            modifier = Modifier
                .size(200.dp)   // 프로필 크기 수정 2
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,
            // Placeholder/Error 이미지 처리는 Coil 설정에 따라 달라집니다.
        )

        // 편집 모드일 때만 이미지 변경 아이콘 표시
        if (isEditing) {
            Surface(
                onClick = onImageClick,
                modifier = Modifier.size(36.dp).offset(x = 4.dp, y = 4.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "사진 변경",
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

/**
 * 사용자 정보 필드 (닉네임, 관계 등)
 */
@Composable
fun UserInfoFields(
    uiState: ProfileUiState,
    onNameChange: (String) -> Unit,
    // onRelationChange: (String) -> Unit // 관계 변경 핸들러
) {
    // 1. 아이디 (읽기 전용)
    ProfileField(
        label = "아이디",
        value = uiState.email,
        isEditing = false,
        onValueChange = {}
    )

    // 정보 수정 안내 문구
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "정보 수정은 사진 및 닉네임, 관계 설정만 가능해요",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start // 왼쪽 정렬

    )

    Spacer(modifier = Modifier.height(16.dp))

    // 2. 닉네임 (닉네임)
    ProfileField(
        label = "닉네임",
        value = uiState.name,
        isEditing = uiState.isEditing,
        onValueChange = onNameChange
    )
    Spacer(modifier = Modifier.height(16.dp))

    // 3. 반려동물과의 관계 (드롭다운)
    // family id 랑 family relationship 가져와야할듯
    Text(text = "반려동물과의 관계", style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())

    TextField(
        value = "언니", // 현재 uiState.relation 값 사용 필요
        onValueChange = {}, // 관계 변경 로직 연결
        enabled = uiState.isEditing, // 편집 모드일 때만 활성화
        readOnly = !uiState.isEditing,
        trailingIcon = { Icon(
            Icons.Default.Edit,
            contentDescription = "관계 선택") },
        modifier = Modifier.fillMaxWidth()
    )


}

/**
 * 재사용 가능한 프로필 필드 컴포넌트
 */
@Composable
fun ProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing,
            readOnly = !isEditing,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.7f),
                // 배경색을 투명하게 설정
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
            //contentPadding = PaddingValues(0.dp) // 모든 패딩을 0으로 설정
        )
    }
}
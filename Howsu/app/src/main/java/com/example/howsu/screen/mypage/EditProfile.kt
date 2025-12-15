package com.example.howsu.screen.mypage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.ModeEdit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.howsu.screen.todo.YellowBox

@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: EditProfileViewModel = viewModel()
) {

    // 이미지 선택기
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent() // 갤러리에서 콘텐츠를 가져오는 계약
    ) { uri: Uri? ->
        // 2. 결과 처리: URI가 있으면 ViewModel에 업데이트
        viewModel.updateProfileImageUri(uri)
    }

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
                newProfileImageUri = uiState.newProfileImageUri,
                isEditing = uiState.isEditing,
                onImageClick = {
                    imagePickerLauncher.launch("image/*")
                    // 선택 후 viewModel.updateProfileImageUri(selectedUri) 호출
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. 사용자 정보 입력/표시 영역
            UserInfoFields(
                uiState = uiState,
                onNameChange = viewModel::updateName,
                onRelationChange = viewModel::updateRelationship
            )
        }
    }
}

// ----------------------------------------------------------------------
// Compose Components
// ----------------------------------------------------------------------

// 상단 바 (TopBar)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileTopBar(
    navController: NavHostController,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "내 정보 수정하기",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        },
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
        },
        modifier = Modifier.padding(10.dp)
    )
}

// 프로필 이미지 영역
@Composable
fun ProfileImageArea(
    profileImageUrl: String?,
    newProfileImageUri: Uri?, // 로컬 URI를 추가로 받음
    isEditing: Boolean,
    onImageClick: () -> Unit
) {
    val imageSource = if (newProfileImageUri != null) newProfileImageUri else profileImageUrl

    Box(
        modifier = Modifier.size(200.dp),   // 프로필 크기 수정 1
        contentAlignment = Alignment.BottomEnd
    ) {
        // 프로필 이미지 표시
        AsyncImage(
            model = imageSource,
            contentDescription = "프로필 사진",
            modifier = Modifier
                .size(200.dp)   // 프로필 크기 수정 2
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,

        )

        // 편집 모드일 때만 이미지 변경 아이콘 표시
        if (isEditing) {
            Surface(
                onClick = onImageClick,
                modifier = Modifier
                    .size(40.dp) // 크기를 약간 키워 시인성 확보
                    .offset(x = (-4).dp, y = (-4).dp) // 위치를 오른쪽 아래로 옮겨 프로필 테두리에 걸치도록 조정
                    .clip(CircleShape)
                    // Surface에 그림자(Elevation) 추가하여 떠있는 느낌 부여
                    .shadow(4.dp, shape = CircleShape),
                color = YellowBox  // 아이콘 색

            ) {
                Icon(
                    Icons.Outlined.ModeEdit,
                    contentDescription = "사진 변경",
                    // Icon 색상을 흰색으로 지정하여 배경색과 대비
                    modifier = Modifier.padding(10.dp) // 아이콘 크기 조정
                )
            }
        }
    }
}

// 사용자 정보 필드 (닉네임, 관계 등)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoFields(
    uiState: ProfileUiState,
    onNameChange: (String) -> Unit,
    onRelationChange: (String) -> Unit // 관계 변경 핸들러
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
    Text(
        text = "반려동물과의 관계",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    val isFamilyMember = !uiState.currentFamilyId.isNullOrBlank()
    val isSelectable = uiState.isEditing && isFamilyMember

    var expanded by remember { mutableStateOf(false) }

    // 로딩 중에는 로딩 인디케이터 표시
    if (uiState.isRelationLoading){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // TextField 높이와 유사하게 설정
            contentAlignment = Alignment.CenterStart
        ) {
            CircularProgressIndicator(Modifier.size(24.dp))
        }
    } else {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (isSelectable) {
                    expanded = !expanded
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = uiState.relationship,
                onValueChange = {}, // 드롭다운이므로 직접 수정 불가
                readOnly = true,
                label = { Text("가족 내 관계") },
                trailingIcon = {
                    if (isSelectable) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    } else {
                        // 선택 불가 시 다른 아이콘 또는 빈 공간
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                enabled = isSelectable, // 편집 가능 여부
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.7f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    disabledTextColor = if (isFamilyMember) Color.Black else Color.Gray // 가족 소속 여부에 따른 텍스트 색상
                )
            )

            // 드롭다운 메뉴
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                uiState.relationshipOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onRelationChange(option) // 선택된 값 ViewModel에 업데이트
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }

    /*TextField(
        value = if (uiState.isRelationLoading) "로딩 중..." else uiState.relationship,
        onValueChange = onRelationChange,
        // 가족 ID가 있을 때만 편집 가능
        enabled = uiState.isEditing && !uiState.currentFamilyId.isNullOrBlank(),
        readOnly = !uiState.isEditing || uiState.currentFamilyId.isNullOrBlank(),
        trailingIcon = {
            if(uiState.isRelationLoading) {
                CircularProgressIndicator(
                    Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else if (uiState.isEditing && !uiState.currentFamilyId.isNullOrBlank()) {
                Icon(Icons.Default.Edit, contentDescription = "관계 선택")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )*/


}

// 재사용 가능한 프로필 필드 컴포넌트
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
        )
    }
}

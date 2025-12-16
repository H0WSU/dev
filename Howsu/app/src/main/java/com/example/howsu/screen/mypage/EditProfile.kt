package com.example.howsu.screen.mypage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ModeEdit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.howsu.screen.todo.ContentBlack
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
        containerColor = Color.White,
        topBar = {
            EditProfileTopBar(
                navController = navController,
                isEditing = uiState.isEditing,
                onEditClick = { viewModel.toggledEditMode(true) },
                onCancelClick = { viewModel.cancelEditing() }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
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
                },
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. 사용자 정보 입력/표시 영역
            UserInfoFields(
                uiState = uiState,
                onNameChange = viewModel::updateName,
                onRelationChange = viewModel::updateRelationship
            )

            Spacer(modifier = Modifier.height(32.dp))
            if (uiState.isEditing) {
                SaveBottomButton(
                    onSaveClick = { viewModel.saveProfile() }
                )
            }

        }

    }
}


// 상단 바 (TopBar)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileTopBar(
    navController: NavHostController,
    isEditing: Boolean,
    onEditClick: () -> Unit,
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
                IconButton(onClick = onCancelClick){
                    Icon(Icons.Default.Close, contentDescription = "취소")
                }
            } else {
                // 보기 모드일 때: 편집 버튼
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "편집")
                }
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ),
        modifier = Modifier.padding(10.dp)
    )
}

// 새로운 하단 저장 버튼 컴포넌트
@Composable
private fun SaveBottomButton(
    modifier: Modifier = Modifier, // ★ 1. modifier 파라미터 추가
    onSaveClick: () -> Unit,
) {
    Column(
        modifier = modifier // ★ 2. 전달받은 modifier 사용 (align(BottomCenter))
            .fillMaxWidth()
            // ★ 3. 배경을 투명하게 (Scaffold 배경이 보이도록)
            .background(Color.Transparent)
            // ★ 4. (수정) 패딩 변경 (상단 공백 16dp, 하단 공백 32dp)
            .padding(
                top = 16.dp,
                bottom = 16.dp
            )
    ) {
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowBox, // ★ 색상 적용 (노랑)
                contentColor = ContentBlack // ★ 색상 적용 (검정)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("저장하기", fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
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
    // 1. 다이얼로그 표시 상태 추가
    var showRelationDialog by remember { mutableStateOf(false) }

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
    Spacer(modifier = Modifier.height(16.dp))

    val isFamilyMember = !uiState.currentFamilyId.isNullOrBlank()
    val isSelectable = uiState.isEditing && isFamilyMember

    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .height(56.dp)
            .clip(RoundedCornerShape(25.dp))
            .clickable(enabled = isSelectable) {
                if (isSelectable) {
                    showRelationDialog = true
                }
            },
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.7f)),
        color = Color.Transparent, // 배경 투명
        shadowElevation = 0.dp // 그림자 없음
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // 내부 패딩
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // 텍스트와 아이콘을 양 끝에 배치
        ) {
            // 선택된 관계 텍스트
            Text(
                text = uiState.relationship,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                // isSelectable에 따라 텍스트 색상 조정
                color = if (isSelectable) Color.Black else Color.Gray,
                fontWeight = FontWeight.Normal
            )

            // 드롭다운 아이콘
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = if (isSelectable) Color.Black else Color.Gray // isSelectable에 따라 아이콘 색상 조정
            )
        }
    }


    // 3. RelationPickerDialog 호출 (로직 유지)
    if (showRelationDialog) {
        RelationBottomSheet(
            currentRelation = uiState.relationship,
            relations = uiState.relationshipOptions,
            onDismiss = { showRelationDialog = false },
            onRelationSelected = { selected ->
                onRelationChange(selected) // 선택된 값 ViewModel에 업데이트
                showRelationDialog = false
            }
        )
    }

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
                unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.7f),
                // 배경색을 투명하게 설정
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
        )
    }
}


// 재정의
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationBottomSheet(
    currentRelation: String,
    relations: List<String>, // RelationBottomSheet에는 이 파라미터가 없으나, 여기서는 사용
    onDismiss: () -> Unit,
    onRelationSelected: (String) -> Unit // onConfirm과 동일 역할
) {
    var selected by rememberSaveable {
        mutableStateOf(if (currentRelation.isNotBlank()) currentRelation else relations[0])
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "어떤 역할을 맡고 있나요?",
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            relations.forEach { rel ->
                val isSelected = rel == selected

                Text(
                    text = rel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { selected = rel },
                    textAlign = TextAlign.Center,
                    fontSize = if (isSelected) 22.sp else 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Black else Color(0xFFBDBDBD)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onRelationSelected(selected) // onRelationSelected 사용
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black, // 기존 RelationBottomSheet은 Black
                    contentColor = Color.White
                )
            ) {
                Text(text = "선택 완료")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
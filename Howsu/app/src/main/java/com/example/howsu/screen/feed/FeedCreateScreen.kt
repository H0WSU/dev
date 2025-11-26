package com.example.howsu.screen.feed

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.howsu.data.model.FeedPost
import com.example.howsu.screen.pet.component.ImageSourceBottomSheet
import com.example.howsu.screen.todo.ContentBlack
import com.example.howsu.screen.todo.YellowBox
import java.io.File

/* -------------------------------------------
   TopBar
   ------------------------------------------- */
@Composable
fun FeedWriteTopBar(
    title: String = "피드 공유하기",
    onCloseClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center),
            color = Color.Black
        )
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                modifier = Modifier.size(20.dp),
                tint = Color.Black
            )
        }
    }
}

/* -------------------------------------------
   BottomBar
   ------------------------------------------- */
@Composable
fun FeedWriteBottomBar(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    isEditMode: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowBox,
                contentColor = ContentBlack
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isEditMode) "수정 완료" else "업로드하기",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}



/* -------------------------------------------
   Screen 본문
   ------------------------------------------- */
@Composable
fun FeedWriteScreen(
    viewModel: FeedViewModel,
    onFinishWrite: () -> Unit,
    editPost: FeedPost? = null
) {
    val titleMax = 30
    val contentMax = 300

    var title by remember(editPost) { mutableStateOf(editPost?.title ?: "") }
    var content by remember(editPost) { mutableStateOf(editPost?.content ?: "") }
    var hashtagInput by remember(editPost) {
        mutableStateOf(editPost?.hashtags?.joinToString(" ") { "#$it" } ?: "")
    }

    val imageUris = remember(editPost) {
        mutableStateListOf<String>().apply {
            if (editPost != null) addAll(editPost.imageUris)
        }
    }
    val videoUris = remember(editPost) {
        mutableStateListOf<String>().apply {
            if (editPost != null) addAll(editPost.videoUris)
        }
    }
    val hashtags = remember(editPost) {
        mutableStateListOf<String>().apply {
            if (editPost != null) addAll(editPost.hashtags)
        }
    }

    val context = LocalContext.current

    val mediaChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult

        val data = result.data ?: return@rememberLauncherForActivityResult

        // 최대 5개 제한 예시
        val currentCount = imageUris.size + videoUris.size
        var remain = 5 - currentCount
        if (remain <= 0) return@rememberLauncherForActivityResult

        fun handleUri(uri: Uri) {
            if (remain <= 0) return
            val type = context.contentResolver.getType(uri) ?: ""
            val s = uri.toString()
            when {
                type.startsWith("image/") -> {
                    imageUris.add(s)
                    remain--
                }
                type.startsWith("video/") -> {
                    videoUris.add(s)
                    remain--
                }
            }
        }

        // 여러 장 선택된 경우 (clipData)
        val clip = data.clipData
        if (clip != null) {
            for (i in 0 until clip.itemCount) {
                val uri = clip.getItemAt(i).uri
                handleUri(uri)
                if (remain <= 0) break
            }
        } else {
            // 한 장만 선택된 경우
            data.data?.let { handleUri(it) }
        }
    }

    // 바텀시트 On/Off
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // 카메라 촬영용 임시 Uri
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }


    // 카메라 사진 촬영 런처
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            val currentCount = imageUris.size + videoUris.size
            if (currentCount < 5) {
                imageUris.add(cameraImageUri.toString())
            }
        }
    }

    // 실제 카메라 실행 함수
    fun startCamera() {
        val uri = createFeedImageUri(context)
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
            // 필요하면 스낵바/다이얼로그로 "카메라 권한이 필요해요" 안내 가능
        }
    }

    val canUpload = title.isNotBlank() && content.isNotBlank()
    val isEditMode = editPost != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단바
        FeedWriteTopBar(onCloseClick = onFinishWrite)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // 제목
            Text("제목", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= titleMax) title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("제목을 입력해 주세요", fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Text(
                text = "${title.length}/$titleMax",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End,
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(20.dp))

            // 내용
            Text("내용", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { if (it.length <= contentMax) content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = { Text("내용을 입력해 주세요", fontSize = 14.sp) },
                shape = RoundedCornerShape(14.dp)
            )
            Text(
                text = "${content.length}/$contentMax",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End,
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(24.dp))

            // 사진/동영상 추가
            val mediaCount = imageUris.size + videoUris.size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("사진/동영상 추가", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("$mediaCount/5", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))

            // + 박스 → 바텀시트 열기
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .border(
                        BorderStroke(1.dp, Color(0xFFE5E5E5)),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        showImageSourceDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 28.sp, color = Color.Gray)
            }

            // 선택된 사진/동영상 썸네일
            SelectedMediaRow(imageUris = imageUris, videoUris = videoUris)

            Spacer(Modifier.height(24.dp))

            // 해시태그
            Text("해시태그", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = hashtagInput,
                onValueChange = { newText ->
                    hashtagInput = newText
                    val cleaned = newText
                        .replace("#", " ")
                        .trim()
                        .split(" ")
                        .filter { it.isNotBlank() }

                    hashtags.clear()
                    hashtags.addAll(cleaned)
                },
                placeholder = { Text("예) #일상 #산책", fontSize = 14.sp, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(24.dp))
        }

        // 업로드 버튼
        FeedWriteBottomBar(
            enabled = canUpload,
            isEditMode = isEditMode
        ) {
            if (editPost == null) {
                viewModel.addPost(
                    title = title,
                    content = content,
                    imageUris = imageUris.toList(),
                    videoUris = videoUris.toList(),
                    hashtags = hashtags.toList()
                )
            } else {
                viewModel.updatePost(
                    id = editPost.id,
                    title = title,
                    content = content,
                    imageUris = imageUris.toList(),
                    videoUris = videoUris.toList(),
                    hashtags = hashtags.toList()
                )
            }
            onFinishWrite()
        }

        // 🔻 여기: 바텀시트 (디자인은 네가 쓰고 있는 ImageSourceBottomSheet 그대로)
        if (showImageSourceDialog) {
            ImageSourceBottomSheet(
                onDismiss = { showImageSourceDialog = false },
                onPickGallery = {
                    showImageSourceDialog = false

                    // 1) 이미지 + 동영상 모두 허용, 여러 개 선택 요청
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                        putExtra(
                            Intent.EXTRA_MIME_TYPES,
                            arrayOf("image/*", "video/*")
                        )
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }

                    // 2) "사용할 애플리케이션" 선택창 강제
                    val chooser = Intent.createChooser(intent, "사진/동영상 선택")

                    // 3) 런처 실행
                    mediaChooserLauncher.launch(chooser)
                },
                onTakePhoto = {
                    showImageSourceDialog = false
                    // 여기에는 기존 카메라 권한 + 촬영 로직(startCamera / permissionLauncher) 그대로 사용
                }
            )
        }
    }
}

/** 피드용 카메라 사진 임시 Uri 생성 */
private fun createFeedImageUri(context: Context): Uri {
    val imageFile = File(
        context.cacheDir,
        "feed_${System.currentTimeMillis()}.jpg"
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@Composable
fun SelectedMediaRow(
    imageUris: MutableList<String>,
    videoUris: MutableList<String>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(top = 8.dp)
    ) {
        // 이미지 썸네일
        imageUris.forEach { uri ->
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(72.dp)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "선택한 사진",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(8.dp))
                )
                IconButton(
                    onClick = { imageUris.remove(uri) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "삭제"
                    )
                }
            }
        }

        // 동영상 썸네일 (현재는 이미지처럼 표시, 필요하면 재생 아이콘 추가 가능)
        videoUris.forEach { uri ->
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "동영상",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(8.dp))
                )
                IconButton(
                    onClick = { videoUris.remove(uri) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "삭제"
                    )
                }
            }
        }
    }
}

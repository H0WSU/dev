package com.example.howsu.screen.feed

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.example.howsu.ui.theme.HowsuTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    // 갤러리/파일 앱 선택 런처
    val mediaChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult

        val data = result.data ?: return@rememberLauncherForActivityResult

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

        val clip = data.clipData
        if (clip != null) {
            for (i in 0 until clip.itemCount) {
                handleUri(clip.getItemAt(i).uri)
                if (remain <= 0) break
            }
        } else {
            data.data?.let { handleUri(it) }
        }
    }

    // 바텀시트 On/Off
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // 카메라 촬영용 임시 Uri
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // 사진 촬영 런처
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

    fun startCamera() {
        val uri = createFeedImageUri(context)
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    // 동영상 촬영 런처
    val videoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult

        val currentCount = imageUris.size + videoUris.size
        if (currentCount < 5) {
            videoUris.add(uri.toString())
        }
    }

    fun startVideoCapture() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        videoCaptureLauncher.launch(intent)
    }

    // 권한 런처
    val photoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
    }

    val videoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVideoCapture()
    }

    val canUpload = title.isNotBlank() && content.isNotBlank()
    val isEditMode = editPost != null

    var showExitConfirm by remember { mutableStateOf(false) }

    val hasDraft = remember(
        title, content, hashtagInput,
        imageUris.size, videoUris.size, hashtags.size,
        editPost
    ) {
        // 수정 모드/작성 모드 둘 다 커버
        val baseTitle = editPost?.title ?: ""
        val baseContent = editPost?.content ?: ""
        val baseHashtags = editPost?.hashtags ?: emptyList()
        val baseImages = editPost?.imageUris ?: emptyList()
        val baseVideos = editPost?.videoUris ?: emptyList()

        val titleChanged = title != baseTitle
        val contentChanged = content != baseContent

        // hashtagInput은 이제 "입력 중 텍스트"라서, 칩(hashtags) 기준으로 비교하는 게 더 정확합니다.
        val hashtagsChanged = hashtags.toList() != baseHashtags
        val imagesChanged = imageUris.toList() != baseImages
        val videosChanged = videoUris.toList() != baseVideos

        val hasAnyText = title.isNotBlank() || content.isNotBlank() || hashtagInput.isNotBlank()
        val hasAnyMedia = imageUris.isNotEmpty() || videoUris.isNotEmpty()
        val hasAnyTags = hashtags.isNotEmpty()

        // 작성 모드: 뭔가 하나라도 있으면 true
        if (editPost == null) {
            hasAnyText || hasAnyMedia || hasAnyTags
        } else {
            // 수정 모드: 기존과 비교해서 변경이 있으면 true
            titleChanged || contentChanged || hashtagsChanged || imagesChanged || videosChanged || hashtagInput.isNotBlank()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            FeedWriteTopBar(
                onCloseClick = {
                    if (hasDraft) showExitConfirm = true
                    else onFinishWrite()
                }
            )
        }

    ) {
        innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 스크롤 가능한 본문 (하단 버튼 높이만큼 패딩 추가)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 120.dp)
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
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFDF37),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
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
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFDF37),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
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

                // + 카드(72dp)와 썸네일(72dp)을 동일 규격으로 가로 스크롤 영역에 배치합니다.
                SelectedMediaRow(
                    imageUris = imageUris,
                    videoUris = videoUris,
                    enabledAdd = mediaCount < 5,
                    onAddClick = { showImageSourceDialog = true }
                )

                Spacer(Modifier.height(24.dp))

                // 해시태그
                Text("해시태그", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))

                HashtagInput(
                    hashtags = hashtags,                  // 기존 mutableStateListOf<String>()
                    input = hashtagInput,                 // 기존 String state
                    onInputChange = { hashtagInput = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                HashtagChipsRow(
                    hashtags = hashtags,
                    onRemove = { tag -> hashtags.remove(tag) }
                )

                Spacer(Modifier.height(24.dp))
            }

            // 하단 버튼
            FeedWriteBottomBar(
                enabled = canUpload,
                isEditMode = isEditMode,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                // ★ 수정됨: onFinishWrite를 뷰모델 함수의 인자로 넘겨서,
                // 뷰모델이 작업을 "끝낸 후"에 호출하도록 해야 안전합니다.

                // 만약 ViewModel의 addPost가 비동기 함수(suspend)가 아니고
                // 콜백도 지원하지 않는다면, 아래 코드가 실행되는 순간 화면이 닫히면서
                // 이미지 업로드 코루틴이 취소되거나 Context가 소실되어 앱이 죽습니다.

                // [해결책] FeedViewModel의 addPost/updatePost 함수에
                // onComplete: () -> Unit 파라미터를 추가하고, 작업 끝난 뒤 호출하게 수정하세요.

                if (editPost == null) {
                    viewModel.addPost(
                        title = title,
                        content = content,
                        imageUris = imageUris.toList(),
                        videoUris = videoUris.toList(),
                        hashtags = hashtags.toList(),
                        onComplete = onFinishWrite // ★ 이렇게 전달해야 함
                    )
                } else {
                    viewModel.updatePost(
                        id = editPost.id,
                        title = title,
                        content = content,
                        imageUris = imageUris.toList(),
                        videoUris = videoUris.toList(),
                        hashtags = hashtags.toList(),
                        onComplete = onFinishWrite // ★ 이렇게 전달해야 함
                    )
                }
                // onFinishWrite()  <-- ★ 이 줄을 삭제하세요! (여기 있으면 바로 닫혀서 에러남)
            }
            // 이미지 소스 선택 바텀시트
            if (showImageSourceDialog) {
                ImageSourceBottomSheet(
                    onDismiss = { showImageSourceDialog = false },
                    onPickGallery = {
                        showImageSourceDialog = false
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "*/*"
                            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                        mediaChooserLauncher.launch(intent)
                    },
                    onTakePhoto = {
                        showImageSourceDialog = false
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (granted) {
                            startCamera()
                        } else {
                            photoPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onTakeVideo = {
                        showImageSourceDialog = false
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (granted) {
                            startVideoCapture()
                        } else {
                            videoPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
            }
        }
    }

    if (showExitConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("작성 중인 내용이 있어요") },
            text = { Text("나가면 작성 중인 내용이 삭제됩니다. \n정말 나가시겠어요?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirm = false
                    onFinishWrite()
                }) {
                    Text("나가기", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text("취소")
                }
            }
        )
    }

}

@Composable
private fun HashtagInput(
    hashtags: MutableList<String>,
    input: String,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    fun commitTag(raw: String) {
        val cleaned = raw.trim()
            .removePrefix("#")
            .replace(" ", "")
        if (cleaned.isBlank()) return

        // 중복 방지
        if (hashtags.contains(cleaned)) return

        hashtags.add(cleaned)
    }

    OutlinedTextField(
        value = input,
        onValueChange = { newText ->
            // 엔터/스페이스로 태그 확정 처리
            if (newText.contains("\n") || newText.contains(" ")) {
                val parts = newText
                    .replace("\n", " ")
                    .split(" ")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                // 마지막 조각은 아직 입력 중일 수 있으니: 보통 "끝이 공백"일 때만 전부 커밋
                val endsWithSeparator = newText.endsWith(" ") || newText.endsWith("\n")

                if (parts.isNotEmpty()) {
                    val commitCount = if (endsWithSeparator) parts.size else (parts.size - 1).coerceAtLeast(0)
                    for (i in 0 until commitCount) commitTag(parts[i])
                }

                onInputChange(if (endsWithSeparator) "" else parts.lastOrNull().orEmpty())
            } else {
                onInputChange(newText)
            }
        },
        placeholder = { Text("예) #일상 입력 후 엔터", fontSize = 14.sp, color = Color.Gray) },
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                // 키보드 Done으로도 태그 확정
                commitTag(input)
                onInputChange("")
            }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFFDF37),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            cursorColor = Color.Black,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

@Composable
private fun HashtagChipsRow(
    hashtags: List<String>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (hashtags.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        hashtags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFFF5F5F5),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    IconButton(
                        onClick = { onRemove(tag) },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "삭제",
                            modifier = Modifier.size(14.dp),
                            tint = Color.DarkGray
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "#$tag",
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

/* 피드용 카메라 사진 임시 Uri 생성 */
private fun createFeedImageUri(context: Context): Uri {
    val imageFile = File(context.cacheDir, "feed_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

/* 썸네일 줄 + 동일 크기(72dp) 추가 카드 */
@Composable
fun SelectedMediaRow(
    imageUris: MutableList<String>,
    videoUris: MutableList<String>,
    enabledAdd: Boolean,
    onAddClick: () -> Unit
) {
    val borderColor = Color(0xFFE5E5E5)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // + 카드 (썸네일과 동일 크기)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
                .clickable(enabled = enabledAdd) { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 20.sp, color = Color.Gray)
        }

        Spacer(Modifier.width(8.dp))

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
                        contentDescription = "삭제",
                        tint = Color.White
                    )
                }
            }
        }

        // 동영상 썸네일
        videoUris.forEach { uri ->
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // 1. 동영상 썸네일
                val thumb = rememberVideoThumbnail(uri)

                if (thumb != null) {
                    Image(
                        bitmap = thumb.asImageBitmap(),
                        contentDescription = "동영상 썸네일",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.DarkGray)
                    )
                }

                // 2. ▶ 재생 아이콘 (썸네일 위 오버레이)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "동영상",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 3. 삭제 버튼
                IconButton(
                    onClick = { videoUris.remove(uri) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "삭제",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun rememberVideoThumbnail(uriString: String): Bitmap? {
    val context = LocalContext.current
    var bitmap by remember(uriString) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uriString) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, Uri.parse(uriString))
                val frame = retriever.getFrameAtTime(
                    0,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                retriever.release()
                frame
            }.getOrNull()
        }
    }
    return bitmap
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun FeedWriteScreenPreview() {
    HowsuTheme {
        val titleMax = 30
        val contentMax = 300

        var title by remember { mutableStateOf("자몽이 산책 일기") }
        var content by remember {
            mutableStateOf(
                "오늘 자몽이랑 동네 한 바퀴 산책하고 왔어요.\n" +
                        "날씨가 좋아서 그런지 엄청 신나게 뛰어다녔어요!"
            )
        }
        var hashtagInput by remember { mutableStateOf("#일상 #산책") }

        val imageUris = remember { mutableStateListOf<String>() }
        val videoUris = remember { mutableStateListOf<String>() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            FeedWriteTopBar(onCloseClick = {})

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
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

                SelectedMediaRow(
                    imageUris = imageUris,
                    videoUris = videoUris,
                    enabledAdd = true,
                    onAddClick = {}
                )

                Spacer(Modifier.height(24.dp))

                Text("해시태그", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = hashtagInput,
                    onValueChange = { hashtagInput = it },
                    placeholder = { Text("예) #일상 #산책", fontSize = 14.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(24.dp))
            }

            FeedWriteBottomBar(
                enabled = title.isNotBlank() && content.isNotBlank(),
                isEditMode = false,
                onClick = {}
            )
        }
    }


}

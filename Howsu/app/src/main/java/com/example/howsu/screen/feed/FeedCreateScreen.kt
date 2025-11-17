package com.example.howsu.screen.feed

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.howsu.data.model.FeedPost
import kotlin.contracts.contract

@Composable
private fun FeedWriteTopBar(onCloseClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        Text(
            text = "피드 작성하기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterEnd)
                .border(
                    BorderStroke(0.1.dp, Color.LightGray),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * editPost == null  → 새 글 작성
 * editPost != null → 수정 모드
 */
@Composable
fun FeedWriteScreen(
    viewModel: FeedViewModel,
    onFinishWrite: () -> Unit,
    editPost: FeedPost? = null
) {
    // 제목/내용 초기값
    var title by remember(editPost) { mutableStateOf(editPost?.title ?: "") }
    var content by remember(editPost) { mutableStateOf(editPost?.content ?: "") }

    // 해시태그 입력창 초기값 (#일상 #산책)
    var hashtagInput by remember(editPost) {
        mutableStateOf(editPost?.hashtags?.joinToString(" ") { "#$it" } ?: "")
    }

    // 첨부 리스트
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

   val mediaPickerLauncher = rememberLauncherForActivityResult(
       contract = ActivityResultContracts.OpenMultipleDocuments()
   ) { uris ->
       if(uris != null){
           // 현재 몇 개 선택되어 있는지
           val currentCount = imageUris.size + videoUris.size
           val remain = 5 - currentCount

           if (remain <= 0) {
               // 이미 5개 선택된 상태면 더 안 받음
               return@rememberLauncherForActivityResult
           }

           // 이번에 새로 고른 것들 중에서 남은 개수까지만 추가
           uris.take(remain).forEach { uri ->
               val type = context.contentResolver.getType(uri) ?: ""
               val uriString = uri.toString()

               when {
                   type.startsWith("image/") -> imageUris.add(uriString)
                   type.startsWith("video/") -> videoUris.add(uriString)
               }
           }
       }
   }

    // 업로드 가능 조건
    val canUpload = title.isNotBlank() && content.isNotBlank()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 바 (고정)
        FeedWriteTopBar(onCloseClick = onFinishWrite)

        // 스크롤되는 내용 영역
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("제목")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목을 입력해 주세요") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("내용")
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용을 입력해 주세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text("사진/동영상 추가 (${imageUris.size + videoUris.size}/5)")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable {
                        mediaPickerLauncher.launch(arrayOf("image/*", "video/*"))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("+")
            }

            // 선택된 사진/동영상 썸네일 줄
            SelectedMediaRow(
                imageUris = imageUris,
                videoUris = videoUris
            )

            Spacer(Modifier.height(16.dp))

            Text("해시태그")
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
                label = { Text("예) #일상 #산책") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }

            Button(
                onClick = {
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                enabled = canUpload
            ) {
                Text(if (editPost == null) "업로드하기" else "수정 완료")
            }
        }
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
        // 이미지 썸네일들
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
                        contentDescription = "삭제",
                    )
                }
            }
        }
    }
}


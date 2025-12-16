package com.example.howsu.screen.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.howsu.R
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.FeedPost

@Composable
fun FeedItem(
    post: FeedPost,
    isLiked: Boolean,                // ← ViewModel에서 내려주는 값만 사용
    onClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onToggleLike: () -> Unit = {}
) {
    val dateText = remember(post.createdAt) {
        java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.KOREAN)
            .format(java.util.Date(post.createdAt))
    }

    val heartIcon =
        if (isLiked) R.drawable.yellow_heart else R.drawable.empty_heart

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            // 작성자 영역
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!post.authorProfileImage.isNullOrBlank()) {
                    AsyncImage(
                        model = post.authorProfileImage,
                        contentDescription = "프로필",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = post.authorName.ifBlank { "익명" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                    Text(
                        text = dateText,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 해시태그
            if (post.hashtags.isNotEmpty()) {
                Text(
                    text = post.hashtags.joinToString(" ") { "#$it" },
                    fontSize = 12.sp,
                    color = Color(0xFF3F51B5)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 제목
            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 내용
            Text(
                text = post.content,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 미디어
            if (post.imageUris.isNotEmpty() || post.videoUris.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    post.imageUris.forEach { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    post.videoUris.forEach { uri ->
                        VideoThumbnailWithPlayIcon(
                            uriString = uri,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(90.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // 좋아요 / 댓글
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleLike() }
                ) {
                    Icon(
                        painter = painterResource(id = heartIcon),
                        contentDescription = "좋아요",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likeCount.toString(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.comment),
                        contentDescription = "댓글",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.commentCount.toString(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = onClick) {
                    Text("수정", fontSize = 12.sp)
                }
                TextButton(onClick = onDeleteClick) {
                    Text("삭제", fontSize = 12.sp, color = Color.Red)
                }
            }
        }
    }
}


@Composable
fun VideoThumbnailWithPlayIcon(
    uriString: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        val thumb = rememberVideoThumbnail(uriString)

        if (thumb != null) {
            Image(
                bitmap = thumb.asImageBitmap(),
                contentDescription = "동영상 썸네일",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        // 재생 아이콘 오버레이
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "동영상",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


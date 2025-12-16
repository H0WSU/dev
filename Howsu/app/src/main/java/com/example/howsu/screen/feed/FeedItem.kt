package com.example.howsu.screen.feed

import android.R.attr.onClick
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.howsu.R
import com.example.howsu.data.model.FeedPost
import com.example.howsu.screen.todo.ContentBlack

@Composable
fun FeedItem(
    post: FeedPost,
    isLiked: Boolean,
    onPostClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onToggleLike: () -> Unit = {}
) {
    val dateText = remember(post.createdAt) {
        java.text.SimpleDateFormat("yy/MM/dd", java.util.Locale.KOREAN)
            .format(java.util.Date(post.createdAt))
    }

    val heartIcon = if (isLiked) R.drawable.yellow_heart else R.drawable.empty_heart
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onPostClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // 1. 상단: 해시태그 (메뉴 제거)
            if (post.hashtags.isNotEmpty()) {
                Text(
                    text = post.hashtags.joinToString(" ") { "#$it" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2962FF),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // 2. 제목 + 메뉴 (Row로 배치)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f) // 제목이 길면 줄바꿈, 메뉴 밀어내지 않음
                )

                Spacer(modifier = Modifier.width(8.dp))

                // ▼▼▼ 메뉴 위치 이동 (제목 옆) ▼▼▼
                Box {
                    IconButton(
                        onClick = { isMenuExpanded = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "메뉴",
                            tint = ContentBlack
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        modifier = Modifier.background(Color.Black)
                    ) {
                        DropdownMenuItem(
                            text = { Text("수정") },
                            onClick = {
                                isMenuExpanded = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제", color = Color.Red) },
                            onClick = {
                                isMenuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. 작성자 프로필
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!post.authorProfileImage.isNullOrBlank()) {
                    AsyncImage(
                        model = post.authorProfileImage,
                        contentDescription = "프로필",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = post.authorName.ifBlank { "익명" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ContentBlack
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. 내용
            Text(
                text = post.content,
                fontSize = 15.sp,
                color = Color.Black,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5) 미디어(썸네일)
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

            // 좋아요 / 댓글 (아래는 그대로)
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

                Text(
                    text = dateText,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
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


package com.example.howsu.screen.feed

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.howsu.data.model.FeedPost

@Composable
fun FeedItem(
    post: FeedPost,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
            colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 해시태그
            if (post.hashtags.isNotEmpty()) {
                Text(
                    text = post.hashtags.joinToString(" ") { "#$it" },
                    fontSize = 12.sp,
                    color = Color.Blue
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 제목
            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 내용
            Text(
                text = post.content,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 썸네일 영역
            if (post.imageUris.isNotEmpty() || post.videoUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    // 사진
                    post.imageUris.forEach { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .width(90.dp)
                                .height(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    // 동영상 (Coil이 자동으로 첫 프레임을 썸네일로 사용)
                    post.videoUris.forEach { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "동영상",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .width(90.dp)
                                .height(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 좋아요 / 댓글 카운트 + 수정/삭제 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "♥ ${post.likeCount}  💬 ${post.commentCount}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

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

@Preview(showBackground = true)
@Composable
fun FeedItemPreview() {

    val samplePost = FeedPost(
        authorId = "user123",
        authorName = "홍길동",
        authorProfileImage = "https://picsum.photos/50/50",
        title = "오늘 강아지랑 산책 다녀왔어요!",
        content = "날씨가 좋아서 공원에서 한참 놀다 왔어요. 강아지가 너무 신나 해서 보기만 해도 기분이 좋아지더라고요.",
        imageUris = listOf(
            "https://picsum.photos/200/200",
            "https://picsum.photos/200/300"
        ),
        videoUris = emptyList(),
        hashtags = listOf("산책", "강아지", "행복"),
        likeCount = 12,
        commentCount = 3,
        createdAt = System.currentTimeMillis()
    )

    FeedItem(
        post = samplePost,
        onClick = {},
        onDeleteClick = {}
    )
}

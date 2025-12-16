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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.howsu.R
import com.example.howsu.data.model.FeedPost

@Composable
fun FeedItem(
    post: FeedPost,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    // createdAt( Long ) → "MM/dd HH:mm" 형식으로 변환
    val dateText = remember(post.createdAt) {
        java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.KOREAN)
            .format(java.util.Date(post.createdAt))
    }

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
            // 🔹 프로필 + 닉네임 + 날짜 (상단 영역)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!post.authorProfileImage.isNullOrBlank()) {
                    AsyncImage(
                        model = post.authorProfileImage,
                        contentDescription = "프로필",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White        // 배경이 어두운 카드라면 흰색/연한색으로
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 해시태그 (원하면 제목/내용 아래 쪽에 배치)
            if (post.hashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = post.hashtags.joinToString(" ") { "#$it" },
                    fontSize = 12.sp,
                    color = Color(0xFF3F51B5)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 제목
            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 🔹 내용
            Text(
                text = post.content,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 썸네일 영역
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
                                .width(90.dp)
                                .height(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
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
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좋아요 아이콘 + 숫자
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.yellow_heart),
                        contentDescription = "좋아요",
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    if (post.likeCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.likeCount}",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 댓글 아이콘 + 숫자
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.comment),
                        contentDescription = "댓글",
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    if (post.likeCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.likeCount}",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                // 오른쪽 정렬
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


@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun FeedItemPreview() {
    val samplePost = FeedPost(
        id = 1L,
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

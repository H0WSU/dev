package com.example.howsu.screen.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.data.model.Comment
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.FeedPost
import com.example.howsu.common.FeedHomeTopBar
import com.example.howsu.ui.theme.HowsuTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun FeedDetailScreen(
    navController: NavHostController,
    viewModel: FeedViewModel,
    postId: Long
) {
    val member by viewModel.currentMember.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val likedPostIds by viewModel.likedPostIds.collectAsState()
    val isLiked = likedPostIds.contains(postId)

    var commentText by remember { mutableStateOf("") }

    // 대댓글 달 대상
    var replyTarget by remember { mutableStateOf<Comment?>(null) }

    // 수정 중인 댓글
    var editingComment by remember { mutableStateOf<Comment?>(null) }
    var editText by remember { mutableStateOf("") }

    val post: FeedPost? = viewModel.posts.firstOrNull { it.id == postId }

    LaunchedEffect(postId) {
        viewModel.fetchComments(postId)
        viewModel.loadLikeStateForPost(postId)
    }

    if (post == null || member == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("게시글을 찾을 수 없습니다.") }
        return
    }

    // 수정 다이얼로그
    if (editingComment != null) {
        AlertDialog(
            onDismissRequest = { editingComment = null },
            title = { Text("댓글 수정") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = editingComment ?: return@TextButton
                    viewModel.updateComment(postId, target.id, editText.trim())
                    editingComment = null
                }) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingComment = null }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            FeedHomeTopBar(
                member = member!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        bottomBar = { MyBottomNavigationBar(navController = navController) }
    ) { padding ->

        FeedDetailContentBody(
            member = member!!,
            post = post,
            comments = comments,
            isLiked = isLiked,
            commentText = commentText,
            replyTarget = replyTarget,
            onCommentTextChange = { commentText = it },
            onClickLike = { viewModel.toggleLike(postId) },
            onSubmitComment = {
                if (commentText.isNotBlank()) {
                    viewModel.addComment(
                        postId = postId,
                        content = commentText.trim(),
                        parentCommentId = replyTarget?.id
                    )
                    commentText = ""
                    replyTarget = null
                }
            },
            onClickReply = { c -> replyTarget = c },
            onClickEdit = { c ->
                editingComment = c
                editText = c.content
            },
            onClickDelete = { c ->
                viewModel.deleteComment(postId, c.id)
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        )
    }
}

@Composable
fun FeedDetailContentBody(
    member: FamilyMember,
    post: FeedPost,
    comments: List<Comment>,
    isLiked: Boolean,
    commentText: String,
    replyTarget: Comment?,
    onCommentTextChange: (String) -> Unit,
    onClickLike: () -> Unit,
    onSubmitComment: () -> Unit,
    onClickReply: (Comment) -> Unit,
    onClickEdit: (Comment) -> Unit,
    onClickDelete: (Comment) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {

        PostCard(
            post = post,
            isLiked = isLiked,
            onClickLike = onClickLike
        )

        Text(
            text = "댓글",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        // parentCommentId == null 인 최상위 댓글만 뽑기
        val rootComments = comments.filter { it.parentCommentId == null }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            items(rootComments, key = { it.id }) { root ->
                CommentThread(
                    comment = root,
                    allComments = comments,
                    depth = 0,
                    onClickReply = onClickReply,
                    onClickEdit = onClickEdit,
                    onClickDelete = onClickDelete
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 입력 영역: 대댓글 대상 표시
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (replyTarget != null) {
                    Text(
                        text = "↪ ${replyTarget.userName} 님께 대댓글 작성 중",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("댓글을 입력해 주세요") },
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSubmitComment) {
                Text("등록")
            }
        }
    }
}


@Composable
private fun PostCard(
    post: FeedPost,
    isLiked: Boolean,
    onClickLike: () -> Unit
) {
    val dateFormat = remember {
        SimpleDateFormat("yy/MM/dd", Locale.getDefault())
    }
    val dateText = remember(post.createdAt) {
        dateFormat.format(Date(post.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            /* 해시태그 */
            if (post.hashtags.isNotEmpty()) {
                Text(
                    text = post.hashtags.joinToString(" ") { "#$it" },
                    color = Color(0xFF2196F3),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            /* 제목 */
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            /* 내용 */
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))


            /* 좋아요 / 댓글 / 날짜 / 좋아요 버튼 */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ♥ 좋아요 수
                Text(
                    text = if (isLiked) "♥ ${post.likeCount}" else "♡ ${post.likeCount}",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(12.dp))

                // 💬 댓글 수
                Text(
                    text = "💬 ${post.commentCount}",
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                // 작성일
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 좋아요 버튼
                TextButton(onClick = onClickLike) {
                    Text(
                        text = if (isLiked) "좋아요 취소" else "좋아요",
                        color = if (isLiked) Color.Red else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    onClickReply: () -> Unit,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit
) {
    val timeFormat = remember {
        SimpleDateFormat("yy/MM/dd  HH:mm", Locale.getDefault())
    }
    val timeText = remember(comment.createdAt) {
        timeFormat.format(Date(comment.createdAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 프로필
                if (!comment.userProfileImage.isNullOrBlank()) {
                    AsyncImage(
                        model = comment.userProfileImage,
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
                        text = comment.userName,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClickReply) {
                    Text("답글")
                }
                TextButton(onClick = onClickEdit) {
                    Text("수정")
                }
                TextButton(onClick = onClickDelete) {
                    Text("삭제", color = Color.Red)
                }
            }
        }
    }
}

@Composable
private fun CommentThread(
    comment: Comment,
    allComments: List<Comment>,
    depth: Int,
    onClickReply: (Comment) -> Unit,
    onClickEdit: (Comment) -> Unit,
    onClickDelete: (Comment) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp) // 깊이에 따라 들여쓰기
    ) {
        CommentItem(
            comment = comment,
            onClickReply = { onClickReply(comment) },
            onClickEdit = { onClickEdit(comment) },
            onClickDelete = { onClickDelete(comment) }
        )

        val children = allComments.filter { it.parentCommentId == comment.id }
        children.forEach { child ->
            Spacer(modifier = Modifier.height(4.dp))
            CommentThread(
                comment = child,
                allComments = allComments,
                depth = depth + 1,
                onClickReply = onClickReply,
                onClickEdit = onClickEdit,
                onClickDelete = onClickDelete
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun FeedDetailContentBodyPreview() {
    val dummyMember = FamilyMember(
        userId = "u1",
        familyId = "f1",
        nickName = "이구역의짱",
        relationship = "집사",
        profileImageUrl = null
    )

    val dummyPost = FeedPost(
        id = 1L,
        authorId = "u1",
        authorName = "이구역의짱",
        authorProfileImage = null,
        title = "요즘 자몽이가 밥을 잘 안 먹어",
        content = "날이 추워져서 그런지 감기 걸렸나?\n좀 지켜보다가 병원을 데려가야 할 것 같아...",
        hashtags = listOf("일상", "병원"),
        likeCount = 2,
        commentCount = 3,
        createdAt = System.currentTimeMillis()
    )

    // c1 = 최상위 댓글, c2 = c1의 대댓글, c3 = c2의 대댓글 (깊이 2)
    val c1 = Comment(
        id = "c1",
        postId = 1L,
        parentCommentId = null,
        userId = "u2",
        userName = "자몽아기야",
        content = "저번에 보니까 재채기 하는 거 같던데",
        createdAt = System.currentTimeMillis()
    )
    val c2 = Comment(
        id = "c2",
        postId = 1L,
        parentCommentId = "c1",
        userId = "u1",
        userName = "이구역의짱",
        content = "너가 다음주에 자몽이 데리고 병원 갔다 와줘",
        createdAt = System.currentTimeMillis()
    )
    val c3 = Comment(
        id = "c3",
        postId = 1L,
        parentCommentId = "c2",
        userId = "u2",
        userName = "자몽아기야",
        content = "ㅇㅋ",
        createdAt = System.currentTimeMillis()
    )

    val dummyComments = listOf(c1, c2, c3)

    HowsuTheme {
        FeedDetailContentBody(
            member = dummyMember,
            post = dummyPost,
            comments = dummyComments,
            isLiked = true,
            commentText = "",
            replyTarget = null,
            onCommentTextChange = {},
            onClickLike = {},
            onSubmitComment = {},
            onClickReply = {},
            onClickEdit = {},
            onClickDelete = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

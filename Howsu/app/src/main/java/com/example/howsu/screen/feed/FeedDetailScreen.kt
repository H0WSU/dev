package com.example.howsu.screen.feed

import android.R.attr.text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.FeedHomeTopBar
import com.example.howsu.data.model.Comment
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.FeedPost
import com.example.howsu.screen.todo.ContentBlack
import com.example.howsu.screen.todo.YellowBox
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
    val likedCommentIds by viewModel.likedCommentIds.collectAsState()

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
        viewModel.loadCommentLikeState(postId)
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
        containerColor = Color.White,
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
            likedCommentIds = likedCommentIds,
            onClickCommentLike = { c ->
                viewModel.toggleCommentLike(postId, c.id)
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
    likedCommentIds: Set<String>,
    onClickCommentLike: (Comment) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {

        PostCard(
            post = post,
            isLiked = isLiked,
            onClickLike = onClickLike
        )

        Text(
            text = "댓글 ${post.commentCount}",
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
                    likedCommentIds = likedCommentIds,
                    onClickReply = onClickReply,
                    onClickEdit = onClickEdit,
                    onClickDelete = onClickDelete,
                    onClickCommentLike = onClickCommentLike
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 45.dp),
                    placeholder = {
                        Text(
                            text = "댓글을 입력해 주세요",
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSubmitComment,
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowBox,
                    contentColor = ContentBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("등록", fontWeight = FontWeight.Medium, fontSize = 15.sp)
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

            if (post.hashtags.isNotEmpty()) {
                Text(
                    text = post.hashtags.joinToString(" ") { "#$it" },
                    color = Color(0xFF2196F3),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLiked) "♥ ${post.likeCount}" else "♡ ${post.likeCount}",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onClickLike() }
                )
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "💬 ${post.commentCount}",
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    isLiked: Boolean,
    onClickLike: () -> Unit,
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

    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
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

                // ★ 가운데 빈 공간 → 오른쪽 끝으로 밀기
                Spacer(modifier = Modifier.weight(1f))

                // 오른쪽 상단 점 세 개
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더 보기"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("수정") },
                            onClick = {
                                menuExpanded = false
                                onClickEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제", color = Color.Red) },
                            onClick = {
                                menuExpanded = false
                                onClickDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLiked) "♥ ${comment.likeCount}" else "♡ ${comment.likeCount}",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onClickLike() }
                )

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = onClickReply) {
                    Text("답글")
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
    likedCommentIds: Set<String>,
    onClickReply: (Comment) -> Unit,
    onClickEdit: (Comment) -> Unit,
    onClickDelete: (Comment) -> Unit,
    onClickCommentLike: (Comment) -> Unit
) {
    // 현재 댓글 한 줄 렌더링
    val startPadding = if (depth == 0) 0.dp else 16.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding),
        verticalAlignment = Alignment.Top
    ) {
        // 대댓글이면 화살표 표시
        if (depth > 0) {
            Text(
                text = "↳",
                color = Color.DarkGray,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(end = 8.dp, top = 8.dp)
            )
        }

        // 댓글 카드
        CommentItem(
            comment = comment,
            isLiked = likedCommentIds.contains(comment.id),
            onClickLike = { onClickCommentLike(comment) },
            onClickReply = { onClickReply(comment) },
            onClickEdit = { onClickEdit(comment) },
            onClickDelete = { onClickDelete(comment) }
        )
    }

    // 자식 댓글들(대댓글들) 렌더링
    val children = allComments.filter { it.parentCommentId == comment.id }
    children.forEach { child ->
        Spacer(modifier = Modifier.height(6.dp))

        // ★ 깊이는 1로 고정해서 "대댓글"로만 표시
        CommentThread(
            comment = child,
            allComments = allComments,
            depth = 1,
            likedCommentIds = likedCommentIds,
            onClickReply = onClickReply,
            onClickEdit = onClickEdit,
            onClickDelete = onClickDelete,
            onClickCommentLike = onClickCommentLike
        )
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
            likedCommentIds = setOf("c1"),
            onClickCommentLike = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

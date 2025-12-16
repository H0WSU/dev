package com.example.howsu.screen.feed

import com.example.howsu.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// -----------------------------------------------------------------------------
// DETAIL SCREEN
// -----------------------------------------------------------------------------

@Composable
fun FeedDetailScreen(
    navController: NavHostController,
    viewModel: FeedViewModel,
    postId: Long,
) {
    val member by viewModel.currentMember.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val likedPostIds by viewModel.likedPostIds.collectAsState()
    val likedCommentIds by viewModel.likedCommentIds.collectAsState()

    var commentText by remember { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<Comment?>(null) }

    var editingComment by remember { mutableStateOf<Comment?>(null) }
    var editText by remember { mutableStateOf("") }

    val post = viewModel.posts.firstOrNull { it.id == postId }

    LaunchedEffect(postId) {
        viewModel.fetchComments(postId)
        viewModel.loadLikeStateForPost(postId)
        viewModel.loadCommentLikeState(postId)
    }

    if (post == null || member == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("게시글을 찾을 수 없습니다.")
        }
        return
    }

    val isLiked = likedPostIds.contains(post.id)

    // ---- 댓글 수정창 ----
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
                }) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { editingComment = null }) { Text("취소") }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            FeedHomeTopBar(
                member = member!!,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 40.dp)
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
            onClickLike = { viewModel.toggleLike(post.id) },
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
            onClickReply = { replyTarget = it },
            onClickEdit = { c ->
                editingComment = c
                editText = c.content
            },
            onClickDelete = { c -> viewModel.deleteComment(postId, c.id) },
            likedCommentIds = likedCommentIds,
            onClickCommentLike = { c -> viewModel.toggleCommentLike(postId, c.id) },
            modifier = Modifier.padding(padding)
        )
    }
}

// -----------------------------------------------------------------------------
// 게시글 상단 콘텐츠
// -----------------------------------------------------------------------------

@Composable
private fun FeedDetailPostContent(
    post: FeedPost,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    member: FamilyMember,
    ) {
    val dateText = remember(post.createdAt) {
        SimpleDateFormat("MM/dd HH:mm", Locale.KOREAN).format(Date(post.createdAt))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            AsyncImage(
                model = member.profileImageUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(8.dp))

            Column {
                Text(post.authorName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(dateText, fontSize = 11.sp, color = Color.Gray)
            }
        }

        Spacer(Modifier.height(10.dp))

        if (post.hashtags.isNotEmpty()) {
            Text(
                text = post.hashtags.joinToString(" ") { "#$it" },
                color = Color(0xFF3F51B5),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(8.dp))
        }

        Text(post.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text(post.content, fontSize = 14.sp)

        if (post.imageUris.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                post.imageUris.forEach { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .fillMaxWidth()
                            .size(320.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }

        if (post.videoUris.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            post.videoUris.forEach { uri ->
                VideoPlayerBlock(uriString = uri)
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.clickable { onToggleLike() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = if (isLiked) R.drawable.yellow_heart else R.drawable.empty_heart
                Icon(painterResource(icon), null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("${post.likeCount}", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.width(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.comment), null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("${post.commentCount}", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.weight(1f))
            Text(dateText, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

// -----------------------------------------------------------------------------
// DETAIL BODY
// -----------------------------------------------------------------------------

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
    val rootComments = comments.filter { it.parentCommentId == null }

    Column(modifier = modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {

            item {
                FeedDetailPostContent(
                    post = post,
                    isLiked = isLiked,
                    member = member,
                    onToggleLike = onClickLike
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                Text("댓글 ${post.commentCount}", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
            }

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
                Spacer(Modifier.height(8.dp))
            }
        }

        CommentInputBar(
            replyTarget = replyTarget,
            commentText = commentText,
            onCommentTextChange = onCommentTextChange,
            onSubmit = onSubmitComment
        )
    }
}

// -----------------------------------------------------------------------------
// 입력창
// -----------------------------------------------------------------------------

@Composable
private fun CommentInputBar(
    replyTarget: Comment?,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
    ) {
        if (replyTarget != null) {
            Text(
                "↪ ${replyTarget.userName} 님께 대댓글 작성 중",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 45.dp),
                placeholder = { Text("댓글을 입력해 주세요", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFDF37),      // 포커스 시
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowBox,
                    contentColor = ContentBlack
                )
            ) { Text("등록") }
        }
    }
}

// -----------------------------------------------------------------------------
// 댓글 항목
// -----------------------------------------------------------------------------

@Composable
private fun CommentItem(
    comment: Comment,
    isLiked: Boolean,
    showReplyButton: Boolean,
    onClickLike: () -> Unit,
    onClickReply: () -> Unit,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit
) {
    val isDeleted = comment.deleted
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White )
    )
        {

        if (isDeleted) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                )
                Spacer(Modifier.width(10.dp))
                Text("삭제된 댓글입니다.", color = Color.Gray)
            }
            return@Card
        }

        val timeFormat = SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault())
        val timeText = timeFormat.format(Date(comment.createdAt))

        Column(modifier = Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                AsyncImage(
                    model = comment.userProfileImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                )

                Spacer(Modifier.width(8.dp))

                Text(comment.userName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                Spacer(Modifier.weight(1f))

                IconButton(onClick = onClickLike) {
                    val icon = if (isLiked) R.drawable.yellow_heart else R.drawable.empty_heart
                    Icon(painterResource(icon), null, modifier = Modifier.size(16.dp))
                }

                if (showReplyButton) {
                    IconButton(onClick = onClickReply) {
                        Icon(painterResource(R.drawable.comment), null, modifier = Modifier.size(16.dp))
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, null)
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

            Spacer(Modifier.height(6.dp))
            Text(comment.content)

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeText, fontSize = 11.sp, color = Color.Gray)

                if (comment.likeCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painterResource(R.drawable.yellow_heart),
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = Color(0xFFFFDF37)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${comment.likeCount}", fontSize = 11.sp)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 댓글 트리
// -----------------------------------------------------------------------------

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
    val isRoot = depth == 0
    val paddingStart = if (isRoot) 0.dp else 16.dp

    Column(modifier = Modifier.padding(start = paddingStart)) {

        Row(verticalAlignment = Alignment.Top) {
            if (!isRoot) ReplyArrow()

            CommentItem(
                comment = comment,
                isLiked = likedCommentIds.contains(comment.id),
                showReplyButton = isRoot,
                onClickLike = { onClickCommentLike(comment) },
                onClickReply = { onClickReply(comment) },
                onClickEdit = { onClickEdit(comment) },
                onClickDelete = { onClickDelete(comment) }
            )
        }

        val children = allComments.filter { it.parentCommentId == comment.id }

        children.forEach { child ->
            Spacer(Modifier.height(6.dp))
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
}

@Composable
private fun ReplyArrow() {
    Text(
        "↳",
        color = Color.DarkGray,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(end = 8.dp, top = 10.dp)
    )
}

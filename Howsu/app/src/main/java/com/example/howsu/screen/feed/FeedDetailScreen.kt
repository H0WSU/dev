package com.example.howsu.screen.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.howsu.R
import com.example.howsu.common.FeedHomeTopBar
import com.example.howsu.common.MyBottomNavigationBar
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

    val isLiked = viewModel.isPostLiked(post.id)

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
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 40.dp,
                    bottom = 10.dp
                )
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
            // [추가] 게시글 수정/삭제 핸들러
            onPostEditClick = {
                // 수정 화면으로 이동하는 코드를 넣으세요. 예: navController.navigate("feed_edit/$postId")
            },
            onPostDeleteClick = {
                viewModel.deletePost(postId)
                navController.popBackStack() // 삭제 후 뒤로가기
            },
            modifier = Modifier.padding(padding),
            navController = navController
        )
    }
}

// -----------------------------------------------------------------------------
// 게시글 상단 콘텐츠 (배치 수정됨)
// -----------------------------------------------------------------------------

@Composable
private fun FeedDetailPostContent(
    post: FeedPost,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateText = remember(post.createdAt) {
        SimpleDateFormat("yy/MM/dd", Locale.KOREAN).format(Date(post.createdAt))
    }
    val heartIcon = if (isLiked) R.drawable.yellow_heart else R.drawable.empty_heart
    var isMenuExpanded by remember { mutableStateOf(false) }

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
                    modifier = Modifier.background(Color.White)
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

        // 5. 미디어 (버벅임 방지 썸네일 적용)
        if (post.imageUris.isNotEmpty() || post.videoUris.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                post.imageUris.forEach { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(300.dp) // 상세 화면이라 크게
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                post.videoUris.forEach { uri ->
                    var play by remember(uri) { mutableStateOf(false) }

                    if (play) {
                        VideoPlayerBlock(
                            uriString = uri,
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(300.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(300.dp)
                                .clickable { play = true }
                        ) {
                             VideoThumbnailWithPlayIcon(
                                uriString = uri,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 6. 하단: 좋아요 / 댓글 / 날짜
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 좋아요
            Box(modifier = Modifier.clickable { onToggleLike() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = heartIcon),
                        contentDescription = "좋아요",
                        modifier = Modifier.size(14.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likeCount.toString(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 댓글 아이콘
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.comment),
                    contentDescription = "댓글",
                    modifier = Modifier.size(14.dp),
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

            // 날짜
            Text(
                text = dateText,
                fontSize = 12.sp,
                color = Color.Gray
            )
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
    onPostEditClick: () -> Unit, // [추가]
    onPostDeleteClick: () -> Unit, // [추가]
    modifier: Modifier = Modifier,
    navController: NavHostController

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
                    onToggleLike = onClickLike,
                    onEditClick = { navController.navigate("edit_feed/${post.id}") },
                    onDeleteClick = onPostDeleteClick
                )
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFEEEEEE))
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                Text("댓글 ${post.commentCount}", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp))
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
                placeholder = { Text("댓글을 입력해 주세요", fontSize = 14.sp) },
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
            ) { Text("등록", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) }
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        if (isDeleted) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(Modifier.width(8.dp))
                Text("삭제된 댓글입니다.", color = Color.Gray, fontSize = 13.sp)
            }
            return@Card
        }

        val timeFormat = SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault())
        val timeText = timeFormat.format(Date(comment.createdAt))

        Column(modifier = Modifier.padding(12.dp)) {

            // [상단] 프로필 | 이름 | (여백) | 좋아요 | 답글 | 메뉴
            Row(verticalAlignment = Alignment.CenterVertically) {

                // 1. 프로필 (24dp 유지)
                AsyncImage(
                    model = comment.userProfileImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.width(8.dp))

                Text(comment.userName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                Spacer(Modifier.weight(1f))

                // 2. 좋아요 버튼 (아이콘 키움, 버튼 간격 넓힘)
                IconButton(
                    onClick = onClickLike,
                    modifier = Modifier.size(32.dp) // 버튼 크기를 키워 간격 확보
                ) {
                    val icon = if (isLiked) R.drawable.yellow_heart else R.drawable.empty_heart
                    Icon(
                        painterResource(icon),
                        contentDescription = "좋아요",
                        modifier = Modifier.size(15.dp), // 아이콘 사이즈 업 (14 -> 18)
                        tint = Color.Unspecified
                    )
                }

                // 3. 답글 버튼
                if (showReplyButton) {
                    IconButton(
                        onClick = onClickReply,
                        modifier = Modifier.size(32.dp) // 버튼 크기를 키워 간격 확보
                    ) {
                        Icon(
                            painterResource(R.drawable.comment),
                            contentDescription = "답글",
                            modifier = Modifier.size(15.dp), // 아이콘 사이즈 업 (14 -> 18)
                            tint = ContentBlack
                        )
                    }
                }

                // 4. 더보기 메뉴 (점 세 개는 작게 유지하되, 정렬을 위해 버튼 크기는 맞춤)
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp) // 이건 너무 넓으면 보기 싫으니 약간 작게 유지
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "메뉴",
                            modifier = Modifier.size(16.dp), // 점 사이즈는 작게 유지
                            tint = ContentBlack
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("수정", fontSize = 13.sp) },
                            onClick = {
                                menuExpanded = false
                                onClickEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제", color = Color.Red, fontSize = 13.sp) },
                            onClick = {
                                menuExpanded = false
                                onClickDelete()
                            }
                        )
                    }
                }
            }

            // [간격] 프로필 라인과 본문 사이 간격 늘림 (4dp -> 8dp)
            Spacer(Modifier.height(8.dp))

            // [중단] 내용
            Text(
                text = comment.content,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 2.dp)
            )

            Spacer(Modifier.height(6.dp))

            // [하단] 날짜 | 좋아요 수 (왼쪽 정렬)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 날짜
                Text(timeText, fontSize = 11.sp, color = Color.Gray)

                // 좋아요 수 (날짜 옆에 표시)
                if (comment.likeCount > 0) {
                    Spacer(Modifier.width(8.dp)) // 날짜와 간격
                    Icon(
                        painterResource(R.drawable.yellow_heart),
                        contentDescription = null,
                        modifier = Modifier.size(11.dp), // 정보 표시용 하트는 작게 유지
                        tint = Color(0xFFFFDF37)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${comment.likeCount}", fontSize = 11.sp, color = Color.Gray)
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
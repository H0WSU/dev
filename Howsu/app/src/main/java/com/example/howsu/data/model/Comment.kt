package com.example.howsu.data.model

data class Comment(
    val id: String = "",                  // Firestore 문서 ID
    val postId: Long = 0L,                // 어느 글의 댓글인지

    val parentCommentId: String? = null,  // 부모 댓글 ID (null이면 최상위 댓글)

    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String? = null,

    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val deleted: Boolean = false
)

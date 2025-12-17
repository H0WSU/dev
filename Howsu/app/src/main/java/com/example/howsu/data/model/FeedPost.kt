package com.example.howsu.data.model

data class FeedPost(
    val id: Long = System.currentTimeMillis(),

    val authorId: String = "",
    val authorName: String = "",
    val authorProfileImage: String? = null,

    val title: String = "",
    val content: String = "",

    val imageUris: List<String> = emptyList(),
    val videoUris: List<String> = emptyList(),

    val hashtags: List<String> = emptyList(),

    // Firestore number 기본이 Long이라 Long으로 받는 게 안전합니다.
    val likeCount: Long = 0L,
    val commentCount: Long = 0L,

    // UI용 상태(문서에 없어도 기본값으로 안전)
    val isLiked: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val familyId: String = ""
)

enum class FeedFilter {
    ALL, TEXT, IMAGE, VIDEO
}

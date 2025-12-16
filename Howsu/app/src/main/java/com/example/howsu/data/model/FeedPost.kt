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

    val likeCount: Int = 0,
    val commentCount: Int = 0,

    val isLiked: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val familyId: String = ""
)


enum class FeedFilter {
    ALL, TEXT, IMAGE, VIDEO
}

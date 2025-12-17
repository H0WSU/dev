package com.example.howsu.data.model

import com.google.firebase.firestore.PropertyName

data class FeedPost(
    var id: Long = 0L,
    var authorId: String = "",
    var authorName: String = "",
    var authorProfileImage: String? = null,
    var title: String = "",
    var content: String = "",
    var imageUris: List<String> = emptyList(),
    var videoUris: List<String> = emptyList(),
    var hashtags: List<String> = emptyList(),

    // @get:@set을 모두 붙여서 Firestore의 likeCount 필드와 강제로 연결합니다.
    @get:PropertyName("likeCount") @set:PropertyName("likeCount")
    var likeCount: Long = 0L,

    @get:PropertyName("commentCount") @set:PropertyName("commentCount")
    var commentCount: Long = 0L,

    // DB에 있는 'liked' 필드와 충돌하지 않도록 UI용임을 확실히 합니다.
    @get:PropertyName("isLiked") @set:PropertyName("isLiked")
    var isLiked: Boolean = false,

    var createdAt: Long = 0L,
    var familyId: String = ""
) {
    // Firebase용 빈 생성자
    constructor() : this(0L)
}

enum class FeedFilter {
    ALL, TEXT, IMAGE, VIDEO
}
package com.example.howsu.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FeedPost(
    val id: Long = System.currentTimeMillis(),

    //누가 쓴 글인지 화면에 표시하기 위함
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

    val createdAt: Long = System.currentTimeMillis(),

    val familyId: String = ""   //속한 가족
)


//탭(전체/글/사진/동영상) 상태용
enum class FeedFilter{
    ALL, TEXT, IMAGE, VIDEO
}


package com.example.howsu.feed

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

//feed 등록 데이터

data class FeedPost (
    val id : Long = System.currentTimeMillis(),
    val title : String,
    val content : String,
    val imageUrls : List<String> = emptyList(),
    val hashtags : List<String> = emptyList(),
    val likeCount : Int = 0,
    val commentCount:Int = 0,
    val createdAt: String = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
)
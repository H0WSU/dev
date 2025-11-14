package com.example.howsu.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Schedule(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val memo: String = "",
    val isAllDay: Boolean = false,
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val petNames: List<String> = emptyList(),

    val color: String = "#000000" // (기본값 검은색)
)
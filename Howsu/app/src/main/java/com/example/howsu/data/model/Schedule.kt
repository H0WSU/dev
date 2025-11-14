package com.example.howsu.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName


data class Schedule(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val memo: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val petNames: List<String> = emptyList(),

    val color: String = "#000000", // (기본값 검은색)
    val recurrenceRule: String = "반복 안 함",
    val alarmRule: String = "설정 안 함",

    @get:PropertyName("isAllDay")
    @set:PropertyName("isAllDay")
    var isAllDay: Boolean = false

)
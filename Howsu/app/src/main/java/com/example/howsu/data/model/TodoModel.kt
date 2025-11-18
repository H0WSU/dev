package com.example.howsu.data.model

import androidx.annotation.DrawableRes
import com.google.firebase.firestore.DocumentId

data class Task(
    val id: String = "", // <-- Firestore가 DB 값을 채워넣을 수 있도록 빈 값으로 변경
    val title: String? = null,
    val date: String? = null,
    @JvmField
    val isChecked: Boolean = false
)

data class TodoGroup(
    @DocumentId
    val documentId: String = "",

    val assigneeId: String? = null,
    val assigneeName: String? = null,
    @DrawableRes val assigneeProfileRes: Int? = null,
    val tasks: List<Task> = emptyList(),
    val petNames: List<String> = emptyList()
)
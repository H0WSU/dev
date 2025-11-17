package com.example.howsu.data.model

import androidx.annotation.DrawableRes
import com.google.firebase.firestore.DocumentId
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String? = null,
    val date: String? = null,
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
package com.example.howsu.data.model

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

    val familyId: String = "",

    // 여러 명(List)으로 변경
    val assigneeIds: List<String> = emptyList(),         // ID 리스트
    val assigneeNames: List<String> = emptyList(),       // 이름 리스트
    val assigneeProfileUrls: List<String?> = emptyList(), // 사진 주소 리스트

    val tasks: List<Task> = emptyList(),
    val petNames: List<String> = emptyList(),
    val petProfileUrls: List<String?> = emptyList()
)
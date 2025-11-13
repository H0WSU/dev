package com.example.howsu.data.model

import androidx.annotation.DrawableRes

data class Task(
    var id: Int = 0,
    var title: String = "",
    var date: String = "",
    var isChecked: Boolean = false
)

data class TodoGroup(
    var id: Int = 0,
    var assigneeName: String = "",
    @DrawableRes var assigneeProfileRes: Int? = null,
    var tasks: List<Task> = emptyList()
)
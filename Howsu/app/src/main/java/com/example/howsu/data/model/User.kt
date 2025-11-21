package com.example.howsu.data.model

data class User(
    val uid: String = "",
    val email: String? = null,
    val name: String = "",
    val currentFamilyId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
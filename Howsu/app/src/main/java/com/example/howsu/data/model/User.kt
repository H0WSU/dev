package com.example.howsu.data.model

data class User(
    val uid: String,            // Firebase Auth UID (로그인 고유 키)
    val email: String?,         // 이메일
    val name: String,           // 가입 시 사용자 이름
    val currentFamilyId: String?, // 현재 보고 있는 가족 방 ID (없으면 null)
    val createdAt: Long = System.currentTimeMillis()
)
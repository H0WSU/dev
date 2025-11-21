package com.example.howsu.data.model

data class FamilyMember(
    val userId: String,        // User의 uid (누구인가?)
    val familyId: String,      // Family의 familyId (어느 방인가?)
    val nickName: String,      // 이 방에서 불릴 닉네임 (예: "엄마", "집사")
    val relationship: String,  // 호칭/역할 (예: "보호자", "언니")
    val profileImageUrl: String? = null
)
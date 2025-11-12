package com.example.howsu.data.model

data class FamilyMember(
    val userId: String,        // 사용자 고유 ID (알림 보낼 때 필요)
    val relationship: String,  // 관계
    val profileImageUrl: String? // 프로필 사진 URL (Coil 등으로 로드)
)
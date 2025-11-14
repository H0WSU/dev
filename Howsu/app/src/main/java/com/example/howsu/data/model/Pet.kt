package com.example.howsu.data.model

data class Pet(
    val petId: String,
    val name: String,
    val profileImageUrl: String? // 프로필 사진 URL (Coil 등으로 로드)
)
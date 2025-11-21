package com.example.howsu.data.model

data class Family(
    val familyId: String,       // 가족 고유 ID (초대 코드로 사용, 예: "sda@1234")
    val familyName: String,     // 가족 이름 (예: "루비네", "나혼자산다")
    val ownerUserId: String,    // 방장(생성자)의 uid
    val isSoloMode: Boolean = false, // ★ 중요: 혼자 쓰기 모드 여부
    val memberIds: List<String> = emptyList(), // 이 가족에 속한 멤버 uid 리스트
    val createdAt: Long = System.currentTimeMillis()
)
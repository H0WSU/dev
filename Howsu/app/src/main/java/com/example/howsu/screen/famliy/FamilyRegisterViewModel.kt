package com.example.howsu.screen.family

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.random.Random

// 상태 Enum
enum class FamilyRegState { NONE, SKIP, PRE_DO_IT, DO_IT }
enum class FamilyTab { CREATE, JOIN }

class FamilyRegisterViewModel : ViewModel() {
    // UI 상태 관리
    var regState by mutableStateOf(FamilyRegState.NONE)
    var selectedTab by mutableStateOf(FamilyTab.CREATE)

    // 입력 필드
    var inputFamilyName by mutableStateOf("") // 생성할 가족 이름
    var inputFamilyId by mutableStateOf("")   // 참여할 가족 ID

    // 결과 저장용 (생성된 가족 ID)
    var createdFamilyId by mutableStateOf("")

    // [로직 1] 공유 가족 생성 (Create Shared Family)
    fun createSharedFamily() {
        val newId = generateRandomId()
        createdFamilyId = newId
        // 이름이 없으면 기본값
        if (inputFamilyName.isBlank()) inputFamilyName = "우리 가족"
        println("공유 가족 생성: $inputFamilyName, ID: $newId")
    }

    // [로직 2] 1인 가족 생성 (Create Solo Family)
    // ★ nickname을 받아서 자동 작명
    fun createSoloFamily(nickname: String) {
        val newId = generateRandomId()
        createdFamilyId = newId

        // 이름이 없으면 "OOO님의 집"으로 자동 설정
        if (inputFamilyName.isBlank()) {
            inputFamilyName = "${nickname}님의 집"
        }
        println("1인 가족 생성: ID: $newId, 이름: $inputFamilyName")
    }

    // [로직 3] 가족 참여 (Join Family)
    fun joinFamily(): Boolean {
        if (inputFamilyId.isBlank()) return false
        println("가족 참여 시도: $inputFamilyId")
        return true
    }

    private fun generateRandomId(): String {
        return "with@${Random.nextInt(1000, 9999)}"
    }
}
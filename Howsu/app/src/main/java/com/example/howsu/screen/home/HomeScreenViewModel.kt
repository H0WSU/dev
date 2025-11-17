package com.example.howsu.screen.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ----------------------------------------------------
// HomeScreenViewModel.kt
// 데이터 모델, 상태 및 비즈니스 로직 관리
// ----------------------------------------------------

// 1. 데이터 모델 (Data Classes) 정의
data class Reminder(
    val text : String,
    val date: String,
    val isDone : Boolean
)
data class Pet(
    val name : String,
    val age : Int,
    val gender : String,
    val imageUrl: String = ""
)
data class FamilyMember(
    val name: String,
    val isUser: Boolean = false
)
data class ScheduleDay(
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val isSelected: Boolean
)

// UI에 필요한 모든 상태를 담는 State 클래스
data class HomeUiState(
    val pets: List<Pet> = emptyList(),
    val familyMembers: List<FamilyMember> = emptyList(),
    val scheduleDays: List<ScheduleDay> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val showInviteDialog: Boolean = false
)


class HomeScreenViewModel : ViewModel() {

    // MutableStateFlow를 사용하여 UI 상태를 관리
    private val _uiState = MutableStateFlow(
        HomeUiState(
            pets = listOf(
                Pet("자몽",7,"여아"),
                Pet("두부", 2,"남아"),
                Pet("코코", 5,"남아"),
                Pet("복실", 1,"여아")
            ),
            familyMembers = listOf(
                FamilyMember("언니", isUser = true),
                FamilyMember("엄마", isUser = false),
            ),
            scheduleDays = listOf(
                ScheduleDay("화", 13, false),
                ScheduleDay("수", 14, false),
                ScheduleDay("목", 15, true), // 오늘 날짜처럼 보이게 선택됨
                ScheduleDay("금", 16, false),
                ScheduleDay("토", 17, false),
                ScheduleDay("일", 18, false),
            ),
            reminders = listOf(
                Reminder("츄르 사오기", "2025. 10. 28", false),
                Reminder("병원 방문하기", "2025. 10. 28", false),
                Reminder("목욕시키기", "2025. 10. 28", true)
            )
        )
    )
    // UI가 읽을 수 있는 읽기 전용 StateFlow 노출
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


    // ----------------------
    // 이벤트 핸들러 (로직)
    // ----------------------

    fun onInviteDialogVisibilityChange(isVisible: Boolean) {
        _uiState.update { it.copy(showInviteDialog = isVisible) }
    }

    /**
     * 가족 초대 로직 (현재는 임시로 목록에 추가)
     */
    fun inviteFamilyMember(email: String) {
        // 실제로는 API 호출 로직이 들어갑니다.
        println("Invitation sent to: $email")

        val newMember = FamilyMember(
            name = email.substringBefore("@"),
            isUser = false
        )

        _uiState.update { currentState ->
            currentState.copy(
                familyMembers = currentState.familyMembers + newMember,
                showInviteDialog = false // 초대 후 다이얼로그 닫기
            )
        }
    }

    /**
     * 리마인더 체크박스 상태 변경 로직
     */
    fun onReminderCheckedChange(reminder: Reminder, isChecked: Boolean) {
        _uiState.update { currentState ->
            val updatedReminders = currentState.reminders.map {
                if (it == reminder) {
                    it.copy(isDone = isChecked)
                } else {
                    it
                }
            }
            currentState.copy(reminders = updatedReminders)
        }
    }

    // TODO: 펫 정보 보기, 일정 날짜 선택 등 다른 이벤트 로직도 여기에 추가
}
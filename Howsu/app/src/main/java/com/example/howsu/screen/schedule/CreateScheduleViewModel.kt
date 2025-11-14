package com.example.howsu.screen.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Pet
import com.google.firebase.Timestamp // ★ 1. 임포트
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date // ★ 2. 임포트

class CreateScheduleViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _allPets = MutableStateFlow<List<Pet>>(emptyList())
    val allPets: StateFlow<List<Pet>> = _allPets.asStateFlow()

    private val _selectedPets = MutableStateFlow<List<Pet>>(emptyList())
    val selectedPets: StateFlow<List<Pet>> = _selectedPets.asStateFlow()

    private val _isPetDropdownVisible = MutableStateFlow(false)
    val isPetDropdownVisible: StateFlow<Boolean> = _isPetDropdownVisible.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _memo = MutableStateFlow("")
    val memo: StateFlow<String> = _memo.asStateFlow()

    private val _isAllDay = MutableStateFlow(false)
    val isAllDay: StateFlow<Boolean> = _isAllDay.asStateFlow()

    private val _startDate = MutableStateFlow(System.currentTimeMillis())
    val startDate: StateFlow<Long> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(System.currentTimeMillis() + 3600000) // 1시간 뒤
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    // --- ★ 3. 색상 관련 상태 추가 ---
    private val _selectedColor = MutableStateFlow("#000000") // 기본값 검은색
    val selectedColor: StateFlow<String> = _selectedColor.asStateFlow()

    // 선택 가능한 색상 리스트
    val predefinedColors = listOf(
        "#000000", // 검은색
        "#4285F4", // 파란색
        "#EA4335", // 빨간색
        "#34A853", // 녹색
        "#FABC05", // 노란색
        "#7986CB"  // 연보라
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // (임시) 더미 데이터
            val dummyPets = listOf(
                Pet(petId = "pet_id_1", name = "자몽", profileImageUrl = null),
                Pet(petId = "pet_id_2", name = "레몬", profileImageUrl = null),
                Pet(petId = "pet_id_3", name = "망고", profileImageUrl = null)
            )
            _allPets.value = dummyPets
        }
    }

    // (펫 관련 함수 ... onPetDropdownClicked, onPetSelected 등 ... )
    fun onPetDropdownClicked() { _isPetDropdownVisible.value = true }
    fun onPetDropdownDismissed() { _isPetDropdownVisible.value = false }
    fun onPetSelected(pet: Pet) {
        if (!_selectedPets.value.any { it.petId == pet.petId }) {
            _selectedPets.update { currentList -> currentList + pet }
        }
        _isPetDropdownVisible.value = false
    }
    fun onPetTagRemoved(pet: Pet) {
        _selectedPets.update { currentList ->
            currentList.filterNot { it.petId == pet.petId }
        }
    }

    // (일정 관련 함수)
    fun onTitleChanged(newTitle: String) { _title.value = newTitle }
    fun onMemoChanged(newMemo: String) { _memo.value = newMemo.take(20) }
    fun onAllDayToggled(isChecked: Boolean) { _isAllDay.value = isChecked }

    // --- ★ 4. 색상 선택 이벤트 핸들러 추가 ---
    fun onColorSelected(hexColor: String) {
        _selectedColor.value = hexColor
    }

    fun createSchedule(onComplete: () -> Unit) {
        val title = _title.value
        val pets = _selectedPets.value

        if (title.isBlank()) {
            return
        }

        // --- ★ 5. (핵심) Timestamp로 저장 ---
        val newSchedule = mapOf(
            "title" to title,
            "isAllDay" to _isAllDay.value,
            "startDate" to Timestamp(Date(_startDate.value)), // ★ Long -> Timestamp
            "endDate" to Timestamp(Date(_endDate.value)),     // ★ Long -> Timestamp
            "memo" to _memo.value,
            "petNames" to pets.map { it.name },
            "color" to _selectedColor.value // ★ 색상 Hex 저장
        )

        db.collection("schedules")
            .add(newSchedule)
            .addOnSuccessListener {
                println("--- 일정 생성 성공 ---")
                onComplete()
            }
            .addOnFailureListener { e ->
                println("--- 일정 생성 실패 ---: $e")
            }
    }
}
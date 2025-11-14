package com.example.howsu.screen.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Pet
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateScheduleViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _allPets = MutableStateFlow<List<Pet>>(emptyList())
    val allPets: StateFlow<List<Pet>> = _allPets.asStateFlow()

    private val _selectedPets = MutableStateFlow<List<Pet>>(emptyList())
    val selectedPets: StateFlow<List<Pet>> = _selectedPets.asStateFlow()

    private val _isPetDropdownVisible = MutableStateFlow(false)
    val isPetDropdownVisible: StateFlow<Boolean> = _isPetDropdownVisible.asStateFlow()

    // --- 2. 일정 생성 상태 (새로 추가) ---
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _memo = MutableStateFlow("")
    val memo: StateFlow<String> = _memo.asStateFlow()

    private val _isAllDay = MutableStateFlow(false)
    val isAllDay: StateFlow<Boolean> = _isAllDay.asStateFlow()

    // TODO: 날짜/시간 피커와 연동 필요
    private val _startDate = MutableStateFlow(System.currentTimeMillis())
    val startDate: StateFlow<Long> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(System.currentTimeMillis() + 3600000) // 1시간 뒤
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    // TODO: 색상, 반복, 알림 상태도 여기에 추가...
    // private val _selectedColor = ...


    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // (임시) 더미 데이터로 반려동물 목록 (Todo와 동일)
            val dummyPets = listOf(
                Pet(petId = "pet_id_1", name = "자몽", profileImageUrl = null),
                Pet(petId = "pet_id_2", name = "레몬", profileImageUrl = null),
                Pet(petId = "pet_id_3", name = "망고", profileImageUrl = null)
            )
            _allPets.value = dummyPets
        }
    }

    // --- 3. 이벤트 핸들러 (UI에서 호출) ---

    // (펫 관련 - Todo와 동일)
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

    // (일정 관련 - 새로 추가)
    fun onTitleChanged(newTitle: String) { _title.value = newTitle }
    fun onMemoChanged(newMemo: String) { _memo.value = newMemo.take(20) } // 20자 제한
    fun onAllDayToggled(isChecked: Boolean) { _isAllDay.value = isChecked }

    // TODO: onDateSelected, onRepeatSelected, onAlarmSelected...

    fun createSchedule(onComplete: () -> Unit) {
        val title = _title.value
        val pets = _selectedPets.value

        if (title.isBlank()) {
            // TODO: 제목이 비었을 때 사용자에게 알림
            return
        }

        // --- '일정 등록' 로K ---
        // (날짜 포맷 예시 - 필요에 따라 수정)
        val formattedDate = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).format(Date(_startDate.value))

        val newSchedule = mapOf(
            "id" to (0..10000).random(),
            "title" to title,
            "isAllDay" to _isAllDay.value,
            "startDate" to formattedDate, // (예시)
            "memo" to _memo.value,
            "petNames" to pets.map { it.name } // (예시: 펫 이름 리스트 저장)
        )

        // ★ "schedules"라는 새 컬렉션에 저장
        db.collection("schedules")
            .add(newSchedule)
            .addOnSuccessListener {
                println("--- 일정 생성 성공 ---")
                onComplete() // ★ 성공 시에만 화면 닫기
            }
            .addOnFailureListener { e ->
                println("--- 일정 생성 실패 ---: $e")
            }
    }
}
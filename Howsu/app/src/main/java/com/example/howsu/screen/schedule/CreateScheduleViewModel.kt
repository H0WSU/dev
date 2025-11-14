package com.example.howsu.screen.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Pet
import com.example.howsu.data.model.Schedule
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date

enum class DateTimePickerTarget {
    START, END
}

class CreateScheduleViewModel : ViewModel() {

    private val db = Firebase.firestore

    private var currentScheduleId: String? = null

    // --- 상태(State) 정의 ---
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

    // 날짜/시간 상태
    private val _startDate = MutableStateFlow(System.currentTimeMillis())
    val startDate: StateFlow<Long> = _startDate.asStateFlow()
    private val _endDate = MutableStateFlow(System.currentTimeMillis() + 3600000) // 1시간 뒤
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    // 날짜/시간 피커 제어 상태
    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()
    private val _showTimePicker = MutableStateFlow(false)
    val showTimePicker: StateFlow<Boolean> = _showTimePicker.asStateFlow()
    private val _pickerTarget = MutableStateFlow(DateTimePickerTarget.START)
    val pickerTarget: StateFlow<DateTimePickerTarget> = _pickerTarget.asStateFlow()

    // 반복/알림 상태
    private val _recurrenceRule = MutableStateFlow("반복 안 함")
    val recurrenceRule: StateFlow<String> = _recurrenceRule.asStateFlow()
    private val _showRecurrencePicker = MutableStateFlow(false)
    val showRecurrencePicker: StateFlow<Boolean> = _showRecurrencePicker.asStateFlow()
    val recurrenceOptions = listOf("반복 안 함", "매일", "매주", "매월", "매년")

    private val _alarmRule = MutableStateFlow("설정 안 함")
    val alarmRule: StateFlow<String> = _alarmRule.asStateFlow()
    private val _showAlarmPicker = MutableStateFlow(false)
    val showAlarmPicker: StateFlow<Boolean> = _showAlarmPicker.asStateFlow()
    val alarmOptions = listOf("설정 안 함", "일정 시작 시간", "10분 전", "1시간 전", "1일 전")

    // (색상 관련 상태 - 기존과 동일)
    private val _selectedColor = MutableStateFlow("#000000")
    val selectedColor: StateFlow<String> = _selectedColor.asStateFlow()
    private val _isColorPickerVisible = MutableStateFlow(false)
    val isColorPickerVisible: StateFlow<Boolean> = _isColorPickerVisible.asStateFlow()
    val predefinedColors = listOf(
        "#000000", "#4285F4", "#EA4335", "#34A853", "#FABC05", "#7986CB"
    )

    fun initialize(scheduleId: String?) {
        if (currentScheduleId == scheduleId && scheduleId != null) return
        currentScheduleId = scheduleId

        viewModelScope.launch {
            loadPetsData()
            if (scheduleId != null) {
                loadScheduleForEdit(scheduleId)
            }
        }
    }

    private suspend fun loadPetsData() {
        // (임시) 더미 데이터
        val dummyPets = listOf(
            Pet(petId = "pet_id_1", name = "자몽", profileImageUrl = null),
            Pet(petId = "pet_id_2", name = "레몬", profileImageUrl = null),
            Pet(petId = "pet_id_3", name = "망고", profileImageUrl = null)
        )
        _allPets.value = dummyPets
    }

    private suspend fun loadScheduleForEdit(scheduleId: String) {
        try {
            val doc = db.collection("schedules").document(scheduleId).get().await()
            val schedule = doc.toObject<Schedule>()
            if (schedule != null) {
                _title.value = schedule.title
                _memo.value = schedule.memo
                _selectedColor.value = schedule.color
                _isAllDay.value = schedule.isAllDay
                _startDate.value = schedule.startDate.toDate().time
                _endDate.value = schedule.endDate.toDate().time
                _recurrenceRule.value = schedule.recurrenceRule
                _alarmRule.value = schedule.alarmRule
                _selectedPets.value = _allPets.value.filter { pet ->
                    schedule.petNames.contains(pet.name)
                }
            }
        } catch (e: Exception) {
            Log.e("CreateScheduleVM", "수정할 일정($scheduleId) 로드 실패", e)
        }
    }

    // --- 펫 관련 이벤트 핸들러 ---
    fun onPetDropdownClicked() {
        _isPetDropdownVisible.value = true
    }

    fun onPetDropdownDismissed() {
        _isPetDropdownVisible.value = false
    }

    fun onPetSelected(pet: Pet) {
        if (!_selectedPets.value.any { it.petId == pet.petId }) {
            _selectedPets.update { it + pet }
        }
        _isPetDropdownVisible.value = false
    }

    fun onPetTagRemoved(pet: Pet) {
        _selectedPets.update { it.filterNot { p -> p.petId == pet.petId } }
    }

    // --- 일정 관련 이벤트 핸들러 ---
    fun onTitleChanged(newTitle: String) {
        _title.value = newTitle
    }

    fun onMemoChanged(newMemo: String) {
        _memo.value = newMemo.take(20)
    }

    fun onAllDayToggled(isChecked: Boolean) {
        _isAllDay.value = isChecked
    }

    fun onColorSelected(hexColor: String) {
        _selectedColor.value = hexColor
        _isColorPickerVisible.value = false // ★ 2. (수정) 색상 선택 시 팝업 닫기
    }

    // --- ★ 3. (신규) 색상 팝업 핸들러 ---
    fun onColorPickerClicked() {
        _isColorPickerVisible.value = true
    }

    fun onColorPickerDismissed() {
        _isColorPickerVisible.value = false
    }


    fun onDatePickerClicked(target: DateTimePickerTarget) {
        _pickerTarget.value = target
        _showDatePicker.value = true
    }

    fun onDatePickerDismissed() {
        _showDatePicker.value = false
    }

    fun onDateSelected(selectedMillis: Long?) {
        _showDatePicker.value = false
        val selectedDate = Instant.ofEpochMilli(selectedMillis ?: System.currentTimeMillis())
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        updateDateTime(date = selectedDate)
    }

    fun onTimePickerClicked(target: DateTimePickerTarget) {
        _pickerTarget.value = target
        _showTimePicker.value = true
    }

    fun onTimePickerDismissed() {
        _showTimePicker.value = false
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        _showTimePicker.value = false
        val selectedTime = LocalTime.of(hour, minute)

        updateDateTime(time = selectedTime)
    }

    // 날짜 또는 시간이 선택됐을 때 _startDate, _endDate를 업데이트하는 헬퍼 함수
    private fun updateDateTime(date: LocalDate? = null, time: LocalTime? = null) {
        val targetState =
            if (_pickerTarget.value == DateTimePickerTarget.START) _startDate else _endDate

        val currentMillis = targetState.value
        val currentLocalDateTime = Instant.ofEpochMilli(currentMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        // 새 날짜, 기존 시간 OR 기존 날짜, 새 시간
        val newDateTime = LocalDateTime.of(
            date ?: currentLocalDateTime.toLocalDate(),
            time ?: currentLocalDateTime.toLocalTime()
        )

        // Long(Millis)로 변환하여 State 업데이트
        val newMillis = newDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        targetState.value = newMillis

        // (보너스) 시작 시간이 종료 시간보다 늦어지면, 종료 시간을 시작 시간 + 1시간으로 자동 설정
        if (_pickerTarget.value == DateTimePickerTarget.START && newMillis >= _endDate.value) {
            _endDate.value = newMillis + 3600000 // + 1 hour
        }
    }

    // --- ★ 6. (신규) 반복/알림 이벤트 핸들러 ---
    fun onRecurrenceClicked() {
        _showRecurrencePicker.value = true
    }

    fun onRecurrenceDismissed() {
        _showRecurrencePicker.value = false
    }

    fun onRecurrenceSelected(rule: String) {
        _recurrenceRule.value = rule
        _showRecurrencePicker.value = false
    }

    fun onAlarmClicked() {
        _showAlarmPicker.value = true
    }

    fun onAlarmDismissed() {
        _showAlarmPicker.value = false
    }

    fun onAlarmSelected(rule: String) {
        _alarmRule.value = rule
        _showAlarmPicker.value = false
    }

    fun saveSchedule(onComplete: () -> Unit) {
        val title = _title.value
        if (title.isBlank()) {
            return
        }

        val scheduleMap = mapOf(
            "title" to title,
            "memo" to _memo.value,
            "isAllDay" to _isAllDay.value,
            "startDate" to Timestamp(Date(_startDate.value)),
            "endDate" to Timestamp(Date(_endDate.value)),
            "petNames" to _selectedPets.value.map { it.name },
            "color" to _selectedColor.value,
            "recurrenceRule" to _recurrenceRule.value,
            "alarmRule" to _alarmRule.value
        )

        viewModelScope.launch {
            try {
                if (currentScheduleId == null) {
                    // 새 일정 생성
                    db.collection("schedules").add(scheduleMap).await()
                    Log.d("CreateScheduleVM", "일정 생성 성공")
                } else {
                    // 기존 일정 수정 (set으로 덮어쓰기)
                    db.collection("schedules").document(currentScheduleId!!).set(scheduleMap)
                        .await()
                    Log.d("CreateScheduleVM", "일정 수정 성공")
                }
                onComplete() // 완료 후 화면 닫기
            } catch (e: Exception) {
                Log.e("CreateScheduleVM", "저장 실패", e)
            }
        }
    }
}
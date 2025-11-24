package com.example.howsu.screen.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Pet
import com.example.howsu.data.model.Schedule
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
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
import java.util.Calendar
import java.util.Date

enum class DateTimePickerTarget {
    START, END
}

class CreateScheduleViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth // Auth 추가
    private var currentScheduleId: String? = null

    // --- (기존 상태들) ---
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
    private val _endDate = MutableStateFlow(System.currentTimeMillis() + 3600000)
    val endDate: StateFlow<Long> = _endDate.asStateFlow()
    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()
    private val _showTimePicker = MutableStateFlow(false)
    val showTimePicker: StateFlow<Boolean> = _showTimePicker.asStateFlow()
    private val _pickerTarget = MutableStateFlow(DateTimePickerTarget.START)
    val pickerTarget: StateFlow<DateTimePickerTarget> = _pickerTarget.asStateFlow()

    // ★★★ (신규) 반복 종료 날짜 상태 ★★★
    private val _recurrenceEndDate = MutableStateFlow<Long?>(null) // null이면 "계속 반복"
    val recurrenceEndDate: StateFlow<Long?> = _recurrenceEndDate.asStateFlow()

    private val _showRecurrenceEndDatePicker = MutableStateFlow(false)
    val showRecurrenceEndDatePicker: StateFlow<Boolean> = _showRecurrenceEndDatePicker.asStateFlow()
    // ★★★ (신규 끝) ★★★

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
            loadRealPets()

            if (scheduleId != null) loadScheduleForEdit(scheduleId)
        }
    }

    private suspend fun loadRealPets() {
        val user = auth.currentUser ?: return

        try {
            // 1. 내 가족 ID 찾기
            val userDoc = db.collection("users").document(user.uid).get().await()
            val myFamilyId = userDoc.getString("currentFamilyId")

            if (myFamilyId != null) {
                // 2. 펫 목록 가져오기
                val petsSnapshot = db.collection("families")
                    .document(myFamilyId)
                    .collection("pets")
                    .get()
                    .await()

                val realPets = petsSnapshot.documents.mapNotNull { doc ->
                    doc.toObject<Pet>()?.copy(petId = doc.id)
                }
                _allPets.value = realPets
            }
        } catch (e: Exception) {
            Log.e("CreateScheduleVM", "펫 로드 실패", e)
        }
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
                // (참고) 수정 시에는 반복 종료 날짜 로드 로직이 필요할 수 있으나,
                // 현재는 '새로 생성' 시에만 반복을 지원하므로 생략합니다.
            }
        } catch (e: Exception) {
            Log.e("CreateScheduleVM", "수정할 일정($scheduleId) 로드 실패", e)
        }
    }

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
        _isColorPickerVisible.value = false
    }

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

    private fun updateDateTime(date: LocalDate? = null, time: LocalTime? = null) {
        val targetState =
            if (_pickerTarget.value == DateTimePickerTarget.START) _startDate else _endDate
        val currentMillis = targetState.value
        val currentLocalDateTime = Instant.ofEpochMilli(currentMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val newDateTime = LocalDateTime.of(
            date ?: currentLocalDateTime.toLocalDate(),
            time ?: currentLocalDateTime.toLocalTime()
        )
        val newMillis = newDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        targetState.value = newMillis
        if (_pickerTarget.value == DateTimePickerTarget.START && newMillis >= _endDate.value) {
            _endDate.value = newMillis + 3600000 // + 1 hour
        }
    }

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

    // ★★★ (신규) 반복 종료 날짜 핸들러 ★★★
    fun onRecurrenceEndDateClicked() {
        _showRecurrenceEndDatePicker.value = true
    }

    fun onRecurrenceEndDateSelected(selectedMillis: Long?) {
        _showRecurrenceEndDatePicker.value = false
        if (selectedMillis == null) {
            // "취소" 누른 경우
            return
        }
        // (선택된 날짜의 자정으로 설정)
        val selectedDate = Instant.ofEpochMilli(selectedMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atTime(23, 59, 59) // 그날 23:59:59 까지 포함
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        _recurrenceEndDate.value = selectedDate
    }


    fun saveSchedule(context: Context, onComplete: () -> Unit) {
        val title = _title.value
        val user = auth.currentUser ?: return

        // 제목이 비어 있으면 저장 안 함
        if (title.isBlank()) return

        viewModelScope.launch {
            try {
                // 1. [핵심] 내 유저 정보에서 '가족 ID' 먼저 가져오기
                val userDoc = db.collection("users").document(user.uid).get().await()
                val myFamilyId = userDoc.getString("currentFamilyId")

                // 가족 ID가 없으면 저장 중단
                if (myFamilyId == null) {
                    Log.e("CreateScheduleVM", "가족 ID를 찾을 수 없습니다.")
                    return@launch
                }

                // 2. 저장할 데이터 맵 만들기 (이제 myFamilyId 사용 가능)
                val baseScheduleMap = mapOf(
                    "familyId" to myFamilyId, // ★ 가족 ID 저장
                    "title" to title,
                    "memo" to _memo.value,
                    "isAllDay" to _isAllDay.value,
                    "petNames" to _selectedPets.value.map { it.name },
                    "petProfileUrls" to _selectedPets.value.map { it.profileImageUrl },
                    "color" to _selectedColor.value,
                    "recurrenceRule" to _recurrenceRule.value,
                    "alarmRule" to _alarmRule.value
                )

                // 3. 반복 일정인지 확인하고 저장 시작
                if (currentScheduleId == null && _recurrenceRule.value != "반복 안 함") {
                    // [반복 일정 저장 로직]
                    val batch = db.batch()
                    val schedulesRef = db.collection("schedules")
                    val duration = _endDate.value - _startDate.value

                    val recurrenceDates = calculateNextRecurrenceDates(
                        _startDate.value,
                        _recurrenceRule.value,
                        _recurrenceEndDate.value
                    )

                    recurrenceDates.forEach { startDateMillis ->
                        val newDocRef = schedulesRef.document()
                        val newEndDateMillis = startDateMillis + duration
                        val newMap = baseScheduleMap.toMutableMap().apply {
                            this["startDate"] = Timestamp(Date(startDateMillis))
                            this["endDate"] = Timestamp(Date(newEndDateMillis))
                        }
                        batch.set(newDocRef, newMap)

                        // 알림 설정
                        scheduleAlarm(
                            context = context,
                            scheduleId = newDocRef.id,
                            title = title,
                            startDateMillis = startDateMillis,
                            alarmRule = _alarmRule.value
                        )
                    }
                    batch.commit().await()
                    Log.d("CreateScheduleVM", "${recurrenceDates.size}개 반복 일정 생성 성공")

                } else {
                    // [단일 일정 저장/수정 로직]
                    val scheduleMap = baseScheduleMap.toMutableMap().apply {
                        this["startDate"] = Timestamp(Date(_startDate.value))
                        this["endDate"] = Timestamp(Date(_endDate.value))
                    }

                    val docId: String
                    if (currentScheduleId == null) {
                        val docRef = db.collection("schedules").add(scheduleMap).await()
                        docId = docRef.id
                        Log.d("CreateScheduleVM", "일정 생성 성공")
                    } else {
                        docId = currentScheduleId!!
                        db.collection("schedules").document(docId).set(scheduleMap).await()
                        Log.d("CreateScheduleVM", "일정 수정 성공")
                    }

                    // 알림 설정
                    scheduleAlarm(
                        context = context,
                        scheduleId = docId,
                        title = title,
                        startDateMillis = _startDate.value,
                        alarmRule = _alarmRule.value
                    )
                }

                // 4. 저장 완료 후 화면 닫기
                onComplete()

            } catch (e: Exception) {
                Log.e("CreateScheduleVM", "저장 실패", e)
            }
        }
    }

    // --- ★ 10. (대폭 수정) 반복 날짜 계산 헬퍼 ---
    private fun calculateNextRecurrenceDates(
        startDateMillis: Long,
        rule: String,
        endDateMillis: Long? // ★ (신규) 반복 종료 날짜
    ): List<Long> {
        val dates = mutableListOf<Long>()
        val calendar = Calendar.getInstance().apply { timeInMillis = startDateMillis }

        // 1. 종료 날짜가 없으면, '계속 반복'으로 간주하고 임의로 1년치만 생성
        // (365L * 24 * 60 * 60 * 1000) = 1년 뒤
        val finalEndDateMillis = endDateMillis ?: (calendar.timeInMillis + 31536000000L)

        // 2. 'while' 루프를 사용해 종료 날짜 전까지만 반복
        while (calendar.timeInMillis <= finalEndDateMillis) {
            dates.add(calendar.timeInMillis) // 현재 날짜 추가

            // 다음 날짜로 이동
            when (rule) {
                "매일" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                "매주" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                "매월" -> calendar.add(Calendar.MONTH, 1)
                "매년" -> calendar.add(Calendar.YEAR, 1)
                else -> break // "반복 안 함" 또는 예외
            }
        }

        Log.d("CreateScheduleVM", "${dates.size}개의 반복 일정 날짜 생성됨 (종료: ${Date(finalEndDateMillis)})")
        return dates
    }


    // --- ★ 11. (신규) 알림 시간 계산 헬퍼 ---
    private fun calculateAlarmTime(startDateMillis: Long, alarmRule: String): Long? {
        val tenMinutes = 10 * 60 * 1000
        val oneHour = 60 * 60 * 1000
        val oneDay = 24 * 60 * 60 * 1000

        return when (alarmRule) {
            "일정 시작 시간" -> startDateMillis
            "10분 전" -> startDateMillis - tenMinutes
            "1시간 전" -> startDateMillis - oneHour
            "1일 전" -> startDateMillis - oneDay
            else -> null // "설정 안 함"
        }
    }

    // --- ★ 12. (신규) 알림 예약 헬퍼 ---
    private fun scheduleAlarm(
        context: Context,
        scheduleId: String,
        title: String,
        startDateMillis: Long,
        alarmRule: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = calculateAlarmTime(startDateMillis, alarmRule)

        if (triggerAtMillis != null && triggerAtMillis > System.currentTimeMillis()) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("SCHEDULE_ID", scheduleId)
                putExtra("SCHEDULE_TITLE", title)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                // ★ 수정된 부분: 버전(SDK_INT)을 확인해서 분기 처리
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    // 안드로이드 12(S) 이상: 권한 체크 필요
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        // 권한 없으면 일반 알림으로 (또는 로그 출력)
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                    }
                } else {
                    // 안드로이드 12 미만: 권한 체크 없이 바로 정확한 알림 사용 가능
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }

                Log.d("CreateScheduleVM", "알림 예약 성공")

            } catch (e: SecurityException) {
                Log.e("CreateScheduleVM", "알림 예약 실패", e)
            }
        }
    }
}
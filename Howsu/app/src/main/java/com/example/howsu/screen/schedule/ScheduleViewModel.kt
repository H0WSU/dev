package com.example.howsu.screen.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Schedule
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

// ★★★ (신규) 3가지 삭제 유형을 정의하는 Enum 클래스
enum class DeletionType {
    SINGLE, // 이 일정만
    FUTURE, // 이 일정 + 미래
    ALL     // 모든 관련 일정
}

class ScheduleViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val zoneId = ZoneId.systemDefault()

    // --- (기존 상태 - 변경 없음) ---
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth = _currentMonth.asStateFlow()
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules = _schedules.asStateFlow()
    private val _monthSchedules = MutableStateFlow<Map<Int, List<Schedule>>>(emptyMap())
    val monthSchedules = _monthSchedules.asStateFlow()
    private val _selectedSchedule = MutableStateFlow<Schedule?>(null)
    val selectedSchedule = _selectedSchedule.asStateFlow()

    init {
        fetchSchedulesForDate(_selectedDate.value)
        loadMonthSchedules(_currentMonth.value)
    }

    // --- (기존 함수 - 변경 없음) ---
    fun onDateSelected(day: Int) {
        val newDate = _currentMonth.value.atDay(day)
        _selectedDate.value = newDate
        fetchSchedulesForDate(newDate)
    }

    fun onMonthChange(isNext: Boolean) {
        val newMonth = if (isNext) _currentMonth.value.plusMonths(1) else _currentMonth.value.minusMonths(1)
        _currentMonth.value = newMonth
        loadMonthSchedules(newMonth)
        onDateSelected(1)
    }

    fun onMonthYearChange(year: Int, month: Int) {
        val newYearMonth = YearMonth.of(year, month)
        _currentMonth.value = newYearMonth
        loadMonthSchedules(newYearMonth)
        val currentDay = _selectedDate.value.dayOfMonth
        val maxDayInNewMonth = newYearMonth.lengthOfMonth()
        val newDay = currentDay.coerceAtMost(maxDayInNewMonth)
        onDateSelected(newDay)
    }

    private fun fetchSchedulesForDate(date: LocalDate) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            _schedules.value = emptyList()
            return
        }
        val currentFamilyId = currentUser.uid // ★ 내 아이디 가져오기

        viewModelScope.launch {
            try {
                val startOfDay = date.atStartOfDay(zoneId)
                val endOfDay = date.plusDays(1).atStartOfDay(zoneId)
                val startTimestamp = Timestamp(Date.from(startOfDay.toInstant()))
                val endTimestamp = Timestamp(Date.from(endOfDay.toInstant()))

                val querySnapshot = db.collection("schedules")
                    .whereEqualTo("familyId", currentFamilyId) // ★ [필수] 내 가족(나)의 일정만
                    .whereGreaterThanOrEqualTo("startDate", startTimestamp)
                    .whereLessThan("startDate", endTimestamp)
                    .orderBy("startDate")
                    .get()
                    .await()
                _schedules.value = querySnapshot.toObjects<Schedule>()
            } catch (e: Exception) {
                Log.e("ScheduleVM", "선택일 일정 로드 실패", e)
                _schedules.value = emptyList()
            }
        }
    }

    private fun loadMonthSchedules(yearMonth: YearMonth) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) return
        val currentFamilyId = currentUser.uid

        viewModelScope.launch {
            try {
                val startOfMonth = yearMonth.atDay(1).atStartOfDay(zoneId)
                val startOfNextMonth = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId)
                val startTimestamp = Timestamp(Date.from(startOfMonth.toInstant()))
                val endTimestamp = Timestamp(Date.from(startOfNextMonth.toInstant()))

                val querySnapshot = db.collection("schedules")
                    .whereEqualTo("familyId", currentFamilyId) // ★ [필수]
                    .whereGreaterThanOrEqualTo("startDate", startTimestamp)
                    .whereLessThan("startDate", endTimestamp)
                    .get()
                    .await()

                val schedulesList = querySnapshot.toObjects<Schedule>()
                val groupedSchedules = schedulesList
                    .groupBy { schedule ->
                        schedule.startDate.toDate().toInstant()
                            .atZone(zoneId)
                            .toLocalDate()
                            .dayOfMonth
                    }
                _monthSchedules.value = groupedSchedules
                Log.d("ScheduleVM", "월간 일정(${yearMonth}) 로드 성공")
            } catch (e: Exception) {
                Log.e("ScheduleVM", "월간 일정 로드 실패", e)
                _monthSchedules.value = emptyMap()
            }
        }
    }

    fun getHourFromTimestamp(timestamp: Timestamp): Int {
        val instant = timestamp.toDate().toInstant()
        return instant.atZone(zoneId).hour
    }

    fun loadScheduleDetails(scheduleId: String?) {
        if (scheduleId == null || scheduleId == "temp_id") {
            Log.e("ScheduleViewModel", "유효하지 않은 scheduleId: $scheduleId")
            _selectedSchedule.value = null
            return
        }
        _selectedSchedule.value = null
        viewModelScope.launch {
            try {
                val document = db.collection("schedules").document(scheduleId).get().await()
                _selectedSchedule.value = document.toObject<Schedule>()
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "일정($scheduleId) 불러오기 실패", e)
                _selectedSchedule.value = null
            }
        }
    }

    // ★★★ (대폭 수정) 일정 삭제 함수 ★★★
    fun deleteSchedule(deletionType: DeletionType, onComplete: () -> Unit) {
        val schedule = _selectedSchedule.value ?: return

        // ★ 안전장치: 현재 로그인한 사람 확인
        val currentUser = Firebase.auth.currentUser ?: return
        val currentFamilyId = currentUser.uid

        viewModelScope.launch {
            try {
                when (deletionType) {
                    DeletionType.SINGLE -> {
                        db.collection("schedules").document(schedule.id).delete().await()
                    }
                    DeletionType.FUTURE, DeletionType.ALL -> {
                        if (schedule.recurrenceRule == "반복 안 함") {
                            db.collection("schedules").document(schedule.id).delete().await()
                        } else {
                            var query = db.collection("schedules")
                                .whereEqualTo("familyId", currentFamilyId) // ★ [중요] 내 일정 중에서만 검색
                                .whereEqualTo("title", schedule.title)
                                .whereEqualTo("recurrenceRule", schedule.recurrenceRule)

                            if (deletionType == DeletionType.FUTURE) {
                                query = query.whereGreaterThanOrEqualTo("startDate", schedule.startDate)
                            }

                            val querySnapshot = query.get().await()
                            val batch = db.batch()
                            querySnapshot.documents.forEach { doc ->
                                batch.delete(doc.reference)
                            }
                            batch.commit().await()
                            Log.d("ScheduleViewModel", "${querySnapshot.size()}개 반복 일정(${deletionType}) 삭제 성공")
                        }
                    }
                }

                // (공통 로직) 목록 및 캘린더 새로고침
                fetchSchedulesForDate(_selectedDate.value)
                loadMonthSchedules(_currentMonth.value)
                onComplete()

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "일정 삭제 실패", e)
            }
        }
    }

    fun refreshAllSchedules() {
        Log.d("ScheduleVM", "모든 일정 새로고침 (목록 + 월간 캘린더)")
        fetchSchedulesForDate(_selectedDate.value)
        loadMonthSchedules(_currentMonth.value)
    }
}
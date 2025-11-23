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

// 삭제 유형 Enum
enum class DeletionType {
    SINGLE, FUTURE, ALL
}

class ScheduleViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth // Auth 추가
    private val zoneId = ZoneId.systemDefault()

    private var myFamilyId: String? = null

    // --- 상태 변수들 ---
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
        // 앱 켜지면 가족 ID부터 찾고 -> 스케줄 로딩 시작
        initializeFamilyId()
    }

    // 내 가족 ID 가져오기
    private fun initializeFamilyId() {
        val user = auth.currentUser
        if (user == null) {
            _schedules.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                // users 컬렉션에서 currentFamilyId 조회
                val doc = db.collection("users").document(user.uid).get().await()
                val familyId = doc.getString("currentFamilyId")

                if (familyId != null) {
                    myFamilyId = familyId
                    Log.d("ScheduleVM", "가족 ID 확인됨: $familyId")

                    // ID를 찾았으니 이제 진짜 데이터를 불러옴!
                    refreshAllSchedules()
                } else {
                    Log.e("ScheduleVM", "가족 ID가 없습니다.")
                }
            } catch (e: Exception) {
                Log.e("ScheduleVM", "유저 정보 로드 실패", e)
            }
        }
    }

    // 날짜 선택
    fun onDateSelected(day: Int) {
        val newDate = _currentMonth.value.atDay(day)
        _selectedDate.value = newDate
        fetchSchedulesForDate(newDate)
    }

    // 월 변경
    fun onMonthChange(isNext: Boolean) {
        val newMonth = if (isNext) _currentMonth.value.plusMonths(1) else _currentMonth.value.minusMonths(1)
        _currentMonth.value = newMonth
        loadMonthSchedules(newMonth)
        onDateSelected(1)
    }

    // 년/월 직접 선택
    fun onMonthYearChange(year: Int, month: Int) {
        val newYearMonth = YearMonth.of(year, month)
        _currentMonth.value = newYearMonth
        loadMonthSchedules(newYearMonth)
        val currentDay = _selectedDate.value.dayOfMonth
        val maxDayInNewMonth = newYearMonth.lengthOfMonth()
        val newDay = currentDay.coerceAtMost(maxDayInNewMonth)
        onDateSelected(newDay)
    }

    // ★ 일간 일정 불러오기 (수정됨: myFamilyId 사용)
    private fun fetchSchedulesForDate(date: LocalDate) {
        val familyId = myFamilyId ?: return // 가족 ID 없으면 중단

        viewModelScope.launch {
            try {
                val startOfDay = date.atStartOfDay(zoneId)
                val endOfDay = date.plusDays(1).atStartOfDay(zoneId)
                val startTimestamp = Timestamp(Date.from(startOfDay.toInstant()))
                val endTimestamp = Timestamp(Date.from(endOfDay.toInstant()))

                val querySnapshot = db.collection("schedules")
                    .whereEqualTo("familyId", familyId) // ★ 진짜 가족 ID로 검색
                    .whereGreaterThanOrEqualTo("startDate", startTimestamp)
                    .whereLessThan("startDate", endTimestamp)
                    .orderBy("startDate")
                    .get()
                    .await()
                _schedules.value = querySnapshot.toObjects<Schedule>()
            } catch (e: Exception) {
                Log.e("ScheduleVM", "일간 일정 로드 실패", e)
                _schedules.value = emptyList()
            }
        }
    }

    // 월간 일정 불러오기 (수정됨: myFamilyId 사용)
    private fun loadMonthSchedules(yearMonth: YearMonth) {
        val familyId = myFamilyId ?: return // 가족 ID 없으면 중단

        viewModelScope.launch {
            try {
                val startOfMonth = yearMonth.atDay(1).atStartOfDay(zoneId)
                val startOfNextMonth = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId)
                val startTimestamp = Timestamp(Date.from(startOfMonth.toInstant()))
                val endTimestamp = Timestamp(Date.from(startOfNextMonth.toInstant()))

                val querySnapshot = db.collection("schedules")
                    .whereEqualTo("familyId", familyId) // ★ 진짜 가족 ID로 검색
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
                Log.d("ScheduleVM", "월간 일정 로드 성공: ${schedulesList.size}개")
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
        if (scheduleId == null) {
            _selectedSchedule.value = null
            return
        }
        viewModelScope.launch {
            try {
                val document = db.collection("schedules").document(scheduleId).get().await()
                _selectedSchedule.value = document.toObject<Schedule>()?.copy(id = document.id) // ID 복사
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "일정 상세 로드 실패", e)
                _selectedSchedule.value = null
            }
        }
    }

    // 일정 삭제 (수정됨: myFamilyId 사용)
    fun deleteSchedule(deletionType: DeletionType, onComplete: () -> Unit) {
        val schedule = _selectedSchedule.value ?: return
        val familyId = myFamilyId ?: return

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
                                .whereEqualTo("familyId", familyId) // ★ 내 가족 일정 중에서만 삭제
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
                        }
                    }
                }
                // 삭제 후 새로고침
                refreshAllSchedules()
                onComplete()
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "삭제 실패", e)
            }
        }
    }

    fun refreshAllSchedules() {
        if (myFamilyId == null) {
            initializeFamilyId() // ID가 없으면 다시 로드 시도
        } else {
            fetchSchedulesForDate(_selectedDate.value)
            loadMonthSchedules(_currentMonth.value)
        }
    }
}
package com.example.howsu.screen.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Schedule
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

class ScheduleViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val zoneId = ZoneId.systemDefault() // zoneId를 멤버 변수로 선언

    // --- 1. 기존 상태 (선택된 날짜, 현재 월, 선택된 날의 일정 목록) ---
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth = _currentMonth.asStateFlow()

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules = _schedules.asStateFlow() // 하단 목록용

    // --- ★ 2. (신규) 캘린더 UI(색상 선)를 위한 현재 '월'의 전체 일정 ---
    private val _monthSchedules = MutableStateFlow<Map<Int, List<Schedule>>>(emptyMap())
    val monthSchedules = _monthSchedules.asStateFlow() // 캘린더 인디케이터용

    // --- 3. (기존) 일정 상세보기용 상태 ---
    private val _selectedSchedule = MutableStateFlow<Schedule?>(null)
    val selectedSchedule = _selectedSchedule.asStateFlow()

    init {
        // 앱 시작 시, 1. 선택된 날짜의 목록 / 2. 현재 월의 전체 일정을 모두 불러옴
        fetchSchedulesForDate(_selectedDate.value)
        loadMonthSchedules(_currentMonth.value) // ★ 신규 호출
    }

    /**
     * 날짜를 클릭했을 때 호출 (기존과 동일)
     * _schedules (하단 목록)만 업데이트
     */
    fun onDateSelected(day: Int) {
        val newDate = _currentMonth.value.atDay(day)
        _selectedDate.value = newDate
        fetchSchedulesForDate(newDate)
    }

    /**
     * 이전/다음 달 버튼 클릭 (★ 수정됨)
     */
    fun onMonthChange(isNext: Boolean) {
        val newMonth = if (isNext) _currentMonth.value.plusMonths(1) else _currentMonth.value.minusMonths(1)
        _currentMonth.value = newMonth

        loadMonthSchedules(newMonth) // ★ 신규: 월 전체 일정을 새로고침
        onDateSelected(1)          // ★ 추가: 1일을 기본으로 선택
    }

    /**
     * 월/년 픽커에서 선택 (★ 수정됨)
     */
    fun onMonthYearChange(year: Int, month: Int) {
        val newYearMonth = YearMonth.of(year, month)
        _currentMonth.value = newYearMonth

        loadMonthSchedules(newYearMonth) // ★ 신규: 월 전체 일정을 새로고침

        // (기존 로직 유지 - 선택한 '일'을 보존하려는 좋은 로직)
        val currentDay = _selectedDate.value.dayOfMonth
        val maxDayInNewMonth = newYearMonth.lengthOfMonth()
        val newDay = currentDay.coerceAtMost(maxDayInNewMonth)
        onDateSelected(newDay)
    }

    /**
     * [선택된 날짜]의 일정만 불러오기 (기존과 동일)
     * (하단 목록을 채우는 용도)
     */
    private fun fetchSchedulesForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                val startOfDay = date.atStartOfDay(zoneId)
                val endOfDay = date.plusDays(1).atStartOfDay(zoneId) // 다음 날 0시
                val startTimestamp = Timestamp(Date.from(startOfDay.toInstant()))
                val endTimestamp = Timestamp(Date.from(endOfDay.toInstant()))

                val querySnapshot = db.collection("schedules")
                    .whereGreaterThanOrEqualTo("startDate", startTimestamp)
                    .whereLessThan("startDate", endTimestamp) // endOfDay 미만
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

    /**
     * ★★★ (신규) [현재 월]의 전체 일정을 불러오기
     * (캘린더의 색상 선을 채우는 용도)
     */
    private fun loadMonthSchedules(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                // 1. 현재 월의 시작 (1일 00:00)
                val startOfMonth = yearMonth.atDay(1).atStartOfDay(zoneId)
                // 2. 다음 달의 시작 (다음 달 1일 00:00)
                val startOfNextMonth = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId)

                val startTimestamp = Timestamp(Date.from(startOfMonth.toInstant()))
                val endTimestamp = Timestamp(Date.from(startOfNextMonth.toInstant())) // (쿼리용)

                // 3. 'startDate'가 이번 달 1일 00:00 ~ 다음 달 1일 00:00 "미만"인 일정
                val querySnapshot = db.collection("schedules")
                    .whereGreaterThanOrEqualTo("startDate", startTimestamp)
                    .whereLessThan("startDate", endTimestamp)
                    .get()
                    .await()

                val schedulesList = querySnapshot.toObjects<Schedule>()

                // 4. List<Schedule> -> Map<Int, List<Schedule>>로 가공
                val groupedSchedules = schedulesList
                    .groupBy { schedule ->
                        // startDate의 '일(day)'을 기준으로 그룹화
                        schedule.startDate.toDate().toInstant()
                            .atZone(zoneId)
                            .toLocalDate()
                            .dayOfMonth
                    }

                _monthSchedules.value = groupedSchedules
                Log.d("ScheduleVM", "월간 일정(${yearMonth}) 로드 성공")

            } catch (e: Exception) {
                Log.e("ScheduleVM", "월간 일정 로드 실패", e)
                _monthSchedules.value = emptyMap() // 실패 시 비워줌
            }
        }
    }

    // (기존) 시간 변환 함수
    fun getHourFromTimestamp(timestamp: Timestamp): Int {
        val instant = timestamp.toDate().toInstant()
        return instant.atZone(zoneId).hour
    }

    // (기존) 일정 상세 로드 함수
    fun loadScheduleDetails(scheduleId: String?) {
        if (scheduleId == null || scheduleId == "temp_id") {
            Log.e("ScheduleViewModel", "유효하지 않은 scheduleId: $scheduleId")
            _selectedSchedule.value = null
            return
        }
        _selectedSchedule.value = null // 로딩 시작

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

    /**
     * 일정 삭제 함수 (★ 수정됨)
     */
    fun deleteSchedule(scheduleId: String, onComplete: () -> Unit) {
        if (scheduleId.isBlank() || scheduleId == "temp_id") {
            Log.e("ScheduleViewModel", "유효하지 않은 ID로 삭제를 시도했습니다: $scheduleId")
            return
        }

        viewModelScope.launch {
            try {
                db.collection("schedules").document(scheduleId).delete().await()
                Log.d("ScheduleViewModel", "일정 삭제 성공: $scheduleId")

                // (추가) 목록 화면 갱신
                fetchSchedulesForDate(_selectedDate.value)
                // (★★★ 신규) 캘린더(색상 선)도 갱신
                loadMonthSchedules(_currentMonth.value)

                onComplete()
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "일정 삭제 실패", e)
            }
        }
    }

    /**
     * ★★★ (신규) 일정 생성/수정 후 ScheduleScreen에서 호출할 새로고침 함수
     */
    fun refreshAllSchedules() {
        Log.d("ScheduleVM", "모든 일정 새로고침 (목록 + 월간 캘린더)")
        fetchSchedulesForDate(_selectedDate.value)
        loadMonthSchedules(_currentMonth.value)
    }
}
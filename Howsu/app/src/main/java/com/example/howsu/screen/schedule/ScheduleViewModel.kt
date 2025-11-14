// /screen/schedule/ScheduleViewModel.kt

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

class ScheduleViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth = _currentMonth.asStateFlow()
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules = _schedules.asStateFlow()


    // --- ★ 3. (신규) 일정 상세보기를 위한 상태 ---
    private val _selectedSchedule = MutableStateFlow<Schedule?>(null)
    val selectedSchedule = _selectedSchedule.asStateFlow()

    init {
        fetchSchedulesForDate(_selectedDate.value)
    }

    // ... (onDateSelected, onMonthChange, fetchSchedulesForDate, getHourFromTimestamp 등 기존 함수들) ...
    // (onDateSelected, onMonthChange, fetchSchedulesForDate는 여기에 있어야 합니다)
    fun onDateSelected(day: Int) {
        val newDate = _currentMonth.value.atDay(day)
        _selectedDate.value = newDate
        fetchSchedulesForDate(newDate)
    }
    fun onMonthChange(isNext: Boolean) {
        val newMonth = if (isNext) _currentMonth.value.plusMonths(1) else _currentMonth.value.minusMonths(1)
        _currentMonth.value = newMonth
    }

    fun onMonthYearChange(year: Int, month: Int) {
        val newYearMonth = YearMonth.of(year, month)
        _currentMonth.value = newYearMonth

        // 1. 현재 선택되어 있던 날짜(일)를 가져옴
        val currentDay = _selectedDate.value.dayOfMonth

        // 2. 새로 선택된 월의 마지막 날짜를 확인
        val maxDayInNewMonth = newYearMonth.lengthOfMonth()

        // 3. 날짜 보정 (예: 31일이었다가 2월로 가면 -> 28일로)
        val newDay = currentDay.coerceAtMost(maxDayInNewMonth)

        // 4. (★핵심) onDateSelected를 강제로 호출해서
        //    날짜(_selectedDate)와 일정(_schedules)을 모두 새로고침
        onDateSelected(newDay)
    }
    private fun fetchSchedulesForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                val zoneId = ZoneId.systemDefault()
                val startOfDay = date.atStartOfDay(zoneId)
                val endOfDay = date.plusDays(1).atStartOfDay(zoneId)
                val startTimestamp = Timestamp(Date.from(startOfDay.toInstant()))
                val endTimestamp = Timestamp(Date.from(endOfDay.toInstant()))

                val querySnapshot = db.collection("schedules")
                    .whereGreaterThanOrEqualTo("startDate", startTimestamp)
                    .whereLessThan("startDate", endTimestamp)
                    .orderBy("startDate")
                    .get()
                    .await()

                val scheduleList = querySnapshot.toObjects<Schedule>()
                _schedules.value = scheduleList
            } catch (e: Exception) {
                _schedules.value = emptyList()
            }
        }
    }
    fun getHourFromTimestamp(timestamp: Timestamp): Int {
        val instant = timestamp.toDate().toInstant()
        return instant.atZone(ZoneId.systemDefault()).hour
    }


    // --- ★ 4. (신규) ID로 일정 1개 불러오는 함수 ---
    fun loadScheduleDetails(scheduleId: String?) {
        if (scheduleId == null || scheduleId == "temp_id") {
            Log.e("ScheduleViewModel", "유효하지 않은 scheduleId: $scheduleId")
            _selectedSchedule.value = null
            return
        }

        // 화면이 다시 열릴 때를 대비해 이전 값 초기화 (로딩 표시)
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

    // ★ 5. (신규) 일정 삭제 함수 (이전에 추가함)
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

                onComplete()
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "일정 삭제 실패", e)
            }
        }
    }
}
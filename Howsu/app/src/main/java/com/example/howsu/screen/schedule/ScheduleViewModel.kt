package com.example.howsu.screen.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Schedule
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
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

    // --- 1. 상태(State) ---

    // 현재 선택된 날짜 (기본값: 오늘)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    // 캘린더 헤더에 표시될 년/월 (기본값: 이번 달)
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth = _currentMonth.asStateFlow()

    // 선택된 날짜의 일정 목록
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules = _schedules.asStateFlow()

    // TODO: 캘린더에 점 찍기 (이벤트가 있는 날짜 목록)
    // private val _eventDaysInMonth = MutableStateFlow<Set<Int>>(emptySet())
    // val eventDaysInMonth = _eventDaysInMonth.asStateFlow()

    init {
        // ViewModel이 생성될 때 오늘 날짜의 일정을 불러옴
        fetchSchedulesForDate(_selectedDate.value)
    }

    // --- 2. 이벤트(Event) 핸들러 ---

    /**
     * 캘린더에서 날짜(Int)를 클릭했을 때 호출
     */
    fun onDateSelected(day: Int) {
        val newDate = _currentMonth.value.atDay(day)
        _selectedDate.value = newDate
        fetchSchedulesForDate(newDate)
    }

    /**
     * TODO: 캘린더 헤더의 화살표로 월을 변경할 때 호출
     */
    fun onMonthChange(isNext: Boolean) {
        val newMonth = if (isNext) _currentMonth.value.plusMonths(1) else _currentMonth.value.minusMonths(1)
        _currentMonth.value = newMonth
        // TODO: 월이 바뀌면 _eventDaysInMonth를 새로 불러와야 함
    }

    // --- 3. Firestore 데이터 로직 ---

    /**
     * 특정 날짜(LocalDate)의 모든 일정을 Firestore에서 불러옵니다.
     */
    private fun fetchSchedulesForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                // 1. 쿼리할 날짜/시간 범위 계산 (예: 11월 14일 00:00:00 ~ 11월 15일 00:00:00)
                val zoneId = ZoneId.systemDefault()
                val startOfDay = date.atStartOfDay(zoneId)
                val endOfDay = date.plusDays(1).atStartOfDay(zoneId)

                // 2. ZonedDateTime -> java.util.Date -> Firebase Timestamp로 변환
                val startTimestamp = Timestamp(Date.from(startOfDay.toInstant()))
                val endTimestamp = Timestamp(Date.from(endOfDay.toInstant()))

                // 3. 쿼리 실행
                // "startDate"가 오늘 0시(포함) ~ 내일 0시(미포함) 사이인 문서 찾기
                val querySnapshot = db.collection("schedules")
                    .whereGreaterThanOrEqualTo("startDate", startTimestamp)
                    .whereLessThan("startDate", endTimestamp)
                    .orderBy("startDate") // 시간순 정렬
                    .get()
                    .await()

                // 4. Schedule 데이터 클래스로 변환 후 StateFlow 업데이트
                val scheduleList = querySnapshot.toObjects<Schedule>()
                _schedules.value = scheduleList
                Log.d("ScheduleViewModel", "Fetched ${scheduleList.size} schedules for $date")

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error fetching schedules", e)
                _schedules.value = emptyList()
            }
        }
    }

    /**
     * Timestamp를 앱의 시간대(ZoneId) 기준으로 Int(시간)으로 변환
     */
    fun getHourFromTimestamp(timestamp: Timestamp): Int {
        val instant = timestamp.toDate().toInstant()
        return instant.atZone(ZoneId.systemDefault()).hour
    }

    fun deleteSchedule(scheduleId: String, onComplete: () -> Unit) {
        // ID가 비어있거나 "temp_id" 같은 임시 ID이면 중단
        if (scheduleId.isBlank() || scheduleId == "temp_id") {
            Log.e("ScheduleViewModel", "유효하지 않은 ID로 삭제를 시도했습니다: $scheduleId")
            return
        }

        viewModelScope.launch {
            try {
                // Firestore에서 해당 ID의 문서를 찾아 삭제
                db.collection("schedules").document(scheduleId).delete().await()

                Log.d("ScheduleViewModel", "일정 삭제 성공: $scheduleId")

                // ★ 삭제 성공 시, onComplete 콜백을 실행
                // (이 콜백은 화면을 닫는 데 사용됩니다)
                onComplete()

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "일정 삭제 실패", e)
                // TODO: 사용자에게 "삭제에 실패했습니다" 토스트/팝업 표시
            }
        }
    }
}
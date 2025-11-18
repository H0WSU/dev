package com.example.howsu.screen.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.TodoGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class TodoViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _allTodoGroups = MutableStateFlow<List<TodoGroup>>(emptyList())

    // ★ (수정) 초기화 시점에도 '지난 일요일'을 기준으로 설정 (앱 켜자마자 다음주가 보이는 문제 해결)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _currentWeekStart = MutableStateFlow(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    )
    val currentWeekStart = _currentWeekStart.asStateFlow()

    val todoGroups = combine(_allTodoGroups, _selectedDate) { groups, date ->
        val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy. MM. dd", Locale.KOREA))

        val groupsForDate = groups.mapNotNull { group ->
            val tasksForDate = group.tasks.filter { it.date == formattedDate }
            if (tasksForDate.isNotEmpty()) {
                group.copy(tasks = tasksForDate.sortedBy { it.isChecked })
            } else null
        }

        groupsForDate.sortedBy { it.tasks.all { task -> task.isChecked } }
    }

    init {
        fetchTodoGroups()
    }

    // ★ (수정) 오늘 날짜로 리셋할 때도 '이번 주 일요일(시작일)'을 정확히 계산
    fun resetToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        // "오늘이 포함된 주의 일요일"을 계산
        _currentWeekStart.value = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    }

    private fun fetchTodoGroups() {
        db.collection("todoGroups")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("TodoViewModel", "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val groups = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(TodoGroup::class.java)?.copy(documentId = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _allTodoGroups.value = groups
                }
            }
    }

    fun deleteGroup(documentId: String) {
        if (documentId.isBlank()) return
        db.collection("todoGroups").document(documentId).delete()
    }

    fun onTaskCheckedChange(documentId: String, taskId: String, isChecked: Boolean) {
        viewModelScope.launch {
            try {
                val docRef = db.collection("todoGroups").document(documentId)
                val document = docRef.get().await()
                val group = document.toObject(TodoGroup::class.java) ?: return@launch

                val newTasks = group.tasks.map { task ->
                    if (task.id == taskId) task.copy(isChecked = isChecked)
                    else task
                }
                docRef.update("tasks", newTasks).await()
            } catch (e: Exception) {
                Log.e("TodoViewModel", "태스크 업데이트 실패", e)
            }
        }
    }

    fun onWeekDaySelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun onWeekSwipe(days: Long) {
        val newWeekStart = _currentWeekStart.value.plusDays(days)
        _currentWeekStart.value = newWeekStart

        val currentSelected = _selectedDate.value
        if (currentSelected !in newWeekStart..newWeekStart.plusDays(6)) {
            _selectedDate.value = newWeekStart.plusDays(if (days > 0) 0 else 6)
        }
    }

    fun selectDateFromPicker(millis: Long?) {
        if (millis == null) return
        val newDate = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        _selectedDate.value = newDate
        _currentWeekStart.value = newDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    }
}
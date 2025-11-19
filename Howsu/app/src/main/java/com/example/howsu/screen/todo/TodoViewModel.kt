package com.example.howsu.screen.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.TodoGroup
import com.google.firebase.auth.ktx.auth
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
        // 초기화 시점에 데이터를 가져옴
        fetchTodoGroups()
    }

    private fun fetchTodoGroups() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            _allTodoGroups.value = emptyList()
            return
        }

        // ★ [핵심 로직]
        // 가족 기능 구현 전: 내 UID를 familyId로 사용
        // 가족 기능 구현 후: API로 받아온 shared_family_id를 사용하면 됨
        val currentFamilyId = currentUser.uid

        db.collection("todoGroups")
            .whereEqualTo("familyId", currentFamilyId) // ★ 이 조건이 있어야 내 것만 보임!
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

    fun resetToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        // "오늘이 포함된 주의 일요일"을 계산
        _currentWeekStart.value = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    }

    fun deleteGroup(documentId: String) {
        if (documentId.isBlank()) return
        db.collection("todoGroups").document(documentId).delete()
    }

    fun onTaskCheckedChange(documentId: String, taskId: String, isChecked: Boolean) {
        viewModelScope.launch {
            val docRef = db.collection("todoGroups").document(documentId)
            try {
                // 'get'/'update' 대신 'transaction'을 사용
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(docRef)
                    val group = snapshot.toObject(TodoGroup::class.java)
                        ?: throw Exception("Group not found") // 그룹이 없으면 중단

                    val newTasks = group.tasks.map { task ->
                        if (task.id == taskId) {
                            task.copy(isChecked = isChecked)
                        } else {
                            task
                        }
                    }

                    transaction.update(docRef, "tasks", newTasks)

                    // (트랜잭션이 성공하면 null을 반환)
                    null
                }.await()
                // 트랜잭션이 성공하면 snapshotListener가 알아서 UI를 갱신합니다.

            } catch (e: Exception) {
                Log.e("TodoViewModel", "태스크 업데이트 실패 (Transaction)", e)
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
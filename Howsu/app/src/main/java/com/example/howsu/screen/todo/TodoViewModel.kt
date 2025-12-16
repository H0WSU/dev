package com.example.howsu.screen.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.TodoGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
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

    // 실시간 업데이트 리스너
    private var listenerRegistration: ListenerRegistration? = null

    private val _allTodoGroups = MutableStateFlow<List<TodoGroup>>(emptyList())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _currentWeekStart = MutableStateFlow(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    )
    val currentWeekStart = _currentWeekStart.asStateFlow()

    // 닉네임 표시용
    private val _userNickname = MutableStateFlow("")
    val userNickname = _userNickname.asStateFlow()

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

    // ★★★ [이 함수가 없어서 빨간 줄이 떴던 겁니다!] ★★★
    fun updateCurrentFamily(familyId: String) {
        if (familyId.isBlank()) return

        // 1. 기존 리스너 제거
        listenerRegistration?.remove()

        // 2. 리스트 초기화
        _allTodoGroups.value = emptyList()

        // 3. 새 가족 리스너 연결
        startListeningToTodos(familyId)

        // 4. 지난 할 일 정리 (백그라운드)
        checkAndMigrateOverdueTasks(familyId)
    }

    fun fetchTodoGroups() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            _allTodoGroups.value = emptyList()
            return
        }

        // 1단계: 내 유저 정보에서 닉네임 & 가족 ID 가져옴
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: "사용자"
                _userNickname.value = name

                val myFamilyId = document.getString("currentFamilyId")

                if (myFamilyId != null) {
                    // ★ 이제 빨간 줄 안 뜸 (위에 함수를 만들었으니까)
                    updateCurrentFamily(myFamilyId)
                } else {
                    Log.e("TodoViewModel", "가족 ID를 찾을 수 없습니다.")
                    _allTodoGroups.value = emptyList()
                }
            }
            .addOnFailureListener { e ->
                Log.e("TodoViewModel", "유저 정보 로드 실패", e)
            }
    }

    // 지난 할 일(미완료) 오늘로 자동 이월 함수
    private fun checkAndMigrateOverdueTasks(familyId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd", Locale.KOREA)
                val todayStr = today.format(formatter)

                val snapshot = db.collection("todoGroups")
                    .whereEqualTo("familyId", familyId)
                    .get()
                    .await()

                val batch = db.batch()
                var hasUpdates = false

                for (doc in snapshot.documents) {
                    val group = doc.toObject(TodoGroup::class.java) ?: continue
                    var groupUpdated = false

                    val newTasks = group.tasks.map { task ->
                        val taskDateStr = task.date ?: ""
                        var taskDate: LocalDate? = null
                        try {
                            taskDate = LocalDate.parse(taskDateStr, formatter)
                        } catch (e: Exception) {
                            null
                        }

                        if (task.isChecked.not() && taskDate != null && taskDate.isBefore(today)) {
                            groupUpdated = true
                            hasUpdates = true
                            task.copy(date = todayStr)
                        } else {
                            task
                        }
                    }

                    if (groupUpdated) {
                        batch.update(doc.reference, "tasks", newTasks)
                    }
                }

                if (hasUpdates) {
                    batch.commit().await()
                    Log.d("TodoViewModel", "지난 할 일들을 오늘로 이동했습니다.")
                }

            } catch (e: Exception) {
                Log.e("TodoViewModel", "할 일 이월 실패", e)
            }
        }
    }

    private fun startListeningToTodos(familyId: String) {
        listenerRegistration?.remove()

        listenerRegistration = db.collection("todoGroups")
            .whereEqualTo("familyId", familyId)
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

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun resetToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
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
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(docRef)
                    val group = snapshot.toObject(TodoGroup::class.java)
                        ?: throw Exception("Group not found")

                    val newTasks = group.tasks.map { task ->
                        if (task.id == taskId) {
                            task.copy(isChecked = isChecked)
                        } else {
                            task
                        }
                    }
                    transaction.update(docRef, "tasks", newTasks)
                    null
                }.await()
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
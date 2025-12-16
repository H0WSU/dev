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

    // 실시간 업데이트 리스너 관리
    private var listenerRegistration: ListenerRegistration? = null

    private val _allTodoGroups = MutableStateFlow<List<TodoGroup>>(emptyList())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _currentWeekStart = MutableStateFlow(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    )
    val currentWeekStart = _currentWeekStart.asStateFlow()

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

    // ★★★ [속도 개선] 순서 변경 및 최적화
    fun updateCurrentFamily(familyId: String) {
        if (familyId.isBlank()) return

        // 1. 기존 리스너 제거 및 리스트 초기화 (즉시 반응)
        listenerRegistration?.remove()
        _allTodoGroups.value = emptyList()

        // 2. [가장 중요] 데이터를 먼저 보여줍니다! (사용자가 안 기다리게)
        startListeningToTodos(familyId)

        // 3. [백그라운드] 지난 할 일 정리는 뒷단에서 천천히 수행
        // 완료되면 리스너가 알아서 화면을 한 번 더 갱신해줍니다.
        checkAndMigrateOverdueTasks(familyId)
    }

    fun fetchTodoGroups() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            _allTodoGroups.value = emptyList()
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: "사용자"
                _userNickname.value = name

                val myFamilyId = document.getString("currentFamilyId")

                if (myFamilyId != null) {
                    // 앱 처음 켤 때도 데이터 먼저 보여주고 마이그레이션
                    startListeningToTodos(myFamilyId)
                    checkAndMigrateOverdueTasks(myFamilyId)
                } else {
                    Log.e("TodoViewModel", "가족 ID를 찾을 수 없습니다.")
                    _allTodoGroups.value = emptyList()
                }
            }
            .addOnFailureListener { e ->
                Log.e("TodoViewModel", "유저 정보 로드 실패", e)
            }
    }

    // ★★★ [속도 개선] IO 스레드에서 비동기로 실행하도록 명시 (UI 버벅임 방지)
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

                        // 조건: "체크 안 됨" AND "오늘보다 이전 날짜" -> 오늘로 변경
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
        // 기존 리스너 확실히 제거
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
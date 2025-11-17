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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TodoViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _allTodoGroups = MutableStateFlow<List<TodoGroup>>(emptyList())
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    val todoGroups = combine(_allTodoGroups, _selectedDate) { groups, date ->
        val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy. MM. dd", Locale.KOREA))

        val groupsForDate = groups.mapNotNull { group ->
            val tasksForDate = group.tasks.filter { it.date == formattedDate }
            if (tasksForDate.isNotEmpty()) {
                val sortedTasks = tasksForDate.sortedBy { it.isChecked }
                group.copy(tasks = sortedTasks)
            } else {
                null
            }
        }

        groupsForDate.sortedBy { group ->
            group.tasks.all { it.isChecked }
        }
    }

    init {
        fetchTodoGroups()
    }

    private fun fetchTodoGroups() {
        db.collection("todoGroups")
            .addSnapshotListener { snapshot, error -> // ★ 실시간 리스너
                if (error != null) {
                    Log.w("TodoViewModel", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val groups = snapshot.documents.mapNotNull { doc ->
                        try {
                            // ★ documentId를 포함하여 객체로 변환
                            doc.toObject(TodoGroup::class.java)?.copy(documentId = doc.id)
                        } catch (e: Exception) {
                            Log.e("TodoViewModel", "데이터 모델 변환 실패: ${doc.id}", e)
                            null
                        }
                    }
                    _allTodoGroups.value = groups // ★ 리스너가 로컬 상태를 갱신
                }
            }
    }

    fun deleteGroup(documentId: String) {
        if (documentId.isBlank()) return
        db.collection("todoGroups").document(documentId)
            .delete()
            .addOnSuccessListener { Log.d("TodoViewModel", "삭제 성공: $documentId") }
            .addOnFailureListener { e -> Log.w("TodoViewModel", "삭제 실패", e) }
    }

    // ★★★ (대폭 수정) 태스크 체크/언체크 (깜빡임/정렬 버그 수정) ★★★
    fun onTaskCheckedChange(documentId: String, taskId: String, isChecked: Boolean) {
        // ★ 1. 로컬 상태(_allTodoGroups.value)를 직접 수정하지 않음!

        viewModelScope.launch {
            try {
                val docRef = db.collection("todoGroups").document(documentId)

                // 2. Firestore에서 현재 'tasks' 배열을 가져옴
                val document = docRef.get().await()
                val group = document.toObject(TodoGroup::class.java) ?: return@launch

                // 3. tasks 리스트에서 해당 task를 찾아 isChecked 업데이트
                val newTasks = group.tasks.map { task ->
                    if (task.id == taskId) {
                        task.copy(isChecked = isChecked)
                    } else {
                        task
                    }
                }

                // 4. Firestore 문서의 'tasks' 필드만 업데이트
                docRef.update("tasks", newTasks).await()
                Log.d("TodoViewModel", "태스크 업데이트 성공: $taskId")

                // 5. 업데이트가 성공하면, 'fetchTodoGroups'의 실시간 리스너가
                //    자동으로 변경을 감지하고 _allTodoGroups.value를 갱신함.
                //    (따라서 로컬 상태를 여기서 건드릴 필요가 없음)

            } catch (e: Exception) {
                Log.e("TodoViewModel", "태스크 업데이트 실패", e)
            }
        }
    }

    // ★ (수정) 7일씩 변경
    fun onDateChange(days: Long) {
        _selectedDate.update {
            it.plusDays(days)
        }
    }

    fun onWeekDaySelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectDateFromPicker(millis: Long?) {
        if (millis == null) return
        _selectedDate.value = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
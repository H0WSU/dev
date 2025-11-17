package com.example.howsu.screen.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.Pet
import com.example.howsu.data.model.Task
import com.example.howsu.data.model.TodoGroup
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreateTodoViewModel : ViewModel() {

    private val db = Firebase.firestore
    private var currentTodoDocumentId: String? = null
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    // --- (기존 State - 변경 없음) ---
    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()
    private val _selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedMember: StateFlow<FamilyMember?> = _selectedMember.asStateFlow()
    private val _taskTitle = MutableStateFlow("")
    val taskTitle: StateFlow<String> = _taskTitle.asStateFlow()
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()
    private val _isDatePickerVisible = MutableStateFlow(false)
    val isDatePickerVisible: StateFlow<Boolean> = _isDatePickerVisible.asStateFlow()
    private val _allPets = MutableStateFlow<List<Pet>>(emptyList())
    val allPets: StateFlow<List<Pet>> = _allPets.asStateFlow()
    private val _selectedPets = MutableStateFlow<List<Pet>>(emptyList())
    val selectedPets: StateFlow<List<Pet>> = _selectedPets.asStateFlow()
    private val _isPetDropdownVisible = MutableStateFlow(false)
    val isPetDropdownVisible: StateFlow<Boolean> = _isPetDropdownVisible.asStateFlow()

    // (기존) initialize
    fun initialize(documentId: String?) {
        viewModelScope.launch {
            loadInitialData()
            if (documentId != null) {
                currentTodoDocumentId = documentId
                _isEditMode.value = true
                loadTodoForEdit(documentId)
            } else {
                _isEditMode.value = false
                if (_familyMembers.value.isNotEmpty()) {
                    _selectedMember.value = _familyMembers.value.first()
                }
            }
        }
    }

    // (기존) loadInitialData
    private suspend fun loadInitialData() {
        val dummyFamily = listOf(
            FamilyMember(userId = "user_id_1", relationship = "언니", profileImageUrl = null, nickName = "이구역의짱"),
            FamilyMember(userId = "user_id_2", relationship = "엄마", profileImageUrl = null, nickName = "엄마2"),
            FamilyMember(userId = "user_id_3", relationship = "형", profileImageUrl = null, nickName = "형2")
        )
        _familyMembers.value = dummyFamily

        val dummyPets = listOf(
            Pet(petId = "pet_id_1", name = "자몽", profileImageUrl = null),
            Pet(petId = "pet_id_2", name = "레몬", profileImageUrl = null),
            Pet(petId = "pet_id_3", name = "망고", profileImageUrl = null),
            Pet(petId = "pet_id_4", name = "수박", profileImageUrl = null),
            Pet(petId = "pet_id_5", name = "키위", profileImageUrl = null)
        )
        _allPets.value = dummyPets
    }

    private suspend fun loadTodoForEdit(documentId: String) {
        try {
            val doc = db.collection("todoGroups").document(documentId).get().await()
            val group = doc.toObject<TodoGroup>()
            if (group != null) {
                _selectedMember.value = _familyMembers.value.find { it.userId == group.assigneeId }
                _selectedPets.value = _allPets.value.filter { group.petNames.contains(it.name) }

                // ★★★ (수정) 여기서 제목을 ""로 비우는 대신, 그룹의 '첫 번째' 태스크 제목을 불러옵니다. ★★★
                val taskToEdit = group.tasks.firstOrNull() // 그룹의 첫 번째 태스크
                if (taskToEdit != null) {
                    _taskTitle.value = taskToEdit.title ?: ""
                } else {
                    _taskTitle.value = "" // 태스크가 없는 그룹이면 비워둠
                }

                // (선택) 날짜도 첫 번째 태스크의 날짜로 맞출 수 있습니다.
                val firstTaskDate = taskToEdit?.date
                if (firstTaskDate != null) {
                    try {
                        // 날짜 형식이 "yyyy. MM. dd"이므로 파싱합니다.
                        val formatter = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA)
                        _selectedDate.value = formatter.parse(firstTaskDate)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        _selectedDate.value = System.currentTimeMillis() // 파싱 실패 시 오늘 날짜
                    }
                } else {
                    _selectedDate.value = System.currentTimeMillis() // 태스크가 없으면 오늘 날짜
                }
            }
        } catch (e: Exception) {
            Log.e("CreateTodoVM", "수정할 할 일($documentId) 로드 실패", e)
        }
    }

    // --- (기존 UI 이벤트 핸들러 - 변경 없음) ---
    fun onMemberSelected(member: FamilyMember) { _selectedMember.value = member }
    fun onTaskTitleChanged(newTitle: String) { _taskTitle.value = newTitle.take(20) }
    fun onDatePickerClicked() { _isDatePickerVisible.value = true }
    fun onDateSelected(epochMillis: Long?) {
        epochMillis?.let { _selectedDate.value = it }
        _isDatePickerVisible.value = false
    }
    fun onDatePickerDismissed() { _isDatePickerVisible.value = false }
    fun onPetDropdownClicked() { _isPetDropdownVisible.value = true }
    fun onPetDropdownDismissed() { _isPetDropdownVisible.value = false }
    fun onPetSelected(pet: Pet) {
        if (!_selectedPets.value.any { it.petId == pet.petId }) {
            _selectedPets.update { currentList -> currentList + pet }
        }
        _isPetDropdownVisible.value = false
    }
    fun onPetTagRemoved(pet: Pet) {
        _selectedPets.update { currentList ->
            currentList.filterNot { it.petId == pet.petId }
        }
    }

    // ★★★ (대폭 수정) saveTodo (펫 병합 로직 수정) ★★★
    fun saveTodo(onComplete: () -> Unit) {
        val assignee = _selectedMember.value
        val title = _taskTitle.value
        val dateInMillis = _selectedDate.value

        if (assignee == null || title.isBlank()) {
            return
        }

        val formattedDate = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).format(Date(dateInMillis))

        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            date = formattedDate,
            isChecked = false
        )

        viewModelScope.launch {
            try {
                if (_isEditMode.value && currentTodoDocumentId != null) {
                    // --- 수정 모드 (Task 추가 + Pet 병합) ---
                    val docRef = db.collection("todoGroups").document(currentTodoDocumentId!!)

                    // 1. Task Map 변환
                    val taskMap = mapOf(
                        "id" to newTask.id,
                        "title" to newTask.title,
                        "date" to newTask.date,
                        "isChecked" to newTask.isChecked
                    )

                    // 2. (수정) 펫 이름 목록 (중복 제거)
                    // (수정 모드에서는 _selectedPets가 기존+신규 펫을 모두 들고 있음)
                    val mergedPetNames = _selectedPets.value.map { it.name }.distinct()

                    // 3. 'tasks'는 추가(arrayUnion), 'petNames'는 덮어쓰기(set)
                    docRef.update(
                        "tasks", FieldValue.arrayUnion(taskMap),
                        "petNames", mergedPetNames
                    ).await()

                    Log.d("CreateTodoVM", "기존 할 일 그룹에 Task/Pet 추가 성공")

                } else {
                    // --- 생성 모드 (새 그룹 생성) ---
                    val newTodoGroup = TodoGroup(
                        assigneeId = assignee.userId,
                        assigneeName = assignee.relationship,
                        assigneeProfileRes = null,
                        tasks = listOf(newTask),
                        petNames = _selectedPets.value.map { it.name }
                    )

                    db.collection("todoGroups").add(newTodoGroup).await()
                    Log.d("CreateTodoVM", "새 할 일 그룹 생성 성공")
                }
                onComplete()

            } catch (e: Exception) {
                Log.e("CreateTodoVM", "할 일 저장/수정 실패", e)
            }
        }
    }
}
package com.example.howsu.screen.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.Pet
import com.example.howsu.data.model.Task
import com.example.howsu.data.model.TodoGroup
import com.google.firebase.auth.ktx.auth
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
    private var currentTaskId: String? = null // ★ (신규) 수정할 태스크의 ID
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
                // (수정) 생성 모드일 때 변수 초기화
                currentTodoDocumentId = null
                currentTaskId = null
                _isEditMode.value = false
                _taskTitle.value = "" // (추가)
                _selectedPets.value = emptyList() // (추가)
                _selectedDate.value = System.currentTimeMillis() // (추가)
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

    // ★★★ (수정) loadTodoForEdit (태스크 ID 저장) ★★★
    private suspend fun loadTodoForEdit(documentId: String) {
        try {
            val doc = db.collection("todoGroups").document(documentId).get().await()
            val group = doc.toObject<TodoGroup>()
            if (group != null) {
                _selectedMember.value = _familyMembers.value.find { it.userId == group.assigneeId }
                _selectedPets.value = _allPets.value.filter { group.petNames.contains(it.name) }

                // (수정) 그룹의 '첫 번째' 태스크를 수정 대상으로 간주
                val taskToEdit = group.tasks.firstOrNull()
                if (taskToEdit != null) {
                    currentTaskId = taskToEdit.id // ★ (신규) 태스크 ID 저장
                    _taskTitle.value = taskToEdit.title ?: ""
                } else {
                    currentTaskId = null
                    _taskTitle.value = ""
                }

                val firstTaskDate = taskToEdit?.date
                if (firstTaskDate != null) {
                    try {
                        val formatter = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA)
                        _selectedDate.value = formatter.parse(firstTaskDate)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        _selectedDate.value = System.currentTimeMillis()
                    }
                } else {
                    _selectedDate.value = System.currentTimeMillis()
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

    fun saveTodo(onComplete: () -> Unit) {
        val assignee = _selectedMember.value
        val title = _taskTitle.value
        val dateInMillis = _selectedDate.value

        val currentUser = Firebase.auth.currentUser

        // 유효성 검사 및 familyId 확보
        val myFamilyId = currentUser?.uid ?: return

        if (assignee == null || title.isBlank()) {
            return
        }
        val formattedDate = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).format(Date(dateInMillis))

        viewModelScope.launch {
            try {
                // --- 수정 모드 ---
                // (주의: 이 로직은 그룹의 '첫 번째' 태스크만 수정하는 한계가 있음)
                if (_isEditMode.value && currentTodoDocumentId != null && currentTaskId != null) {

                    val docRef = db.collection("todoGroups").document(currentTodoDocumentId!!)
                    val document = docRef.get().await()
                    val group = document.toObject<TodoGroup>() ?: return@launch

                    // 1. (수정) 기존 tasks 리스트에서 'currentTaskId'를 찾아 제목/날짜를 업데이트
                    val updatedTasks = group.tasks.map { task ->
                        if (task.id == currentTaskId) {
                            task.copy(title = title, date = formattedDate) // ★ 수정
                        } else {
                            task // ★ 나머지는 그대로 둠
                        }
                    }

                    // 2. 펫 이름 목록
                    val mergedPetNames = _selectedPets.value.map { it.name }.distinct()

                    // 3. (수정) 담당자
                    val finalAssigneeId = assignee.userId
                    val finalAssigneeName = assignee.relationship

                    docRef.update(
                        "tasks", updatedTasks,
                        "petNames", mergedPetNames,
                        "assigneeId", assignee.userId,
                        "assigneeName", assignee.relationship,
                        "familyId", myFamilyId // 혹시 모르니 familyId도 업데이트
                    ).await()

                    Log.d("CreateTodoVM", "기존 할 일 그룹 수정 성공")

                } else {
                    // --- 생성 모드 (새 그룹 생성) ---
                    val newTask = Task(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        date = formattedDate,
                        isChecked = false
                    )

                    val newTodoGroup = TodoGroup(
                        familyId = myFamilyId,
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
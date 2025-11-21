package com.example.howsu.screen.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.Pet
import com.example.howsu.data.model.Task
import com.example.howsu.data.model.TodoGroup
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
    private var currentTaskId: String? = null
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    // State Flows
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

    fun initialize(documentId: String?) {
        viewModelScope.launch {
            loadInitialData()
            if (documentId != null) {
                currentTodoDocumentId = documentId
                _isEditMode.value = true
                loadTodoForEdit(documentId)
            } else {
                currentTodoDocumentId = null
                currentTaskId = null
                _isEditMode.value = false
                _taskTitle.value = ""
                _selectedPets.value = emptyList()
                _selectedDate.value = System.currentTimeMillis()
                if (_familyMembers.value.isNotEmpty()) {
                    _selectedMember.value = _familyMembers.value.first()
                }
            }
        }
    }

    private suspend fun loadInitialData() {
        // ★ [수정됨] FamilyMember 생성자에 familyId 추가!
        // (FamilyMember 데이터 클래스가 변경되었기 때문에 여기도 맞춰줘야 함)
        val dummyFamily = listOf(
            FamilyMember(
                userId = "user_id_1",
                familyId = "test_family", // 추가됨
                relationship = "언니",
                profileImageUrl = null,
                nickName = "이구역의짱"
            ),
            FamilyMember(
                userId = "user_id_2",
                familyId = "test_family", // 추가됨
                relationship = "엄마",
                profileImageUrl = null,
                nickName = "엄마2"
            ),
            FamilyMember(
                userId = "user_id_3",
                familyId = "test_family", // 추가됨
                relationship = "형",
                profileImageUrl = null,
                nickName = "형2"
            )
        )
        _familyMembers.value = dummyFamily

        // ★ [확인 필요] Pet 데이터 모델도 패키지가 바뀌었는지 확인하세요.
        // Pet 클래스 생성자에 맞춰서 아래 코드도 수정이 필요할 수 있습니다.
        // 일단 기존 코드를 유지합니다.
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

                val taskToEdit = group.tasks.firstOrNull()
                if (taskToEdit != null) {
                    currentTaskId = taskToEdit.id
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
            Log.e("CreateTodoVM", "수정할 할 일 로드 실패", e)
        }
    }

    // --- UI Events ---
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

        if (assignee == null || title.isBlank()) {
            return
        }

        // ★ [중요 수정] 내 UID가 아니라, 선택된 멤버(assignee)가 속한 가족 ID를 사용해야 함
        // FamilyMember 객체 안에 familyId가 들어있으므로 그걸 사용
        val myFamilyId = assignee.familyId

        val formattedDate = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).format(Date(dateInMillis))

        viewModelScope.launch {
            try {
                if (_isEditMode.value && currentTodoDocumentId != null && currentTaskId != null) {
                    // --- 수정 모드 ---
                    val docRef = db.collection("todoGroups").document(currentTodoDocumentId!!)
                    val document = docRef.get().await()
                    val group = document.toObject<TodoGroup>() ?: return@launch

                    val updatedTasks = group.tasks.map { task ->
                        if (task.id == currentTaskId) {
                            task.copy(title = title, date = formattedDate)
                        } else {
                            task
                        }
                    }
                    val mergedPetNames = _selectedPets.value.map { it.name }.distinct()

                    docRef.update(
                        "tasks", updatedTasks,
                        "petNames", mergedPetNames,
                        "assigneeId", assignee.userId,
                        "assigneeName", assignee.relationship, // 또는 assignee.nickName (기획에 따라)
                        "familyId", myFamilyId
                    ).await()

                    Log.d("CreateTodoVM", "수정 성공")

                } else {
                    // --- 생성 모드 ---
                    val newTask = Task(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        date = formattedDate,
                        isChecked = false
                    )

                    val newTodoGroup = TodoGroup(
                        familyId = myFamilyId,
                        assigneeId = assignee.userId,
                        assigneeName = assignee.relationship, // 또는 assignee.nickName
                        assigneeProfileRes = null,
                        tasks = listOf(newTask),
                        petNames = _selectedPets.value.map { it.name }
                    )

                    db.collection("todoGroups").add(newTodoGroup).await()
                    Log.d("CreateTodoVM", "생성 성공")
                }
                onComplete()

            } catch (e: Exception) {
                Log.e("CreateTodoVM", "저장 실패", e)
            }
        }
    }
}
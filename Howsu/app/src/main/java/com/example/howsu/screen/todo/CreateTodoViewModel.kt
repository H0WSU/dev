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
    private val auth = Firebase.auth

    private var currentTodoDocumentId: String? = null
    private var currentTaskId: String? = null
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    // --- 상태 변수들 ---
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

    // ★ 진짜 가족 ID 저장용
    private var realFamilyId: String? = null

    fun initialize(documentId: String?) {
        viewModelScope.launch {
            // 1. 진짜 데이터 로드 (가족 멤버, 내 정보)
            loadRealData()

            if (documentId != null) {
                currentTodoDocumentId = documentId
                _isEditMode.value = true
                loadTodoForEdit(documentId)
            } else {
                // 생성 모드 초기화
                currentTodoDocumentId = null
                currentTaskId = null
                _isEditMode.value = false
                _taskTitle.value = ""
                _selectedPets.value = emptyList()
                _selectedDate.value = System.currentTimeMillis()
            }
        }
    }

    // ★★★ [핵심] 진짜 내 가족 정보 가져오기
    private suspend fun loadRealData() {
        val user = auth.currentUser ?: return

        try {
            // 1. 내 User 정보에서 currentFamilyId 확인
            val userDoc = db.collection("users").document(user.uid).get().await()
            val myFamilyId = userDoc.getString("currentFamilyId")

            if (myFamilyId != null) {
                realFamilyId = myFamilyId

                // 2. 그 가족의 멤버들 가져오기
                val membersSnapshot = db.collection("families")
                    .document(myFamilyId)
                    .collection("members")
                    .get()
                    .await()

                val members = membersSnapshot.documents.mapNotNull { doc ->
                    doc.toObject<FamilyMember>()
                }
                _familyMembers.value = members

                // ★★★ 3. [자동 선택] 멤버 리스트에서 '나'를 찾아 기본 선택
                // 이게 돼야 "누가" 칸이 채워지고 저장이 됩니다!
                if (members.isNotEmpty()) {
                    val me = members.find { it.userId == user.uid }
                    _selectedMember.value = me ?: members.first()
                }

                // 4. (임시) 펫 데이터는 아직 DB에 없다면 더미 사용 (나중에 수정 필요)
                if (_allPets.value.isEmpty()) {
                    _allPets.value = listOf(
                        Pet(petId = "p1", name = "자몽", profileImageUrl = null),
                        Pet(petId = "p2", name = "루비", profileImageUrl = null)
                    )
                }
            } else {
                Log.e("CreateTodoVM", "가족 ID가 없습니다. 가족 등록을 먼저 해주세요.")
            }
        } catch (e: Exception) {
            Log.e("CreateTodoVM", "데이터 로드 실패: ${e.message}")
        }
    }

    // 수정 모드 로드
    private suspend fun loadTodoForEdit(documentId: String) {
        try {
            val doc = db.collection("todoGroups").document(documentId).get().await()
            val group = doc.toObject<TodoGroup>()
            if (group != null) {
                // 저장된 담당자 ID로 멤버 찾기
                _selectedMember.value = _familyMembers.value.find { it.userId == group.assigneeId }

                // 펫 선택 복원
                _selectedPets.value = _allPets.value.filter { group.petNames.contains(it.name) }

                val taskToEdit = group.tasks.firstOrNull()
                if (taskToEdit != null) {
                    currentTaskId = taskToEdit.id
                    _taskTitle.value = taskToEdit.title ?: ""

                    // 날짜 복원
                    try {
                        val formatter = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA)
                        _selectedDate.value =
                            formatter.parse(taskToEdit.date)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        _selectedDate.value = System.currentTimeMillis()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CreateTodoVM", "수정 데이터 로드 실패", e)
        }
    }

    // --- UI 이벤트 ---
    fun onMemberSelected(member: FamilyMember) {
        _selectedMember.value = member
    }

    fun onTaskTitleChanged(newTitle: String) {
        _taskTitle.value = newTitle.take(20)
    }

    fun onDatePickerClicked() {
        _isDatePickerVisible.value = true
    }

    fun onDateSelected(epochMillis: Long?) {
        epochMillis?.let { _selectedDate.value = it }
        _isDatePickerVisible.value = false
    }

    fun onDatePickerDismissed() {
        _isDatePickerVisible.value = false
    }

    fun onPetDropdownClicked() {
        _isPetDropdownVisible.value = true
    }

    fun onPetDropdownDismissed() {
        _isPetDropdownVisible.value = false
    }

    fun onPetSelected(pet: Pet) {
        if (!_selectedPets.value.any { it.petId == pet.petId }) {
            _selectedPets.update { currentList -> currentList + pet }
        }
        _isPetDropdownVisible.value = false
    }

    fun onPetTagRemoved(pet: Pet) {
        _selectedPets.update { currentList -> currentList.filterNot { it.petId == pet.petId } }
    }

    fun saveTodo(onComplete: () -> Unit) {
        val assignee = _selectedMember.value
        val title = _taskTitle.value
        val dateInMillis = _selectedDate.value

        val finalFamilyId = realFamilyId ?: assignee?.familyId

        if (assignee == null || title.isBlank() || finalFamilyId == null) {
            Log.e("CreateTodoVM", "저장 실패: 정보 누락")
            return
        }

        val formattedDate =
            SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).format(Date(dateInMillis))

        viewModelScope.launch {
            try {
                if (_isEditMode.value && currentTodoDocumentId != null && currentTaskId != null) {
                    // [수정]
                    val docRef = db.collection("todoGroups").document(currentTodoDocumentId!!)
                    val groupSnapshot = docRef.get().await()
                    val group = groupSnapshot.toObject<TodoGroup>() ?: return@launch

                    val updatedTasks = group.tasks.map { task ->
                        if (task.id == currentTaskId) task.copy(title = title, date = formattedDate)
                        else task
                    }

                    docRef.update(
                        "tasks", updatedTasks,
                        "petNames", _selectedPets.value.map { it.name },
                        "assigneeId", assignee.userId,
                        "assigneeName", assignee.relationship,

                        // ★★★ [추가] 프로필 사진 주소 저장!
                        "assigneeProfileRes", assignee.profileImageUrl,

                        "familyId", finalFamilyId
                    ).await()

                } else {
                    // [생성]
                    val newTask = Task(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        date = formattedDate,
                        isChecked = false
                    )

                    val newTodoGroup = TodoGroup(
                        familyId = finalFamilyId,
                        assigneeId = assignee.userId,
                        assigneeName = assignee.relationship,

                        // ★★★ [추가] 프로필 사진 주소 저장!
                        assigneeProfileRes = assignee.profileImageUrl,

                        tasks = listOf(newTask),
                        petNames = _selectedPets.value.map { it.name }
                    )

                    db.collection("todoGroups").add(newTodoGroup).await()
                }
                onComplete()

            } catch (e: Exception) {
                Log.e("CreateTodoVM", "Firestore 저장 오류", e)
            }
        }
    }
}
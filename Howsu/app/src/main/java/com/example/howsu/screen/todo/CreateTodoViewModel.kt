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

    // ★★★ [수정] 다중 선택 리스트
    private val _selectedMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val selectedMembers: StateFlow<List<FamilyMember>> = _selectedMembers.asStateFlow()

    // (selectedMember 하나짜리는 삭제함)

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

    private var realFamilyId: String? = null

    fun initialize(documentId: String?) {
        viewModelScope.launch {
            loadRealData()

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
            }
        }
    }

    private suspend fun loadRealData() {
        val user = auth.currentUser ?: return

        try {
            // 1. 내 정보에서 가족 ID 찾기
            val userDoc = db.collection("users").document(user.uid).get().await()
            val myFamilyId = userDoc.getString("currentFamilyId")

            if (myFamilyId != null) {
                realFamilyId = myFamilyId

                // 2. 가족 방에 있는 멤버 명단 가져오기
                val membersSnapshot = db.collection("families")
                    .document(myFamilyId)
                    .collection("members")
                    .get()
                    .await()

                val updatedMemberList = mutableListOf<FamilyMember>()

                // ★★★ [핵심 수정] 모든 멤버의 최신 정보를 'users' 컬렉션에서 다시 확인!
                for (doc in membersSnapshot) {
                    var member = doc.toObject<FamilyMember>()

                    try {
                        // 이 멤버의 진짜(원본) 유저 정보 조회
                        val realUserDoc = db.collection("users").document(member.userId).get().await()

                        // 최신 사진과 닉네임 가져오기
                        val realProfileUrl = realUserDoc.getString("profileImageUrl")
                        val realName = realUserDoc.getString("name")

                        // 가족 방 정보보다 원본 정보(users)를 우선 적용
                        member = member.copy(
                            profileImageUrl = realProfileUrl ?: member.profileImageUrl,
                            nickName = realName ?: member.nickName
                        )
                    } catch (e: Exception) {
                        // 유저 정보 조회 실패 시 기존 가족 방 정보 유지
                        Log.w("CreateTodoVM", "유저 정보 동기화 실패: ${member.userId}")
                    }

                    updatedMemberList.add(member)
                }

                // 3. 정렬: 나를 맨 앞으로
                val sortedMembers = updatedMemberList.sortedByDescending { it.userId == user.uid }
                _familyMembers.value = sortedMembers

                // 4. 자동 선택 (내가 있으면 나, 없으면 첫 번째)
                if (_selectedMembers.value.isEmpty() && sortedMembers.isNotEmpty()) {
                    val me = sortedMembers.find { it.userId == user.uid }
                    _selectedMembers.value = listOf(me ?: sortedMembers.first())
                }

                // 5. 펫 데이터 로드
                val petsSnapshot = db.collection("families")
                    .document(myFamilyId)
                    .collection("pets")
                    .get()
                    .await()

                val realPets = petsSnapshot.documents.mapNotNull { doc ->
                    doc.toObject<Pet>()?.copy(petId = doc.id)
                }

                if (realPets.isNotEmpty()) {
                    _allPets.value = realPets
                } else {
                    _allPets.value = emptyList()
                }

            } else {
                Log.e("CreateTodoVM", "가족 ID가 없습니다.")
            }
        } catch (e: Exception) {
            Log.e("CreateTodoVM", "데이터 로드 실패", e)
        }
    }

    private suspend fun loadTodoForEdit(documentId: String) {
        try {
            val doc = db.collection("todoGroups").document(documentId).get().await()
            val group = doc.toObject<TodoGroup>()
            if (group != null) {
                // 저장된 ID들로 멤버들 찾아서 리스트 복원
                val savedMembers = _familyMembers.value.filter { member ->
                    group.assigneeIds.contains(member.userId)
                }
                _selectedMembers.value = savedMembers

                _selectedPets.value = _allPets.value.filter { group.petNames.contains(it.name) }

                val taskToEdit = group.tasks.firstOrNull()
                if (taskToEdit != null) {
                    currentTaskId = taskToEdit.id
                    _taskTitle.value = taskToEdit.title ?: ""

                    try {
                        val formatter = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA)
                        _selectedDate.value = formatter.parse(taskToEdit.date)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        _selectedDate.value = System.currentTimeMillis()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CreateTodoVM", "수정 데이터 로드 실패", e)
        }
    }

    // 멤버 선택 토글 (최소 1명 유지 로직 포함)
    fun onMemberSelected(member: FamilyMember) {
        _selectedMembers.update { currentList ->
            if (currentList.any { it.userId == member.userId }) {
                // 이미 있으면 제거하되, 1명 남았을 땐 제거 안 함
                if (currentList.size > 1) {
                    currentList.filter { it.userId != member.userId }
                } else {
                    currentList // 그대로 유지 (최소 1명)
                }
            } else {
                // 없으면 추가
                currentList + member
            }
        }
    }

    fun onTaskTitleChanged(newTitle: String) {
        _taskTitle.value = newTitle.take(20)
    }

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
        _selectedPets.update { currentList -> currentList.filterNot { it.petId == pet.petId } }
    }

    fun saveTodo(onComplete: () -> Unit) {
        val assignees = _selectedMembers.value // 리스트
        val title = _taskTitle.value
        val dateInMillis = _selectedDate.value
        val finalFamilyId = realFamilyId ?: assignees.firstOrNull()?.familyId

        if (assignees.isEmpty() || title.isBlank() || finalFamilyId == null) {
            Log.e("CreateTodoVM", "저장 불가: 필수 정보 누락")
            return
        }

        val formattedDate = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).format(Date(dateInMillis))

        // ★ 저장할 데이터 리스트 준비
        val assigneeIds = assignees.map { it.userId }
        val assigneeNames = assignees.map { it.relationship }
        val assigneeProfileUrls = assignees.map { it.profileImageUrl }

        viewModelScope.launch {
            try {
                if (_isEditMode.value && currentTodoDocumentId != null && currentTaskId != null) {
                    // [수정 모드]
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
                        "petProfileUrls", _selectedPets.value.map { it.profileImageUrl },

                        // 리스트로 업데이트
                        "assigneeIds", assigneeIds,
                        "assigneeNames", assigneeNames,
                        "assigneeProfileUrls", assigneeProfileUrls,

                        "familyId", finalFamilyId
                    ).await()

                } else {
                    // [생성 모드]
                    val newTask = Task(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        date = formattedDate,
                        isChecked = false
                    )

                    val newTodoGroup = TodoGroup(
                        familyId = finalFamilyId,

                        // 리스트로 저장
                        assigneeIds = assigneeIds,
                        assigneeNames = assigneeNames,
                        assigneeProfileUrls = assigneeProfileUrls,

                        tasks = listOf(newTask),
                        petNames = _selectedPets.value.map { it.name },
                        petProfileUrls = _selectedPets.value.map { it.profileImageUrl }
                    )

                    db.collection("todoGroups").add(newTodoGroup).await()

                    // (알림 로직 자리)
                }
                onComplete()

            } catch (e: Exception) {
                Log.e("CreateTodoVM", "Firestore 저장 오류", e)
            }
        }
    }
}
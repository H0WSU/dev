package com.example.howsu.screen.todo

// ★ 1. 날짜 포맷을 위한 임포트 3줄
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.Pet
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateTodoViewModel : ViewModel() {

    private val db = Firebase.firestore

    // --- (모든 State 변수와 init, loadInitialData, 이벤트 핸들러는 동일) ---
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

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // (임시) 더미 데이터로 가족 목록
            val dummyFamily = listOf(
                FamilyMember(userId = "user_id_1", relationship = "언니", profileImageUrl = null),
                FamilyMember(userId = "user_id_2", relationship = "엄마", profileImageUrl = null),
                FamilyMember(userId = "user_id_3", relationship = "형", profileImageUrl = null)
            )
            _familyMembers.value = dummyFamily
            if (dummyFamily.isNotEmpty()) {
                _selectedMember.value = dummyFamily.first()
            }
            // (임시) 더미 데이터로 반려동물 목록
            val dummyPets = listOf(
                Pet(petId = "pet_id_1", name = "자몽", profileImageUrl = null),
                Pet(petId = "pet_id_2", name = "레몬", profileImageUrl = null),
                Pet(petId = "pet_id_3", name = "망고", profileImageUrl = null)
            )
            _allPets.value = dummyPets
        }
    }

    // --- (UI 이벤트 핸들러들은 동일) ---
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

    /**
     * '투두 생성 완료' 버튼 클릭 (★ 여기가 수정되었습니다 ★)
     */
    fun createTodo(onComplete: () -> Unit) {
        val assignee = _selectedMember.value
        val title = _taskTitle.value
        val dateInMillis = _selectedDate.value
        val pets = _selectedPets.value

        if (assignee == null || title.isBlank()) {
            return
        }

        // --- '투두 등록' 로직 ---

        // ★ 2. 날짜 포맷 변경 (Long -> "yyyy. MM. dd")
        val formattedDate = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA).format(Date(dateInMillis))

        // ★ 3. 담당자 이름 포맷 변경 ("언니" -> "언니(이)가")
        val formattedAssigneeName = when (assignee.relationship) {
            "언니" -> "언니(이)가"
            "엄마" -> "엄마(이)가"
            "형" -> "형(이)가"
            else -> "${assignee.relationship}(이)가"
        }

        // ★ 4. Task 맵 생성 (Task 모델에 맞게)
        val newTasks = listOf(
            mapOf(
                "id" to (0..10000).random(), // 임시 ID
                "title" to title,
                "date" to formattedDate, // ★ 포맷된 날짜 사용
                "isChecked" to false
            )
        )

        // ★ 5. TodoGroup 맵 생성 (TodoGroup 모델에 맞게)
        val newTodoGroup = mapOf(
            "id" to (0..10000).random(), // 임시 ID
            "assigneeName" to formattedAssigneeName, // ★ 포맷된 이름 사용
            "assigneeProfileRes" to null,
            "tasks" to newTasks
        )

        // ★ 6. Firestore에 저장
        db.collection("todoGroups")
            .add(newTodoGroup)
            .addOnSuccessListener {
                println("--- 할 일 생성 성공 ---")
                onComplete() // ★ 성공 시에만 화면 이동
            }
            .addOnFailureListener { e ->
                println("--- 할 일 생성 실패 ---: $e")
            }
    }
}
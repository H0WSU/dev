package com.example.howsu.screen.mypage

// Firebase Coroutines 확장 함수를 사용하기 위해 import
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


data class ProfileUiState(
    val uid: String = "",
    val name: String = "",  // 이름
    val email: String = "", // 이메일
    val profileImageUrl: String? = null,
    val newProfileImageUri: Uri? = null, // 새 이미지의 로컬 URI

    val isLoading: Boolean = false, // 로딩 중
    val error: String? = null, // 에러 메시지
    val isEditing: Boolean = false, // 현재 편집 모드인지 확인

    // 가족 id에 따라 역할 달라짐.
    val currentFamilyId: String? = null,
    val relationship: String = "관계 정보 없음", // 반려동물과의 관계
    val originalRelationship: String = "관계 정보 없음", // 취소 시 복구용
    val isRelationLoading: Boolean = false, // 관계 로딩 상태
    // 관계 드롭 다운 항목
    val relationshipOptions: List<String> = listOf("엄마", "아빠", "언니", "누나", "오빠", "형", "동생")
)


// Context가 필요 없는 기본 ViewModel로 복구
class EditProfileViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage: FirebaseStorage = Firebase.storage

    // 편집 취소 시 복구를 위한 원본 데이터 저장
    private var originalUser: User? = null

    // UI 상태 관리 (초기 로딩 상태 true)
    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // ViewModel 초기화 시 사용자 정보 로드 시작
        loadUserProfile()
    }

    // ----------------------------------------------------
    // 데이터 업데이트 함수 (변경 없음)
    // ----------------------------------------------------

    fun updateProfileImageUri(uri: Uri?){
        _uiState.update { it.copy(newProfileImageUri = uri) }
    }

    fun toggledEditMode(enable: Boolean) {
        _uiState.update { it.copy(isEditing = enable) }
    }

    fun updateName(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun updateRelationship(newRelationship: String) {
        _uiState.update { it.copy(relationship = newRelationship) }
    }

    fun cancelEditing() {
        if (originalUser != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    name = originalUser!!.name,
                    profileImageUrl = originalUser!!.profileImageUrl,
                    relationship = currentState.originalRelationship, // 관계
                    newProfileImageUri = null, // 임시 URI 제거
                    isEditing = false  // 편집 모드 끔
                )
            }
        } else {
            _uiState.update { it.copy(isEditing = false, newProfileImageUri = null) }
        }
    }


    // < 데이터 로드/업데이트 및 예외 처리 함수 >

    // Firebase에서 사용자 정보를 로드함 (변경 없음)
    fun loadUserProfile() {
        val currentUser = auth.currentUser
        val currentUid = auth.currentUser?.uid ?: return
        val userEmail = currentUser?.email ?: ""

        _uiState.update { it.copy(isLoading = true) }

        db.collection("users").document(currentUid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                val familyId = document.getString("currentFamilyId")
                if (user != null) {
                    originalUser = user
                    _uiState.update { currentState ->
                        currentState.copy(
                            uid = user.uid,
                            name = user.name,
                            email = userEmail,
                            profileImageUrl = user.profileImageUrl,
                            currentFamilyId = familyId,
                            isLoading = false,
                            isEditing = false
                        )
                    }
                    loadRelationship(currentUid, familyId)
                } else {
                    handleFailure("사용자 정보를 찾을 수 없습니다.")
                }
            }
            .addOnFailureListener { exception ->
                handleFailure("데이터 로드 실패: ${exception.message}")
            }
    }

    private fun handleFailure(errorMessage: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = errorMessage
            )
        }
    }

    private fun loadRelationship(uid: String, familyId: String?) {
        if (familyId.isNullOrBlank()) {
            _uiState.update { it.copy(relationship = "가족에 소속되지 않음", originalRelationship = "가족에 소속되지 않음", isRelationLoading = false) }
            return
        }

        _uiState.update { it.copy(isRelationLoading = true) }

        db.collection("families").document(familyId)
            .collection("members").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val relationship = document.getString("relationship") ?: "관계 설정 필요"

                _uiState.update { currentState ->
                    currentState.copy(
                        relationship = relationship,
                        originalRelationship = relationship,
                        isRelationLoading = false
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EditProfileViewModel", "관계 정보 로드 실패: ${exception.message}")
                _uiState.update { it.copy(isRelationLoading = false, error = "관계 로드 실패") }
            }
    }


    /**
     * Firestore에 이름 및 이미지 URL + 관계를 병렬로 저장하는 함수
     * Coroutines의 async와 awaitAll을 사용하여 users와 members 컬렉션 업데이트를 동시에 실행
     */
    private suspend fun saveProfileToFirestore(
        uid: String,
        familyId: String?,
        newName: String,
        newImageUrl: String?,
        newRelationship: String
    ) = withContext(Dispatchers.IO) { // IO 스레드에서 실행

        try {
            // 1. users 컬렉션 업데이트
            val userUpdates = mapOf(
                "name" to newName,
                "profileImageUrl" to newImageUrl
            )
            // 비동기 작업 1 시작
            val userUpdateTask = async {
                db.collection("users").document(uid).update(userUpdates).await()
            }

            // 2. members 컬렉션 업데이트 (가족에 소속된 경우에만)
            // 비동기 작업 2 시작 (조건부)
            val memberUpdateTask = if (!familyId.isNullOrBlank()) {
                async {
                    val memberUpdates = mapOf(
                        "relationship" to newRelationship,
                        "profileImageUrl" to newImageUrl
                    )
                    db.collection("families").document(familyId)
                        .collection("members").document(uid)
                        .update(memberUpdates).await()
                }
            } else {
                null
            }

            // 모든 비동기 작업이 완료되기를 기다립니다. (병렬 실행)
            if (memberUpdateTask != null) {
                awaitAll(userUpdateTask, memberUpdateTask)
            } else {
                userUpdateTask.await()
            }

            // 모든 작업 성공 시 상태 업데이트
            updateSuccessState(newName, newImageUrl, newRelationship)

        } catch (e: Exception) {
            handleFailure("프로필 정보 저장 실패: ${e.message}")
        }
    }

    private suspend fun uploadImageAndSaveProfile(uid: String, imageUri: Uri, newName: String) = withContext(Dispatchers.IO) {
        val state = _uiState.value
        val currentFamilyId = state.currentFamilyId
        val newRelationship = state.relationship

        val imageRef = storage.reference.child("profile_images/$uid/profile.jpg")

        try {
            // 1. 이미지 업로드 (await으로 동기처럼 대기)
            imageRef.putFile(imageUri).await() // ★ putBytes 대신 putFile 사용

            // 2. 다운로드 URL 가져오기 (await으로 동기처럼 대기)
            val downloadUri = imageRef.downloadUrl.await()

            // 3. Firestore에 병렬 저장 호출
            saveProfileToFirestore(
                uid,
                currentFamilyId,
                newName,
                downloadUri.toString(),
                newRelationship
            )
        } catch (e: Exception) {
            handleFailure("이미지 업로드 또는 URL 가져오기 실패: ${e.message}")
        }
    }


    // 저장 성공 시 UI 상태 업데이트를 위한 헬퍼 함수
    private fun updateSuccessState(newName: String, newImageUrl: String?, newRelationship: String) {
        originalUser = originalUser?.copy(name = newName, profileImageUrl = newImageUrl)
        _uiState.update {
            it.copy(
                isLoading = false,
                isEditing = false,
                profileImageUrl = newImageUrl,
                name = newName,
                relationship = newRelationship,
                originalRelationship = newRelationship,
                newProfileImageUri = null
            )
        }
    }

    // 최종 저장 로직 (이미지 유무에 따라 분기)
    fun saveProfile(){
        val currentUid = auth.currentUser?.uid
        val state = _uiState.value

        if(currentUid == null || state.currentFamilyId == null){
            _uiState.update { it.copy(error = "사용자 인증 실패") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val newImageUri = state.newProfileImageUri
            val newName = state.name
            val currentFamilyId = state.currentFamilyId
            val newRelationship = state.relationship

            if(newImageUri != null){
                // 새 이미지가 선택된 경우 -> Storage 업로드 후 Firestore 병렬 저장
                uploadImageAndSaveProfile(currentUid, newImageUri, newName)
            } else{
                // 이미지가 변경되지 않은 경우 -> 이름만 Firestore 병렬 저장
                saveProfileToFirestore(currentUid, currentFamilyId, newName, state.profileImageUrl, newRelationship)
            }
        }
    }
}
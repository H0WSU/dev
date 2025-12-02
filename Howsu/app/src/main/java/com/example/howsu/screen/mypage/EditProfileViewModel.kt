package com.example.howsu.screen.mypage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.howsu.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ----------------------------------------------------------------------
// 1. UI State (화면 상태)
// ----------------------------------------------------------------------

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
)

// ----------------------------------------------------------------------
// 2. ViewModel
// ----------------------------------------------------------------------

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

    // < 상태 관리 함수 >

    /**
     * 사용자가 새 프로필 이미지를 갤러리/파일에서 선택 시 호출
     */
    fun updateProfileImageUri(uri: Uri?){
        _uiState.update { it.copy(newProfileImageUri = uri) }
    }

    /**
     * 편집 모드 on & off
     */
    fun toggledEditMode(enable: Boolean) {
        _uiState.update { it.copy(isEditing = enable) }
    }

    /**
     * 사용자 이름 입력 필드 값 업데이트 (임시 상태)
     */
    fun updateName(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    /**
     * 반려동물과의 관계 입력 필드 값 업데이트
     */
    fun updateRelationship(newRelationship: String) {
        _uiState.update { it.copy(relationship = newRelationship) }
    }

    /**
     * 편집 취소: 모든 입력값을 원본 데이터로 되돌리고 편집 모드 해제
     */
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

    /**
     * Firebase에서 사용자 정보를 로드합니다.
     */
    fun loadUserProfile() {
        val currentUser = auth.currentUser
        val currentUid = auth.currentUser?.uid ?: return
        val userEmail = currentUser?.email ?: ""  // Firebase Auth에서 이메일 정보 가져오기

        _uiState.update { it.copy(isLoading = true) }

        db.collection("users").document(currentUid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                val familyId = document.getString("currentFamilyId")
                if (user != null) {
                    originalUser = user  // 원본 저장
                    _uiState.update { currentState ->
                        currentState.copy(
                            uid = user.uid,
                            name = user.name,
                            email = userEmail,
                            profileImageUrl = user.profileImageUrl,
                            currentFamilyId = familyId,
                            isLoading = false,
                            isEditing = false // 초기에는 보기 모드
                        )
                    }
                    // 프로필 로드 후, 관계 정보 로드를 시작
                    loadRelationship(currentUid, familyId)
                } else {
                    handleFailure("사용자 정보를 찾을 수 없습니다.")
                }
            }
            .addOnFailureListener { exception ->
                handleFailure("데이터 로드 실패: ${exception.message}")
            }
    }

    /**
     * 모든 에러 처리를 담당하고 UI 상태를 업데이트합니다.
     */
    private fun handleFailure(errorMessage: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = errorMessage
            )
        }
    }
    /**
     * 현재 가족 ID를 기반으로 사용자의 관계(relationship)를 로드합니다.
     */
    private fun loadRelationship(uid: String, familyId: String?) {
        if (familyId.isNullOrBlank()) {
            _uiState.update { it.copy(relationship = "가족에 소속되지 않음", originalRelationship = "가족에 소속되지 않음", isRelationLoading = false) }
            return
        }

        _uiState.update { it.copy(isRelationLoading = true) }

        // 컬렉션 구조: families/{familyId}/familymembers/{uid}
        db.collection("families").document(familyId)
            .collection("familymembers").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val relationship = document.getString("relationship") ?: "관계 설정 필요"

                _uiState.update { currentState ->
                    currentState.copy(
                        relationship = relationship,
                        originalRelationship = relationship, // 원본으로 저장
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
     * Firestore에 이름 및 이미지 URL + 관계 저장
     */
    private fun saveProfileToFirestore(
        uid: String,
        familyId: String?,
        newName: String,
        newImageUrl: String?,
        newRelationship: String
    ) {
        val updates = mapOf(
            "name" to newName,
            "profileImageUrl" to newImageUrl
        )

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                if (!familyId.isNullOrBlank()) {
                    // 2. familymembers 컬렉션 업데이트 (관계)
                    db.collection("families").document(familyId)
                        .collection("familymembers").document(uid)
                        .update("relationship", newRelationship)
                        .addOnSuccessListener {
                            // 모두 저장 성공 시 상태 업데이트
                            updateSuccessState(newName, newImageUrl, newRelationship)
                        }
                        .addOnFailureListener { exception ->
                            handleFailure("관계 정보 저장 실패: ${exception.message}")
                        }
                } else {
                    // 가족이 없는 경우 이름/이미지만 저장 성공
                    updateSuccessState(newName, newImageUrl, newRelationship)
                }
                /*// 저장 성공 시, 원본 데이터 업데이트 및 UI 상태 초기화
                originalUser = originalUser?.copy(name = newName, profileImageUrl = newImageUrl)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEditing = false,
                        profileImageUrl = newImageUrl,
                        newProfileImageUri = null // 임시 URI 제거
                    )
                }*/
            }
            .addOnFailureListener { exception ->
                handleFailure("프로필 정보 저장 실패: ${exception.message}")
            }
    }

    /**
     * Firebase Storage에 이미지를 업로드하고 Firestore 저장을 호출합니다.
     */
    private fun uploadImageAndSaveProfile(uid: String, imageUri: Uri, newName: String) {
        val state = _uiState.value // 현재 상태 가져오기
        val currentFamilyId = state.currentFamilyId
        val newRelationship = state.relationship
        // 파일 이름을 UID와 'profile.jpg'로 지정
        val imageRef = storage.reference.child("profile_images/$uid/profile.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                // 업로드 성공 후 다운로드 URL 가져옴
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // 2단계: Firestore에 새 URL과 이름 저장
                    saveProfileToFirestore(
                        uid,
                        currentFamilyId,
                        newName,
                        downloadUri.toString(),
                        newRelationship
                    )
                }
                    .addOnFailureListener { exception ->
                        handleFailure("이미지 URL 가져오기 실패: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                handleFailure("이미지 업로드 실패: ${exception.message}")
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
    /**
     * 최종 저장 로직 (이미지 유무에 따라 분기)
     */
    fun saveProfile(){
        val currentUid = auth.currentUser?.uid
        val state = _uiState.value

        if(currentUid == null|| state.currentFamilyId == null){
            _uiState.update { it.copy(error = "사용자 인증 실패") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        val newImageUri = _uiState.value.newProfileImageUri
        val newName = _uiState.value.name
        val currentFamilyId = state.currentFamilyId
        val newRelationship = state.relationship

        if(newImageUri != null){
            // 새 이미지가 선택된 경우 -> storage에 업데이트 후 firestore 업데이트
            uploadImageAndSaveProfile(currentUid, newImageUri, newName)
        } else{
            // 이미지가 변경되지 않은 경우 -> 이름만 firestore 업데이트
            saveProfileToFirestore(currentUid, currentFamilyId,newName, state.profileImageUrl, newRelationship)
        }
    }
}
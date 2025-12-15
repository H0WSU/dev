package com.example.howsu.screen.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// 화면에 보여줄 가족 구성원 정보
data class DisplayFamilyMember(
    val userId: String,
    val nickName: String,         // FamilyMember.nickName
    val relationship: String,     // FamilyMember.relationship
    val profileImageUrl: String?, // FamilyMember.profileImageUrl
)

// 가족 가입 상태
sealed class JoinStatus {
    object Idle : JoinStatus()
    object Loading : JoinStatus()
    object Success : JoinStatus()
    data class Error(val message: String) : JoinStatus()
}

class FamilyDetailScreenViewModel : ViewModel(){
    // 현재 로그인된 사용자 UID
    private val _currentUserId = MutableStateFlow("")
    val currentUserId = _currentUserId.asStateFlow()

    // 본인 닉네임, 관계 (본인 FamilyMember 정보)
    private val _nickName = MutableStateFlow("")
    val nickName = _nickName.asStateFlow()
    private val _familyRelationship = MutableStateFlow("")
    val familyRelationship = _familyRelationship.asStateFlow()

    // 가족 전체 정보 (FamilyId)
    private val _familyId = MutableStateFlow("")
    val familyId = _familyId.asStateFlow()

    //가족 이름 (FamilyName)
    private val _familyName = MutableStateFlow("")
    val familyName = _familyName.asStateFlow()

    // 본인 프로필 URL
    private val _familyProfileUrl = MutableStateFlow<String?>(null)
    val familyProfileUrl = _familyProfileUrl.asStateFlow()

    // 전체 가족 구성원 목록 (정렬 상태)
    private val _familyMembers = MutableStateFlow<List<DisplayFamilyMember>>(emptyList())
    val familyMembers = _familyMembers.asStateFlow()


    // 가족 가입 상태 확인
    private val _joinStatus = MutableStateFlow<JoinStatus>(JoinStatus.Idle)
    val joinStatus = _joinStatus.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        // ⭐️ 현재 사용자 UID 초기화
        auth.currentUser?.let {
            _currentUserId.value = it.uid
        }
        loadMyFamilyInfo()
    }

    fun joinFamily(targetFamilyId: String){
        val user = auth.currentUser
        if(user == null || targetFamilyId.isBlank()){
            _joinStatus.value = JoinStatus.Error("로그인 필요 or 가족 id가 유효하지 않음")
            return
        }
        if (_familyId.value == targetFamilyId){
            _joinStatus.value = JoinStatus.Error("이미 가족에 소속되어 있습니다.")
            return
        }

        val currentNickName = _nickName.value
        val currentRelationship = _familyRelationship.value

        if(currentNickName.isBlank() || currentRelationship.isBlank()){
            _joinStatus.value = JoinStatus.Error("닉네임 또는 관게 정보가 설정되어있지 않습니다.")
            return
        }

        _joinStatus.value = JoinStatus.Loading


        viewModelScope.launch {
            try{
                val uid = user.uid
                val batch = db.batch()  // 트랜잭션 대신 Batch Write를 사용하여 원자성 보장

                val newMemberDocRef = db.collection("familyMembers").document()
                val newFamilyMember = mapOf(
                    "userId" to uid,
                    "familyId" to targetFamilyId,
                    "nickName" to currentNickName,       // ⭐️ 뷰모델의 현재 값 사용
                    "relationship" to currentRelationship, // ⭐️ 뷰모델의 현재 값 사용
                    "profileImageUrl" to _familyProfileUrl.value,
                    "isManager" to false
                )
                batch.set(newMemberDocRef, newFamilyMember)

                // 2. User 컬렉션의 currentFamilyId 업데이트
                val userDocRef = db.collection("user").document(uid)
                batch.update(userDocRef, "currentFamilyId", targetFamilyId)

                // 3. Batch 실행
                batch.commit().await()

                _joinStatus.value = JoinStatus.Success
                loadMyFamilyInfo() // 새로 가입한 가족 정보를 화면에 반영

            } catch (e: Exception) {
                Log.e("FamilyDetailScreenViewModel", "가족 가입 실패", e)
                _joinStatus.value = JoinStatus.Error("가족 가입에 실패했습니다: ${e.message}")
            }
            }
        }
    fun resetJoinStatus(){
        _joinStatus.value = JoinStatus.Idle
    }

    private fun loadMyFamilyInfo(){
        val user = auth.currentUser
        if (user == null) return

        viewModelScope.launch {
            try{
                val uid = user.uid
                // 1. 현재 사용자 정보 로드 (currentFamilyId 획득)
                val userDoc = db.collection("user").document(uid).get().await()
                val myFamilyId = userDoc.getString("currentFamilyId")

                if(!myFamilyId.isNullOrBlank()){
                    _familyId.value = myFamilyId // familyId 설정

                    // ⭐️ [추가] 1-1. Family 컬렉션에서 가족 이름 로드
                    loadFamilyName(myFamilyId)

                    // 2. 현재 사용자의 FamilyMember 정보 로드 (본인 카드 정보)
                    // ... (기존 코드와 동일) ...
                    val familyMemberQuery = db.collection("familyMembers")
                        .whereEqualTo("userId", uid)
                        .whereEqualTo("familyId", myFamilyId)
                        .limit(1)
                        .get().await()

                    if(familyMemberQuery.documents.isNotEmpty()){
                        val memberDoc = familyMemberQuery.documents.first()
                        _nickName.value = memberDoc.getString("nickName") ?: "닉네임 설정 필요"
                        _familyRelationship.value = memberDoc.getString("relationship") ?: "관계 설정 필요"
                        _familyProfileUrl.value = memberDoc.getString("profileImageUrl")
                    } else {
                        Log.w("FamilyDetailScreenViewModel", "FamilyMember 정보가 없습니다.")
                    }

                    // 3. 전체 가족 구성원 정보 로드 및 정렬 호출
                    loadFamilyMembers(myFamilyId)

                }else{
                    // ... (가족이 없는 경우 처리 기존 코드와 동일) ...
                }
            } catch (e: Exception){
                Log.e("FamilyDetailScreenViewModel", "데이터 로드 실패", e)
            }
        }
    }

    // ⭐️ [추가] familyId로 familyName을 로드하는 함수
    private suspend fun loadFamilyName(familyId: String) {
        try {
            val familyDoc = db.collection("Family").document(familyId).get().await()
            val name = familyDoc.getString("familyName") // Family 컬렉션에 familyName 필드가 있다고 가정
            _familyName.value = name ?: "우리 가족"
        } catch (e: Exception) {
            Log.e("FamilyDetailScreenViewModel", "가족 이름 로드 실패", e)
            _familyName.value = "우리 가족"
        }
    }
    private suspend fun loadFamilyMembers(familyId: String) {
        try {
            // familyMembers 컬렉션에서 familyId가 일치하는 모든 문서 조회
            val familyMembersSnapshot = db.collection("familyMembers")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()

            val displayMembers = mutableListOf<DisplayFamilyMember>()

            // User 컬렉션 조회 없이 FamilyMember 정보만 사용
            for (memberDoc in familyMembersSnapshot.documents) {
                val userId = memberDoc.getString("userId")
                val nickName = memberDoc.getString("nickName")
                val relationship = memberDoc.getString("relationship")
                val profileImageUrl = memberDoc.getString("profileImageUrl")

                if (userId != null && nickName != null && relationship != null) {
                    displayMembers.add(
                        DisplayFamilyMember(
                            userId = userId,
                            nickName = nickName,
                            relationship = relationship,
                            profileImageUrl = profileImageUrl
                        )
                    )
                }
            }

            // ⭐️ 현재 사용자를 리스트의 맨 앞으로 이동시켜 정렬
            val sortedMembers = displayMembers.sortedWith(
                compareByDescending { it.userId == _currentUserId.value }
            )

            _familyMembers.value = sortedMembers

        } catch (e: Exception) {
            Log.e("FamilyDetailScreenViewModel", "가족 구성원 로드 실패", e)
            _familyMembers.value = emptyList()
        }
    }
}

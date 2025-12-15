package com.example.howsu.screen.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
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

    // 본인 닉네임, 관계 (본인 FamilyMember 정보 또는 User 기본 정보)
    private val _nickName = MutableStateFlow("")
    val nickName = _nickName.asStateFlow()
    private val _familyRelationship = MutableStateFlow("")
    val familyRelationship = _familyRelationship.asStateFlow()

    // 가족 전체 정보 (FamilyId)
    private val _familyId = MutableStateFlow("")
    val familyId = _familyId.asStateFlow()

    // 가족 이름 (FamilyName)
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
        auth.currentUser?.let {
            _currentUserId.value = it.uid
        }
        loadMyFamilyInfo()
    }

    /**
     * 지정된 ID의 가족에 현재 사용자를 가입시킵니다.
     * @param targetFamilyId 가입을 시도할 가족의 ID (초대 코드)
     */
    fun joinFamily(targetFamilyId: String){
        val user = auth.currentUser
        if(user == null || targetFamilyId.isBlank()){
            _joinStatus.value = JoinStatus.Error("로그인 필요 or 가족 id가 유효하지 않음")
            return
        }
        if (_familyId.value == targetFamilyId){
            _joinStatus.value = JoinStatus.Error("이미 현재 가족에 소속되어 있습니다.")
            return
        }

        val currentNickName = _nickName.value
        val currentRelationship = _familyRelationship.value

        if(currentNickName.isBlank() || currentRelationship.isBlank() || currentNickName == "사용자" || currentRelationship == "나"){
            _joinStatus.value = JoinStatus.Error("닉네임 또는 관계 정보가 설정되어있지 않습니다. 마이페이지에서 설정을 확인해주세요.")
            return
        }

        _joinStatus.value = JoinStatus.Loading


        viewModelScope.launch {
            try{
                val uid = user.uid
                val batch = db.batch()

                // 1. FamilyMembers 컬렉션에 새 문서 추가 (families/{familyId}/members/{uid} 구조)
                // ⭐️ FamilyRegisterViewModel 구조에 맞춰 하위 컬렉션에 set
                val newMemberDocRef = db.collection("families").document(targetFamilyId)
                    .collection("members").document(uid)

                val newFamilyMember = mapOf(
                    "userId" to uid,
                    "familyId" to targetFamilyId,
                    "nickName" to currentNickName,
                    "relationship" to currentRelationship,
                    "profileImageUrl" to _familyProfileUrl.value,
                    "isManager" to false // 새로 가입하는 멤버는 방장이 아님
                )
                batch.set(newMemberDocRef, newFamilyMember)

                // 2. User 컬렉션의 currentFamilyId 업데이트 (컬렉션 이름은 users)
                val userDocRef = db.collection("users").document(uid)
                batch.update(userDocRef, "currentFamilyId", targetFamilyId)

                // 3. Family 컬렉션의 memberIds 리스트에 uid 추가 (컬렉션 이름은 families)
                // ⭐️ "Family" -> "families"
                val familyDocRef = db.collection("families").document(targetFamilyId)
                batch.update(familyDocRef, "memberIds", FieldValue.arrayUnion(uid))

                // 4. Batch 실행
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

    /**
     * 현재 사용자의 가족 소속 정보를 로드하고 관련 상태를 업데이트합니다.
     */
    private fun loadMyFamilyInfo(){
        val user = auth.currentUser
        if (user == null) return

        viewModelScope.launch {
            try{
                val uid = user.uid
                // 1. 현재 사용자 정보 로드 (currentFamilyId, name, profileImageUrl 획득)
                val userDoc = db.collection("users").document(uid).get().await()

                if (!userDoc.exists()) {
                    Log.e("FamilyDetailScreenViewModel", "User document not found for UID: $uid")
                    return@launch
                }

                val myFamilyId = userDoc.getString("currentFamilyId")

                // 가족이 없어도 User의 기본 정보를 가져와서 초기값으로 사용
                _nickName.value = userDoc.getString("name") ?: userDoc.getString("email") ?: "사용자"
                _familyRelationship.value = "나"
                _familyProfileUrl.value = userDoc.getString("profileImageUrl")

                if(!myFamilyId.isNullOrBlank()){
                    _familyId.value = myFamilyId // familyId 설정

                    // 1-1. Family 컬렉션에서 가족 이름 로드 (컬렉션 이름은 families)
                    loadFamilyName(myFamilyId)

                    // 2. 현재 사용자의 FamilyMember 정보 로드 (가족 내에서의 닉네임, 관계)
                    // ⭐️ 경로 수정: families/{myFamilyId}/members/{uid}
                    val familyMemberQuerySnapshot = db.collection("families").document(myFamilyId)
                        .collection("members").document(uid).get().await()

                    if(familyMemberQuerySnapshot.exists()){
                        val memberDoc = familyMemberQuerySnapshot

                        // ⭐️ 가족에 소속된 경우 FamilyMember 문서의 닉네임/관계로 덮어쓰기
                        _nickName.value = memberDoc.getString("nickName") ?: _nickName.value
                        _familyRelationship.value = memberDoc.getString("relationship") ?: "관계 설정 필요"
                        _familyProfileUrl.value = memberDoc.getString("profileImageUrl")
                    } else {
                        Log.w("FamilyDetailScreenViewModel", "FamilyMember 정보가 없습니다. (데이터 불일치 가능성)")
                    }

                    // 3. 전체 가족 구성원 정보 로드
                    loadFamilyMembers(myFamilyId)

                }else{
                    _familyId.value = ""
                    _familyName.value = ""
                    _familyMembers.value = emptyList()
                    Log.i("FamilyDetailScreenViewModel", "현재 소속된 가족이 없습니다.")
                }
            } catch (e: Exception){
                Log.e("FamilyDetailScreenViewModel", "데이터 로드 실패", e)
            }
        }
    }

    /**
     * familyId로 familyName을 Family 컬렉션에서 로드합니다. (컬렉션 이름은 families)
     */
    private suspend fun loadFamilyName(familyId: String) {
        try {
            // ⭐️ "Family" -> "families"
            val familyDoc = db.collection("families").document(familyId).get().await()
            val name = familyDoc.getString("familyName")
            _familyName.value = name ?: "우리 가족"
        } catch (e: Exception) {
            Log.e("FamilyDetailScreenViewModel", "가족 이름 로드 실패", e)
            _familyName.value = "우리 가족"
        }
    }

    /**
     * familyId에 해당하는 모든 가족 구성원의 목록을 로드하고 현재 사용자를 맨 앞에 정렬합니다.
     */
    private suspend fun loadFamilyMembers(familyId: String) {
        try {
            // ⭐️ 경로 수정: families/{familyId}/members
            val familyMembersSnapshot = db.collection("families").document(familyId)
                .collection("members")
                .get()
                .await()

            val displayMembers = mutableListOf<DisplayFamilyMember>()

            for (memberDoc in familyMembersSnapshot.documents) {
                // 이 하위 컬렉션의 문서 ID가 곧 userId일 가능성이 높지만, 문서에 저장된 필드 사용
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

            // 현재 사용자를 리스트의 맨 앞으로 이동시켜 정렬
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
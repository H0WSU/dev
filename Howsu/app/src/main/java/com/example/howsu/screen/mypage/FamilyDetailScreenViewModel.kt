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


    // 지정된 ID의 가족에 현재 사용자를 가입 (수정된 로직)
    // targetFamilyId: 사용자가 입력하거나 QR로 스캔한 가족 ID
    // onSuccess: 가입 성공 시, 실제 가족 이름과 프로필 URL을 전달하는 콜백
    fun joinFamily(targetFamilyId: String, onSuccess: (String, String?) -> Unit) {
        val user = auth.currentUser
        if(user == null || targetFamilyId.isBlank()){
            _joinStatus.value = JoinStatus.Error("로그인 필요 or 가족 id가 유효하지 않음")
            return
        }
        if (_familyId.value == targetFamilyId){
            _joinStatus.value = JoinStatus.Error("이미 현재 가족에 소속되어 있습니다.")
            return
        }

        // loadMyFamilyInfo에서 로드된 현재 사용자 정보를 사용함.
        val currentNickName = _nickName.value
        val currentRelationship = _familyRelationship.value
        val myProfileUrl = _familyProfileUrl.value

        // 기존의 유효성 검사 로직 (닉네임/관계 설정 필수) 제거 -> 마이페이지에서는 일단 가입부터 진행

        _joinStatus.value = JoinStatus.Loading


        viewModelScope.launch {
            try{
                val uid = user.uid

                val docRef = db.collection("families").document(targetFamilyId)
                val snapshot = docRef.get().await()

                if (snapshot.exists()) {
                    // DB에서 진짜 가족 이름 가져오기
                    val realFamilyName = snapshot.getString("familyName") ?: "우리 가족"

                    val batch = db.batch()

                    // 1. 멤버 리스트 추가 (families/{familyId} 문서)
                    batch.update(docRef, "memberIds", FieldValue.arrayUnion(uid))

                    // 2. 멤버 정보 생성 (families/{familyId}/members/{uid} 문서)
                    val newMemberDocRef = docRef.collection("members").document(uid)

                    val newFamilyMember = mapOf(
                        "userId" to uid,
                        "familyId" to targetFamilyId,
                        "nickName" to currentNickName, // 유저의 최신 닉네임 사용
                        "relationship" to currentRelationship, // 유저의 최신 관계 사용
                        "profileImageUrl" to myProfileUrl, // 유저의 최신 프로필 URL 사용
                    )
                    batch.set(newMemberDocRef, newFamilyMember)

                    // 3. 유저 정보 업데이트 (users/{uid} 문서)
                    val userDocRef = db.collection("users").document(uid)
                    batch.update(userDocRef, "currentFamilyId", targetFamilyId)

                    // 4. Batch 실행
                    batch.commit().await()

                    _joinStatus.value = JoinStatus.Success
                    loadMyFamilyInfo() // 새로 가입한 가족 정보를 화면에 반영

                    // 성공 시 콜백 호출: 가족 이름과 프로필 URL 전달
                    onSuccess(realFamilyName, myProfileUrl)

                } else {
                    Log.e("FamilyDetailScreenViewModel", "참여 실패: ID 없음")
                    _joinStatus.value = JoinStatus.Error("유효하지 않은 가족 아이디입니다.")
                }
            } catch (e: Exception) {
                Log.e("FamilyDetailScreenViewModel", "가족 가입 실패", e)
                _joinStatus.value = JoinStatus.Error("가족 가입에 실패했습니다: ${e.message}")
            }
        }
    }

    fun resetJoinStatus(){
        _joinStatus.value = JoinStatus.Idle
    }

    // 현재 사용자의 가족 소속 정보를 로드하고 관련 상태를 업데이트
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

                    // 1-1. Family 컬렉션에서 가족 이름 로드
                    loadFamilyName(myFamilyId)

                    // 2. 현재 사용자의 FamilyMember 정보 로드 -> families/{myFamilyId}/members/{uid}
                    val familyMemberQuerySnapshot = db.collection("families").document(myFamilyId)
                        .collection("members").document(uid).get().await()

                    if(familyMemberQuerySnapshot.exists()){
                        val memberDoc = familyMemberQuerySnapshot

                        // 가족에 소속된 경우 FamilyMember 문서의 닉네임/관계로 덮어쓰기
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

    // familyId로 familyName을 Family 컬렉션에서 로드
    private suspend fun loadFamilyName(familyId: String) {
        try {
            val familyDoc = db.collection("families").document(familyId).get().await()
            val name = familyDoc.getString("familyName")
            _familyName.value = name ?: "우리 가족"
        } catch (e: Exception) {
            Log.e("FamilyDetailScreenViewModel", "가족 이름 로드 실패", e)
            _familyName.value = "우리 가족"
        }
    }

    // familyId에 해당하는 모든 가족 구성원의 목록을 로드하고 현재 사용자를 맨 앞에 정렬
    private suspend fun loadFamilyMembers(familyId: String) {
        try {

            // 경로: families/{familyId}/members
            val familyMembersSnapshot = db.collection("families").document(familyId)
                .collection("members")
                .get()
                .await()

            val displayMembers = mutableListOf<DisplayFamilyMember>()

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

    // 특정 가족 구성원 삭제 (방장 확인 없이 실행)
    fun removeFamilyMember(targetUserId: String){
        val currentUid = auth.currentUser?.uid
        val familyIdValue = _familyId.value

        if (currentUid.isNullOrBlank() || familyIdValue.isBlank()) {
            Log.e("FamilyDetailScreenViewModel", "권한 또는 가족 ID 부족")
            return
        }

        viewModelScope.launch {
            try{

                val batch = db.batch()
                val familyDocRef = db.collection("families").document(familyIdValue)

                // 1. families/{familyId} 문서 업데이트
                batch.update(familyDocRef, "memberIds", FieldValue.arrayRemove(targetUserId))

                // 2. FamilyMember 문서 삭제: families/{familyId}/members/{targetUserId}
                val memberDocRef = familyDocRef.collection("members").document(targetUserId)
                batch.delete(memberDocRef)

                // 3. User 문서 업데이트
                val userDocRef = db.collection("users").document(targetUserId)
                batch.update(userDocRef,"currentFamilyId", null)

                batch.commit().await()

                // 4. 화면 데이터 새로고침
                loadMyFamilyInfo()
            } catch (e: Exception){
                Log.e("FamilyDetailScreenViewModel", "가족 구성원 삭제 실패", e)
            }
        }
    }
}
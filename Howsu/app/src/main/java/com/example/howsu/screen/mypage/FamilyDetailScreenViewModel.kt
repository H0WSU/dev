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
    val nickName: String,
    val relationship: String,
    val profileImageUrl: String?,
)

// 가족 가입 상태
sealed class JoinStatus {
    object Idle : JoinStatus()
    object Loading : JoinStatus()
    object Success : JoinStatus()
    data class Error(val message: String) : JoinStatus()
}

class FamilyDetailScreenViewModel : ViewModel() {
    // 현재 로그인된 사용자 UID
    private val _currentUserId = MutableStateFlow("")
    val currentUserId = _currentUserId.asStateFlow()

    // 본인 닉네임, 관계
    private val _nickName = MutableStateFlow("")
    val nickName = _nickName.asStateFlow()
    private val _familyRelationship = MutableStateFlow("")
    val familyRelationship = _familyRelationship.asStateFlow()

    // 가족 전체 정보
    private val _familyId = MutableStateFlow("")
    val familyId = _familyId.asStateFlow()

    private val _familyName = MutableStateFlow("")
    val familyName = _familyName.asStateFlow()

    // 본인 프로필 URL
    private val _familyProfileUrl = MutableStateFlow<String?>(null)
    val familyProfileUrl = _familyProfileUrl.asStateFlow()

    // 전체 가족 구성원 목록
    private val _familyMembers = MutableStateFlow<List<DisplayFamilyMember>>(emptyList())
    val familyMembers = _familyMembers.asStateFlow()

    // 가족 가입 상태
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

    // 가족 가입 (변경 없음)
    fun joinFamily(targetFamilyId: String, onSuccess: (String, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null || targetFamilyId.isBlank()) {
            _joinStatus.value = JoinStatus.Error("로그인 필요 or 가족 id가 유효하지 않음")
            return
        }
        if (_familyId.value == targetFamilyId) {
            _joinStatus.value = JoinStatus.Error("이미 현재 가족에 소속되어 있습니다.")
            return
        }

        val currentNickName = _nickName.value
        val currentRelationship = _familyRelationship.value
        val myProfileUrl = _familyProfileUrl.value

        _joinStatus.value = JoinStatus.Loading

        viewModelScope.launch {
            try {
                val uid = user.uid
                val docRef = db.collection("families").document(targetFamilyId)
                val snapshot = docRef.get().await()

                if (snapshot.exists()) {
                    val realFamilyName = snapshot.getString("familyName") ?: "우리 가족"
                    val batch = db.batch()

                    batch.update(docRef, "memberIds", FieldValue.arrayUnion(uid))

                    val newMemberDocRef = docRef.collection("members").document(uid)
                    val newFamilyMember = mapOf(
                        "userId" to uid,
                        "familyId" to targetFamilyId,
                        "nickName" to currentNickName,
                        "relationship" to currentRelationship,
                        "profileImageUrl" to myProfileUrl,
                    )
                    batch.set(newMemberDocRef, newFamilyMember)

                    val userDocRef = db.collection("users").document(uid)
                    batch.update(userDocRef, "currentFamilyId", targetFamilyId)

                    batch.commit().await()

                    _joinStatus.value = JoinStatus.Success
                    loadMyFamilyInfo()
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

    fun resetJoinStatus() {
        _joinStatus.value = JoinStatus.Idle
    }

    // ★★★ [수정됨] 정보 로드 로직 개선
    fun loadMyFamilyInfo() {
        val user = auth.currentUser
        if (user == null) return

        viewModelScope.launch {
            try {
                val uid = user.uid

                // 1. Users DB (최신 정보) 가져오기
                val userDoc = db.collection("users").document(uid).get().await()

                if (!userDoc.exists()) return@launch

                val myFamilyId = userDoc.getString("currentFamilyId")

                // ★ 유저 DB의 최신 닉네임과 이미지를 우선 저장
                val freshNickName = userDoc.getString("name") ?: "사용자"
                val freshProfileUrl = userDoc.getString("profileImageUrl")

                _nickName.value = freshNickName
                _familyProfileUrl.value = freshProfileUrl
                _familyRelationship.value = "나" // 기본값

                if (!myFamilyId.isNullOrBlank()) {
                    _familyId.value = myFamilyId
                    loadFamilyName(myFamilyId)

                    // 2. FamilyMember DB 정보 확인
                    val familyMemberDoc = db.collection("families").document(myFamilyId)
                        .collection("members").document(uid).get().await()

                    if (familyMemberDoc.exists()) {
                        // ★ [수정] 닉네임/이미지는 덮어쓰지 않고, '관계(relationship)'만 가져옴
                        // 이유: 유저 정보 수정 시 users DB만 업데이트 되는 경우를 대비해 users 정보를 최우선으로 함
                        _familyRelationship.value =
                            familyMemberDoc.getString("relationship") ?: "관계 설정 필요"

                        // 단, users DB에 정보가 없었다면 family DB 것을 사용
                        if (freshNickName == "사용자") {
                            _nickName.value = familyMemberDoc.getString("nickName") ?: "사용자"
                        }
                    }

                    // 3. 목록 로드
                    loadFamilyMembers(myFamilyId)

                } else {
                    _familyId.value = ""
                    _familyName.value = ""
                    _familyMembers.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("FamilyDetailScreenViewModel", "데이터 로드 실패", e)
            }
        }
    }

    private suspend fun loadFamilyName(familyId: String) {
        try {
            val familyDoc = db.collection("families").document(familyId).get().await()
            val name = familyDoc.getString("familyName")
            _familyName.value = name ?: "우리 가족"
        } catch (e: Exception) {
            _familyName.value = "우리 가족"
        }
    }

    // ★★★ [수정됨] 목록 로드 시 내 정보 교체
    private suspend fun loadFamilyMembers(familyId: String) {
        try {
            val familyMembersSnapshot = db.collection("families").document(familyId)
                .collection("members")
                .get()
                .await()

            val displayMembers = mutableListOf<DisplayFamilyMember>()
            val myUid = _currentUserId.value

            for (memberDoc in familyMembersSnapshot.documents) {
                val userId = memberDoc.getString("userId") ?: continue
                var nickName = memberDoc.getString("nickName") ?: "알 수 없음"
                val relationship = memberDoc.getString("relationship") ?: "알 수 없음"
                var profileImageUrl = memberDoc.getString("profileImageUrl")

                // ★ [핵심] 만약 이 멤버가 '나'라면, DB 정보 대신 방금 가져온 최신 User 정보를 사용
                if (userId == myUid) {
                    nickName = _nickName.value
                    profileImageUrl = _familyProfileUrl.value
                }

                displayMembers.add(
                    DisplayFamilyMember(
                        userId = userId,
                        nickName = nickName,
                        relationship = relationship,
                        profileImageUrl = profileImageUrl
                    )
                )
            }

            // 내 계정 맨 위로 정렬
            val sortedMembers = displayMembers.sortedWith(
                compareByDescending { it.userId == myUid }
            )

            _familyMembers.value = sortedMembers

        } catch (e: Exception) {
            Log.e("FamilyDetailScreenViewModel", "가족 구성원 로드 실패", e)
            _familyMembers.value = emptyList()
        }
    }

    // 특정 가족 구성원 삭제 (본인 탈퇴 또는 타인 강퇴)
    fun removeFamilyMember(targetUserId: String) {
        val currentUid = auth.currentUser?.uid
        val familyIdValue = _familyId.value

        if (currentUid.isNullOrBlank() || familyIdValue.isBlank()) {
            Log.e("FamilyDetailScreenViewModel", "권한 또는 가족 ID 부족")
            return
        }

        viewModelScope.launch {
            try {
                val batch = db.batch()
                val familyDocRef = db.collection("families").document(familyIdValue)

                // 1. families/{familyId} 문서에서 멤버 ID 제거
                batch.update(familyDocRef, "memberIds", FieldValue.arrayRemove(targetUserId))

                // 2. FamilyMember 문서 삭제: families/{familyId}/members/{targetUserId}
                val memberDocRef = familyDocRef.collection("members").document(targetUserId)
                batch.delete(memberDocRef)

                // 3. User 문서 업데이트 (핵심 로직 변경)
                val userDocRef = db.collection("users").document(targetUserId)

                // ★ [수정됨] 나 자신을 삭제(탈퇴)하는 경우, 다른 가족이 있는지 찾아봄
                if (targetUserId == currentUid) {
                    // 내가 속한 '다른' 가족 찾기
                    val otherFamiliesSnapshot = db.collection("families")
                        .whereArrayContains("memberIds", targetUserId)
                        .get()
                        .await()

                    // 현재 탈퇴하려는 가족(familyIdValue)을 제외한 첫 번째 가족을 찾음
                    val nextFamily =
                        otherFamiliesSnapshot.documents.firstOrNull { it.id != familyIdValue }

                    if (nextFamily != null) {
                        // 다른 가족이 있다면 그 가족으로 currentFamilyId 변경 (자동 환승)
                        batch.update(userDocRef, "currentFamilyId", nextFamily.id)
                        Log.d("FamilyDetail", "탈퇴 후 ${nextFamily.getString("familyName")}(으)로 이동")
                    } else {
                        // 다른 가족이 없다면 null 처리 (가족 없음)
                        batch.update(userDocRef, "currentFamilyId", null)
                        Log.d("FamilyDetail", "탈퇴 후 남은 가족 없음")
                    }
                } else {
                    // ★ [기존 유지] 남을 강퇴하는 경우: 일단 null로 만듦
                    // (심화: 상대방이 현재 이 가족을 보고 있을 때만 null로 하는 게 좋지만, 일단 강퇴 처리는 null이 확실함)
                    batch.update(userDocRef, "currentFamilyId", null)
                }

                // 4. 커밋
                batch.commit().await()

                // 5. 화면 데이터 새로고침
                loadMyFamilyInfo()

            } catch (e: Exception) {
                Log.e("FamilyDetailScreenViewModel", "가족 구성원 삭제 실패", e)
            }
        }
    }
}
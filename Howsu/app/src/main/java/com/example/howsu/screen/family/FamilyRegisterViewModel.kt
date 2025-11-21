package com.example.howsu.screen.family

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Family
import com.example.howsu.data.model.FamilyMember
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

// 상태 Enum
enum class FamilyRegState { NONE, SKIP, PRE_DO_IT, DO_IT }
enum class FamilyTab { CREATE, JOIN }

class FamilyRegisterViewModel : ViewModel() {
    // UI 상태 관리
    var regState by mutableStateOf(FamilyRegState.NONE)
    var selectedTab by mutableStateOf(FamilyTab.CREATE)

    // 입력 필드
    var inputFamilyName by mutableStateOf("") // 생성할 가족 이름
    var inputFamilyId by mutableStateOf("")   // 참여할 가족 ID

    // 결과 저장용 (생성된 가족 ID) - ★ 여기가 ID 저장하는 곳!
    var createdFamilyId by mutableStateOf("")

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    fun createSharedFamily(nickname: String, profileUrl: String?) {
        val user = auth.currentUser ?: return

        val newId = generateRandomId()
        createdFamilyId = newId // 변수에 저장!

        if (inputFamilyName.isBlank()) inputFamilyName = "우리 가족"

        viewModelScope.launch {
            saveFamilyToFirebase(
                familyId = newId,
                familyName = inputFamilyName,
                isSolo = false,
                userUid = user.uid,
                nickname = nickname,
                profileUrl = profileUrl
            )
            // 로그로 확인해보세요!
            println("공유 가족 생성 완료: ID = $newId")
        }
    }

    // [로직 2] 1인 가족 생성
    fun createSoloFamily(nickname: String, profileUrl: String?) {
        val user = auth.currentUser ?: return
        val newId = generateRandomId()
        createdFamilyId = newId

        if (inputFamilyName.isBlank()) {
            inputFamilyName = "${nickname}님의 집"
        }

        viewModelScope.launch {
            saveFamilyToFirebase(
                familyId = newId,
                familyName = inputFamilyName,
                isSolo = true,
                userUid = user.uid,
                nickname = nickname,
                profileUrl = profileUrl,
                initialRelationship = "나"
            )
            println("1인 가족 생성 완료: ID = $newId")
        }
    }

    // [로직 3] 가족 참여
    fun joinFamily(onSuccess: () -> Unit, onFailure: () -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onFailure()
            return
        }
        if (inputFamilyId.isBlank()) {
            onFailure()
            return
        }

        viewModelScope.launch {
            try {
                // 1. 가족 문서 확인
                val docRef = db.collection("families").document(inputFamilyId)
                val snapshot = docRef.get().await()

                if (snapshot.exists()) {
                    // 2. 가족 멤버 리스트에 나 추가
                    docRef.update("memberIds", FieldValue.arrayUnion(user.uid)).await()

                    // 3. 멤버 정보 생성 (기본 닉네임은 유저 이름으로 하면 더 좋음)
                    // 유저의 닉네임을 가져오기 위해 users 컬렉션 조회
                    val userSnapshot = db.collection("users").document(user.uid).get().await()
                    val myNickname = userSnapshot.getString("name") ?: "구성원"
                    val myProfileUrl = userSnapshot.getString("profileImageUrl")

                    val newMember = FamilyMember(
                        userId = user.uid,
                        familyId = inputFamilyId,
                        nickName = myNickname,
                        relationship = "참여자", // 나중에 관계 설정에서 변경 가능
                        profileImageUrl = myProfileUrl
                    )
                    docRef.collection("members").document(user.uid).set(newMember).await()

                    // 4. 내 유저 정보에 '현재 가족 ID' 업데이트
                    db.collection("users").document(user.uid)
                        .set(mapOf("currentFamilyId" to inputFamilyId), SetOptions.merge())
                        .await()

                    println("가족 참여 성공: $inputFamilyId")

                    // ★ [핵심] 모든 DB 작업이 끝나면 성공 신호를 보냄
                    onSuccess()
                } else {
                    println("참여 실패: 존재하지 않는 가족 ID")
                    onFailure()
                }
            } catch (e: Exception) {
                Log.e("FamilyVM", "참여 실패", e)
                onFailure()
            }
        }
    }

    // 내부 저장 함수
    private suspend fun saveFamilyToFirebase(
        familyId: String,
        familyName: String,
        isSolo: Boolean,
        userUid: String,
        nickname: String = "방장",
        initialRelationship: String = "나", // 기본값
        profileUrl: String? = null
    ) {
        try {
            val familyData = Family(
                familyId = familyId,
                familyName = familyName,
                ownerUserId = userUid,
                isSoloMode = isSolo,
                memberIds = listOf(userUid)
            )
            db.collection("families").document(familyId).set(familyData).await()

            val memberData = FamilyMember(
                userId = userUid,
                familyId = familyId,
                nickName = nickname,
                relationship = initialRelationship,
                profileImageUrl = profileUrl
            )

            db.collection("families").document(familyId)
                .collection("members").document(userUid).set(memberData).await()

            db.collection("users").document(userUid)
                .set(mapOf("currentFamilyId" to familyId), SetOptions.merge())
                .await()

        } catch (e: Exception) {
            Log.e("FamilyVM", "DB 저장 실패", e)
        }
    }

    private fun generateRandomId(): String {
        return "with@${Random.nextInt(1000, 9999)}"
    }
}
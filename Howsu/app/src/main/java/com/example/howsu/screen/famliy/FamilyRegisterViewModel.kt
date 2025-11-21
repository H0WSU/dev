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

    // 결과 저장용 (생성된 가족 ID)
    var createdFamilyId by mutableStateOf("")

    private val db = Firebase.firestore
    private val auth = Firebase.auth


    // [로직 3] 가족 참여
    fun joinFamily(): Boolean {
        val user = auth.currentUser ?: return false
        if (inputFamilyId.isBlank()) return false

        viewModelScope.launch {
            try {
                val docRef = db.collection("families").document(inputFamilyId)
                val snapshot = docRef.get().await()

                if (snapshot.exists()) {
                    // 1. 가족 멤버 리스트에 나 추가
                    docRef.update("memberIds", FieldValue.arrayUnion(user.uid)).await()

                    // 2. 멤버 정보 생성
                    val newMember = FamilyMember(
                        userId = user.uid,
                        familyId = inputFamilyId,
                        nickName = "구성원", // 닉네임 기본값 (필요시 수정)
                        relationship = "참여자",
                        profileImageUrl = null
                    )
                    docRef.collection("members").document(user.uid).set(newMember).await()

                    // ★★★ [중요] 내 유저 정보에 '현재 가족 ID' 업데이트
                    // 이게 있어야 투두 화면에서 데이터를 가져옵니다!
                    db.collection("users").document(user.uid)
                        .set(mapOf("currentFamilyId" to inputFamilyId), SetOptions.merge())
                        .await()

                    println("가족 참여 성공: $inputFamilyId")
                }
            } catch (e: Exception) {
                Log.e("FamilyVM", "참여 실패", e)
            }
        }
        return true
    }

    // [수정] createSharedFamily: profileUrl 파라미터 추가
    fun createSharedFamily(nickname: String, profileUrl: String?) {
        val user = auth.currentUser ?: return
        val newId = generateRandomId()
        createdFamilyId = newId

        if (inputFamilyName.isBlank()) inputFamilyName = "우리 가족"

        viewModelScope.launch {
            saveFamilyToFirebase(
                familyId = newId,
                familyName = inputFamilyName,
                isSolo = false,
                userUid = user.uid,
                nickname = nickname,
                profileUrl = profileUrl // ★ 전달
            )
            println("공유 가족 생성 완료")
        }
    }

    // [수정] createSoloFamily: profileUrl 파라미터 추가
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
                profileUrl = profileUrl, // ★ 전달
                initialRelationship = "나"
            )
            println("1인 가족 생성 완료")
        }
    }

    // [수정] 내부 저장 함수: profileUrl 파라미터 추가 및 저장
    private suspend fun saveFamilyToFirebase(
        familyId: String,
        familyName: String,
        isSolo: Boolean,
        userUid: String,
        nickname: String = "방장",
        initialRelationship: String = "나",
        profileUrl: String? = null // ★ 추가됨
    ) {
        try {
            // 1. 가족 문서 생성 (동일)
            val familyData = Family(
                familyId = familyId,
                familyName = familyName,
                ownerUserId = userUid,
                isSoloMode = isSolo,
                memberIds = listOf(userUid)
            )
            db.collection("families").document(familyId).set(familyData).await()

            // 2. 멤버 문서 생성 (여기가 핵심!)
            val memberData = FamilyMember(
                userId = userUid,
                familyId = familyId,
                nickName = nickname,
                relationship = initialRelationship,

                // ★★★ [핵심] 여기서 드디어 사진 주소를 저장합니다!
                profileImageUrl = profileUrl
            )

            db.collection("families").document(familyId)
                .collection("members").document(userUid).set(memberData).await()

            // 3. 유저 정보 업데이트 (동일)
            db.collection("users").document(userUid)
                .set(mapOf("currentFamilyId" to familyId), com.google.firebase.firestore.SetOptions.merge())
                .await()

        } catch (e: Exception) {
            Log.e("FamilyVM", "DB 저장 실패", e)
        }
    }

    private fun generateRandomId(): String {
        return "with@${Random.nextInt(1000, 9999)}"
    }
}
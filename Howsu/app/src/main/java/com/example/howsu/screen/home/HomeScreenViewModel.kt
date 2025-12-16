package com.example.howsu.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Family
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.Pet
import com.example.howsu.data.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

// 1. UI 상태 변경: Family와 FamilyMember 객체 자체를 보유하도록 수정
data class HomeUiState(
    val isLoading: Boolean = false,
    val family: Family = Family(),             // TopBar용 현재 활성화된 가족 객체
    val member: FamilyMember = FamilyMember(), // TopBar용 내 멤버 정보
    val pets: List<PetUiModel> = emptyList(),
    val familyMembers: List<FamilyMember> = emptyList(),
    // ★ 1. [추가] 사용자가 소속된 모든 가족 리스트 (선택 드롭다운용)
    val userFamilies: List<Family> = emptyList(),
)

data class PetUiModel(
    val originalPet: Pet,
    val ageText: String,
    val displayGender: String
)

class HomeScreenViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 현재 로그인중인 FamilyMember (프로필, 닉네임)
    private val _currentMember = MutableStateFlow<FamilyMember?>(null)
    val currentMember: StateFlow<FamilyMember?> = _currentMember.asStateFlow()

    init {
        // ★ fetchHomeData() 대신 fetchInitialData()를 호출하여 모든 가족 정보를 먼저 로드
        fetchInitialData()
    }

    // ★ 2. [추가] 가족 ID 변경 함수
    fun updateCurrentFamily(newFamilyId: String) {
        if (newFamilyId == _uiState.value.family.familyId) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // 1. 유저 문서의 currentFamilyId 업데이트
                val uid = auth.currentUser?.uid ?: return@launch
                db.collection("users").document(uid)
                    .update("currentFamilyId", newFamilyId)
                    .await()

                // 2. 새로운 가족 ID로 홈 데이터 다시 로드
                fetchHomeData(newFamilyId)

            } catch (e: Exception) {
                Log.e("HomeScreenVM", "Error updating current family ID", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ★ 3. [수정] 초기 데이터 로드 (모든 소속 가족 리스트 로드)
    private fun fetchInitialData() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            try {
                // 1. User 정보 가져오기 (현재 활성화된 familyId 확인)
                val userDoc = db.collection("users").document(uid).get().await()
                val user = userDoc.toObject(User::class.java)

                val currentFamilyId = user?.currentFamilyId ?: ""
                val userName = user?.name ?: "알 수 없음"
                val userProfileUrl = userDoc.getString("profileImageUrl")

                // 2. [추가] 사용자가 소속된 모든 가족 ID를 포함하는 Family 문서 찾기
                // Firestore 쿼리: memberIds 필드에 현재 UID가 포함된 모든 Family 문서
                val allFamiliesSnapshot = db.collection("families")
                    .whereArrayContains("memberIds", uid)
                    .get()
                    .await()

                val allFamilies = allFamiliesSnapshot.documents.mapNotNull { it.toObject(Family::class.java) }

                _uiState.update { it.copy(userFamilies = allFamilies) }


                if (currentFamilyId.isNotEmpty()) {
                    // 3. 현재 활성화된 가족 ID로 전체 홈 데이터 로드
                    fetchHomeData(currentFamilyId)
                } else {
                    // 가족 없음: 임시 멤버 객체 생성
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            member = FamilyMember(
                                nickName = userName,
                                profileImageUrl = userProfileUrl,
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeScreenVM", "Error fetching initial data", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ★ 4. [수정] fetchHomeData는 이제 인자로 familyId를 받습니다.
    private suspend fun fetchHomeData(
        familyId: String,
    ) {
        val myUid = auth.currentUser?.uid ?: return

        try {
            // User 문서에서 최신 name과 profileUrl을 다시 가져옵니다.
            val userDoc = db.collection("users").document(myUid).get().await()
            val myName = userDoc.getString("name") ?: "알 수 없음"
            val myProfileUrl = userDoc.getString("profileImageUrl")

            // A. 가족 정보 가져오기 (객체로 변환)
            val familyDoc = db.collection("families").document(familyId).get().await()
            val familyObj = familyDoc.toObject(Family::class.java) ?: Family(familyName = "우리 가족")

            // B. 가족 구성원 가져오기
            val membersSnapshot = db.collection("families").document(familyId)
                .collection("members")
                .get()
                .await()

            val members = membersSnapshot.documents.mapNotNull { it.toObject(FamilyMember::class.java) }

            // C. 구성원 목록에서 '나' 찾기 (TopBar 표시용)
            val myMemberInfo = members.find { it.userId == myUid }
                ?: FamilyMember(
                    nickName = myName,
                    userId = myUid,
                    profileImageUrl = myProfileUrl
                )

            // member 객체에 현재 familyId를 할당
            val myMemberInfoWithFamilyId = myMemberInfo.copy(familyId = familyId)

            val sortedMenbers = members.sortedWith(   // 사용자 본인을 가장 앞에 보여줌
                compareByDescending { it.userId == myUid }
            )

            // D. 펫 목록 가져오기
            val petsSnapshot = db.collection("families").document(familyId)
                .collection("pets")
                .get()
                .await()

            val petsList = petsSnapshot.documents.mapNotNull { doc ->
                val pet = doc.toObject(Pet::class.java)?.copy(petId = doc.id)

                pet?.let {
                    val age = calculatePetAge(it)
                    val gender = translateGender(it.gender)

                    PetUiModel(it, age, gender)
                }
            }

            // 상태 업데이트
            _uiState.update {
                it.copy(
                    isLoading = false,
                    family = familyObj,
                    member = myMemberInfoWithFamilyId,
                    pets = petsList,
                    familyMembers = sortedMenbers
                )
            }

        } catch (e: Exception) {
            Log.e("HomeScreenVM", "Error fetching family data", e)
        }
    }


    /* -------------------------------------------------------------
      1) 내 프로필(FamilyMember) 불러오기 (초기 가족 ID가 바뀔 수 있어 수정)
      ------------------------------------------------------------- */
    fun fetchMyProfile() {
        val uid = auth.currentUser?.uid ?: run {
            _currentMember.value = null
            return
        }

        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid).get().await()
                val currentFamilyId = userDoc.getString("currentFamilyId") ?: ""

                val nickname = userDoc.getString("name") ?: "알 수 없음"
                val profileUrl = userDoc.getString("profileImageUrl")
                var relationship = "나"

                if (currentFamilyId.isNotEmpty()) {
                    val memberDoc = db.collection("families").document(currentFamilyId)
                        .collection("members").document(uid).get().await()
                    relationship = memberDoc.getString("relationship") ?: "관계 설정 필요"
                }


                val me = FamilyMember(
                    userId = uid,
                    familyId = currentFamilyId,
                    nickName = nickname,
                    profileImageUrl = profileUrl,
                    relationship = relationship
                )
                _currentMember.value = me

            } catch (e: Exception) {
                Log.e("FeedViewModel", "fetchMyProfile 실패", e)
            }
        }
    }

    private fun calculatePetAge(pet: Pet): String {
        return try {
            if (!pet.birthdayExact.isNullOrEmpty()) {
                val birthDate = LocalDate.parse(pet.birthdayExact, DateTimeFormatter.ISO_DATE)
                val now = LocalDate.now()
                "${Period.between(birthDate, now).years}세"
            } else if (!pet.birthdayYearApprox.isNullOrEmpty()) {
                val birthYear = pet.birthdayYearApprox.toInt()
                val currentYear = LocalDate.now().year
                "${currentYear - birthYear}세"
            } else {
                "?세"
            }
        } catch (e: Exception) { "?세" }
    }

    private fun translateGender(gender: String?): String {   // DB 상 저장된 성별 형태 변경
        return when (gender?.uppercase()) {
            "MALE" -> "남아"
            "FEMALE" -> "여아"
            else -> "성별미상"
        }
    }
}
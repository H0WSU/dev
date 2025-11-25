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
    val family: Family = Family(),             // TopBar용 가족 객체
    val member: FamilyMember = FamilyMember(), // TopBar용 내 멤버 정보
    val pets: List<PetUiModel> = emptyList(),
    val familyMembers: List<FamilyMember> = emptyList(),
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

    init {
        fetchHomeData()
    }

    private fun fetchHomeData() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            try {
                // 1. User 정보 가져오기
                val userDoc = db.collection("users").document(uid).get().await()
                val user = userDoc.toObject(User::class.java)

                if (user?.currentFamilyId != null) {
                    // 2. 가족 데이터 가져오기
                    fetchFamilyData(user.currentFamilyId!!, uid, user.name)
                } else {
                    // 가족 없음: 임시 멤버 객체 생성
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            member = FamilyMember(nickName = user?.name ?: "알 수 없음")
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeScreenVM", "Error fetching home data", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun fetchFamilyData(familyId: String, myUid: String, myName: String) {
        try {
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
            // FamilyMember에 userId 필드가 있다고 가정하고 매칭합니다.
            val myMemberInfo = members.find { it.userId == myUid }
                ?: FamilyMember(nickName = myName, userId = myUid)

            // D. 펫 목록 가져오기
            val petsSnapshot = db.collection("families").document(familyId)
                .collection("pets")
                .get()
                .await()

            val petsList = petsSnapshot.documents.mapNotNull { doc ->
                val pet = doc.toObject(Pet::class.java)
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
                    family = familyObj,   // 가족 객체 저장
                    member = myMemberInfo,// 내 멤버 객체 저장
                    pets = petsList,
                    familyMembers = members
                )
            }

        } catch (e: Exception) {
            Log.e("HomeScreenVM", "Error fetching family data", e)
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

    private fun translateGender(gender: String?): String {
        return when (gender?.uppercase()) {
            "MALE" -> "남아"
            "FEMALE" -> "여아"
            else -> "성별미상"
        }
    }
}
package com.example.howsu.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

// UI 상태를 관리하는 데이터 클래스 (showInviteDialog 제거)
data class HomeUiState(
    val isLoading: Boolean = false,
    val myName: String = "",
    val myProfileUrl: String? = null,
    val familyName: String = "",
    val pets: List<PetUiModel> = emptyList(),
    val familyMembers: List<FamilyMember> = emptyList(),
    // 초대 관련 필드 제거됨
)

// UI에 보여주기 위해 가공된 펫 모델
data class PetUiModel(
    val originalPet: Pet,
    val ageText: String // "7세" 등으로 계산된 문자열
)

class HomeScreenViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // 1. UI 전체 상태 (펫, 가족, 내정보)
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchHomeData()
    }

    /**
     * Firebase에서 유저 -> 가족 -> 펫/멤버 순으로 데이터를 가져옴
     */
    private fun fetchHomeData() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                // 로그인 안됨 처리
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            try {
                // 1. 내 정보 가져오기 (users 컬렉션)
                val userDoc = db.collection("users").document(uid).get().await()
                val user = userDoc.toObject(User::class.java)

                if (user?.currentFamilyId != null) {
                    // 2. 가족 정보 및 멤버 가져오기
                    fetchFamilyData(user.currentFamilyId!!, user.name)
                } else {
                    // 가족 없음 (솔로 모드 등)
                    _uiState.update { it.copy(isLoading = false, myName = user?.name ?: "알 수 없음") }
                }

            } catch (e: Exception) {
                Log.e("HomeScreenVM", "Error fetching home data", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun fetchFamilyData(familyId: String, myName: String) {
        try {
            // A. 가족 기본 정보 (이름 등)
            val familyDoc = db.collection("families").document(familyId).get().await()
            val familyName = familyDoc.getString("familyName") ?: "우리 가족"

            // B. 가족 구성원 (families -> members 서브컬렉션 가정)
            val membersSnapshot = db.collection("families").document(familyId)
                .collection("members")
                .get()
                .await()

            val members = membersSnapshot.documents.mapNotNull { it.toObject(FamilyMember::class.java) }

            // C. 펫 목록 (families -> pets 서브컬렉션 가정)
            val petsSnapshot = db.collection("families").document(familyId)
                .collection("pets")
                .get()
                .await()

            val petsList = petsSnapshot.documents.mapNotNull { doc ->
                val pet = doc.toObject(Pet::class.java)
                pet?.let {
                    // 나이 계산 로직 적용
                    val age = calculatePetAge(it)
                    PetUiModel(it, age)
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    myName = myName,
                    familyName = familyName,
                    pets = petsList,
                    familyMembers = members
                )
            }

        } catch (e: Exception) {
            Log.e("HomeScreenVM", "Error fetching family data", e)
        }
    }

    // 생일 문자열(YYYY-MM-DD)을 기반으로 나이 계산
    private fun calculatePetAge(pet: Pet): String {
        return try {
            if (!pet.birthdayExact.isNullOrEmpty()) {
                val birthDate = LocalDate.parse(pet.birthdayExact, DateTimeFormatter.ISO_DATE)
                val now = LocalDate.now()
                val age = Period.between(birthDate, now).years
                "${age}세"
            } else if (!pet.birthdayYearApprox.isNullOrEmpty()) {
                val birthYear = pet.birthdayYearApprox.toInt()
                val currentYear = LocalDate.now().year
                "${currentYear - birthYear}세"
            } else {
                "?세"
            }
        } catch (e: Exception) {
            "?세"
        }
    }
}
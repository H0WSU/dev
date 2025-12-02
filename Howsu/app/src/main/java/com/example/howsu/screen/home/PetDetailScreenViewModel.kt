/*package com.example.howsu.screen.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Pet
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

data class PetDetailUiState(
    val isLoading: Boolean = true,
    val pet: Pet? = null,
    val ageText: String = "",
    val familyId: String? = null,
    val error: String? = null
)
class PetDetailViewModel(
    savedStateHandle: SavedStateHandle // 네비게이션으로 전달된 arguments를 받음
) : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // petId와 familyId로 정보 가져옴
    private val familyId: String? = savedStateHandle["familyId"]
    //private val petName: String? = savedStateHandle["petName"]
    private val petId: String? = savedStateHandle["petId"]

    private val _uiState = MutableStateFlow(PetDetailUiState())
    val uiState: StateFlow<PetDetailUiState> = _uiState.asStateFlow()

    init {
        fetchPetDetail()
    }

    private fun fetchPetDetail() {
        // 💡 인수가 유효한지 확인
        if (familyId.isNullOrEmpty() || petId.isNullOrEmpty()) {
            _uiState.update { it.copy(isLoading = false, error = "가족 ID 또는 펫 이름을 찾을 수 없습니다.") }
            return
        }

        viewModelScope.launch {
            try {
                // 1. familyId와 petName을 사용하여 쿼리 실행
                // 주의: petName은 고유하지 않을 수 있습니다.
                val snapshot = db.collection("families").document(familyId!!)
                    .collection("pets")
                    .whereEqualTo("name", petId) // 이름으로 필터링
                    .limit(1)
                    .get()
                    .await()

                val petDoc = snapshot.documents.firstOrNull()

                if (petDoc != null) {
                    // 2. Pet 객체로 변환하고, ViewModel에서 사용하기 위해 문서 ID를 petId에 할당
                    val pet = petDoc.toObject(Pet::class.java)?.copy(petId = petDoc.id)

                    if (pet != null) {
                        // DB에 저장된 성별을 화면에 표시할 수 있도록 변환
                        val translatedGender = translateGender(pet.gender)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                // UI에 표시하기 위해 성별 필드를 변환된 값으로 업데이트
                                pet = pet.copy(gender = translatedGender),
                                ageText = calculateAge(pet),
                                familyId = familyId // familyId 상태 업데이트
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "펫 정보를 찾을 수 없습니다.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "해당 이름의 펫을 찾을 수 없습니다.") }
                }
            } catch (e: Exception) {
                Log.e("PetDetailVM", "Error fetching pet", e)
                _uiState.update { it.copy(isLoading = false, error = "데이터 로드 실패") }
            }
        }
    }

    // 나이 계산 로직 (유지)
    private fun calculateAge(pet: Pet): String {
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

    // 💡 추가된 로직: 성별 DB 값을 화면 표시용으로 변환 (HomeScreenVM에서 가져옴)
    private fun translateGender(gender: String?): String {
        return when (gender?.uppercase()) {
            "MALE" -> "남아"
            "FEMALE" -> "여아"
            else -> "성별미상"
        }
    }
}*/
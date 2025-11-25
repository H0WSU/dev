package com.example.howsu.screen.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class PetDetailUiState(
    val isLoading: Boolean = true,
    val pet: Pet? = null,
    val ageText: String = "",
    val error: String? = null
)

class PetDetailViewModel(
    savedStateHandle: SavedStateHandle // 네비게이션으로 전달된 arguments를 받음
) : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val petId: String? = savedStateHandle["petId"] // NavGraph에서 전달받은 ID

    private val _uiState = MutableStateFlow(PetDetailUiState())
    val uiState: StateFlow<PetDetailUiState> = _uiState.asStateFlow()

    init {
        fetchPetDetail()
    }

    private fun fetchPetDetail() {
        if (petId == null) {
            _uiState.update { it.copy(isLoading = false, error = "펫 ID를 찾을 수 없습니다.") }
            return
        }

        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch

                // 1. 내 정보에서 currentFamilyId 찾기
                val userDoc = db.collection("users").document(uid).get().await()
                val user = userDoc.toObject(User::class.java)
                val familyId = user?.currentFamilyId

                if (familyId != null) {
                    // 2. 가족 -> pets 컬렉션에서 해당 petId 조회
                    val petDoc = db.collection("families").document(familyId)
                        .collection("pets").document(petId)
                        .get().await()

                    val pet = petDoc.toObject(Pet::class.java)

                    if (pet != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                pet = pet,
                                ageText = calculateAge(pet)
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "펫 정보를 찾을 수 없습니다.") }
                    }
                }
            } catch (e: Exception) {
                Log.e("PetDetailVM", "Error fetching pet", e)
                _uiState.update { it.copy(isLoading = false, error = "데이터 로드 실패") }
            }
        }
    }

    // 나이 계산 로직 (HomeScreen과 동일)
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
}
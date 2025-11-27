package com.example.howsu.screen.home

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

data class EditPetUiState(
    val isLoading: Boolean = true,
    val pet: Pet? = null, // 원본 Pet 데이터
    val ageText: String = "", // 계산된 나이 (읽기 전용)

    // 💡 편집 가능한 필드 상태
    val name: String = "",
    val gender: String = "남아", // "남아" 또는 "여아" (UI 표시용)
    val isNeutered: Boolean = false,
    val weight: String = "", // String으로 입력 받음
    val birthdayExact: String? = null,
    val birthdayYearApprox: String? = null,
    val birthdayMonthApprox: String? = null,

    // 💡 저장에 필요한 ID
    val familyId: String? = null,
    val petId: String? = null, // Firestore 문서 ID
    val error: String? = null
)

class EditPetViewModel(
    savedStateHandle: SavedStateHandle // 네비게이션으로 전달된 familyId와 petId를 받음
) : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // 💡 NavArguments에서 familyId와 petId를 추출
    private val familyId: String? = savedStateHandle["familyId"]
    private val petId: String? = savedStateHandle["petId"] // 편집 모드에서는 petId가 필요

    private val _uiState = MutableStateFlow(EditPetUiState())
    val uiState: StateFlow<EditPetUiState> = _uiState.asStateFlow()

    init {
        fetchPetDetail()
    }

    private fun fetchPetDetail() {
        // 인수가 유효한지 확인
        if (familyId.isNullOrEmpty() || petId.isNullOrEmpty()) {
            _uiState.update { it.copy(isLoading = false, error = "가족 ID 또는 펫 ID를 찾을 수 없습니다.") }
            return
        }

        viewModelScope.launch {
            try {
                // petId를 사용하여 특정 Pet 문서 가져오기
                val petDoc = db.collection("families").document(familyId!!)
                    .collection("pets").document(petId!!)
                    .get()
                    .await()

                val pet = petDoc.toObject(Pet::class.java)?.copy(petId = petDoc.id)

                if (pet != null) {
                    // DB 성별 ("MALE", "FEMALE")을 UI 성별 ("남아", "여아")로 변환
                    val translatedGender = translateGender(pet.gender)

                    // 💡 UI State에 편집 가능한 값들을 초기화
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pet = pet, // 원본 데이터 보관
                            ageText = calculateAge(pet),
                            familyId = familyId,
                            petId = petId,

                            // 편집 필드 초기값
                            name = pet.name.orEmpty(),
                            gender = translatedGender,
                            isNeutered = pet.isNeutered ?: false,
                            weight = pet.weight.orEmpty(),
                            birthdayExact = pet.birthdayExact,
                            birthdayYearApprox = pet.birthdayYearApprox,
                            birthdayMonthApprox = pet.birthdayMonthApprox
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "펫 정보를 찾을 수 없습니다.") }
                }
            } catch (e: Exception) {
                Log.e("EditPetVM", "Error fetching pet detail", e)
                _uiState.update { it.copy(isLoading = false, error = "데이터 로드 실패: ${e.message}") }
            }
        }
    }

    // ----------------------------------------------------
    // 데이터 업데이트 함수
    // ----------------------------------------------------

    fun updateName(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    // UI ("남아"/"여아")를 받아서 State 업데이트
    fun updateGender(newGender: String) {
        _uiState.update { it.copy(gender = newGender) }
    }

    fun toggleNeutered() {
        _uiState.update { it.copy(isNeutered = !it.isNeutered) }
    }

    // 체중 입력은 String으로 받음
    fun updateWeight(newWeight: String) {
        _uiState.update { it.copy(weight = newWeight) }
    }

    // TODO: 생년월일 업데이트 함수 (DatePicker 연동 시 필요)

    // ----------------------------------------------------
    // 저장 함수
    // ----------------------------------------------------

    fun savePetDetail() {
        val state = uiState.value

        if (state.familyId.isNullOrEmpty() || state.petId.isNullOrEmpty()) {
            _uiState.update { it.copy(error = "저장에 필요한 정보가 부족합니다.") }
            return
        }

        // UI 성별 ("남아"/"여아")을 DB 성별 ("MALE"/"FEMALE")로 역변환
        val dbGender = reverseTranslateGender(state.gender)

        // 💡 업데이트할 데이터 맵 생성
        val petUpdates = hashMapOf<String, Any?>(
            "name" to state.name,
            "gender" to dbGender,
            "isNeutered" to state.isNeutered,
            "weight" to state.weight.takeIf { it.isNotBlank() }, // 빈 문자열일 경우 null 저장 가능
            "birthdayExact" to state.birthdayExact,
            "birthdayYearApprox" to state.birthdayYearApprox,
            "birthdayMonthApprox" to state.birthdayMonthApprox,
            // profileImageUrl 업데이트 로직은 제외됨
        )

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                db.collection("families").document(state.familyId!!)
                    .collection("pets").document(state.petId!!)
                    .update(petUpdates) // 업데이트 실행
                    .await()

                // TODO: 성공 메시지 또는 상태 업데이트
                Log.d("EditPetVM", "Pet details updated successfully.")

                // 💡 업데이트된 Pet 객체로 UI State의 pet 필드도 갱신
                val updatedPet = state.pet?.copy(
                    name = state.name,
                    gender = dbGender, // DB 값으로 갱신
                    isNeutered = state.isNeutered,
                    weight = state.weight.takeIf { it.isNotBlank() },
                    birthdayExact = state.birthdayExact,
                    birthdayYearApprox = state.birthdayYearApprox,
                    birthdayMonthApprox = state.birthdayMonthApprox,
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pet = updatedPet,
                        ageText = updatedPet?.let { calculateAge(it) } ?: it.ageText
                    )
                }

            } catch (e: Exception) {
                Log.e("EditPetVM", "Error saving pet detail", e)
                _uiState.update { it.copy(isLoading = false, error = "저장 실패: ${e.message}") }
            }
        }
    }

    // ----------------------------------------------------
    // 유틸리티 함수 (PetDetailViewModel과 동일)
    // ----------------------------------------------------

    // DB 성별 ("MALE"/"FEMALE") -> UI 성별 ("남아"/"여아")
    private fun translateGender(gender: String?): String {
        return when (gender?.uppercase()) {
            "MALE" -> "남아"
            "FEMALE" -> "여아"
            else -> "성별미상"
        }
    }

    // UI 성별 ("남아"/"여아") -> DB 성별 ("MALE"/"FEMALE")
    private fun reverseTranslateGender(gender: String): String {
        return when (gender) {
            "남아" -> "MALE"
            "여아" -> "FEMALE"
            else -> "UNKNOWN"
        }
    }

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
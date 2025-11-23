package com.example.howsu.Pet

import androidx.lifecycle.ViewModel
import com.example.howsu.data.model.BirthdayInputType
import com.example.howsu.data.model.Pet
import com.example.howsu.data.model.PetRegisterStep
import com.example.howsu.data.model.PetRegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PetRegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PetRegisterUiState())
    val uiState: StateFlow<PetRegisterUiState> = _uiState

    // 닉네임 (지금은 안 쓰더라도 남겨둬도 됨)
    fun updateNickName(value: String) =
        _uiState.update { it.copy(nickName = value) }

    // 반려동물 이름
    fun updatePetName(value: String) =
        _uiState.update { it.copy(petName = value) }

    fun updateName(value: String) = updatePetName(value)

    // 성별
    fun updateGender(g: String) =
        _uiState.update { it.copy(gender = g) }

    // 몸무게
    fun updateWeight(value: String) =
        _uiState.update { it.copy(weight = value) }

    // 가족 관계 (언니 / 형 / 엄마 …)
    fun updateRelation(value: String) =
        _uiState.update { it.copy(relation = value) }

    // 유저 프로필 이미지
    fun updateUserProfileImage(url: String?) =
        _uiState.update { it.copy(profileUserImageUrl = url) }

    // 반려동물 프로필 이미지
    fun updatePetProfileImage(url: String?) =
        _uiState.update { it.copy(profilePetImageUrl = url) }

    // 생일 입력 타입 (정확/대략)
    fun updateBirthdayType(type: BirthdayInputType) =
        _uiState.update { it.copy(birthdayInputType = type) }

    // 정확한 생일
    fun updateBirthdayExact(date: String) =
        _uiState.update { it.copy(birthdayExact = date) }

    // 대략적 생년 (년도)
    fun updateBirthdayYear(year: String) =
        _uiState.update { it.copy(birthdayYearApprox = year) }

    // 대략적 생년 (월)
    fun updateBirthdayMonth(month: String) =
        _uiState.update { it.copy(birthdayMonthApprox = month) }

    // 중성화 여부
    fun updateNeutered(isNeutered: Boolean) =
        _uiState.update { it.copy(isNeutered = isNeutered) }

    // 다음 단계로 이동
    fun nextStep() {
        _uiState.update { s ->
            val next = when (s.step) {
                PetRegisterStep.PHOTO_NAME    -> PetRegisterStep.GENDER_WEIGHT
                PetRegisterStep.GENDER_WEIGHT -> PetRegisterStep.BIRTHDAY
                PetRegisterStep.BIRTHDAY      -> PetRegisterStep.RELATIONSHIP
                PetRegisterStep.RELATIONSHIP  -> PetRegisterStep.RELATIONSHIP
            }
            s.copy(step = next)
        }
    }

    // 이전 단계로 이동
    fun previousStep() {
        _uiState.update { s ->
            val prev = when (s.step) {
                PetRegisterStep.PHOTO_NAME    -> PetRegisterStep.PHOTO_NAME
                PetRegisterStep.GENDER_WEIGHT -> PetRegisterStep.PHOTO_NAME
                PetRegisterStep.BIRTHDAY      -> PetRegisterStep.GENDER_WEIGHT
                PetRegisterStep.RELATIONSHIP  -> PetRegisterStep.BIRTHDAY
            }
            s.copy(step = prev)
        }
    }

    // 다음 버튼 활성화 여부
    fun isNextEnabled(): Boolean {
        val s = _uiState.value
        return when (s.step) {
            // 반려동물 이름 단계
            PetRegisterStep.PHOTO_NAME ->
                s.petName.isNotBlank()

            // 성별 + 몸무게 단계
            PetRegisterStep.GENDER_WEIGHT ->
                s.gender != null && s.weight.isNotBlank()

            // 생일 단계
            PetRegisterStep.BIRTHDAY ->
                when (s.birthdayInputType) {
                    BirthdayInputType.EXACT ->
                        s.birthdayExact.isNotBlank()

                    BirthdayInputType.APPROX ->
                        s.birthdayYearApprox.isNotBlank() &&
                                s.birthdayMonthApprox.isNotBlank()
                }

            // 가족 관계 등록 단계
            PetRegisterStep.RELATIONSHIP ->
                s.relation.isNotBlank()
        }
    }

    // 저장
    fun submit(onFinished: (Pet) -> Unit) {
        val s = _uiState.value

        val pet = Pet(
            name = s.petName,
            gender = s.gender,
            profileImageUrl = s.profilePetImageUrl,
            weight = s.weight.ifBlank { null },
            isNeutered = s.isNeutered,
            relation = s.relation,

            birthdayInputType = s.birthdayInputType,
            birthdayExact = s.birthdayExact.ifBlank { null },
            birthdayYearApprox = s.birthdayYearApprox.ifBlank { null },
            birthdayMonthApprox = s.birthdayMonthApprox.ifBlank { null }
        )

        onFinished(pet)
    }

    fun resetForNewPet() {
        _uiState.update { s ->
            s.copy(
                step = PetRegisterStep.PHOTO_NAME,
                petName = "",
                gender = null,
                weight = "",
                isNeutered = null,
                birthdayInputType = BirthdayInputType.EXACT,
                birthdayExact = "",
                birthdayYearApprox = "",
                birthdayMonthApprox = "",
                relation = ""
            )
        }
    }
}

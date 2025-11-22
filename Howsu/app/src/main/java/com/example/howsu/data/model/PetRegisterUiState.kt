package com.example.howsu.data.model


//입력 중인 임시 단계 데이터
data class PetRegisterUiState(
    val step: PetRegisterStep = PetRegisterStep.PHOTO_NAME,

    val nickName: String = "",
    val petName: String = "",
    val gender: String? = null,
    val relation: String = "",   //가족 관계

    val profileUserImageUrl: String? = null,   // 유저 프로필
    val profilePetImageUrl: String? = null,    // 펫 프로필

    val weight: String = "",
    val isNeutered: Boolean? = null,

    val birthdayInputType: BirthdayInputType = BirthdayInputType.EXACT,
    val birthdayExact: String = "",
    val birthdayYearApprox: String = "",
    val birthdayMonthApprox: String = ""
)

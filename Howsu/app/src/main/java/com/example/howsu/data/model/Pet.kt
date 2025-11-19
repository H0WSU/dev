package com.example.howsu.data.model

enum class BirthdayInputType {
    EXACT,      // 정확한 생일(날짜)
    APPROX      // 대략적인 년/월
}

data class Pet(
    val strp: PetRegisterStep = PetRegisterStep.PHOTO_NAME,
    val petId: String? = null,
    val name: String = "",
    val gender: String? = null,
    val profileImageUrl: String? = null, // 프로필 사진 URL (Coil 등으로 로드)
    val weight: String? = null,
    val isNeutered: Boolean? = null, //중성화 여부

    // 생일 관련
    val birthdayInputType: BirthdayInputType = BirthdayInputType.EXACT,
    val birthdayExact: String? = "",        // YYYY-MM-DD
    val birthdayYearApprox: String? = "",   // YYYY
    val birthdayMonthApprox: String? = ""   // 1~12
)



package com.example.howsu.data.model

//단계
enum class PetRegisterStep {
    NICKNAME,        // 0: 닉네임 등록 (필수)
    PHOTO_NAME,      // 1: 사진 + 이름
    GENDER_WEIGHT,   // 2: 성별 + 몸무게
    BIRTHDAY         // 3: 생년월일
}

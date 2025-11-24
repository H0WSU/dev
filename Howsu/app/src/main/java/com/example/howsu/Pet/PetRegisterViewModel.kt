package com.example.howsu.Pet

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.howsu.data.model.BirthdayInputType
import com.example.howsu.data.model.Pet
import com.example.howsu.data.model.PetRegisterStep
import com.example.howsu.data.model.PetRegisterUiState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PetRegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PetRegisterUiState())
    val uiState: StateFlow<PetRegisterUiState> = _uiState

    // 추가한 부분
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage // 이게 없으면 껐다 켰을 때 프로필 사진이 사라짐

    // 닉네임
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
    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    fun submit(onFinished: (Pet) -> Unit) {
        val s = _uiState.value
        val user = auth.currentUser ?: return

        // [속임수] 화면에 보여줄 '임시 펫 데이터'를 먼저 만듦
        val tempPet = Pet(
            name = s.petName,
            gender = s.gender,
            profileImageUrl = s.profilePetImageUrl, // 로컬 주소
            weight = s.weight.ifBlank { null },
            isNeutered = s.isNeutered,
            relation = s.relation,
            birthdayInputType = s.birthdayInputType,
            birthdayExact = s.birthdayExact.ifBlank { null },
            birthdayYearApprox = s.birthdayYearApprox.ifBlank { null },
            birthdayMonthApprox = s.birthdayMonthApprox.ifBlank { null }
        )

        // 화면부터 먼저 넘겨 버림
        onFinished(tempPet)

        // [백그라운드 작업] 사용자가 다음 화면 구경하는 동안 뒤에서 몰래 업로드 & 저장
        GlobalScope.launch {
            try {
                // 가족 ID 가져오기
                val userDoc = db.collection("users").document(user.uid).get().await()
                val familyId = userDoc.getString("currentFamilyId")

                if (familyId != null) {
                    var finalProfileUrl = s.profilePetImageUrl

                    // A. 이미지 업로드 (시간이 걸리는 작업)
                    if (finalProfileUrl != null && finalProfileUrl.startsWith("content://")) {
                        val imageUri = Uri.parse(finalProfileUrl)
                        val fileName = "${java.util.UUID.randomUUID()}.jpg"
                        val storageRef = storage.reference.child("pet_images/$fileName")

                        // 여기서 시간 걸려도 사용자는 모름 (이미 화면 넘어감)
                        storageRef.putFile(imageUri).await()
                        finalProfileUrl = storageRef.downloadUrl.await().toString()
                        Log.d("PetRegisterVM", "몰래 업로드 성공: $finalProfileUrl")
                    }

                    // B. 진짜 저장할 펫 객체 (이제 인터넷 주소 https:// 로 교체됨)
                    val realPet = tempPet.copy(profileImageUrl = finalProfileUrl)

                    // C. DB 저장
                    db.collection("families").document(familyId)
                        .collection("pets")
                        .add(realPet)
                        .await()

                    // D. 관계 업데이트
                    if (s.relation.isNotBlank()) {
                        db.collection("families").document(familyId)
                            .collection("members").document(user.uid)
                            .update("relationship", s.relation)
                            .await()
                    }

                    Log.d("PetRegisterVM", "몰래 DB 저장까지 완료!")

                } else {
                    Log.e("PetRegisterVM", "가족 정보 없음")
                }
            } catch (e: Exception) {
                // 이미 화면은 넘어갔으니 에러 나도 사용자한테는 안 보임 (로그만 남김)
                Log.e("PetRegisterVM", "백그라운드 저장 실패", e)
            }
        }
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
                relation = "",
                profilePetImageUrl = null // 이미지 초기화
            )
        }
    }
}

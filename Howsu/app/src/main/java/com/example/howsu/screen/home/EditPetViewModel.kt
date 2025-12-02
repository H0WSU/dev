package com.example.howsu.screen.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.FamilyMember // 필요하지 않다면 제거
import com.example.howsu.data.model.Pet
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import com.google.firebase.storage.ktx.storage

data class EditPetUiState(
    val isLoading: Boolean = true,
    val error: String? = null, // 에러 메시지
    val isEditing: Boolean = false, // 현재 편집 모드인지 확인

    val pet: Pet? = null, // 원본 Pet 데이터
    val ageText: String = "", // 계산된 나이 (읽기 전용)

    // 💡 편집 가능한 필드 상태
    val petprofileImageUrl: String? = null,
    val newPetprofileImageUri: Uri?= null,   // 새 이미지 로컬 uri
    val petname: String = "",   // 이름
    val gender: String = "남아", // 성별
    val isNeutered: Boolean = false,   // 중성화 여부
    val weight: String = "", // 몸무게
    val birthdayExact: String? = null,
    val birthdayYearApprox: String? = null,
    val birthdayMonthApprox: String? = null,

    // 💡 저장에 필요한 ID
    val familyId: String? = null,
    val petId: String? = null, // Firestore 문서 ID
)

class EditPetViewModel(
    savedStateHandle: SavedStateHandle // 네비게이션으로 전달된 familyId와 petId를 받음
) : ViewModel() {

    private val db = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage

    // 💡 NavArguments에서 familyId와 petName을 추출 (NavArgument가 petId를 전달한다고 가정)
    private val navFamilyId: String? = savedStateHandle["familyId"]
    private val navPetId: String? = savedStateHandle["petId"] // petId를 네비게이션으로 받는다고 가정

    private var originalPet: Pet?= null
    // 편집 취소 시 복구를 위한 원본 데이터 저장

    private val _uiState = MutableStateFlow(EditPetUiState(
        familyId = navFamilyId,
        petId = navPetId,
        isLoading = true
    ))
    val uiState: StateFlow<EditPetUiState> = _uiState.asStateFlow()

    init {
        // NavArguments가 유효할 때만 데이터 로드 시작
        if (!navFamilyId.isNullOrEmpty() && !navPetId.isNullOrEmpty()) {
            fetchPetDetail()
        } else {
            _uiState.update { it.copy(isLoading = false, error = "가족 ID 또는 펫 ID가 누락되었습니다.") }
        }
    }

    // firebase에서 펫 상세 정보를 로드함
    private fun fetchPetDetail() {
        val currentFamilyId = navFamilyId ?: return
        val currentPetId = navPetId ?: return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val petDoc = db.collection("families").document(currentFamilyId)
                    .collection("pets").document(currentPetId)
                    .get().await()

                val pet = petDoc.toObject(Pet::class.java)?.copy(petId = petDoc.id)

                if (pet != null) {
                    originalPet = pet // 원본 저장

                    // DB 성별을 UI 성별로 변환
                    val translatedGender = translateGender(pet.gender)

                    _uiState.update { currentState ->
                        currentState.copy(
                            pet = pet,
                            ageText = calculateAge(pet),
                            isLoading = false,
                            isEditing = false,

                            // 편집 필드 초기화
                            petprofileImageUrl = pet.profileImageUrl,
                            petname = pet.name.orEmpty(),
                            gender = translatedGender,
                            isNeutered = pet.isNeutered ?: false,
                            weight = pet.weight.orEmpty(),
                            birthdayExact = pet.birthdayExact,
                            birthdayYearApprox = pet.birthdayYearApprox,
                            birthdayMonthApprox = pet.birthdayMonthApprox,
                        )
                    }
                } else {
                    handleFailure("펫 정보를 찾을 수 없습니다.")
                }
            } catch (e: Exception) {
                Log.e("EditPetVM", "Error fetching pet detail: ${e.message}", e)
                handleFailure("데이터 로드 실패: ${e.message}")
            }
        }
    }

    // 모든 에러 처리를 담당하고 UI 상태를 업데이트함
    private fun handleFailure(errorMessage: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = errorMessage
            )
        }
    }

    /**
     * Firestore에 이미지 URL을 포함한 모든 펫 정보 저장
     */
    private fun savePetprofileFirestore(
        petId: String,
        familyId: String,
        newPetprofileImageUrl: String?,
        newpetName: String,
        newGender: String, // UI gender ("남아"/"여아")
        newNeutered: Boolean,
        newWeight: String,
        newBirthdayExact: String?,
        newBirthdayYearApprox: String?,
        newBirthdayMonthApprox: String?
    ) {
        // UI 성별을 DB 성별로 역변환
        val dbGender = reverseTranslateGender(newGender)

        val updates = mapOf(
            "profileImageUrl" to newPetprofileImageUrl,
            "name" to newpetName,
            "gender" to dbGender,
            "isNeutered" to newNeutered,
            "weight" to newWeight.takeIf { it.isNotBlank() }, // 빈 문자열일 경우 null 저장
            "birthdayExact" to newBirthdayExact,
            "birthdayYearApprox" to newBirthdayYearApprox,
            "birthdayMonthApprox" to newBirthdayMonthApprox
        )

        db.collection("families").document(familyId)
            .collection("pets").document(petId)
            .update(updates)
            .addOnSuccessListener {
                // 저장 성공 시 상태 업데이트 헬퍼 함수 호출
                updateSuccessState(
                    newPetprofileImageUrl,
                    newpetName,
                    newGender,
                    newNeutered,
                    newWeight,
                    newBirthdayExact,
                    newBirthdayYearApprox,
                    newBirthdayMonthApprox
                )
            }
            .addOnFailureListener { exception ->
                handleFailure("펫 정보 저장 실패: ${exception.message}")
            }
    }

    /**
     * Firebase Storage에 이미지를 업로드하고 Firestore 저장을 호출함
     */
    private fun uploadImageAndSavePetprofile(petId: String, familyId: String, imageUri: Uri, newName: String) {
        val state = _uiState.value

        val imageRef = storage.reference.child("pet_profile_images/$petId/pet_profile.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // 2단계: Firestore에 새 URL과 모든 필드 저장 호출
                    savePetprofileFirestore(
                        petId,
                        familyId,
                        downloadUri.toString(), // 새 이미지 URL
                        newName,
                        state.gender,
                        state.isNeutered,
                        state.weight,
                        state.birthdayExact,
                        state.birthdayYearApprox,
                        state.birthdayMonthApprox
                    )
                }
                    .addOnFailureListener { exception ->
                        handleFailure("이미지 URL 가져오기 실패: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                handleFailure("이미지 업로드 실패: ${exception.message}")
            }
    }

    /**
     * 저장 성공 시 UI 상태 업데이트 헬퍼 함수
     */
    private fun updateSuccessState(
        newImageUrl: String?,
        newpetName: String,
        newGender: String,
        newNeutered: Boolean,
        newWeight: String,
        newBirthdayExact: String?,
        newBirthdayYearApprox: String?,
        newBirthdayMonthApprox: String?
    ){
        val dbGender = reverseTranslateGender(newGender)

        // 원본 Pet 객체 및 UI State 업데이트
        originalPet = originalPet?.copy(
            profileImageUrl = newImageUrl,
            name = newpetName,
            gender = dbGender, // DB 값으로 갱신
            isNeutered = newNeutered,
            weight = newWeight.takeIf { it.isNotBlank() },
            birthdayExact = newBirthdayExact,
            birthdayYearApprox = newBirthdayYearApprox,
            birthdayMonthApprox = newBirthdayMonthApprox
        )

        val updatedPet = originalPet // 원본을 갱신했으므로 pet 필드도 갱신

        _uiState.update{
            it.copy(
                isLoading = false,
                isEditing = false,
                pet = updatedPet,
                ageText = updatedPet?.let { calculateAge(it) } ?: it.ageText, // 나이 갱신
                petprofileImageUrl = newImageUrl,
                petname = newpetName,
                gender = newGender,
                isNeutered = newNeutered,
                weight = newWeight,
                birthdayExact = newBirthdayExact,
                birthdayYearApprox = newBirthdayYearApprox,
                birthdayMonthApprox = newBirthdayMonthApprox,
                newPetprofileImageUri = null // 임시 URI 제거
            )
        }
    }

    /**
     * 최종 저장 로직 (이미지 유무에 따라 분기)
     */
    fun savePetProfile(){
        val state = _uiState.value
        val currentPetId = state.petId
        val currentFamilyId = state.familyId

        if(currentPetId.isNullOrEmpty() || currentFamilyId.isNullOrEmpty()){
            _uiState.update{it.copy(error = "펫 또는 가족 인증 정보가 부족합니다.")}
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }

        val newImageUri = state.newPetprofileImageUri
        val newpetName = state.petname

        if(newImageUri != null){
            // 새 이미지가 선택된 경우 -> storage에 업데이트 후 firestore 업데이트
            uploadImageAndSavePetprofile(currentPetId, currentFamilyId, newImageUri, newpetName)
        } else{
            // 이미지가 변경되지 않은 경우 -> 모든 텍스트/데이터 필드만 firestore 업데이트
            savePetprofileFirestore(
                currentPetId,
                currentFamilyId,
                state.petprofileImageUrl, // 기존 이미지 URL 사용
                newpetName,
                state.gender,
                state.isNeutered,
                state.weight,
                state.birthdayExact,
                state.birthdayYearApprox,
                state.birthdayMonthApprox
            )
        }
    }


    // ----------------------------------------------------
    // 데이터 업데이트 함수
    // ----------------------------------------------------

    fun updatePetProfileImgaeUri(uri: Uri){
        _uiState.update { it.copy(newPetprofileImageUri = uri) }
    }

    fun updateName(newpetName: String) {
        _uiState.update { it.copy(petname = newpetName) }
    }

    fun updateGender(newGender: String) {
        _uiState.update { it.copy(gender = newGender) }
    }

    fun toggleNeutered() {
        _uiState.update { it.copy(isNeutered = !it.isNeutered) }
    }

    fun toggledEditMode(enable: Boolean) {
        _uiState.update { it.copy(isEditing = enable) }
    }

    fun updateWeight(newWeight: String) {
        _uiState.update { it.copy(weight = newWeight) }
    }

    /**
     * 편집 취소: 모든 입력값을 원본 데이터로 되돌리고 편집 모드 해제
     */
    fun cancelEditing() {
        if (originalPet != null) {
            val pet = originalPet!!
            // DB 성별을 UI 성별로 역변환
            val translatedGender = translateGender(pet.gender)

            _uiState.update { currentState ->
                currentState.copy(
                    petname = pet.name.orEmpty(),
                    petprofileImageUrl = pet.profileImageUrl,
                    gender = translatedGender,
                    isNeutered = pet.isNeutered ?: false,
                    weight = pet.weight.orEmpty(),
                    birthdayExact = pet.birthdayExact,
                    birthdayYearApprox = pet.birthdayYearApprox,
                    birthdayMonthApprox = pet.birthdayMonthApprox,
                    newPetprofileImageUri = null,
                    isEditing = false
                )
            }
        } else {
            _uiState.update { it.copy(isEditing = false, newPetprofileImageUri = null) }
        }
    }

    // TODO: 생년월일 업데이트 함수 (DatePicker 연동 시 필요)
    // 예시:
    fun updateBirthdayExact(date: LocalDate?) {
        _uiState.update {
            it.copy(
                birthdayExact = date?.format(DateTimeFormatter.ISO_DATE),
                ageText = date?.let { calculateAge(it) } ?: "?세"
            )
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
                calculateAge(birthDate)
            } else if (!pet.birthdayYearApprox.isNullOrEmpty()) {
                val birthYear = pet.birthdayYearApprox.toInt()
                val currentYear = LocalDate.now().year
                "${currentYear - birthYear}세"
            } else {
                "?세"
            }
        } catch (e: Exception) { "?세" }
    }

    private fun calculateAge(birthDate: LocalDate): String {
        val now = LocalDate.now()
        return "${Period.between(birthDate, now).years}세"
    }
}
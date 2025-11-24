package com.example.howsu.screen.family

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.PetRegisterUiState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SetRelationshipViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // 기존 UI State 재활용 (화면에 보여주기 위해)
    private val _uiState = MutableStateFlow(PetRegisterUiState())
    val uiState: StateFlow<PetRegisterUiState> = _uiState

    init {
        fetchFamilyPet()
    }

    // 1. 우리 가족의 펫 정보 불러오기
    private fun fetchFamilyPet() {
        val user = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                // 내 가족 ID 찾기
                val userDoc = db.collection("users").document(user.uid).get().await()
                val familyId = userDoc.getString("currentFamilyId")

                if (familyId != null) {
                    // 가족의 펫 목록 가져오기 (첫 번째 펫 기준)
                    val petsSnapshot = db.collection("families").document(familyId)
                        .collection("pets").limit(1).get().await()

                    if (!petsSnapshot.isEmpty) {
                        val petDoc = petsSnapshot.documents[0]
                        val petName = petDoc.getString("name") ?: "반려동물"
                        val petImage = petDoc.getString("profileImageUrl")

                        // UI 상태 업데이트
                        _uiState.update {
                            it.copy(
                                petName = petName,
                                profilePetImageUrl = petImage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SetRelationVM", "펫 정보 로드 실패", e)
            }
        }
    }

    // 2. 관계 선택 시 업데이트
    fun updateRelation(relation: String) {
        _uiState.update { it.copy(relation = relation) }
    }

    // 3. 저장하고 홈으로 이동
    fun saveRelationship(onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return
        val relation = _uiState.value.relation

        if (relation.isBlank()) return // 선택 안 했으면 무시

        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(user.uid).get().await()
                val familyId = userDoc.getString("currentFamilyId")

                if (familyId != null) {
                    // 내 멤버 정보에 관계(호칭) 업데이트
                    db.collection("families").document(familyId)
                        .collection("members").document(user.uid)
                        .update("relationship", relation)
                        .await()

                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SetRelationVM", "관계 저장 실패", e)
            }
        }
    }
}
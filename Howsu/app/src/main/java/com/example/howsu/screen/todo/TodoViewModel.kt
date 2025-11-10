package com.example.howsu.screen.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.TodoGroup
import com.google.firebase.firestore.ktx.firestore // ★ Firebase Firestore 의존성 필요
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TodoViewModel : ViewModel() {

    // 1. 실시간 할 일 목록을 담을 StateFlow
    // (UI는 이 변수를 구독(collect)합니다)
    private val _todoGroups = MutableStateFlow<List<TodoGroup>>(emptyList())
    val todoGroups = _todoGroups.asStateFlow()

    // 2. Firestore 인스턴스
    private val db = Firebase.firestore

    init {
        // 3. ViewModel이 생성되자마자 Firestore의 "todoGroups" 컬렉션을 구독
        fetchTodoGroups()
    }

    private fun fetchTodoGroups() {
        // "todoGroups" 컬렉션의 변경을 실시간으로 감지
        db.collection("todoGroups")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // 에러 처리
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // 4. Firestore 문서를 TodoGroup 객체 리스트로 변환
                    val groups = snapshot.toObjects<TodoGroup>()
                    // 5. StateFlow에 새 리스트를 업데이트 (UI가 자동으로 새로고침됨)
                    _todoGroups.value = groups
                }
            }
    }

    // 6. 삭제 함수 (DropdownMenu에서 사용)
    fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            // 1. 'id' 필드 값이 'groupId'와 일치하는 문서를 찾습니다.
            db.collection("todoGroups")
                .whereEqualTo("id", groupId) // ★ 우리가 저장한 숫자 ID로 검색
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // 2. 검색된 문서가 있다면
                    if (querySnapshot.documents.isNotEmpty()) {
                        // 3. 그 문서의 '진짜' ID (예: aBcD123Xyz)를 가져옵니다.
                        val documentId = querySnapshot.documents[0].id

                        // 4. '진짜' ID를 사용해 문서를 삭제합니다.
                        db.collection("todoGroups").document(documentId)
                            .delete()
                            .addOnSuccessListener {
                                println("삭제 성공: $documentId")
                                // (삭제가 성공하면 fetchTodoGroups()가 실시간으로 감지해서
                                //  _todoGroups.value가 자동으로 업데이트됩니다)
                            }
                            .addOnFailureListener { e -> println("삭제 실패: $e") }
                    } else {
                        println("삭제할 문서를 찾지 못했습니다 (ID: $groupId)")
                    }
                }
                .addOnFailureListener { e ->
                    println("문서 검색 실패: $e")
                }
        }
    }
}
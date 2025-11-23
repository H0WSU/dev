package com.example.howsu.screen.feed

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.FeedFilter
import com.example.howsu.data.model.FeedPost
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FeedViewModel : ViewModel() {

    // Firebase 인스턴스 선언
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _currentMember = MutableStateFlow<FamilyMember?>(null)
    val currentMember: StateFlow<FamilyMember?> = _currentMember.asStateFlow()

    // 게시물 리스트
    private val _posts = mutableStateListOf<FeedPost>()
    val posts: List<FeedPost> get() = _posts

    // 현재 선택된 탭
    var selectedFilter by mutableStateOf(FeedFilter.ALL)
        private set

    fun fetchMyProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _currentMember.value = null
            return
        }

        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid).get().await()

                if (userDoc.exists()) {
                    val nickname = userDoc.getString("name") ?: "알 수 없음"

                    // DB에서 사진 주소 가져오기
                    val profileUrl = userDoc.getString("profileImageUrl")

                    val me = FamilyMember(
                        userId = uid,
                        familyId = "",
                        nickName = nickname,
                        profileImageUrl = profileUrl, // 여기에 넣기
                        relationship = "나"
                    )
                    _currentMember.value = me
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "로드 실패", e)
            }
        }
    }

    // 탭에 따라 화면에 보여줄 리스트
    val filteredPosts :  List<FeedPost>
        get(){
            val base = when(selectedFilter) {
                FeedFilter.ALL -> posts
                FeedFilter.TEXT -> posts.filter {
                    it.imageUris.isEmpty() && it.videoUris.isEmpty()
                }

                FeedFilter.IMAGE -> posts.filter {
                    it.imageUris.isNotEmpty()
                }

                FeedFilter.VIDEO -> posts.filter {
                    it.videoUris.isNotEmpty()
                }
            }
            // 등록순(최근 등록이 위로 오게) 정렬
            return base.sortedByDescending { it.createdAt }
        }

    fun changeFilter(filter : FeedFilter){
        selectedFilter = filter
    }

    // 글 + 사진/동영상 첨부해서 등록
    fun addPost(
        title : String,
        content : String,
        imageUris : List<String>,
        videoUris : List<String>,
        hashtags : List<String>
    ){
        val newPost = FeedPost(
            title = title,
            content = content,
            imageUris = imageUris,
            videoUris = videoUris,
            hashtags = hashtags
        )
        _posts.add(newPost)
    }

    // 수정
    fun updatePost(
        id: Long,
        title: String,
        content: String,
        imageUris: List<String>,
        videoUris: List<String>,
        hashtags: List<String>
    ) {
        val index = _posts.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = _posts[index]
            _posts[index] = old.copy(
                title = title,
                content = content,
                imageUris = imageUris,
                videoUris = videoUris,
                hashtags = hashtags
            )
        }
    }

    // 삭제
    fun deletePost(id: Long) {
        _posts.removeAll { it.id == id }
    }
}
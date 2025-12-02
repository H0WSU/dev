package com.example.howsu.screen.feed

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.howsu.data.model.Comment
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.FeedFilter
import com.example.howsu.data.model.FeedPost
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FeedViewModel : ViewModel() {

    // Firebase 인스턴스
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // 현재 로그인중인 FamilyMember (프로필, 닉네임)
    private val _currentMember = MutableStateFlow<FamilyMember?>(null)
    val currentMember: StateFlow<FamilyMember?> = _currentMember.asStateFlow()

    // 피드 목록
    private val _posts = mutableStateListOf<FeedPost>()
    val posts: List<FeedPost> get() = _posts

    // 현재 선택된 탭
    var selectedFilter by mutableStateOf(FeedFilter.ALL)
        private set

    // 댓글 상태
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    // 내가 좋아요 누른 글 ID들 (상세화면에서 하트 상태 표시용)
    private val _likedPostIds = MutableStateFlow<Set<Long>>(emptySet())
    val likedPostIds: StateFlow<Set<Long>> = _likedPostIds.asStateFlow()

    // 내가 댓글에 좋아요 누른 댓글 ID들
    private val _likedCommentIds = MutableStateFlow<Set<String>>(emptySet())
    val likedCommentIds: StateFlow<Set<String>> = _likedCommentIds.asStateFlow()


    /* -------------------------------------------------------------
       1) 내 프로필(FamilyMember) 불러오기
       ------------------------------------------------------------- */
    fun fetchMyProfile() {
        val uid = auth.currentUser?.uid ?: run {
            _currentMember.value = null
            return
        }

        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid).get().await()

                if (userDoc.exists()) {
                    val nickname = userDoc.getString("name") ?: "알 수 없음"
                    val profileUrl = userDoc.getString("profileImageUrl")

                    val me = FamilyMember(
                        userId = uid,
                        familyId = "",
                        nickName = nickname,
                        profileImageUrl = profileUrl,
                        relationship = "나"
                    )
                    _currentMember.value = me
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "fetchMyProfile 실패", e)
            }
        }
    }

    /* -------------------------------------------------------------
       2) 피드 목록 불러오기 (Firestore → _posts)
       ------------------------------------------------------------- */
    fun loadPosts() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("feeds")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FeedPost::class.java)
                }

                _posts.clear()
                _posts.addAll(list)
            } catch (e: Exception) {
                Log.e("FeedViewModel", "loadPosts 실패", e)
            }
        }
    }

    /* -------------------------------------------------------------
       3) 탭 필터링 (ALL / TEXT / IMAGE / VIDEO)
       ------------------------------------------------------------- */
    val filteredPosts: List<FeedPost>
        get() {
            val base = when (selectedFilter) {
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
            return base.sortedByDescending { it.createdAt }
        }

    fun changeFilter(filter: FeedFilter) {
        selectedFilter = filter
    }

    /* -------------------------------------------------------------
       4) 글 작성 (Firestore 저장 + 로컬 리스트 추가)
       ------------------------------------------------------------- */
    fun addPost(
        title: String,
        content: String,
        imageUris: List<String>,
        videoUris: List<String>,
        hashtags: List<String>,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: return
        val me = currentMember.value

        val newPost = FeedPost(
            id = System.currentTimeMillis(),
            authorId = uid,
            authorName = me?.nickName ?: "익명",
            authorProfileImage = me?.profileImageUrl,
            title = title,
            content = content,
            imageUris = imageUris,
            videoUris = videoUris,
            hashtags = hashtags,
            likeCount = 0,
            commentCount = 0,
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                db.collection("feeds")
                    .document(newPost.id.toString())
                    .set(newPost)
                    .await()

                _posts.add(0, newPost)
                onSuccess()
            } catch (e: Exception) {
                Log.e("FeedViewModel", "addPost 실패", e)
                onError(e)
            }
        }
    }

    /* -------------------------------------------------------------
       5) 글 수정 (Firestore + 로컬 둘 다)
       ------------------------------------------------------------- */
    fun updatePost(
        id: Long,
        title: String,
        content: String,
        imageUris: List<String>,
        videoUris: List<String>,
        hashtags: List<String>,
        onFinish: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val updateMap = mapOf(
                    "title" to title,
                    "content" to content,
                    "imageUris" to imageUris,
                    "videoUris" to videoUris,
                    "hashtags" to hashtags
                )

                db.collection("feeds")
                    .document(id.toString())
                    .update(updateMap)
                    .await()

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

                onFinish()
            } catch (e: Exception) {
                Log.e("FeedViewModel", "updatePost 실패", e)
            }
        }
    }

    /* -------------------------------------------------------------
       6) 글 삭제
       ------------------------------------------------------------- */
    fun deletePost(id: Long, onFinish: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                db.collection("feeds")
                    .document(id.toString())
                    .delete()
                    .await()

                _posts.removeAll { it.id == id }
                onFinish()
            } catch (e: Exception) {
                Log.e("FeedViewModel", "deletePost 실패", e)
            }
        }
    }

    /* -------------------------------------------------------------
       7) 좋아요 토글 + 개수 갱신
       feeds/{postId}/likes/{uid}
       ------------------------------------------------------------- */
    fun loadLikeStateForPost(postId: Long) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val snap = db.collection("feeds")
                    .document(postId.toString())
                    .collection("likes")
                    .document(uid)
                    .get()
                    .await()

                _likedPostIds.value =
                    if (snap.exists()) _likedPostIds.value + postId
                    else _likedPostIds.value - postId
            } catch (e: Exception) {
                Log.e("FeedViewModel", "loadLikeStateForPost 실패", e)
            }
        }
    }

    fun toggleLike(postId: Long) {
        val uid = auth.currentUser?.uid ?: return

        val likeRef = db.collection("feeds")
            .document(postId.toString())
            .collection("likes")
            .document(uid)

        viewModelScope.launch {
            try {
                val snap = likeRef.get().await()

                if (snap.exists()) {
                    // 좋아요 취소
                    likeRef.delete().await()
                    _likedPostIds.value = _likedPostIds.value - postId
                } else {
                    // 좋아요 추가
                    likeRef.set(mapOf("isLike" to true)).await()
                    _likedPostIds.value = _likedPostIds.value + postId
                }

                // 좋아요 개수 다시 계산
                val feedRef = db.collection("feeds").document(postId.toString())
                val likesSnap = feedRef.collection("likes").get().await()
                val count = likesSnap.size()

                feedRef.update("likeCount", count).await()

                val index = _posts.indexOfFirst { it.id == postId }
                if (index != -1) {
                    val old = _posts[index]
                    _posts[index] = old.copy(likeCount = count)
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "toggleLike 실패", e)
            }
        }
    }


    /**
     * 댓글 좋아요 토글
     * feeds/{postId}/comments/{commentId}/likes/{uid} 구조로 저장
     */
    fun toggleCommentLike(postId: Long, commentId: String) {
        val uid = auth.currentUser?.uid ?: return

        val commentRef = db.collection("feeds")
            .document(postId.toString())
            .collection("comments")
            .document(commentId)

        val likeRef = commentRef
            .collection("likes")
            .document(uid)

        viewModelScope.launch {
            try {
                val snap = likeRef.get().await()

                if (snap.exists()) {
                    // 좋아요 취소
                    likeRef.delete().await()
                    _likedCommentIds.value = _likedCommentIds.value - commentId
                } else {
                    // 좋아요 추가
                    likeRef.set(mapOf("isLike" to true)).await()
                    _likedCommentIds.value = _likedCommentIds.value + commentId
                }

                // 좋아요 개수 다시 계산
                val likesSnap = commentRef.collection("likes").get().await()
                val count = likesSnap.size()

                // 댓글 문서에 likeCount 업데이트
                commentRef.update("likeCount", count).await()

                // 로컬 _comments 에도 반영
                _comments.value = _comments.value.map { c ->
                    if (c.id == commentId) c.copy(likeCount = count) else c
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "toggleCommentLike 실패", e)
            }
        }
    }

    /**
     * 해당 게시글의 댓글들 중, 내가 좋아요 누른 댓글 ID들 로딩
     */
    fun loadCommentLikeState(postId: Long) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val commentsSnap = db.collection("feeds")
                    .document(postId.toString())
                    .collection("comments")
                    .get()
                    .await()

                val likedIds = mutableSetOf<String>()

                for (doc in commentsSnap.documents) {
                    val likeDoc = doc.reference
                        .collection("likes")
                        .document(uid)
                        .get()
                        .await()

                    if (likeDoc.exists()) {
                        likedIds += doc.id
                    }
                }

                _likedCommentIds.value = likedIds.toSet()
            } catch (e: Exception) {
                Log.e("FeedViewModel", "loadCommentLikeState 실패", e)
            }
        }
    }



    /**
     * 댓글(대댓글 포함) 전부 불러오기
     * feeds/{postId}/comments 전체를 flat하게 가져와서 _comments에 넣음
     */
    fun fetchComments(postId: Long) {
        viewModelScope.launch {
            try {
                val snap = db.collection("feeds")
                    .document(postId.toString())
                    .collection("comments")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val list = snap.documents.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(
                        id = doc.id,
                        postId = postId
                    )
                }

                _comments.value = list

                // 댓글 수(모든 깊이 합산)를 FeedPost에도 반영
                val count = list.size
                val feedRef = db.collection("feeds").document(postId.toString())
                feedRef.update("commentCount", count).await()

                val index = _posts.indexOfFirst { it.id == postId }
                if (index != -1) {
                    val old = _posts[index]
                    _posts[index] = old.copy(commentCount = count)
                }

            } catch (e: Exception) {
                Log.e("FeedViewModel", "fetchComments 실패", e)
            }
        }
    }

    /**
     * 댓글/대댓글 추가
     * parentCommentId가 null이면 일반 댓글, 아니면 대댓글
     */
    fun addComment(
        postId: Long,
        content: String,
        parentCommentId: String? = null
    ) {
        val uid = auth.currentUser?.uid ?: return
        val me = currentMember.value ?: return

        val commentMap = mapOf(
            "postId" to postId,
            "parentCommentId" to parentCommentId,
            "userId" to uid,
            "userName" to me.nickName,
            "userProfileImage" to me.profileImageUrl,
            "content" to content,
            "createdAt" to System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                db.collection("feeds")
                    .document(postId.toString())
                    .collection("comments")
                    .add(commentMap)
                    .await()

                // 다시 전체 댓글 로드해서 상태/개수 갱신
                fetchComments(postId)

            } catch (e: Exception) {
                Log.e("FeedViewModel", "addComment 실패", e)
            }
        }
    }

    /** 댓글 내용 수정 */
    fun updateComment(
        postId: Long,
        commentId: String,
        newContent: String
    ) {
        viewModelScope.launch {
            try {
                db.collection("feeds")
                    .document(postId.toString())
                    .collection("comments")
                    .document(commentId)
                    .update("content", newContent)
                    .await()

                fetchComments(postId)
            } catch (e: Exception) {
                Log.e("FeedViewModel", "updateComment 실패", e)
            }
        }
    }

    /**
     * 댓글 삭제 (해당 댓글 + 모든 자식 대댓글 재귀 삭제)
     * 깊이 제한 없음
     */
    fun deleteComment(postId: Long, commentId: String) {
        viewModelScope.launch {
            try {
                deleteCommentRecursive(postId, commentId)
                fetchComments(postId)
            } catch (e: Exception) {
                Log.e("FeedViewModel", "deleteComment 실패", e)
            }
        }
    }

    // 내부용: 자식 대댓글까지 전부 삭제
    private suspend fun deleteCommentRecursive(postId: Long, commentId: String) {
        val commentsRef = db.collection("feeds")
            .document(postId.toString())
            .collection("comments")

        // 1) 나 자신 삭제
        commentsRef.document(commentId).delete().await()

        // 2) 나를 부모로 가지는 자식들 찾기
        val children = commentsRef
            .whereEqualTo("parentCommentId", commentId)
            .get()
            .await()

        for (child in children.documents) {
            deleteCommentRecursive(postId, child.id)
        }
    }
}

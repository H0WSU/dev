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
import com.example.howsu.data.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.filter
import kotlin.collections.sortedByDescending

class FeedViewModel : ViewModel() {

    // Firebase 인스턴스
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // 현재 로그인한 User (계정 정보)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // 현재 로그인중인 FamilyMember (프로필, 닉네임)
    private val _currentMember = MutableStateFlow<FamilyMember?>(null)
    val currentMember: StateFlow<FamilyMember?> = _currentMember.asStateFlow()

    var searchQuery by mutableStateOf("")
        private set

    fun changeFilter(filter: FeedFilter) {
        selectedFilter = filter
    }

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

    private var feedListener: ListenerRegistration? = null

    /* -------------------------------------------------------------
       1) 내 프로필(FamilyMember) 불러오기
       ------------------------------------------------------------- */
    fun fetchMyProfile() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // 1) users 컬렉션에서 familyId 먼저 가져오기
                val userDoc = db.collection("users").document(uid).get().await()

                val user = User(
                    uid = uid,
                    email = userDoc.getString("email"),
                    name = userDoc.getString("name") ?: "",           // ← 여기!
                    currentFamilyId = userDoc.getString("currentFamilyId"),
                    createdAt = userDoc.getLong("createdAt") ?: System.currentTimeMillis()
                )
                _currentUser.value = user

                // familyId 는 스키마에 맞게 가져오기
                val familyId =
                    user.currentFamilyId                // User에 들어있다면 이걸 쓰고
                        ?: userDoc.getString("familyId") // 아니면 기존 필드 쓰고
                        ?: ""

                if (familyId.isEmpty()) {
                    _currentMember.value = null
                    return@launch
                }

                // 2) families/{familyId}/members/{uid} 에서 가족 정보 가져오기
                val memberDoc = db.collection("families")
                    .document(familyId)
                    .collection("members")
                    .document(uid)
                    .get()
                    .await()

                val nicknameInFamily = memberDoc.getString("nickName") ?: "알 수 없음"

                val profileUrl = memberDoc.getString("profileImageUrl")

                val me = FamilyMember(
                    userId = uid,
                    familyId = familyId,
                    nickName = nicknameInFamily,
                    relationship = memberDoc.getString("relationship") ?: "",
                    profileImageUrl = profileUrl
                )

                _currentMember.value = me


            } catch (e: Exception) {
                Log.e("FeedViewModel", "fetchMyProfile 실패", e)
            }
        }
    }


    /* -------------------------------------------------------------
       2) '내 가족' 피드 목록 불러오기 (Firestore → _posts)
       ------------------------------------------------------------- */
    fun loadPostsForMyFamilyRealtime() {
        val familyId = _currentMember.value?.familyId ?: return

        // 이미 리스너 있으면 제거 (중복 방지)
        feedListener?.remove()

        feedListener = db.collection("feeds")
            .whereEqualTo("familyId", familyId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (snapshot == null) return@addSnapshotListener

                val newList = snapshot.documents.mapNotNull { doc ->
                    val post = doc.toObject(FeedPost::class.java) ?: return@mapNotNull null
                    val isLiked = _likedPostIds.value.contains(post.id)
                    post.copy(isLiked = isLiked)
                }

                _posts.clear()
                _posts.addAll(newList)
            }

    }

    override fun onCleared() {
        super.onCleared()
        feedListener?.remove()
        feedListener = null
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

            //검색 기능 추가
            val raw = searchQuery.trim()
            if(raw.isBlank()) return base.sortedByDescending { it.createdAt }

            val q = raw.lowercase()

            val searched = if(q.startsWith("#")){
                val tag = q.removePrefix("#").trim()
                if(tag.isBlank()){
                    base
                }else{
                    base.filter { post ->
                        post.hashtags.any{it.lowercase().contains(tag)}
                    }
                }
            }else{
                base.filter { post ->
                    post.title.lowercase().contains(q) || post.content.lowercase().contains(q)
                }
            }

            return base.sortedByDescending { it.createdAt }
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
        onComplete: () -> Unit = {} // ★ 이름 변경 & 기본값 추가
    ) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // 1) 프로필/유저 정보 로딩 대기
                val me = currentMember.value ?: run {
                    fetchMyProfile()
                    kotlinx.coroutines.delay(300)
                    currentMember.value ?: return@launch // 로딩 실패 시 중단
                }

                val user = currentUser.value ?: run {
                    fetchMyProfile()
                    kotlinx.coroutines.delay(300)
                    currentUser.value ?: return@launch
                }

                val authorName = when {
                    !user.name.isNullOrBlank() -> user.name!!
                    !me.nickName.isNullOrBlank() -> me.nickName
                    else -> "익명"
                }

                val newPost = FeedPost(
                    id = System.currentTimeMillis(),
                    authorId = uid,
                    authorName = authorName,
                    authorProfileImage = me.profileImageUrl ?: "",
                    title = title,
                    content = content,
                    imageUris = imageUris,
                    videoUris = videoUris,
                    hashtags = hashtags,
                    likeCount = 0,
                    commentCount = 0,
                    createdAt = System.currentTimeMillis(),
                    familyId = me.familyId
                )

                // 2) Firestore 저장
                db.collection("feeds")
                    .document(newPost.id.toString())
                    .set(newPost)
                    .await()

                // 3) 로컬 리스트 갱신
                _posts.add(0, newPost)

                // 4) ★ 작업 완료 콜백 호출 (화면 닫기)
                onComplete()

            } catch (e: Exception) {
                Log.e("FeedViewModel", "addPost 실패", e)
                // 에러 처리 필요 시 onError 콜백 추가 가능
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
        onComplete: () -> Unit = {} // ★ 이름 통일 (onFinish -> onComplete)
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

                // ★ 작업 완료 후 호출
                onComplete()

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
            val snap = db.collection("feeds")
                .document(postId.toString())
                .collection("likes")
                .document(uid)
                .get()
                .await()

            val liked = snap.exists()

            // 전역 likedPostIds 업데이트
            _likedPostIds.value =
                if (liked) _likedPostIds.value + postId
                else _likedPostIds.value - postId

            // 로컬 posts 업데이트
            val index = _posts.indexOfFirst { it.id == postId }
            if (index != -1) {
                val old = _posts[index]
                _posts[index] = old.copy(isLiked = liked)
            }
        }
    }


    fun toggleLike(postId: Long) {
        val uid = auth.currentUser?.uid ?: return

        val feedRef = db.collection("feeds").document(postId.toString())
        val likeRef = feedRef.collection("likes").document(uid)

        val index = _posts.indexOfFirst { it.id == postId }
        if (index == -1) return

        val old = _posts[index]
        val nowLiked = !old.isLiked
        val newCount = (old.likeCount + if (nowLiked) 1 else -1).coerceAtLeast(0)

        // 즉시 UI 반영 -----------------------------------
        _posts[index] = old.copy(
            isLiked = nowLiked,
            likeCount = newCount
        )

        // likedPostIds 즉시 반영
        _likedPostIds.value = if (nowLiked)
            _likedPostIds.value + postId
        else
            _likedPostIds.value - postId
        // ------------------------------------------------

        viewModelScope.launch {
            try {
                db.runTransaction { tx ->
                    val snap = tx.get(likeRef)
                    if (snap.exists()) {
                        tx.delete(likeRef)
                        tx.update(feedRef, "likeCount", FieldValue.increment(-1))
                    } else {
                        tx.set(likeRef, mapOf("isLike" to true))
                        tx.update(feedRef, "likeCount", FieldValue.increment(1))
                    }
                }.await()
            } catch (e: Exception) {
                // Firestore 실패 → 롤백
                _posts[index] = old
                _likedPostIds.value = if (old.isLiked)
                    _likedPostIds.value + postId
                else
                    _likedPostIds.value - postId
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
     * 댓글 삭제 (소프트 삭제)
     * 깊이 제한 없음
     */
    fun deleteComment(postId: Long, commentId: String) {
        viewModelScope.launch {
            try {
                softDeleteComment(postId, commentId)
                fetchComments(postId) // 화면/카운트 갱신
            } catch (e: Exception) {
                Log.e("FeedViewModel", "deleteComment(soft) 실패", e)
            }
        }
    }

    private suspend fun softDeleteComment(postId: Long, commentId: String) {
        val docRef = db.collection("feeds")   // ★ feed_posts -> feeds 로 수정
            .document(postId.toString())
            .collection("comments")
            .document(commentId)

        docRef.update(
            mapOf(
                "deleted" to true,
                "content" to ""
            )
        ).await()

    }
}

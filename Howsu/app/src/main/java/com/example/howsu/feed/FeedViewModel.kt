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
import com.google.firebase.firestore.FieldPath
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

    fun isPostLiked(postId: Long): Boolean {
        return _likedPostIds.value.contains(postId)
    }


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

    private suspend fun fetchLikedIdsForPosts(postIds: List<Long>): Set<Long> {
        val uid = auth.currentUser?.uid ?: return emptySet()

        val result = mutableSetOf<Long>()
        for (postId in postIds) {
            val snap = db.collection("feeds")
                .document(postId.toString())
                .collection("likes")
                .document(uid)
                .get()
                .await()

            if (snap.exists()) result += postId
        }
        return result
    }


    fun loadPostsForMyFamilyRealtime() {
        val familyId = _currentMember.value?.familyId ?: return

        feedListener?.remove()

        feedListener = db.collection("feeds")
            .whereEqualTo("familyId", familyId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FeedViewModel", "loadPostsForMyFamilyRealtime 실패", e)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val uid = auth.currentUser?.uid ?: return@addSnapshotListener

                viewModelScope.launch {
                    val docs = snapshot.documents

                    // 문서 id를 postId로 확정
                    val postIds = docs.mapNotNull { it.id.toLongOrNull() }

                    // 각 글에 대해 likes/{uid} 존재 여부 체크
                    val likedIds = mutableSetOf<Long>()
                    for (postId in postIds) {
                        val likeDoc = db.collection("feeds")
                            .document(postId.toString())
                            .collection("likes")
                            .document(uid)
                            .get()
                            .await()

                        if (likeDoc.exists()) likedIds += postId
                    }
                    _likedPostIds.value = likedIds

                    val newPosts = docs.mapNotNull { doc ->
                        val postId = doc.id.toLongOrNull() ?: return@mapNotNull null
                        val base = doc.toObject(FeedPost::class.java) ?: FeedPost(id = postId)

                        val likeAny = doc.get("likeCount")
                        val commentAny = doc.get("commentCount")

                        val like = when (likeAny) {
                            is Long -> likeAny
                            is Int -> likeAny.toLong()
                            is Double -> likeAny.toLong()
                            else -> 0L
                        }

                        val comment = when (commentAny) {
                            is Long -> commentAny
                            is Int -> commentAny.toLong()
                            is Double -> commentAny.toLong()
                            else -> 0L
                        }
                        Log.d("FeedVM", "doc=${doc.id}, rawLike=${doc.get("likeCount")}, getLong=${doc.getLong("likeCount")}")


                        base.copy(
                            id = postId, // 여기서 id를 문서 id로 강제 고정
                            likeCount = like,
                            commentCount = comment,
                            isLiked = likedIds.contains(postId)
                        )

                    }

                    _posts.clear()
                    _posts.addAll(newPosts)

                }
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
                    likeCount = 0L,
                    commentCount = 0L,
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
        val postRef = db.collection("feeds").document(postId.toString())
        val likeRef = postRef.collection("likes").document(uid)

        viewModelScope.launch {
            try {
                val (newLiked, newCount) = db.runTransaction { tx ->
                    val likeSnap = tx.get(likeRef)
                    val postSnap = tx.get(postRef)

                    val currentCount = postSnap.getLong("likeCount") ?: 0L
                    val alreadyLiked = likeSnap.exists()

                    val updatedLiked = !alreadyLiked
                    val updatedCount = if (alreadyLiked) {
                        (currentCount - 1L).coerceAtLeast(0L)
                    } else {
                        currentCount + 1L
                    }

                    if (alreadyLiked) {
                        tx.delete(likeRef)
                    } else {
                        tx.set(
                            likeRef,
                            mapOf(
                                "userId" to uid,
                                "createdAt" to System.currentTimeMillis()
                            )
                        )

                    }

                    tx.update(postRef, "likeCount", updatedCount)

                    Pair(updatedLiked, updatedCount)
                }.await()

                _likedPostIds.value = if (newLiked) {
                    _likedPostIds.value + postId
                } else {
                    _likedPostIds.value - postId
                }

                val index = _posts.indexOfFirst { it.id == postId }
                if (index != -1) {
                    val old = _posts[index]
                    _posts[index] = old.copy(
                        isLiked = newLiked,
                        likeCount = newCount
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("FeedViewModel", "toggleLike 실패", e)
            }
        }
    }

    fun loadLikeStateForAllPosts() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val liked = mutableSetOf<Long>()

                val snapshot = db.collectionGroup("likes")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()

                snapshot.documents.forEach { doc ->
                    // feeds/{postId}/likes/{uid}
                    val postId = doc.reference.parent.parent?.id?.toLongOrNull()
                    if (postId != null) liked += postId
                }

                _likedPostIds.value = liked

                _posts.forEachIndexed { index, post ->
                    _posts[index] = post.copy(isLiked = liked.contains(post.id))
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "loadLikeStateForAllPosts 실패", e)
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
                val count = list.size.toLong()   // ← 여기만 변경

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

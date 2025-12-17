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
import com.google.firebase.firestore.ListenerRegistration
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

    private var profileListener: ListenerRegistration? = null
    private var memberListener: ListenerRegistration? = null // 멤버 정보 실시간 리스너 추가
    private var feedListener: ListenerRegistration? = null

    private var userListener: ListenerRegistration? = null
    private var isSyncStarted = false
    private var lastSyncedUid: String? = null

    fun isPostLiked(postId: Long): Boolean {
        // 1순위: 리스트(_posts)에 해당 포스트가 있다면 그 안의 isLiked 상태를 직접 확인 (리스너와 동기화됨)
        val postInList = _posts.find { it.id == postId }
        if (postInList != null) return postInList.isLiked

        // 2순위: 리스트에 없다면(직접 진입 등) 세트값 확인
        return _likedPostIds.value.contains(postId)
    }

    /**
     * [핵심 수정] 좋아요 ID 목록이 변경될 때마다 게시글 리스트의 하트 상태를 동기화
     */
    private fun observeLikedIdsSync() {
        viewModelScope.launch {
            // _likedPostIds (StateFlow)를 수집하여 값이 바뀔 때마다 실행
            _likedPostIds.collect { likedIds ->
                _posts.forEachIndexed { index, post ->
                    val shouldBeLiked = likedIds.contains(post.id)
                    // 현재 상태와 다를 때만 객체를 교체하여 UI 갱신 유도
                    if (post.isLiked != shouldBeLiked) {
                        _posts[index] = post.copy(isLiked = shouldBeLiked)
                    }
                }
            }
        }
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

    // 1. _likedPostIds를 관찰하여 변경될 때마다 게시글의 isLiked 상태를 갱신
    private fun observeLikedIds() {
        viewModelScope.launch {
            // _likedPostIds가 빈 세트였다가 서버에서 데이터를 받아오는 순간!
            _likedPostIds.collect { likedIds ->
                // 현재 화면에 보여주고 있는 posts 리스트를 루프 돌면서
                _posts.forEachIndexed { index, post ->
                    val newIsLiked = likedIds.contains(post.id)
                    // 하트 상태가 서버 데이터와 다르면 강제로 업데이트 (여기서 하트가 노란색으로 바뀜)
                    if (post.isLiked != newIsLiked) {
                        _posts[index] = post.copy(isLiked = newIsLiked)
                    }
                }
            }
        }
    }

    /**
     * [기능 추가] 현재 로그인한 사용자가 작성자인지 확인하는 함수
     */
    fun isAuthor(authorId: String): Boolean {
        return auth.currentUser?.uid == authorId
    }


    /* -------------------------------------------------------------
         1) 실시간 프로필 + 피드 통합 동기화 (닉네임/가족변경 완벽 대응)
         ------------------------------------------------------------- */
    fun startProfileAndFeedSync() {
        val uid = auth.currentUser?.uid ?: return
        if (lastSyncedUid != null && lastSyncedUid != uid) { clearAllState() }
        if (isSyncStarted && lastSyncedUid == uid) return

        isSyncStarted = true
        lastSyncedUid = uid

        // ★ 좋아요 상태 관찰 시작
        observeLikedIdsSync()

        profileListener?.remove()
        profileListener = db.collection("users").document(uid)
            .addSnapshotListener { userSnap, e ->
                if (e != null || userSnap == null) return@addSnapshotListener

                val familyId = userSnap.getString("currentFamilyId") ?: ""

                val me = FamilyMember(
                    userId = uid,
                    familyId = familyId,
                    nickName = userSnap.getString("name") ?: "알 수 없음",
                    profileImageUrl = userSnap.getString("profileImageUrl"),
                    relationship = ""
                )
                _currentMember.value = me

                if (familyId.isNotEmpty()) {
                    loadPostsForMyFamilyRealtime(familyId)
                }
            }
    }

    //  상태 초기화 함수 추가
    private fun clearAllState() {
        isSyncStarted = false
        lastSyncedUid = null
        currentLoadingFamilyId = null

        // 리스너 확실히 제거
        profileListener?.remove()
        feedListener?.remove()
        memberListener?.remove()
        profileListener = null
        feedListener = null
        memberListener = null

        // ★ 모든 상태값 초기화 (이게 빠지면 이전 계정 하트가 보임)
        _posts.clear()
        _likedPostIds.value = emptySet()
        _likedCommentIds.value = emptySet()
        _currentUser.value = null
        _currentMember.value = null
        _comments.value = emptyList()
    }

    private var currentLoadingFamilyId: String? = null // 현재 로딩 중인 가족 ID 추적

    private var detailPostListener: ListenerRegistration? = null

    /**
     * 상세 화면 진입 시 해당 게시글 하나만 실시간으로 감시
     * 전체 리스트 로딩과 별개로 서버에서 원본 데이터를 즉시 가져와 '0' 표시 현상 방지
     */
    fun observePostDetail(postId: Long) {
        detailPostListener?.remove() // 이전 리스너 제거

        detailPostListener = db.collection("feeds")
            .document(postId.toString())
            .addSnapshotListener { doc, e ->
                if (e != null || doc == null || !doc.exists()) return@addSnapshotListener

                // Firestore 문서에서 원본 숫자 데이터를 직접 추출 (Long 매핑 오류 방지)
                val serverLikeCount = (doc.get("likeCount") as? Number)?.toLong() ?: 0L
                val serverCommentCount = (doc.get("commentCount") as? Number)?.toLong() ?: 0L

                // 로컬 리스트(_posts) 내의 해당 게시글 정보를 즉시 갱신
                val index = _posts.indexOfFirst { it.id == postId }
                if (index != -1) {
                    _posts[index] = _posts[index].copy(
                        likeCount = serverLikeCount,
                        commentCount = serverCommentCount
                    )
                }
            }
    }

    /** 상세 화면을 나갈 때 리스너 해제 */
    fun stopObservingPostDetail() {
        detailPostListener?.remove()
        detailPostListener = null
    }



    private fun loadPostsForMyFamilyRealtime(familyId: String) {
        val uid = auth.currentUser?.uid ?: return
        feedListener?.remove()

        feedListener = db.collection("feeds")
            .whereEqualTo("familyId", familyId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                viewModelScope.launch {
                    // 좋아요 목록을 '먼저' 가져온 후 게시글을 처리하도록 순서 보장
                    val likedIds = fetchLikedIdsForUser(uid)
                    _likedPostIds.value = likedIds

                    val newPosts = snapshot.documents.mapNotNull { doc ->
                        val postId = doc.id.toLongOrNull() ?: return@mapNotNull null
                        val base = doc.toObject(FeedPost::class.java) ?: FeedPost(id = postId)

                        val serverLikeCount = (doc.get("likeCount") as? Number)?.toLong() ?: 0L
                        val serverCommentCount = (doc.get("commentCount") as? Number)?.toLong() ?: 0L

                        base.copy(
                            id = postId,
                            likeCount = serverLikeCount,
                            commentCount = serverCommentCount,
                            isLiked = likedIds.contains(postId) // 최신화된 likedIds 사용
                        )
                    }
                    _posts.clear()
                    _posts.addAll(newPosts)
                }
            }
    }

    // 좋아요 목록을 한 번에 가져오는 헬퍼 함수
    private suspend fun fetchLikedIdsForUser(uid: String): Set<Long> {
        return try {
            // collectionGroup을 사용하면 모든 피드의 'likes' 서브컬렉션 중 내 UID인 것만 한 번에 긁어올 수 있습니다.
            val query = db.collectionGroup("likes")
                .whereEqualTo("userId", uid)
                .get()
                .await()

            query.documents.mapNotNull { it.reference.parent.parent?.id?.toLongOrNull() }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }


    override fun onCleared() {
        super.onCleared()
        profileListener?.remove()
        memberListener?.remove() // 리스너 해제 잊지 말기
        feedListener?.remove()
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

                val authorName = if (!me.nickName.isNullOrBlank()) {
                    me.nickName  // 가족에서 설정한 닉네임을 1순위로
                } else if (!user.name.isNullOrBlank()) {
                    user.name!!
                } else {
                    "익명"
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

        viewModelScope.launch {
            val postRef = db.collection("feeds").document(postId.toString())
            val likeRef = postRef.collection("likes").document(uid)

            val (finalLiked, finalCount) = db.runTransaction { tx ->
                val likeSnap = tx.get(likeRef)
                val postSnap = tx.get(postRef)

                val currentCount =
                    (postSnap.get("likeCount") as? Number)?.toLong() ?: 0L
                val alreadyLiked = likeSnap.exists()

                val newLiked = !alreadyLiked
                val newCount = if (alreadyLiked) {
                    (currentCount - 1).coerceAtLeast(0)
                } else {
                    currentCount + 1
                }

                if (alreadyLiked) {
                    tx.delete(likeRef)
                } else {
                    tx.set(likeRef, mapOf("userId" to uid))
                }

                tx.update(postRef, "likeCount", newCount)

                Pair(newLiked, newCount)
            }.await()

            // 서버에서 확정된 값으로 UI 덮어쓰기
            val index = _posts.indexOfFirst { it.id == postId }
            if (index != -1) {
                _posts[index] = _posts[index].copy(
                    isLiked = finalLiked,
                    likeCount = finalCount
                )
            }

            _likedPostIds.value =
                if (finalLiked) _likedPostIds.value + postId
                else _likedPostIds.value - postId
        }
    }


    // 닉네임 결정 로직 (addPost 등에서 사용 시)
    private fun getAuthorName(user: User?, me: FamilyMember?): String {
        return when {
            // ★ 1순위: 현재 선택된 가족 내의 닉네임 (가장 중요)
            me != null && !me.nickName.isNullOrBlank() -> me.nickName
            // 2순위: 유저 기본 이름
            user != null && !user.name.isNullOrBlank() -> user.name!!
            else -> "익명"
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
        val uid = auth.currentUser?.uid ?: return // 내 UID 확인

        viewModelScope.launch {
            try {
                val snap = db.collection("feeds")
                    .document(postId.toString())
                    .collection("comments")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val list = snap.documents.mapNotNull { doc ->
                    val base = doc.toObject(Comment::class.java) ?: return@mapNotNull null

                    // ★ 여기 추가: 내 댓글이면 실시간 닉네임과 프사로 덮어씌움
                    val isMe = base.userId == uid
                    val displayUserName = if (isMe) _currentMember.value?.nickName ?: base.userName else base.userName
                    val displayProfileImage = if (isMe) _currentMember.value?.profileImageUrl ?: base.userProfileImage else base.userProfileImage

                    base.copy(
                        id = doc.id,
                        postId = postId,
                        userName = displayUserName,       // 덮어쓰기
                        userProfileImage = displayProfileImage // 프사도 덮어쓰기
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

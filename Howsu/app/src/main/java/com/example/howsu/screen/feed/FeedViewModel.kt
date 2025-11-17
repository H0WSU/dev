package com.example.howsu.screen.feed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.howsu.data.model.FeedFilter
import com.example.howsu.data.model.FeedPost

class FeedViewModel : ViewModel() {

    //게시물 리스트
    private val _posts = mutableStateListOf<FeedPost>()
    val posts: List<FeedPost> get() = _posts

    //현재 선택된 탭
    var selectedFilter by mutableStateOf(FeedFilter.ALL)
        private set

    //탭에 따라 화면에 보여줄 리스트
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
            //등록순(최근 등록이 위로 오게) 정렬
            return base.sortedByDescending { it.createdAt }
        }

    fun changeFilter(filter : FeedFilter){
        selectedFilter = filter
    }

    //글 + 사진/동영상 첨부해서 등록
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

    //수정
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

    //삭제
    fun deletePost(id: Long) {
        _posts.removeAll { it.id == id }

    }

}
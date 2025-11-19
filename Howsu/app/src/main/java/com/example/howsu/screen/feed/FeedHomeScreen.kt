package com.example.howsu.screen.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.FeedFilter
import com.example.howsu.feed.FeedHomeTopBar


@Composable
fun FeedHomeScreen(
    viewModel: FeedViewModel,
    navController: NavHostController,
    member : FamilyMember

){
    val filteredPosts = viewModel.filteredPosts
    val selectedFilter = viewModel.selectedFilter

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(10.dp))
            // 1) TopBar — 상태바 넘치지 않도록 패딩
            FeedHomeTopBar(member,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()       // ★ 상단 잘림 방지 핵심
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 2) TabRow — TopBar 바로 아래 적당한 간격
            FilterTabRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { filter ->
                    viewModel.changeFilter(filter)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3) 피드 목록 — 스크롤 가능
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 4.dp),
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    FeedItem(
                        post = post,
                        onClick = {

                            navController.navigate("edit_feed/${post.id}")
                        },
                        onDeleteClick = {
                            viewModel.deletePost(post.id)
                        }
                    )
                }
            }

        }
        // 4) FAB — 오른쪽 아래 고정
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            MyFloatingActionButton(
                onTodoClick = { navController.navigate("create_todo") },
                onScheduleClick = { navController.navigate("create_schedule") },
                onFeedCreateClick = { navController.navigate("create_feed") }
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            MyBottomNavigationBar(navController = navController)
        }

    }
}

@Composable
fun FilterTabRow(
    selectedFilter: FeedFilter,
    onFilterSelected: (FeedFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterTab("전체", FeedFilter.ALL, selectedFilter, onFilterSelected)
        FilterTab("글", FeedFilter.TEXT, selectedFilter, onFilterSelected)
        FilterTab("사진", FeedFilter.IMAGE, selectedFilter, onFilterSelected)
        FilterTab("동영상", FeedFilter.VIDEO, selectedFilter, onFilterSelected)
    }
}


@Composable
private fun FilterTab(
    text: String,
    filter: FeedFilter,
    selectedFilter: FeedFilter,
    onFilterSelected: (FeedFilter) -> Unit
){
    val isSelected = filter == selectedFilter

    Text(
        text = text,
        modifier = Modifier
            .clickable{onFilterSelected(filter)}
            .padding(vertical = 8.dp, horizontal = 16.dp),
        color = if(isSelected) Color.Black else Color.Black,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
}


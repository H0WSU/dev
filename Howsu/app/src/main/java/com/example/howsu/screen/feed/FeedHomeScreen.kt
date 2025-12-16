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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.FeedHomeTopBar
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.FeedFilter
import com.example.howsu.ui.theme.HowsuTheme

@Composable
fun FeedHomeScreen(
    viewModel: FeedViewModel,
    navController: NavHostController
) {
    val filteredPosts = viewModel.filteredPosts
    val selectedFilter = viewModel.selectedFilter

    LaunchedEffect(Unit) {
        viewModel.fetchMyProfile()
    }

    val myInfo by viewModel.currentMember.collectAsState()

    LaunchedEffect(myInfo) {
        myInfo?.let {
            viewModel.loadPostsForMyFamilyRealtime()
        }
    }


    val displayMember = myInfo ?: FamilyMember(
        userId = "",
        familyId = "",
        nickName = "",
        relationship = "",
        profileImageUrl = null
    )

    Scaffold(
        topBar = {
            FeedHomeTopBar(
                member = displayMember,
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 40.dp,
                    bottom = 10.dp
                )
            )
        },
        bottomBar = {
            MyBottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = { navController.navigate("create_todo") },
                onScheduleClick = { navController.navigate("create_schedule") },
                onFeedCreateClick = { navController.navigate("create_feed") }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Spacer(modifier = Modifier.height(4.dp))

                FilterTabRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { filter ->
                        viewModel.changeFilter(filter)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 4.dp),
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        FeedItem(
                            post = post,
                            isLiked = post.isLiked,
                            onClick = { navController.navigate("feed_detail/${post.id}") },
                            onDeleteClick = { viewModel.deletePost(post.id) },
                            onToggleLike = { viewModel.toggleLike(post.id) }   // ★ 추가
                        )
                    }
                }
            }
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
) {
    val isSelected = filter == selectedFilter

    Text(
        text = text,
        modifier = Modifier
            .clickable { onFilterSelected(filter) }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        color = Color.Black,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun FeedHomeScreenPreview() {
    val navController = rememberNavController()
    val viewModel = remember { FeedViewModel() }

    // 프리뷰에서는 currentMember/Firestore 로딩이 안 돌 수 있으니,
    // TopBar가 빈 닉네임으로 보이는 건 정상입니다.

    HowsuTheme {
        FeedHomeScreen(
            viewModel = viewModel,
            navController = navController
        )
    }
}

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.data.model.FamilyMember
import com.example.howsu.data.model.FeedFilter
import com.example.howsu.common.FeedHomeTopBar
import com.example.howsu.data.model.FeedPost
import com.example.howsu.ui.theme.HowsuTheme

@Composable
fun FeedHomeScreen(
    viewModel: FeedViewModel,
    navController: NavHostController
) {
    val filteredPosts = viewModel.filteredPosts
    val selectedFilter = viewModel.selectedFilter

    // 프로필 불러오기
    LaunchedEffect(Unit) {
        viewModel.fetchMyProfile()
    }

// myInfo 변화를 보고, familyId가 준비되면 피드 로딩
    val myInfo by viewModel.currentMember.collectAsState()

    LaunchedEffect(myInfo) {
        myInfo?.let {
            viewModel.loadPostsForMyFamily()
        }
    }


    // 로딩 중일 때 사용할 임시 데이터
    val displayMember = myInfo ?: FamilyMember(
        userId = "",
        familyId = "",
        nickName = "",
        relationship = "",
        profileImageUrl = null
    )

    Scaffold(
        // 상단 탑바
        topBar = {
            FeedHomeTopBar(
                member = displayMember,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        },
        // 하단 네비게이션바
        bottomBar = {
            MyBottomNavigationBar(navController = navController)
        },
        // 오른쪽 아래 FAB
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
                .padding(paddingValues)  // Scaffold에서 준 패딩 적용
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 탑바는 Scaffold가 알아서 그려주므로 여기서는 필요 X

                Spacer(modifier = Modifier.height(4.dp))

                // 2) TabRow
                FilterTabRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { filter ->
                        viewModel.changeFilter(filter)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3) 피드 목록
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
                                navController.navigate("feed_detail/${post.id}")
                            },
                            onDeleteClick = {
                                viewModel.deletePost(post.id)
                            }
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun FeedHomeScreenPreview() {
    val dummyMember = FamilyMember(
        userId = "u1",
        familyId = "f1",
        nickName = "이구역의짱",
        relationship = "집사",
        profileImageUrl = null
    )

    val dummyPosts = listOf(
        FeedPost(
            id = 1L,
            authorId = "u1",
            authorName = "이구역의짱",
            title = "자몽이 오늘 산책 다녀옴",
            content = "날씨가 좋아서 그런지 신나게 뛰어다녔어요!",
            hashtags = listOf("산책", "일상"),
            likeCount = 3,
            commentCount = 2
        ),
        FeedPost(
            id = 2L,
            authorId = "u2",
            authorName = "자몽아기야",
            title = "사료 바꿔야 할까?",
            content = "요즘 사료를 남기는 것 같아서 고민 중...",
            hashtags = listOf("사료", "고민"),
            likeCount = 1,
            commentCount = 0
        )
    )

    val navController = rememberNavController()

    HowsuTheme {
        Scaffold(
            topBar = {
                FeedHomeTopBar(
                    member = dummyMember,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            },
            bottomBar = {
                MyBottomNavigationBar(navController = navController)
            },
            floatingActionButton = {
                MyFloatingActionButton(
                    onTodoClick = { },
                    onScheduleClick = { },
                    onFeedCreateClick = { }
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // 탭 (전체/글/사진/동영상) – 프리뷰에선 선택 고정
                    FilterTabRow(
                        selectedFilter = FeedFilter.ALL,
                        onFilterSelected = { /* no-op */ }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 4.dp)
                    ) {
                        items(dummyPosts, key = { it.id }) { post ->
                            FeedItem(
                                post = post,
                                onClick = { /* no-op */ },
                                onDeleteClick = { /* no-op */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
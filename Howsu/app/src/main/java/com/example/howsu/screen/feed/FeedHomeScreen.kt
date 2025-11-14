package com.example.howsu.screen.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import com.example.howsu.feed.FeedCategoryTabs
import com.example.howsu.feed.FeedHomeTopBar

@Composable
fun FeedHomeScreen(
    navController : NavHostController,
    onClickCreate : () -> Unit = {}
){
    Scaffold (
        topBar = { FeedHomeTopBar() },
        bottomBar = { MyBottomNavigationBar(navController = navController) },
        floatingActionButton = {
            MyFloatingActionButton(onClick = onClickCreate)
        }
    ){
        innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ){
            FeedCategoryTabs()
            // FeedLists() 구현 예정
        }
    }
}
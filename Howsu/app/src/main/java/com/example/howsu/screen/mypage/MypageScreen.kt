package com.example.howsu.screen.mypage

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.howsu.common.MyBottomNavigationBar
import com.example.howsu.common.MyFloatingActionButton
import androidx.compose.material3.Text

@Composable
fun MypageScreen(
    navController: NavHostController,
){
    Scaffold(
        bottomBar = {MyBottomNavigationBar(navController = navController)},
        floatingActionButton = {
            MyFloatingActionButton(
                onTodoClick = {
                    navController.navigate("create_todo")
                },
                onScheduleClick = {
                    navController.navigate("create_schedule")
                },
                onFeedCreateClick = {
                    navController.navigate("create_feed")
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // mypage 내용 구성
            item{Text("마이페이지")}
        }

    }
}


// ----------------------------------------------------
// Preview 함수
// ----------------------------------------------------

@Preview(showBackground = true)
@Composable
fun MypageScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        // Preview에서는 ViewModel을 직접 생성자로 전달하지 않고 기본 함수를 사용하거나
        // Mock ViewModel을 사용하는 것이 일반적입니다. 여기서는 기본 설정으로 둡니다.
        MypageScreen(
            navController = navController,
        )
    }
}
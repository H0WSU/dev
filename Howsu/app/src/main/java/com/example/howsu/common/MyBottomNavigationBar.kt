package com.example.howsu.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.howsu.R// 2. 본인 R 파일 import

// (이전에 만든 BottomNavItem 데이터 클래스 사용)
data class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
)

@Composable
fun MyBottomNavigationBar(navController: NavHostController) { // 3. navController를 받음

    // 4. 아이템 리스트 정의 (route는 NavHost에 등록할 경로와 일치해야 함)
    val items = listOf(
        BottomNavItem("home", "Home", R.drawable.home_under),
        BottomNavItem("calendar", "Calendar", R.drawable.date_under),
        BottomNavItem("todo", "Todo", R.drawable.todo_under),
        BottomNavItem("feed", "Feed", R.drawable.feed_under),
        BottomNavItem("profile", "Profile", R.drawable.user_under)
    )

    // 5. 현재 경로를 실시간으로 추적
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.clip(
            RoundedCornerShape(
                topStart = 30.dp, // 왼쪽 위 모서리
                topEnd = 30.dp     // 오른쪽 위 모서리
                // (bottomStart, bottomEnd는 0.dp가 기본값)
            )
        )
    ) {
        items.forEach { item ->
            NavigationBarItem(
                // 6. 'selectedItem' 대신 'currentRoute'로 선택 상태 결정
                selected = (currentRoute == item.route),

                // 7. 'selectedItem = index' 대신 'navController.navigate'로 화면 이동
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(30.dp) // 아이콘 크기 고정
                    )
                }
            )
        }
    }
}
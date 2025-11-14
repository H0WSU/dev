package com.example.howsu.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.howsu.R

data class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
)

@Composable
fun MyBottomNavigationBar(navController: NavHostController) {

    val items = listOf(
        BottomNavItem("home", "Home", R.drawable.home_under),
        BottomNavItem("schedule", "Calendar", R.drawable.date_under),
        BottomNavItem("todo", "Todo", R.drawable.todo_under),
        BottomNavItem("feed", "Feed", R.drawable.feed_under),
        BottomNavItem("profile", "Profile", R.drawable.user_under)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            // 시스템 내비게이션 영역(ㅡ)만큼 자동 여백 추가
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(60.dp),
        containerColor = Color(0xFFD3D3D3)
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = (currentRoute == item.route),
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
                        modifier = Modifier.size(30.dp)
                    )
                }
            )
        }
    }
}

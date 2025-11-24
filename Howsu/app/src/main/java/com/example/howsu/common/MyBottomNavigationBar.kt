package com.example.howsu.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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

    val borderColor = Color(0xFFFFDF37)
    // 상단 모서리 둥글게
    val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)

    NavigationBar(
        modifier = Modifier
            .clip(shape) // 모양 먼저 자르고
            .border(1.dp, borderColor, shape) // ★ 테두리 추가 (1.5dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(60.dp),
        containerColor = Color.White // ★ 배경 흰색
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    indicatorColor = Color.Transparent
                ),
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(28.dp),
                        tint = Color.Unspecified
                    )
                }
            )
        }
    }
}
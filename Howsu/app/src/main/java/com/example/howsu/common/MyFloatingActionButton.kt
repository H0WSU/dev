package com.example.howsu.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 클릭 시 'TODO'와 '일정' 버튼으로 확장되는 스피드 다이얼 FAB
 * @param onTodoClick 'TODO' 버튼 클릭 시 실행될 람다
 * @param onScheduleClick '일정' 버튼 클릭 시 실행될 람다
 */
@Composable
fun MyFloatingActionButton(
    onTodoClick: () -> Unit,
    onScheduleClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        onTodoClick()
                        isMenuExpanded = false
                    },
                    containerColor = Color(0xFF6E6E6E).copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("TODO", color = Color.White, fontWeight = FontWeight.Bold)
                }

                FloatingActionButton(
                    onClick = {
                        onScheduleClick()
                        isMenuExpanded = false
                    },
                    containerColor = Color(0xFF6E6E6E).copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("일정", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        FloatingActionButton(
            onClick = { isMenuExpanded = !isMenuExpanded },
            containerColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                tint = Color.White,
                contentDescription = "메뉴 열기"
            )
        }
    }
}

package com.example.howsu.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
    onScheduleClick: () -> Unit,
    onFeedCreateClick: () -> Unit
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
                    containerColor = Color(0xFFFFDF37).copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("TODO", color = Color(color = 0xFF121212), fontWeight = FontWeight.Bold)
                }

                FloatingActionButton(
                    onClick = {
                        onScheduleClick()
                        isMenuExpanded = false
                    },
                    containerColor = Color(0xFFFFDF37).copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("일정", color = Color(color = 0xFF121212), fontWeight = FontWeight.Bold)
                }

                // '피드 추가' 버튼
                FloatingActionButton(
                    onClick = {
                        onFeedCreateClick() // 파라미터로 받은 onScheduleClick 실행
                        isMenuExpanded = false // 메뉴 닫기
                    },
                    containerColor = Color(0xFFFFDF37),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Text(
                        text = "피드",
                        color = Color(color = 0xFF121212),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { isMenuExpanded = !isMenuExpanded },
            containerColor = Color(0xFFFFDF37), // 노란색 배경
            shape = CircleShape,
        ) {
            // ★ 2. + 아이콘 직접 그리기 (Canvas)
            Canvas(modifier = Modifier.size(24.dp)) {
                // 선 두께랑 색상 설정
                val strokeWidth = 1.5.dp.toPx() // 1.5dp 두께
                val iconColor = Color(0xFF121212) // 검정색

                // 가로 선 그리기
                drawLine(
                    color = iconColor,
                    start = Offset(size.width * 0.2f, size.height / 2), // 왼쪽
                    end = Offset(size.width * 0.8f, size.height / 2),   // 오른쪽
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square
                )

                // 세로 선 그리기
                drawLine(
                    color = iconColor,
                    start = Offset(size.width / 2, size.height * 0.2f), // 위쪽
                    end = Offset(size.width / 2, size.height * 0.8f),   // 아래쪽
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square
                )
            }
        }
    }
}

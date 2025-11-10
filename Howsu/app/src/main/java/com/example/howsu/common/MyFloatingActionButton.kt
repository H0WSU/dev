package com.example.howsu.common

import androidx.compose.animation.AnimatedVisibility
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
    // 1. 파라미터가 변경되었습니다.
    // 'TODO'와 '일정'을 눌렀을 때 실행할 동작을 외부(ScheduleScreen)에서 받아옴
    onTodoClick: () -> Unit,
    onScheduleClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp) // 버튼 사이 간격
    ) {

        // isMenuExpanded가 true일 때만 이 부분을 보여줌
        AnimatedVisibility(visible = isMenuExpanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 'TODO' 버튼
                FloatingActionButton(
                    onClick = {
                        onTodoClick() // 파라미터로 받은 onTodoClick 실행
                        isMenuExpanded = false // 메뉴 닫기
                    },
                    containerColor = Color(0xFF6E6E6E), // 이미지와 비슷한 회색
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Text(text = "TODO", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // '일정' 버튼
                FloatingActionButton(
                    onClick = {
                        onScheduleClick() // 파라미터로 받은 onScheduleClick 실행
                        isMenuExpanded = false // 메뉴 닫기
                    },
                    containerColor = Color(0xFF6E6E6E),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Text(text = "일정", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 7. 메인 '+' 버튼
        FloatingActionButton(
            onClick = {
                // 이 버튼의 역할은 이제 '메뉴 토글(열기/닫기)'
                isMenuExpanded = !isMenuExpanded
            },
            containerColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                tint = Color.White,
                contentDescription = "새 할 일 추가"
            )
        }
    }
}
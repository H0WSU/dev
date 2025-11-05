package com.example.howsu.common

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

@Composable
fun MyFloatingActionButton(
    onClick: () -> Unit // 2. 클릭 이벤트를 파라미터로 받음
) {
    FloatingActionButton(
        onClick = onClick, // 3. 받은 onClick 이벤트를 그대로 연결
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "새 할 일 추가"
        )
    }
}
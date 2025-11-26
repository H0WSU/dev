package com.example.howsu.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.howsu.R

// 색상 상수
private val YellowCustom = Color(0xFFFFDF37)
private val ContentBlack = Color(0xFF121212)

@Composable
fun MyFloatingActionButton(
    onTodoClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onFeedCreateClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    // + 버튼 회전 애니메이션
    val rotation by animateFloatAsState(targetValue = if (isMenuExpanded) 45f else 0f)

    // 1. 메뉴가 열리면 배경 흐리게 (Dim) 처리하는 팝업 레이어
    AnimatedVisibility(
        visible = isMenuExpanded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Popup(
            alignment = Alignment.BottomEnd,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // ★ 배경 흐림 색상 (검정색 30% 투명도)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { isMenuExpanded = false } // 배경 터치 시 닫기
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        // 팝업이 아닐 때도 메인 FAB가 맨 위에 오도록
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        // 2. 열렸을 때 나타나는 메뉴 버튼들
        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
            exit = fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 }
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // (1) 투두 (고양이)
                FloatingActionMenuItem(
                    text = "투두",
                    iconId = R.drawable.cat,
                    onClick = {
                        onTodoClick()
                        isMenuExpanded = false
                    }
                )
                // (2) 일정 (뼈다귀)
                FloatingActionMenuItem(
                    text = "일정",
                    iconId = R.drawable.bone,
                    onClick = {
                        onScheduleClick()
                        isMenuExpanded = false
                    }
                )
                // (3) 피드 (강아지 발바닥)
                FloatingActionMenuItem(
                    text = "피드",
                    iconId = R.drawable.dog,
                    onClick = {
                        onFeedCreateClick()
                        isMenuExpanded = false
                    }
                )
            }
        }

        // 3. 메인 + 버튼 (노란색)
        FloatingActionButton(
            onClick = { isMenuExpanded = !isMenuExpanded },
            containerColor = YellowCustom,
            shape = CircleShape,
            // 메뉴가 열리면 메인 버튼이 그림자 없이 배경 위로 뜨도록
            elevation = if (isMenuExpanded) FloatingActionButtonDefaults.elevation(0.dp) else FloatingActionButtonDefaults.elevation(),
            modifier = Modifier.rotate(rotation)
        ) {
            // + 아이콘 그리기
            Canvas(modifier = Modifier.size(24.dp)) {
                val strokeWidth = 1.5.dp.toPx()
                val iconColor = ContentBlack
                drawLine(
                    color = iconColor,
                    start = Offset(size.width * 0.2f, size.height / 2),
                    end = Offset(size.width * 0.8f, size.height / 2),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square
                )
                drawLine(
                    color = iconColor,
                    start = Offset(size.width / 2, size.height * 0.2f),
                    end = Offset(size.width / 2, size.height * 0.8f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Square
                )
            }
        }
    }
}

// 4. 메뉴 아이템 컴포넌트 (텍스트 + 흰색 원형 버튼)
@Composable
private fun FloatingActionMenuItem(
    text: String,
    iconId: Int,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // 텍스트 라벨
        Text(
            text = text,
            color = ContentBlack,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(16.dp))

        // 흰색 원형 버튼
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = text,
                modifier = Modifier.size(28.dp),
                tint = Color.Unspecified
            )
        }
    }
}
package com.example.howsu.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val rotation by animateFloatAsState(if (isMenuExpanded) 45f else 0f)
        // 2) FAB + 메뉴 — Dim 위에 overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),     // 이 padding은 FAB에게만 적용됨
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(
                    visible = isMenuExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FloatingActionMenuItem("투두", R.drawable.cat) {
                            onTodoClick()
                            isMenuExpanded = false
                        }
                        FloatingActionMenuItem("일정", R.drawable.bone) {
                            onScheduleClick()
                            isMenuExpanded = false
                        }
                        FloatingActionMenuItem("피드", R.drawable.dog) {
                            onFeedCreateClick()
                            isMenuExpanded = false
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { isMenuExpanded = !isMenuExpanded },
                    containerColor = YellowCustom,
                    shape = CircleShape,
                    elevation = if (isMenuExpanded)
                        FloatingActionButtonDefaults.elevation(0.dp)
                    else
                        FloatingActionButtonDefaults.elevation(),
                    modifier = Modifier.rotate(rotation)
                ) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        val strokeWidth = 1.5.dp.toPx()
                        drawLine(
                            color = ContentBlack,
                            start = Offset(size.width * 0.2f, size.height / 2),
                            end = Offset(size.width * 0.8f, size.height / 2),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = ContentBlack,
                            start = Offset(size.width / 2, size.height * 0.2f),
                            end = Offset(size.width / 2, size.height * 0.8f),
                            strokeWidth = strokeWidth
                        )
                    }
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
                .background(Color.White)
                .border(1.dp, YellowCustom, CircleShape),
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
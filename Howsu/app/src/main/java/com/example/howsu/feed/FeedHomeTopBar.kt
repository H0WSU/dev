package com.example.howsu.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedHomeTopBar(){
    Row {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        Spacer(Modifier.width(8.dp))

        Column {
            Text(
                text = "소중한 추억을 공유하세요",
                fontSize = 16.sp,
                color = Color.LightGray
            )
            Text(
                text = "이구역의짱",  //추후 닉네임
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        IconButton(onClick = {/* 추후 검색 기능 추가*/}) {
            Icon(Icons.Default.Search, contentDescription = "검색")
        }

        IconButton(onClick = {/* 추후 검색 기능 추가*/}) {
            Icon(Icons.Default.Menu, contentDescription = "메뉴")
        }
    }

}
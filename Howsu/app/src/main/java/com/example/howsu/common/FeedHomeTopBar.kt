package com.example.howsu.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.howsu.data.model.FamilyMember


@Composable
fun FeedHomeTopBar(
    member: FamilyMember,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 프로필 이미지
        if (!member.profileImageUrl.isNullOrBlank()) {
            // 1. 사진 URL이 있으면 이미지 표시
            AsyncImage(
                model = member.profileImageUrl,
                contentDescription = "프로필 이미지",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        }

        Spacer(Modifier.width(12.dp))

        // 가운데 텍스트(위: 안내문구, 아래: 닉네임)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "소중한 추억을 공유하세요",
                fontSize = 16.sp,
                color = Color.LightGray
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = member.nickName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 오른쪽 아이콘들
        IconButton(onClick = { /* 추후 검색 기능 */ }) {
            Icon(Icons.Default.Search, contentDescription = "검색")
        }
    }
}

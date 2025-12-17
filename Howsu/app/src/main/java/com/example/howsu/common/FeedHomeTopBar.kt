package com.example.howsu.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
        val profileOffset = 1.dp

        // 1. 왼쪽 프로필 이미지
        if (!member.profileImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = member.profileImageUrl,
                contentDescription = "프로필 이미지",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .offset(y = profileOffset)
                    .size(42.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .offset(y = profileOffset) // ★ 여기도 추가! (기본 이미지일 때)
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "기본 프로필",
                    modifier = Modifier.align(Alignment.Center),
                    tint = Color.Gray
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // 2. 가운데 텍스트
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.height(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "소중한 추억을 공유하세요",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = member.nickName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
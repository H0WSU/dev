package com.example.howsu.common

import androidx.compose.foundation.border
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
        // 1. 왼쪽 프로필 이미지 (HomeTopAppBar와 동일한 로직)
        if (!member.profileImageUrl.isNullOrBlank()) {
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
                    tint = Color.Gray
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // 2. 가운데 텍스트 (위치 보정)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // ★ 수정: 홈 화면의 '가족 선택 버튼' 높이(24dp)와 동일한 Box로 감싸서
            // 텍스트 위치가 미세하게 어긋나는 현상을 방지함
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

            // 홈 화면과 동일한 간격 유지
            Spacer(Modifier.height(4.dp)) // 기존엔 0dp 였으나 Home과 맞추려면 있어야 할 수도 있음. Home 코드 확인 시 4dp 있으면 유지.

            Text(
                text = member.nickName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
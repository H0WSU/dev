package com.example.howsu.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.howsu.data.model.Family
import com.example.howsu.data.model.FamilyMember

@Composable
fun HomeTopAppBar(
    member: FamilyMember,
    family: Family,
    userFamilies: List<Family>,
    onFamilySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 왼쪽 프로필 이미지
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
                    )
            ){
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "기본 프로필",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(Modifier.width(15.dp))

        // 2. 가운데 텍스트
        Column(
            modifier = Modifier.weight(1f)
        ) {
            FamilyNameSelector(
                currentFamily = family,
                allFamilies = userFamilies,
                onFamilySelected = onFamilySelected
            )

            Text(
                text = member.nickName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 3. 알림 버튼 (기존 유지)
        IconButton(onClick = { /* 연결 필요 */ }) {
            Icon(Icons.Filled.Notifications, contentDescription = "알림")
        }
    }
}

// 가족 이름 드롭다운 선택 컴포넌트
@Composable
fun FamilyNameSelector(
    currentFamily: Family,
    allFamilies: List<Family>,
    onFamilySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // 가족이 여러 개이거나, 현재 선택된 가족이 있다면 드롭다운 표시
    if (allFamilies.size > 1 && currentFamily.familyId.isNotBlank()) {
        Box {
            TextButton(
                onClick = { expanded = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White,
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp) // 패딩 제거
            ) {
                Text(
                    text = currentFamily.familyName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "가족 선택",
                    modifier = Modifier.size(15.dp),
                    tint = Color.Gray
                )
            }

            // 드롭다운 메뉴
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                allFamilies.forEach { familyItem ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = familyItem.familyName,
                                fontWeight = if (familyItem.familyId == currentFamily.familyId) FontWeight.Bold else FontWeight.Normal,
                                color = if (familyItem.familyId == currentFamily.familyId) Color.Black else Color.Gray
                            )
                        },
                        onClick = {
                            onFamilySelected(familyItem.familyId)
                            expanded = false
                        }
                    )
                }
            }
        }
    } else {
        // 가족이 1개 이하일 경우 또는 ID가 없을 경우 일반 텍스트 표시
        Text(
            text = currentFamily.familyName.ifBlank { "가족 없음" },
            fontSize = 16.sp,
            color = Color.LightGray
        )
    }
}
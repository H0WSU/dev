package com.example.howsu.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
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

        Spacer(Modifier.width(12.dp))

        // 2. 가운데 텍스트
        Column(
            modifier = Modifier.weight(1f)
        ) {
            FamilyNameSelector(
                currentFamily = family,
                allFamilies = userFamilies,
                onFamilySelected = onFamilySelected
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = member.nickName,
                fontSize = 18.sp,
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

    // 이름 뒤에 '네 가족' / '이네 가족' 붙여주는 로직
    fun getDisplayName(name: String): String {
        if (name.isBlank()) return "가족 없음"
        val lastChar = name.last()
        val hasBatchim = if (lastChar.code in 0xAC00..0xD7A3) {
            (lastChar.code - 0xAC00) % 28 > 0
        } else {
            false
        }
        return if (hasBatchim) "${name}이네 가족" else "${name}네 가족"
    }

    val displayTitle = getDisplayName(currentFamily.familyName)

    // 가족이 여러 개이거나, 현재 선택된 가족이 있다면 드롭다운 표시
    if (allFamilies.size > 1 && currentFamily.familyId.isNotBlank()) {
        Box {
            TextButton(
                onClick = { expanded = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Gray, // ★ 버튼 글씨는 원래대로 회색
                    containerColor = Color.Transparent
                ),
                // ★ 공백 제거 및 높이 고정 (닉네임과 간격 좁힘)
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(24.dp) // 글자가 작으므로 높이도 24dp로 더 줄임
            ) {
                Text(
                    text = displayTitle,
                    fontSize = 16.sp,           // ★ 원래대로 16sp
                    fontWeight = FontWeight.Medium, // ★ 원래대로 Medium
                    color = Color.Gray          // ★ 원래대로 회색
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "가족 선택",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray           // ★ 아이콘도 회색
                )
            }

            // 드롭다운 메뉴 스타일
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White
            ) {
                allFamilies.forEach { familyItem ->
                    val isSelected = familyItem.familyId == currentFamily.familyId

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = getDisplayName(familyItem.familyName),
                                // ★ 드롭다운 안에서는 선택된 것만 진하게(#121212)
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF121212) else Color.Gray
                            )
                        },
                        onClick = {
                            onFamilySelected(familyItem.familyId)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    } else {
        // 가족이 1개 이하일 경우 (드롭다운 없음) - 원래 스타일 유지
        Text(
            text = displayTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium, // 원래대로
            color = Color.LightGray // 보내주신 코드에 맞춰 연한 회색
        )
    }
}
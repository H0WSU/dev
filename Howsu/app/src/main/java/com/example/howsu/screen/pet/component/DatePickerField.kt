package com.example.howsu.screen.pet.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.howsu.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DatePickerField(
    birthdayExact: String,
    onClick: () -> Unit
) {
    val displayText = remember(birthdayExact) {
        if (birthdayExact.isBlank()) {
            "날짜를 선택해 주세요"
        } else {
            runCatching {
                val localDate = LocalDate.parse(birthdayExact) // yyyy-MM-dd
                val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
                localDate.format(formatter)
            }.getOrElse { "날짜를 선택해 주세요" }
        }
    }

    // 투두 DatePickerField 와 동일한 스타일
    val borderColor = Color(0xFF121212)
    val contentBlack = Color(0xFF121212)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(17.dp)),
        shape = RoundedCornerShape(17.dp),
        color = Color.White,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.date_under), // 투두와 같은 아이콘 사용
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.padding(end = 8.dp)
            )

            Column {
                Text(
                    text = "date",
                    fontSize = 10.sp,
                    color = contentBlack.copy(alpha = 0.7f)
                )
                Text(
                    text = displayText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = contentBlack
                )
            }
        }
    }
}

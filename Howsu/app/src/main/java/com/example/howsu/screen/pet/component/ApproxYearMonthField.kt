package com.example.howsu.screen.pet.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.howsu.R
import java.time.LocalDate
import java.time.Period

@Composable
fun ApproxYearMonthField(
    year: String,
    month: String,
    onClick: () -> Unit
) {
    val now = LocalDate.now()

    val ageText = remember(year, month) {
        val y = year.toIntOrNull()
        val m = month.toIntOrNull()

        if (y == null || m == null || m !in 1..12) null
        else {
            val birthDate = LocalDate.of(y, m, 1)
            val period = Period.between(birthDate, now)
            val years = period.years
            val months = period.months + years * 12

            when {
                months < 12 -> "${months}개월"
                else -> "${years}살"
            }
        }
    }

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
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.date_under),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.padding(end = 8.dp)
            )

            Column {
                Text(
                    "date",
                    fontSize = 10.sp,
                    color = contentBlack.copy(alpha = 0.7f)
                )

                if (year.isNotBlank() && month.isNotBlank()) {
                    Text(
                        text = "${year}년 ${month}월",
                        fontSize = 13.sp,
                        color = contentBlack
                    )
                } else {
                    Text(
                        text = "생년월을 선택해 주세요",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (ageText != null) {
                Text(
                    text = ageText,
                    fontSize = 14.sp,
                    color = contentBlack.copy(alpha = 0.8f),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

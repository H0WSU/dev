package com.example.howsu.screen.pet.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
            var age = now.year - y
            if (now.monthValue < m) age--
            if (age < 0) age = 0
            "${age}살"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )

            Column {
                Text("date", fontSize = 10.sp, color = Color.Gray)

                if (year.isNotBlank() && month.isNotBlank()) {
                    Text(
                        text = "${year}년 ${month}월",
                        fontSize = 13.sp
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
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

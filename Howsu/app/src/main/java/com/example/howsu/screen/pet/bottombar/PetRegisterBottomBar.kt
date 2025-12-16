package com.example.howsu.screen.pet.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.howsu.screen.todo.ContentBlack
import com.example.howsu.screen.todo.YellowBox

@Composable
fun PetRegisterBottomBar(
    enabled: Boolean,
    isLastStep: Boolean,
    showSkip: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 45.dp)
    ) {

        Button(
            onClick = onNext,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowBox, // ★ 색상 적용 (노랑)
                contentColor = ContentBlack // ★ 색상 적용 (검정)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isLastStep) "완료하기" else "계속하기",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (showSkip) {
            Spacer(Modifier.height(13.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Gray
                )
            ) {
                Text("나중에 등록하고 싶어요! 건너뛰기",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp)
            }
        }
    }
}
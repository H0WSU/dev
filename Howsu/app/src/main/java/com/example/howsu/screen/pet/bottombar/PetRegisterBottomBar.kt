package com.example.howsu.screen.pet.bottombar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PetRegisterBottomBar(
    enabled: Boolean,
    isLastStep: Boolean,
    showSkip: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 60.dp)
    ) {

        Button(
            onClick = onNext,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) Color.Black else Color(0xFFE0E0E0),
                contentColor = if (enabled) Color.White else Color(0xFFBDBDBD)
            )
        ) {
            Text(if (isLastStep) "완료하기" else "계속하기", fontSize = 14.sp)
        }

        if (showSkip) {
            Spacer(Modifier.height(13.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("나중에 등록하고 싶어요! 건너뛰기", color = Color.Gray)
            }
        }
    }
}

package com.example.howsu.screen.pet.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GenderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) Color.Black else Color.White
    val border = if (selected) Color.Black else Color(0xFFE0E0E0)
    val textColor = if (selected) Color.White else Color.Black

    Box(
        modifier = modifier
            .height(44.dp)
            .border(1.dp, border, RoundedCornerShape(24.dp))
            .background(bg, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor)
    }
}

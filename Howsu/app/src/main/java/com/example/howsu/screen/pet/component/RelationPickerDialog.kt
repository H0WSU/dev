package com.example.howsu.screen.pet.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RelationPickerDialog(
    currentRelation: String,
    onDismiss: () -> Unit,
    onRelationSelected: (String) -> Unit
) {
    val relations = listOf("엄마", "아빠", "언니", "누나", "형", "오빠", "동생")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        },
        title = { Text("어떤 역할을 맡고 있나요?") },
        text = {
            Column {
                relations.forEach { rel ->
                    val selected = rel == currentRelation
                    Text(
                        text = rel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRelationSelected(rel) }
                            .padding(vertical = 6.dp),
                        fontSize = 15.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.Black else Color.Gray
                    )
                }
            }
        }
    )
}

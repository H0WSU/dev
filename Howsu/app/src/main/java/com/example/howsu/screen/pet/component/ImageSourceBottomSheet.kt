package com.example.howsu.screen.pet.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourceBottomSheet(
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "사진 추가 방법을 선택해 주세요",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 앨범에서 선택
            SheetOption(
                text = "앨범에서 선택",
                onClick = onPickGallery
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 카메라 촬영
            SheetOption(
                text = "카메라로 촬영",
                onClick = onTakePhoto
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SheetOption(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 15.sp
            )
        }
    }
}

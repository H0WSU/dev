package com.example.howsu.screen.pet.step

import android.R.attr.font
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.howsu.data.model.PetRegisterUiState
import com.example.howsu.screen.pet.component.DoubleRingProfileImage

@Composable
fun PhotoNameStep(
    state: PetRegisterUiState,
    onName: (String) -> Unit,
    onPickImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        DoubleRingProfileImage(
            imageUrl = state.profilePetImageUrl,
            onClick = onPickImage
        )

        Text(
            text = "아이의 이름을 입력해 주세요",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )

        OutlinedTextField(
            value = state.petName,
            onValueChange = onName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            placeholder = { Text("이름 입력하기") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

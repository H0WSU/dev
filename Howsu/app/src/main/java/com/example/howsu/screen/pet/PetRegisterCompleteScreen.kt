package com.example.howsu.screen.pet

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.howsu.data.model.BirthdayInputType
import com.example.howsu.data.model.PetRegisterUiState
import com.example.howsu.screen.pet.component.PetProfileImageOnly

@Composable
fun PetRegisterCompleteScreen(
    uiState: PetRegisterUiState,
    onAddMore: () -> Unit,
    onFinish: () -> Unit,
    onPickImage: () -> Unit

) {
    Scaffold (
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "반려동물 등록이 완료되었어요!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            PetProfileImageOnly(
                imageUrl = uiState.profilePetImageUrl,
                size = 160.dp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = uiState.petName.ifBlank { "우리 아이" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold, // 글씨 굵게
                color = Color(0xFF121212)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(
                    label = "관계",
                    value = if (uiState.relation.isNotBlank()) uiState.relation else "미입력"
                )
                InfoRow(label = "성별", value = when (uiState.gender) {
                    "MALE" -> "남아"
                    "FEMALE" -> "여아"
                    else -> "미입력"
                })
                InfoRow(
                    label = "중성화",
                    value = when (uiState.isNeutered) {
                        true -> "했어요"
                        false -> "안 했어요"
                        null -> "미입력"
                    }
                )
                InfoRow(
                    label = "몸무게",
                    value = if (uiState.weight.isNotBlank()) "${uiState.weight} kg" else "미입력"
                )
                InfoRow(
                    label = "생년월일",
                    value = when (uiState.birthdayInputType) {
                        BirthdayInputType.EXACT ->
                            if (uiState.birthdayExact.isNotBlank()) uiState.birthdayExact else "미입력"

                        BirthdayInputType.APPROX -> {
                            val y = uiState.birthdayYearApprox
                            val m = uiState.birthdayMonthApprox
                            if (y.isNotBlank() && m.isNotBlank()) "${y}년 ${m}월경"
                            else "미입력"
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onAddMore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDF37),
                    contentColor = Color.Black
                )
            ) {
                Text("반려동물 추가로 등록하기")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F5),
                    contentColor = Color.Black
                )
            ) {
                Text("그만하기")
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        Text(text = value, fontSize = 13.sp)
    }
}

package com.example.howsu.screen.pet.step

import android.R.attr.fontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.howsu.data.model.PetRegisterUiState
import com.example.howsu.screen.pet.component.DoubleRingProfileImage
import com.example.howsu.screen.pet.component.GenderChip
import com.example.howsu.screen.pet.component.PetProfileImageOnly

@Composable
fun GenderWeightStep(
    state: PetRegisterUiState,
    onGender: (String) -> Unit,
    onWeight: (String) -> Unit,
    onNeuteredChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PhotoNameStep 과 동일: 맨 위 여백
        Spacer(modifier = Modifier.height(28.dp))

        PetProfileImageOnly(
            imageUrl = state.profilePetImageUrl,
            size = 160.dp
        )

        // 이름
        Text(
            text = state.petName.ifBlank { "우리 아이" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black
        )

        // 성별 질문
        Text(
            text = "성별은 무엇인가요?",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )

        // 성별 선택 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenderChip(
                text = "여아",
                selected = state.gender == "FEMALE",
                onClick = { onGender("FEMALE") },
                modifier = Modifier.weight(1f)
            )
            GenderChip(
                text = "남아",
                selected = state.gender == "MALE",
                onClick = { onGender("MALE") },
                modifier = Modifier.weight(1f)
            )
        }

        // 중성화 여부 (네가 쓰던 로직 그대로)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val current = state.isNeutered == true
                    onNeuteredChanged(!current)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = state.isNeutered == true,
                onClick = {
                    val current = state.isNeutered == true
                    onNeuteredChanged(!current)
                }
            )
            Text(
                text = "중성화했어요",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF828282),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // 몸무게 질문
        Text(
            text = "몸무게는 몇 kg 인가요?",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )

        // 몸무게 입력
        // 몸무게 입력
        OutlinedTextField(
            value = state.weight,
            onValueChange = { new ->
                // 숫자와 '.' 만 허용
                var filtered = new.filter { it.isDigit() || it == '.' }

                // '.' 은 최대 1개만 허용
                val dotCount = filtered.count { it == '.' }
                if (dotCount > 1) {
                    val lastDotIndex = filtered.lastIndexOf('.')
                    filtered = filtered.removeRange(lastDotIndex, lastDotIndex + 1)
                }

                // 맨 앞이 '.' 이면 "0." 으로 보정
                if (filtered.startsWith(".")) {
                    filtered = "0$filtered"
                }

                onWeight(filtered)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            placeholder = { Text("예) 2.3") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            suffix = {                    // ← 오른쪽 안쪽에 붙는 텍스트
                Text(
                    text = "kg",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        )

// PhotoNameStep 과 동일: 맨 아래 여백
        Spacer(modifier = Modifier.height(40.dp))


        // PhotoNameStep 과 동일: 맨 아래 여백
        Spacer(modifier = Modifier.height(40.dp))
    }
}



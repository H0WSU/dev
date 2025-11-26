package com.example.howsu.screen.pet.step

import android.R.attr.text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import com.example.howsu.screen.pet.component.ApproxYearMonthField
import com.example.howsu.screen.pet.component.DatePickerField
import com.example.howsu.screen.pet.component.PetProfileImageOnly

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BirthdayStep(
    state: PetRegisterUiState,
    onType: (BirthdayInputType) -> Unit,
    onExact: (String) -> Unit,
    onYear: (String) -> Unit,
    onMonth: (String) -> Unit,
    onDatePickerClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        PetProfileImageOnly(
            imageUrl = state.profilePetImageUrl,
            size = 160.dp
        )

        Text(
            text = state.petName.ifBlank { "우리 아이" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black
        )

        Text(
            text = "생년월일을 알고 있나요?",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF616161)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1) 정확히 알고 있어요
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onType(BirthdayInputType.EXACT)
                    onDatePickerClicked()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            ) {
            RadioButton(
                selected = state.birthdayInputType == BirthdayInputType.EXACT,
                onClick = {
                    onType(BirthdayInputType.EXACT)
                    onDatePickerClicked()
                }
            )
            Text(
                text = "정확히 알고 있어요",
                fontSize = 15.sp,
                color = Color.Black
            )
        }

        // 선택됐을 때만, 이 사이에 카드가 "끼어 들어감"
        AnimatedVisibility(
            visible = state.birthdayInputType == BirthdayInputType.EXACT,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))   // 위 여백
                DatePickerField(
                    birthdayExact = state.birthdayExact,
                    onClick = onDatePickerClicked
                )
                Spacer(modifier = Modifier.height(8.dp))   // 아래 여백
            }
        }

        // 2) 대략만 알고 있어요
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onType(BirthdayInputType.APPROX)
                    onDatePickerClicked()
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = state.birthdayInputType == BirthdayInputType.APPROX,
                onClick = {
                    onType(BirthdayInputType.APPROX)
                    onDatePickerClicked()
                }
            )
            Text(
                text = "대략만 알고 있어요",
                fontSize = 15.sp,
                color = Color.Black
            )
        }

        AnimatedVisibility(
            visible = state.birthdayInputType == BirthdayInputType.APPROX,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                ApproxYearMonthField(
                    year = state.birthdayYearApprox,
                    month = state.birthdayMonthApprox,
                    onClick = onDatePickerClicked
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

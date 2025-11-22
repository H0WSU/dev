package com.example.howsu.screen.pet.step

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
import androidx.compose.ui.unit.dp
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
    onExact: (String) -> Unit,  // 현재 코드에서는 직접 사용 안 하지만 시그니처 유지
    onYear: (String) -> Unit,
    onMonth: (String) -> Unit,
    onDatePickerClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        PetProfileImageOnly(
            imageUrl = state.profilePetImageUrl,
            size = 160.dp
        )

        Text(
            text = state.petName.ifBlank { "우리 아이" },
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "생년월일을 알고 있나요?",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1) 정확히 알고 있어요
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable  {
                        onType(BirthdayInputType.EXACT)
                        onDatePickerClicked()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = state.birthdayInputType == BirthdayInputType.EXACT,
                    onClick = {
                        onType(BirthdayInputType.EXACT)
                        onDatePickerClicked()
                    }
                )
                Text(text = "정확히 알고 있어요")
            }

            AnimatedVisibility(
                visible = state.birthdayInputType == BirthdayInputType.EXACT,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                DatePickerField(
                    birthdayExact = state.birthdayExact,
                    onClick = onDatePickerClicked
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2) 대략만 알고 있어요
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable  {
                    onType(BirthdayInputType.APPROX)
                    onDatePickerClicked()
                }
            ) {
                RadioButton(
                    selected = state.birthdayInputType == BirthdayInputType.APPROX,
                    onClick = {
                        onType(BirthdayInputType.APPROX)
                        onDatePickerClicked()
                    }
                )
                Text(text = "대략만 알고 있어요")
            }

            AnimatedVisibility(
                visible = state.birthdayInputType == BirthdayInputType.APPROX,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ApproxYearMonthField(
                    year = state.birthdayYearApprox,
                    month = state.birthdayMonthApprox,
                    onClick = onDatePickerClicked
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
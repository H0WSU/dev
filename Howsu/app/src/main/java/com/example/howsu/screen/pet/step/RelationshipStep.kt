package com.example.howsu.screen.pet.step

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.howsu.data.model.PetRegisterUiState
import com.example.howsu.screen.pet.component.PetProfileImageOnly
import com.example.howsu.screen.pet.component.RelationChip

@Composable
fun RelationshipStep(
    state: PetRegisterUiState,
    onRelationClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        PetProfileImageOnly(
            imageUrl = state.profilePetImageUrl,
            size = 160.dp
        )

        Text(
            text = "${state.petName.ifBlank { "우리 아이" }}는 나를",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RelationChip(
                text = if (state.relation.isNotBlank()) state.relation else "선택",
                onClick = onRelationClick
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = "(으)로 생각해요",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

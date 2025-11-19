package com.example.howsu.screen.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.howsu.screen.login.AuthViewModel

@Composable
fun SettingScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()

) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { SettingTopBar(onBackClick = { navController.popBackStack() }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            SettingNotificationRow()
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingAccountSection(
                onLogoutClick = { showLogoutDialog = true },
                onWithdrawClick = { showWithdrawDialog = true }
            )
        }
    }

    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "로그아웃",
            text = "로그아웃 하시겠어요?",
            confirmText = "로그아웃",
            onConfirm = {
                authViewModel.signOut()
                showLogoutDialog = false
                navController.navigate("login") {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showWithdrawDialog) {
        ConfirmationDialog(
            title = "회원 탈퇴",
            text = "정말 탈퇴하시겠어요?",
            confirmText = "회원 탈퇴",
            onConfirm = {
                authViewModel.deleteUserAndLogout()
                showWithdrawDialog = false
                navController.navigate("login") {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            },
            onDismiss = { showWithdrawDialog = false }
        )
    }
}

// --- 공통 컴포저블 ---

@Composable
private fun SettingTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.ArrowBack, "뒤로가기", modifier = Modifier.size(24.dp))
        }
        Text(
            "설정",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun SettingNotificationRow() {
    val customSwitchColors = SwitchDefaults.colors(
        checkedTrackColor = Color.Black,
        checkedThumbColor = Color.White,
        uncheckedTrackColor = Color.LightGray,
        uncheckedThumbColor = Color.White,
        uncheckedBorderColor = Color.LightGray,
        disabledCheckedTrackColor = Color.Black,
        disabledCheckedThumbColor = Color.White,
        disabledUncheckedTrackColor = Color.LightGray,
        disabledUncheckedThumbColor = Color.White,
        disabledUncheckedBorderColor = Color.LightGray
    )

    var isChecked by remember { mutableStateOf(true) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isChecked = !isChecked }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(40.dp)
    ) {
        Text(
            text = "알림",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            modifier = Modifier.scale(0.8f),
            colors = customSwitchColors
        )
    }
}

@Composable
private fun SettingAccountSection(
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        // '계정' 섹션 헤더 - 16sp, Bold
        Text(
            text = "계정",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        SettingMenuItem(label = "로그아웃", onClick = onLogoutClick)
        SettingMenuItem(label = "회원 탈퇴", onClick = onWithdrawClick)
    }
}

@Composable
private fun SettingMenuItem(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onClick) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "이동",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
@Composable
private fun ConfirmationDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(text)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (confirmText.contains("탈퇴")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("취소")
            }
        }
    )
}
package com.example.howsu.screen.family

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share // ★ 공유 아이콘 추가
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.howsu.screen.todo.ContentBlack
import com.example.howsu.screen.todo.YellowBox
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FamilyInviteScreen(
    navController: NavHostController,
    familyNameInput: String,
    invitedFamilyId: String,
    userProfileUrl: String? = null,
    isFromMypage: Boolean = false // ★ 마이페이지에서 왔는지 확인하는 플래그 추가
) {
    val displayName = getFamilyNameWithSuffix(familyNameInput)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // ★ 공유하기 함수
    fun shareFamilyInfo() {
        val shareText = "[하우스] 우리 가족으로 초대합니다!\n\n가족 ID: $invitedFamilyId\n\n앱에서 가족 ID를 입력해 주세요."
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "가족 초대 코드 공유하기")
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            InviteTopBar(
                onBack = { navController.popBackStack() }, // 스택 구조상 마이페이지에서 왔으면 마이페이지로 감
                onShare = { shareFamilyInfo() } // ★ 공유 버튼 동작 연결
            )
        },
        bottomBar = {
            InviteBottomBar(
                isFromMypage = isFromMypage, // ★ 상태 전달
                onComplete = {
                    if (isFromMypage) {
                        navController.popBackStack() // 마이페이지면 그냥 닫기(뒤로가기)
                    } else {
                        navController.navigate("register_pet") // 회원가입 흐름이면 펫 등록으로
                    }
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // --- QR 카드 영역 ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        RealQrCodeImage(
                            content = invitedFamilyId,
                            modifier = Modifier.size(180.dp)
                        )
                    }
                }
                ProfileImageCircle(
                    imageUrl = userProfileUrl,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- "또는" 구분선 ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
                Text(
                    "또는",
                    color = Color(0xFFBDBDBD),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- 아이디 복사 섹션 ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        clipboardManager.setText(AnnotatedString(invitedFamilyId))
                        android.widget.Toast
                            .makeText(context, "아이디가 복사되었습니다", android.widget.Toast.LENGTH_SHORT)
                            .show()
                    }
                    .background(Color.White)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "아이디 복사하기",
                        color = Color(0xFF757575),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        invitedFamilyId,
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ... QR 생성 관련 함수들 (RealQrCodeImage, generateQrBitmap)은 그대로 유지 ...
@Composable
fun RealQrCodeImage(
    content: String,
    modifier: Modifier = Modifier
) {
    var qrBitmap by remember(content) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(content) {
        withContext(Dispatchers.IO) {
            qrBitmap = generateQrBitmap(content)
        }
    }

    Box(
        modifier = modifier.background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap!!.asImageBitmap(),
                contentDescription = "QR Code",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)))
        }
    }
}

fun generateQrBitmap(content: String): Bitmap? {
    return try {
        val hints = hashMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = 1
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        for (x in 0 until w) {
            for (y in 0 until h) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ... getFamilyNameWithSuffix 그대로 유지 ...
fun getFamilyNameWithSuffix(name: String): String {
    if (name.isBlank()) return "우리 가족"
    val lastChar = name.last()
    if (lastChar < '가' || lastChar > '힣') return "${name}네 가족"
    val hasBatchim = (lastChar.code - 0xAC00) % 28 > 0
    return if (hasBatchim) "${name}이네 가족" else "${name}네 가족"
}


// ★ TopBar 수정: 공유 버튼 추가
@Composable
fun InviteTopBar(onBack: () -> Unit, onShare: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(40.dp)
    ) {
        // 뒤로가기 버튼
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(39.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.ArrowBack, "뒤로 가기", modifier = Modifier.size(24.dp))
        }

        // 제목
        Text(
            "가족 초대하기",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        // 공유하기 버튼 (오른쪽 끝)
        IconButton(
            onClick = onShare,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Share, "공유하기", modifier = Modifier.size(24.dp))
        }
    }
}

// ★ BottomBar 수정: 마이페이지 여부에 따라 버튼 텍스트 변경
@Composable
fun InviteBottomBar(isFromMypage: Boolean, onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 60.dp)
    ) {
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowBox, // ★ 색상 적용 (노랑)
                contentColor = ContentBlack // ★ 색상 적용 (검정)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = true
        ) {
            // 마이페이지면 '확인', 가입 흐름이면 '계속하기'
            Text(
                if (isFromMypage) "확인" else "계속하기",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ProfileImageCircle(imageUrl: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFEBEBEB))
            .border(4.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun FamilyInvitePreview() {
    val navController = rememberNavController()
    // Preview: 마이페이지에서 온 경우
    FamilyInviteScreen(navController, familyNameInput = "자몽", invitedFamilyId = "with@1234", isFromMypage = true)
}
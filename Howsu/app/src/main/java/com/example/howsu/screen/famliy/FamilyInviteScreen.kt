package com.example.howsu.screen.family

import android.graphics.Bitmap
import android.widget.Toast
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FamilyInviteScreen(
    navController: NavHostController,
    familyNameInput: String,
    invitedFamilyId: String, // 예: "sda@1234" -> 이걸 QR로 변환
    userProfileUrl: String? = null
) {
    val displayName = getFamilyNameWithSuffix(familyNameInput)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            InviteTopBar(onBack = { navController.popBackStack() })
        },
        bottomBar = {
            InviteBottomBar(
                onComplete = {
                    navController.navigate("register_pet")
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
                            .padding(top = 50.dp, bottom = 40.dp, start = 24.dp, end = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // ★ [수정됨] 가짜 캔버스 대신 진짜 QR 코드 컴포저블 사용
                        // invitedFamilyId(초대코드)를 QR로 변환해서 보여줌
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
                Text(
                    text = "또는",
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
                        Toast.makeText(context, "아이디가 복사되었습니다", Toast.LENGTH_SHORT).show()
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
                        text = "아이디 복사하기",
                        color = Color(0xFF757575),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = invitedFamilyId,
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------
// ★ [신규] 진짜 QR 코드를 보여주는 컴포저블
// -------------------------------------------------------------------
@Composable
fun RealQrCodeImage(
    content: String,
    modifier: Modifier = Modifier
) {
    // QR 생성은 약간의 연산이 필요하므로 비동기로 처리하거나 remember로 캐싱
    var qrBitmap by remember(content) { mutableStateOf<Bitmap?>(null) }

    // content가 바뀔 때마다 QR 비트맵 새로 생성
    LaunchedEffect(content) {
        // IO 스레드에서 생성 (UI 버벅임 방지)
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
            // 로딩 중이거나 실패했을 때 빈 공간 (또는 로딩 인디케이터)
            Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
        }
    }
}

// ★ [신규] 문자열 -> QR Bitmap 변환 함수 (ZXing 라이브러리 사용)
fun generateQrBitmap(content: String): Bitmap? {
    return try {
        val hints = hashMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = 1 // 테두리 여백 최소화

        val writer = QRCodeWriter()
        // 512x512 픽셀 크기의 QR 비트 매트릭스 생성
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                // 검은색(0xFF000000) or 흰색(0xFFFFFFFF)
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// --- 기존 로직 함수들 ---
fun getFamilyNameWithSuffix(name: String): String {
    if (name.isBlank()) return "우리 가족"
    val lastChar = name.last()
    if (lastChar < '가' || lastChar > '힣') return "${name}네 가족"
    val hasBatchim = (lastChar.code - 0xAC00) % 28 > 0
    return if (hasBatchim) "${name}이네 가족" else "${name}네 가족"
}

@Composable
fun InviteTopBar(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp).height(40.dp)) {
        Text("가족 초대하기", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
        IconButton(onClick = onBack, modifier = Modifier.size(39.dp).align(Alignment.CenterStart)) {
            Icon(Icons.Default.ArrowBack, "뒤로 가기", modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun InviteBottomBar(onComplete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 60.dp)) {
        Button(
            onClick = onComplete, modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp), enabled = true
        ) { Text("완료하기", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}

@Composable
fun ProfileImageCircle(imageUrl: String?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(CircleShape).background(Color(0xFFEBEBEB)).border(4.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
        if (!imageUrl.isNullOrBlank()) { AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun FamilyInvitePreview() {
    val navController = rememberNavController()
    // 진짜 QR 코드가 "sda@1234"라는 내용을 담아서 생성됨
    FamilyInviteScreen(navController, familyNameInput = "자몽", invitedFamilyId = "sda@1234")
}

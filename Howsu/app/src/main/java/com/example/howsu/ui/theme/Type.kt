package com.example.howsu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle // TextStyle 임포트
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.howsu.R

// 1. 폰트 패밀리 정의
val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_semibold, FontWeight.SemiBold)
)

// 2. Material 3 기본 타이포그래피
private val M3DefaultTypography = Typography()

// 3. Poppins 폰트만 적용된 기본 스타일
private val PoppinsBaseStyle = TextStyle(
    fontFamily = PoppinsFontFamily
)

// 4. M3 기본 스타일에 Poppins 스타일을 'merge'
val Typography = Typography(
    displayLarge = M3DefaultTypography.displayLarge.merge(PoppinsBaseStyle),
    displayMedium = M3DefaultTypography.displayMedium.merge(PoppinsBaseStyle),
    displaySmall = M3DefaultTypography.displaySmall.merge(PoppinsBaseStyle),

    headlineLarge = M3DefaultTypography.headlineLarge.merge(PoppinsBaseStyle),
    headlineMedium = M3DefaultTypography.headlineMedium.merge(PoppinsBaseStyle),
    headlineSmall = M3DefaultTypography.headlineSmall.merge(PoppinsBaseStyle),

    titleLarge = M3DefaultTypography.titleLarge.merge(PoppinsBaseStyle),
    titleMedium = M3DefaultTypography.titleMedium.merge(PoppinsBaseStyle),
    titleSmall = M3DefaultTypography.titleSmall.merge(PoppinsBaseStyle),

    bodyLarge = M3DefaultTypography.bodyLarge.merge(PoppinsBaseStyle),
    bodyMedium = M3DefaultTypography.bodyMedium.merge(PoppinsBaseStyle),
    bodySmall = M3DefaultTypography.bodySmall.merge(PoppinsBaseStyle),

    labelLarge = M3DefaultTypography.labelLarge.merge(PoppinsBaseStyle),
    labelMedium = M3DefaultTypography.labelMedium.merge(PoppinsBaseStyle),
    labelSmall = M3DefaultTypography.labelSmall.merge(PoppinsBaseStyle)
)
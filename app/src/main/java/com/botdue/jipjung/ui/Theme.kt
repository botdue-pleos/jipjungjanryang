package com.botdue.jipjung.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import android.content.res.AssetManager
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pleos 공식 서체 Asta Sans. 가변 글꼴 하나를 굵기별로 불러 쓴다.
 * res/font로 넣으면 굵기(wght) 축이 적용되지 않아 assets에서 읽는다.
 */
@OptIn(ExperimentalTextApi::class)
fun astaSans(assets: AssetManager): FontFamily = FontFamily(
    listOf(FontWeight.Normal, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold).map { w ->
        Font(
            path = "fonts/AstaSans.ttf",
            assetManager = assets,
            weight = w,
            variationSettings = FontVariation.Settings(FontVariation.weight(w.weight)),
        )
    }
)

val LocalFont = staticCompositionLocalOf<FontFamily> { FontFamily.Default }

/** 현대차 CI 색 기반 팔레트. 야간은 Hyundai Blue 바탕, 주간은 Light Sand 바탕 */
data class Palette(
    val bg: Color,
    val fg: Color,
    val sub: Color,
    val line: Color,
    val cellEmpty: Color,
    val cellBar: Color,
    val cellNow: Color,
    val primaryBg: Color,
    val primaryFg: Color,
    val outline: Color,
    val cardBg: Color,
    val cardFg: Color,
    val cardSub: Color,
    val accent: Color,
    val mapNone: Color,
    val mapStable: Color,
    val mapSlight: Color,
    val mapOften: Color,
)

val NightPalette = Palette(
    bg = Color(0xFF002C5F), fg = Color(0xFFF6F3F2), sub = Color(0xFFAACAE6), line = Color(0xFF2A5A8F),
    cellEmpty = Color(0xFF0E3B6F), cellBar = Color(0xFF3A7FB8), cellNow = Color(0xFFE4DCD3),
    primaryBg = Color(0xFF00AAD2), primaryFg = Color(0xFF002C5F), outline = Color(0xFF3F6799),
    cardBg = Color(0xFFE4DCD3), cardFg = Color(0xFF002C5F), cardSub = Color(0xFF3B4A5C), accent = Color(0xFF00AAD2),
    mapNone = Color(0xFF0E3B6F), mapStable = Color(0xFF3A7FB8), mapSlight = Color(0xFF7FA9C9), mapOften = Color(0xFFE4DCD3),
)

val DayPalette = Palette(
    bg = Color(0xFFF6F3F2), fg = Color(0xFF002C5F), sub = Color(0xFF4A5A70), line = Color(0xFFD5CCC2),
    cellEmpty = Color(0xFFE4DCD3), cellBar = Color(0xFF6B8FB3), cellNow = Color(0xFF002C5F),
    primaryBg = Color(0xFF002C5F), primaryFg = Color(0xFFF6F3F2), outline = Color(0xFFB9AFA4),
    cardBg = Color(0xFFE4DCD3), cardFg = Color(0xFF002C5F), cardSub = Color(0xFF3B4A5C), accent = Color(0xFF007FA8),
    mapNone = Color(0xFFE4DCD3), mapStable = Color(0xFFAACAE6), mapSlight = Color(0xFF6B8FB3), mapOften = Color(0xFF002C5F),
)

val LocalPalette = staticCompositionLocalOf { NightPalette }

val pal: Palette
    @Composable get() = LocalPalette.current

/**
 * 시안(920x1330 px 창 기준) 수치를 그대로 옮기기 위한 변환.
 * Pleos 샘플 글자 체계(headline 38, body 20, 최소 18sp)에 맞춰 1.5로 나누고, 글자는 18sp 아래로 내리지 않는다.
 */
val Int.px: Dp get() = (this / 1.5f).dp
val Int.tx: TextUnit get() = maxOf(this / 1.5f, 18f).sp

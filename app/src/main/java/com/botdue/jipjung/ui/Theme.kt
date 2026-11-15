package com.botdue.jipjung.ui

import android.content.res.AssetManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
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
    /** 모래색 카드: 운전자가 맡겨 둔 생각에만 쓴다 (사람의 말이 화면의 주인공) */
    val cardBg: Color,
    val cardFg: Color,
    val cardSub: Color,
    val accent: Color,
    val mapNone: Color,
    val mapStable: Color,
    val mapSlight: Color,
    val mapOften: Color,
    /** 차가 알려 주는 정보 카드 (창문·전조등 등) */
    val surface: Color,
    /** 칸이 다 찬 충전 칸 (회전 안내를 가장 잘 따른 시간) */
    val cellFull: Color,
    /** 주행 화면 아래 자막 띠 */
    val band: Color,
    /** 주행 중 자막(깜빡이 안내). 바탕과 대비가 가장 큰 색: 야간은 흰색 */
    val caption: Color,
)

/*
 * 야간: 광고 "밤낚시"의 밤 충전소. 바탕은 현대차 고유색 Hyundai Blue.
 * 충전 칸은 찬 정도에 따라 세 단계(Sky Blue > 중간 > 빈 칸 테두리), 충전 빛 Active Blue는 "지금"에만.
 * 모래색은 운전자가 맡겨 둔 생각에만 쓴다.
 * 주간 지도: 운전 안 한 칸은 빈 테두리(mapNone 투명), 자주 흐트러진 칸(바닥)은 경고 톤 호박색으로 채운다.
 */
val NightPalette = Palette(
    bg = Color(0xFF002C5F), fg = Color(0xFFF6F3F2), sub = Color(0xFFAACAE6), line = Color(0xFF1E4A7C),
    cellEmpty = Color(0xFF1E4A7C), cellBar = Color(0xFF5E8FBF), cellNow = Color(0xFF00AAD2),
    primaryBg = Color(0xFF00AAD2), primaryFg = Color(0xFF002C5F), outline = Color(0xFF4C77A6),
    cardBg = Color(0xFFE4DCD3), cardFg = Color(0xFF002C5F), cardSub = Color(0xFF3B4A5C), accent = Color(0xFF00AAD2),
    mapNone = Color.Transparent, mapStable = Color(0xFFAACAE6), mapSlight = Color(0xFF5E8FBF), mapOften = Color(0xFFF2A33A),
    surface = Color(0xFF00214A), cellFull = Color(0xFFAACAE6), band = Color(0xFF00214A), caption = Color(0xFFFFFFFF),
)

val DayPalette = Palette(
    bg = Color(0xFFF2F5F9), fg = Color(0xFF002C5F), sub = Color(0xFF4A5A70), line = Color(0xFFC9D6E4),
    cellEmpty = Color(0xFFC9D6E4), cellBar = Color(0xFF7FA3C6), cellNow = Color(0xFF007FA8),
    primaryBg = Color(0xFF002C5F), primaryFg = Color(0xFFF6F3F2), outline = Color(0xFF9DB2C8),
    cardBg = Color(0xFFE4DCD3), cardFg = Color(0xFF002C5F), cardSub = Color(0xFF3B4A5C), accent = Color(0xFF007FA8),
    mapNone = Color.Transparent, mapStable = Color(0xFF3A6EA5), mapSlight = Color(0xFF7FA3C6), mapOften = Color(0xFFC46A12),
    surface = Color(0xFFFFFFFF), cellFull = Color(0xFF3A6EA5), band = Color(0xFFE3EAF2), caption = Color(0xFF002C5F),
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

/**
 * 설계 기준 창(세로 1칸 613×887dp, 가로 2칸 1247×887dp — 920×1330·1870×1330px, 240dpi)을 실제 앱 창 크기에 맞춰 통째로 비례 확대한다.
 * Pleos Connect v2.1.0 에뮬레이터(2560×1440, 160dpi)의 세로 1칸 창은 약 841×1210dp라, 이대로 두면 화면이 작게 그려지고 아래가 빈다.
 */
@Composable
fun FitWindow(content: @Composable () -> Unit) {
    val c = LocalConfiguration.current
    val w = c.screenWidthDp.toFloat()
    val h = c.screenHeightDp.toFloat()
    val k = (if (w < h) minOf(w / 613f, h / 887f) else minOf(w / 1247f, h / 887f)).coerceIn(0.5f, 3f)
    val d = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density * k, d.fontScale)) { content() }
}

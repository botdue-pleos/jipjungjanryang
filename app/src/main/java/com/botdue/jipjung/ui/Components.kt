package com.botdue.jipjung.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em

@Composable
fun T(
    text: String,
    size: TextUnit,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Normal,
    color: Color = pal.fg,
    lineHeight: Float = 1.4f,
    align: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = LocalFont.current,
            fontSize = size,
            fontWeight = weight,
            color = color,
            lineHeight = lineHeight.em,
            letterSpacing = if (size.value >= 30) (-0.02).em else 0.em,
            // 한국어를 어절 단위로 줄바꿈 ("있/어요"처럼 끊기지 않게). 기기 언어와 상관없이 ko로 지정해야 적용된다
            localeList = LocaleList("ko-KR"),
            textAlign = align ?: TextAlign.Unspecified,
            lineBreak = LineBreak(
                strategy = if (size.value >= 30) LineBreak.Strategy.Balanced else LineBreak.Strategy.HighQuality,
                strictness = LineBreak.Strictness.Strict,
                wordBreak = LineBreak.WordBreak.Phrase,
            ),
        ),
    )
}

/** 3x3 픽셀 로고 */
@Composable
fun PixelLogo(sizeDp: Dp = 32.px) {
    val p = pal
    Canvas(Modifier.size(sizeDp)) {
        val cell = size.width * 7f / 26f
        val step = size.width * 9.5f / 26f
        val colors = listOf(
            p.accent, p.accent, Color(0xFF0E3B6F),
            p.accent, Color(0xFFE4DCD3), Color(0xFF0E3B6F),
            p.accent, p.accent, p.accent,
        )
        colors.forEachIndexed { i, c ->
            drawRoundRect(
                color = c,
                topLeft = Offset((i % 3) * step, (i / 3) * step),
                size = Size(cell, cell),
                cornerRadius = CornerRadius(cell * 0.15f),
            )
        }
    }
}

@Composable
fun Header(title: String, trailing: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        PixelLogo()
        T(title, 26.tx, Modifier.padding(start = 14.px).weight(1f), FontWeight.Bold)
        if (trailing != null) T(trailing, 26.tx, color = pal.sub)
    }
}

@Composable
fun SettingRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 24.px),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        T(label, 28.tx, color = pal.sub)
        T(value, 28.tx)
    }
}

@Composable
fun Divider(color: Color = pal.line) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.px)
            .background(color)
    )
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(104.px)
            .background(pal.primaryBg, RoundedCornerShape(14.px))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { T(text, 32.tx, weight = FontWeight.Bold, color = pal.primaryFg) }
}

@Composable
fun OutlineButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(104.px)
            .border(BorderStroke(2.px, pal.outline), RoundedCornerShape(14.px))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { T(text, 30.tx, weight = FontWeight.SemiBold) }
}

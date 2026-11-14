package com.botdue.jipjung.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.botdue.jipjung.engine.FocusCurve
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 출발할 때: 화면에 한 가지만. 하루 집중 잔량을 둥근 충전 링으로 보여 주고, 지금이 평소 흐트러지는 때면
 * 도움 모드를 권한다. 링의 칸 하나가 한 시간(6시~24시), 바깥으로 많이 찰수록 회전 안내를 잘 따른 시간이다.
 */
@Composable
fun DepartScreen(
    curve: FocusCurve,
    hour: Int,
    clock: String,
    onStart: (helpMode: Boolean) -> Unit,
) {
    val risky = curve.suggestHelpMode(hour)
    val window = hour until hour + FocusCurve.HELP_WINDOW_HOURS
    val ratio = curve.eveningVsMorningRatio().roundToInt()

    Column(
        Modifier
            .fillMaxSize()
            .background(pal.bg)
            .padding(start = 64.px, end = 64.px, top = 52.px, bottom = 56.px),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        T(clock, 28.tx, Modifier.align(Alignment.End), color = pal.sub)
        ChargeRing(curve.barHeights(), window, risky)
        Spacer(Modifier.height(12.px))
        // 결론은 링 가운데에 있으니 여기서는 근거만. 칸 읽는 법은 링 설명(contentDescription)에 둔다
        T(
            if (risky) "지난 3주, 이 시간엔 회전 안내를 오전보다 ${ratio}배 자주 놓쳤어요."
            else "지난 3주, 이 시간엔 회전 안내를 잘 따르셨어요.",
            30.tx, Modifier.width(720.px), color = pal.fg, lineHeight = 1.5f, align = TextAlign.Center,
        )
        if (risky) {
            Spacer(Modifier.height(10.px))
            T(
                "깜빡이 놓침 ${curve.signalMissCount(window)}번, 지나친 갈림길 ${curve.passedCount(window)}번",
                26.tx, color = pal.sub, align = TextAlign.Center,
            )
            // 도움 모드 제안에 늘 붙인다. 판정이 아니라 본인이 돌아보게 하는 한 문장 (TTS도 같은 문장)
            Spacer(Modifier.height(18.px))
            T(BODY_CHECK, 26.tx, color = pal.fg, weight = FontWeight.SemiBold, align = TextAlign.Center)
        }
        Spacer(Modifier.weight(1f))
        T("도움 모드: 편한 길, 회전 전 한 번 더, 시원한 바람", 24.tx, color = pal.sub, align = TextAlign.Center)
        Spacer(Modifier.height(20.px))
        Box(
            Modifier
                .fillMaxWidth()
                .height(120.px)
                .background(pal.primaryBg, RoundedCornerShape(60.px))
                .clickable(role = Role.Button) { onStart(true) },
            contentAlignment = Alignment.Center,
        ) { T("도움 모드로 출발", 36.tx, weight = FontWeight.ExtraBold, color = pal.primaryFg) }
        Spacer(Modifier.height(8.px))
        Box(
            Modifier
                .height(80.px)
                .clickable(role = Role.Button) { onStart(false) }
                .padding(horizontal = 40.px),
            contentAlignment = Alignment.Center,
        ) { T("그냥 갈게요", 30.tx, color = pal.sub) }
    }
}

/**
 * 하루 집중 잔량 링. 270도에 18칸(6시~24시). 칸의 길이 = 그 시간의 충전 정도(1~6칸).
 * 다 찬 칸은 Sky Blue, 덜 찬 칸은 중간 파랑, 지금부터 3시간은 Active Blue. 빈 자리는 테두리만.
 */
@Composable
fun ChargeRing(heights: List<Int>, nowHours: IntRange, risky: Boolean) {
    val p = pal
    Box(
        Modifier
            .size(width = 720.px, height = 690.px)
            .semantics { contentDescription = "하루 집중 잔량. 칸이 많이 찬 시간일수록 회전 안내를 잘 따랐어요. 지금 ${nowHours.first}시부터 ${nowHours.last + 1}시는 " + if (risky) "흐트러지기 쉬운 시간이에요." else "잘 따르는 시간이에요." },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height * 0.53f)
            val r0 = size.width * 0.24f
            val step = size.width * 0.038f
            fun pt(r: Float, deg: Float): Offset {
                val a = Math.toRadians((deg - 90).toDouble())
                return Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat())
            }
            fun sector(r1: Float, r2: Float, a1: Float, a2: Float) = Path().apply {
                arcTo(androidx.compose.ui.geometry.Rect(c, r2), a1 - 90, a2 - a1, true)
                lineTo(pt(r1, a2).x, pt(r1, a2).y)
                arcTo(androidx.compose.ui.geometry.Rect(c, r1), a2 - 90, a1 - a2, false)
                close()
            }
            heights.forEachIndexed { i, h ->
                val a1 = -135f + i * 15f + 1.5f
                val a2 = a1 + 12f
                val charge = 7 - h
                drawPath(sector(r0, r0 + 6 * step, a1, a2), p.cellEmpty, style = Stroke(width = 1.5f))
                val color = when {
                    (i + 6) in nowHours -> p.cellNow
                    charge >= 6 -> p.cellFull
                    else -> p.cellBar
                }
                drawPath(sector(r0, r0 + charge * step, a1, a2), color)
            }
        }
        val label = @Composable { text: String, x: Int, y: Int, now: Boolean ->
            T(text, 24.tx, Modifier.offset(x = x.px, y = y.px), color = if (now) p.cellNow else p.sub, weight = if (now) FontWeight.Bold else FontWeight.Normal)
        }
        label("6시", 116, 626, false)
        label("12시", 96, 94, false)
        label("지금", 582, 154, true)
        label("24시", 582, 626, false)
        // 링 가운데는 결론부터: 지금 몇 시부터 몇 시가 어떤 시간인지
        Column(
            Modifier.align(Alignment.Center).offset(y = 24.px),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            T("지금 ${nowHours.first}~${nowHours.last + 1}시", 34.tx, color = p.cellNow, weight = FontWeight.Bold)
            Spacer(Modifier.height(6.px))
            T(
                if (risky) "흐트러지기\n쉬운 시간" else "잘 따르는\n시간",
                46.tx, weight = FontWeight.ExtraBold, lineHeight = 1.25f, align = TextAlign.Center,
            )
        }
    }
}

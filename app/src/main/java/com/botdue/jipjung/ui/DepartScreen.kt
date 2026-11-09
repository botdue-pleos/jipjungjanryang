package com.botdue.jipjung.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.botdue.jipjung.engine.FocusCurve
import kotlin.math.roundToInt

/** 출발할 때: 지금 시간대가 평소 집중이 흐트러지는 때인지 알려주고 도움 모드를 권한다 */
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
            .padding(start = 64.px, end = 64.px, top = 64.px, bottom = 56.px)
    ) {
        Header("집중잔량", clock)
        Spacer(Modifier.height(64.px))
        if (risky) {
            T("지금은 평소에 집중이\n흐트러지는 시간이에요", 56.tx, weight = FontWeight.Bold, lineHeight = 1.25f)
            Spacer(Modifier.height(24.px))
            T("지난 3주 동안 이 시간대에는 회전 안내를\n오전보다 ${ratio}배 자주 놓치셨어요.", 30.tx, color = pal.sub, lineHeight = 1.5f)
            Spacer(Modifier.height(12.px))
            T("깜빡이 놓침 ${curve.signalMissCount(window)}번, 지나친 갈림길 ${curve.passedCount(window)}번", 26.tx, color = pal.sub)
        } else {
            T("지금은 평소에 집중이\n안정적인 시간이에요", 56.tx, weight = FontWeight.Bold, lineHeight = 1.25f)
            Spacer(Modifier.height(24.px))
            T("지난 3주 동안 이 시간대에는\n회전 안내를 잘 따르셨어요.", 30.tx, color = pal.sub, lineHeight = 1.5f)
        }

        Spacer(Modifier.height(56.px))
        FocusCurveGrid(curve.barHeights(), nowHours = window)
        Spacer(Modifier.height(14.px))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("6시", "12시", "18시", "24시").forEach { T(it, 22.tx, color = pal.sub) }
        }
        Spacer(Modifier.height(8.px))
        T("칸이 많이 찬 시간일수록 회전 안내를 잘 따랐어요. 파란 칸이 지금부터 3시간이에요.", 22.tx, color = pal.sub)

        Spacer(Modifier.height(44.px))
        Divider()
        SettingRow("경로", "차선 변경이 적은 편한 길"); Divider()
        SettingRow("안내", "회전하기 전에 한 번 더"); Divider()
        SettingRow("공조", "시원한 바람"); Divider()

        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(16.px)) {
            OutlineButton("그냥 갈게요", Modifier.weight(1f)) { onStart(false) }
            PrimaryButton("도움 모드로 출발", Modifier.weight(2f)) { onStart(true) }
        }
    }
}

/**
 * 하루 집중 곡선을 전기차 충전 표시처럼 그린다: 6시~24시 18개 막대, 막대마다 6칸.
 * 그 시간에 회전 안내를 잘 따랐을수록 칸이 많이 찬다(집중이 남아 있던 시간).
 * 지금부터 3시간은 충전 빛(Active Blue)으로 칠하고 빈 칸도 같은 색 테두리로 보여 준다.
 */
@Composable
fun FocusCurveGrid(heights: List<Int>, nowHours: IntRange, rows: Int = 6) {
    val p = pal
    Column(
        Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "하루 집중 잔량. 칸이 많이 찬 시간일수록 회전 안내를 잘 따랐어요." },
        verticalArrangement = Arrangement.spacedBy(8.px),
    ) {
        for (row in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.px)) {
                heights.forEachIndexed { col, h ->
                    val charge = rows + 1 - h
                    val filled = rows - row <= charge
                    val isNow = (col + 6) in nowHours
                    val shape = RoundedCornerShape(3.px)
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .then(
                                when {
                                    filled -> Modifier.background(if (isNow) p.cellNow else p.cellBar, shape)
                                    else -> Modifier.border(BorderStroke(2.px, if (isNow) p.cellNow else p.cellEmpty), shape)
                                }
                            )
                    )
                }
            }
        }
    }
}

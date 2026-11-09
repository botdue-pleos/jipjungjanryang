package com.botdue.jipjung.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

/**
 * 주행 중: 화면 가운데 다음 회전 하나만. 남은 거리를 링이 줄어들며 보여 주고,
 * 할 말은 영화 자막처럼 아래 띠에 한 줄로. 시연에서는 남은 거리가 1초마다 줄어든다.
 */
@Composable
fun DrivingScreen(
    helpMode: Boolean,
    memos: List<String>,
    onAddMemo: () -> Unit,
    onPark: () -> Unit,
) {
    var distance by remember { mutableIntStateOf(300) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            distance = if (distance <= 0) 300 else distance - 25
        }
    }
    val remind = helpMode && distance in 1..100

    Column(Modifier.fillMaxSize().background(pal.bg)) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 52.px),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            T(if (helpMode) "도움 모드로 운전 중" else "운전 중", 28.tx, color = pal.sub)
            Spacer(Modifier.weight(1f))
            DistanceRing(distance)
            Spacer(Modifier.weight(1.2f))
        }
        // 아래 자막 띠: 지금 할 일 한 줄, 맡겨 둔 생각, 시연 버튼
        Column(
            Modifier
                .fillMaxWidth()
                .background(pal.band)
                .padding(horizontal = 64.px, vertical = 44.px),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            T(
                when {
                    remind -> "지금 오른쪽 깜빡이를 켜세요"
                    helpMode -> "100m 앞에서 깜빡이를 알려드려요"
                    else -> "오른쪽으로 도세요"
                },
                if (remind) 44.tx else 32.tx,
                weight = if (remind) FontWeight.ExtraBold else FontWeight.Normal,
                color = if (remind) pal.cellNow else pal.sub,
                align = TextAlign.Center,
            )
            Spacer(Modifier.height(22.px))
            Row(verticalAlignment = Alignment.CenterVertically) {
                memos.take(5).forEach { Box(Modifier.padding(end = 10.px).size(20.px).background(pal.cardBg, CircleShape)) }
                T(
                    if (memos.isEmpty()) "떠오른 생각은 \"메모\"라고 말씀만 하세요"
                    else "맡겨 둔 생각 ${memos.size}개, 도착하면 돌려드려요",
                    26.tx, Modifier.padding(start = if (memos.isEmpty()) 0.px else 6.px), color = pal.fg,
                )
            }
            Spacer(Modifier.height(36.px))
            // 시연용: 실제 차량에서는 STT("메모")와 기어 P 신호가 대신한다
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.px)) {
                DemoButton("\"메모\" 말하기", Modifier.weight(1f), onAddMemo)
                DemoButton("P단에 넣기", Modifier.weight(1f), onPark)
            }
        }
    }
}

@Composable
private fun DemoButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(88.px)
            .border(BorderStroke(2.px, pal.outline), RoundedCornerShape(44.px))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { T(text, 26.tx, weight = FontWeight.SemiBold) }
}

/** 남은 거리 링: 300m에서 시작해 0으로 줄어든다. 가운데 화살표·거리·방향 */
@Composable
private fun DistanceRing(distance: Int) {
    val p = pal
    Box(
        Modifier
            .size(640.px)
            .semantics { contentDescription = "${distance}미터 앞에서 오른쪽으로" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width * 0.032f
            val inset = w / 2
            val arcSize = androidx.compose.ui.geometry.Size(size.width - w, size.height - w)
            val tl = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(p.cellEmpty, 0f, 360f, false, tl, arcSize, style = Stroke(w))
            drawArc(p.cellNow, -90f, 360f * (distance.coerceIn(0, 300) / 300f), false, tl, arcSize, style = Stroke(w, cap = StrokeCap.Round))
            // 우회전 화살표 (거리 숫자 위)
            val u = size.width / 320f
            val stroke = Stroke(width = 9f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val dy = -92f * u
            val arrow = Path().apply {
                moveTo(128 * u, 196 * u + dy); lineTo(128 * u, 150 * u + dy)
                quadraticTo(128 * u, 132 * u + dy, 146 * u, 132 * u + dy); lineTo(190 * u, 132 * u + dy)
                moveTo(174 * u, 114 * u + dy); lineTo(192 * u, 132 * u + dy); lineTo(174 * u, 150 * u + dy)
            }
            drawPath(arrow, p.fg, style = stroke)
        }
        Column(Modifier.offset(y = 64.px), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                T("$distance", 184.tx, weight = FontWeight.ExtraBold, lineHeight = 1f)
                T("m", 60.tx, Modifier.padding(start = 8.px, bottom = 18.px), weight = FontWeight.Bold)
            }
            T("오른쪽으로 도세요", 34.tx, color = pal.sub)
        }
    }
}

package com.botdue.jipjung.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

/**
 * 주행 중: 큰 글씨 회전 안내와 생각 주차장만 보여준다.
 * 시연에서는 남은 거리가 1초마다 줄어들고, 100m 앞에서 깜빡이 리마인드가 나온다.
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

    Column(
        Modifier
            .fillMaxSize()
            .background(pal.bg)
            .padding(64.px)
    ) {
        Header(if (helpMode) "도움 모드로 운전 중" else "운전 중")
        Spacer(Modifier.height(140.px))
        RightTurnArrow()
        Spacer(Modifier.height(36.px))
        T("${distance}m", 150.tx, weight = FontWeight.ExtraBold, lineHeight = 1f)
        Spacer(Modifier.height(20.px))
        T("오른쪽으로 도세요", 52.tx, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(18.px))
        when {
            remind -> T("지금 오른쪽 깜빡이를 켜세요", 40.tx, weight = FontWeight.Bold, color = pal.accent)
            helpMode -> T("100m 앞에서 깜빡이를 알려드려요", 30.tx, color = pal.sub)
        }

        Spacer(Modifier.weight(1f))
        Divider()
        Spacer(Modifier.height(36.px))
        T("생각 주차장", 26.tx, color = pal.sub)
        Spacer(Modifier.height(12.px))
        if (memos.isEmpty()) {
            T("떠오른 생각은 \"메모\"라고 말씀만 하세요", 34.tx, weight = FontWeight.Bold)
        } else {
            T("\"${memos.last()}\" 보관했어요", 38.tx, weight = FontWeight.Bold)
            Spacer(Modifier.height(12.px))
            T("지금까지 ${memos.size}개예요. 도착하면 보여드릴게요.", 28.tx, color = pal.sub)
        }

        Spacer(Modifier.height(40.px))
        // 시연용: 실제 차량에서는 STT("메모")와 기어 P 신호가 대신한다
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.px)) {
            OutlineButton("\"메모\" 말하기", Modifier.weight(1f), onAddMemo)
            OutlineButton("P단에 넣기", Modifier.weight(1f), onPark)
        }
    }
}

@Composable
private fun RightTurnArrow() {
    val color = pal.fg
    Canvas(
        Modifier
            .size(220.px)
            .semantics { contentDescription = "우회전" }
    ) {
        val u = size.width / 24f
        val stroke = Stroke(width = 1.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val shaft = Path().apply {
            moveTo(7 * u, 21 * u)
            lineTo(7 * u, 12 * u)
            cubicTo(7 * u, 9.8f * u, 8.8f * u, 8 * u, 11 * u, 8 * u)
            lineTo(19 * u, 8 * u)
        }
        val head = Path().apply {
            moveTo(15 * u, 4 * u)
            lineTo(19 * u, 8 * u)
            lineTo(15 * u, 12 * u)
        }
        drawPath(shaft, color, style = stroke)
        drawPath(head, color, style = stroke)
    }
}

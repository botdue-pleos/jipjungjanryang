package com.botdue.jipjung.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

/**
 * 주행 중: 회전 화살표와 거리는 내비가 이미 보여 주므로 여기선 작은 보조 링으로만 둔다.
 * 화면의 주인공은 이 앱만 할 수 있는 한 줄 자막 "지금 오른쪽 깜빡이를 켜세요"다.
 * 주행 중에는 누를 버튼을 두지 않는다. 메모는 말로("메모"), P단은 기어 신호로 들어온다.
 *
 * 시연 조작(실제 차량에서는 STT와 기어 P 신호가 대신한다): 아래 음성 힌트 줄을 누르면 "메모"를 말한 것으로,
 * 화면 다른 곳을 길게 누르면 P단에 넣은 것으로 친다. [demoHints]가 false(캡처 모드)면 이 안내 글자도 숨긴다.
 * [fixedDistance]를 주면 남은 거리를 그 값에 멈춘다(캡처용). 없으면 1초마다 25m씩 줄어든다.
 */
@Composable
fun DrivingScreen(
    helpMode: Boolean,
    memos: List<String>,
    onAddMemo: () -> Unit,
    onPark: () -> Unit,
    demoHints: Boolean = true,
    fixedDistance: Int? = null,
) {
    var distance by remember { mutableIntStateOf(fixedDistance ?: 300) }
    LaunchedEffect(fixedDistance) {
        if (fixedDistance != null) return@LaunchedEffect
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
            // 시연용 숨은 조작: 길게 누르면 P단
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onPark() }) }
            .padding(start = 64.px, end = 64.px, top = 52.px, bottom = 40.px),
    ) {
        // 위: 지금 상태와 작은 남은 거리 링 (보조)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            T(if (helpMode) "도움 모드로 운전 중" else "운전 중", 28.tx, Modifier.weight(1f), color = pal.sub)
            DistanceRing(distance)
        }

        // 가운데: 자막이 주인공
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (remind) {
                T(
                    "지금 오른쪽\n깜빡이를 켜세요",
                    104.tx, Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    weight = FontWeight.ExtraBold, color = pal.caption, lineHeight = 1.2f, align = TextAlign.Center,
                )
            } else {
                T(
                    if (helpMode) "회전 100m 앞에서\n깜빡이를 알려드려요" else "도움 모드 꺼짐\n운전 기록만 남겨요",
                    44.tx, color = pal.sub, lineHeight = 1.4f, align = TextAlign.Center,
                )
            }
        }

        // 아래: 맡겨 둔 생각과 음성 힌트 한 줄. 누를 것은 없다
        Divider()
        Row(
            Modifier
                .fillMaxWidth()
                // 시연용 숨은 조작: 이 줄을 누르면 "메모"라고 말한 것으로 친다
                .pointerInput(Unit) { detectTapGestures(onTap = { onAddMemo() }) }
                .padding(top = 36.px, bottom = 12.px),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            memos.take(5).forEach { Box(Modifier.padding(end = 10.px).size(20.px).background(pal.cardBg, CircleShape)) }
            T(
                if (memos.isEmpty()) "떠오른 생각은 \"메모\"라고 말하세요"
                else "생각 ${memos.size}개 맡아 뒀어요 · \"메모\"라고 말하세요",
                30.tx, Modifier.padding(start = if (memos.isEmpty()) 0.px else 6.px), color = pal.fg,
            )
        }
        if (demoHints) {
            T("시연: 이 줄을 누르면 메모, 화면을 길게 누르면 P단", 24.tx, color = pal.outline)
        }
    }
}

/** 남은 거리 링(보조): 300m에서 0으로 줄어든다. 가운데 작은 화살표와 거리. 방향 문장은 두지 않는다 */
@Composable
private fun DistanceRing(distance: Int) {
    val p = pal
    Box(
        Modifier
            .size(200.px)
            .semantics { contentDescription = "${distance}미터 앞에서 오른쪽으로" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width * 0.05f
            val inset = w / 2
            val arcSize = androidx.compose.ui.geometry.Size(size.width - w, size.height - w)
            val tl = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(p.cellEmpty, 0f, 360f, false, tl, arcSize, style = Stroke(w))
            drawArc(p.cellNow, -90f, 360f * (distance.coerceIn(0, 300) / 300f), false, tl, arcSize, style = Stroke(w, cap = StrokeCap.Round))
            // 작은 우회전 화살표 (거리 숫자 위)
            val u = size.width / 320f
            val stroke = Stroke(width = 14f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val dy = -70f * u
            val arrow = Path().apply {
                moveTo(136 * u, 180 * u + dy); lineTo(136 * u, 150 * u + dy)
                quadraticTo(136 * u, 132 * u + dy, 154 * u, 132 * u + dy); lineTo(186 * u, 132 * u + dy)
                moveTo(170 * u, 116 * u + dy); lineTo(186 * u, 132 * u + dy); lineTo(170 * u, 148 * u + dy)
            }
            drawPath(arrow, p.sub, style = stroke)
        }
        Row(Modifier.offset(y = 24.px), verticalAlignment = Alignment.Bottom) {
            T("$distance", 52.tx, weight = FontWeight.Bold, color = p.sub, lineHeight = 1f)
            T("m", 26.tx, Modifier.padding(start = 2.px, bottom = 4.px), color = p.sub)
        }
    }
}

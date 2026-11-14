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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

data class Memo(val text: String, val note: String, val done: Boolean = false)

/** 시연용 주차 위치: 실제로는 P단에서 "몇 층 어디에 세우셨어요?"라고 묻고 STT로 받는다 */
private const val SAMPLE_SPOT = "지하 2층 C구역 14번 기둥"

/** P단에 넣었을 때: 생각 주차장에 보관한 메모를 돌려주고, 세운 자리를 남기고, 내리기 전 확인할 것을 알려준다 */
@Composable
fun ParkedScreen(
    memos: List<Memo>,
    onToggle: (Int) -> Unit,
    onWeekly: () -> Unit,
    onHome: () -> Unit,
) {
    var windowClosed by remember { mutableStateOf(false) }
    var spot by remember { mutableStateOf<String?>(null) }
    // 휴대폰 전송은 본인이 켰을 때만. 기본은 차 안에만 저장
    var sendToPhone by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(pal.bg)
            .verticalScroll(rememberScrollState())
            // 세로 1칸 창(920x1330)에 메모 3개·세운 자리·내리기 전 확인·버튼까지 스크롤 없이 들어오게 간격을 잡았다
            .padding(start = 64.px, end = 64.px, top = 56.px, bottom = 44.px)
    ) {
        T("도착했어요. 오는 길에 떠올린 생각이에요.", 28.tx, color = pal.sub)
        Spacer(Modifier.height(10.px))
        val title = if (memos.isEmpty()) "보관한 생각은 없어요" else "생각 ${memos.size}개를 보관해 뒀어요"
        T(title, 48.tx, weight = FontWeight.Bold, lineHeight = 1.25f)

        // 맡겨 둔 생각은 모래색 카드로: 이 화면의 주인공은 운전자의 말이다
        Spacer(Modifier.height(32.px))
        Column(verticalArrangement = Arrangement.spacedBy(12.px)) {
            memos.forEachIndexed { i, memo ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(if (memo.done) pal.surface else pal.cardBg, RoundedCornerShape(14.px))
                        .padding(start = 32.px, end = 20.px, top = 16.px, bottom = 16.px),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        T(memo.text, 36.tx, weight = FontWeight.Bold, color = if (memo.done) pal.sub else pal.cardFg)
                        T(memo.note, 24.tx, color = if (memo.done) pal.sub else pal.cardSub)
                    }
                    CheckChip(memo.done, memo.text) { onToggle(i) }
                }
            }
        }

        Spacer(Modifier.height(36.px))
        T("세운 자리", 26.tx, weight = FontWeight.Bold, color = pal.sub)
        Spacer(Modifier.height(10.px))
        if (spot == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T("몇 층 어디에 세우셨어요?", 36.tx, Modifier.weight(1f), FontWeight.Bold)
                SmallButton("말로 남기기") { spot = SAMPLE_SPOT }
            }
        } else {
            T(spot!!, 36.tx, weight = FontWeight.Bold)
            Spacer(Modifier.height(10.px))
            Row(verticalAlignment = Alignment.CenterVertically) {
                T(
                    if (sendToPhone) "휴대폰으로 보냈어요" else "다음 시동 때까지 차에 남겨 둘게요",
                    26.tx, Modifier.weight(1f), color = pal.sub,
                )
                if (!sendToPhone) SmallButton("휴대폰으로 보내기") { sendToPhone = true }
            }
        }

        Spacer(Modifier.height(36.px))
        Column(
            Modifier
                .fillMaxWidth()
                .background(pal.surface, RoundedCornerShape(14.px))
                .padding(horizontal = 32.px, vertical = 26.px),
            verticalArrangement = Arrangement.spacedBy(14.px),
        ) {
            T("내리기 전에", 26.tx, weight = FontWeight.Bold, color = pal.sub)
            Row(verticalAlignment = Alignment.CenterVertically) {
                T(
                    if (windowClosed) "조수석 창문을 닫았어요" else "조수석 창문이 열려 있어요",
                    34.tx, Modifier.weight(1f), FontWeight.Bold, pal.fg,
                )
                if (!windowClosed) {
                    Box(
                        Modifier
                            .height(80.px)
                            .background(pal.primaryBg, RoundedCornerShape(12.px))
                            .clickable(role = Role.Button) { windowClosed = true }
                            .padding(horizontal = 34.px),
                        contentAlignment = Alignment.Center,
                    ) { T("창문 닫기", 30.tx, weight = FontWeight.Bold, color = pal.primaryFg) }
                }
            }
            T("전조등은 꺼져 있어요", 26.tx, color = pal.sub)
        }

        // 메모가 더 많으면 스크롤된다. 버튼은 내용 아래에 붙인다
        Spacer(Modifier.height(36.px))
        Row(horizontalArrangement = Arrangement.spacedBy(16.px)) {
            OutlineButton("처음으로", Modifier.weight(1f), onHome)
            PrimaryButton("이번 주 집중 지도", Modifier.weight(2f), onWeekly)
        }
    }
}

@Composable
private fun SmallButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .height(80.px)
            .border(BorderStroke(2.px, pal.outline), RoundedCornerShape(12.px))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 30.px),
        contentAlignment = Alignment.Center,
    ) { T(text, 28.tx, weight = FontWeight.Bold) }
}

/**
 * 생각 카드의 확인 버튼. 체크 표시만 있으면 "완료"인지 "삭제"인지 헷갈려 글자로 적는다.
 * 눌러도 지우지 않는다. 회색으로 바뀌어 "봤어요" 표시만 남고, 다시 누르면 되돌린다.
 */
@Composable
private fun CheckChip(checked: Boolean, memo: String, onClick: () -> Unit) {
    val p = pal
    val fg = if (checked) p.sub else p.cardFg
    Row(
        Modifier
            .height(76.px)
            .border(BorderStroke(2.px, if (checked) p.outline else p.cardFg), RoundedCornerShape(38.px))
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics { contentDescription = if (checked) "$memo, 확인함. 누르면 되돌려요" else "$memo, 확인하기" }
            .padding(start = 20.px, end = 26.px),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(30.px)) {
            val u = size.width / 24f
            val path = Path().apply {
                moveTo(5 * u, 12 * u); lineTo(10 * u, 17 * u); lineTo(19 * u, 7 * u)
            }
            drawPath(path, fg, style = Stroke(2.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        T(if (checked) "확인함" else "확인", 26.tx, Modifier.padding(start = 8.px), FontWeight.Bold, fg)
    }
}

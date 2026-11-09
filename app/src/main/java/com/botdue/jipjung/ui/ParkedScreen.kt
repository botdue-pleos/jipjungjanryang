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
            .padding(start = 64.px, end = 64.px, top = 64.px, bottom = 56.px)
    ) {
        Header("집중잔량")
        Spacer(Modifier.height(80.px))
        T("도착했어요. 오는 길에 떠올린 생각이에요.", 30.tx, color = pal.sub)
        Spacer(Modifier.height(16.px))
        val title = if (memos.isEmpty()) "보관한 생각은\n없어요" else "생각 ${memos.size}개를\n보관해 뒀어요"
        T(title, 56.tx, weight = FontWeight.Bold, lineHeight = 1.25f)

        // 맡겨 둔 생각은 모래색 카드로: 이 화면의 주인공은 운전자의 말이다
        Spacer(Modifier.height(48.px))
        Column(verticalArrangement = Arrangement.spacedBy(14.px)) {
            memos.forEachIndexed { i, memo ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(if (memo.done) pal.surface else pal.cardBg, RoundedCornerShape(14.px))
                        .padding(horizontal = 32.px, vertical = 24.px),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        T(memo.text, 36.tx, weight = FontWeight.Bold, color = if (memo.done) pal.sub else pal.cardFg)
                        T(memo.note, 24.tx, color = if (memo.done) pal.sub else pal.cardSub)
                    }
                    CheckBox(memo.done, "${memo.text} 확인함") { onToggle(i) }
                }
            }
        }

        Spacer(Modifier.height(56.px))
        T("세운 자리", 26.tx, weight = FontWeight.Bold, color = pal.sub)
        Spacer(Modifier.height(16.px))
        if (spot == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T("몇 층 어디에 세우셨어요?", 36.tx, Modifier.weight(1f), FontWeight.Bold)
                SmallButton("말로 남기기") { spot = SAMPLE_SPOT }
            }
        } else {
            T(spot!!, 38.tx, weight = FontWeight.Bold)
            Spacer(Modifier.height(20.px))
            Row(verticalAlignment = Alignment.CenterVertically) {
                T(
                    if (sendToPhone) "휴대폰으로 보냈어요" else "다음 시동 때까지 차에 남겨 둘게요",
                    26.tx, Modifier.weight(1f), color = pal.sub,
                )
                if (!sendToPhone) SmallButton("휴대폰으로 보내기") { sendToPhone = true }
            }
        }

        Spacer(Modifier.height(56.px))
        Column(
            Modifier
                .fillMaxWidth()
                .background(pal.surface, RoundedCornerShape(14.px))
                .padding(36.px),
            verticalArrangement = Arrangement.spacedBy(22.px),
        ) {
            T("내리기 전에", 26.tx, weight = FontWeight.Bold, color = pal.sub)
            Row(verticalAlignment = Alignment.CenterVertically) {
                T(
                    if (windowClosed) "조수석 창문을 닫았어요" else "조수석 창문이 열려 있어요",
                    38.tx, Modifier.weight(1f), FontWeight.Bold, pal.fg,
                )
                if (!windowClosed) {
                    Box(
                        Modifier
                            .height(84.px)
                            .background(pal.primaryBg, RoundedCornerShape(12.px))
                            .clickable(role = Role.Button) { windowClosed = true }
                            .padding(horizontal = 34.px),
                        contentAlignment = Alignment.Center,
                    ) { T("창문 닫기", 30.tx, weight = FontWeight.Bold, color = pal.primaryFg) }
                }
            }
            T("전조등은 꺼져 있어요", 26.tx, color = pal.sub)
        }

        // 메모 개수에 따라 길이가 달라져 스크롤로 두고 버튼은 내용 아래에 붙인다
        Spacer(Modifier.height(56.px))
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
            .height(84.px)
            .border(BorderStroke(2.px, pal.outline), RoundedCornerShape(12.px))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 30.px),
        contentAlignment = Alignment.Center,
    ) { T(text, 28.tx, weight = FontWeight.Bold) }
}

@Composable
private fun CheckBox(checked: Boolean, label: String, onClick: () -> Unit) {
    val p = pal
    Box(
        Modifier
            .size(80.px)
            .then(
                if (checked) Modifier.background(p.accent, RoundedCornerShape(14.px))
                else Modifier.border(BorderStroke(2.px, p.outline), RoundedCornerShape(14.px))
            )
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(36.px)) {
            val u = size.width / 24f
            val path = Path().apply {
                moveTo(5 * u, 12 * u); lineTo(10 * u, 17 * u); lineTo(19 * u, 7 * u)
            }
            drawPath(
                path,
                if (checked) p.bg else p.outline,
                style = Stroke(2.5f * u, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

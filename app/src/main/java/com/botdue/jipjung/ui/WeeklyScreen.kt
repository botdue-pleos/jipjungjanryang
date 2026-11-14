package com.botdue.jipjung.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.botdue.jipjung.engine.FocusCurve
import com.botdue.jipjung.engine.FocusLevel
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

/** 이번 주 집중 지도: 가로 2칸 창에서는 좌우로, 세로 1칸 창에서는 위아래로 배치 */
@Composable
fun WeeklyScreen(curve: FocusCurve, helpDays: Set<LocalDate>, memoCount: Int, onHome: () -> Unit) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(pal.bg)
    ) {
        if (maxWidth > maxHeight) {
            Row(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .width(680.px)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(64.px)
                ) { Summary(curve, helpDays, memoCount, onHome) }
                Box(
                    Modifier
                        .width(1.px)
                        .fillMaxHeight()
                        .background(pal.line)
                )
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(64.px)
                ) { FocusMap(curve) }
            }
        } else {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(64.px)
            ) {
                FocusMap(curve)
                Spacer(Modifier.height(48.px))
                Summary(curve, helpDays, memoCount, onHome, scrolling = true)
            }
        }
    }
}

@Composable
private fun ColumnScope.Summary(curve: FocusCurve, helpDays: Set<LocalDate>, memoCount: Int, onHome: () -> Unit, scrolling: Boolean = false) {
    T("이번 주", 28.tx, color = pal.sub)
    Spacer(Modifier.height(48.px))
    T("평일 퇴근길에 가장\n자주 흐트러졌어요", 56.tx, weight = FontWeight.Bold, lineHeight = 1.25f)
    Spacer(Modifier.height(24.px))
    T("아침 운전은 안정적이에요. 오후 6시가 지나면 회전 안내를 놓치는 일이 늘었어요.", 30.tx, color = pal.sub, lineHeight = 1.5f)
    Spacer(Modifier.height(40.px))
    Divider()
    SettingRow("퇴근길에 지나친 갈림길", "${curve.passedCount(18..20)}번"); Divider()
    SettingRow("보관한 생각", "${13 + memoCount}개"); Divider()
    val effect = curve.helpEffect(18..20, helpDays)
    if (effect.withHelp != null && effect.withoutHelp != null) {
        SettingRow("퇴근길 놓침, 도움 모드 켠 ${effect.helpDayCount}일", effect.withHelp.percent()); Divider()
        SettingRow("퇴근길 놓침, 끈 날", effect.withoutHelp.percent()); Divider()
    }
    // 스크롤 안에서는 weight를 쓸 수 없어 고정 간격으로 둔다
    Spacer(if (scrolling) Modifier.height(40.px) else Modifier.height(40.px).weight(1f))
    // 앱이 하는 일과 하지 않는 일. 기록은 이 차 안에만 저장된다
    T(SCOPE_NOTICE, 24.tx, color = pal.sub, lineHeight = 1.5f)
    Spacer(Modifier.height(28.px))
    OutlineButton("처음으로", Modifier.fillMaxWidth(), onHome)
}

/**
 * 시각 기준 지도가 기본. "복용 시각 기준"은 기본 꺼짐이고, 본인이 켤 때 저장 위치와 한계를 먼저 알린다.
 * 복용 시각은 운전 기록을 돌아보는 다른 눈금일 뿐, 약의 효과를 추정하거나 운전 가능 여부를 판정하지 않는다.
 */
@Composable
private fun FocusMap(curve: FocusCurve) {
    val p = pal
    var byTime by remember { mutableStateOf(true) }
    // 복용 시각 기준 보기 켜짐 여부. 기본 꺼짐 (실제로는 차 안 로컬 DB에 저장하고 언제든 지운다)
    var doseOn by remember { mutableStateOf(false) }
    T("이번 주 집중 지도", 38.tx, weight = FontWeight.Bold)
    Spacer(Modifier.height(24.px))
    Row {
        Toggle("시각 기준", byTime) { byTime = true }
        Spacer(Modifier.width(12.px))
        Toggle(if (doseOn) "복용 시각 기준" else "복용 시각 기준 · 꺼짐", !byTime) { byTime = false }
    }
    Spacer(Modifier.height(44.px))

    if (!byTime) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(p.surface, RoundedCornerShape(14.px))
                .padding(36.px),
            verticalArrangement = Arrangement.spacedBy(20.px),
        ) {
            T(
                if (doseOn) "복용 시각을 입력하면 그 시각을 기준으로 운전 기록을 다시 펼쳐 볼 수 있어요."
                else "복용 시각을 기준으로 운전 기록을 다시 펼쳐 보는 보기예요. 지금은 꺼져 있어요.",
                30.tx, color = p.fg, lineHeight = 1.5f,
            )
            T(DOSE_NOTICE, 26.tx, color = p.sub, lineHeight = 1.5f)
            if (doseOn) OutlineButton("복용 시각 기록 지우고 끄기", Modifier.fillMaxWidth()) { doseOn = false; byTime = true }
            else PrimaryButton("복용 시각 기준 켜기", Modifier.fillMaxWidth()) { doseOn = true }
        }
        return
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.px)) {
        Spacer(Modifier.width(64.px))
        FocusCurve.SLOT_LABELS.forEach {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { T(it, 26.tx, color = p.sub) }
        }
    }
    Spacer(Modifier.height(16.px))
    val map = curve.weeklyMap()
    Column(
        Modifier.semantics { contentDescription = "요일과 시간대별 집중 지도" },
        verticalArrangement = Arrangement.spacedBy(12.px),
    ) {
        DayOfWeek.entries.forEach { day ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.px), verticalAlignment = Alignment.CenterVertically) {
                T(DAY_LABELS[day.value - 1], 26.tx, Modifier.width(64.px), color = p.sub)
                map.getValue(day).forEach { level ->
                    Box(Modifier.weight(1f).height(96.px).cell(level, 48))
                }
            }
        }
    }
    Spacer(Modifier.height(32.px))
    val legend = listOf(
        FocusLevel.NONE to "운전 안 함",
        FocusLevel.STABLE to "잔량 넉넉 (안정적)",
        FocusLevel.SLIGHT to "조금 줄어듦",
        FocusLevel.OFTEN to "바닥 (자주 흐트러짐)",
    )
    legend.chunked(2).forEach { pair ->
      Row(Modifier.padding(bottom = 16.px)) {
        pair.forEach { (level, label) ->
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                // 범례도 지도 칸과 같은 알약 모양
                Box(Modifier.size(width = 56.px, height = 28.px).cell(level, 14))
                T(label, 26.tx, Modifier.padding(start = 12.px), color = p.sub)
            }
        }
      }
    }
}

/**
 * 지도 칸과 범례가 같은 모양을 쓰도록 한곳에서 그린다.
 * 운전 안 한 칸은 빈 테두리, 바닥(자주 흐트러짐)은 경고 톤으로 채운다. 비어 보이는 칸이 "가장 나쁨"으로 읽히지 않게.
 */
@Composable
private fun Modifier.cell(level: FocusLevel, radius: Int): Modifier {
    val shape = RoundedCornerShape(radius.px)
    return this
        .background(level.color(), shape)
        .border(BorderStroke(2.px, if (level == FocusLevel.NONE) pal.outline else Color.Transparent), shape)
}

@Composable
private fun FocusLevel.color(): Color = when (this) {
    FocusLevel.NONE -> pal.mapNone
    FocusLevel.STABLE -> pal.mapStable
    FocusLevel.SLIGHT -> pal.mapSlight
    FocusLevel.OFTEN -> pal.mapOften
}

@Composable
private fun Toggle(text: String, selected: Boolean, onClick: () -> Unit) {
    val p = pal
    Box(
        Modifier
            .height(72.px)
            .then(
                if (selected) Modifier.background(p.fg, RoundedCornerShape(12.px))
                else Modifier.border(BorderStroke(2.px, p.outline), RoundedCornerShape(12.px))
            )
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 28.px),
        contentAlignment = Alignment.Center,
    ) {
        T(text, 26.tx, weight = if (selected) FontWeight.Bold else FontWeight.SemiBold, color = if (selected) p.bg else p.fg)
    }
}

private fun Double.percent() = "${(this * 100).roundToInt()}%"

private val DAY_LABELS = listOf("월", "화", "수", "목", "금", "토", "일")

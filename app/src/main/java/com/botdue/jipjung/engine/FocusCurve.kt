package com.botdue.jipjung.engine

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 도움 모드를 켠 날과 끈 날의 놓침 비율 */
data class HelpEffect(val withHelp: Double?, val withoutHelp: Double?, val helpDayCount: Int)

/** 시간대별 집중 상태. 운전 기록이 없으면 NONE */
enum class FocusLevel { NONE, STABLE, SLIGHT, OFTEN }

/**
 * 회전 판정 결과로 "언제 자주 흐트러지는지"를 계산한다.
 * 남과 비교하지 않고 본인 기록 안에서만 본다.
 */
class FocusCurve(
    private val results: List<TurnResult>,
    private val zone: ZoneId = ZoneId.of("Asia/Seoul"),
) {
    private fun hourOf(r: TurnResult) = Instant.ofEpochMilli(r.timeMs).atZone(zone).hour
    private fun dayOf(r: TurnResult) = Instant.ofEpochMilli(r.timeMs).atZone(zone).dayOfWeek

    /** 6시~24시를 1시간씩 나눈 놓침 비율 (18칸). 기록이 없는 시간은 0 */
    fun missRateByHour(): List<Double> = (6 until 24).map { hour ->
        val inHour = results.filter { hourOf(it) == hour }
        if (inHour.isEmpty()) 0.0 else inHour.count { !it.signalled }.toDouble() / inHour.size
    }

    /** 집중 곡선 막대 높이 (1~maxHeight). 본인 기록 중 가장 나쁜 시간이 가장 높다 */
    fun barHeights(maxHeight: Int = 6): List<Int> {
        val rates = missRateByHour()
        val worst = rates.maxOrNull()?.takeIf { it > 0 } ?: return rates.map { 1 }
        return rates.map { rate -> (1 + (rate / worst) * (maxHeight - 1)).toInt().coerceIn(1, maxHeight) }
    }

    /** 주어진 시간 범위의 놓침 비율 */
    fun missRate(hours: IntRange): Double {
        val r = results.filter { hourOf(it) in hours }
        return if (r.isEmpty()) 0.0 else r.count { !it.signalled }.toDouble() / r.size
    }

    /** 전체 기록의 놓침 비율 (본인 평소 기준) */
    fun overallMissRate(): Double =
        if (results.isEmpty()) 0.0 else results.count { !it.signalled }.toDouble() / results.size

    /**
     * 도움 모드를 권할지. 지금부터 [HELP_WINDOW_HOURS]시간의 놓침 비율이
     * 본인 평소의 [HELP_RATIO]배 이상이면 "흐트러지는 시간"으로 본다.
     */
    fun suggestHelpMode(hour: Int): Boolean {
        val overall = overallMissRate()
        if (overall == 0.0) return false
        return missRate(hour until hour + HELP_WINDOW_HOURS) >= overall * HELP_RATIO
    }

    /** 주어진 시간 범위에서 깜빡이를 놓친 회전 수 (지나친 갈림길 제외) */
    fun signalMissCount(hours: IntRange): Int =
        results.count { hourOf(it) in hours && !it.signalled && !it.passed }

    /** 주어진 시간 범위에서 안내받은 회전을 지나쳐 경로를 다시 찾은 수 */
    fun passedCount(hours: IntRange): Int =
        results.count { hourOf(it) in hours && it.passed }

    /**
     * 도움 모드 효과: 같은 시간대에서 도움 모드를 켠 날과 끈 날의 놓침 비율.
     * 기록이 없는 쪽은 null. 약효나 원인을 추정하지 않고 관찰된 비율만 견준다.
     */
    fun helpEffect(hours: IntRange, helpDays: Set<LocalDate>): HelpEffect {
        val inHours = results.filter { hourOf(it) in hours }
        val (on, off) = inHours.partition { dateOf(it) in helpDays }
        return HelpEffect(rateOf(on), rateOf(off), on.map { dateOf(it) }.distinct().size)
    }

    private fun dateOf(r: TurnResult) = Instant.ofEpochMilli(r.timeMs).atZone(zone).toLocalDate()
    private fun rateOf(r: List<TurnResult>) = if (r.isEmpty()) null else r.count { !it.signalled }.toDouble() / r.size

    /** 오후(18~21시) 놓침 비율이 오전(6~12시)의 몇 배인지 */
    fun eveningVsMorningRatio(): Double {
        val morning = missRate(6..11)
        return if (morning == 0.0) 0.0 else missRate(18..20) / morning
    }

    /** 요일 x 시간대(7~9, 9~12, 12~15, 15~18, 18~21, 21~24) 집중 지도 */
    fun weeklyMap(): Map<DayOfWeek, List<FocusLevel>> = DayOfWeek.entries.associateWith { day ->
        SLOTS.map { slot ->
            val inSlot = results.filter { dayOf(it) == day && hourOf(it) in slot }
            if (inSlot.isEmpty()) {
                FocusLevel.NONE
            } else {
                val rate = inSlot.count { !it.signalled }.toDouble() / inSlot.size
                when {
                    rate >= 0.3 -> FocusLevel.OFTEN
                    rate >= 0.15 -> FocusLevel.SLIGHT
                    else -> FocusLevel.STABLE
                }
            }
        }
    }

    companion object {
        const val HELP_WINDOW_HOURS = 3
        const val HELP_RATIO = 1.5
        val SLOTS = listOf(7..8, 9..11, 12..14, 15..17, 18..20, 21..23)
        /** 각 칸이 시작하는 시각 */
        val SLOT_LABELS = listOf("7시", "9시", "12시", "15시", "18시", "21시")
    }
}

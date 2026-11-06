package com.botdue.jipjung.engine

import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

/**
 * 운전 신호 공급원.
 * - [DemoSignalSource]: 시연용 가짜 3주치 주행 기록 (현재 사용)
 * - PleosSignalSource: Vehicle SDK TurnSignal, NaviHelper onTBTInfo 콜백을 [DriveSignal]로 변환 (2차 개발 예정)
 */
interface VehicleSignalSource {
    fun signals(): List<DriveSignal>
}

/**
 * 성인 ADHD 운전자의 3주 주행을 흉내 낸 데이터.
 * 오전 출근길은 안정적이고, 저녁 퇴근길(18~21시)에 깜빡이를 자주 놓치고
 * 가끔 안내받은 갈림길을 지나쳐 경로를 다시 찾는다.
 */
class DemoSignalSource(
    private val endDate: LocalDate = LocalDate.of(2026, 11, 5),
    private val days: Int = 21,
    private val seed: Int = 42,
    private val zone: ZoneId = ZoneId.of("Asia/Seoul"),
    /** 도움 모드를 켜고 달린 날. 시연 데이터는 이날 저녁 놓침이 적다고 가정한다 */
    val helpDays: Set<LocalDate> = (0L..3L).map { endDate.minusDays(it) }.toSet(),
) : VehicleSignalSource {

    override fun signals(): List<DriveSignal> {
        val random = Random(seed)
        // 지나침은 따로 뽑아 기존 시연 기록(깜빡이 판정)이 바뀌지 않게 한다
        val passRandom = Random(seed + 1)
        val out = mutableListOf<DriveSignal>()
        for (d in 0 until days) {
            val date = endDate.minusDays((days - 1 - d).toLong())
            val weekend = date.dayOfWeek.value >= 6
            val trips = if (weekend) {
                listOf(random.nextInt(10, 13), random.nextInt(14, 17))
            } else {
                listOfNotNull(
                    random.nextInt(7, 10),
                    if (random.nextInt(3) == 0) random.nextInt(12, 16) else null,
                    random.nextInt(18, 21),
                    if (random.nextInt(4) == 0) random.nextInt(21, 23) else null,
                )
            }
            for (hour in trips) {
                val start = date.atTime(hour, random.nextInt(0, 40)).atZone(zone).toInstant().toEpochMilli()
                val help = date in helpDays
                val missChance = if (help && hour in 18..20) 0.12 else missChanceAt(hour, weekend)
                repeat(random.nextInt(4, 8)) { i ->
                    val t = start + i * 90_000L + random.nextLong(0, 30_000)
                    val dir = if (random.nextBoolean()) TurnDirection.LEFT else TurnDirection.RIGHT
                    out += TurnGuidance(t - 25_000, dir, 300)
                    val signalled = random.nextDouble() >= missChance
                    if (passRandom.nextDouble() < (if (help) 0.0 else passChanceAt(hour))) {
                        out += ReRouted(t + 20_000)
                        return@repeat
                    }
                    if (signalled) {
                        val state = if (dir == TurnDirection.LEFT) TurnSignalState.LEFT else TurnSignalState.RIGHT
                        out += TurnSignalChanged(t - 5_000, state)
                        out += TurnSignalChanged(t + 3_000, TurnSignalState.OFF)
                    }
                    out += TurnExecuted(t, dir)
                }
            }
        }
        return out
    }

    private fun passChanceAt(hour: Int) = if (hour in 18..20) 0.1 else 0.0

    private fun missChanceAt(hour: Int, weekend: Boolean) = when {
        hour in 18..20 -> 0.38
        hour < 12 && !weekend -> 0.08
        else -> 0.14
    }
}

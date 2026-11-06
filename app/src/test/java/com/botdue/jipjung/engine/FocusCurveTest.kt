package com.botdue.jipjung.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class FocusCurveTest {
    private val zone = ZoneId.of("Asia/Seoul")
    // 2026-11-02 은 월요일
    private val monday = LocalDate.of(2026, 11, 2)

    private fun at(hour: Int, day: LocalDate = monday) =
        day.atTime(hour, 10).atZone(zone).toInstant().toEpochMilli()

    private fun turns(hour: Int, signalled: Int, missed: Int, passed: Int = 0, day: LocalDate = monday) =
        List(signalled) { TurnResult(at(hour, day), TurnDirection.LEFT, signalled = true) } +
            List(missed) { TurnResult(at(hour, day), TurnDirection.LEFT, signalled = false) } +
            List(passed) { TurnResult(at(hour, day), TurnDirection.LEFT, signalled = false, passed = true) }

    @Test
    fun `지나친 갈림길도 놓침 비율에 들어간다`() {
        val curve = FocusCurve(turns(19, signalled = 6, missed = 2, passed = 2))
        assertEquals(0.4, curve.missRate(19..19), 1e-9)
        assertEquals(2, curve.signalMissCount(18..20))
        assertEquals(2, curve.passedCount(18..20))
    }

    @Test
    fun `평소의 1점5배 이상 흐트러지는 시간에 출발하면 도움 모드를 권한다`() {
        // 아침 20번 중 1번, 저녁 10번 중 5번 놓침 → 평소 20%, 저녁 50%
        val curve = FocusCurve(turns(8, signalled = 19, missed = 1) + turns(19, signalled = 5, missed = 3, passed = 2))
        assertTrue(curve.suggestHelpMode(18))
        assertFalse(curve.suggestHelpMode(7))
    }

    @Test
    fun `평소와 비슷한 시간에는 도움 모드를 권하지 않는다`() {
        val curve = FocusCurve(turns(8, signalled = 8, missed = 2) + turns(19, signalled = 8, missed = 2))
        assertFalse(curve.suggestHelpMode(18))
    }

    @Test
    fun `기록이 없으면 도움 모드를 권하지 않는다`() {
        assertFalse(FocusCurve(emptyList()).suggestHelpMode(18))
    }

    @Test
    fun `주간 지도는 15%와 30%로 나눈다`() {
        val curve = FocusCurve(
            turns(8, signalled = 9, missed = 1) +   // 10% 안정적
                turns(10, signalled = 8, missed = 2) +  // 20% 조금
                turns(19, signalled = 7, missed = 2, passed = 1), // 30% 자주
        )
        val monday = curve.weeklyMap().getValue(DayOfWeek.MONDAY)
        assertEquals(FocusLevel.STABLE, monday[0])
        assertEquals(FocusLevel.SLIGHT, monday[1])
        assertEquals(FocusLevel.NONE, monday[2])
        assertEquals(FocusLevel.OFTEN, monday[4])
        assertEquals(FocusLevel.NONE, curve.weeklyMap().getValue(DayOfWeek.TUESDAY)[0])
    }

    @Test
    fun `데모 데이터의 지나친 갈림길은 저녁에 몰린다`() {
        val curve = FocusCurve(MissedTurnSignalDetector().detect(DemoSignalSource().signals()))
        assertTrue(curve.passedCount(18..20) > curve.passedCount(6..17))
        assertTrue(curve.suggestHelpMode(18))
        assertFalse(curve.suggestHelpMode(7))
    }

    @Test
    fun `도움 모드를 켠 날과 끈 날의 놓침 비율을 따로 센다`() {
        val tuesday = monday.plusDays(1)
        val curve = FocusCurve(turns(19, signalled = 9, missed = 1) + turns(19, signalled = 6, missed = 3, passed = 1, day = tuesday))
        val effect = curve.helpEffect(18..20, setOf(monday))
        assertEquals(0.1, effect.withHelp!!, 1e-9)
        assertEquals(0.4, effect.withoutHelp!!, 1e-9)
        assertEquals(1, effect.helpDayCount)
    }

    @Test
    fun `도움 모드를 켠 날이 없으면 비교하지 않는다`() {
        val effect = FocusCurve(turns(19, signalled = 9, missed = 1)).helpEffect(18..20, emptySet())
        assertEquals(null, effect.withHelp)
        assertEquals(0, effect.helpDayCount)
    }

    @Test
    fun `데모 데이터는 도움 모드를 켠 날 퇴근길 놓침이 적다`() {
        val source = DemoSignalSource()
        val effect = FocusCurve(MissedTurnSignalDetector().detect(source.signals())).helpEffect(18..20, source.helpDays)
        assertEquals(4, effect.helpDayCount)
        assertTrue(effect.withHelp!! < effect.withoutHelp!!)
    }
}

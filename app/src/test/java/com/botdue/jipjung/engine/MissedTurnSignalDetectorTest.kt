package com.botdue.jipjung.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissedTurnSignalDetectorTest {
    private val detector = MissedTurnSignalDetector(signalWindowMs = 10_000)

    @Test
    fun `안내 후 같은 방향 깜빡이를 켜고 돌면 켬으로 판정`() {
        val r = detector.detect(
            listOf(
                TurnGuidance(0, TurnDirection.RIGHT, 300),
                TurnSignalChanged(20_000, TurnSignalState.RIGHT),
                TurnExecuted(25_000, TurnDirection.RIGHT),
            )
        )
        assertEquals(1, r.size)
        assertTrue(r[0].signalled)
    }

    @Test
    fun `깜빡이 없이 돌면 놓침`() {
        val r = detector.detect(
            listOf(
                TurnGuidance(0, TurnDirection.LEFT, 300),
                TurnExecuted(25_000, TurnDirection.LEFT),
            )
        )
        assertFalse(r.single().signalled)
    }

    @Test
    fun `반대 방향 깜빡이는 놓침`() {
        val r = detector.detect(
            listOf(
                TurnGuidance(0, TurnDirection.LEFT, 300),
                TurnSignalChanged(20_000, TurnSignalState.RIGHT),
                TurnExecuted(25_000, TurnDirection.LEFT),
            )
        )
        assertFalse(r.single().signalled)
    }

    @Test
    fun `회전 10초보다 훨씬 전에 켰다 끈 깜빡이는 인정하지 않음`() {
        val r = detector.detect(
            listOf(
                TurnGuidance(0, TurnDirection.RIGHT, 300),
                TurnSignalChanged(1_000, TurnSignalState.RIGHT),
                TurnSignalChanged(3_000, TurnSignalState.OFF),
                TurnExecuted(40_000, TurnDirection.RIGHT),
            )
        )
        assertFalse(r.single().signalled)
    }

    @Test
    fun `안내 없는 회전은 판정하지 않음`() {
        val r = detector.detect(listOf(TurnExecuted(25_000, TurnDirection.RIGHT)))
        assertTrue(r.isEmpty())
    }

    @Test
    fun `안내 후 돌지 않고 경로를 다시 찾으면 지나침`() {
        val r = detector.detect(
            listOf(
                TurnGuidance(0, TurnDirection.RIGHT, 300),
                ReRouted(40_000),
            )
        )
        val passed = r.single()
        assertTrue(passed.passed)
        assertFalse(passed.signalled)
        assertEquals(TurnDirection.RIGHT, passed.direction)
    }

    @Test
    fun `안내 없이 경로를 다시 찾은 것은 세지 않음`() {
        assertTrue(detector.detect(listOf(ReRouted(40_000))).isEmpty())
    }

    @Test
    fun `안내대로 돈 뒤의 경로 재탐색은 지나침이 아님`() {
        val r = detector.detect(
            listOf(
                TurnGuidance(0, TurnDirection.LEFT, 300),
                TurnSignalChanged(20_000, TurnSignalState.LEFT),
                TurnExecuted(25_000, TurnDirection.LEFT),
                ReRouted(40_000),
            )
        )
        assertTrue(r.single().signalled)
        assertFalse(r.single().passed)
    }

    @Test
    fun `안내 90초가 지난 뒤의 경로 재탐색은 세지 않음`() {
        val r = detector.detect(
            listOf(
                TurnGuidance(0, TurnDirection.LEFT, 300),
                ReRouted(120_000),
            )
        )
        assertTrue(r.isEmpty())
    }

    @Test
    fun `데모 데이터는 저녁에 더 자주 놓친다`() {
        val results = detector.detect(DemoSignalSource().signals())
        val curve = FocusCurve(results)
        assertTrue(curve.eveningVsMorningRatio() > 1.5)
    }
}

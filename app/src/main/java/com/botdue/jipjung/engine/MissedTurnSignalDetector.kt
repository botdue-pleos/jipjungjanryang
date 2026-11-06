package com.botdue.jipjung.engine

/**
 * 회전 안내 대비 깜빡이 누락 판정.
 *
 * 규칙: 내비가 안내한 방향으로 실제 회전했을 때, 회전 직전 [signalWindowMs] 안에
 * 같은 방향 깜빡이가 한 번이라도 켜져 있었으면 "켬", 아니면 "놓침"으로 본다.
 * 안내 없이 한 회전(골목 진입 등)은 판정하지 않는다.
 *
 * 안내를 받고 [guidanceWindowMs] 안에 회전하지 않은 채 경로를 다시 찾으면
 * "지나침"으로 보고, 깜빡이 놓침과 함께 흐트러진 회전으로 센다.
 */
class MissedTurnSignalDetector(
    private val signalWindowMs: Long = 10_000,
    private val guidanceWindowMs: Long = 90_000,
) {
    fun detect(signals: List<DriveSignal>): List<TurnResult> {
        val sorted = signals.sortedBy { it.timeMs }
        val results = mutableListOf<TurnResult>()
        var signalState = TurnSignalState.OFF
        val signalHistory = mutableListOf<TurnSignalChanged>()
        var lastGuidance: TurnGuidance? = null

        for (signal in sorted) {
            when (signal) {
                is TurnGuidance -> lastGuidance = signal
                is ReRouted -> {
                    val guidance = lastGuidance
                    if (guidance != null && signal.timeMs - guidance.timeMs <= guidanceWindowMs) {
                        results += TurnResult(signal.timeMs, guidance.direction, signalled = false, passed = true)
                    }
                    lastGuidance = null
                }
                is TurnSignalChanged -> {
                    signalState = signal.state
                    signalHistory += signal
                }
                is TurnExecuted -> {
                    val guidance = lastGuidance
                    val guided = guidance != null &&
                        guidance.direction == signal.direction &&
                        signal.timeMs - guidance.timeMs <= guidanceWindowMs
                    if (guided) {
                        val expected = signal.direction.toSignalState()
                        val windowStart = signal.timeMs - signalWindowMs
                        val onInWindow = signalHistory.any {
                            it.state == expected && it.timeMs in windowStart..signal.timeMs
                        }
                        val alreadyOn = signalState == expected
                        results += TurnResult(signal.timeMs, signal.direction, onInWindow || alreadyOn)
                        lastGuidance = null
                    }
                }
            }
        }
        return results
    }

    private fun TurnDirection.toSignalState() = when (this) {
        TurnDirection.LEFT -> TurnSignalState.LEFT
        TurnDirection.RIGHT -> TurnSignalState.RIGHT
    }
}

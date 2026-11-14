package com.botdue.jipjung

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.botdue.jipjung.engine.DemoSignalSource
import com.botdue.jipjung.engine.FocusCurve
import com.botdue.jipjung.engine.MissedTurnSignalDetector
import com.botdue.jipjung.ui.DayPalette
import com.botdue.jipjung.ui.DepartScreen
import com.botdue.jipjung.ui.DrivingScreen
import com.botdue.jipjung.ui.LocalFont
import com.botdue.jipjung.ui.LocalPalette
import com.botdue.jipjung.ui.astaSans
import com.botdue.jipjung.ui.Memo
import com.botdue.jipjung.ui.NightPalette
import com.botdue.jipjung.ui.ParkedScreen
import com.botdue.jipjung.ui.WeeklyScreen

enum class Screen { Depart, Driving, Parked, Weekly }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 3주치 시연 주행 기록 → 깜빡이 누락 판정 → 집중 곡선
        val source = DemoSignalSource()
        val results = MissedTurnSignalDetector().detect(source.signals())
        val curve = FocusCurve(results)
        // 화면 캡처·영상용: adb shell am start ... --es screen Driving --ei memos 2 --ei distance 80 --ez clean true
        val start = intent.getStringExtra("screen")?.let { runCatching { Screen.valueOf(it) }.getOrNull() } ?: Screen.Depart
        val startMemos = intent.getIntExtra("memos", 0)
        // 캡처 모드: 시연 조작 안내 글자를 숨긴다 (숨은 조작 자체는 그대로 동작)
        val clean = intent.getBooleanExtra("clean", false)
        // 주행 화면의 남은 거리를 이 값(m)에 멈춘다. 없으면 300m부터 줄어든다
        val distance = if (intent.hasExtra("distance")) intent.getIntExtra("distance", 300) else null
        setContent { JipjungApp(curve, source.helpDays, start, startMemos, clean, distance) }
    }
}

/** 시연용 메모: 실제로는 STT로 받은 문장이 들어온다 */
private val SAMPLE_MEMOS = listOf(
    Memo("내일 세탁소", "오후 6:52, 신호 기다리다가"),
    Memo("엄마한테 전화", "오후 7:03, 막히는 길에서"),
    Memo("트렁크 우유 챙기기", "오후 7:10, 내릴 때 챙길 것"),
)

@Composable
fun JipjungApp(
    curve: FocusCurve,
    helpDays: Set<java.time.LocalDate>,
    start: Screen = Screen.Depart,
    startMemos: Int = 0,
    clean: Boolean = false,
    fixedDistance: Int? = null,
) {
    // AAOS 주간/야간 모드를 따른다
    val palette = if (isSystemInDarkTheme()) NightPalette else DayPalette
    var screen by remember { mutableStateOf(start) }
    var helpMode by remember { mutableStateOf(true) }
    val memos = remember { mutableStateListOf<Memo>().apply { addAll(SAMPLE_MEMOS.take(startMemos)) } }
    val assets = LocalContext.current.assets
    val font = remember { astaSans(assets) }

    BackHandler(enabled = screen != Screen.Depart) {
        screen = when (screen) {
            Screen.Weekly -> Screen.Parked
            Screen.Parked -> Screen.Driving
            else -> Screen.Depart
        }
    }

    CompositionLocalProvider(LocalPalette provides palette, LocalFont provides font) {
        when (screen) {
            // 시연 시나리오는 저녁 퇴근길(오후 6시 40분)에 출발하는 상황
            Screen.Depart -> DepartScreen(curve, hour = 18, clock = "오후 6:40") { help ->
                helpMode = help
                memos.clear()
                screen = Screen.Driving
            }
            Screen.Driving -> DrivingScreen(
                helpMode = helpMode,
                memos = memos.map { it.text },
                onAddMemo = { if (memos.size < SAMPLE_MEMOS.size) memos += SAMPLE_MEMOS[memos.size] },
                onPark = { screen = Screen.Parked },
                demoHints = !clean,
                fixedDistance = fixedDistance,
            )
            Screen.Parked -> ParkedScreen(
                memos = memos,
                onToggle = { i -> memos[i] = memos[i].copy(done = !memos[i].done) },
                onWeekly = { screen = Screen.Weekly },
                onHome = { screen = Screen.Depart },
            )
            Screen.Weekly -> WeeklyScreen(curve, helpDays, memos.size) { screen = Screen.Depart }
        }
    }
}

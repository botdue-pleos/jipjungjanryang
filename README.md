# 집중잔량

**내 운전으로 찾은 집중 시간대, 그 시간엔 차가 덜어준다.**
성인 ADHD 운전자를 위한 Pleos Connect 차량 앱. 운전 신호로 "내가 언제 집중이 흐트러지는지"를 본인 기준으로 그려 보여주고, 그 시간대에는 안내를 한 번 더 해 주고 떠오른 생각은 말로 맡아 두었다가 주차하면 돌려준다.

> 2026 현대자동차그룹 차량용 앱 개발 공모전 "Code your App into Pleos App Market" 출품작, 팀 봇듀

**[시연 영상 보기 (60초)](docs/demo.mp4)** · [APK 받기](apk/봇듀_집중잔량_release.apk) · [개발일지](docs/개발일지.md)

<p>
<img src="docs/screens/1_depart.png" width="245" alt="출발할 때: 하루 집중 곡선">
<img src="docs/screens/2_driving.png" width="245" alt="주행 중: 깜빡이 알림과 생각 주차장">
<img src="docs/screens/3_parked.png" width="245" alt="P단에 넣었을 때: 메모 돌려주기">
</p>
<img src="docs/screens/4_weekly.png" width="760" alt="이번 주 집중 지도 (주간 모드, 가로 2칸)">

<sub>Pleos Connect 앱 창 크기(세로 1칸 920×1330, 가로 2칸 1870×1330)로 맞춘 에뮬레이터에서 찍은 실제 실행 화면</sub>

## 왜 만들었나

| 사실 | 출처 |
| --- | --- |
| 20세 이상 ADHD 진료 인원 2020년 2만 5,297명에서 2024년 12만 2,614명으로 4.85배 | 국민건강보험공단 자료, 국회 남인순 의원실 분석 ([헤럴드경제 2025.10.07](https://www.heraldk.com/article/2025100722425224371)) |
| ADHD 약을 먹은 달에는 운전 사고 위험이 남성 38%, 여성 42% 낮았다 | Chang et al., JAMA Psychiatry 2017. 미국 건강보험 청구자료 230만 명 분석 |

같은 사람도 시간대에 따라 집중이 다르다. 퇴근길은 아침에 먹은 약의 효과가 떨어지는 시간과 겹치기 쉬운데, 본인도 언제 흐트러지는지 객관적으로 알 방법이 없다. 휴대폰 운전 모드는 알림만 막고, 약효 추적 앱은 실제 운전 행동을 보지 않는다.

## 무엇을 하나

| 기능 | 내용 |
| --- | --- |
| 나의 집중 곡선 | 회전 안내 대비 깜빡이 누락과 지나친 갈림길을 시간대별로 모아 "언제 흐트러지는지"를 보여준다 |
| 도움 모드 | 평소 흐트러지는 시간에 출발하면 먼저 알리고, 동의하면 회전 100m 앞에서 한 번 더 안내한다 |
| 생각 주차장 | 운전 중 떠오른 생각은 "메모"라고 말하면 보관한다. 주행 중 화면에는 아무것도 띄우지 않는다 |
| P단에서 돌려주기 | 주차하면 보관한 생각을 하나씩 돌려주고, 창문과 전조등까지 확인한다 |
| 세운 자리 남기기 | 주차하면 "몇 층 어디에 세우셨어요?"라고 묻고 말로 남긴다. 휴대폰으로는 본인이 보낼 때만 |
| 이번 주 집중 지도 | 요일과 시간대별 집중 상태. 약 먹은 시각 기준으로도 볼 수 있다 |
| 도움 모드 효과 | 같은 퇴근길에서 도움 모드를 켠 날과 끈 날의 놓침 비율을 나란히 보여준다 |

약효를 추정하지 않는다. 실제로 관찰된 운전 패턴만 보여준다.

**왜 휴대폰이 아니라 차여야 하나**: 깜빡이, 회전 안내, 기어 같은 흐트러짐의 증거는 차 안에서만 정확히 잡힌다. 생각 주차장은 기어 상태를 아는 차만 안전한 순간(P단)에 돌려줄 수 있다.

## 판정 규칙

판정은 화면과 분리된 엔진(`app/src/main/java/com/botdue/jipjung/engine/`)이 한다. 모든 규칙에 단위 테스트가 있다.

1. **깜빡이 누락**: 내비가 회전을 안내하고(`NaviHelper onTBTInfo`) 90초 안에 같은 방향으로 회전했을 때, 회전 직전 10초 안에 같은 방향 깜빡이(`Vehicle TurnSignal`)가 켜지지 않았으면 놓침. 안내 없는 회전은 판정하지 않는다.
2. **지나친 갈림길**: 회전 안내 후 90초 안에 회전하지 않고 경로를 다시 찾으면(`onReRouteEnded`) 지나침. 깜빡이 놓침과 함께 흐트러진 회전으로 센다. 안내 없는 재탐색은 세지 않는다.
3. **하루 집중 곡선**: 6시부터 24시까지 1시간 단위 놓침 비율. 본인 기록 안에서만 비교하고, 남과 비교하거나 점수로 판정하지 않는다.
4. **도움 모드 제안**: 지금부터 3시간의 놓침 비율이 본인 평소의 1.5배 이상이면 "흐트러지는 시간"으로 보고 출발할 때 알린다.
5. **주간 지도**: 요일 x 시간대 6칸. 놓침 15% 미만은 안정적, 30% 이상은 자주 흐트러짐.
6. **도움 모드 효과**: 같은 시간대(퇴근길 18~21시)에서 도움 모드를 켠 날과 끈 날의 놓침 비율을 따로 센다. 원인을 추정하지 않고 관찰된 비율만 견준다. 시연 데이터는 켠 날 놓침을 낮게 가정해 만든 것이다.

## 구조

```
app/src/main/java/com/botdue/jipjung/
├── engine/                        판정 엔진 (화면과 분리, 단위 테스트 대상)
│   ├── DriveSignal.kt             차량 신호 모델: 회전 안내, 방향지시등, 회전 통과, 경로 재탐색
│   ├── MissedTurnSignalDetector   깜빡이 누락과 지나친 갈림길 판정
│   ├── FocusCurve.kt              하루 집중 곡선, 도움 모드 판단과 효과, 주간 지도
│   └── VehicleSignalSource.kt     신호 공급원 인터페이스와 시연용 데이터
├── ui/                            Jetpack Compose 화면 (현대차 CI 색, Asta Sans, 주간·야간)
└── MainActivity.kt                화면 흐름
```

### Pleos SDK 연결 계획

| 신호 | API |
| --- | --- |
| 방향지시등 | `TurnSignal.registerTurnSignalState()` |
| 회전 안내, 경로 재탐색, 편한 길 | `NaviHelperEventListener.onTBTInfo()`, `onReRouteEnded()`, `changeRouteOption(COMFORTABLE)` |
| 문, 창문, 전조등 | `Door.registerDoorPosition()`, `Window.registerWindowPosition()`, `Light.registerLowBeamLightState()` |
| 기어(P단), 차속 | AAOS `CarPropertyManager` (GEAR_SELECTION, PERF_VEHICLE_SPEED) |
| 생각 주차장, 세운 자리 | ON_DEVICE STT, TTS |

`VehicleSignalSource` 를 구현한 `PleosSignalSource` 를 추가하면 화면과 엔진은 그대로 쓴다.

## 실행

```bash
adb install apk/봇듀_집중잔량_release.apk   # Pleos Connect Emulator(AAOS)
./gradlew :app:testDebugUnitTest            # 판정 엔진 테스트 19개
./gradlew :app:assembleRelease              # APK 빌드 (JDK 17)
```

차량 신호는 아직 시연용 3주치 주행 데이터로 재생한다. 시연 시나리오는 저녁 퇴근길(오후 6시 40분 출발)이다. 주행 화면의 "메모 말하기"가 음성 인식을, "P단에 넣기"가 기어 신호를 대신한다.

## 현재 상태

- [x] 화면 4종과 판정 엔진, 단위 테스트 6개 (v0.1.0)
- [x] 지나친 갈림길 판정, 도움 모드 판단 엔진 이동, 테스트 16개 (v0.2.0)
- [x] 세운 자리 남기기, 도움 모드 효과 비교, 테스트 19개 (v0.3.0)
- [x] Pleos 앱 창 크기 에뮬레이터에서 동작 확인, 시연 영상
- [ ] Pleos Connect Emulator에서 SDK 콜백 연결 (`PleosSignalSource`)
- [ ] 생각 주차장·세운 자리 STT·TTS, 휴대폰 전송 방식 확인, 내리기 전 확인 실제 신호, 로컬 DB 저장
- [ ] 도움 모드 경로 옵션·공조 제어, 시연용 주행 데이터 재생 도구 (2차 PT용)

자세한 진행은 [개발일지](docs/개발일지.md)에 있다.

## 원칙

- 복용·운전 기록은 차 안에만 저장하고 밖으로 보내지 않는다.
- 운전 습관을 보여줄 뿐 의학적 진단이나 약효 예측이 아니다.
- ADHD를 고쳐야 할 결함이 아니라 차가 맞춰주는 특성으로 다룬다.

## 라이선스

글꼴 Asta Sans: SIL Open Font License 1.1

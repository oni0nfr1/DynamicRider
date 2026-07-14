# DynamicRider HUD 리팩토링 계획

## 목표

HUD 장면 정의를 Kotlin DSL에서 JSON으로 통일하고, 인게임 GUI 편집기가 동일한 장면 모델을 읽고 수정하도록 전환한다.

장면 해석 우선순위는 다음과 같다.

```text
config/dynrider/hud/{mode}/{kartStateType}.json
    → assets/dynrider/hud/{mode}/{kartStateType}.json
    → assets/dynrider/hud/{mode}/default.json
    → 빈 HUD 장면
```

`assets`는 Fabric `ResourceManager`를 통해 읽으므로 리소스팩 우선순위를 따른다. 손상된 커스텀 파일은 삭제하거나 덮어쓰지 않고 오류를 알린 뒤 리소스 장면으로 fallback한다.

## 1. JSON 장면 기반 통합

- [x] 장면 포맷에 `formatVersion`을 추가한다.
- [x] 누락된 HUD 요소 serializer를 등록한다.
- [x] JSON codec을 파일 입출력과 분리한다.
- [x] 리소스 reload 시 장면 JSON을 검증하고 캐시한다.
- [x] config 저장을 임시 파일 작성 후 교체하는 방식으로 구현한다.
- [x] 기본 장면을 resource JSON으로 이관하고 builtin 장면 DSL을 제거한다.
- [x] 엔진 전용 resource가 없으면 mode별 `default.json`을 사용한다.
- [x] 요소 내부의 미사용 `Builder` 및 DSL 기반 클래스를 제거한다.

## 2. 장면 저장소와 수명주기

- [x] config/resource 우선순위를 `HudSceneRepository`로 통합한다.
- [x] resolve 결과에 `CUSTOM_CONFIG`, `RESOURCE`, `CUSTOM_FALLBACK` 출처를 기록한다.
- [x] 커스텀 저장 및 삭제 API를 제공한다.
- [x] 장면 생성 실패 시 게임을 중단하지 않고 빈 장면으로 복구한다.
- [ ] 현재 ride/spectate 엔진 분기를 `KartStateType` 기반 조회로 교체한다.
- [ ] 저장·삭제·리소스 reload 후 현재 장면을 즉시 다시 생성하는 controller를 도입한다.

## 3. 편집 문서와 레이아웃

- [x] 각 요소의 영속 ID를 JSON에 선택적으로 저장한다.
- [x] 기존 JSON에 ID가 없으면 결정적인 ID를 생성하는 `HudSceneDocument`를 제공한다.
- [x] 추가·삭제·재정렬·spec 교체를 undo/redo command로 제공한다.
- [x] anchor·scale 좌표 계산과 hit-test bounds를 `HudLayoutEngine`으로 분리한다.
- [ ] 런타임 `HudScene`이 document 변경을 감지하고 변경된 요소만 재생성하도록 연결한다.
- [ ] compound element의 자식에도 영속 ID와 편집 가능한 bounds를 제공한다.

## 4. Annotation 기반 요소 메타데이터와 Preview 데이터

요소별 descriptor 구현은 만들지 않는다. `Spec`에 선언된 annotation과 `kotlinx.serialization`의 `SerialDescriptor`를 읽어 요소 정보, 속성 편집기 및 validation 규칙을 자동 생성한다.

- [ ] `@SerialInfo` 기반의 `@HudElementInfo`, `@HudProperty`, `@HudRange`, `@HudColor`, `@HudHidden`, `@HudLayout` annotation을 정의한다.
- [ ] 클래스의 `@HudElementInfo`에서 표시 이름, 카테고리 및 선택적 아이콘 정보를 읽는다.
- [ ] property의 직렬화 타입과 annotation을 조합해 Boolean toggle, 숫자 입력, slider, enum selector, 문자열 입력, color picker 및 anchor selector를 자동 선택한다.
- [ ] 타입만으로 편집 방식을 결정할 수 있는 property에는 annotation을 요구하지 않고, 표시 이름·범위·색상 등 추가 정보가 필요할 때만 annotation을 사용한다.
- [ ] serializer, type ID, 상태 타입 호환성 및 runtime factory를 중앙 type registry에 등록하되 속성별 descriptor 코드는 작성하지 않는다.
- [ ] 현재 spec을 `JsonElement`로 encode하고 변경된 property만 교체한 뒤 같은 serializer로 decode하여 immutable spec을 갱신한다.
- [ ] generic property 변경 결과는 `ReplaceElementSpecCommand`로 document에 적용한다.
- [ ] 모든 top-level 및 compound child spec에 기본값을 제공해 type discriminator만으로 기본 요소를 생성할 수 있게 한다.
- [ ] `@HudRange` 등의 metadata를 GUI 입력 제한과 JSON load validation에서 공통으로 사용한다.
- [ ] 범위를 벗어난 외부 JSON 값은 자동 보정하지 않고 경로가 포함된 validation 오류로 반환한다.
- [ ] 초기에는 type/serializer 등록을 중앙에서 명시적으로 관리하고, 요소 수 증가로 등록 비용이 커질 때 KSP 기반 registry 생성을 검토한다.
- [ ] legacy HUD/state 코드를 신규 계약으로 이관하거나 제거한다.

## 5. 런타임·프리뷰 상태 계층

HUD 요소가 Skid API나 `KartRef`를 직접 읽지 않도록 상태 공급과 표시 효과를 분리한다. `KartState`는 Skid API 또는 편집기 프리뷰 상태를 HUD가 이해할 수 있는 원본 값으로 변환하는 어댑터이고, 기존 `Speedometer`, `GaugeBar`, `NitroSlot` 등의 delegate는 그 값을 보간·지연·클램핑하는 표시 효과 계층으로 유지한다.

```text
Skid API ───── LiveKartState ───┐
                                ├─ Speedometer / GaugeBar / NitroSlot ── HUD Element
편집기 ─── PreviewKartState ────┘
```

- [ ] 공통 카트 값을 제공하는 읽기 전용 `KartState` 인터페이스를 정의한다.
- [ ] HUD 코어의 엔진 구별 기준으로 안정적인 ID와 상태 클래스를 가진 `KartStateType<S>`를 정의하고, `KartEngine` 타입은 Skid 연결 계층 밖으로 노출하지 않는다.
- [ ] 엔진 고유 값은 `JiuKartState`, `ChargeKartState`, `V1KartState`처럼 공통 상태를 확장한 엔진별 인터페이스로 분리한다.
- [ ] `KartRef.Specific<E>`와 Skid API를 감싸는 엔진별 `LiveKartState` 구현을 제공한다.
- [ ] 편집기에서 값을 자유롭게 변경할 수 있는 엔진별 `PreviewKartState` 구현과 초기 프리셋을 제공한다.
- [ ] `KartRef`와 Skid API에 대한 직접 접근은 `LiveKartState` 계층으로 제한한다.
- [ ] 기존 `Speedometer`, `GaugeBar`, `NitroSlot` 구현이 `KartRef` 대신 호환되는 `KartState`를 입력으로 받도록 변경한다.
- [ ] raw, interpolation, trailing 등 기존 delegate 구현의 다형성과 효과를 유지한다.
- [ ] `KartState`는 보간되지 않은 의미상의 원본 값을 제공하고, 시간에 따른 보간·지연·잔상은 delegate가 담당하도록 경계를 정한다.
- [ ] `KartState`는 장면 전체에서 공유하고, 가변 애니메이션 상태를 가진 효과 delegate는 기본적으로 요소별로 생성한다.
- [ ] 경기 시간·랩처럼 카트 엔진 외부의 진행 값은 `RaceState`, 참가자·순위·로컬 플레이어 값은 `RankingState`라는 별도 상태 어댑터로 분리한다.
- [ ] 랭킹을 제공하지 않는 타임어택과 아직 참가자가 없는 랭킹을 구분할 수 있도록 `RankingState.Unavailable`과 사용 가능한 랭킹 상태를 명시적으로 모델링한다.
- [ ] `KartState`, `RaceState`, `RankingState` 및 애니메이션 시계를 읽기 전용 속성으로 묶는 `HudSceneContext<S>`를 정의한다.
- [ ] 모든 `HudElementSpec.create`와 요소 생성자는 상태별로 다른 인자를 받지 않고 `HudSceneContext<S>`와 `ElementHolder`를 받는 공통 생성 규약을 사용한다.
- [ ] 요소는 공통 context에서 필요한 상태를 골라 효과 delegate를 생성하고, `Speedometer`, `GaugeBar`, `RaceTimer` 등의 delegate에는 전체 context가 아닌 필요한 상태 인터페이스만 전달한다.
- [ ] `HudSceneContext`에는 임의 타입 조회, backend 객체 및 상태 변경 명령을 추가하지 않아 service locator로 확장되지 않게 한다.
- [ ] 실제 게임용 `LiveHudSceneContext`와 편집기용 `PreviewHudSceneContext`가 동일한 요소 생성 및 렌더링 경로를 사용하도록 한다.
- [ ] 프리뷰 애니메이션의 일시 정지·재시작·시간 이동을 지원할 수 있도록 `HudClock`을 상태 공급원과 분리한다.
- [ ] 프리뷰 값은 장면 JSON에 저장하지 않고 편집 세션 상태로 관리한다. 필요하면 별도의 편집기 설정에 마지막 사용값만 저장한다.
- [ ] 초기 구현은 기존 getter 기반 갱신 방식을 사용할 수 있으며, 일관성이나 접근 비용 문제가 확인되면 프레임 단위 불변 상태 snapshot을 도입한다.

패키지와 의존 방향은 다음과 같이 고정한다.

```text
hud/state                 외부 API를 모르는 읽기 전용 상태 계약
hud/scene                 HudSceneContext와 장면 수명주기 계약
hud/runtime/state         Skid API와 rider backend를 읽는 live adapter
hud/editor/preview        편집 가능한 preview 상태와 preset
hud/elements/**/bridge    상태값에 표시 효과를 적용하는 기존 delegate
```

- [ ] `hud.state`는 Skid API, rider backend 구현, Minecraft singleton 및 편집기 구현을 참조하지 않는다.
- [ ] `HudSceneContext`는 장면의 실행 환경이므로 `hud.scene`에 둔다.
- [ ] `KartEngine`과 `KartRef` 양쪽을 아는 live factory와 registry는 `hud.runtime`에 격리한다.
- [ ] 실제 상태 adapter는 `hud.runtime.state`, 가변 preview 구현과 preset은 `hud.editor.preview`에 둔다.
- [ ] 기존 효과 delegate의 패키지는 우선 유지하고 역할 변경과 무관한 이름 변경은 별도 작업으로 미룬다.
- [ ] 의존성은 `hud.state`와 `hud.scene`의 계약을 runtime·preview·elements가 사용하는 방향으로만 흐르게 한다.

## 6. 인게임 GUI 편집기

- [ ] 리소스 장면은 읽기 전용으로 열고 첫 변경 시 config 문서를 생성한다.
- [ ] 요소 팔레트, 캔버스 선택·이동, 속성 패널을 구현한다.
- [ ] undo/redo, 저장, 커스텀 삭제 및 리소스 기본값 복원을 제공한다.
- [ ] drag 중 명령을 병합하고 anchor 기준 좌표로 역변환한다.
- [ ] 저장 또는 삭제 후 현재 HUD를 즉시 갱신한다.

## 검증 기준

- 모든 resource JSON이 codec과 상태 타입 호환성 검사를 통과한다.
- 유효한 config가 resource보다 우선하고, config가 없으면 현재 리소스팩 장면을 사용한다.
- 잘못된 config는 보존되며 resource 장면으로 fallback한다.
- JSON encode/decode, ID 보존, atomic save, delete-to-fallback을 테스트한다.
- annotation이 serializer descriptor에 보존되고 각 property 타입에 맞는 편집 metadata가 생성되는지 테스트한다.
- generic property 변경의 encode-update-decode round-trip과 validation 오류 경로를 테스트한다.
- 모든 등록 요소가 기본 spec을 생성하고 현재 상태 타입 호환성 검사를 수행할 수 있는지 테스트한다.
- 다양한 화면 크기와 anchor/scale 조합에서 bounds와 inverse 좌표를 테스트한다.
- 모든 command는 undo 후 원본 document를 복원하고 redo 후 변경 상태를 복원한다.
- 실제 카트와 프리뷰 카트가 같은 요소 및 효과 delegate를 사용해 동일한 원본 상태에서 동일한 표시 결과를 만든다.
- 월드, 플레이어 및 실제 `KartRef`가 없는 상태에서도 모든 프리뷰 요소를 생성하고 렌더링할 수 있다.
- `PreviewKartState` 값을 변경하면 raw 및 보간형 delegate에 각각 의도한 방식으로 반영된다.
- 엔진별 요소가 호환되는 `KartState`만 입력받도록 컴파일 시점 또는 registry validation에서 검사한다.
- `compileClientKotlin`, `test`, `build`가 통과한다.

## 기본 결정

- 리소스팩의 built-in JSON override를 공식 지원한다.
- GUI는 resource 파일을 수정하지 않고 config override만 생성한다.
- 커스텀 JSON 오류 시 사용자 파일을 자동 수정하거나 삭제하지 않는다.
- 요소별 수동 property descriptor 대신 annotation과 serialization descriptor를 단일 메타데이터 원천으로 사용한다.
- immutable spec 수정은 reflection 기반 `copy()` 호출이 아니라 JSON tree round-trip으로 구현한다.
- annotation만으로 해결되지 않는 serializer/runtime factory 연결은 중앙 registry가 담당한다.
- 초기 구현은 spec 변경 시 요소를 재생성하고, 세부 runtime patch는 성능 문제가 확인된 뒤 추가한다.
- `KartState`는 기존 효과 delegate를 대체하지 않고 외부 상태 공급원을 추상화하는 입력 계층으로 사용한다.
- Skid API 및 프리뷰 구현의 차이는 상태 어댑터에서 끝내고, 이후의 효과 처리와 요소 렌더링 경로는 공유한다.
- 카트 엔진에 속하지 않는 경기·순위·시간 상태는 `KartState`에 포함하지 않는다.
- `RaceState`와 `RankingState`는 별도의 기능 계약으로 유지하되 `HudSceneContext`가 함께 소유한다.
- 요소 생성 API는 `HudSceneContext + ElementHolder`로 통일하고, 세부 의존성은 요소가 생성하는 delegate 경계에서 좁힌다.

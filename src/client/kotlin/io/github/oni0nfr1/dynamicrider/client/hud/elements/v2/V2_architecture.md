# DynamicRider V2 아키텍처 명세

## 목표
DynamicRider의 기존 HUD API는 다음 문제가 있었다.

- HUD 레이아웃을 직렬화 가능한 형태로 다루기 어렵다.
- 요소별 설정과 공통 레이아웃 정보가 일관된 구조로 정리되어 있지 않다.
- HUD가 참조하는 런타임 데이터 전달 구조가 복잡하고, 일부는 지나치게 state 중심적이다.

V2의 목표는 다음과 같다.

- HUD 구조를 직렬화 가능한 spec 계층으로 분리한다.
- 공통 레이아웃과 요소별 설정을 일관된 DSL로 정의한다.
- 실제 렌더링 객체는 spec으로부터 생성되도록 하여 구조를 단순화한다.
- 기존 V1 동작을 깨지 않고, 전이 기간 동안 공존 가능한 형태로 옮긴다.

## 작업 원칙
- V2가 완성되기 전까지 기존 V1 진입점을 망가뜨리는 변경은 하지 않는다.
- 전이 기간에는 V2가 기존 V1 렌더 경로를 통해 마운트되는 것을 허용한다.
- V2의 목표와 직접 관계없는 V1 코드는 가능한 한 그대로 둔다.
- 설계 일반화보다 실제 포팅 패턴 검증을 우선한다.
- SkidMC로의 장기 마이그레이션 가능성을 항상 염두에 두고, 현재 임시 계층은 그 전환이 쉽게 유지한다.

## 계층 구조

### 1. Spec 계층
V2에서 HUD의 정적 정의는 spec 계층에 저장한다.

- `HudLayoutSpec`
  - 공통 레이아웃 정보
  - `screenAnchor`
  - `elementAnchor`
  - `scaleX`
  - `scaleY`
  - `x`
  - `y`
  - `zIndex`
- `HudElementSpec`
  - 각 HUD 요소의 직렬화 가능한 정의
  - 반드시 `layout: HudLayoutSpec`를 가진다.
  - `create()`를 통해 실제 런타임 HUD 요소를 만든다.

핵심 원칙은 다음과 같다.

- spec에는 정적 설정만 들어간다.
- 직렬화가 필요한 값은 평탄화된 값으로 저장한다.
- 런타임에서 필요한 수학 객체는 spec에서 변환한다.

예:
- scale -> `scaleX`, `scaleY`
- position -> `x`, `y`

## 2. DSL 계층
V2 DSL의 결과물은 런타임 HUD 요소가 아니라 spec이다.

- 각 요소는 자신의 spec과 builder를 함께 가진다.
- 공통 레이아웃 입력은 `HudLayoutBuilder`가 담당한다.
- 요소별 입력은 `HudElementBuilder`의 구체 구현이 담당한다.

객체 생성 흐름은 다음과 같다.

1. DSL이 요소별 `HudElementBuilder`를 생성한다.
2. builder 내부의 `layout {}` 블록이 `HudLayoutBuilder`를 채운다.
3. builder는 완성 즉시 대응되는 `HudElementSpec`을 만든다.
4. 실제 렌더링이 필요할 때 `HudElementSpec.create()`로 런타임 객체를 만든다.

즉, V2에서 DSL은 "렌더러를 바로 조립하는 API"가 아니라 "spec을 선언하는 API"다.

## 3. 런타임 HUD 요소 계층
실제 렌더링은 spec으로부터 생성된 런타임 HUD 요소가 담당한다.

- V2 공통 기반 구현은 `hud/elements/v2/impl/HudElementImpl.kt`에 있다.
- 이 계층은 `HudLayoutSpec`을 받아 V1과 유사한 anchor/position/scale 계산을 수행한다.
- 각 구체 요소는 `resolveSize()`와 `render()`를 구현한다.

이 계층의 책임은 다음과 같다.

- screen anchor와 element anchor 기준 위치 계산
- scale, position, zIndex 적용
- draw 시점의 실제 크기 계산
- backend에서 동적 값을 읽어 렌더링

## 4. Scene 계층
V2 scene은 상속 기반이 아니라 builder/factory 기반이다.

- `hudScene { ... }`
- `HudScene.element { ... }`
- `onEnable { ... }`
- `onDisable { ... }`

현재 scene의 실제 역할은 다음과 같다.

- 여러 `HudElementSpec`을 수집한다.
- 필요할 때 runtime element list를 생성한다.
- scene enable/disable 콜백을 관리한다.

## 5. 전이용 마운트 계층
현재 V2는 독립적인 scene entrypoint가 아니다.

- V2 `HudScene`은 `HudSceneAdapter`를 통해 V1 `hud.scenes.impl.HudScene`으로 감싸진다.
- 따라서 현재도 mounted scene 선택, state manager 연결, 실제 draw 진입은 기존 V1 파이프라인을 통한다.

이 계층은 전이용이다.

- 유지 목적:
  - V1 entrypoint를 바꾸지 않고 V2 scene를 실험/검증하기 위해
- 장기 목표:
  - V2가 직접 scene entrypoint가 되도록 교체

## 6. 런타임 데이터 계층
V2 요소는 동적 값을 spec에 저장하지 않는다.
동적 값은 런타임 backend가 제공하고, 요소가 draw 시점에 직접 읽는다.

현재 이 계층은 `client/rider/v2` 아래에 있다.

예:
- `KartGaugeTracker`
- `KartNitroCounter`
- `KartSpeedometer`
- `KartRaceTimer`
- `KartLapTracker`
- `KartTeamBoostTracker`
- `KartExpProgressReader`
- `KartRankingManager`

이 계층의 원칙은 다음과 같다.

- spec에는 동적 값이 들어가지 않는다.
- backend는 object-backed runtime 데이터 공급자로 동작한다.
- HUD 요소는 필요한 backend를 직접 참조한다.

## 7. Backend lifecycle 계층
현재 구현 기준으로 V2 backend는 단순 object 집합이 아니라,
`RiderBackendRegistry`를 중심으로 통합 관리되는 구조다.

- `RiderBackend`
  - `init()`
  - `onRaceStart()`
  - `onRaceEnd()`
  - `onRiderMount()`
  - `onRiderDismount()`
- `RiderBackendRegistry`
  - backend bootstrap
  - race 이벤트 분배
  - mount/dismount 이벤트 분배

이 계층에 대한 설계 판단은 확정되어 있다.

- `RiderBackendRegistry`는 임시 구조가 아니다.
- V2의 핵심 구조 중 하나로 유지한다.
- backend 객체의 초기화와 상태 관리를 진입점에서 통합적으로 수행하기 위해 필요하다.

즉, 현재의 V2는 "HUD spec + runtime renderer"만 있는 구조가 아니라,
그 아래에서 데이터를 공급하는 lifecycle-aware backend 계층도 함께 형성되고 있다.

## 8. Packet/event 입력 계층
현재 V2 backend 일부는 새 packet/event 계층에 의존한다.

- `event.v2.KartMountEvents`
- `event.v2.KartSummonEvents`
- `ClientPacketListenerMixin`

이 계층은 현재 다음 목적을 가진다.

- 카트 summon/remove 감지
- passenger packet 기반 mount/dismount 감지
- 첫 attribute update 시점의 늦은 mount 확정
- backend lifecycle 계층에 필요한 mount 관련 입력 제공

이 계층에 대한 설계 판단은 다음과 같다.

- 현재 2.0.0 준비 단계에서는 실질적으로 정식 구조처럼 사용한다.
- 그러나 장기적으로는 최종 구조가 아니다.
- 최종 목표는 SkidMC가 실전 사용 가능한 수준이 되었을 때, 이 계층을 SkidMC 기반 구현으로 치환하는 것이다.
- 따라서 현재 `event.v2`는 SkidMC 개발 중 코드를 최소 수정으로 이식한 임시 브리지 계층이다.

즉, 현 시점에서는 V2 runtime data 계층의 일부로 취급하되,
장기적으로는 별도 라이브러리 기반 구현으로 교체될 것을 전제로 설계한다.

## 현재 구현 원칙

### 1. 동적 값은 spec에 넣지 않는다
예를 들어 다음 값들은 spec에 저장하지 않는다.

- 게이지 현재값
- 니트로 개수
- 현재 속도
- 현재 랩/타이머 값
- 순위표 스냅샷

이 값은 runtime backend가 가지고, 각 HUD 요소가 직접 읽는다.

### 2. binding은 아직 하드코딩한다
현재는 범용 binding 시스템을 도입하지 않았다.

예:
- `GradientGaugeBar` -> `KartGaugeTracker`
- `PlainNitroSlot` -> `KartNitroCounter`
- `PlainRankingTable` -> `KartRankingManager`
- `JiuTachometer` -> `KartSpeedometer`
- `HudTimer` -> `KartRaceTimer`, `KartLapTracker`

이 결정을 유지하는 이유는 다음과 같다.

- type-safe generic binding 설계가 아직 검증되지 않았다.
- serialization format과의 호환성 문제가 있다.
- binding ID rename/missing 문제를 아직 해결하지 않았다.
- editor UX와 validation 문제를 함께 설계해야 한다.

추가로 현재 판단은 다음과 같다.

- 범용 binding 시스템 일반화는 SkidMC 도입 이후에 다시 검토한다.
- 현재 단계에서는 요소 포팅과 backend 정리가 우선이다.

### 3. V2는 아직 V1과 공존한다
현재 V2에서 state 기반 HUD를 적극적으로 사용하지는 않지만,
전이 계층 때문에 다음 V1 인프라는 여전히 남아 있다.

- V1 scene mount 경로
- `HudStateManager`
- 기존 race/session/event 시스템 일부

즉, "V2는 state를 직접 중심 모델로 삼지 않는다"가 정확한 표현이지,
"현재 코드에서 V1 state 관련 코드가 완전히 사라졌다"는 뜻은 아니다.

## 현재 존재하는 V2 요소
현재 실제 V2 요소로 포팅된 것은 다음과 같다.

- `GradientGaugeBar`
- `PlainNitroSlot`
- `PlainRankingTable`
- `JiuTachometer`
- `HudTimer`

추가로 샘플/디버그 요소인 `SomeElement`가 있다.

## 테스트/검증 방식
현재는 dev mode에서 mounted HUD scene를 V2 샘플 scene로 치환해 실제 화면 출력 여부를 확인한다.

- 위치:
  - `hud/scenes/SceneSelection.kt`
- 조건:
  - `DynRiderJvmFlags.devMode == true`
- JVM property:
  - `-Doni0nfr1.dynrider.dev=true`

## 아직 미정이거나 남아 있는 문제
- V2 전용 scene entrypoint로 완전히 전환할지
- `event.v2` 계층을 장기적으로 어디에 둘지
- generic binding system을 SkidMC 도입 이후 어떤 형태로 도입할지
- ranking table 등 대형 요소를 어떤 순서로 포팅할지

## 요약
현재 V2 아키텍처는 다음 흐름으로 보는 것이 가장 정확하다.

1. DSL이 spec을 만든다.
2. spec이 runtime HUD element를 만든다.
3. runtime HUD element는 draw 시점에 backend 값을 읽는다.
4. backend는 기존 event 계층과 새 `event.v2` 계층에서 데이터를 받는다.
5. scene는 아직 `HudSceneAdapter`를 통해 V1 파이프라인으로 마운트된다.

즉, 현재의 V2는 "직렬화 가능한 HUD 정의 계층"과 "object-backed runtime data 계층"을 동시에 정리해 가는 전이 단계다.

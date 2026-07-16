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

## 이번 버전 릴리즈 범위

- [x] 편집 모드와 실제 HUD 확인용 미리보기 모드를 수동 검증하고 현재 GUI 변경을 확정한다.
- [x] `PreviewKartState` 값을 조절하고 preset을 적용할 수 있는 preview 상태 UI를 완성한다.
- [x] inspector, repository/session 저장 정책 및 모든 preview factory/preset의 핵심 회귀 테스트를 보강한다.
- [x] 신규 요소의 색상 변경에 사용할 texture hue shift shader와 GUI RenderType을 제공한다.
- [x] 단일 요소 Spec 내 sealed style property의 subtype 선택·편집·검증을 지원한다.
- [ ] 신규 HUD 요소 1개를 추가하고 metadata, 번역, 기본 Spec 및 상태 호환성 검사를 통과시킨다.

일반 중첩 object/list, 최상위 요소 Spec 다형성, compound child 편집과 KSP registry 생성 및 프레임 상태 snapshot은 이번 릴리즈 범위에서 제외한다.

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
- [x] repository resolve 결과를 runtime `HudScene`이 아닌 최종 `HudSceneSpec`, 원본 경로, 출처와 진단으로 분리한다.
- [x] live lifecycle이 repository의 Spec resolve 결과를 별도 `HudSceneLoader`로 runtime 장면에 투영하도록 연결한다.
- [x] `HudSceneSpec`과 `HudSceneMode`를 loader 전용 패키지에서 중립적인 `hud.scene.model`로 이동한다.
- [x] 커스텀 저장 및 삭제 API를 제공한다.
- [x] 장면 생성 실패 시 게임을 중단하지 않고 빈 장면으로 복구한다.
- [x] 현재 ride/spectate 엔진 분기를 `KartStateType` 기반 조회로 교체한다.
- [x] 저장·삭제·리소스 reload와 현재 live HUD의 재생성을 분리한다.
  - 편집 중 변경은 preview 장면에만 즉시 반영한다.
  - custom 저장·삭제와 리소스 reload는 repository 상태를 갱신하고 이후 생성되는 HUD부터 적용한다.
  - 현재 live HUD 갱신은 자동으로 수행하지 않으며, 필요해지면 명시적인 apply 기능으로 별도 구현한다.

## 3. 편집 문서와 레이아웃

- [x] 각 요소의 영속 ID를 JSON에 선택적으로 저장한다.
- [x] 기존 JSON에 ID가 없으면 결정적인 ID를 생성하는 `HudSceneDocument`를 제공한다.
- [x] 추가·삭제·재정렬·spec 교체를 undo/redo command로 제공한다.
- [x] command 실행·undo·redo 결과를 정규화한 `HudDocumentChange`로 발행하고 clean snapshot 기준 dirty 상태를 제공한다.
- [x] anchor·scale 좌표 계산과 hit-test bounds를 `HudLayoutEngine`으로 분리한다.
- [x] 검증 없는 Kotlin DSL용 `HudScene.addSpec`을 제거하고 호환성·유효성을 검사하는 ID 기반 mutation API로 통합한다.
- [x] 런타임 `HudScene`이 document 변경을 감지하고 변경된 요소만 재생성하도록 연결한다.
  - [x] command stack이 document 변경을 element ID와 변경 종류가 포함된 event로 노출한다.
  - [x] element ID와 runtime element의 대응을 유지한다.
  - [x] 추가·삭제·재정렬은 scene의 runtime element 목록에 반영한다.
  - [x] spec 교체 시 context는 유지하고 해당 runtime element만 재생성한다.
- [x] preview 선택 및 hit-test에 사용할 element ID별 runtime bounds 조회를 제공한다.
- [ ] compound element의 자식에도 영속 ID와 편집 가능한 bounds를 제공한다.

## 4. Annotation 기반 요소 메타데이터와 Preview 데이터

요소별 descriptor 구현은 만들지 않는다. `Spec`에 선언된 annotation과 `kotlinx.serialization`의 `SerialDescriptor`를 읽어 요소 정보, 속성 편집기 및 validation 규칙을 자동 생성한다.

- [x] `@SerialInfo` 기반의 `@HudElementInfo`, `@HudProperty`, `@HudRange`, `@HudColor`, `@HudHidden`, `@HudLayout` annotation을 정의한다.
- [x] 클래스의 `@HudElementInfo`에서 표시 이름 번역 key, 카테고리 및 선택적 아이콘 정보를 읽는다.
- [x] property의 직렬화 타입과 annotation을 조합해 Boolean toggle, 숫자 입력, slider, enum selector, 문자열 입력, color picker 및 anchor selector를 자동 선택한다.
- [x] 타입만으로 편집 방식을 결정할 수 있는 property에는 annotation을 요구하지 않고, 표시 이름·범위·색상 등 추가 정보가 필요할 때만 annotation을 사용한다.
- [x] 모든 등록 요소 Spec에 element/layout/color 및 필요한 range metadata와 `en_us`·`ko_kr` 번역을 제공한다.
- [x] serializer, type ID, 상태 타입 호환성 및 runtime factory를 중앙 type registry에 등록하되 속성별 descriptor 코드는 작성하지 않는다.
- [x] 현재 spec을 `JsonElement`로 encode하고 변경된 property만 교체한 뒤 같은 serializer로 decode하여 immutable spec을 갱신한다.
- [x] registry metadata와 현재 Spec 값을 결합한 GUI용 읽기 모델을 제공한다.
  - `HudElementEditorModel`은 element ID, type ID, 요소 이름·카테고리 번역 key, icon 및 편집 property 목록을 포함한다.
  - `HudEditableProperty`는 안정적인 `HudPropertyPath`, 이름·설명 번역 key, `HudPropertyEditorType`, 현재 `JsonElement` 값과 optional·nullable 정보를 포함한다.
  - `HudElementTypeRegistry.bySpec()`으로 serializer와 metadata를 찾고 현재 Spec을 `JsonObject`로 encode해 각 metadata의 `serialName`과 현재 값을 결합한다.
  - `@HudHidden` property는 속성 패널에서 제외하고 `Unsupported` property는 기본 GUI에서 비활성 또는 미지원 상태로 구별한다.
  - 미등록 Spec, 존재하지 않는 element ID 및 encode 실패는 구조화된 inspector 결과로 반환한다.
- [x] `HudEditorSession`에 GUI용 읽기 API를 추가한다.
  - `inspectElement(elementId)`는 GUI가 registry나 serializer를 직접 참조하지 않고 선택 요소의 편집 모델을 읽게 한다.
  - `availableElementTypes()`는 현재 `KartStateType`과 호환되는 요소만 팔레트 모델로 반환하며 기본 Spec 생성 정보도 세션 내부에 둔다.
  - `HudEditorState` 변경을 받은 GUI는 선택 ID로 inspector를 다시 조회해 현재 property 값을 갱신한다.
  - property 수정은 기존 `updateProperty(elementId, path, value)`만 사용해 읽기 모델과 쓰기 경로를 분리한다.
- [x] generic property 변경 결과는 `ReplaceElementSpecCommand`로 document에 적용한다.
  - 변경 실패 시 document와 undo/redo stack을 수정하지 않는다.
  - 변경 성공 시 element ID를 유지하며 새 spec으로 교체한다.
  - 같은 요소의 같은 property에 대한 연속 변경을 이후 command 병합에 사용할 수 있도록 경로를 보존한다.
- [x] 모든 top-level 및 compound child spec에 기본값을 제공해 type discriminator만으로 기본 요소를 생성할 수 있게 한다.
- [x] `@HudRange` 등의 metadata를 GUI 입력 제한과 JSON load validation에서 공통으로 사용한다.
- [x] 범위를 벗어난 외부 JSON 값은 자동 보정하지 않고 경로가 포함된 validation 오류로 반환한다.
  - `HudSpecValidator`는 registry의 serializer와 metadata를 입력으로 받아 전체 spec을 검사한다.
  - `HudSpecValidator.validateScene()`은 파일 경로와 무관한 element ID·index·상태 호환성·개별 Spec 오류를 반환한다.
  - repository와 loader는 중립적인 장면 validation 오류에 원본 경로를 결합해 `HudSceneLoadError`로 변환한다.
  - 유한값과 range 등의 의미 오류는 validation 결과로 반환하고, enum·color 형식 등의 구조 오류는 serializer가 검사해 호출자가 편집 또는 decode 오류로 변환한다.
  - 오류에는 element ID와 `property.path`를 포함할 수 있도록 구조화된 경로를 사용한다.
  - generic property 편집기와 resource/config JSON loader가 같은 validator를 사용한다.
- [x] 초기에는 type/serializer 등록을 중앙에서 명시적으로 관리하고, 요소 수 증가로 등록 비용이 커질 때 KSP 기반 registry 생성을 검토한다.
- [x] legacy HUD/state 코드를 신규 계약으로 이관하거나 제거한다.

## 5. 런타임·프리뷰 상태 계층

HUD 요소가 Skid API나 `KartRef`를 직접 읽지 않도록 상태 공급과 표시 효과를 분리한다. `KartState`는 Skid API 또는 편집기 프리뷰 상태를 HUD가 이해할 수 있는 원본 값으로 변환하는 어댑터이고, 기존 `Speedometer`, `GaugeBar`, `NitroSlot` 등의 delegate는 그 값을 보간·지연·클램핑하는 표시 효과 계층으로 유지한다.

```text
Skid API ───── LiveKartState ───┐
                                ├─ Speedometer / GaugeBar / NitroSlot ── HUD Element
편집기 ─── PreviewKartState ────┘
```

- [x] 공통 카트 값을 제공하는 읽기 전용 `KartState` 인터페이스를 정의한다.
- [x] HUD 코어의 엔진 구별 기준으로 안정적인 ID와 상태 클래스를 가진 `KartStateType<S>`를 정의하고, `KartEngine` 타입은 Skid 연결 계층 밖으로 노출하지 않는다.
- [x] 엔진 고유 값은 `JiuKartState`, `ChargeKartState`, `V1KartState`처럼 공통 상태를 확장한 엔진별 인터페이스로 분리한다.
- [x] `KartRef.Specific<E>`와 Skid API를 감싸는 엔진별 `LiveKartState` 구현을 제공한다.
- [x] 모든 `KartStateType`에 대응해 값을 자유롭게 변경할 수 있는 엔진별 `PreviewKartState` 구현을 제공한다.
- [x] `KartStateType`에 맞는 기본 가변 상태와 `PreviewHudSceneContext`를 조립하는 preview factory를 제공한다.
- [x] 대표 상태를 바로 재현할 수 있는 초기 preview preset을 제공한다.
- [x] `KartRef`와 Skid API에 대한 직접 접근은 `hud.runtime`의 live 연결 계층으로 제한한다.
- [x] 기존 `Speedometer`, `GaugeBar`, `NitroSlot` 구현이 `KartRef` 대신 호환되는 `KartState`를 입력으로 받도록 변경한다.
- [x] raw, interpolation, trailing 등 기존 delegate 구현의 다형성과 효과를 유지한다.
- [x] `KartState`는 보간되지 않은 의미상의 원본 값을 제공하고, 시간에 따른 보간·지연·잔상은 delegate가 담당하도록 경계를 정한다.
- [x] `KartState`는 장면 전체에서 공유하고, 가변 애니메이션 상태를 가진 효과 delegate는 기본적으로 요소별로 생성한다.
- [x] 경기 시간·랩처럼 카트 엔진 외부의 진행 값은 `RaceState`, 참가자·순위·로컬 플레이어 값은 `RankingState`라는 별도 상태 어댑터로 분리한다.
- [x] 랭킹을 제공하지 않는 타임어택과 아직 참가자가 없는 랭킹을 구분할 수 있도록 `RankingState.Unavailable`과 사용 가능한 랭킹 상태를 명시적으로 모델링한다.
- [x] `KartState`, `RaceState`, `RankingState` 및 애니메이션 시계를 읽기 전용 속성으로 묶는 `HudSceneContext<S>`를 정의한다.
- [x] 모든 `HudElementSpec.create`와 요소 생성자는 상태별로 다른 인자를 받지 않고 `HudSceneContext<S>`와 `ElementHolder`를 받는 공통 생성 규약을 사용한다.
- [x] 요소는 공통 context에서 필요한 상태를 골라 사용하고, `Speedometer`, `GaugeBar`, `NitroSlot` 등의 효과 delegate에는 전체 context가 아닌 필요한 상태 인터페이스만 전달한다.
- [x] `HudSceneContext`에는 임의 타입 조회, backend 객체 및 상태 변경 명령을 추가하지 않아 service locator로 확장되지 않게 한다.
- [x] 실제 게임용 `LiveHudSceneContext`와 편집기용 `PreviewHudSceneContext`가 동일한 요소 생성 및 렌더링 경로를 사용하도록 한다.
- [x] 프리뷰 애니메이션의 일시 정지·재시작·시간 이동을 지원할 수 있도록 `HudClock`을 상태 공급원과 분리한다.
- [x] 프리뷰 값은 장면 JSON에 저장하지 않고 편집 세션 상태로 관리한다. 필요하면 별도의 편집기 설정에 마지막 사용값만 저장한다.
- [ ] 초기 구현은 기존 getter 기반 갱신 방식을 사용할 수 있으며, 일관성이나 접근 비용 문제가 확인되면 프레임 단위 불변 상태 snapshot을 도입한다.

패키지와 의존 방향은 다음과 같이 고정한다.

```text
hud/state                 외부 API를 모르는 읽기 전용 상태 계약
hud/scene                 HudSceneContext와 장면 수명주기 계약
hud/runtime/state         Skid API와 rider backend를 읽는 live adapter
hud/editor/preview        편집 가능한 preview 상태와 preset
hud/elements/**/bridge    상태값에 표시 효과를 적용하는 기존 delegate
```

- [x] `hud.state`는 Skid API, rider backend 구현, Minecraft singleton 및 편집기 구현을 참조하지 않는다.
- [x] `HudSceneContext`는 장면의 실행 환경이므로 `hud.scene`에 둔다.
- [x] `KartEngine`과 `KartRef` 양쪽을 아는 live factory와 registry는 `hud.runtime`에 격리한다.
- [x] 실제 상태 adapter는 `hud.runtime.state`, 가변 preview 구현은 `hud.editor.preview`에 둔다.
- [x] preview preset은 `hud.editor.preview`에 둔다.
- [x] 기존 효과 delegate의 패키지는 우선 유지하고 역할 변경과 무관한 이름 변경은 별도 작업으로 미룬다.
- [x] 의존성은 `hud.state`와 `hud.scene`의 계약을 runtime·preview·elements가 사용하는 방향으로만 흐르게 한다.

## 6. 인게임 GUI 편집기

- [x] repository, document, command stack, preview context와 preview scene을 묶는 편집 세션 모델을 제공한다.
  - 현재 mode, `KartStateType`, 장면 출처 및 dirty 상태를 소유한다.
  - resource 장면은 원본으로 유지하고 첫 실제 변경 시 custom 작업 사본을 만든다.
  - GUI는 파일 경로와 fallback 규칙을 직접 다루지 않고 세션 API만 사용한다.
- [x] 리소스 장면은 메모리 작업 사본으로 편집하고 저장 시에만 config override를 생성한다.
- [x] 중앙 preview와 `요소`·`속성`·`프리뷰 상태` 탭을 가진 단일 side panel 화면 골격을 구현한다.
  - 요소 목록 선택, 호환 요소 palette, 추가·삭제·재정렬을 session API에 연결한다.
  - undo/redo, 저장 및 resource 복원 동작을 상단 도구 모음에 연결한다.
  - 요소 목록·palette·속성 목록은 독립적인 scroll 위치와 scrollbar를 제공한다.
  - side panel 구분선 drag로 너비를 조절하고 더블클릭으로 기본 너비를 복원하며 preview 최소 너비를 보장한다.
  - preview 영역이 부족할 때 편집 UI를 숨기고 실제 HUD만 확인하는 미리보기 모드를 제공한다.
- [x] boolean·string·number·range slider·enum·color 입력 widget과 property별 validation 오류 표시를 구현한다.
  - text 기반 입력은 확인 시점에만 적용하고 range slider는 drag release 시 한 번만 command를 생성한다.
  - 긴 property 목록과 widget scroll을 수동 검증할 수 있는 editor stress-test 요소를 registry에 제공한다.
- [x] `HudLayoutSpec` 전용 layout 입력 widget을 구현한다.
  - 일반 중첩 object 편집으로 확장하지 않고 속성 탭 내부의 전용 하위 화면에서 anchor·scale·offset·zIndex를 편집한다.
  - 하위 필드 변경은 현재 layout JSON에서 해당 값만 교체한 새 `JsonObject`를 top-level `layout` 변경으로 적용한다.
- [x] preview 캔버스 선택·이동을 구현한다.
  - 선택 요소의 bounds, 화면 anchor, 요소 anchor와 두 anchor 사이 offset을 overlay로 표시한다.
  - 선택 bounds의 단일 모서리 handle로 `scaleX`와 `scaleY`를 같은 값으로 조절한다.
- [x] undo/redo, 저장, 커스텀 삭제 및 리소스 기본값 복원을 세션 API로 제공한다.
- [x] drag 중 명령을 병합하고 anchor 기준 좌표로 역변환한다.
- [x] 저장 또는 삭제 후 현재 live HUD를 자동 갱신하지 않고 이후 생성되는 HUD부터 최신 설정을 사용한다.
- [x] 속성 및 layout 편집 행의 왼쪽 label을 입력 widget의 세로 중앙에 정렬한다.
  - validation 오류가 나타나도 label과 입력 widget의 기준선은 움직이지 않게 한다.
- [x] dirty 상태에서 editor 종료 또는 장면 전환 시 저장 확인 절차를 제공한다.
  - `저장 후 나가기/전환`, `저장하지 않고 나가기/전환`, `취소`를 구별한다.
  - 저장 실패 시 editor와 현재 session을 유지하고 오류를 표시한다.
  - Done 버튼과 ESC 종료에 같은 정책을 적용한다.
- [x] 별도 launcher 화면을 제거하고 설정 화면에서 editor를 직접 연다.
  - 마지막으로 편집한 mode와 `KartStateType`을 editor UI 설정으로 기억하고, 기록이 없으면 `RIDE / JIU`를 사용한다.
  - 상단에 현재 `HudSceneMode / KartStateType`과 장면 변경 버튼을 표시한다.
  - 장면 변경 UI는 mode 선택과 scroll 가능한 전체 엔진 목록을 제공한다.
  - dirty session에서 장면을 변경하면 종료와 같은 저장 확인 절차를 거친다.
- [x] 실제 live HUD 화면 구성을 확인하는 미리보기 모드를 구현한다.
  - 편집 모드는 기존처럼 side panel 옆 preview pane을 논리 viewport로 사용해 1:1로 렌더링한다.
  - 미리보기 모드는 toolbar, side panel과 편집 overlay를 숨기고 논리 viewport를 현재 게임 GUI 전체 해상도로 전환한다.
  - 모드 전환 중 현재 선택·dirty·history·panel 상태를 보존한다.
  - 미리보기 중앙에 반투명 ESC 복귀 안내를 표시하고 ESC로 편집 모드에 복귀할 수 있다.
  - 렌더링, scissor, overlay와 mouse 조작은 하나의 `HudPreviewTransform`을 공유한다.
  - hit-test와 이동·배율 drag는 화면 좌표를 논리 좌표로 역변환한 뒤 처리한다.
  - bounds와 anchor 위치는 논리 좌표에서 계산하고 handle 및 선 두께는 화면상 일정한 크기로 표시한다.
- [ ] 기본 편집기 완성 후 중첩 object와 list property의 재귀 metadata 및 편집 UI를 추가한다.

초기 GUI 구현 순서는 요소 목록과 선택, 요소 팔레트, primitive·enum·color 속성 패널,
layout·anchor 편집, 프리뷰 캔버스 drag, undo/redo, 저장·삭제·복원 순으로 한다.
validation 실패는 해당 property 경로와 함께 속성 패널에 표시한다.

## 7. 이후 구현 우선순위

1. [x] 공통 `HudSpecValidator`와 구조화된 validation 오류를 구현하고 JSON loader와 generic property 편집기에 적용한다.
2. [x] generic property 변경 성공 결과를 `ReplaceElementSpecCommand`와 `HudCommandStack`에 연결한다.
3. [x] `HudSceneDocument` 변경을 preview `HudScene`에 동기화하고 element ID 단위 runtime 재생성을 구현한다.
4. [x] repository, document, command stack과 preview를 묶는 편집 세션 모델을 구현한다.
   - [x] document, command stack, property editor와 preview synchronizer를 소유하는 core session 및 GUI 상태·결과 모델을 구현한다.
   - [x] registry metadata와 현재 Spec 값을 결합하는 element inspector 및 호환 요소 palette 조회 API를 session에 추가한다.
   - [x] repository의 spec resolve 결과로 session을 여는 공개 `open()`을 연결하고 저수준 session 조립 함수는 비공개로 둔다.
5. [x] 편집 세션에 custom 저장·삭제 및 resource 기본값 복원 흐름을 구현한다.
6. [x] 기본 인게임 GUI를 요소 선택부터 저장·복원까지 순차적으로 구현한다.
   - [x] 편집 mode·상태 타입 선택 화면과 탭식 editor 화면 골격을 구현한다.
   - [x] 요소 목록·palette·구조 변경과 undo/redo·저장·복원 UI를 session에 연결한다.
   - [x] 탭별 side panel scroll과 drag 기반 panel 너비 조절을 구현한다.
   - [x] inspector metadata 기반 primitive·enum·color property 입력 widget을 구현한다.
   - [x] layout property 입력 widget을 구현한다.
   - [x] 속성 행 정렬, dirty 종료 확인과 editor 내부 장면 전환을 구현한다.
   - [x] 편집 UI를 숨기고 전체 GUI 크기의 HUD를 표시하는 미리보기 모드를 구현한다.
   - [x] preview 상태 조절 UI를 구현한다.
     - 현재 `PreviewKartState` capability에 맞는 카트 값과 공통 레이스 값만 노출한다.
     - 호환 preset은 scroll 가능한 dropdown으로 선택하고 명시적인 적용 버튼으로 반영한다.
       - [x] HUD 외 GUI에서도 재사용할 수 있는 `AbstractWidget` 기반 `DropdownWidget`을 공용 widget 패키지에 제공하고 preset 선택에 적용한다.
     - 호환 preset은 기본 preview 값으로 초기화한 뒤 적용해 이전 수동 값이 섞이지 않게 한다.
     - preview 상태 변경은 장면 Spec, dirty와 undo/redo history를 변경하지 않는다.
7. [ ] 기본 GUI가 완성된 뒤 compound child와 중첩 object/list spec의 재귀 편집을 구현한다.
8. [x] 신규 부스터 슬롯의 선행 작업으로 sealed interface style Spec의 다형성 편집을 지원한다.
   - `PolymorphicKind.SEALED`에서 등록된 하위 타입과 현재 type discriminator를 inspector metadata로 노출한다.
   - GUI에서 하위 타입을 선택하고, 선택된 타입의 프로퍼티를 재귀적으로 편집한다.
   - subtype 교체와 중첩 프로퍼티 변경을 기존 immutable JSON round-trip과 command/undo/redo 흐름으로 처리한다.
   - `HudSpecValidator`가 실제 하위 타입 descriptor를 따라가 범위·유한값 등의 중첩 validation을 수행하게 한다.
   - 이번 범위는 구체 요소 Spec의 sealed style property에 한정하고, 최상위 `HudElementSpec` 다형성과 일반 list/compound 편집은 후속 작업으로 남긴다.

프레임 단위 상태 snapshot, KSP registry 생성 및 runtime element의 세부 property patch는
정확성 또는 성능 문제가 확인되기 전까지 후순위로 둔다.

## 검증 기준

- [x] client source output을 사용하는 JUnit 테스트 소스셋을 구성하고 `test`와 `build`에 연결한다.
- [x] generic property 변경, registry/metadata 번역, built-in JSON, document command, layout 및 custom 저장·삭제의 기본 회귀 테스트를 제공한다.
- [x] 공통 validator의 전체 spec 검사와 구조화된 오류 경로를 테스트한다.
- [x] 장면 전체 validator가 element ID를 보존한 Spec 오류와 상태 비호환을 loader 문맥 없이 반환하는지 테스트한다.
- [x] property 편집부터 command 실행 및 undo/redo까지의 통합 흐름을 테스트한다.
- [x] document 변경에 따른 preview runtime element 재생성을 테스트한다.
- [x] repository가 유효한 custom Spec을 runtime 생성 없이 resolve하고 잘못된 custom을 보존하며 진단하는지 테스트한다.
- [x] inspector가 metadata와 현재 Spec 값을 같은 property path로 결합하고 hidden·unsupported·오류 상태를 구별하는지 테스트한다.
- [x] palette 조회가 현재 `KartStateType`과 호환되는 요소만 제공하고 type ID 추가가 세션 내부에서 기본 Spec을 생성하는지 테스트한다.
- [x] resource 작업 사본의 custom 저장, 삭제 확인 및 resource 기본값 복원을 편집 세션 수준에서 테스트한다.
- [x] 저장·삭제 후 repository와 편집 세션 상태가 갱신되고 현재 live HUD에는 자동 적용되지 않는지 테스트한다.
- [x] 모든 preview factory와 preset이 대응하는 `KartStateType`에서 동작하는지 테스트한다.
- [x] preview 표시 좌표의 logical-screen 왕복, 표시 영역 전환 및 drag 역변환을 테스트한다.

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
- 요소와 property의 표시 metadata에는 번역 문자열이 아닌 자동 생성 또는 명시적으로 override한 i18n key를 저장한다.
- 초기 속성 편집은 primitive, enum 및 layout에 집중하고 중첩 object/list 편집은 기본 GUI 완성 뒤로 미룬다.
- 초기 구현은 spec 변경 시 요소를 재생성하고, 세부 runtime patch는 성능 문제가 확인된 뒤 추가한다.
- `KartState`는 기존 효과 delegate를 대체하지 않고 외부 상태 공급원을 추상화하는 입력 계층으로 사용한다.
- Skid API 및 프리뷰 구현의 차이는 상태 어댑터에서 끝내고, 이후의 효과 처리와 요소 렌더링 경로는 공유한다.
- 카트 엔진에 속하지 않는 경기·순위·시간 상태는 `KartState`에 포함하지 않는다.
- `RaceState`와 `RankingState`는 별도의 기능 계약으로 유지하되 `HudSceneContext`가 함께 소유한다.
- 요소 생성 API는 `HudSceneContext + ElementHolder`로 통일하고, 세부 의존성은 요소가 생성하는 delegate 경계에서 좁힌다.

# Datapack Component 구상

## 문서 상태

이 문서는 HUD 요소에 부착할 수 있는 데이터 기반 컴포넌트와 클라이언트 명령 인터프리터에 대한 초기 구상을 기록한다. 아직 구체적인 직렬화 형식, 실행 규칙 및 API를 확정하지 않으며, HUD Hierarchy와 Inspector의 현재 리팩토링 범위를 불필요하게 ECS화하지 않는다.

> [!IMPORTANT]
> 이 문서의 구조와 코드 예시는 확정된 설계가 아니다. 실제 구현과 검증 과정에서 개념, 책임 경계, 명칭, 데이터 형식 및 API가 대폭 수정되거나 새로운 계층이 추가될 수 있다. 현재 내용 사이의 호환성이나 향후 구현과의 일치를 전제하지 않는다.

첫 번째 컴포넌트 구현을 통해 실제 요구를 확인한 뒤 이 문서의 개념을 구체화한다.

## 목표

- HUD 요소에 제한적인 컴포넌트를 부착할 수 있게 한다.
- Fabric client command를 진입점으로 사용하는 데이터팩 유사 인터프리터를 제공한다.
- Kart, Race 및 Ranking 상태에 따라 요소의 표시, 애니메이션과 장면 동작을 런타임에 변경할 수 있게 한다.
- 동일한 실행 구조를 라이브 HUD와 에디터 프리뷰에서 사용한다.
- 컴포넌트 설정을 HUD Inspector에서 편집할 수 있게 한다.

## 우선 도입 범위

초기 구현에서는 기존 `HudElementSpec`을 컴포넌트의 소유자로 취급한다. 컴포넌트는 Hierarchy의 별도 자식 노드가 아니라 선택한 요소의 Inspector 섹션으로 표시한다.

```text
Hierarchy
└─ Nitro Slot
   Inspector
   ├─ Element Properties
   │  ├─ Layout
   │  └─ Style
   └─ Datapack Components
      ├─ Visibility Condition
      ├─ State Binding
      └─ Animation Trigger
```

범용 Entity 또는 요소와 무관한 singleton Entity는 초기 범위에 포함하지 않는다.

## 영속 문서 모델

컴포넌트를 모든 `HudElementSpec` 구현의 공통 프로퍼티로 반복해서 선언하지 않는다. 대신 장면의 document entry가 요소 Spec과 부착된 컴포넌트를 함께 소유하는 구조를 고려한다.

```kotlin
data class HudDocumentElement(
    val id: String,
    val spec: HudElementSpec<*, *>,
    val components: List<HudDatapackComponentSpec> = emptyList(),
)
```

향후 저장 형식은 다음과 같은 entry 기반 구조로 마이그레이션할 수 있다.

```json
{
  "id": "element-1",
  "spec": {
    "type": "STYLED_NITRO_SLOT"
  },
  "components": [
    {
      "type": "STATE_VISIBILITY",
      "condition": "kart.isBoosting"
    }
  ]
}
```

기존 장면 JSON은 빈 컴포넌트 목록을 가진 것으로 읽어 하위 호환하는 방향을 고려한다.

## 컴포넌트 모델 초안

```kotlin
interface HudDatapackComponentSpec {
    fun create(context: HudComponentContext): HudDatapackComponent
}

interface HudDatapackComponent {
    fun update(commandBuffer: HudRuntimeCommandBuffer)
}
```

구체 컴포넌트 타입은 요소와 유사한 레지스트리를 통해 직렬화, 기본값 생성 및 편집 메타데이터를 제공할 수 있다.

```kotlin
data class HudComponentType<SPEC : HudDatapackComponentSpec>(
    val typeId: String,
    val serializer: KSerializer<SPEC>,
    val metadata: HudObjectMetadata,
    val defaultFactory: () -> SPEC,
    val allowMultiple: Boolean,
)
```

HUD 요소 편집기에 도입할 `HudValueSchema`를 컴포넌트 프로퍼티에도 재사용한다. 이를 통해 nullable, sealed variant, 복합 객체, 컬렉션 및 사용자 정의 위젯을 같은 메타데이터 체계로 편집할 수 있게 한다.

## Inspector 통합

Inspector는 장기적으로 단일 프로퍼티 목록 대신 여러 섹션을 제공할 수 있어야 한다.

```kotlin
data class HudInspectorModel(
    val target: HudElementSelection,
    val sections: List<HudInspectorSection>,
)

sealed interface HudInspectorSection {
    data class ElementProperties(
        val properties: List<HudEditableProperty>,
    ) : HudInspectorSection

    data class Component(
        val componentId: String,
        val typeId: String,
        val nameKey: String,
        val properties: List<HudEditableProperty>,
    ) : HudInspectorSection
}
```

컴포넌트의 추가, 삭제 및 설정 변경은 영속 문서 편집이므로 에디터의 undo/redo와 dirty 상태에 포함한다.

```text
AddComponentCommand
RemoveComponentCommand
ReplaceComponentSpecCommand
```

## 편집 Command와 런타임 Command

에디터가 문서를 변경하는 Command와 컴포넌트가 실행 중 발생시키는 Command를 분리한다.

- `HudEditCommand`: 영속 Spec을 변경하고 undo/redo 및 dirty 판정에 참여한다.
- `HudRuntimeCommand`: 현재 실행 중인 장면에만 적용되며 영속 문서를 변경하지 않는다.

런타임 구조 변경은 실행 도중 즉시 적용하기보다 command buffer를 통해 안전한 시점에 반영하는 방향을 고려한다.

```kotlin
interface HudRuntimeCommandBuffer {
    fun setComponent(/* ... */)
    fun removeComponent(/* ... */)
    fun setVisible(/* ... */)
    fun switchScene(/* ... */)
}
```

## 영속 상태와 런타임 상태

상태에 따른 동적 동작은 저장된 Spec을 직접 변경하지 않는다.

```text
Authored Scene Spec
        +
Runtime Overrides
        =
Rendered Scene State
```

따라서 다음 동작을 구분한다.

- 문서 또는 컴포넌트 설정 편집: dirty 발생
- Preview State 변경: dirty 변화 없음
- 인터프리터 실행 결과 적용: dirty 변화 없음
- 런타임 결과를 명시적으로 문서에 반영: dirty 발생

## 인터프리터 방향

Fabric client command는 사용자 진입점 또는 디버깅 인터페이스로 사용할 수 있다. 데이터 리소스의 실행 모델 자체는 명령 문자열을 매번 직접 해석하지 않고, 로드 시 검증된 중간 표현으로 변환하는 방향을 고려한다.

```text
Resource Script
→ Parse
→ Validate
→ Runtime Command IR
→ HudRuntimeCommandBuffer
```

추후 다음 항목을 정의해야 한다.

- 접근할 수 있는 상태 및 컴포넌트 범위
- 존재하지 않는 요소 참조의 처리
- 실행 횟수, 재귀 및 반복 제한
- 클라이언트 스레드에서의 적용 시점
- 리소스 리로드 시 재컴파일 및 오류 보고
- 에디터 프리뷰에서의 실행, 일시 정지 및 진단 방식

## Singleton Entity 도입 판단

요소와 무관한 singleton Entity는 첫 번째 요소 부착 컴포넌트를 구현한 뒤 필요성을 다시 판단한다. 현재로서는 도입 가능성이 낮다.

다음 요구가 실제로 반복될 때 도입을 검토한다.

- 특정 시각 요소에 속하지 않는 장면 전역 타이머
- 여러 요소가 공유하는 변수 또는 상태 머신
- 장면 전체 요소를 동시에 제어하는 로직
- 요소가 없어도 실행되어야 하는 이벤트 처리
- 실행을 위해 의미 없는 투명 요소를 만들어야 하는 상황

이런 요구가 없다면 요소에 부착되는 제한적인 컴포넌트 모델만 유지한다.

## 현재 에디터 설계에 미치는 영향

현재 Hierarchy 및 Inspector 리팩토링에서는 다음 경계만 유지한다.

- Hierarchy는 계속 HUD 요소 중심으로 설계한다.
- Inspector가 향후 여러 섹션을 제공할 수 있는 여지를 남긴다.
- 프로퍼티 메타데이터는 요소와 컴포넌트가 함께 사용할 수 있게 설계한다.
- 컴포넌트 영속 데이터는 개별 요소 Spec 내부가 아닌 scene entry에 부착하는 방향을 유지한다.
- 영속 Edit Command와 일시적 Runtime Command를 혼합하지 않는다.
- 범용 Entity 선택 모델과 singleton Entity는 실제 필요가 확인될 때 도입한다.

## 미확정 사항

- 컴포넌트의 정확한 직렬화 형식과 버전 마이그레이션
- 한 요소에 같은 컴포넌트를 여러 개 부착할 수 있는 조건
- 컴포넌트 실행 순서와 의존성
- 런타임 이벤트 및 lifecycle API
- 인터프리터 문법과 중간 표현
- 런타임 override의 저장 및 디버깅 방식
- 에디터에서 컴포넌트 실행 상태를 시각화하는 방법

위 사항은 첫 컴포넌트 구현에서 얻은 요구를 바탕으로 구체화한다.

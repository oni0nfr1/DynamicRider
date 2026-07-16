# HUD Scene Editor 시나리오

## 목적

인게임 GUI는 HUD loader, repository, document 및 runtime scene을 직접 조작하지 않는다.
GUI는 `HudEditorSession`을 단일 진입점으로 사용하고, 세션이 편집 명령 실행과 preview 및 live HUD 갱신을 조율한다.

```text
GUI
 │ 사용자 입력 / 편집 요청
 ▼
HudEditorSession
 ├─ HudSceneRepository
 ├─ HudSceneDocument
 ├─ HudCommandStack
 ├─ HudSpecPropertyEditor
 ├─ PreviewHudSceneContext
 └─ PreviewHudSceneSynchronizer
        │
        ├─ Preview HudScene
        └─ Live HUD Lifecycle Controller
```

편집 상태의 단일 원천은 `HudSceneDocument`다. Preview scene은 document의 runtime 투영이며,
실제 파일과 live HUD는 저장·삭제 또는 resource reload 경계에서 갱신한다.

`Command`는 undo/redo를 위해 보관하는 사용자 작업 기록이고, `HudDocumentChange`는 command 실행 결과를
preview 등에 한 번 전달하는 단발성 이벤트다. `HudCommandStack`은 document와 history를 갱신한 뒤
정규화된 change를 발행한다.

## GUI와 세션의 계약

GUI는 세션에서 다음 상태를 읽는다.

- 현재 `HudSceneMode`와 `KartStateType`
- 장면 출처 `CUSTOM_CONFIG`, `RESOURCE` 또는 `CUSTOM_FALLBACK`
- 요소 ID와 Spec의 순서가 보존된 document snapshot
- 선택 요소의 현재 값과 registry metadata가 결합된 inspector model
- 현재 `KartStateType`과 호환되는 요소의 palette model
- 현재 선택된 element ID
- dirty, undo 가능 여부 및 redo 가능 여부
- property 경로가 포함된 validation 또는 저장 오류
- preview 렌더 결과와 hit-test에 사용할 runtime bounds

GUI는 세션에 다음 의도를 전달한다.

- 장면 열기 및 전환
- 요소 선택·추가·삭제·재정렬
- Spec property 변경
- 캔버스 drag 시작·갱신·종료
- undo·redo
- custom 저장·삭제 및 resource 기본값 복원
- preview 상태 및 preset 변경

GUI는 Spec, document collection, command stack 및 config 파일을 직접 수정하지 않는다.

## 1. 장면 열기와 전환

```text
설정 화면에서 editor 직접 열기
→ 마지막으로 편집한 mode와 KartStateType 또는 RIDE / JIU 기본값 선택
→ HudEditorSessionFactory.open()
→ HudSceneRepository.resolveSpec()
→ 선택된 HudSceneSpec으로 HudSceneDocument 생성
→ PreviewHudSceneContextFactory로 preview 상태 생성
→ document의 모든 요소로 Preview HudScene 생성
→ 요소 목록, 장면 출처 및 clean 상태를 GUI에 제공
```

손상된 custom config가 있으면 해당 파일은 보존한다. Repository가 resource 장면으로 fallback하면
세션은 `CUSTOM_FALLBACK` 출처와 진단을 GUI에 함께 제공한다.

별도 launcher 화면은 사용하지 않는다. Editor 상단에는 현재 `HudSceneMode / KartStateType`과
`장면 변경` 버튼을 표시한다. 버튼을 누르면 mode와 scroll 가능한 엔진 목록을 가진 임시 선택 UI를 열고,
확인한 경우에만 현재 session을 닫고 선택한 장면의 새 session을 연다. 선택을 취소하면 기존 session을 유지한다.

편집 세션에는 runtime `HudScene`이 아니라 `HudSceneSpec`, 출처와 진단을 반환하는 repository 조회 API가
필요하다. 기존 runtime resolve도 같은 spec 조회 결과를 사용해 장면을 생성하도록 공통화한다.

## 2. 요소 추가

```text
GUI가 현재 KartStateType과 호환되는 요소 팔레트 요청
→ session.availableElementTypes()
→ 사용자가 요소 타입 선택
→ session.addElement(typeId, index)
→ 세션 내부에서 HudElementTypeRegistry 조회 및 기본 Spec 생성
→ HudSpecValidator
→ AddElementCommand
→ HudCommandStack.execute()
→ HudSceneDocument 변경 event
→ Preview HudScene에 runtime 요소 추가
```

새 element ID는 세션 또는 document의 ID 정책에서 생성한다. GUI는 ID 생성 규칙을 소유하지 않는다.

## 3. 속성 편집

```text
GUI가 요소 선택 또는 HudEditorState 변경 수신
→ session.inspectElement(elementId)
→ 현재 Spec 값과 metadata가 결합된 HudElementEditorModel로 속성 패널 갱신
```

```text
GUI 속성 패널 입력
→ session.updateProperty(elementId, propertyPath, JsonElement)
→ document에서 현재 Spec 조회
→ HudSpecPropertyEditor.update()
→ serializer round-trip
→ HudSpecValidator
→ Success(newSpec)
→ ReplaceElementSpecCommand
→ HudCommandStack.execute()
→ document 변경 event
→ 해당 preview runtime 요소 재생성
```

validation이 실패하면 document와 command stack을 변경하지 않는다. 세션은 오류의 property 경로,
오류 코드와 표시 메시지를 GUI에 반환하고 GUI는 해당 입력 항목에 오류를 표시한다.

초기 속성 패널은 primitive, enum, color와 layout만 다룬다. 일반 object, list 및 중첩 Element Spec은
`UNSUPPORTED_PROPERTY`로 처리하고 기본 GUI 완성 후 재귀 편집 기능으로 확장한다.

## 4. 요소 선택과 캔버스 이동

요소 선택은 preview runtime bounds를 사용한다.

```text
캔버스 클릭
→ z-index와 렌더 순서를 고려한 bounds hit-test
→ 선택된 element ID 갱신
→ 요소 목록과 속성 패널 선택 상태 동기화
```

요소 이동은 layout Spec 편집으로 변환한다.

```text
drag 시작
→ 시작 bounds와 기존 layout 저장
→ HudLayoutEngine으로 화면 좌표를 anchor 기준 좌표로 역변환
→ layout.x / layout.y 변경
→ Preview 갱신
→ drag 종료 시 하나의 undo 가능한 command로 확정
```

drag 중 발생하는 연속 갱신은 undo stack을 매 프레임 증가시키지 않도록 하나의 command로 병합한다.

선택 bounds에서 요소 anchor와 가장 먼 모서리 handle을 drag하면 `scaleX`와 `scaleY`를 같은 값으로
변경한다. 음수 scale과 축별 비균등 scale은 handle에서 만들지 않고 속성 패널에서만 편집한다.

## 5. 요소 삭제와 재정렬

```text
삭제
→ RemoveElementCommand
→ document 변경 event
→ preview runtime 요소 제거
```

```text
재정렬
→ MoveElementCommand
→ document 변경 event
→ preview 렌더 및 저장 순서 갱신
```

선택된 요소가 삭제되면 세션이 선택을 해제하거나 인접 요소로 옮기고 GUI에 새 선택 상태를 제공한다.

## 6. Undo와 Redo

```text
GUI undo / redo
→ HudCommandStack
→ HudSceneDocument 복원
→ HudCommandStack이 반대 방향의 HudDocumentChange 발행
→ Preview HudScene 동기화
→ dirty와 canUndo / canRedo 상태 갱신
```

GUI는 이전 Spec이나 요소 순서를 별도로 보관하지 않는다. Preview 동기화는 명령 타입이 아니라
command stack이 발행한 document 변경 event만 읽는다.

dirty는 변경 함수 호출 여부가 아니라 현재 document와 마지막 `markClean()` snapshot의 내용 차이로
계산한다. 따라서 undo로 저장 상태까지 돌아오면 false, 다시 redo하면 true가 되며, redo 상태에서
저장한 뒤 undo하면 다시 true가 된다.

## 7. Custom 저장

```text
GUI 저장
→ session.save()
→ HudSceneDocument.toSpec()
→ HudSceneRepository.saveCustom()
→ 성공 시 document.markClean()
→ 장면 출처를 CUSTOM_CONFIG로 갱신
```

resource 장면을 열어 편집하더라도 resource 파일은 수정하지 않는다. 변경 내용은 메모리 document에만
존재하다가 저장 시 config override로 생성된다. 저장 실패 시 document는 dirty 상태를 유지한다.
저장된 장면은 이후 생성되는 HUD부터 사용하며 현재 live HUD는 자동으로 교체하지 않는다.

## 8. Custom 삭제와 Resource 복원

```text
GUI custom 삭제 또는 resource 기본값 복원
→ session.deleteCustom()
→ HudSceneRepository.deleteCustom()
→ 현재 resource 장면 다시 resolve
→ document, command stack과 preview scene 교체
```

삭제 전에 dirty 변경이 있으면 GUI가 폐기 확인을 받을 수 있도록 세션이 별도의 확인 필요 결과를 반환한다.
삭제 후에도 현재 live HUD는 유지하고 이후 생성되는 HUD부터 resource 장면을 사용한다.

## 9. Preview 상태와 Preset

```text
GUI가 속도, 게이지, 랩, 순위 값 변경
→ PreviewHudSceneContext의 가변 상태 변경
→ runtime 요소가 다음 render에서 새 상태를 읽음
```

```text
GUI가 preset 선택
→ PreviewStatePresetApplier
→ 호환성과 capability 불변식 검사
→ preview 상태 일괄 갱신
```

Preview 상태 변경은 장면 Spec, document dirty 상태와 undo/redo stack에 포함하지 않는다.

## 10. Resource Reload

```text
리소스팩 reload
→ HudSceneResourceRegistry 재검증 및 cache 교체
→ resource 기반 editor session에 변경 알림
```

resource 장면을 편집 중이고 document가 dirty하면 외부 reload로 작업 사본을 자동 덮어쓰지 않는다.
현재 live HUD는 자동으로 재생성하지 않으며, 필요하면 별도의 명시적 apply 기능을 추가한다.
세션은 resource 변경 사실을 GUI에 알리고 reload 또는 현재 작업 유지 선택을 받는다.

## 11. 편집기 표시와 종료 UX

속성 및 layout 편집 행의 왼쪽 label은 입력 widget의 세로 중앙에 맞춘다. Validation 오류는 별도 줄에
표시하되 오류 표시 여부에 따라 label과 입력 widget의 기준선이 움직이지 않게 한다.

Editor 종료, Done 버튼, ESC 및 다른 장면으로 전환할 때 document가 dirty이면 다음 선택을 제공한다.

```text
저장 후 나가기 또는 전환
→ session.save()
→ 성공하면 기존 session 닫기
→ 실패하면 현재 editor 유지 및 오류 표시

저장하지 않고 나가기 또는 전환
→ 변경 폐기 확인
→ 기존 session 닫기

취소
→ 현재 session과 editor 유지
```

Preview는 side panel의 남은 공간을 논리 viewport로 사용하지 않는다. 논리 viewport는 현재 게임 GUI 전체
해상도와 같게 유지하고, `HudPreviewTransform`이 toolbar 아래 표시 영역에 맞는 uniform scale과 중앙 위치를
계산한다.

```text
실제 GUI 논리 좌표
→ Pose translate + uniform scale
→ 화면비를 유지한 preview 렌더
→ 남는 영역은 letterbox
```

Side panel을 접으면 같은 논리 viewport를 더 큰 배율로 표시하고 화면 가장자리에 panel 복원 버튼을 남긴다.
Panel 표시 여부는 요소의 anchor, bounds 및 Spec 좌표에 영향을 주지 않는다. 선택을 해제하면 overlay가 없어져
축소된 live HUD와 같은 결과를 확인할 수 있다.

Scene과 runtime bounds는 논리 좌표에서 유지한다. 모든 mouse 입력은 공유 transform으로 화면 좌표에서 논리
좌표로 한 번만 역변환한 뒤 hit-test와 이동·배율 drag에 사용한다. Scissor는 변환된 화면 영역으로 설정하고,
선택 outline과 anchor 위치는 화면 좌표로 옮기되 handle 크기와 선 두께는 화면상 일정하게 유지한다.

## 오류 처리 원칙

- validation 실패는 property 경로와 함께 GUI에 반환한다.
- 저장·삭제 I/O 실패는 document를 변경하거나 clean 상태로 만들지 않는다.
- 손상된 custom config는 보존하고 resource fallback 진단을 표시한다.
- runtime 요소 생성 실패는 게임을 중단하지 않고 기존 복구 정책을 사용한다.
- 실패한 편집 요청은 command stack과 preview scene을 변경하지 않는다.

## 구현 순서

1. [x] Generic Spec 편집 결과를 검증된 `ReplaceElementSpecCommand`로 변환
2. [x] `HudDocumentChange`, clean snapshot 기반 dirty와 command stack event 발행 구현
3. [x] element ID 기반 `PreviewHudSceneSynchronizer` 구현
4. [x] `HudEditorSession`과 GUI용 상태 및 결과 모델 구현
5. [x] 편집 세션의 custom 저장·삭제 및 resource 기본값 복원 구현
6. [ ] 요소 목록, 팔레트와 속성 패널 구현
   - [x] 중앙 preview와 요소·속성·프리뷰 상태 탭을 가진 단일 side panel 화면 및 요소 구조 편집 구현
   - [x] 탭별 목록 scroll과 drag 기반 side panel 너비 조절 구현
   - [x] metadata 기반 primitive·enum·color property 입력 widget 구현
   - [x] layout property 입력 widget 구현
   - [x] 속성 행 정렬, dirty 종료 확인과 editor 내부 장면 전환 구현
   - [ ] side panel을 접을 수 있고 화면비를 보존하는 preview 구현
   - [ ] preview 상태 조절 구현
7. [x] 캔버스 선택·drag와 command 병합 구현
8. [x] 저장·삭제·복원 UI 구현
9. [ ] 기본 GUI 완성 후 중첩 object/list 및 compound child 편집 구현

## 유지해야 하는 불변식

- 영속 편집은 항상 command를 통해 document에 적용한다.
- document에 적용되는 Spec은 공통 validator를 통과해야 한다.
- Preview HudScene은 document와 동일한 element ID, 순서 및 Spec을 반영한다.
- resource 파일은 GUI에서 수정하지 않는다.
- Preview 상태는 장면 JSON에 저장하지 않는다.
- GUI는 repository, loader 및 runtime element 구현에 직접 의존하지 않는다.

# DynamicRider HUD 리팩토링 계획

## 목표

HUD 장면 정의를 Kotlin DSL에서 JSON으로 통일하고, 인게임 GUI 편집기가 동일한 장면 모델을 읽고 수정하도록 전환한다.

장면 해석 우선순위는 다음과 같다.

```text
config/dynrider/hud/{mode}/{engine}.json
    → assets/dynrider/hud/{mode}/{engine}.json
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
- [ ] 요소 내부의 미사용 `Builder` 및 DSL 기반 클래스를 제거한다.

## 2. 장면 저장소와 수명주기

- [x] config/resource 우선순위를 `HudSceneRepository`로 통합한다.
- [x] resolve 결과에 `CUSTOM_CONFIG`, `RESOURCE`, `CUSTOM_FALLBACK` 출처를 기록한다.
- [x] 커스텀 저장 및 삭제 API를 제공한다.
- [x] 장면 생성 실패 시 게임을 중단하지 않고 빈 장면으로 복구한다.
- [ ] 현재 ride/spectate 엔진 분기를 engine descriptor 기반 조회로 교체한다.
- [ ] 저장·삭제·리소스 reload 후 현재 장면을 즉시 다시 생성하는 controller를 도입한다.

## 3. 편집 문서와 레이아웃

- [x] 각 요소의 영속 ID를 JSON에 선택적으로 저장한다.
- [x] 기존 JSON에 ID가 없으면 결정적인 ID를 생성하는 `HudSceneDocument`를 제공한다.
- [x] 추가·삭제·재정렬·spec 교체를 undo/redo command로 제공한다.
- [x] anchor·scale 좌표 계산과 hit-test bounds를 `HudLayoutEngine`으로 분리한다.
- [ ] 런타임 `HudScene`이 document 변경을 감지하고 변경된 요소만 재생성하도록 연결한다.
- [ ] compound element의 자식에도 영속 ID와 편집 가능한 bounds를 제공한다.

## 4. 요소 등록과 Preview 데이터

- [ ] serializer, type ID, 표시 이름, 기본 spec, 엔진 호환성 및 runtime factory를 element descriptor registry로 통합한다.
- [ ] 요소 속성의 범위, 색상, enum 등 편집기 입력 메타데이터를 descriptor에 추가한다.
- [ ] 실제 카트·전역 manager 접근을 `HudDataContext`로 감싼다.
- [ ] 속도, 게이지, 니트로, 타이머 및 순위용 preview context를 제공한다.
- [ ] legacy HUD/state 코드를 신규 계약으로 이관하거나 제거한다.

## 5. 인게임 GUI 편집기

- [ ] 리소스 장면은 읽기 전용으로 열고 첫 변경 시 config 문서를 생성한다.
- [ ] 요소 팔레트, 캔버스 선택·이동, 속성 패널을 구현한다.
- [ ] undo/redo, 저장, 커스텀 삭제 및 리소스 기본값 복원을 제공한다.
- [ ] drag 중 명령을 병합하고 anchor 기준 좌표로 역변환한다.
- [ ] 저장 또는 삭제 후 현재 HUD를 즉시 갱신한다.

## 검증 기준

- 모든 resource JSON이 codec과 엔진 호환성 검사를 통과한다.
- 유효한 config가 resource보다 우선하고, config가 없으면 현재 리소스팩 장면을 사용한다.
- 잘못된 config는 보존되며 resource 장면으로 fallback한다.
- JSON encode/decode, ID 보존, atomic save, delete-to-fallback을 테스트한다.
- 다양한 화면 크기와 anchor/scale 조합에서 bounds와 inverse 좌표를 테스트한다.
- 모든 command는 undo 후 원본 document를 복원하고 redo 후 변경 상태를 복원한다.
- `compileClientKotlin`, `test`, `build`가 통과한다.

## 기본 결정

- 리소스팩의 built-in JSON override를 공식 지원한다.
- GUI는 resource 파일을 수정하지 않고 config override만 생성한다.
- 커스텀 JSON 오류 시 사용자 파일을 자동 수정하거나 삭제하지 않는다.
- 초기 구현은 spec 변경 시 요소를 재생성하고, 세부 runtime patch는 성능 문제가 확인된 뒤 추가한다.

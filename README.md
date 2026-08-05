# DynamicRider

마크라이더(KartRider: Minecraft)를 위한 Fabric 클라이언트 HUD 모드입니다.

카트 엔진과 주행 상태에 맞는 HUD를 제공하며, HUD 커스터마이징과 마크라이더 이용을 위한 편의 기능을 포함합니다.

DynamicRider is a Fabric client mod that provides customizable, engine-aware HUDs and convenience features for KartRider: Minecraft.

> 이 프로젝트는 **Nexon Games**의 의사에 따라 사전 예고 없이 개발이 중단될 수 있으며, 소스 코드를 제외한 일부 리소스가 저장소에서 제거될 수 있습니다.
>
> Development of this project may be discontinued without prior notice at the request of **Nexon Games**, and some resources other than the source code may be removed from the repository.

## 요구 사항

| 항목 | 요구 버전 |
| --- | --- |
| Minecraft | 1.21.5 |
| Fabric Loader | 0.18.4 이상 |
| Fabric API | 1.21.5용 버전 |
| Fabric Language Kotlin | 1.13.8+kotlin.2.3.0 이상 |
| SkidMC | 1.0.0-beta.4와 호환되는 1.0 계열 |

[Mod Menu](https://modrinth.com/mod/modmenu)는 선택 사항입니다. 설치하지 않아도 일시 정지 화면의 DynamicRider 설정 버튼을 이용할 수 있습니다.

## 설치

1. Minecraft 1.21.5용 Fabric Loader를 설치합니다.
2. Fabric API, Fabric Language Kotlin 및 호환되는 SkidMC를 설치합니다.
3. DynamicRider JAR 파일을 Minecraft 인스턴스의 `mods` 디렉터리에 넣습니다.
4. 게임을 실행한 뒤 일시 정지 화면 또는 Mod Menu에서 DynamicRider 설정을 엽니다.

DynamicRider는 클라이언트 전용 모드입니다. 서버에 별도로 설치할 필요는 없지만, HUD 데이터 제공을 위해 호환되는 마크라이더 데이터팩과 SkidMC 환경이 필요합니다.

## 소스에서 빌드

### 요구 사항

- JDK 21 이상
- Git
- Gradle 9.2.1 또는 Gradle을 실행할 수 있는 IntelliJ IDEA

```bash
git clone https://github.com/oni0nfr1/DynamicRider.git
cd DynamicRider
gradle build
```

IntelliJ IDEA를 사용하는 경우 프로젝트를 Gradle 프로젝트로 연 뒤 `Tasks > build > build`를 실행할 수도 있습니다.

빌드 결과는 `build/libs/`에 생성됩니다. 일반 모드 JAR에는 `-sources`가 붙지 않은 파일을 사용합니다.

## 개발자 및 기여자

프로젝트 개발자와 리소스 기여 내역은 [CONTRIBUTORS.md](src/client/resources/assets/dynrider/licenses/CONTRIBUTORS.md)에서 확인할 수 있습니다.

## 라이선스

리소스를 제외한 소스 코드에는 **GPL-3.0-or-later** 라이선스가 적용됩니다. GPL 라이선스에 따라 타인에게 본 저작물 또는 수정본의 바이너리를 제공할 때 소스 코드와 변경 내역도 함께 제공해야 합니다. 자세한 내용은 [LICENSE.txt](LICENSE.txt)를 확인해 주세요.

Except for resources, the source code is licensed under **GPL-3.0-or-later**. When distributing binaries of this project or a modified version, the corresponding source code and modification information must also be provided in accordance with the GPL. See [LICENSE.txt](LICENSE.txt) for details.

리소스 라이선스는 다음과 같습니다.

- DSEG7 Classic Bold Italic: SIL Open Font License 1.1
- 그 외 넥슨 관련 리소스: [넥슨 브랜드 아이덴티티 가이드라인](https://brand.nexon.com/ko/ci-brand-guidelines/) 참고

Resource licenses include:

- DSEG7 Classic Bold Italic: SIL Open Font License 1.1
- Other Nexon-related resources: refer to the [Nexon Brand Identity Guidelines](https://brand.nexon.com/ko/ci-brand-guidelines/)

**라이선스가 별도로 명시되지 않은 리소스는 모든 권리가 보호됩니다(All Rights Reserved).** 이용하려면 해당 리소스의 원저작자와 상의해 주세요.

**All rights are reserved for resources without an explicitly stated license.** Contact the respective original author before using such resources.

# DynamicRider

마크라이더를 위한 HUD 모드
본 모드는 카트라이더 : 마인크래프트 맵 및 데이터팩을 이용할 때 더욱 깔끔하고 보기 좋은 HUD를 제공하며,
이외에도 카트 엔진 적용 커맨드의 간소화와 같은 간단한 편의성 기능들을 포함합니다.

This mode provides a cleaner and better-looking HUD when using the KartRider: Minecraft map and data pack,
as well as simple convenience features such as simplifying cart engine application commands.

이 프로젝트는 **Nexon Games**의 의사에 따라 사전 예고 없이 중단 및 숨김 처리될 수 있으니, 숙지 바랍니다.

## 빌드 방법
소스 코드를 빌드하여 이용하고자 한다면, 다음의 방식을 따를 수 있습니다

### 요구 사항
1. IntelliJ IDEA (필수)
2. Git           (선택)

이후 작업 편의와 라이선스 이행을 위해 Git을 설치하는 것을 권장드립니다.

### Git이 있는 경우
1. 소스 코드 다운로드를 원하는 경로에서 터미널을 열어 다음 명령어를 실행합니다
    ```bash
    git clone https://github.com/oni0nfr1/DynamicRider.git
    ```
2. IntelliJ IDEA를 통해 설치된 디렉토리를 엽니다.
3. IDE 창 우측의 Gradle 탭을 열어 DynamicRider > Tasks > build > build를 클릭합니다.
4. 빌드가 완료되면 프로젝트 루트의 `build/libs/`에 빌드된 JAR 파일이 출력됩니다.

### Git이 없는 경우
1. Github 레포지토리에서 Code라 적힌 초록색 버튼을 클릭합니다.
2. 팝업 탭 하단의 Download ZIP을 클릭하여 원하는 경로에 소스 코드를 설치 및 압축 해제합니다.
3. Git이 있는 경우의 2번부터 진행하면 됩니다.

## CREDIT
### oni0nfr1
개발자
### Dominogames0229
부스터 아이콘 텍스쳐 제공  
**(카트라이더 부스터 아이콘을 본따 만들었습니다)**

## THANKS TO
### RLZL
폰트 관련 정보 제공  
**(폰트의 저작권은 넥슨코리아에 있습니다.)**

## LICENSE
리소스를 제외한 소스 코드에 **GPL-3.0-or-later** 라이선스가 적용됩니다.  
GPL 라이선스에 따라 타인에게 본 저작물 또는 그 수정본의 바이너리를 제공할 때 소스 코드와 변경 내역을 함께 제공하여야 합니다.  
자세한 내용은 **LICENSE.txt**를 확인해 주시기 바랍니다.

리소스에 대한 라이선스는 다음과 같습니다.
DSEG7 Classic Bold Italic - SIL Open Font License 1.1 
이외 넥슨과 관련된 리소스들은 [넥슨 브랜드 아이덴티티 가이드라인](https://brand.nexon.com/ko/ci-brand-guidelines/)을 참고해주시기 바랍니다.

**라이선스가 명시되지 않은 리소스들**에 대해서는 **모든 권리가 보호됩니다(All Rights Reserved)**. 이용을 위해서는 리소스 원저작자와 상의해 보시기 바랍니다.
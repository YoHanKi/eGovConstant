# eGovConstant

![Build](https://github.com/YoHanKi/eGovConstant/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

<!-- Plugin description -->
eGovConstant is an IntelliJ IDEA plugin that recommends standardized variable names (fields, parameters, local variables, constants, etc.) based on Korean government and public standard dictionaries.

행정표준용어 및 공공기관 표준 사전을 기반으로 표준화된 변수명(필드, 파라미터, 지역 변수, 상수 등)을 추천해주는 IntelliJ IDEA 플러그인입니다.

### 주요 기능:
- **내장 표준 데이터셋**: 별도의 설정 없이도 즉시 사용할 수 있는 권위 있는 표준 데이터셋을 기본으로 포함하고 있습니다.
- **JSON 임포트**: 사용자 정의 사전이나 추가 데이터를 JSON 형식으로 간편하게 가져올 수 있습니다. 유효성 검사 및 기존 데이터와의 병합을 지원합니다.
- **강력한 검색 및 랭킹**: 용어(Terms), 단어(Words), 도메인(Domains) 통합 모델을 지원하며, 동의어 및 금칙어 처리가 포함되어 있습니다. (정확도, 접두사, 포함, 퍼지 검색 지원)
- **변수명 생성 규칙**: 일관된 약어 처리 및 접미사 규칙을 적용하여 camelCase, snake_case, PascalCase 등 다양한 형식을 제공합니다.
- **eGovConstant 도구 창**: 데이터셋 관리, 검색, 복사 및 삽입 기능을 제공하며 로딩 상태와 통계를 확인할 수 있습니다.
- **코드 자동 완성**: 변수 선언 시 2글자 이상 입력하면 표준 용어를 기반으로 이름을 추천합니다. (Java, Kotlin 지원)
- **리네임 인텐션**: "Rename to eGovConstant recommended name" 기능을 통해 기존 변수명을 표준 용어로 손쉽게 변경할 수 있습니다.
- **영구 저장소**: 사용자가 추가하거나 임포트한 데이터는 로컬에 안전하게 저장되어 유지됩니다.

### 사용 방법:
1. **도구 창 열기**: `eGovConstant` 도구 창을 엽니다.
2. **검색 및 사용**: 검색창에 한국어 용어를 입력하여 검색된 영문 약어나 이름을 코드에 복사하거나 즉시 삽입할 수 있습니다.
3. **사용자 데이터 추가**:
   - `Settings` 탭에서 JSON 형식의 사전 데이터를 붙여넣고 "Import JSON"을 클릭합니다.
   - 데이터는 자동으로 검증되어 기존 사전과 병합됩니다.
   - "Reset to Default"를 통해 언제든지 기본 상태로 초기화할 수 있습니다.
4. **코드 작성 중 활용**: 
   - 변수 선언 시 자동 완성 목록에서 추천 이름을 선택합니다.
   - 기존 변수명 위에서 `Alt + Enter`를 눌러 권장 이름으로 변경합니다.
<!-- Plugin description end -->

## 설치 방법

- **IDE 내 설치**:
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>"eGovConstant" 검색</kbd> > <kbd>Install</kbd>

- **JetBrains Marketplace**:
  [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)에서 <kbd>Install to ...</kbd> 버튼을 클릭하여 설치할 수 있습니다.

- **수동 설치**:
  [최신 릴리스](https://github.com/YoHanKi/eGovConstant/releases/latest)를 다운로드한 후 <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>를 통해 설치하세요.

---
이 플러그인은 [IntelliJ Platform Plugin Template][template]을 기반으로 제작되었습니다.

[template]: https://github.com/JetBrains/intellij-platform-plugin-template

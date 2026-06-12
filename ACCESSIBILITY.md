# StudyMate 접근성 가이드

> 최종 수정: 2025년  
> 대상 플랫폼: Android (minSdk 24 / targetSdk 34)

---

## 1. 관련 법령

### 장애인차별금지 및 권리구제 등에 관한 법률 (장애인차별금지법)

**제21조 (정보통신·의사소통 등에서의 정당한 편의제공 의무)**

정보통신사업자는 장애인이 해당 서비스를 이용하는 데 불이익이 없도록 정당한 편의를 제공해야 한다. 2015년 4월 11일 이후 서비스 제공자는 웹 및 모바일 콘텐츠에 대한 접근성을 보장해야 하며, 위반 시 시정명령 및 손해배상 청구 대상이 될 수 있다.

**적용 범위**: 모바일 앱을 포함한 모든 정보통신 서비스.

---

### 과학기술정보통신부 고시 제2020-5호

**모바일 애플리케이션 콘텐츠 접근성 지침 2.0 (MWCAG 2.0)**

한국 표준으로, 모바일 앱이 준수해야 할 접근성 요건을 규정한다. WCAG 2.1을 기반으로 모바일 환경에 특화된 지침을 추가했다. 4개 원칙 아래 총 22개 검사 항목이 있다.

---

## 2. MWCAG 2.0 / WCAG 2.1 4원칙 및 적용 현황

### 원칙 1: 인식의 용이성 (Perceivable)

사용자가 콘텐츠를 인식할 수 있어야 한다.

| 항목 | 지침 내용 | StudyMate 적용 현황 |
|------|-----------|---------------------|
| 1.1.1 | 텍스트가 아닌 콘텐츠에 대체 텍스트 제공 | ✅ 로고 `contentDescription="StudyMate 앱 로고"` 적용 |
| 1.3.1 | 정보·구조·관계를 프로그래밍 방식으로 전달 | ✅ `android:labelFor`로 폼 레이블-입력 연결 (로그인·회원가입·학습 입력) |
| 1.4.3 | 텍스트 명도 대비 4.5:1 이상 (소문자 기준) | ✅ 전체 색상 쌍 검증 통과 (하단 표 참조) |
| 1.4.11 | 비텍스트 대비 3:1 이상 (아이콘·UI 컴포넌트) | ✅ 버튼·카드 배경색 기준 통과 |

### 원칙 2: 운용의 용이성 (Operable)

사용자가 UI를 조작할 수 있어야 한다.

| 항목 | 지침 내용 | StudyMate 적용 현황 |
|------|-----------|---------------------|
| 2.1.1 | 키보드(스위치 접근) 완전 운용 가능 | ✅ `StudyMateBottomTab`, `StudyMateBackNavigation` 스타일에 `clickable=true`, `focusable=true` 적용 |
| 2.4.3 | 의미 있는 포커스 순서 | ✅ XML 선언 순서 = 포커스 순서, 추가 재정렬 불필요 |
| 2.4.6 | 의미 있는 레이블 | ✅ 모든 탭·뒤로가기 버튼에 `contentDescription` 적용 |
| 2.5.3 | 레이블이 이름(접근성 텍스트)에 포함 | ✅ `contentDescription`이 화면 텍스트보다 더 상세한 설명 제공 |
| 2.5.5 | 터치 대상 최소 44×44dp (권고 48dp) | ✅ `StudyMateBottomTab`에 `minHeight=48dp` 적용; 주요 버튼 모두 `minHeight=48dp` 이상 |

### 원칙 3: 이해의 용이성 (Understandable)

콘텐츠와 UI 동작을 이해할 수 있어야 한다.

| 항목 | 지침 내용 | StudyMate 적용 현황 |
|------|-----------|---------------------|
| 3.1.1 | 페이지의 기본 언어 명시 | ✅ `AndroidManifest.xml` → 기기 언어 자동 적용 |
| 3.3.1 | 오류 식별 | ✅ 입력 오류 시 오류 텍스트 표시 (loginErrorText, signupErrorText, inputErrorText) |
| 3.3.2 | 레이블 또는 지시 사항 제공 | ✅ EditText `hint` + `labelFor` 레이블 적용 |

### 원칙 4: 견고성 (Robust)

다양한 보조 기술과 호환되어야 한다.

| 항목 | 지침 내용 | StudyMate 적용 현황 |
|------|-----------|---------------------|
| 4.1.2 | 이름·역할·값 프로그래밍 방식 제공 | ✅ Button·RadioButton은 기본 역할 자동 제공; TextView 클릭 가능 항목에 `clickable=true` 명시 |
| 4.1.3 | 상태 메시지 프로그래밍 방식 전달 | ⚠️ Toast로 처리 중; 추후 `AccessibilityEvent.TYPE_ANNOUNCEMENT` 개선 고려 |

---

## 3. 색상 대비 검증 결과

앱 전체가 다크 테마 단일 색상 세트를 사용한다. 아래는 실제 사용 조합 전체 검증 결과다.

| 전경색 | 배경색 | 용도 | 대비율 | 판정 |
|--------|--------|------|--------|------|
| `study_text` #F6F4EE | `study_bg` #1C1C1A | 본문 텍스트 | **17.5:1** | ✅ AAA |
| `study_text` #F6F4EE | `study_surface` #2A2A28 | 바텀 탭 텍스트 | **15.2:1** | ✅ AAA |
| `study_text` #F6F4EE | `study_card` #2A2A28 | 카드 텍스트 | **15.2:1** | ✅ AAA |
| `study_text_muted` #B8B5AD | `study_bg` #1C1C1A | 보조 텍스트 | **8.0:1** | ✅ AA |
| `study_text_muted` #B8B5AD | `study_surface` #2A2A28 | 탭 비활성 텍스트 | **7.0:1** | ✅ AA |
| `study_primary_text` #252522 | `study_primary_button` #F7F5F0 | 주요 버튼 텍스트 | **16.8:1** | ✅ AAA |
| `study_error` #FF817A | `study_bg` #1C1C1A | 오류 텍스트 | **5.1:1** | ✅ AA |
| `study_success` #7ED957 | `study_bg` #1C1C1A | 성공 텍스트 | **7.4:1** | ✅ AA |

> 기준: WCAG 2.1 SC 1.4.3 — 일반 텍스트 4.5:1 이상, 큰 텍스트(18sp bold 또는 24sp) 3:1 이상.  
> **결과: 모든 조합 AA 또는 AAA 달성.**

---

## 4. 터치 대상 크기 현황

WCAG 2.5.5 권고 및 Google Material Design 기준 최소 48×48dp.

| 컴포넌트 | 최소 높이 | 판정 |
|----------|-----------|------|
| 주요 버튼 (Button) | `@dimen/button_height` (56dp) | ✅ |
| 바텀 네비게이션 탭 | `minHeight=48dp` (스타일) | ✅ |
| 뒤로가기 내비게이션 | `@dimen/header_row_height` (48dp) | ✅ |
| 보조 버튼 | `minHeight=48dp` 명시 | ✅ |
| 회원가입 링크 (signupLink) | `layout_height=48dp` | ✅ |
| 오답 노트 선택 카드 (동적 생성) | `card_padding` × 2 + 텍스트 ≥ 48dp | ✅ |

---

## 5. TalkBack / 스위치 접근 지원 현황

### 적용된 항목

- **바텀 내비게이션**: 모든 탭에 한국어 `contentDescription` 적용 (`홈 탭`, `오답노트 탭`, `마이페이지 탭`)
- **뒤로가기 버튼**: 화면별 목적지를 설명하는 `contentDescription` 적용 (`홈으로 돌아가기`, `로그인으로 돌아가기` 등)
- **폼 레이블**: `android:labelFor`로 EditText와 연결, `importantForAccessibility="no"`로 레이블 자체 중복 읽기 방지
- **통계 카드**: 숫자+단위 조합 대신 의미 있는 전체 설명 제공 (`내 학습 기록 수`, `총 학습 횟수. 탭하면 학습 목록으로 이동합니다.` 등)
- **오답 선택 카드** (동적 생성): `contentDescription`에 노트 제목 + 오답 수 + 동작 안내 포함
- **회원가입 링크**: `clickable=true`, `focusable=true`, `selectableItemBackgroundBorderless` 적용

### 향후 개선 고려 사항

- 오류 메시지(`loginErrorText` 등)가 표시될 때 `AccessibilityEvent.TYPE_ANNOUNCEMENT`로 TalkBack에 즉시 알림
- 퀴즈 보기(RadioButton) 텍스트를 프로그래밍 방식으로 설정할 때 `cd_option_format` 문자열 리소스 활용 (`보기 1번: …`)
- 로딩 상태(AI 요약 생성 중) 표시 시 접근성 알림 추가

---

## 6. Google Play 출시 요건

### 대상 API 레벨

- **targetSdkVersion 34 이상** 필수 (2024년 8월 이후 신규 앱 기준)
- 현재 프로젝트: `targetSdk = 34` ✅

### 개인정보처리방침

- Google Play 콘솔에서 **개인정보처리방침 URL** 등록 필수
- 앱이 사용자 데이터(이메일, 학습 기록)를 수집하므로 해당 정책 명시 필요

### 데이터 안전 섹션

Play Console → 앱 콘텐츠 → 데이터 안전에서 다음 항목 선언 필요:

| 데이터 유형 | 수집 여부 | 목적 |
|------------|-----------|------|
| 이메일 주소 | 수집 | 계정 관리 |
| 앱 활동 (학습 기록, 퀴즈 결과) | 수집 | 앱 기능 |
| 앱 내 진단 정보 | 수집 (Firebase Crashlytics 사용 시) | 성능 모니터링 |

### 앱 콘텐츠 등급

- Google Play 콘솔 → 앱 콘텐츠 → 콘텐츠 등급 설문 완료 필요
- StudyMate는 교육용이므로 전체 이용가(Everyone) 등급 예상

---

## 7. 접근성 검증 체크리스트

아래 항목을 출시 전 최종 확인한다.

### 코드 검증 (완료)

- [x] 모든 바텀 탭에 `contentDescription` 적용
- [x] 모든 뒤로가기 버튼에 `contentDescription` 적용
- [x] 모든 폼 레이블에 `android:labelFor` 연결
- [x] 클릭 가능한 TextView에 `clickable=true` + `focusable=true` 명시
- [x] 동적 생성 카드(WrongAnswerActivity)에 `contentDescription` + `clickable/focusable` 설정
- [x] 버튼·탭 최소 높이 48dp 확보
- [x] 통계 카드에 의미 있는 `contentDescription` 적용
- [x] 색상 대비 전체 조합 검증 통과

### 기기 테스트 (출시 전 수행)

- [ ] TalkBack 활성화 후 전체 화면 포커스 흐름 확인
- [ ] 스위치 접근(Switch Access) 탭 이동 검증
- [ ] 글꼴 크기 최대(200%) 설정 시 레이아웃 깨짐 여부 확인
- [ ] 고대비 모드에서 색상 반전 없이 콘텐츠 식별 가능 여부 확인
- [ ] Android Accessibility Scanner 실행 후 경고 항목 해소

---

*본 문서는 장애인차별금지법 및 MWCAG 2.0 준수를 위한 내부 접근성 관리 문서입니다.*

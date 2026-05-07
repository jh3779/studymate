# 팀 작업 분담표

## 1. 문서 목적

이 문서는 StudyMate MVP를 4개 파트로 나누어 협업자가 병렬로 작업할 수 있도록 역할, 책임 범위, 담당 파일, 주차별 작업을 정리한다.

현재 확정된 협업자는 3명이며, 4번째 인원은 `TBD(미정)`으로 둔다. 실제 팀원이 확정되면 `TBD` 항목의 이름만 교체한다.

## 2. 핵심 내용

| 담당 | 파트 | 핵심 책임 | 주요 산출물 |
| --- | --- | --- | --- |
| 협업자 1 | PM / UX / 화면 구조 | 화면 흐름, 공통 UI, 라우팅, 발표 문서 | 화면 기본 UI, 공통 위젯, 발표 자료 |
| 협업자 2 | Firebase / Auth / DB | 인증, Firestore, 데이터 모델, 보안 규칙 초안 | 로그인/회원가입, DB 저장/조회 |
| 협업자 3 | AI / 요약 / 프롬프트 | AIService, 요약 생성, 퀴즈 생성 요청, JSON 파싱 | AI 요약/퀴즈 생성 기능 |
| TBD | 퀴즈 / 오답 / QA | 퀴즈 풀이, 결과 계산, 오답노트, 통합 테스트 | QuizScreen, 결과 화면, 오답노트 |

## 3. 상세 설명

### 협업자 1: PM / UX / 화면 구조

담당 목표는 앱 전체 흐름이 끊기지 않도록 화면 구조와 사용자 경험을 먼저 잡는 것이다.

| 구분 | 담당 내용 |
| --- | --- |
| 주요 화면 | SplashScreen, HomeScreen, 공통 레이아웃 |
| 공통 위젯 | common_button, study_card, loading_dialog |
| 문서 | 화면 흐름, 화면 설계, 발표 구성, 시연 시나리오 |
| 협업 지점 | 각 기능 담당자가 만든 화면을 라우팅에 연결 |

담당 파일 예시는 다음과 같다.

```text
lib/app.dart
lib/screens/splash_screen.dart
lib/screens/home_screen.dart
lib/widgets/common_button.dart
lib/widgets/study_card.dart
lib/widgets/loading_dialog.dart
docs/03_design/screen_flow.md
docs/03_design/screen_spec.md
docs/07_presentation/
```

### 협업자 2: Firebase / Auth / DB

담당 목표는 사용자별 데이터 저장과 조회 기반을 안정적으로 만드는 것이다.

| 구분 | 담당 내용 |
| --- | --- |
| 인증 | 회원가입, 로그인, 로그아웃, 로그인 상태 유지 |
| DB | users, study_notes, quizzes, quiz_results, wrong_answers 저장/조회 |
| 모델 | UserModel, StudyNoteModel, QuizModel, QuizResultModel, WrongAnswerModel |
| 보안 | API Key 제외, Firestore 사용자별 접근 기준 정리 |

담당 파일 예시는 다음과 같다.

```text
lib/models/
lib/services/auth_service.dart
lib/services/firestore_service.dart
lib/providers/auth_provider.dart
lib/utils/constants.dart
docs/04_database/
docs/05_api/api_security_notes.md
```

### 협업자 3: AI / 요약 / 프롬프트

담당 목표는 입력된 학습 내용을 AI 요약과 퀴즈 데이터로 변환하는 기능을 구현하는 것이다.

| 구분 | 담당 내용 |
| --- | --- |
| AI 요약 | `generateSummary(text)` 구현 |
| AI 퀴즈 | `generateQuizzes(text)` 구현 |
| 파싱 | summary, keywords, quizzes JSON 검증 |
| 예외 처리 | API 실패, JSON 파싱 실패, 빈 응답 처리 |

담당 파일 예시는 다음과 같다.

```text
lib/services/ai_service.dart
lib/providers/study_provider.dart
lib/screens/study_input_screen.dart
lib/screens/summary_result_screen.dart
docs/03_design/ai_flow.md
docs/05_api/ai_prompt_spec.md
```

### TBD: 퀴즈 / 오답 / QA

담당 목표는 생성된 퀴즈를 사용자가 풀고, 결과와 오답을 저장하는 학습 완성 흐름을 구현하는 것이다.

| 구분 | 담당 내용 |
| --- | --- |
| 퀴즈 풀이 | 문제 표시, 보기 선택, 다음 문제 이동 |
| 결과 계산 | 정답 여부 확인, 점수 계산, 정답률 표시 |
| 오답노트 | 틀린 문제 저장, 목록 조회, 상세 확인, 다시 풀기 |
| QA | 전체 플로우 테스트, 발표 시연 데이터 점검 |

담당 파일 예시는 다음과 같다.

```text
lib/services/quiz_service.dart
lib/providers/quiz_provider.dart
lib/screens/quiz_screen.dart
lib/screens/quiz_result_screen.dart
lib/screens/wrong_answer_screen.dart
lib/screens/my_page_screen.dart
lib/widgets/quiz_option_card.dart
docs/06_development/development_schedule.md
docs/07_presentation/demo_scenario.md
```

### 주차별 병렬 작업 기준

| 주차 | 협업자 1 | 협업자 2 | 협업자 3 | TBD |
| --- | --- | --- | --- | --- |
| 1주차 | 화면 흐름, 발표 구조 초안 | DB 구조, Firebase 프로젝트 | AI 프롬프트 초안 | 테스트 체크리스트 초안 |
| 2주차 | 앱 구조, 공통 위젯 | Firebase 연동, 인증 | AIService 인터페이스 | QuizService 인터페이스 |
| 3주차 | HomeScreen, StudyCard | study_notes 저장/조회 | StudyInput 연동 지원 | 퀴즈 화면 와이어프레임 |
| 4주차 | SummaryResult UI | 요약 저장 연동 | 요약 API 구현 | 요약 결과 테스트 |
| 5주차 | QuizScreen UI 지원 | quizzes 저장/조회 | 퀴즈 API 구현 | 퀴즈 표시/선택 구현 |
| 6주차 | 결과 화면 UI 지원 | quiz_results, wrong_answers 저장 | AI 응답 오류 처리 | 점수 계산, 오답 추출 |
| 7주차 | MyPage UI, 발표 자료 | 통계 조회 지원 | AI 기능 안정화 | 오답노트, 다시 풀기 |
| 8주차 | 발표 자료, 시연 정리 | 데이터 초기화, 보안 점검 | API 실패 대비 자료 | 통합 테스트, 버그 수정 |

### 작업 경계

| 영역 | 1차 담당 | 리뷰 담당 |
| --- | --- | --- |
| 화면 흐름과 공통 UI | 협업자 1 | TBD |
| Firebase 인증과 Firestore | 협업자 2 | 협업자 1 |
| AI 요청과 JSON 파싱 | 협업자 3 | 협업자 2 |
| 퀴즈 풀이와 오답노트 | TBD | 협업자 3 |
| 발표와 시연 | 협업자 1 | 전체 |
| 통합 테스트 | TBD | 전체 |

## 4. 개발 시 참고사항

- 각 담당자는 자기 파트의 파일을 우선 수정하고, 다른 파트 파일 수정이 필요하면 작업 전에 공유한다.
- 공통 모델과 FirestoreService는 여러 파트가 사용하므로 협업자 2가 인터페이스를 먼저 정리한다.
- AI 응답 형식이 바뀌면 협업자 3은 협업자 2와 TBD에게 즉시 공유한다.
- QuizScreen과 QuizResultScreen은 AI 퀴즈 데이터 구조에 의존하므로 협업자 3과 TBD가 함께 테스트한다.
- 8주차에는 신규 기능 추가를 중단하고 버그 수정, UI 정리, 발표 준비에 집중한다.

## 5. 확인 체크리스트

- [ ] 4개 파트의 책임이 겹치지 않는가?
- [ ] TBD 담당 범위가 실제 팀원 확정 후 바로 넘겨줄 수 있는 수준인가?
- [ ] 각 파트의 담당 파일이 명확한가?
- [ ] Firestore, AI, 퀴즈 기능 간 연동 지점이 정의되어 있는가?
- [ ] 8주차 통합 테스트와 발표 준비 책임이 정리되어 있는가?

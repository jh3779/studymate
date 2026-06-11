# 우지훈 작업 우선도

## 1. 문서 목적

이 문서는 StudyMate 프로젝트에서 우지훈 담당자의 작업 우선순위와 진행 상태를 정리한다. 우지훈의 역할은 `PM / UX / 화면 구조`이므로 화면 흐름, 공통 UI 기준, 라우팅, 발표 자료, 통합 조율을 중심으로 작업한다.

작성일: 2026-05-07

## 2. 담당 범위 요약

| 구분 | 담당 범위 |
| --- | --- |
| PM | 일정, 우선순위, MVP 범위 관리 |
| UX | 화면 흐름, 와이어프레임, 상태/오류/접근성 기준 |
| 화면 구조 | SplashScreen, HomeScreen, 공통 레이아웃, 하단 탭, 라우팅 |
| 공통 UI | 버튼, 카드, 로딩, 입력 상태, 오류 메시지 기준 |
| 통합 조율 | Firebase, AI, 퀴즈 담당자의 화면 연결 지원 |
| 발표 | 발표 구성, 시연 흐름, 예비 자료 정리 |

## 3. 우선순위 기준

| 우선순위 | 의미 | 처리 기준 |
| --- | --- | --- |
| P0 | 전체 팀 작업을 막는 기반 작업 | 가장 먼저 처리한다. 화면 흐름, 라우팅, 공통 UI 기준이 여기에 해당한다. |
| P1 | MVP 핵심 플로우 구현 작업 | 입력 -> 요약 -> 퀴즈 -> 결과 -> 오답 흐름에 직접 필요한 작업이다. |
| P2 | 통합, 검수, 품질 보강 작업 | 접근성, 오류 상태, QA, 다른 담당자 화면 연결 작업이다. |
| P3 | 발표 및 마감 작업 | 발표 자료, 시연 데이터, 최종 정리 작업이다. |

## 4. 작업 우선도 리스트

| 우선순위 | ID | 작업 | 산출물 | 의존성 | 상태 |
| --- | --- | --- | --- | --- | --- |
| P0 | WJ-001 | 전체 화면 흐름 확정 | `screen_flow.md`, `wireframe.md` | 없음 | 완료 |
| P0 | WJ-002 | 와이어프레임 문서화 및 이미지 정리 | `wireframe.md`, `assets/` | 화면 설계서 | 완료 |
| P0 | WJ-003 | 상태/오류/접근성/QA 기준 확정 | `wireframe.md`, `non_functional_requirements.md` | 와이어프레임 | 완료 |
| P0 | WJ-004 | Android Activity 라우팅 구조 설계 | `AndroidManifest.xml`, Activity 화면 목록 | 폴더 구조 설계 | 완료 |
| P0 | WJ-005 | 공통 UI 리소스 기준 확정 | drawable, colors, dimens, BaseActivity 기준 | 와이어프레임 | 완료 |
| P1 | WJ-006 | SplashScreen UI 및 로그인 상태 분기 연결 지원 | `SplashActivity.java`, `activity_splash.xml` | Auth 담당 작업 | Firebase Auth 연결 |
| P1 | WJ-007 | HomeScreen UI 구현 기준 정리 및 연결 지원 | `HomeActivity.java`, `activity_home.xml` | Firestore 조회 함수 | Firestore 최근 기록/통계 연결 |
| P1 | WJ-008 | StudyInputScreen, SummaryResultScreen UI 일관성 검수 | 화면 상태, 입력 검증, 로딩 표시 | AI 담당 작업 | 구현 완료 / 실기기 QA 필요 |
| P1 | WJ-009 | QuizScreen, QuizResultScreen UI 일관성 검수 | 보기 선택 상태, 결과 표시 | 퀴즈 담당 작업 | 구현 완료 / 실기기 QA 필요 |
| P1 | WJ-010 | WrongAnswerScreen, MyPageScreen 하단 탭 흐름 검수 | 오답노트, 마이페이지 이동 | 퀴즈/DB 담당 작업 | 구현 완료 / 실기기 QA 필요 |
| P2 | WJ-011 | 공통 오류 메시지와 빈 상태 적용 확인 | 오류/빈 상태 체크리스트 | 각 기능 구현 | 진행 예정 |
| P2 | WJ-012 | 접근성 수동 점검 | 스크린리더, 글자 크기 확대, 터치 영역 확인 | 주요 화면 구현 | 진행 예정 |
| P2 | WJ-013 | 통합 QA 시나리오 실행 지원 | QA 결과, 수정 목록 | 기능 통합 | 진행 예정 |
| P3 | WJ-014 | 발표 자료 초안 작성 | `presentation_outline.md` 기반 슬라이드 | 화면/DB/AI 문서 | 진행 예정 |
| P3 | WJ-015 | 시연 흐름과 예비 자료 정리 | `demo_scenario.md`, 예비 스크린샷 | MVP 화면 구현 | 진행 예정 |

## 5. 현재까지 진행된 작업

| 작업 | 결과 |
| --- | --- |
| 와이어프레임 생성 프롬프트 작성 | `docs/03_design/wireframe_generation_prompt.md` 추가 완료 |
| 와이어프레임 문서 작성 | `docs/03_design/wireframe.md` 추가 완료 |
| 와이어프레임 이미지 파일명 정리 | `wireframe_01_auth_home.png`, `wireframe_02_study_quiz.png`, `wireframe_03_wrong_mypage_states.png` 정리 완료 |
| 접근성 기준 반영 | `wireframe.md`, `non_functional_requirements.md`에 반영 완료 |
| 상태/오류/QA/디자인 토큰 보강 | `wireframe.md`에 반영 완료 |
| Android Activity 라우팅 구조 설계 | 본 문서의 `8. 라우팅 구조 설계`와 실제 Java Activity에 반영 완료 |
| 공통 UI 리소스 기준 확정 | 본 문서의 `9. 공통 UI 리소스 기준`과 `res/drawable`, `res/values`에 반영 완료 |
| Splash/Home 화면 골격 구현 | `SplashActivity`, `LoginActivity`, `SignUpActivity`, `HomeActivity` 구현 완료 |
| 핵심 플로우 화면 구현 | 입력, 요약, 퀴즈, 결과, 오답, 마이페이지 Activity 구현 및 서비스 연결 완료 |

## 6. 바로 다음 작업 순서

1. Firebase Functions 배포 URL을 `local.properties`의 `ai.base.url`에 설정하고 앱을 리빌드한다.
2. 실제 기기 또는 에뮬레이터에서 회원가입, 이메일 인증, 로그인 흐름을 확인한다.
3. 정상 학습 내용으로 요약, 퀴즈, 결과, 오답 저장까지 한 번에 시연한다.
4. HomeScreen 최근 기록, MyPageScreen 통계, WrongAnswerScreen 저장 오답 조회를 확인한다.
5. `WJ-012` 접근성 수동 점검 기준에 맞춰 주요 화면을 검수한다.

현재 저장소에는 Android Java 프로젝트가 생성되어 있으며, 우지훈 담당 화면 흐름은 Java Activity와 XML layout으로 구현되어 있다. Firebase, AI, Firestore 연동 코드는 들어가 있으므로 남은 작업은 실제 배포 환경과 수동 QA 확인이다.

## 7. 협업 요청 사항

| 대상 | 요청 내용 | 이유 |
| --- | --- | --- |
| 최백도 | AuthService, FirestoreService 함수명과 반환 타입 공유 | SplashActivity, LoginActivity, HomeActivity 연결에 필요 |
| 윤재이 | AiService 요약/퀴즈 로딩, 실패, 파싱 실패 상태 값 공유 | StudyInputActivity, SummaryResultActivity 상태 처리에 필요 |
| 강도현 | QuizService 선택 답안 상태와 결과 모델 공유 | QuizActivity, QuizResultActivity UI 연결에 필요 |

## 8. 라우팅 구조 설계

Android Java 프로젝트에서 `Activity`와 `Intent`를 기준으로 아래 라우팅을 적용한다.

| 출발 | 도착 Activity | 진입 조건 | 비고 |
| --- | --- | --- | --- |
| 앱 실행 | SplashActivity | Launcher Activity | 로그인 상태 확인 후 분기 |
| SplashActivity | LoginActivity | 비로그인 상태 | Firebase Auth 상태 기준 |
| SplashActivity | HomeActivity | 로그인 상태 | Firebase Auth 상태 기준 |
| LoginActivity | SignUpActivity | 회원가입 링크 클릭 | 가입 성공 후 LoginActivity 복귀 |
| LoginActivity | HomeActivity | 로그인 성공 | AuthService 연동 |
| HomeActivity | StudyInputActivity | 오늘의 학습 시작 클릭 | 제목, 과목명, 학습 내용 입력 |
| StudyInputActivity | SummaryResultActivity | AI 요약 성공 | AiService 연동 |
| SummaryResultActivity | QuizActivity | 퀴즈 생성 성공 | AiService/QuizModel 연동 |
| QuizActivity | QuizResultActivity | 마지막 문제 완료 | correctCount, totalCount, userAnswers, quizList 전달 |
| QuizResultActivity | WrongAnswerActivity | 오답 상세 보기 클릭 | WrongAnswerModel 전달 |
| HomeActivity | WrongAnswerActivity | 하단 탭 클릭 | userId 기준 오답 조회 |
| HomeActivity | MyPageActivity | 하단 탭 클릭 | 사용자 정보, 로그아웃 |

### 라우팅 처리 기준

- Launcher Activity는 SplashActivity로 둔다.
- 인증 상태 확인은 SplashActivity에서 처리한다.
- 하단 탭은 HomeActivity, WrongAnswerActivity, MyPageActivity에만 노출한다.
- 학습 생성 흐름인 StudyInputActivity, SummaryResultActivity, QuizActivity, QuizResultActivity는 하단 탭보다 상단 뒤로가기와 주요 CTA 중심으로 이동한다.
- AI 요약 실패, 퀴즈 생성 실패 시 화면 이동하지 않고 현재 화면에서 오류와 재시도 버튼을 표시한다.
- QuizActivity 중간 이탈 저장은 MVP 범위에서 제외한다.

## 9. 공통 UI 리소스 기준

| 리소스/클래스 | 역할 | 적용 위치 |
| --- | --- | --- |
| BaseActivity.java | 화면 이동, 공통 Toast | 모든 Activity |
| bg_button_primary.xml | 주요 CTA 버튼 | 로그인, 학습 시작, 퀴즈 생성 |
| bg_button_secondary.xml | 보조 버튼 | 홈으로, 다시 풀기 |
| bg_button_danger.xml | 위험 동작 버튼 | 로그아웃 |
| bg_card.xml | 카드 배경 | 통계, 최근 기록, 오답 |
| bg_input.xml | 입력 필드 배경 | 로그인, 회원가입, 학습 입력 |
| bg_option.xml | 퀴즈 보기 기본 상태 | QuizActivity |
| bg_option_selected.xml | 퀴즈 보기 선택 상태 | QuizActivity |
| colors.xml | 다크 테마 색상 | 전체 화면 |
| dimens.xml | 버튼 높이, 여백, 카드 radius | 전체 화면 |

### 공통 UI 적용 기준

- 주요 버튼은 기본 높이 56dp, 보조 버튼은 48dp 기준으로 한다.
- EditText는 placeholder만 사용하지 않고 항상 보이는 라벨을 함께 배치한다.
- 오류 상태는 빨간색만 사용하지 않고 경고 아이콘과 문구를 함께 제공한다.
- 빈 상태는 안내와 다음 행동 CTA를 함께 제공한다.
- 하단 탭은 현재 탭을 색상과 라벨 굵기로 함께 구분한다.
- 퀴즈 보기는 선택됨, 정답, 오답 상태를 색상 외에도 라벨과 테두리로 구분한다.

## 10. 확인 체크리스트

- [ ] 우지훈 담당 작업이 P0, P1, P2, P3로 구분되어 있는가?
- [ ] 팀 전체 작업을 막는 P0 작업이 먼저 배치되어 있는가?
- [ ] 문서 완료 작업과 구현 예정 작업이 구분되어 있는가?
- [ ] 다른 담당자에게 요청해야 할 인터페이스가 정리되어 있는가?
- [ ] 다음 작업 순서가 실제 구현 착수 순서와 맞는가?
- [ ] 라우팅 구조와 공통 UI 리소스 기준이 실제 Android Java 구현에 바로 사용할 수 있는가?

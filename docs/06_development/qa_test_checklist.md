# QA 테스트 체크리스트

## 1. 문서 목적

이 문서는 StudyMate MVP의 발표 전 품질 확인을 위해 자동 검증, 수동 QA, 시연 준비 항목을 한 곳에서 관리한다.

테스트 기준 브랜치는 `main`이며, 테스트 전 최신 원격 상태를 반영한다.

```bash
git switch main
git pull --ff-only origin main
```

## 2. 자동 검증

자동 검증은 코드 변경 후 반드시 먼저 실행한다.

| ID | 검증 항목 | 명령 | 기대 결과 | 상태 |
| --- | --- | --- | --- | --- |
| AUTO-001 | Android Debug 빌드 | `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ANDROID_HOME=/Users/jihun/Library/Android/sdk ./gradlew --no-daemon assembleDebug` | `BUILD SUCCESSFUL` | 통과 |
| AUTO-002 | Android Lint | `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ANDROID_HOME=/Users/jihun/Library/Android/sdk ./gradlew --no-daemon lintDebug` | `BUILD SUCCESSFUL` | 통과 |
| AUTO-003 | Android 단위 테스트 | `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ANDROID_HOME=/Users/jihun/Library/Android/sdk ./gradlew --no-daemon testDebugUnitTest` | `BUILD SUCCESSFUL` | 통과 |
| AUTO-004 | Firestore 보안 규칙 테스트 | `npm --prefix tests/firestore-rules ci` 후 `npm run test:firestore-rules` | 전체 테스트 통과 | 통과 |
| AUTO-005 | Firestore rules 의존성 감사 | `npm run audit:firestore-rules` | 취약점 없음 또는 허용 기준 이하 | 미실행 |

최근 확인 결과:

- `assembleDebug`: 통과
- `lintDebug`: 통과
- `testDebugUnitTest`: 통과
- Firestore rules: `10 pass / 0 fail`

## 3. 작업자별 테스트 체크리스트

테스트 실행은 팀원이 함께 진행한다. 아래 구분은 테스트 실행 담당자가 아니라 기능 오너 기준의 확인 범위다.

### 최백도

- [ ] `QA-001` 회원가입, 이메일 인증, 로그인 후 HomeScreen 이동을 확인한다. 위험도: High
- [ ] `QA-002` 잘못된 비밀번호 입력 시 오류 메시지와 입력값 유지 여부를 확인한다. 위험도: Medium
- [ ] `QA-003` 로그아웃 후 앱 재실행 시 LoginScreen으로 이동하는지 확인한다. 위험도: High
- [ ] `QA-014` 오답 풀이 후 Firestore `wrong_answers`에 로그인 사용자 소유 문서가 생성되는지 확인한다. 위험도: High
- [ ] `QA-017` 학습 완료 후 HomeScreen의 최근 학습 기록 또는 학습 수치 갱신 여부를 확인한다. 위험도: Medium
- [ ] `QA-018` MyPageScreen의 사용자 이메일과 로그아웃 동선을 확인한다. 위험도: Medium

### 윤재이

- [ ] `QA-004` 제목 없이 요약 생성을 시도했을 때 제목 입력 안내가 표시되는지 확인한다. 위험도: Medium
- [ ] `QA-005` 학습 내용 30자 미만 입력 시 AI 요청 차단과 입력 부족 안내를 확인한다. 위험도: Medium
- [ ] `QA-006` 학습 내용 5000자 초과 입력 시 AI 요청 차단과 길이 초과 안내를 확인한다. 위험도: Medium
- [ ] `QA-007` 정상 학습 내용으로 요약 생성 시 SummaryResultScreen에 요약과 키워드가 표시되는지 확인한다. 위험도: High
- [ ] `QA-008` 네트워크/API 실패 시 로딩 종료, 오류 메시지, 입력값 유지 여부를 확인한다. 위험도: Medium

### 강도현

- [ ] `QA-009` SummaryResultScreen에서 퀴즈 생성 시 QuizScreen에 4지선다 문제 3개가 표시되는지 확인한다. 위험도: High
- [ ] `QA-010` 보기를 선택하지 않은 상태에서 다음 버튼이 비활성화되는지 확인한다. 위험도: Medium
- [ ] `QA-011` 보기를 선택한 뒤 다음 문제로 이동할 때 진행도와 문제/보기가 갱신되는지 확인한다. 위험도: High
- [ ] `QA-012` 일부 오답이 있을 때 정답률, 틀린 문제 수, 저장 중/성공 상태가 표시되는지 확인한다. 위험도: High
- [ ] `QA-013` 모든 문제를 맞혔을 때 오답 없음 문구, 비활성화된 오답 버튼, `저장할 오답이 없습니다.` 문구가 표시되는지 확인한다. 위험도: Medium
- [ ] `QA-014` 일부 오답 풀이 후 Firestore `wrong_answers` 저장 결과를 최백도와 함께 확인한다. 위험도: High
- [ ] `QA-015` 오답노트 확인 버튼 클릭 시 WrongAnswerScreen에서 내 답, 정답, 해설이 구분되어 표시되는지 확인한다. 위험도: Medium
- [ ] `QA-016` 오답 없이 WrongAnswerScreen에 비정상 진입했을 때 오답 0/0 빈 상태와 돌아가기 버튼이 표시되는지 확인한다. 위험도: Low

### 우지훈

- [ ] `QA-017` 학습 완료 후 HomeScreen의 최근 학습 기록 또는 학습 수치 갱신 여부를 최백도와 함께 확인한다. 위험도: Medium
- [ ] `QA-018` MyPageScreen의 사용자 이메일과 로그아웃 동선을 최백도와 함께 확인한다. 위험도: Medium
- [ ] `QA-019` 시스템 글자 크기를 크게 설정했을 때 주요 화면 텍스트가 잘리지 않고 스크롤 가능한지 확인한다. 위험도: Low
- [ ] `QA-020` 스크린리더로 QuizScreen을 탐색했을 때 문제, 보기 번호, 선택 상태, 다음 버튼이 의미 있게 읽히는지 확인한다. 위험도: Low

## 4. 수동 QA 시나리오

수동 QA는 실제 기기 또는 Android Emulator에서 팀원이 함께 진행한다. 네트워크는 AI API 호출이 가능한 상태여야 한다.

위험도 기준:

- High: 발표 핵심 플로우를 차단하거나 데이터 저장/권한에 영향을 주는 항목
- Medium: 주요 기능 품질에 영향을 주지만 우회가 가능한 항목
- Low: 보조 흐름, 사용성, 접근성 보완 항목

| ID | 영역 | 위험도 | 시나리오 | 기대 결과 | 기능 오너 | 상태 |
| --- | --- | --- | --- | --- | --- | --- |
| QA-001 | 인증 | High | 신규 사용자가 회원가입 후 이메일 인증을 완료하고 로그인한다. | HomeScreen으로 이동한다. | 최백도 | 미확인 |
| QA-002 | 인증 | Medium | 로그인 화면에서 잘못된 비밀번호를 입력한다. | 오류 메시지가 표시되고 입력값이 유지된다. | 최백도 | 미확인 |
| QA-003 | 인증 | High | 로그아웃 후 앱을 다시 실행한다. | SplashScreen 이후 LoginScreen으로 이동한다. | 최백도 | 미확인 |
| QA-004 | 학습 입력 | Medium | 제목 없이 요약 생성을 시도한다. | 제목 입력 안내가 표시된다. | 윤재이 | 미확인 |
| QA-005 | 학습 입력 | Medium | 학습 내용 30자 미만으로 요약 생성을 시도한다. | AI 요청이 차단되고 입력 부족 안내가 표시된다. | 윤재이 | 구현 확인 |
| QA-006 | 학습 입력 | Medium | 학습 내용 5000자 초과로 요약 생성을 시도한다. | AI 요청이 차단되고 길이 초과 안내가 표시된다. | 윤재이 | 구현 확인 |
| QA-007 | AI 요약 | High | 정상 학습 내용을 입력해 요약을 생성한다. | SummaryResultScreen에 요약과 키워드가 표시된다. | 윤재이 | 미확인 |
| QA-008 | AI 오류 | Medium | AI 요약 요청 중 네트워크/API 실패를 발생시킨다. | 로딩이 종료되고 오류 메시지가 표시되며 입력값이 유지된다. | 윤재이 | 미확인 |
| QA-009 | 퀴즈 생성 | High | SummaryResultScreen에서 퀴즈 생성을 실행한다. | QuizScreen에 4지선다 문제 3개가 표시된다. | 강도현 | 미확인 |
| QA-010 | 퀴즈 풀이 | Medium | 보기를 선택하지 않은 상태를 확인한다. | 다음 버튼이 비활성화되어 이동하지 않는다. | 강도현 | 구현 확인 |
| QA-011 | 퀴즈 풀이 | High | 각 문제에서 보기를 선택한 뒤 다음 문제로 이동한다. | 진행도와 문제/보기가 정상 갱신된다. | 강도현 | 미확인 |
| QA-012 | 결과 | High | 3문제 중 일부를 틀린다. | 정답률, 틀린 문제 수, 저장 중/성공 상태가 표시된다. | 강도현 | 미확인 |
| QA-013 | 결과 | Medium | 모든 문제를 맞힌다. | 오답 없음 문구, 비활성화된 오답 버튼, `저장할 오답이 없습니다.` 문구가 표시된다. | 강도현 | 구현 확인 |
| QA-014 | 오답 저장 | High | 일부 오답 풀이 후 Firestore `wrong_answers`를 확인한다. | 로그인 사용자 소유의 오답 문서가 생성된다. | 최백도 / 강도현 | 미확인 |
| QA-015 | 오답 상세 | Medium | 오답노트 확인 버튼을 누른다. | WrongAnswerScreen에서 내 답, 정답, 해설이 구분되어 표시된다. | 강도현 | 미확인 |
| QA-016 | 빈 상태 | Low | 오답이 없는 상태에서 WrongAnswerScreen에 비정상 진입한다. | 오답 0/0 빈 상태와 돌아가기 버튼이 표시된다. | 강도현 | 구현 확인 |
| QA-017 | 홈/기록 | Medium | 학습 완료 후 HomeScreen으로 돌아간다. | 최근 학습 기록 또는 학습 수치가 갱신된다. | 우지훈 / 최백도 | 미확인 |
| QA-018 | 마이페이지 | Medium | MyPageScreen으로 이동한다. | 사용자 이메일과 로그아웃 동선이 확인된다. | 우지훈 / 최백도 | 미확인 |
| QA-019 | 접근성 | Low | 시스템 글자 크기를 크게 설정하고 주요 화면을 확인한다. | 버튼, 카드, 요약, 해설 텍스트가 잘리지 않고 스크롤 가능하다. | 우지훈 | 미확인 |
| QA-020 | 접근성 | Low | 스크린리더로 QuizScreen을 탐색한다. | 문제, 보기 번호, 선택 상태, 다음 버튼이 의미 있게 읽힌다. | 우지훈 | 미확인 |

## 5. 시연 준비 체크리스트

| ID | 항목 | 기준 | 상태 |
| --- | --- | --- | --- |
| DEMO-001 | 발표용 계정 | 이메일 인증 완료 계정 1개 | 미확인 |
| DEMO-002 | 시연용 입력 데이터 | 30자 이상, 1~2문단 수준 | 준비됨 |
| DEMO-003 | AI API 호출 환경 | 네트워크와 Functions/API Key 정상 | 미확인 |
| DEMO-004 | 일부러 틀릴 문제 | 오답노트 저장 시연용 문제 1개 이상 | 미확인 |
| DEMO-005 | API 실패 대비 자료 | 스크린샷 또는 더미 응답 | 미확인 |
| DEMO-006 | Firebase 데이터 초기화 | 발표 전 테스트 데이터 정리 | 미확인 |

시연용 입력 예시는 다음을 사용한다.

```text
제목: 데이터베이스 기본키 정리
과목명: 데이터베이스
학습 내용:
기본키는 데이터베이스 테이블에서 각 행을 고유하게 식별하기 위해 사용하는 속성이다. 기본키 값은 중복될 수 없으며 NULL 값을 가질 수 없다. 하나의 테이블에는 기본키가 하나만 존재하며, 기본키는 개체 무결성을 유지하는 데 중요한 역할을 한다.
```

## 6. 결함 기록 양식

수동 QA 중 발견한 문제는 아래 형식으로 기록한다.

| 항목 | 내용 |
| --- | --- |
| 결함 ID | BUG-YYYYMMDD-번호 |
| 발견 화면 | 예: QuizResultScreen |
| 재현 절차 | 1. ... 2. ... 3. ... |
| 기대 결과 | 정상 동작 기준 |
| 실제 결과 | 관찰된 문제 |
| 심각도 | High / Medium / Low |
| 담당자 | 이름 |
| 상태 | Open / In Progress / Fixed / Verified |
| 관련 커밋/PR | 링크 또는 커밋 해시 |

## 7. 완료 기준

발표 전 완료 기준은 다음과 같다.

- 자동 검증 `AUTO-001`부터 `AUTO-004`까지 모두 통과한다.
- `QA-001`부터 `QA-018`까지 수동 테스트 결과가 통과 또는 허용 가능한 이슈로 정리된다.
- `QA-019`, `QA-020` 접근성 항목은 최소 1회 이상 확인하고, 치명적인 잘림/탐색 불가 문제가 없어야 한다.
- `DEMO-001`부터 `DEMO-006`까지 시연 준비가 완료된다.
- High 심각도 결함이 남아 있지 않다.

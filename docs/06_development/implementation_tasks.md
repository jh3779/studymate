# 구현 작업 목록

## 1. 문서 목적

이 문서는 StudyMate MVP 구현을 위한 작업 항목을 개발자가 바로 참고할 수 있도록 기능 단위로 정리한다.

## 2. 핵심 내용

팀 단위 담당자는 [팀 작업 분담표](team_task_distribution.md)를 기준으로 배정한다.

| 단계 | 작업 묶음 | 우선순위 |
| --- | --- | --- |
| 1 | 프로젝트 세팅 | 필수 |
| 2 | 인증 구현 | 필수 |
| 3 | 학습 기록 구현 | 필수 |
| 4 | AI 요약 구현 | 필수 |
| 5 | AI 퀴즈 구현 | 필수 |
| 6 | 퀴즈 풀이 및 결과 구현 | 필수 |
| 7 | 오답노트 구현 | 필수 |
| 8 | 마이페이지 및 마감 정리 | 권장 |

## 3. 상세 설명

### 1. 프로젝트 세팅

- Flutter 프로젝트 생성
- Firebase 프로젝트 생성
- Android, iOS Firebase 설정 파일 연결
- `lib/` 폴더 구조 생성
- 공통 테마와 라우팅 설정

### 2. 인증 구현

- AuthService 작성
- 회원가입 함수 구현: `signUp(email, password)`
- 로그인 함수 구현: `signIn(email, password)`
- 로그아웃 함수 구현: `signOut()`
- 현재 사용자 확인 함수 구현: `getCurrentUserId()`
- SplashScreen 로그인 상태 분기 구현

### 3. 학습 기록 구현

- StudyNoteModel 작성
- StudyInputScreen 입력 UI 구현
- 제목과 학습 내용 검증 구현
- FirestoreService `saveStudyNote(note)` 구현
- FirestoreService `getStudyNotes(userId)` 구현
- HomeScreen 최근 학습 기록 표시

### 4. AI 요약 구현

- AIService `generateSummary(text)` 구현
- 요약 생성 프롬프트 적용
- AI API 요청 처리
- JSON 응답 파싱
- SummaryResultScreen UI 구현
- summary, keywords 저장 처리

### 5. AI 퀴즈 구현

- QuizModel 작성
- AIService `generateQuizzes(text)` 구현
- 퀴즈 생성 프롬프트 적용
- JSON 배열 파싱
- FirestoreService `saveQuizzes(quizzes)` 구현
- FirestoreService `getQuizzesByNoteId(noteId)` 구현

### 6. 퀴즈 풀이 및 결과 구현

- QuizScreen 문제 표시 구현
- 보기 선택 상태 관리
- 다음 문제 이동 구현
- QuizService `checkAnswer(quiz, selectedIndex)` 구현
- QuizService `calculateScore(quizzes, selectedAnswers)` 구현
- QuizResultModel 작성
- FirestoreService `saveQuizResult(result)` 구현
- QuizResultScreen UI 구현

### 7. 오답노트 구현

- WrongAnswerModel 작성
- QuizService `extractWrongAnswers()` 구현
- FirestoreService `saveWrongAnswer(wrongAnswer)` 구현
- FirestoreService `getWrongAnswers(userId)` 구현
- WrongAnswerScreen 목록 및 상세 표시 구현
- 다시 풀기 기능 구현

### 8. 마이페이지 및 마감 정리

- MyPageScreen 구현
- 사용자 이메일 표시
- 총 학습 기록 수 표시
- 총 퀴즈 풀이 수 표시
- 로그아웃 버튼 연결
- 전체 UI 정리
- 오류 메시지 정리
- 발표 시연 데이터 준비

## 4. 개발 시 참고사항

- 각 작업은 완료 후 최소한 화면 이동과 Firestore 저장 여부를 확인한다.
- AI 기능 구현 전에는 더미 데이터로 화면을 먼저 검증할 수 있다.
- API Key는 소스 코드에 넣지 않는다.
- 퀴즈 JSON 파싱 실패를 반드시 테스트한다.
- 오답 저장은 결과 저장 이후 한 번만 실행되도록 중복 저장을 주의한다.

## 5. 확인 체크리스트

- [ ] 인증, 학습, AI, 퀴즈, 오답 작업이 분리되어 있는가?
- [ ] 필요한 모델과 서비스 함수가 모두 포함되어 있는가?
- [ ] 화면 구현 작업과 데이터 저장 작업이 연결되어 있는가?
- [ ] AI API 보안 주의사항이 반영되어 있는가?
- [ ] 발표 전 테스트 작업까지 포함되어 있는가?

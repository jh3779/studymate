# Flutter 폴더 구조 설계서

## 1. 문서 목적

이 문서는 StudyMate Flutter 프로젝트의 기본 폴더 구조와 각 파일의 역할을 정의한다.

## 2. 핵심 내용

```text
lib/
 ├─ main.dart
 ├─ app.dart
 │
 ├─ models/
 │   ├─ user_model.dart
 │   ├─ study_note_model.dart
 │   ├─ quiz_model.dart
 │   ├─ quiz_result_model.dart
 │   └─ wrong_answer_model.dart
 │
 ├─ screens/
 │   ├─ splash_screen.dart
 │   ├─ login_screen.dart
 │   ├─ signup_screen.dart
 │   ├─ home_screen.dart
 │   ├─ study_input_screen.dart
 │   ├─ summary_result_screen.dart
 │   ├─ quiz_screen.dart
 │   ├─ quiz_result_screen.dart
 │   ├─ wrong_answer_screen.dart
 │   └─ my_page_screen.dart
 │
 ├─ services/
 │   ├─ auth_service.dart
 │   ├─ firestore_service.dart
 │   ├─ ai_service.dart
 │   └─ quiz_service.dart
 │
 ├─ providers/
 │   ├─ auth_provider.dart
 │   ├─ study_provider.dart
 │   └─ quiz_provider.dart
 │
 ├─ widgets/
 │   ├─ common_button.dart
 │   ├─ study_card.dart
 │   ├─ quiz_option_card.dart
 │   └─ loading_dialog.dart
 │
 └─ utils/
     ├─ constants.dart
     └─ validators.dart
```

## 3. 상세 설명

### 루트 파일

| 파일 | 역할 |
| --- | --- |
| main.dart | Firebase 초기화, 앱 실행 진입점 |
| app.dart | MaterialApp, 테마, 라우팅 설정 |

### models

| 파일 | 모델 | 주요 필드 |
| --- | --- | --- |
| user_model.dart | UserModel | id, email, nickname, createdAt |
| study_note_model.dart | StudyNoteModel | id, userId, title, subject, originalText, summary, keywords, createdAt |
| quiz_model.dart | QuizModel | id, noteId, userId, question, options, answerIndex, explanation, createdAt |
| quiz_result_model.dart | QuizResultModel | id, userId, noteId, totalCount, correctCount, score, createdAt |
| wrong_answer_model.dart | WrongAnswerModel | id, userId, quizId, noteId, selectedIndex, correctIndex, question, options, explanation, createdAt |

### screens

화면 단위 UI를 작성한다. 화면 파일은 가능하면 입력 처리와 화면 표시 위주로 유지하고, Firebase 또는 AI API 직접 호출은 services 또는 providers로 분리한다.

### services

| 파일 | 역할 |
| --- | --- |
| auth_service.dart | Firebase Authentication 연동 |
| firestore_service.dart | Firestore 데이터 저장 및 조회 |
| ai_service.dart | AI 요약, AI 퀴즈 생성, JSON 파싱 |
| quiz_service.dart | 답안 확인, 점수 계산, 오답 추출 |

### providers

| 파일 | 역할 |
| --- | --- |
| auth_provider.dart | 로그인 상태, 사용자 정보 상태 관리 |
| study_provider.dart | 학습 입력, 요약 결과, 학습 기록 상태 관리 |
| quiz_provider.dart | 퀴즈 목록, 선택 답안, 점수 상태 관리 |

### widgets

공통 UI 컴포넌트를 작성한다. 버튼, 학습 카드, 퀴즈 보기 카드, 로딩 다이얼로그처럼 여러 화면에서 재사용되는 요소를 분리한다.

### utils

| 파일 | 역할 |
| --- | --- |
| constants.dart | 컬렉션명, 라우트명, 공통 문자열, 제한값 관리 |
| validators.dart | 이메일, 비밀번호, 학습 내용 길이 검증 |

## 4. 개발 시 참고사항

- Firestore 컬렉션명은 `constants.dart`에 상수로 관리한다.
- 모델에는 Firestore 변환용 `fromMap`, `toMap` 같은 메서드가 필요하다.
- 서비스 파일은 UI 위젯에 의존하지 않아야 한다.
- MVP에서는 폴더 구조를 과도하게 세분화하지 않는다.

## 5. 확인 체크리스트

- [ ] 문서의 폴더 구조가 실제 Flutter 프로젝트 생성 시 반영 가능한가?
- [ ] 모델, 화면, 서비스, 상태 관리 책임이 분리되어 있는가?
- [ ] Firestore 스키마와 모델 필드가 일치하는가?
- [ ] 공통 위젯이 중복 UI를 줄일 수 있는가?
- [ ] MVP 수준에서 과도하게 복잡하지 않은가?

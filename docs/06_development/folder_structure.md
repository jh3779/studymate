# Android Java 폴더 구조 설계서

## 1. 문서 목적

이 문서는 StudyMate Android Java 프로젝트의 기본 폴더 구조와 각 파일의 역할을 정의한다.

## 2. 핵심 내용

```text
app/
 └─ src/main/
    ├─ AndroidManifest.xml
    │
    ├─ java/com/example/studymate/
    │  ├─ BaseActivity.java
    │  ├─ SplashActivity.java
    │  ├─ LoginActivity.java
    │  ├─ SignUpActivity.java
    │  ├─ HomeActivity.java
    │  ├─ StudyInputActivity.java
    │  ├─ SummaryResultActivity.java
    │  ├─ QuizActivity.java
    │  ├─ QuizResultActivity.java
    │  ├─ WrongAnswerActivity.java
    │  ├─ MyPageActivity.java
    │  │
    │  ├─ model/
    │  │  ├─ UserModel.java
    │  │  ├─ StudyNoteModel.java
    │  │  ├─ QuizModel.java
    │  │  ├─ QuizResultModel.java
    │  │  └─ WrongAnswerModel.java
    │  │
    │  ├─ service/
    │  │  ├─ AuthService.java
    │  │  ├─ FirestoreService.java
    │  │  ├─ AiService.java
    │  │  └─ QuizService.java
    │  │
    │  └─ util/
    │     ├─ AppConstants.java
    │     └─ Validators.java
    │
    └─ res/
       ├─ layout/
       │  ├─ activity_splash.xml
       │  ├─ activity_login.xml
       │  ├─ activity_signup.xml
       │  ├─ activity_home.xml
       │  ├─ activity_study_input.xml
       │  ├─ activity_summary_result.xml
       │  ├─ activity_quiz.xml
       │  ├─ activity_quiz_result.xml
       │  ├─ activity_wrong_answer.xml
       │  └─ activity_my_page.xml
       │
       ├─ drawable/
       │  ├─ bg_button_primary.xml
       │  ├─ bg_button_secondary.xml
       │  ├─ bg_card.xml
       │  ├─ bg_input.xml
       │  └─ bg_option_selected.xml
       │
       └─ values/
          ├─ colors.xml
          ├─ dimens.xml
          ├─ strings.xml
          └─ themes.xml
```

## 3. 상세 설명

### Activity

화면 단위 UI와 사용자 입력 처리를 담당한다. Firebase 또는 AI API 직접 호출은 최소화하고, 실제 구현 단계에서는 service 클래스를 통해 호출한다.

### model

Firestore 문서와 앱 내부 데이터 객체를 정의한다.

| 파일 | 모델 | 주요 필드 |
| --- | --- | --- |
| UserModel.java | UserModel | id, email, nickname, createdAt |
| StudyNoteModel.java | StudyNoteModel | id, userId, title, subject, originalText, summary, keywords, createdAt |
| QuizModel.java | QuizModel | id, noteId, userId, question, options, answerIndex, explanation, createdAt |
| QuizResultModel.java | QuizResultModel | id, userId, noteId, totalCount, correctCount, score, createdAt |
| WrongAnswerModel.java | WrongAnswerModel | id, userId, quizId, noteId, selectedIndex, correctIndex, question, options, explanation, createdAt |

### service

| 파일 | 역할 |
| --- | --- |
| AuthService.java | Firebase Authentication 연동 |
| FirestoreService.java | Firestore 데이터 저장 및 조회 |
| AiService.java | AI 요약, AI 퀴즈 생성, JSON 파싱 |
| QuizService.java | 답안 확인, 점수 계산, 오답 추출 |

### util

| 파일 | 역할 |
| --- | --- |
| AppConstants.java | 컬렉션명, Intent extra key, 공통 문자열, 제한값 관리 |
| Validators.java | 이메일, 비밀번호, 학습 내용 길이 검증 |

### res

| 폴더 | 역할 |
| --- | --- |
| layout | Activity 화면 XML |
| drawable | 버튼, 카드, 입력 필드, 선택 상태 배경 |
| values | 색상, 치수, 문자열, 테마 |

## 4. 개발 시 참고사항

- Activity는 화면 표시와 이벤트 연결 위주로 유지한다.
- Firebase와 AI API 호출은 service 계층으로 분리한다.
- Firestore 컬렉션명은 `AppConstants.java`에 상수로 관리한다.
- 모델에는 Firestore 변환용 `toMap`, `fromDocument` 계열 메서드가 필요하다.
- MVP에서는 Activity 기반 구조로 단순하게 시작하고, 필요 시 Fragment 또는 ViewModel 구조를 추후 도입한다.

## 5. 확인 체크리스트

- [ ] 문서의 폴더 구조가 실제 Android Java 프로젝트와 일치하는가?
- [ ] Activity, model, service, util 책임이 분리되어 있는가?
- [ ] Firestore 스키마와 Java 모델 필드가 일치하는가?
- [ ] 공통 drawable과 values 리소스가 중복 UI를 줄일 수 있는가?
- [ ] MVP 수준에서 과도하게 복잡하지 않은가?
